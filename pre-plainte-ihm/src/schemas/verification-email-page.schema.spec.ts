import { describe, expect, it } from "vitest";
import type { ComposerTranslation } from "vue-i18n";
import { createVerificationEmailPageSchema } from "@/schemas/verification-email-page.schema";
import {
  donneesVerificationEmailValides,
  reglesVerificationEmail,
} from "@/test/business-rules/verification-email.rules";

const t = ((key: string) => key) as ComposerTranslation;

describe("regles metier de verification email", () => {
  const schema = createVerificationEmailPageSchema(t);

  reglesVerificationEmail.forEach(regle => {
    const examples = regle.examples ?? [
      {
        label: regle.precision,
        data: regle.invalidData,
        valid: false,
        errorPath: regle.errorPath,
        errorMessage: regle.errorMessage,
      },
    ];

    examples.forEach(example => {
      if (!example.data) {
        return;
      }

      it(`${regle.champDemande} - ${example.label}`, () => {
        const result = schema.safeParse({
          ...donneesVerificationEmailValides,
          ...example.data,
        });

        expect(result.success).toBe(example.valid);

        if (!example.valid && !result.success) {
          expect(result.error.issues).toEqual(
            expect.arrayContaining([
              expect.objectContaining({
                path: example.errorPath,
                message: example.errorMessage,
              }),
            ]),
          );
        }
      });
    });
  });
});
