import { describe, expect, it } from "vitest";
import type { ComposerTranslation } from "vue-i18n";
import { rendezvousInfoSchema } from "@/schemas/rdv-schema";
import {
  derniereDateRendezVousDisponible,
  donneesRendezVousValides,
  premiereDateRendezVousDisponible,
  reglesRendezVous,
} from "@/test/business-rules/rendez-vous.rules";

const t = ((key: string) => key) as ComposerTranslation;

describe("regles metier du rendez-vous", () => {
  const schema = rendezvousInfoSchema(t, premiereDateRendezVousDisponible, derniereDateRendezVousDisponible);

  reglesRendezVous.forEach(regle => {
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
          ...donneesRendezVousValides,
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
