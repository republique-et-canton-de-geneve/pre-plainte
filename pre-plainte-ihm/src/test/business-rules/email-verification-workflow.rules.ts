import type { BusinessRule } from "./business-rule.types";

export interface EmailVerificationWorkflowRuleData extends Record<string, unknown> {
  hasSendError?: boolean;
  devBypassEmail?: boolean;
  emailValide?: boolean;
  codeSent?: boolean;
  confirmationEmail?: string;
  nextEmail?: string;
  challengeEmailSnapshot?: string;
}

export const donneesEmailVerificationValides: EmailVerificationWorkflowRuleData = {
  hasSendError: false,
  devBypassEmail: false,
  emailValide: true,
  codeSent: true,
  confirmationEmail: "123456",
};

export const reglesEmailVerificationWorkflow: BusinessRule<EmailVerificationWorkflowRuleData>[] = [
  {
    kind: "workflow",
    section: "Verification email",
    champDemande: "Bouton continuer",
    obligatoire: "Oui",
    precision: "La continuation est autorisee apres envoi du code et saisie d'un code de securite valide, sauf bypass de developpement.",
    examples: [
      {
        label: "code valide apres envoi autorise la continuation",
        data: {},
        valid: true,
      },
      {
        label: "code non envoye bloque la continuation",
        data: {
          codeSent: false,
        },
        valid: false,
      },
      {
        label: "code incomplet bloque la continuation",
        data: {
          confirmationEmail: "123",
        },
        valid: false,
      },
      {
        label: "erreur d'envoi bloque la continuation",
        data: {
          hasSendError: true,
        },
        valid: false,
      },
      {
        label: "bypass developpement avec email valide autorise la continuation",
        data: {
          devBypassEmail: true,
          codeSent: false,
          confirmationEmail: "",
        },
        valid: true,
      },
    ],
  },
  {
    kind: "workflow",
    section: "Verification email",
    champDemande: "Code de securite",
    obligatoire: "Selon le cas",
    precision: "Le code de securite est reinitialise si l'adresse email change apres l'envoi du code.",
    examples: [
      {
        label: "email modifie apres envoi reinitialise le challenge",
        data: {
          nextEmail: "nouveau@example.com",
          codeSent: true,
          challengeEmailSnapshot: "citoyen@example.com",
        },
        valid: true,
      },
      {
        label: "email identique apres envoi conserve le challenge",
        data: {
          nextEmail: "citoyen@example.com",
          codeSent: true,
          challengeEmailSnapshot: "citoyen@example.com",
        },
        valid: false,
      },
    ],
  },
];
