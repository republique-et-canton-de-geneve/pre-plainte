import { z } from "zod";
import type { ComposerTranslation } from "vue-i18n";
import { TEXT_FIELD_MAX_LENGTH, VALIDATION_LIMITS } from "@/constants/constant";
import { validateInternationalPhone } from "@/utils/validations/phoneValidation";
import { calculateAge, parseDate } from "@/utils/helpers/dateHelpers.ts";

const INFORMATIONS_PERSONNELLES_TIERS = "TIERS";
const INFORMATIONS_PERSONNELLES_MON_ENTREPRISE = "ENTREPRISE";

const AGE_MIN = 16;
const AGE_MAX = 120;

const VALIDATION_LONGUEUR_MAX = "validation.longueurMax";
const VALIDATION_NUMERO_POSTAL_FORMAT = "validation.numeroPostalFormat";
const VALIDATION_NPA_MIN = "validation.npaMin";
const VALIDATION_LOCALITE_REQUISE = "validation.localiteRequise";
const VALIDATION_TELEPHONE_FORMAT = "validation.telephoneFormat";
const VALIDATION_SELECTION_DOCUMENT = "validation.selectionnerDocument";
const VALIDATION_NOM_MIN = "validation.nomMin";
const VALIDATION_ADRESSE_MIN = "validation.adresseMin";
const VALIDATION_EMAIL_INVALIDE = "validation.emailInvalide";

const ripolSelectionSchema = (errorMessage: string) =>
  z
    .object({
      code: z.string(),
      label: z.string(),
    })
    .nullable()
    .refine(val => val !== null && val.code.length > 0, { message: errorMessage });

const optionalRipolSelectionSchema = z
  .object({
    code: z.string(),
    label: z.string(),
  })
  .nullable()
  .optional();

const hasValue = (value: unknown) => {
  if (Array.isArray(value)) {
    return value.length > 0;
  }
  if (typeof value === "string") {
    return value.trim().length > 0;
  }
  return !!value;
};

const addCustomIssue = (ctx: z.RefinementCtx, path: string, message: string) => {
  ctx.addIssue({
    code: z.ZodIssueCode.custom,
    path: [path],
    message,
  });
};

