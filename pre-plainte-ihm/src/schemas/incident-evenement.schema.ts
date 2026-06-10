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
import { isUrlWebAvecDomaine } from "@/utils/validations/field-validation.utils";

const MIN_ADRESSE_EVENEMENT_TAILLE = 5;
const VALIDATION_FORMAT_DATE_INVALIDE = "validation.formatDateInvalide";
const VALIDATION_FORMAT_HEURE_INVALIDE = "validation.formatHeureInvalide";
const NUMERO_IMEI_REGEX = /^\d{15}$/;
const PLAQUE_SUISSE_PATTERN = /^[A-Z]{2}\s\d{1,6}$/;
const PLAQUE_FRANCE_SIV_PATTERN = /^[A-Z]{2}-\d{3}-[A-Z]{2}$/;
const PLAQUE_FRANCE_FNI_PATTERN = /^\d{1,4}\s[A-Z]{1,3}\s(2A|2B|\d{2,3})$/;
const PLAQUE_INTERNATIONALE_PATTERN = /^[A-Z\d]{1,12}$/;

const optionalStringFromForm = (t: ComposerTranslation) =>
  z.preprocess(
    v => (v === null || v === undefined ? "" : String(v)),
    z.string()
      .max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH }))
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
    { field: "typeObjet", message: t("validation.typeObjetRequis") },
    { field: "avezVousDegradation", message: t("validation.degradationsRequis") },
  ],
  "degat-delit": [
    { field: "typeDommage", message: t("validation.typeDommageRequis") },
    { field: "devise", message: t("validation.deviseRequise") },
    { field: "naturesDommage", message: t("validation.natureDommageRequis") },
    { field: "description", message: t("validation.descriptionDommageRequise") },
    { field: "constatPresent", message: t("validation.constatRequis") },
    { field: "dateConstat", message: t("validation.constatDommageRequis") },
  ],
});

const isEmpty = (value: unknown) => value === undefined || value === null || value === "";

const addCustomIssue = (ctx: z.RefinementCtx, path: string, message: string) => {
  ctx.addIssue({
    code: z.ZodIssueCode.custom,
    path: [path],
    message,
  });
};

const isRipolField = (field: string) => ["typeObjet", "fabricant", "modele"].includes(field);

const validateIncidentRequirement = (
  data: Record<string, any>,
  ctx: z.RefinementCtx,
  field: string,
  message: string,
) => {
  const value = data[field];

  if (field === "constatPresent") {
    if (value === undefined || value === null) {
      addCustomIssue(ctx, field, message);
    }
    return;
  }

  if (field === "dateConstat") {
    if (data.constatPresent === true && isEmpty(value)) {
      addCustomIssue(ctx, field, message);
    }
    return;
  }

  if (field === "naturesDommage") {
    if (value === undefined || value === null || (Array.isArray(value) && value.length === 0)) {
      addCustomIssue(ctx, field, message);
    }
    return;
  }

  if (field === "typeDommage") {
    if (isEmpty(value)) {
      addCustomIssue(ctx, field, message);
    }
    return;
  }

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
      message: t("validation.champRequis"),
    });
  }
  if (data.fabricant.code !== "AUTRE") {
    if (!data.modele?.code) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: [...basePath, "modele"],
        message: t("validation.modeleRequis"),
      });
    }
    if (data.modele?.code === "AUTRE" && !data.modeleAutre?.trim()) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: [...basePath, "modeleAutre"],
        message: t("validation.champRequis"),
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

  if (
    data.typeIncident === "vol" &&
    (hasObjetsVolesEnregistres(data) || data.categorieObjet === "plaque")
  ) {
    rules = rules.filter(r => r.field !== "typeObjet");
  }

  rules.forEach(({ field, message }) => {
    validateIncidentRequirement(data, ctx, field, message);
  });
};

