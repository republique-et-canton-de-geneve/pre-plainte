import type { BusinessRule } from "./business-rule.types";

export interface VerificationEmailRuleData extends Record<string, unknown> {
  email?: string;
  confirmationEmail?: string;
}

export const donneesVerificationEmailValides: VerificationEmailRuleData = {
  email: "citoyen@example.com",
  confirmationEmail: "citoyen@example.com",
};

export const reglesVerificationEmail: BusinessRule<VerificationEmailRuleData>[] = [
  {
    section: "Verification email",
    champDemande: "Adresse email",
    obligatoire: "Oui",
    precision: "L'adresse email est obligatoire, nettoyee des espaces et doit respecter le format email.",
    examples: [
      {
        label: "email vide est refuse",
        data: {
          email: "",
        },
        valid: false,
        errorPath: ["email"],
        errorMessage: "validation.emailRequis",
      },
      {
        label: "email au format invalide est refuse",
        data: {
          email: "email-invalide",
        },
        valid: false,
        errorPath: ["email"],
        errorMessage: "validation.emailInvalide",
      },
      {
        label: "email valide avec espaces est accepte",
        data: {
          email: " citoyen@example.com ",
        },
        valid: true,
      },
    ],
  },
];
