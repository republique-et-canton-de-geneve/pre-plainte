import type { ComposerTranslation } from "vue-i18n";
import { createVerificationEmailPageSchema } from "@/schemas/verification-email-page.schema";
import {
  donneesVerificationEmailValides,
  reglesVerificationEmail,
} from "@/test/business-rules/verification-email.rules";
import { describeBusinessRuleSchema } from "@/test/business-rules/business-rule-spec.helpers";

const t = ((key: string) => key) as ComposerTranslation;

describeBusinessRuleSchema(
  "regles metier de verification email",
  createVerificationEmailPageSchema(t),
  reglesVerificationEmail,
  donneesVerificationEmailValides,
);