const validateDommageSpecificRules = (data: Record<string, any>, ctx: z.RefinementCtx, t: ComposerTranslation) =>  {
  if (data.typeIncident !== "degat-delit") {
    return;
  }

  validateDommageConstatPolice(data, ctx, t);
  if (Array.isArray(data.objetsDegradesValides) && data.objetsDegradesValides.length > 0) {
    data.objetsDegradesValides.forEach((objet: unknown, index: number) => {
      if (objet && typeof objet === "object") {
        validateVehicleBrandAndModel(objet as Record<string, any>, ctx, t, ["objetsDegradesValides", index]);
        validatePlaque(objet as Record<string, any>, ctx, t, ["objetsDegradesValides", index]);
      }
    });
    return;
  }

    validateVehicleBrandAndModel(data, ctx, t);
    validatePlaque(data, ctx, t);
}

const validateDommageConstatPolice = (data: Record<string, any>, ctx: z.RefinementCtx, t: ComposerTranslation) => {
  if (data.constatPresent === false) {
    addCustomIssue(ctx, "constatPresent", t("dommages.constatPoliceWarning"));
    return;
  }

  if (data.constatPresent === true && (!Array.isArray(data.fichiers) || data.fichiers.length === 0)) {
    addCustomIssue(ctx, "fichiers", t("validation.constatPoliceFichierRequis"));
  }
};

const validateVolSpecificRules = (data: Record<string, any>, ctx: z.RefinementCtx, t: ComposerTranslation) => {
  if (data.typeIncident !== "vol") {
    return;
  }

  if (hasObjetsVolesEnregistres(data)) {
    data.objetsVolesValides.forEach((objet: unknown, index: number) => {
      if (objet && typeof objet === "object") {
        validateVehicleBrandAndModel(objet as Record<string, any>, ctx, t, ["objetsVolesValides", index]);
        validatePlaque(objet as Record<string, any>, ctx, t, ["objetsVolesValides", index]);
      }
    });
    return;
  }

  if ((data.categorieObjet === "telephone" ||
      data.categorieObjet === "informatique" ||
      data.categorieObjet === "photo_video") &&
    !data.numeroSerieInconnu &&
    !data.numeroSerie?.trim()
  ) {
    addCustomIssue(ctx, "numeroSerie", t("validation.numeroSerieRequis"));
  }

  if (data.typeObjet?.code === RIPOL.CODE_TELEPHONE_MOBILE && !data.numeroIMEIInconnu && !data.numeroIMEI?.trim()) {
    addCustomIssue(ctx, "numeroIMEI", t("validation.numeroIMEIRequis"));
  }

  if (!data.numeroIMEIInconnu && data.numeroIMEI?.trim() && !NUMERO_IMEI_REGEX.test(data.numeroIMEI.trim())) {
    addCustomIssue(ctx, "numeroIMEI", t("validation.numeroIMEIFormat", { max: NUMERO_IMEI_MAX_LENGTH }));
  }

  validateVehicleBrandAndModel(data, ctx, t);
  validatePlaque(data, ctx, t);
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

const shouldValidatePlaque = (data: Record<string, any>) =>
  !data.plaqueInconnu &&
  (data.categorieObjet === "plaque" ||
  (data.categorieObjet === "vehicule" && VEHICULE_CATEGORIES_AVEC_PLAQUE.includes(data.sousCategorie)) ||
  (data.typeDommage === "vehicule" && VEHICULE_CATEGORIES_AVEC_PLAQUE.includes(data.sousCategorie)));

const validatePlaque = (
  data: Record<string, any>,
  ctx: z.RefinementCtx,
  t: ComposerTranslation,
  basePath: (string | number)[] = [],
) => {
  if (!shouldValidatePlaque(data)) {
    return;
  }

  const pays = data.plaquePays?.code;
  const numero = data.plaqueNumero?.trim();
  const canton = data.plaqueCanton?.code;

  if (!pays) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      path: [...basePath, "plaquePays"],
      message: t("validation.champRequis"),
    });
  }

  if (data.categorieObjet !== "plaque" && pays === RIPOL.PAYS_SUISSE && !canton) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      path: [...basePath, "plaqueCanton"],
      message: t("validation.champRequis"),
    });
  }

  if (!numero) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      path: [...basePath, "plaqueNumero"],
      message: t("validation.champRequis"),
    });
  }

  if (pays === RIPOL.PAYS_SUISSE) {
    if (!PLAQUE_SUISSE_PATTERN.test(numero)) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: [...basePath, "plaqueNumero"],
        message: t("validation.numeroPlaqueSuisseInvalide"),
      });
    }
    return;
  }

  if (pays === RIPOL.PAYS_FRANCE) {
    const valid =
      PLAQUE_FRANCE_SIV_PATTERN.test(numero) ||
      PLAQUE_FRANCE_FNI_PATTERN.test(numero);

    if (!valid) {
      ctx.addIssue({
        code: z.ZodIssueCode.custom,
        path: [...basePath, "plaqueNumero"],
        message: t("validation.numeroPlaqueFranceInvalide"),
      });
    }
    return;
  }

  if (!PLAQUE_INTERNATIONALE_PATTERN.test(numero)) {
    ctx.addIssue({
      code: z.ZodIssueCode.custom,
      path: [...basePath, "plaqueNumero"],
      message: t("validation.numeroPlaqueInternationaleInvalide"),
    });
  }
};

