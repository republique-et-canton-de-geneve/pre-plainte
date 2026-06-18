import { z } from "zod";
import type { ComposerTranslation } from "vue-i18n";
import { TEXT_FIELD_MAX_LENGTH } from "@/constants/constant.ts";

export const createVerificationEmailPageSchema = (t: ComposerTranslation) =>
  z.object({
    email: z.preprocess(
      val => (typeof val === "string" ? val.trim() : ""),
      z
        .string()
        .min(1, t("validation.emailRequis"))
        .max(TEXT_FIELD_MAX_LENGTH, t("validation.emailTropLong", { max: TEXT_FIELD_MAX_LENGTH}))
        .email(t("validation.emailInvalide")),
    ),
    confirmationEmail: z.string().optional(),
  });
