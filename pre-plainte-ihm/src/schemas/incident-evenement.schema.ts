import { z } from "zod";
import type { ComposerTranslation } from "vue-i18n";
import {
  isCybercrimeTypeWithoutDetailFields,
  NUMERO_IMEI_MAX_LENGTH,
  RIPOL,
  TEXT_FIELD_MAX_LENGTH, TEXTAREA_MAX_LENGTH, VEHICULE_CATEGORIES_AVEC_PLAQUE
} from "@/constants/constant";
import { isValidBoundedDate, parseDate, parseTime } from "@/utils/helpers/dateHelpers.ts";
import { validateAchatNonRecuCybercrime } from "@/schemas/incident-evenement-achat-non-recu-refine";
import { validateCommandeFrauduleuseCybercrime } from "@/schemas/incident-evenement-commande-frauduleuse-refine";
import { isUrlWebAvecDomaine } from "@/utils/validations/field-validation.utils";
import { requiresConstatQuestion } from "@/utils/workflows/disclaimer-workflow";

const MIN_ADRESSE_EVENEMENT_TAILLE = 5;
const VALIDATION_FORMAT_DATE_INVALIDE = "validation.formatDateInvalide";
const VALIDATION_FORMAT_HEURE_INVALIDE = "validation.formatHeureInvalide";
const VALIDATION_LONGUEUR_MAX = "validation.longueurMax";

const NUMERO_IMEI_REGEX = /^\d{15}$/;
const PLAQUE_SUISSE_PATTERN = /^[A-Z]{2}\s\d{1,6}$/;
const PLAQUE_FRANCE_SIV_PATTERN = /^[A-Z]{2}-\d{3}-[A-Z]{2}$/;
const PLAQUE_FRANCE_FNI_PATTERN = /^\d{1,4}\s[A-Z]{1,3}\s(2A|2B|\d{2,3})$/;
const PLAQUE_INTERNATIONALE_PATTERN = /^[A-Z\d]{1,12}$/;

const CATEGORIES_AVEC_NUMERO_SERIE = [
  "telephone",
  "informatique",
  "photo_video",
] as const;

const optionalStringFromForm = (t: ComposerTranslation) =>
  z.preprocess(
    v => (typeof v === "string" ? v : ""),
    z.string()
      .max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH }))
      .optional(),
  );

const optionalMontantPositifFromForm = (t: ComposerTranslation) =>
  z.preprocess(
    v => (typeof v === "string" ? v : ""),
    z
      .string()
      .max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH }))
      .refine(
        value => {
          if (!value?.trim()) {
            return true;
          }
          const parsed = Number(value);
          return Number.isFinite(parsed) && parsed >= 0;
        },
        { message: t("validation.montantPositif") },
      )
      .optional(),
  );

const optionalRipolSelectionSchema = z
  .object({
    code: z.string(),
    label: z.string(),
  })
  .nullable()
  .optional();

const createIncidentRequirements = (t: ComposerTranslation): Record<string, { field: string; message: string }[]> => ({
  vol: [
    { field: "volDansVehicule", message: t("validation.volDansVehiculeRequis") },
    { field: "categorieObjet", message: t("validation.categorieObjetRequise") },
    { field: "typeObjet", message: t("validation.typeObjetRequis") },
    { field: "couleur", message: t("validation.couleurRequise") },
    { field: "avezVousDegradation", message: t("validation.degradationsRequis") },
  ],
  "degat-delit": [
    { field: "typeDommage", message: t("validation.typeDommageRequis") },
    { field: "naturesDommage", message: t("validation.natureDommageRequis") },
    { field: "description", message: t("validation.descriptionDommageRequise") },
    { field: "constatPresent", message: t("validation.constatRequis") },
  ],
});

const isEmpty = (value: unknown) => value === undefined || value === null || value === "";

const addCustomIssue = (ctx: z.RefinementCtx, path: string | (string | number)[], message: string) => {
  ctx.addIssue({
    code: z.ZodIssueCode.custom,
    path: Array.isArray(path) ? path : [path],
    message,
  });
};

const isRipolField = (field: string) => ["typeObjet", "couleur", "fabricant", "modele"].includes(field);

const validateIncidentRequirement = (data: Record<string, any>, ctx: z.RefinementCtx, field: string, message: string) => {
  const value = data[field];

  const validators: Record<string, () => void> = {
    constatPresent: () => validateConstatPresent(value, ctx, field, message),
    dateConstat: () => validateDateConstat(data, value, ctx, field, message),
    naturesDommage: () => validateNaturesDommage(value, ctx, field, message),
    typeDommage: () => validateTypeDommage(value, ctx, field, message),
  };

  if (validators[field]) {
    validators[field]();
    return;
  }

  validateGenericField(value, ctx, field, message);
};

const validateConstatPresent = (value: any, ctx: z.RefinementCtx, field: string, message: string,) => {
  if (value === undefined || value === null) {
    addCustomIssue(ctx, field, message);
  }
};

