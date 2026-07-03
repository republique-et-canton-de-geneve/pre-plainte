import { describe, expect, it } from "vitest";
import {
  canContinueDisclaimer,
  hasConfirmedDisclaimer,
  isRendezVousOnlyDommage,
  shouldResetTypeCybercrime,
  shouldResetTypeDommage,
} from "@/utils/workflows/disclaimer-workflow";
import {
  donneesDisclaimerValides,
  reglesDisclaimerWorkflow,
} from "@/test/business-rules/disclaimer-workflow.rules";

describe("regles metier du workflow informations generales", () => {
  reglesDisclaimerWorkflow.forEach(regle => {
    regle.examples?.forEach(example => {
      it(`${regle.champDemande} - ${example.label}`, () => {
        const data = {
          ...donneesDisclaimerValides,
          ...example.data,
        };

        if (regle.champDemande === "Bouton continuer") {
          expect(canContinueDisclaimer(data)).toBe(example.valid);
          return;
        }

        if (regle.champDemande === "Bouton confirmer") {
          expect(hasConfirmedDisclaimer(data)).toBe(example.valid);
          return;
        }

        if (regle.champDemande === "Parcours rendez-vous seul") {
          expect(isRendezVousOnlyDommage(data)).toBe(example.valid);
          return;
        }

        if (example.errorPath?.[0] === "typeDommage") {
          expect(shouldResetTypeDommage(data.typeIncident)).toBe(example.valid);
        }

        if (example.errorPath?.[0] === "typeCybercrime") {
          expect(shouldResetTypeCybercrime(data.typeIncident)).toBe(example.valid);
        }
      });
    });
  });
});
