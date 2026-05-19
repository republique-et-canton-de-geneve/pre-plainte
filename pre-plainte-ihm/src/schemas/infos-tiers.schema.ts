import { z } from "zod";
import type { ComposerTranslation } from "vue-i18n";
import { VALIDATION_LIMITS } from "@/constants/constant";
import { validateInternationalPhone } from "@/utils/validations/phoneValidation";

const ripolSelectionSchema = (errorMessage: string) =>
  z
    .object({
      code: z.string(),
      label: z.string(),
    })
    .nullable()
    .refine(val => val !== null && val.code.length > 0, { message: errorMessage });

export const createInfosTiersSchema = (t: ComposerTranslation) =>
  z
    .object({
      tiersNom: z.string().min(VALIDATION_LIMITS.NOM_MIN, t("validation.nomMin", { min: VALIDATION_LIMITS.NOM_MIN })),
      tiersPrenom: z
        .string()
        .min(VALIDATION_LIMITS.PRENOM_MIN, t("validation.prenomMin", { min: VALIDATION_LIMITS.PRENOM_MIN })),
      tiersAdresse: z
        .string()
        .min(VALIDATION_LIMITS.ADRESSE_MIN, t("validation.adresseMin", { min: VALIDATION_LIMITS.ADRESSE_MIN })),
      tiersGenre: ripolSelectionSchema(t("validation.genreRequis")),
      tiersNationalite: ripolSelectionSchema(t("validation.nationaliteRequise")),
      tiersAdressePostale: z
        .string()
        .trim()
        .regex(/^[a-zA-Z0-9\s]*$/, t("validation.numeroPostalFormat")),
      tiersNpa: z
        .string()
        .min(1, t("validation.npaMin"))
        .refine(val => /^\d+$/.test(val), t("validation.npaFormat")),
      tiersLocalite: z.string().min(2, t("validation.localiteRequise")),
      tiersDateNaissance: z
        .string()
        .min(1, t("validation.dateNaissanceRequise"))
        .refine(
          date => {
            if (!date) {
              return false;
            }
            const birthDate = new Date(date);
            const today = new Date();
            const age = today.getFullYear() - birthDate.getFullYear();
            const monthDiff = today.getMonth() - birthDate.getMonth();
            const dayDiff = today.getDate() - birthDate.getDate();
            const actualAge = monthDiff < 0 || (monthDiff === 0 && dayDiff < 0) ? age - 1 : age;

            return actualAge >= 10 && actualAge <= 120;
          },
          {
            message: t("validation.ageInvalideTiers"),
          },
        ),
      tiersTelephone: z
        .string()
        .min(1, t("validation.telephoneFormat"))
        .refine(validateInternationalPhone, t("validation.telephoneFormat")),
      tiersEmail: z.string().email(t("validation.emailInvalide")),
      tiersConfirmationEmail: z.string().email(t("validation.emailInvalide")),
    })
    .refine(data => data.tiersEmail === data.tiersConfirmationEmail, {
      message: t("validation.emailsDifferent"),
      path: ["tiersConfirmationEmail"],
    });