export const createInfosPersonnellesSchema = (t: ComposerTranslation) => {
  return z
    .object({
      lienAvecPersonne: z.string().min(1, t("validation.lienAvecPersonneRequis")),
      typeRepresentation: z.string().optional(),
      postePersonneMorale: z.string().optional(),
      justificatifPersonneMorale: z.array(z.instanceof(File)).optional(),

      nom: z
        .string()
        .min(VALIDATION_LIMITS.NOM_MIN, t(VALIDATION_NOM_MIN, { min: VALIDATION_LIMITS.NOM_MIN }))
        .max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })),

      nomNaissance: z
        .string()
        .max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH }))
        .optional(),

      prenom: z
        .string()
        .min(VALIDATION_LIMITS.PRENOM_MIN, t("validation.prenomMin", { min: VALIDATION_LIMITS.PRENOM_MIN }))
        .max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })),

      adresse: z
        .string()
        .min(VALIDATION_LIMITS.ADRESSE_MIN, t(VALIDATION_ADRESSE_MIN, { min: VALIDATION_LIMITS.ADRESSE_MIN }))
        .max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })),

      pays: z.string().min(1, t("validation.paysRequis")),

      genre: ripolSelectionSchema(t("validation.genreRequis")),

      nationalite: ripolSelectionSchema(t("validation.nationaliteRequise")),

      titreSejour: z.string().optional(),

      adressePostale: z
        .string()
        .trim()
        .max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH }))
        .regex(/^[a-zA-Z0-9\s]*$/, t(VALIDATION_NUMERO_POSTAL_FORMAT)),

      npa: z
        .string()
        .min(1, t(VALIDATION_NPA_MIN))
        .max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH }))
        .refine(val => /^\d+$/.test(val), t("validation.npaFormat")),

      localite: z
        .string().min(2, t(VALIDATION_LOCALITE_REQUISE))
        .max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })),

      dateNaissance: z
        .string()
        .min(1, t("validation.dateNaissanceRequise"))
        .refine(value => parseDate(value) !== null, { message: t("validation.formatDateInvalide") })
        .refine(
          value => {
            const birthDate = parseDate(value);
            if (!birthDate) {
              return false;
            }
            const age = calculateAge(birthDate);
            return age >= AGE_MIN && age <= AGE_MAX;
          },
          {
            message: t("validation.ageInvalide"),
          },
        ),

      telephone: z
        .string()
        .min(1, t(VALIDATION_TELEPHONE_FORMAT))
        .refine(validateInternationalPhone, t(VALIDATION_TELEPHONE_FORMAT)),

      typeDocumentIdentite: z
        .string({
          required_error: t(VALIDATION_SELECTION_DOCUMENT),
          invalid_type_error: t(VALIDATION_SELECTION_DOCUMENT),
        })
        .min(1, t(VALIDATION_SELECTION_DOCUMENT)),

      numeroDocumentIdentite: z.preprocess(
        val => (typeof val === "string" ? val.trim() : val),
        z
          .string()
          .max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH }))
          .optional(),
      ),

      tiersTypeDocumentIdentite: z.string().optional(),

      tiersNumeroDocumentIdentite: z.preprocess(
        val => (typeof val === "string" ? val.trim() : val),
        z
          .string()
          .max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH }))
          .optional(),
      ),

      tiersNom: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      tiersPrenom: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      tiersGenre: optionalRipolSelectionSchema,
      tiersNationalite: optionalRipolSelectionSchema,
      tiersDateNaissance: z.string().optional(),
      tiersAdresse: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      tiersAdressePostale: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      tiersNpa: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      tiersLocalite: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      tiersPays: z.string().optional(),
      tiersTelephone: z.string().optional(),
      tiersEmail: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      tiersConfirmationEmail: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),

      organisationNom: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      organisationAdresse: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      organisationAdressePostale: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      organisationNpa: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      organisationLocalite: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      organisationPays: z.string().optional(),
      organisationTelephone: z.string().optional(),
      organisationEmail: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
      organisationConfirmationEmail: z.string().max(TEXT_FIELD_MAX_LENGTH, t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH })).optional(),
    })
    .superRefine((data, ctx) => {
      addBaseValidation(t, data, ctx);
      addTiersValidation(t, data, ctx);
      addOrganisationValidation(t, data, ctx);
    });
};

const addBaseValidation = (t: ComposerTranslation, data: any, ctx: z.RefinementCtx,) => {
  if (
    data.typeDocumentIdentite !== "documents_voles_perdus"
    && !data.numeroDocumentIdentite
  ) {
    addCustomIssue(ctx, "numeroDocumentIdentite", t("validation.numeroDocumentRequis"));
  }

  const isSuisse = data.nationalite?.label?.toLowerCase().includes("suisse");

  if (
    data.nationalite
    && !isSuisse
    && !data.titreSejour
  ) {
    addCustomIssue(ctx, "titreSejour", t("validation.titreSejourRequis"));
  }

  if (
    data.lienAvecPersonne === INFORMATIONS_PERSONNELLES_TIERS
    && !data.typeRepresentation
  ) {
    addCustomIssue(ctx, "typeRepresentation", t("validation.typeRepresentationRequis"));
  }

  if (
    data.lienAvecPersonne === INFORMATIONS_PERSONNELLES_MON_ENTREPRISE
    && !data.postePersonneMorale
  ) {
    addCustomIssue(ctx, "postePersonneMorale", t("validation.postePersonneMoraleRequis"));
  }
};