const validateDateConstat = (data: Record<string, any>, value: any, ctx: z.RefinementCtx, field: string, message: string,) => {
  if (data.constatPresent === true && isEmpty(value)) {
    addCustomIssue(ctx, field, message);
  }
};

const validateNaturesDommage = (value: any, ctx: z.RefinementCtx, field: string, message: string,) => {
  if (value === undefined || value === null || (Array.isArray(value) && value.length === 0)) {
    addCustomIssue(ctx, field, message);
  }
};

const validateTypeDommage = (value: any, ctx: z.RefinementCtx, field: string, message: string,) => {
  if (isEmpty(value)) {
    addCustomIssue(ctx, field, message);
  }
};

const validateGenericField = (value: any, ctx: z.RefinementCtx, field: string, message: string,) => {
  if (isRipolField(field)) {
    if (!value?.code) {
      addCustomIssue(ctx, field, message);
    }
    return;
  }

  if (isEmpty(value)) {
    addCustomIssue(ctx, field, message);
  }
};

const hasObjetsVolesEnregistres = (data: Record<string, unknown>) =>
  Array.isArray(data.objetsVolesValides) && data.objetsVolesValides.length > 0;

const isVehicleVolObject = (data: Record<string, any>) => data.categorieObjet === "vehicule" || data.isVehicle === true;

const validateVehicleFields = (
  data: Record<string, any>,
  ctx: z.RefinementCtx,
  t: ComposerTranslation,
  basePath: (string | number)[] = [],
) => {
  if (!isVehicleVolObject(data)) {
    return;
  }

  if (!data.typeObjet?.code) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      path: [...basePath, "typeObjet"],
      message: t("validation.typeObjetRequis"),
    });
  }

  validateVehicleBrandAndModel(data, ctx, t, basePath);

  if (!data.couleur?.code) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      path: [...basePath, "couleur"],
      message: t("validation.couleurRequise"),
    });
  }

  validatePlaque(data, ctx, t, basePath);

}

const validateVehicleBrandAndModel = (
  data: Record<string, any>,
  ctx: z.RefinementCtx,
  t: ComposerTranslation,
  basePath: (string | number)[] = [],
) => {
  if (!isVehicleVolObject(data)) {
    return;
  }
  if (!data.fabricant?.code) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      path: [...basePath, "fabricant"],
      message: t("validation.fabricantRequis"),
    });
  }
  if (data.fabricant?.code === "AUTRE" && !data.fabricantAutre?.trim()) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      path: [...basePath, "fabricantAutre"],
      message: t("validation.fabricantAutreRequis"),
    });
  }
  if (data.fabricant?.code !== "AUTRE") {
    if (data.modele?.code === "AUTRE" && !data.modeleAutre?.trim()) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: [...basePath, "modeleAutre"],
        message: t("validation.modeleAutreRequis"),
      });
    }
    if (!data.modele?.code && !data.modeleAutre?.trim()) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: [...basePath, "modele"],
        message: t("validation.modeleRequis"),
      });
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: [...basePath, "modeleAutre"],
        message: t("validation.modeleAutreRequis"),
      });
    }
  }
};

const validateIncidentRequirements = (data: Record<string, any>, ctx: z.RefinementCtx, t: ComposerTranslation) => {
  if (!data.typeIncident) {
    addCustomIssue(ctx, "typeIncident", t("validation.typeIncidentRequis"));
    return;
  }

  let rules = createIncidentRequirements(t)[data.typeIncident];
  if (!rules) {
    return;
  }

  if (data.typeIncident === "degat-delit" && !requiresConstatQuestion(data.typeDommage)) {
    rules = rules.filter(r => r.field !== "constatPresent");
  }

  if (
    data.typeIncident === "vol" &&
    (hasObjetsVolesEnregistres(data) || data.categorieObjet === "plaque")
  ) {
    rules = rules.filter(r => r.field !== "typeObjet" && r.field !== "couleur");
  }

  rules.forEach(({ field, message }) => {
    validateIncidentRequirement(data, ctx, field, message);
  });
};

const validateDommageSpecificRules = (data: Record<string, any>, ctx: z.RefinementCtx, t: ComposerTranslation) =>  {
  if (data.typeIncident !== "degat-delit") {
    return;
  }

  if (Array.isArray(data.objetsDegradesValides) && data.objetsDegradesValides.length > 0) {
    data.objetsDegradesValides.forEach((objet: unknown, index: number) => {
      if (objet && typeof objet === "object") {
        validateVehicleFields(objet as Record<string, any>, ctx, t, ["objetsDegradesValides", index]);
      }
    });
    return;
  }

  validateVehicleFields(data, ctx, t);
}

