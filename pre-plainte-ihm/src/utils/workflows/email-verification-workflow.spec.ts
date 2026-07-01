import { describe, expect, it } from "vitest";
import {
  canContinueEmailVerification,
  resolveDevBypassConfirmation,
  shouldResetEmailChallenge,
} from "@/utils/workflows/email-verification-workflow";
import {
  donneesEmailVerificationValides,
  reglesEmailVerificationWorkflow,
} from "@/test/business-rules/email-verification-workflow.rules";

describe("regles metier du workflow de verification email", () => {
  reglesEmailVerificationWorkflow.forEach(regle => {
    regle.examples?.forEach(example => {
      it(`${regle.champDemande} - ${example.label}`, () => {
        const data = {
          ...donneesEmailVerificationValides,
          ...example.data,
        };

        if (regle.champDemande === "Bouton continuer") {
          expect(canContinueEmailVerification(data)).toBe(example.valid);
          return;
        }

        expect(shouldResetEmailChallenge(
          data.nextEmail ?? "",
          Boolean(data.codeSent),
          data.challengeEmailSnapshot ?? "",
        )).toBe(example.valid);
      });
    });
  });

  it("Code de securite - bypass developpement remplit un code par defaut si le code est absent", () => {
    expect(resolveDevBypassConfirmation("")).toBe("000000");
  });

  it("Code de securite - bypass developpement conserve un code valide", () => {
    expect(resolveDevBypassConfirmation("123456")).toBe("123456");
  });
});
