import { describe, expect, it } from "vitest";
import type { ComposerTranslation } from "vue-i18n";
import { createIncidentSchema } from "@/schemas/incident-evenement.schema";
import { donneesEvenementValides, reglesEvenement } from "@/test/business-rules/evenement.rules";

const t = ((key: string) => key) as ComposerTranslation;

describe("regles metier des informations sur l'evenement", () => {
  const schema = createIncidentSchema(t, "CH");

  reglesEvenement.forEach(regle => {
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
          ...donneesEvenementValides,
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