const validateVolSpecificRules = (data: Record<string, any>, ctx: z.RefinementCtx, t: ComposerTranslation) => {
  if (data.typeIncident !== "vol") {
    return;
  }

  if (hasObjetsVolesEnregistres(data)) {
    validateObjetsVolesEnregistres(data, ctx, t);
    return;
  }

  addCustomIssue(ctx, "objetsVolesValides", t("validation.objetVoleRequis"));
  validateNumeroSerie(data, ctx, t);
  validateNumeroIMEI(data, ctx, t);
  validateVehicleBrandAndModel(data, ctx, t);
  validatePlaque(data, ctx, t);
};

const validateObjetsVolesEnregistres = (data: Record<string, any>, ctx: z.RefinementCtx, t: ComposerTranslation,) => {
  data.objetsVolesValides.forEach((objet: unknown, index: number) => {
    if (objet && typeof objet === "object") {
      const objetData = objet as Record<string, any>;
      const basePath = ["objetsVolesValides", index];
      validateVehicleFields(objetData, ctx, t, basePath);
      validateNumeroSerie(objetData, ctx, t, basePath);
      validateNumeroIMEI(objetData, ctx, t, basePath);
    }
  });
};

const validateNumeroSerie = (
  data: Record<string, any>,
  ctx: z.RefinementCtx,
  t: ComposerTranslation,
  basePath: (string | number)[] = [],
) => {
  const needsNumeroSerie = CATEGORIES_AVEC_NUMERO_SERIE.includes(data.categorieObjet) && !data.numeroSerieInconnu && !data.numeroSerie?.trim();
  if (needsNumeroSerie) {
    addCustomIssue(ctx, [...basePath, "numeroSerie"], t("validation.numeroSerieRequis"));
  }
};

const validateNumeroIMEI = (
  data: Record<string, any>,
  ctx: z.RefinementCtx,
  t: ComposerTranslation,
  basePath: (string | number)[] = [],
) => {
  const imei = data.numeroIMEI?.trim();
  const isTelephoneMobile = data.typeObjet?.code === RIPOL.CODE_TELEPHONE_MOBILE;

  if (isTelephoneMobile && !data.numeroIMEIInconnu && !imei) {
    addCustomIssue(ctx, [...basePath, "numeroIMEI"], t("validation.numeroIMEIRequis"));
  }

  if (!data.numeroIMEIInconnu && imei && !NUMERO_IMEI_REGEX.test(imei)) {
    addCustomIssue(ctx, [...basePath, "numeroIMEI"], t("validation.numeroIMEIFormat", {max: NUMERO_IMEI_MAX_LENGTH}));
  }

  if (isTelephoneMobile && data.numeroIMEIInconnu && !data.justificationAbsenceIMEI?.trim()) {
    addCustomIssue(ctx, [...basePath, "justificationAbsenceIMEI"], t("validation.justificationAbsenceIMEIRequise"));
  }
};

function validateRequiredDates(data: any, fields: string[][], ctx: any, t: any) {
  fields.forEach(([field, message]) => {
    if (!data[field]) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: [field],
        message: t(message),
      });
    }
  });
}

function validateChronologieDates(
  data: any,
  config: {
    startDate: string;
    startTime: string;
    endDate: string;
    endTime: string;
    errorPath: string;
    errorMessage: string;
  },
  ctx: any,
  t: any
) {
  const {
    startDate,
    startTime,
    endDate,
    endTime,
    errorPath,
    errorMessage,
  } = config;

  const dateDebutRaw = data[startDate];
  const heureDebutRaw = data[startTime];
  const dateFinRaw = data[endDate];
  const heureFinRaw = data[endTime];

  if (!dateDebutRaw || !heureDebutRaw || !dateFinRaw || !heureFinRaw) {
    return;
  }

  const dateDebut = parseDate(dateDebutRaw);
  const dateFin = parseDate(dateFinRaw);
  const heureDebut = parseTime(heureDebutRaw);
  const heureFin = parseTime(heureFinRaw);

  if (!dateDebut || !dateFin || !heureDebut || !heureFin) {
    return;
  }

  const debut = new Date(
    dateDebut.getFullYear(),
    dateDebut.getMonth(),
    dateDebut.getDate(),
    heureDebut.hour,
    heureDebut.minute
  );

  const fin = new Date(
    dateFin.getFullYear(),
    dateFin.getMonth(),
    dateFin.getDate(),
    heureFin.hour,
    heureFin.minute
  );

  if (fin < debut) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      path: [errorPath],
      message: t(errorMessage),
    });
  }
}

function validateCommandeFrauduleuse(data: any, ctx: any, t: any) {
  validateRequiredFields(data, ctx, t);
  validateCoordonneesCommande(data, ctx, t);
}

function validateRequiredFields(data: any, ctx: any, t: any) {
  const fields = [
    ["prestataire", "validation.prestataireRequis"],
    ["dateDecouverte", "validation.dateDecouverteRequise"],
    ["montant", "validation.montantRequis"],
  ];

  fields.forEach(([field, message]) => {
    if (!data[field]?.trim()) {
      addCustomIssue(ctx, field, t(message));
    }
  });

  if (data.assurance === undefined || data.assurance === null) {
    addCustomIssue(ctx, "assurance", t("validation.assuranceRequise"));
  }

  if (data.livraisonAdresseLesee === undefined || data.livraisonAdresseLesee === null) {
    addCustomIssue(ctx, "livraisonAdresseLesee", t("validation.livraisonAdresseLesee"));
  }
}

