import { describe, expect, it } from "vitest";
import { createInfosPersonnellesSchema } from "@/schemas/infos-personnelles.schema";
import {
  donneesInformationsPersonnellesValides,
  reglesInformationsPersonnelles,
} from "@/test/business-rules/informations-personnelles.rules";
import type { ComposerTranslation } from "vue-i18n";

const t = ((key: string) => key) as ComposerTranslation;

describe("regles metier des informations personnelles", () => {
  const schema = createInfosPersonnellesSchema(t);

  reglesInformationsPersonnelles.forEach(regle => {
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
          ...donneesInformationsPersonnellesValides,
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
