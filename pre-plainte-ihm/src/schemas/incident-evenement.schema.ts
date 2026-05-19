import { z } from "zod";
import type { ComposerTranslation } from "vue-i18n";
import { isCybercrimeTypeWithoutDetailFields, RIPOL } from "@/constants/constant";
import { isValidBoundedDate, parseDate, parseTime } from "@/utils/helpers/dateHelpers.ts";
import { validateAchatNonRecuCybercrime } from "@/schemas/incident-evenement-achat-non-recu-refine";
import { isUrlWebAvecDomaine } from "@/utils/validations/field-validation.utils";

const MIN_ADRESSE_EVENEMENT_TAILLE = 5;
const VALIDATION_FORMAT_DATE_INVALIDE = "validation.formatDateInvalide";
const VALIDATION_FORMAT_HEURE_INVALIDE = "validation.formatHeureInvalide";

const optionalStringFromForm = z.preprocess(
  v => (v === null || v === undefined ? "" : String(v)),
  z.string().optional(),
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
    { field: "descriptionObjet", message: t("validation.descriptionObjetRequise") },
    { field: "avezVousDegradation", message: t("validation.degradationsRequis") },
  ],
  "degat-delit": [
    { field: "typeDommage", message: t("validation.typeDommageRequis") },
    { field: "montantEstime", message: t("validation.montantDommageRequis") },
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
    rules = rules.filter(r => r.field !== "typeObjet" && r.field !== "descriptionObjet");
  }

  rules.forEach(({ field, message }) => {
    validateIncidentRequirement(data, ctx, field, message);
  });
};