function validateCoordonneesCommande(data: any, ctx: any, t: any) {
  if (!data.emailCommandeInconnu && !data.emailCommande?.trim()) {
    addCustomIssue(ctx, "emailCommande", t("validation.emailCommandeRequis"));
  } else if (
    data.emailCommande?.trim() &&
    !z.string().email().safeParse(data.emailCommande).success
  ) {
    addCustomIssue(ctx, "emailCommande", t("validation.emailCommandeFormat"));
  }
  if (!data.telephoneCommandeInconnu && !data.telephoneCommande?.trim()) {
    addCustomIssue(ctx, "telephoneCommande", t("validation.telephoneCommandeRequis"));
  }
}

const isVehiculeAvecPlaque = (data: Record<string, any>) =>
  VEHICULE_CATEGORIES_AVEC_PLAQUE.includes(data.sousCategorie);

const isCategoriePlaque = (data: Record<string, any>) =>
  data.categorieObjet === "plaque";

const isVehiculePlaqueContext = (data: Record<string, any>) =>
  data.categorieObjet === "vehicule" && isVehiculeAvecPlaque(data);

const isDommageVehiculePlaqueContext = (data: Record<string, any>) =>
  data.typeDommage === "vehicule" && isVehiculeAvecPlaque(data);

const shouldValidatePlaque = (data: Record<string, any>) =>
  !data.plaqueInconnu &&
  (
    isCategoriePlaque(data) ||
    isVehiculePlaqueContext(data) ||
    isDommageVehiculePlaqueContext(data)
  );

const validatePlaque = (data: Record<string, any>, ctx: z.RefinementCtx, t: ComposerTranslation, basePath: (string | number)[] = []) => {
  if (!shouldValidatePlaque(data)) {
    return;
  }
  validatePlaqueRequiredFields(data, ctx, t, basePath);
  validatePlaqueByCountry(data, ctx, t, basePath);
};

const validatePlaqueRequiredFields = (data: Record<string, any>, ctx: z.RefinementCtx, t: ComposerTranslation, basePath: (string | number)[] = []) => {
  const pays = data.plaquePays?.code;

  if (!pays) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      path: [...basePath, "plaquePays"],
      message: t("validation.plaquePaysRequise"),
    });
  }

  const isNotPlaque = data.categorieObjet !== "plaque";
  const isSuisse = pays === RIPOL.PAYS_SUISSE;

  if (isNotPlaque && isSuisse && !data.plaqueCanton?.code) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      path: [...basePath, "plaqueCanton"],
      message: t("validation.plaqueCantonRequis"),
    });
  }

  if (!data.plaqueNumero?.trim()) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      path: [...basePath, "plaqueNumero"],
      message: t("validation.plaqueNumeroRequise"),
    });
  }
};

const validatePlaqueByCountry = (data: Record<string, any>, ctx: z.RefinementCtx, t: ComposerTranslation, basePath: (string | number)[] = [],) => {
  const pays = data.plaquePays?.code;
  const numero = data.plaqueNumero?.trim();

  if (!numero || !pays) {
    return;
  }

  if (pays === RIPOL.PAYS_SUISSE) {
    validatePlaqueSuisse(numero, ctx, t, basePath);
    return;
  }

  if (pays === RIPOL.PAYS_FRANCE) {
    validatePlaqueFrance(numero, ctx, t, basePath);
    return;
  }

  validatePlaqueInternationale(numero, ctx, t, basePath);
};

const validatePlaqueSuisse = (numero: string, ctx: z.RefinementCtx, t: ComposerTranslation, basePath: (string | number)[],) => {
  if (!PLAQUE_SUISSE_PATTERN.test(numero)) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      path: [...basePath, "plaqueNumero"],
      message: t("validation.numeroPlaqueSuisseInvalide"),
    });
  }
};

const validatePlaqueFrance = (numero: string, ctx: z.RefinementCtx, t: ComposerTranslation, basePath: (string | number)[],) => {
  const valid = PLAQUE_FRANCE_SIV_PATTERN.test(numero) || PLAQUE_FRANCE_FNI_PATTERN.test(numero);

  if (!valid) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      path: [...basePath, "plaqueNumero"],
      message: t("validation.numeroPlaqueFranceInvalide"),
    });
  }
};

const validatePlaqueInternationale = (numero: string, ctx: z.RefinementCtx, t: ComposerTranslation, basePath: (string | number)[],) => {
  if (!PLAQUE_INTERNATIONALE_PATTERN.test(numero)) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      path: [...basePath, "plaqueNumero"],
      message: t("validation.numeroPlaqueInternationaleInvalide"),
    });
  }
};

