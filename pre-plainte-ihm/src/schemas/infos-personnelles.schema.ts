import { z } from "zod";
import type { ComposerTranslation } from "vue-i18n";
import { VALIDATION_LIMITS } from "@/constants/constant";
import { validateInternationalPhone } from "@/utils/validations/phoneValidation";
import { calculateAge, parseDate } from "@/utils/helpers/dateHelpers.ts";

const INFORMATIONS_PERSONNELLES_TIERS = "TIERS";
const INFORMATIONS_PERSONNELLES_MON_ENTREPRISE = "ENTREPRISE";

const AGE_MIN = 16;
const AGE_MAX = 120;

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

const hasValue = (value: unknown) =>
  Array.isArray(value) ? value.length > 0 : typeof value === "string" ? value.trim().length > 0 : !!value;

export const createInfosPersonnellesSchema = (t: ComposerTranslation) => {
  const isTiers = (lienAvecPersonne?: string) => lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_TIERS);

  const isRequiredWhenTiers = (lienAvecPersonne: string | undefined, value: unknown) =>
    !isTiers(lienAvecPersonne) || hasValue(value);

  return z
    .object({
      lienAvecPersonne: z.string().min(1, t("validation.lienAvecPersonneRequis")),

      typeRepresentation: z.string().optional(),

      postePersonneMorale: z.string().optional(),

      justificatifPersonneMorale: z.array(z.instanceof(File)).optional(),

      nom: z.string().min(VALIDATION_LIMITS.NOM_MIN, t("validation.nomMin", { min: VALIDATION_LIMITS.NOM_MIN })),

      nomNaissance: z.string().optional(),

      prenom: z
        .string()
        .min(VALIDATION_LIMITS.PRENOM_MIN, t("validation.prenomMin", { min: VALIDATION_LIMITS.PRENOM_MIN })),

      adresse: z
        .string()
        .min(VALIDATION_LIMITS.ADRESSE_MIN, t("validation.adresseMin", { min: VALIDATION_LIMITS.ADRESSE_MIN })),

      pays: z.string().min(1, t("validation.paysRequis")),

      genre: ripolSelectionSchema(t("validation.genreRequis")),

      nationalite: ripolSelectionSchema(t("validation.nationaliteRequise")),

      titreSejour: z.string().optional(),

      adressePostale: z
        .string()
        .trim()
        .regex(/^[a-zA-Z0-9\s]*$/, t("validation.numeroPostalFormat")),

      npa: z
        .string()
        .min(1, t("validation.npaMin"))
        .refine(val => /^\d+$/.test(val), t("validation.npaFormat")),

      localite: z.string().min(2, t("validation.localiteRequise")),

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
        .min(1, t("validation.telephoneFormat"))
        .refine(validateInternationalPhone, t("validation.telephoneFormat")),

      typeDocumentIdentite: z
        .string({
          required_error: t("validation.selectionnerDocument"),
          invalid_type_error: t("validation.selectionnerDocument"),
        })
        .min(1, t("validation.selectionnerDocument")),

      numeroDocumentIdentite: z.preprocess(
        val => (val == null || val === undefined ? val : String(val).trim()),
        z.string().min(1, t("validation.numeroDocumentRequis")),
      ),

      tiersTypeDocumentIdentite: z.string().optional(),

      tiersNumeroDocumentIdentite: z.preprocess(
        val => (val == null || val === undefined ? val : String(val).trim()),
        z.string().optional(),
      ),

      tiersNom: z.string().optional(),
      tiersPrenom: z.string().optional(),
      tiersGenre: optionalRipolSelectionSchema,
      tiersNationalite: optionalRipolSelectionSchema,
      tiersDateNaissance: z.string().optional(),
      tiersAdresse: z.string().optional(),
      tiersAdressePostale: z.string().optional(),
      tiersNpa: z.string().optional(),
      tiersLocalite: z.string().optional(),
      tiersPays: z.string().optional(),
      tiersTelephone: z.string().optional(),
      tiersEmail: z.string().optional(),
      tiersConfirmationEmail: z.string().optional(),

      organisationNom: z.string().optional(),
      organisationAdresse: z.string().optional(),
      organisationAdressePostale: z.string().optional(),
      organisationNpa: z.string().optional(),
      organisationLocalite: z.string().optional(),
      organisationPays: z.string().optional(),
      organisationTelephone: z.string().optional(),
      organisationEmail: z.string().optional(),
      organisationConfirmationEmail: z.string().optional(),
    })
    .refine(
      data => {
        return !!(data.numeroDocumentIdentite && data.numeroDocumentIdentite.length > 0);
      },
      {
        message: t("validation.numeroDocumentRequis"),
        path: ["numeroDocumentIdentite"],
      },
    )
    .refine(data => isRequiredWhenTiers(data.lienAvecPersonne, data.tiersTypeDocumentIdentite), {
      message: t("validation.selectionnerDocument"),
      path: ["tiersTypeDocumentIdentite"],
    })
    .refine(data => isRequiredWhenTiers(data.lienAvecPersonne, data.tiersNumeroDocumentIdentite), {
      message: t("validation.numeroDocumentRequis"),
      path: ["tiersNumeroDocumentIdentite"],
    })
    .refine(
      data => {
        const isSuisse = data.nationalite?.label?.toLowerCase().includes("suisse");
        if (data.nationalite && !isSuisse) {
          return !!(data.titreSejour && data.titreSejour.length > 0);
        }
        return true;
      },
      {
        message: t("validation.titreSejourRequis"),
        path: ["titreSejour"],
      },
    )
    .refine(
      data => {
        if (data.lienAvecPersonne && data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_TIERS)) {
          return !!(data.typeRepresentation && data.typeRepresentation.length > 0);
        }
        return true;
      },
      {
        message: t("validation.typeRepresentationRequis"),
        path: ["typeRepresentation"],
      },
    )
    .refine(
      data => {
        if (data.lienAvecPersonne && data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_MON_ENTREPRISE)) {
          return !!(data.postePersonneMorale && data.postePersonneMorale.length > 0);
        }
        return true;
      },
      {
        message: t("validation.postePersonneMoraleRequis"),
        path: ["postePersonneMorale"],
      },
    )
    .refine(
      data => {
        const isTiers = data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_TIERS);
        if (isTiers) {
          return !!(data.tiersNom && data.tiersNom.length >= VALIDATION_LIMITS.NOM_MIN);
        }
        return true;
      },
      {
        message: t("validation.nomMin", { min: VALIDATION_LIMITS.NOM_MIN }),
        path: ["tiersNom"],
      },
    )
    .refine(
      data => {
        const isTiers = data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_TIERS);
        if (isTiers) {
          return !!(data.tiersPrenom && data.tiersPrenom.length >= VALIDATION_LIMITS.PRENOM_MIN);
        }
        return true;
      },
      {
        message: t("validation.prenomMin", { min: VALIDATION_LIMITS.PRENOM_MIN }),
        path: ["tiersPrenom"],
      },
    )
    .refine(
      data => {
        const isTiers = data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_TIERS);
        if (isTiers) {
          return !!data.tiersGenre?.code;
        }
        return true;
      },
      {
        message: t("validation.genreRequis"),
        path: ["tiersGenre"],
      },
    )
    .refine(
      data => {
        const isTiers = data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_TIERS);
        if (isTiers) {
          return !!data.tiersNationalite?.code;
        }
        return true;
      },
      {
        message: t("validation.nationaliteRequise"),
        path: ["tiersNationalite"],
      },
    )
    .refine(
      data => {
        const isTiers = data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_TIERS);
        if (isTiers) {
          return !!(data.tiersDateNaissance && data.tiersDateNaissance.length > 0);
        }
        return true;
      },
      {
        message: t("validation.dateNaissanceRequise"),
        path: ["tiersDateNaissance"],
      },
    )
    .refine(
      data => {
        const isTiers = data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_TIERS);
        if (isTiers) {
          return !!(data.tiersDateNaissance && parseDate(data.tiersDateNaissance) !== null);
        }
        return true;
      },
      {
        message: t("validation.formatDateInvalide"),
        path: ["tiersDateNaissance"],
      },
    )
    .refine(
      data => {
        const isTiers = data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_TIERS);
        if (isTiers) {
          const birthDate = data.tiersDateNaissance ? parseDate(data.tiersDateNaissance) : null;
          if (!birthDate) {
            return false;
          }
          const age = calculateAge(birthDate);
          return age >= AGE_MIN && age <= AGE_MAX;
        }
        return true;
      },
      {
        message: t("validation.ageInvalide"),
        path: ["tiersDateNaissance"],
      },
    )
    .refine(
      data => {
        const isTiers = data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_TIERS);
        if (isTiers) {
          return !!(data.tiersAdresse && data.tiersAdresse.length >= VALIDATION_LIMITS.ADRESSE_MIN);
        }
        return true;
      },
      {
        message: t("validation.adresseMin", { min: VALIDATION_LIMITS.ADRESSE_MIN }),
        path: ["tiersAdresse"],
      },
    )
    .refine(
      data => {
        const isTiers = data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_TIERS);
        if (isTiers) {
          const value = data.tiersAdressePostale?.trim() ?? "";
          return /^[a-zA-Z0-9\s]*$/.test(value);
        }
        return true;
      },
      {
        message: t("validation.numeroPostalFormat"),
        path: ["tiersAdressePostale"],
      },
    )
    .refine(
      data => {
        const isTiers = data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_TIERS);
        if (isTiers) {
          return !!(data.tiersNpa && data.tiersNpa.length > 0 && /^\d+$/.test(data.tiersNpa));
        }
        return true;
      },
      {
        message: t("validation.npaMin"),
        path: ["tiersNpa"],
      },
    )
    .refine(
      data => {
        const isTiers = data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_TIERS);
        if (isTiers) {
          return !!(data.tiersLocalite && data.tiersLocalite.length >= 2);
        }
        return true;
      },
      {
        message: t("validation.localiteRequise"),
        path: ["tiersLocalite"],
      },
    )
    .refine(
      data => {
        const isTiers = data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_TIERS);
        if (isTiers) {
          return !!(
            data.tiersTelephone &&
            data.tiersTelephone.length > 0 &&
            validateInternationalPhone(data.tiersTelephone)
          );
        }
        return true;
      },
      {
        message: t("validation.telephoneFormat"),
        path: ["tiersTelephone"],
      },
    )
    .refine(
      data => {
        const isTiers = data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_TIERS);
        if (!isTiers) {
          return true;
        }
        return !!data.tiersEmail && z.string().email().safeParse(data.tiersEmail).success;
      },
      {
        message: t("validation.emailInvalide"),
        path: ["tiersEmail"],
      },
    )
    .refine(
      data => {
        const isTiers = data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_TIERS);
        if (!isTiers) {
          return true;
        }
        return !!data.tiersConfirmationEmail && z.string().email().safeParse(data.tiersConfirmationEmail).success;
      },
      {
        message: t("validation.emailInvalide"),
        path: ["tiersConfirmationEmail"],
      },
    )
    .refine(
      data => {
        const isTiers = data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_TIERS);
        if (isTiers) {
          return data.tiersEmail === data.tiersConfirmationEmail;
        }
        return true;
      },
      {
        message: t("validation.emailsDifferent"),
        path: ["tiersConfirmationEmail"],
      },
    )
    .refine(
      data => {
        const isOrganisation = data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_MON_ENTREPRISE);
        if (isOrganisation) {
          return !!(data.organisationNom && data.organisationNom.length >= VALIDATION_LIMITS.NOM_MIN);
        }
        return true;
      },
      {
        message: t("validation.nomMin", { min: VALIDATION_LIMITS.NOM_MIN }),
        path: ["organisationNom"],
      },
    )
    .refine(
      data => {
        const isOrganisation = data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_MON_ENTREPRISE);
        if (isOrganisation) {
          return !!(data.organisationAdresse && data.organisationAdresse.length >= VALIDATION_LIMITS.ADRESSE_MIN);
        }
        return true;
      },
      {
        message: t("validation.adresseMin", { min: VALIDATION_LIMITS.ADRESSE_MIN }),
        path: ["organisationAdresse"],
      },
    )
    .refine(
      data => {
        const isOrganisation = data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_MON_ENTREPRISE);
        if (isOrganisation) {
          const value = data.organisationAdressePostale?.trim() ?? "";
          return /^[a-zA-Z0-9\s]*$/.test(value);
        }
        return true;
      },
      {
        message: t("validation.numeroPostalFormat"),
        path: ["organisationAdressePostale"],
      },
    )
    .refine(
      data => {
        const isOrganisation = data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_MON_ENTREPRISE);
        if (isOrganisation) {
          return !!(data.organisationNpa && data.organisationNpa.length > 0 && /^\d+$/.test(data.organisationNpa));
        }
        return true;
      },
      {
        message: t("validation.npaMin"),
        path: ["organisationNpa"],
      },
    )
    .refine(
      data => {
        const isOrganisation = data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_MON_ENTREPRISE);
        if (isOrganisation) {
          return !!(data.organisationLocalite && data.organisationLocalite.length >= 2);
        }
        return true;
      },
      {
        message: t("validation.localiteRequise"),
        path: ["organisationLocalite"],
      },
    )
    .refine(
      data => {
        const isOrganisation = data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_MON_ENTREPRISE);
        if (isOrganisation) {
          return !!(
            data.organisationTelephone &&
            data.organisationTelephone.length > 0 &&
            validateInternationalPhone(data.organisationTelephone)
          );
        }
        return true;
      },
      {
        message: t("validation.telephoneFormat"),
        path: ["organisationTelephone"],
      },
    )
    .refine(
      data => {
        const isOrganisation = data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_MON_ENTREPRISE);
        if (!isOrganisation) {
          return true;
        }
        return !!data.organisationEmail && z.string().email().safeParse(data.organisationEmail).success;
      },
      {
        message: t("validation.emailInvalide"),
        path: ["organisationEmail"],
      },
    )
    .refine(
      data => {
        const isOrganisation = data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_MON_ENTREPRISE);
        if (!isOrganisation) {
          return true;
        }
        return (
          !!data.organisationConfirmationEmail &&
          z.string().email().safeParse(data.organisationConfirmationEmail).success
        );
      },
      {
        message: t("validation.emailInvalide"),
        path: ["organisationConfirmationEmail"],
      },
    )
    .refine(
      data => {
        const isOrganisation = data.lienAvecPersonne === t(INFORMATIONS_PERSONNELLES_MON_ENTREPRISE);
        if (isOrganisation) {
          return data.organisationEmail === data.organisationConfirmationEmail;
        }
        return true;
      },
      {
        message: t("validation.emailsDifferent"),
        path: ["organisationConfirmationEmail"],
      },
    );
};
