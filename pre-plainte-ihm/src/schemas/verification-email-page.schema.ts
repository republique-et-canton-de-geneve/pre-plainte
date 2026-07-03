import { z } from "zod";
import type { ComposerTranslation } from "vue-i18n";
import { TEXT_FIELD_MAX_LENGTH } from "@/constants/constant.ts";
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
    });