const validateAdresseLesee = (data: any, ctx: any, t: any) => {
  if (data.adresseLesee === null) {
    addCustomIssue(ctx, "adresseLesee", t("validation.adresseCorrespondRequise"));
  }
};

const validateAdressePrincipale = (data: any, ctx: any, t: any) => {
  if (data.adresseLesee !== false) {
    return;
  }

  if (data.adresseConnue === null) {
    addCustomIssue(ctx, "adresseConnue", t("validation.adresseConnueRequise"));
  }

  if (data.typeLieu === null) {
    addCustomIssue(ctx, "typeLieu", t("validation.typeLieuRequis"));
  }

  if (data.adresseConnue || data.isTrajet) {
    validateAdresseEvenement(data, "adresseEvenement", ctx, t);
    validateAdressePostaleEvenement(data, "adressePostaleEvenement", ctx, t);
    validateLocaliteEvenement(data, "localiteEvenement", ctx, t);
    validateNpaEvenement(data, "npaEvenement", ctx, t);
  }

  if (data.adresseConnue === false && data.isTrajet === null) {
    addCustomIssue(ctx, "isTrajet", t("validation.adresseTrajetRequise"));
  }
};

const validateAdresseSecondaire = (data: any, ctx: any, t: any) => {
  if (data.adresseLesee !== false || data.adresseConnue !== false) {
    return;
  }

  if (data.isTrajet) {
    validateAdresseEvenement(data, "adresseEvenementSecondaire", ctx, t);
    validateAdressePostaleEvenement(data, "adressePostaleEvenementSecondaire", ctx, t);
    validateLocaliteEvenement(data, "localiteEvenementSecondaire", ctx, t);
    validateNpaEvenement(data, "npaEvenementSecondaire", ctx, t);
  }

  if (data.isTrajet === false && data.lieuOrigineEvenement === null) {
    addCustomIssue(ctx, "lieuOrigineEvenement", t("validation.lieuOrigineRequis"));
  }
};

const validateAdresseEvenement = (data: any, field: string, ctx: any, t: any) => {
  const value = data[field];
  if (!value || value.length < MIN_ADRESSE_EVENEMENT_TAILLE) {
    addCustomIssue(ctx, field, t("validation.adresseEvenementRequise"));
  }
};

const validateAdressePostaleEvenement = (data: any, field: string, ctx: any, t: any) => {
  const value = data[field]?.trim() ?? "";
  if (!/^[a-zA-Z0-9\s]*$/.test(value)) {
    addCustomIssue(ctx, field, t("validation.numeroPostalFormat"));
  }
};

const validateLocaliteEvenement = (data: any, field: string, ctx: any, t: any) => {
  if (!data[field]) {
    addCustomIssue(ctx, field, t("validation.localiteRequise"));
  }
};

const validateNpaEvenement = (data: any, field: string, ctx: any, t: any) => {
  const value = data[field];
  if (!value || value.length < 4) {
    addCustomIssue(ctx, field, t("validation.npaFormat"));
  }
};

const evenementFields = (t: ComposerTranslation) => ({
  typeIncident: z.string().optional(),
  dateDebutEvenement: z
    .string()
    .optional()
    .refine(val => !val || isValidBoundedDate(val), {
      message: t(VALIDATION_FORMAT_DATE_INVALIDE),
    }),
  heureDebutEvenement: z
    .string()
    .optional()
    .refine(val => !val || parseTime(val) !== null, {
      message: t(VALIDATION_FORMAT_HEURE_INVALIDE),
    }),
  dateFinEvenement: z
    .string()
    .optional()
    .refine(val => !val || isValidBoundedDate(val), {
      message: t(VALIDATION_FORMAT_DATE_INVALIDE),
    }),
  heureFinEvenement: z
    .string()
    .optional()
    .refine(val => !val || parseTime(val) !== null, {
      message: t(VALIDATION_FORMAT_HEURE_INVALIDE),
    }),
  adresseEvenement: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  typeLieu: optionalRipolSelectionSchema,
  adresseConnue: z.boolean().nullish(),
  adresseLesee: z.boolean().nullish(),
  isTrajet: z.boolean().nullish(),
  adressePostaleEvenement: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  npaEvenement: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  localiteEvenement: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  paysEvenement: z.string().optional(),
  adresseEvenementSecondaire: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  adressePostaleEvenementSecondaire: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  npaEvenementSecondaire: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  localiteEvenementSecondaire: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  paysEvenementSecondaire: z.string().optional(),
  lieuOrigineEvenement: optionalRipolSelectionSchema,
  volDansVehicule: z.boolean().nullish(),
});