const validateVolSpecificRules = (data: Record<string, any>, ctx: z.RefinementCtx, t: ComposerTranslation) => {
  if (data.typeIncident !== "vol") {
    return;
  }


  if (hasObjetsVolesEnregistres(data)) {
    return;
  }

  if (data.categorieObjet === "plaque") {
    if (!data.plaquePays?.code) {
      addCustomIssue(ctx, "plaquePays", t("validation.champRequis"));
    }
    if (!data.plaqueNumero?.trim()) {
      addCustomIssue(ctx, "plaqueNumero", t("validation.champRequis"));
    }
  }

  if (
    data.categorieObjet !== "vehicule" &&
    data.categorieObjet !== "plaque" &&
    !data.numeroSerieInconnu &&
    !data.numeroSerie?.trim()
  ) {
    addCustomIssue(ctx, "numeroSerie", t("validation.numeroSerieRequis"));
  }

  if (data.typeObjet?.code === RIPOL.CODE_TELEPHONE_MOBILE && !data.numeroIMEIInconnu && !data.numeroIMEI?.trim()) {
    addCustomIssue(ctx, "numeroIMEI", t("validation.numeroIMEIRequis"));
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
      adresseEvenement: z.string().optional(),
      typeLieu: optionalRipolSelectionSchema,
      adresseConnue: z.boolean().nullish(),
      adresseLesee: z.boolean().nullish(),
      isTrajet: z.boolean().nullish(),
      adressePostaleEvenement: z.string().optional(),
      npaEvenement: z.string().optional(),
      localiteEvenement: z.string().optional(),
      paysEvenement: z.string().optional(),
      adresseEvenementSecondaire: z.string().optional(),
      adressePostaleEvenementSecondaire: z.string().optional(),
      npaEvenementSecondaire: z.string().optional(),
      localiteEvenementSecondaire: z.string().optional(),
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
      fabricantAutre: z.string().optional(),
      modele: optionalRipolSelectionSchema,
      modeleAutre: z.string().optional(),
      couleur: optionalRipolSelectionSchema,
      couleurSecondaire: optionalRipolSelectionSchema,
      valeurReelle: z.string().optional(),
      numeroSerie: z.string().optional(),
      numeroSerieInconnu: z.boolean().optional(),
      numeroCadre: z.string().optional(),
      numeroCadreInconnu: z.boolean().optional(),
      numeroIMEI: z.string().optional(),
      numeroIMEIInconnu: z.boolean().optional(),
      justificationAbsenceIMEI: z.string().optional(),
      gravure: z.string().optional(),
      descriptionObjet: z.string().optional(),
      isVehicle: z.boolean().optional(),
      dateAchat: z
        .string()
        .optional()
        .refine(val => !val || isValidBoundedDate(val), { message: t(VALIDATION_FORMAT_DATE_INVALIDE) }),
      vin: z.string().optional(),
      vinInconnu: z.boolean().optional(),
      velofinderId: z.string().optional(),
      plaqueNumero: z.string().optional(),
      plaqueInconnu: z.boolean().optional(),
      plaquePays: optionalRipolSelectionSchema,
      plaqueCanton: optionalRipolSelectionSchema,
      avezVousDegradation: z.boolean().nullish(),
      montantEstime: z.string().optional(),
      devise: z.string().optional(),
      typeDommage: z.string().optional(),
      naturesDommage: z
        .array(z.string(), {
          errorMap: () => ({ message: t("validation.natureDommageFormat") }),
        })
        .optional(),
      description: z.string().optional(),
      dateConstat: z
        .string()
        .optional()
        .refine(val => !val || isValidBoundedDate(val), { message: t(VALIDATION_FORMAT_DATE_INVALIDE) }),
      constatPresent: z.boolean().nullish(),
      fichiers: z.array(z.instanceof(File)).optional(),
      typeCybercrime: z.string().optional(),
      descriptionCybercrime: z.string().optional(),
      justificatifsPaiement: z.array(z.instanceof(File)).optional(),
      copiesEcran: z.array(z.instanceof(File)).optional(),
      autresDocuments: z.array(z.instanceof(File)).optional(),
      prestataire: z.string().optional(),
      dateDecouverte: z
        .string()
        .optional()
        .refine(val => !val || isValidBoundedDate(val), { message: t(VALIDATION_FORMAT_DATE_INVALIDE) }),
      montant: optionalStringFromForm,
      assurance: z.boolean().nullish(),
      emailCommandeInconnu: z.boolean().optional(),
      emailCommande: z.string().optional(),
      telephoneCommandeInconnu: z.boolean().optional(),
      telephoneCommande: z.string().nullable().optional(),
      livraisonAdresseLesee: z.boolean().nullish(),
      livraisonAdresse: z.string().nullable().optional(),
      livraisonAdressePostale: z.string().nullable().optional(),
      livraisonNpa: z.string().nullable().optional(),
      livraisonLocalite: z.string().nullable().optional(),
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
      montantDelitAchatLigne: optionalStringFromForm,
      articleNonLivreDescription: z.string().optional(),
      prenomVendeur: z.string().nullable().optional(),
      nomVendeur: z.string().nullable().optional(),
      telephoneVendeurInconnu: z.boolean().optional(),
      telephoneVendeur: z.string().nullable().optional(),
      emailVendeurInconnu: z.boolean().optional(),
      emailVendeur: z.string().optional(),
      adresseVendeurInconnue: z.boolean().optional(),
      vendeurAdresse: z.string().nullable().optional(),
      vendeurAdressePostale: z.string().nullable().optional(),
      vendeurNpa: z.string().nullable().optional(),
      vendeurLocalite: z.string().nullable().optional(),
      vendeurLocaliteCode: z.string().nullable().optional(),
      vendeurPays: z.string().optional(),
      achatViaPlaceMarche: z.boolean().nullish(),
      plateforme: z.string().optional(),
      plateformeAutre: z.string().nullable().optional(),
      plateformeId: z.string().optional(),
      nomEntrepriseVendeur: z.string().optional(),
      siteWebEntrepriseVendeur: z.string().optional(),
      annonceDocument: z.array(z.instanceof(File)).optional(),
      annonceDocumentIndisponible: z.boolean().optional(),
      raisonAbsenceAnnonce: z.string().optional(),
      moyenPaiement: z.string().optional(),
      moyenPaiementAutre: z.string().nullable().optional(),
      ibanBeneficiaire: z.string().optional(),
      comptePaypalBeneficiaire: z.string().optional(),
      numeroTwintBeneficiaire: z.string().optional(),
      adresseWalletCrypto: z.string().optional(),
      hashTransactionCrypto: z.string().optional(),
      societeBeneficiaire: z.string().nullable().optional(),
      nomBeneficiaire: z.string().nullable().optional(),
      prenomBeneficiaire: z.string().nullable().optional(),
      dateOperation: z
        .string()
        .nullable()
        .optional()
        .refine(val => !val || isValidBoundedDate(val), { message: t(VALIDATION_FORMAT_DATE_INVALIDE) }),
      preuvePaiementDocument: z.array(z.instanceof(File)).optional(),
      preuvePaiementIndisponible: z.boolean().optional(),
      raisonAbsencePreuvePaiement: z.string().optional(),
      copieIdentiteTransmiseAuteur: z.boolean().nullish(),
      copieIdentiteTransmiseAuteurDocument: z.array(z.instanceof(File)).optional(),
      copieIdentiteAuteurTransmise: z.boolean().nullish(),
      copieIdentiteAuteurDocument: z.array(z.instanceof(File)).optional(),
      titreAnnonce: z.string().nullable().optional(),
      nomBailleur: z.string().nullable().optional(),
      emailBailleurInconnu: z.boolean().optional(),
      emailBailleur: z.string().nullable().optional(),
      telephoneBailleurInconnu: z.boolean().optional(),
      telephoneBailleur: z.string().nullable().optional(),
      adresseBienImmobilier: z.string().nullable().optional(),
      montantDemande: z.string().nullable().optional(),
      modePaiementDemande: z.string().nullable().optional(),
      urlComplete: z.string().nullable().optional(),
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
