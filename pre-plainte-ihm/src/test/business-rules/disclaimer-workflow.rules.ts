import type { BusinessRule } from "./business-rule.types";

export interface DisclaimerWorkflowRuleData extends Record<string, unknown> {
  typeIncident?: string;
  typeDommage?: string;
  typeCybercrime?: string;
  confirmeIdentite?: boolean;
  confirmeSituation?: boolean;
  captchaEnabled?: boolean;
  captchaToken?: string;
}

export const donneesDisclaimerValides: DisclaimerWorkflowRuleData = {
  typeIncident: "vol",
  confirmeIdentite: true,
  confirmeSituation: true,
  captchaEnabled: false,
  captchaToken: "",
};

export const reglesDisclaimerWorkflow: BusinessRule<DisclaimerWorkflowRuleData>[] = [
  {
    kind: "workflow",
    section: "Informations generales",
    champDemande: "Bouton continuer",
    obligatoire: "Oui",
    precision: "La continuation est autorisee uniquement si les confirmations et le type d'incident requis sont renseignes.",
    examples: [
      {
        label: "incident vol avec confirmations est autorise",
        data: {},
        valid: true,
      },
      {
        label: "confirmation d'identite absente bloque la continuation",
        data: {
          confirmeIdentite: false,
        },
        valid: false,
      },
      {
        label: "dommage sans type de dommage bloque la continuation",
        data: {
          typeIncident: "degat-delit",
          typeDommage: "",
        },
        valid: false,
      },
      {
        label: "cybercrime autre bloque la continuation",
        data: {
          typeIncident: "cybercrime",
          typeCybercrime: "autre",
        },
        valid: false,
      },
      {
        label: "captcha active sans jeton bloque la continuation",
        data: {
          captchaEnabled: true,
          captchaToken: "",
        },
        valid: false,
      },
      {
        label: "captcha active avec jeton autorise la continuation",
        data: {
          captchaEnabled: true,
          captchaToken: "token",
        },
        valid: true,
      },
    ],
  },
  {
    kind: "workflow",
    section: "Informations generales",
    champDemande: "Changement du type d'incident",
    obligatoire: "Selon le cas",
    precision: "Les champs specifiques dommage ou cybercriminalite sont reinitialises lorsque le type d'incident ne les concerne plus.",
    examples: [
      {
        label: "passage vers vol reinitialise le type de dommage",
        data: {
          typeIncident: "vol",
          typeDommage: "dommage-vehicule",
        },
        valid: true,
        errorPath: ["typeDommage"],
      },
      {
        label: "passage vers vol reinitialise le type de cybercriminalite",
        data: {
          typeIncident: "vol",
          typeCybercrime: "achat-non-recu",
        },
        valid: true,
        errorPath: ["typeCybercrime"],
      },
      {
        label: "incident dommage conserve le type de dommage",
        data: {
          typeIncident: "degat-delit",
          typeDommage: "dommage-vehicule",
        },
        valid: false,
        errorPath: ["typeDommage"],
      },
    ],
  },
];