const objetFields = (t: ComposerTranslation) => ({
  categorieObjet: z.string().optional(),
  objetsVolesValides: z.array(z.unknown()).optional(),
  objetsDegradesValides: z.array(z.unknown()).optional(),
  sousCategorie: z.string().optional(),
  typeObjet: optionalRipolSelectionSchema,
  fabricant: optionalRipolSelectionSchema,
  fabricantAutre: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  modele: optionalRipolSelectionSchema,
  modeleAutre: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  couleur: optionalRipolSelectionSchema,
  couleurSecondaire: optionalRipolSelectionSchema,
  valeurReelle: optionalMontantPositifFromForm(t),
  numeroSerie: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  numeroSerieInconnu: z.boolean().optional(),
  numeroCadre: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  numeroCadreInconnu: z.boolean().optional(),
  numeroIMEI: z.string().max(NUMERO_IMEI_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: NUMERO_IMEI_MAX_LENGTH })).optional(),
  numeroIMEIInconnu: z.boolean().optional(),
  justificationAbsenceIMEI: z.string().max(TEXTAREA_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXTAREA_MAX_LENGTH })).optional(),
  gravure: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  isVehicle: z.boolean().optional(),
  dateAchat: z
    .string()
    .optional()
    .refine(val => !val || isValidBoundedDate(val), { message: t(VALIDATION_FORMAT_DATE_INVALIDE) }),
  vin: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  vinInconnu: z.boolean().optional(),
  velofinderId: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  plaqueNumero: z.string().optional(),
  plaqueInconnu: z.boolean().optional(),
  plaquePays: optionalRipolSelectionSchema,
  plaqueCanton: optionalRipolSelectionSchema,
  assuranceAucune: z.boolean().optional(),
  assureurAutre: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  numeroAssurance: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  numeroVignette: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  numeroMaster: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
});

const dommageFields = (t: ComposerTranslation) => ({
  avezVousDegradation: z.boolean().nullish(),
  montantEstime: optionalStringFromForm(t),
  devise: z.string().optional(),
  typeDommage: z.string().optional(),
  naturesDommage: z
    .array(z.string(), {
      errorMap: () => ({ message: t("validation.natureDommageFormat") }),
    })
    .optional(),
  description: z.string().max(TEXTAREA_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXTAREA_MAX_LENGTH })).optional(),
  dateConstat: z
    .string()
    .optional()
    .refine(val => !val || isValidBoundedDate(val), { message: t(VALIDATION_FORMAT_DATE_INVALIDE) }),
  constatPresent: z.boolean().nullish(),
  fichiers: z.array(z.instanceof(File)).optional(),
});

const cybercrimeCommonFields = (t: ComposerTranslation) => ({
  typeCybercrime: z.string().optional(),
  descriptionCybercrime: z.string().max(TEXTAREA_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXTAREA_MAX_LENGTH })).optional(),
  justificatifsPaiement: z.array(z.instanceof(File)).optional(),
  copiesEcran: z.array(z.instanceof(File)).optional(),
  autresDocuments: z.array(z.instanceof(File)).optional(),
  datePremierContact: z
    .string()
    .optional()
    .refine(val => !val || isValidBoundedDate(val), {
      message: t(VALIDATION_FORMAT_DATE_INVALIDE),
    }),
  heurePremierContact: z
    .string()
    .optional()
    .refine(val => !val || parseTime(val) !== null, {
      message: t(VALIDATION_FORMAT_HEURE_INVALIDE),
    }),
  dateDernierContact: z
    .string()
    .optional()
    .refine(val => !val || isValidBoundedDate(val), {
      message: t(VALIDATION_FORMAT_DATE_INVALIDE),
    }),
  heureDernierContact: z
    .string()
    .optional()
    .refine(val => !val || parseTime(val) !== null, {
      message: t(VALIDATION_FORMAT_HEURE_INVALIDE),
    }),
});

const commandeFrauduleuseFields = (t: ComposerTranslation) => ({
  prestataire: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  dateDecouverte: z
    .string()
    .optional()
    .refine(val => !val || isValidBoundedDate(val), { message: t(VALIDATION_FORMAT_DATE_INVALIDE) }),
  montant: optionalStringFromForm(t),
  assurance: z.boolean().nullish(),
  emailCommandeInconnu: z.boolean().optional(),
  emailCommande: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  telephoneCommandeInconnu: z.boolean().optional(),
  telephoneCommande: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  livraisonAdresseLesee: z.boolean().nullish(),
  livraisonAdresse: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  livraisonAdressePostale: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  livraisonNpa: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  livraisonLocalite: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  livraisonLocaliteCode: z.string().nullable().optional(),
  livraisonPays: z.string().optional(),
  prenomContrevenant: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  nomContrevenant: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  siteWebContrevenant: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  contrevenantAdresse: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  contrevenantAdressePostale: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  contrevenantNpa: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  contrevenantLocalite: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  contrevenantLocaliteCode: z.string().nullable().optional(),
  contrevenantPays: z.string().optional(),
  moyenPaiementNumeriqueDebite: z.boolean().nullish(),
});