const validateTiersIdentity = (t: ComposerTranslation, data: any, ctx: z.RefinementCtx,) => {
  if (!hasValue(data.tiersTypeDocumentIdentite)) {
    addCustomIssue(ctx, "tiersTypeDocumentIdentite", t(VALIDATION_SELECTION_DOCUMENT));
  }

  if (
    data.tiersTypeDocumentIdentite !== "documents_voles_perdus"
    && !hasValue(data.tiersNumeroDocumentIdentite)
  ) {
    addCustomIssue(ctx, "tiersNumeroDocumentIdentite", t("validation.numeroDocumentRequis"));
  }
};

const validateTiersPerson = (t: ComposerTranslation, data: any, ctx: z.RefinementCtx,) => {
  if (!data.tiersNom || data.tiersNom.length < VALIDATION_LIMITS.NOM_MIN) {
    addCustomIssue(ctx, "tiersNom", t(VALIDATION_NOM_MIN, { min: VALIDATION_LIMITS.NOM_MIN }));
  }

  if (!data.tiersPrenom || data.tiersPrenom.length < VALIDATION_LIMITS.PRENOM_MIN) {
    addCustomIssue(ctx, "tiersPrenom", t("validation.prenomMin", { min: VALIDATION_LIMITS.PRENOM_MIN }));
  }

  if (!data.tiersGenre?.code) {
    addCustomIssue(ctx, "tiersGenre", t("validation.genreRequis"));
  }

  if (!data.tiersNationalite?.code) {
    addCustomIssue(ctx, "tiersNationalite", t("validation.nationaliteRequise"));
  }
};

const validateTiersBirthDate = (t: ComposerTranslation, data: any, ctx: z.RefinementCtx,) => {
  if (!data.tiersDateNaissance) {
    addCustomIssue(ctx, "tiersDateNaissance", t("validation.dateNaissanceRequise"));
    return;
  }

  const birthDate = parseDate(data.tiersDateNaissance);

  if (!birthDate) {
    addCustomIssue(ctx, "tiersDateNaissance", t("validation.formatDateInvalide"));
    return;
  }

  const age = calculateAge(birthDate);

  if (age < AGE_MIN || age > AGE_MAX) {
    addCustomIssue(ctx, "tiersDateNaissance", t("validation.ageInvalide"));
  }
};

const validateTiersAddress = (t: ComposerTranslation, data: any, ctx: z.RefinementCtx,) => {
  if (!data.tiersAdresse || data.tiersAdresse.length < VALIDATION_LIMITS.ADRESSE_MIN) {
    addCustomIssue(ctx, "tiersAdresse", t(VALIDATION_ADRESSE_MIN, { min: VALIDATION_LIMITS.ADRESSE_MIN }));
  }

  if (!/^[a-zA-Z0-9\s]*$/.test(data.tiersAdressePostale?.trim() ?? "")) {
    addCustomIssue(ctx, "tiersAdressePostale", t(VALIDATION_NUMERO_POSTAL_FORMAT));
  }

  if (!data.tiersNpa || !/^\d+$/.test(data.tiersNpa)) {
    addCustomIssue(ctx, "tiersNpa", t(VALIDATION_NPA_MIN));
  }

  if (!data.tiersLocalite || data.tiersLocalite.length < 2) {
    addCustomIssue(ctx, "tiersLocalite", t(VALIDATION_LOCALITE_REQUISE));
  }
};

const validateTiersContact = (t: ComposerTranslation, data: any, ctx: z.RefinementCtx,) => {
  if (!data.tiersTelephone || !validateInternationalPhone(data.tiersTelephone)) {
    addCustomIssue(ctx, "tiersTelephone", t(VALIDATION_TELEPHONE_FORMAT));
  }

  if (!data.tiersEmail || !z.string().email().safeParse(data.tiersEmail).success) {
    addCustomIssue(ctx, "tiersEmail", t(VALIDATION_EMAIL_INVALIDE));
  }

  if (
    !data.tiersConfirmationEmail
    || !z.string().email().safeParse(data.tiersConfirmationEmail).success
  ) {
    addCustomIssue(ctx, "tiersConfirmationEmail", t(VALIDATION_EMAIL_INVALIDE));
  }

  if (data.tiersEmail !== data.tiersConfirmationEmail) {
    addCustomIssue(ctx, "tiersConfirmationEmail", t("validation.emailsDifferent"));
  }
};

