import { z } from "zod";
import type { ComposerTranslation } from "vue-i18n";
import { EMAIL_FIELD_MAX_LENGTH } from "@/constants/constant.ts";

export const createVerificationEmailPageSchema = (t: ComposerTranslation) =>
  z.object({
    email: z.preprocess(
      val => (val == null ? "" : String(val).trim()),
      z
        .string()
        .min(1, t("validation.emailRequis"))
        .max(EMAIL_FIELD_MAX_LENGTH, t("validation.emailTropLong"))
        .email(t("validation.emailInvalide")),
    ),
    confirmationEmail: z.string().optional(),
  });