const achatNonRecuFields = (t: ComposerTranslation) => ({
  montantDelitAchatLigne: optionalStringFromForm(t),
  articleNonLivreDescription: z.string().max(TEXTAREA_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXTAREA_MAX_LENGTH })).optional(),
  prenomVendeur: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  nomVendeur: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  telephoneVendeurInconnu: z.boolean().optional(),
  telephoneVendeur: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  emailVendeurInconnu: z.boolean().optional(),
  emailVendeur: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  adresseVendeurInconnue: z.boolean().optional(),
  vendeurAdresse: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  vendeurAdressePostale: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  vendeurNpa: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  vendeurLocalite: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  vendeurLocaliteCode: z.string().nullable().optional(),
  vendeurPays: z.string().optional(),
  achatViaPlaceMarche: z.boolean().nullish(),
  plateforme: z.string().optional(),
  plateformeAutre: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  plateformeId: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  nomEntrepriseVendeur: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  siteWebEntrepriseVendeur: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  annonceDocument: z.array(z.instanceof(File)).optional(),
  annonceDocumentIndisponible: z.boolean().optional(),
  raisonAbsenceAnnonce: z.string().max(TEXTAREA_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXTAREA_MAX_LENGTH })).optional(),
  moyenPaiement: z.string().optional(),
  moyenPaiementAutre: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  ibanBeneficiaire: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  comptePaypalBeneficiaire: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  numeroTransactionPaypal: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  numeroTwintBeneficiaire: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  typeCryptoMonnaie: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  montantUnitesCrypto: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  adresseWalletExpediteur: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  adresseWalletCrypto: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  hashTransactionCrypto: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
  societeBeneficiaire: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  nomBeneficiaire: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  prenomBeneficiaire: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  dateOperation: z
    .string()
    .nullable()
    .optional()
    .refine(val => !val || isValidBoundedDate(val), { message: t(VALIDATION_FORMAT_DATE_INVALIDE) }),
  preuvePaiementDocument: z.array(z.instanceof(File)).optional(),
  preuvePaiementIndisponible: z.boolean().optional(),
  raisonAbsencePreuvePaiement: z.string().max(TEXTAREA_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXTAREA_MAX_LENGTH })).optional(),
  copieIdentiteTransmiseAuteur: z.boolean().nullish(),
  copieIdentiteTransmiseAuteurDocument: z.array(z.instanceof(File)).optional(),
  copieIdentiteTransmiseAuteurDocumentIndisponible: z.boolean().optional(),
  raisonAbsenceCopieIdentiteTransmiseAuteur: z.string().max(TEXTAREA_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXTAREA_MAX_LENGTH })).optional(),
  copieIdentiteAuteurTransmise: z.boolean().nullish(),
  copieIdentiteAuteurDocument: z.array(z.instanceof(File)).optional(),
  copieIdentiteAuteurDocumentIndisponible: z.boolean().optional(),
  raisonAbsenceCopieIdentiteAuteur: z.string().max(TEXTAREA_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXTAREA_MAX_LENGTH })).optional(),
});

const fausseAnnonceFields = (t: ComposerTranslation) => ({
  titreAnnonce: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  nomBailleur: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  emailBailleurInconnu: z.boolean().optional(),
  emailBailleur: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  telephoneBailleurInconnu: z.boolean().optional(),
  telephoneBailleur: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  adresseBienImmobilier: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  montantDemande: optionalStringFromForm(t),
  modePaiementDemande: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
  urlComplete: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
});

