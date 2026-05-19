import { z } from "zod";
import type { ComposerTranslation } from "vue-i18n";

export const createVerificationEmailPageSchema = (t: ComposerTranslation) =>
  z.object({
    email: z.preprocess(
      val => (val == null ? "" : String(val).trim()),
      z.string().min(1, t("validation.emailInvalide")).email(t("validation.emailInvalide")),
    ),
    confirmationEmail: z.string().optional(),
  });
