import { z } from "zod";
import type { ComposerTranslation } from "vue-i18n";
import { TEXT_FIELD_MAX_LENGTH, VALIDATION_LIMITS } from "@/constants/constant.ts";
import { validateInternationalPhone } from "@/utils/validations/phoneValidation";

export const createVerificationEmailPageSchema = (t: ComposerTranslation, requireTelephone = false) =>
  z
    .object({
      email: z.preprocess(
        val => (typeof val === "string" ? val.trim() : ""),
        z
          .string()
          .min(1, t("validation.emailRequis"))
          .max(TEXT_FIELD_MAX_LENGTH, t("validation.emailTropLong", { max: TEXT_FIELD_MAX_LENGTH }))
          .email(t("validation.emailInvalide")),
      ),
      confirmationEmail: z.string().optional(),
      telephone: z.string().optional(),
      nom: z.string().optional(),
      prenom: z.string().optional(),
    })
    .superRefine((data, ctx) => {
      if (!requireTelephone) {
        return;
      }
      if (!data.telephone || !validateInternationalPhone(data.telephone)) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          path: ["telephone"],
          message: t("validation.telephoneFormat"),
        });
      }
      if (!data.nom || data.nom.trim().length < VALIDATION_LIMITS.NOM_MIN) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          path: ["nom"],
          message: t("validation.nomMin", { min: VALIDATION_LIMITS.NOM_MIN }),
        });
      }
      if (!data.prenom || data.prenom.trim().length < VALIDATION_LIMITS.PRENOM_MIN) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          path: ["prenom"],
          message: t("validation.prenomMin", { min: VALIDATION_LIMITS.PRENOM_MIN }),
        });
      }
    });