export const createEvenementInfoSchema = (t: ComposerTranslation) =>
  z
    .object({
      ...evenementFields(t),
      ...objetFields(t),
      ...dommageFields(t),
      ...cybercrimeCommonFields(t),
      ...commandeFrauduleuseFields(t),
      ...achatNonRecuFields(t),
      ...fausseAnnonceFields(t),
    })
    .superRefine((data, ctx) => {
      if (data.typeIncident !== "cybercrime") {
        validateAdresseLesee(data, ctx, t);
        validateAdressePrincipale(data, ctx, t);
        validateAdresseSecondaire(data, ctx, t);
      }
    })
    .superRefine((data, ctx) => {
      const isContact =
        data.typeIncident === "cybercrime" &&
        (data.typeCybercrime === "achat-non-recu" ||
        data.typeCybercrime === 'fausse-annonce');

      const config = isContact
        ? {
          required: [
            ["datePremierContact", "validation.datePremierContactRequise"],
            ["heurePremierContact", "validation.heurePremierContactRequise"],
            ["dateDernierContact", "validation.dateDernierContactRequise"],
            ["heureDernierContact", "validation.heureDernierContactRequise"],
          ],
          chrono: {
            startDate: "datePremierContact",
            startTime: "heurePremierContact",
            endDate: "dateDernierContact",
            endTime: "heureDernierContact",
            errorPath: "heureDernierContact",
            errorMessage: "validation.dateFinContactApresDebut"
          },
        }
        : {
          required: [
            ["dateDebutEvenement", "validation.dateDebutEvenementRequise"],
            ["heureDebutEvenement", "validation.heureDebutEvenementRequise"],
            ["dateFinEvenement", "validation.dateFinEvenementRequise"],
            ["heureFinEvenement", "validation.heureFinEvenementRequise"],
          ],
          chrono: {
            startDate: "dateDebutEvenement",
            startTime: "heureDebutEvenement",
            endDate: "dateFinEvenement",
            endTime: "heureFinEvenement",
            errorPath: "heureFinEvenement",
            errorMessage: "validation.dateFinEvenementApresDebut"
          },
        };

      validateRequiredDates(data, config.required, ctx, t);
      validateChronologieDates(data, config.chrono, ctx, t);
    })
    .superRefine((data, ctx) => {
      if (data.typeIncident === "cybercrime") {
        if (!data.typeCybercrime) {
          addCustomIssue(ctx, "typeCybercrime", t("validation.typeCybercrimeRequis"));
        }

        const skipCybercrimeUrlDescription =
          !data.typeCybercrime ||
          data.typeCybercrime === "achat-non-recu" ||
          isCybercrimeTypeWithoutDetailFields(data.typeCybercrime);

        if (!skipCybercrimeUrlDescription && !data.descriptionCybercrime?.trim()) {
          addCustomIssue(ctx, "descriptionCybercrime", t("validation.descriptionCybercrimeRequise"));
        }
      }
    })
    .superRefine((data, ctx) => {
      validateIncidentRequirements(data, ctx, t);
      validateVolSpecificRules(data, ctx, t);
      validateDommageSpecificRules(data, ctx, t);
    })
    .superRefine((data, ctx) => {
      if (data.typeCybercrime !== "commande-frauduleuse") {
        return;
      }

      validateCommandeFrauduleuse(data, ctx, t);
      validateCommandeFrauduleuseCybercrime(data, ctx, t);
    })
    .superRefine((data, ctx) => validateAchatNonRecuCybercrime(data, ctx, addCustomIssue, t))
    .superRefine((data, ctx) => {
      if (data.typeCybercrime !== "fausse-annonce") {
        return;
      }

      validateAnnonce(data, ctx, t);
      validateBailleur(data, ctx, t);
      validateBienImmobilier(data, ctx, t);
      validatePaiement(data, ctx, t);
    });

const validateAnnonce = (data: any, ctx: any, t: any) => {
  if (!data.urlComplete?.trim()) {
    addCustomIssue(ctx, "urlComplete", t("validation.urlCompleteRequise"));
  } else if (!isUrlWebAvecDomaine(data.urlComplete)) {
    addCustomIssue(ctx, "urlComplete", t("validation.plateformeUrlOuIdInvalide"));
  }

  if (!data.titreAnnonce?.trim()) {
    addCustomIssue(ctx, "titreAnnonce", t("validation.titreAnnonceRequis"));
  }
};

const validateBailleur = (data: any, ctx: any, t: any) => {
  if (!data.nomBailleur?.trim()) {
    addCustomIssue(ctx, "nomBailleur", t("validation.nomBailleurRequis"));
  }

  const hasEmail = data.emailBailleur?.trim();
  if (!data.emailBailleurInconnu && !hasEmail) {
    addCustomIssue(ctx, "emailBailleur", t("validation.emailBailleurRequis"));
  } else if (
    hasEmail &&
    !z.string().email().safeParse(data.emailBailleur).success
  ) {
    addCustomIssue(ctx, "emailBailleur", t("validation.emailBailleurFormat"));
  }

  const hasTelephone = data.telephoneBailleur?.trim();
  if (!data.telephoneBailleurInconnu && !hasTelephone) {
    addCustomIssue(ctx, "telephoneBailleur", t("validation.telephoneBailleurRequis"));
  }
};

const validateBienImmobilier = (data: any, ctx: any, t: any) => {
  if (!data.adresseBienImmobilier?.trim()) {
    addCustomIssue(ctx, "adresseBienImmobilier", t("validation.adresseBienImmobilierRequise"));
  }
};

const validatePaiement = (data: any, ctx: any, t: any) => {
  if (!data.montantDemande?.trim()) {
    addCustomIssue(ctx, "montantDemande", t("validation.montantDemandeRequis"));
  }

  if (!data.modePaiementDemande?.trim()) {
    addCustomIssue(ctx, "modePaiementDemande", t("validation.modePaiementDemandeRequis"));
  }
};

export const createIncidentSchema = (t: ComposerTranslation, nationalite: string) => {
  const isCH = (v?: string) => {
    const value = (v ?? "").toUpperCase();
    return value === "SUISSE" || value === "CH" || value === RIPOL.PAYS_SUISSE;
  };

  return createEvenementInfoSchema(t).superRefine((data, ctx) => {
    const personneLeseeSuisse = isCH(nationalite);
    if (!(isCH(data.paysEvenement) || personneLeseeSuisse)) {
      addCustomIssue(ctx, "paysEvenement", t("validation.paysOuNationaliteSuisse"));
    }
    if (data.isTrajet && !(isCH(data.paysEvenementSecondaire) || personneLeseeSuisse)) {
      addCustomIssue(ctx, "paysEvenementSecondaire", t("validation.paysOuNationaliteSuisse"));
    }
  });
};