export const createEvenementInfoSchema = (t: ComposerTranslation) =>
  z
    .object({
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
      adresseEvenement: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      typeLieu: optionalRipolSelectionSchema,
      adresseConnue: z.boolean().nullish(),
      adresseLesee: z.boolean().nullish(),
      isTrajet: z.boolean().nullish(),
      adressePostaleEvenement: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      npaEvenement: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      localiteEvenement: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      paysEvenement: z.string().optional(),
      adresseEvenementSecondaire: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      adressePostaleEvenementSecondaire: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      npaEvenementSecondaire: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      localiteEvenementSecondaire: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      paysEvenementSecondaire: z.string().optional(),
      lieuOrigine: optionalRipolSelectionSchema,
      temoins: z.string().optional(),
      plainteDeposee: z.string().optional(),
      detailsPlainteDeposee: z.string().optional(),
      volDansVehicule: z.boolean().nullish(),
      categorieObjet: z.string().optional(),
      objetsVolesValides: z.array(z.unknown()).optional(),
      objetsDegradesValides: z.array(z.unknown()).optional(),
      sousCategorie: z.string().optional(),
      typeObjet: optionalRipolSelectionSchema,
      fabricant: optionalRipolSelectionSchema,
      fabricantAutre: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      modele: optionalRipolSelectionSchema,
      modeleAutre: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      couleur: optionalRipolSelectionSchema,
      couleurSecondaire: optionalRipolSelectionSchema,
      valeurReelle: optionalStringFromForm(t),
      numeroSerie: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      numeroSerieInconnu: z.boolean().optional(),
      numeroCadre: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      numeroCadreInconnu: z.boolean().optional(),
      numeroIMEI: z.string().max(NUMERO_IMEI_MAX_LENGTH, t("validation.longueurMax", { max: NUMERO_IMEI_MAX_LENGTH })).optional(),
      numeroIMEIInconnu: z.boolean().optional(),
      justificationAbsenceIMEI: z.string().max(TEXTAREA_MAX_LENGTH, t("validation.longueurMax", { max: TEXTAREA_MAX_LENGTH })).optional(),
      gravure: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      isVehicle: z.boolean().optional(),
      dateAchat: z
        .string()
        .optional()
        .refine(val => !val || isValidBoundedDate(val), { message: t(VALIDATION_FORMAT_DATE_INVALIDE) }),
      vin: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      vinInconnu: z.boolean().optional(),
      velofinderId: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      plaqueNumero: z.string().optional(),
      plaqueInconnu: z.boolean().optional(),
      plaquePays: optionalRipolSelectionSchema,
      plaqueCanton: optionalRipolSelectionSchema,
      assuranceAucune: z.boolean().optional(),
      assureurAutre: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      numeroAssurance: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      numeroVignette: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      numeroMaster: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      avezVousDegradation: z.boolean().nullish(),
      montantEstime: optionalStringFromForm(t),
      devise: z.string().optional(),
      typeDommage: z.string().optional(),
      naturesDommage: z
        .array(z.string(), {
          errorMap: () => ({ message: t("validation.natureDommageFormat") }),
        })
        .optional(),
      description: z.string().max(TEXTAREA_MAX_LENGTH, t("validation.longueurMax", { max: TEXTAREA_MAX_LENGTH })).optional(),
      dateConstat: z
        .string()
        .optional()
        .refine(val => !val || isValidBoundedDate(val), { message: t(VALIDATION_FORMAT_DATE_INVALIDE) }),
      constatPresent: z.boolean().nullish(),
      fichiers: z.array(z.instanceof(File)).optional(),
      typeCybercrime: z.string().optional(),
      descriptionCybercrime: z.string().max(TEXTAREA_MAX_LENGTH, t("validation.longueurMax", { max: TEXTAREA_MAX_LENGTH })).optional(),
      justificatifsPaiement: z.array(z.instanceof(File)).optional(),
      copiesEcran: z.array(z.instanceof(File)).optional(),
      autresDocuments: z.array(z.instanceof(File)).optional(),
      prestataire: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      dateDecouverte: z
        .string()
        .optional()
        .refine(val => !val || isValidBoundedDate(val), { message: t(VALIDATION_FORMAT_DATE_INVALIDE) }),
      montant: optionalStringFromForm(t),
      assurance: z.boolean().nullish(),
      emailCommandeInconnu: z.boolean().optional(),
      emailCommande: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      telephoneCommandeInconnu: z.boolean().optional(),
      telephoneCommande: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
      livraisonAdresseLesee: z.boolean().nullish(),
      livraisonAdresse: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
      livraisonAdressePostale: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
      livraisonNpa: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
      livraisonLocalite: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
      livraisonLocaliteCode: z.string().nullable().optional(),
      livraisonPays: z.string().optional(),
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
      montantDelitAchatLigne: optionalStringFromForm(t),
      articleNonLivreDescription: z.string().max(TEXTAREA_MAX_LENGTH, t("validation.longueurMax", { max: TEXTAREA_MAX_LENGTH })).optional(),
      prenomVendeur: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
      nomVendeur: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
      telephoneVendeurInconnu: z.boolean().optional(),
      telephoneVendeur: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
      emailVendeurInconnu: z.boolean().optional(),
      emailVendeur: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      adresseVendeurInconnue: z.boolean().optional(),
      vendeurAdresse: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
      vendeurAdressePostale: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
      vendeurNpa: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
      vendeurLocalite: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
      vendeurLocaliteCode: z.string().nullable().optional(),
      vendeurPays: z.string().optional(),
      achatViaPlaceMarche: z.boolean().nullish(),
      plateforme: z.string().optional(),
      plateformeAutre: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
      plateformeId: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      nomEntrepriseVendeur: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      siteWebEntrepriseVendeur: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      annonceDocument: z.array(z.instanceof(File)).optional(),
      annonceDocumentIndisponible: z.boolean().optional(),
      raisonAbsenceAnnonce: z.string().max(TEXTAREA_MAX_LENGTH, t("validation.longueurMax", { max: TEXTAREA_MAX_LENGTH })).optional(),
      moyenPaiement: z.string().optional(),
      moyenPaiementAutre: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
      ibanBeneficiaire: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      comptePaypalBeneficiaire: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      numeroTwintBeneficiaire: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      adresseWalletCrypto: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      hashTransactionCrypto: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      societeBeneficiaire: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
      nomBeneficiaire: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
      prenomBeneficiaire: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
      dateOperation: z
        .string()
        .nullable()
        .optional()
        .refine(val => !val || isValidBoundedDate(val), { message: t(VALIDATION_FORMAT_DATE_INVALIDE) }),
      preuvePaiementDocument: z.array(z.instanceof(File)).optional(),
      preuvePaiementIndisponible: z.boolean().optional(),
      raisonAbsencePreuvePaiement: z.string().max(TEXTAREA_MAX_LENGTH, t("validation.longueurMax", { max: TEXTAREA_MAX_LENGTH })).optional(),
      copieIdentiteTransmiseAuteur: z.boolean().nullish(),
      copieIdentiteTransmiseAuteurDocument: z.array(z.instanceof(File)).optional(),
      copieIdentiteAuteurTransmise: z.boolean().nullish(),
      copieIdentiteAuteurDocument: z.array(z.instanceof(File)).optional(),
      titreAnnonce: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
      nomBailleur: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
      emailBailleurInconnu: z.boolean().optional(),
      emailBailleur: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
      telephoneBailleurInconnu: z.boolean().optional(),
      telephoneBailleur: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
      adresseBienImmobilier: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
      montantDemande: optionalStringFromForm(t),
      modePaiementDemande: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
      urlComplete: z.string().max(TEXT_FIELD_MAX_LENGTH, t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH })).nullable().optional(),
    })
    .refine(
      data =>
        data.plainteDeposee !== "Oui" || (data.detailsPlainteDeposee && data.detailsPlainteDeposee.trim().length > 0),
      {
        message: t("validation.detailsPlainteDeposeeRequis"),
        path: ["detailsPlainteDeposee"],
      },
    )
    .superRefine((data, ctx) => {
      if (
        data.typeIncident !== "cybercrime" &&
        data.adresseConnue &&
        (!data.adresseEvenement || data.adresseEvenement.length < MIN_ADRESSE_EVENEMENT_TAILLE)
      ) {
        addCustomIssue(ctx, "adresseEvenement", t("validation.adresseEvenementRequise"));
      }

      if (data.typeIncident !== "cybercrime" && data.adresseConnue) {
        const value = data.adressePostaleEvenement?.trim() ?? "";
        if (!/^[a-zA-Z0-9\s]*$/.test(value)) {
          addCustomIssue(ctx, "adressePostaleEvenement", t("validation.numeroPostalFormat"));
        }
      }

      if (data.typeIncident !== "cybercrime" && data.adresseConnue && !data.localiteEvenement) {
        addCustomIssue(ctx, "localiteEvenement", t("validation.localiteRequise"));
      }

      if (
        data.typeIncident !== "cybercrime" &&
        data.adresseConnue &&
        (!data.npaEvenement || data.npaEvenement.length < 4)
      ) {
        addCustomIssue(ctx, "npaEvenement", t("validation.npaFormat"));
      }

      if (data.typeIncident !== "cybercrime" && data.adresseLesee === null) {
        addCustomIssue(ctx, "adresseLesee", t("validation.champRequis"));
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
    })
    .superRefine((data, ctx) => validateAchatNonRecuCybercrime(data, ctx, addCustomIssue, t))
    .superRefine((data, ctx) => {
      if (data.typeCybercrime === "fausse-annonce") {
        if (
          data.urlComplete?.trim() &&
          !isUrlWebAvecDomaine(data.urlComplete)
        ) {
          addCustomIssue(ctx, "urlComplete", t("validation.plateformeUrlOuIdInvalide"));
        }

        const hasEmail = data.emailBailleur?.trim();
        if (!data.emailBailleurInconnu && !hasEmail) {
          addCustomIssue(ctx, "emailBailleur", t("validation.emailBailleurRequis"));
        } else if (
          data.emailBailleur?.trim() &&
          !z.string().email().safeParse(data.emailBailleur).success
        ) {
          addCustomIssue(ctx, "emailBailleur", t("validation.emailBailleurFormat"));
        }

        const hasTelephone = data.telephoneBailleur?.trim();
        if (!data.telephoneBailleurInconnu && !hasTelephone) {
          addCustomIssue(ctx, "telephoneBailleur", t("validation.telephoneBailleurRequis"));
        }
      }
    });

export const createIncidentSchema = (t: ComposerTranslation, nationalite: string) => {
  const isCH = (v?: string) => {
    const value = (v ?? "").toUpperCase();
    return value === "SUISSE" || value === "CH" || value === RIPOL.PAYS_SUISSE;
  };

  return createEvenementInfoSchema(t).superRefine((data, ctx) => {
    if (!(isCH(data.paysEvenement) || isCH(nationalite))) {
      addCustomIssue(ctx, "paysEvenement", t("validation.paysOuNationaliteSuisse"));
    }
  });
};