const addTiersValidation = (t: ComposerTranslation, data: any, ctx: z.RefinementCtx,) => {
  if (data.lienAvecPersonne !== INFORMATIONS_PERSONNELLES_TIERS) {
    return;
  }

  validateTiersIdentity(t, data, ctx);
  validateTiersPerson(t, data, ctx);
  validateTiersBirthDate(t, data, ctx);
  validateTiersAddress(t, data, ctx);
  validateTiersContact(t, data, ctx);
};

const validateOrganisationIdentity = (t: ComposerTranslation, data: any, ctx: z.RefinementCtx,) => {
  if (!data.organisationNom || data.organisationNom.length < VALIDATION_LIMITS.NOM_MIN) {
    addCustomIssue(ctx, "organisationNom", t(VALIDATION_NOM_MIN, { min: VALIDATION_LIMITS.NOM_MIN }));
  }
};

const validateOrganisationAddress = (t: ComposerTranslation, data: any, ctx: z.RefinementCtx,) => {
  if (!data.organisationAdresse || data.organisationAdresse.length < VALIDATION_LIMITS.ADRESSE_MIN) {
    addCustomIssue(ctx, "organisationAdresse", t(VALIDATION_ADRESSE_MIN, { min: VALIDATION_LIMITS.ADRESSE_MIN }));
  }

  if (!/^[a-zA-Z0-9\s]*$/.test(data.organisationAdressePostale?.trim() ?? "")) {
    addCustomIssue(ctx, "organisationAdressePostale", t(VALIDATION_NUMERO_POSTAL_FORMAT));
  }

  if (!data.organisationNpa || !/^\d+$/.test(data.organisationNpa)) {
    addCustomIssue(ctx, "organisationNpa", t(VALIDATION_NPA_MIN));
  }

  if (!data.organisationLocalite || data.organisationLocalite.length < 2) {
    addCustomIssue(ctx, "organisationLocalite", t(VALIDATION_LOCALITE_REQUISE));
  }
};

const validateOrganisationContact = (t: ComposerTranslation, data: any, ctx: z.RefinementCtx,) => {
  if (!data.organisationTelephone || !validateInternationalPhone(data.organisationTelephone)) {
    addCustomIssue(ctx, "organisationTelephone", t(VALIDATION_TELEPHONE_FORMAT));
  }

  if (!data.organisationEmail || !z.string().email().safeParse(data.organisationEmail).success) {
    addCustomIssue(ctx, "organisationEmail", t(VALIDATION_EMAIL_INVALIDE));
  }

  if (
    !data.organisationConfirmationEmail
    || !z.string().email().safeParse(data.organisationConfirmationEmail).success
  ) {
    addCustomIssue(ctx, "organisationConfirmationEmail", t(VALIDATION_EMAIL_INVALIDE));
  }

  if (data.organisationEmail !== data.organisationConfirmationEmail) {
    addCustomIssue(ctx, "organisationConfirmationEmail", t("validation.emailsDifferent"));
  }
};

const addOrganisationValidation = (t: ComposerTranslation, data: any, ctx: z.RefinementCtx,) => {
  if (data.lienAvecPersonne !== INFORMATIONS_PERSONNELLES_MON_ENTREPRISE) {
    return;
  }

  validateOrganisationIdentity(t, data, ctx);
  validateOrganisationAddress(t, data, ctx);
  validateOrganisationContact(t, data, ctx);
};
