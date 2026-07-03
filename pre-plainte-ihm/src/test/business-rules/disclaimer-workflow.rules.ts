import type { BusinessRule } from "./business-rule.types";

export interface DisclaimerWorkflowRuleData extends Record<string, unknown> {
  typeIncident?: string;
  typeDommage?: string;
  constatPresent?: boolean | null;
  typeCybercrime?: string;
  confirmeIdentite?: boolean;
  confirmeSituation?: boolean;
  confirmeEffraction?: boolean;
  disclaimerConfirmed?: boolean;
  captchaEnabled?: boolean;
  captchaToken?: string;
}

export const donneesDisclaimerValides: DisclaimerWorkflowRuleData = {
  typeIncident: "vol",
  confirmeIdentite: true,
  confirmeSituation: true,
  confirmeEffraction: true,
  disclaimerConfirmed: true,
  captchaEnabled: false,
  captchaToken: "",
};

export const reglesDisclaimerWorkflow: BusinessRule<DisclaimerWorkflowRuleData>[] = [
  {
    kind: "workflow",
    section: "Informations generales",
    champDemande: "Bouton continuer",
    obligatoire: "Oui",
    precision: "La continuation est autorisee uniquement si les confirmations ont ete validees et le type d'incident requis est renseigne.",
    examples: [
      {
        label: "incident vol avec confirmations validees est autorise",
        data: {},
        valid: true,
      },
      {
        label: "confirmations non validees bloquent la continuation",
        data: {
          disclaimerConfirmed: false,
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
        label: "dommage vehicule sans indication de constat bloque la continuation",
        data: {
          typeIncident: "degat-delit",
          typeDommage: "dommage-vehicule",
          constatPresent: null,
        },
        valid: false,
      },
      {
        label: "dommage vehicule avec constat autorise la continuation",
        data: {
          typeIncident: "degat-delit",
          typeDommage: "dommage-vehicule",
          constatPresent: true,
        },
        valid: true,
      },
      {
        label: "dommage autre sans indication de constat autorise la continuation",
        data: {
          typeIncident: "degat-delit",
          typeDommage: "autre",
          constatPresent: null,
        },
        valid: true,
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
    champDemande: "Bouton confirmer",
    obligatoire: "Oui",
    precision: "Le reste de la page est affiche uniquement si les trois confirmations sont cochees.",
    examples: [
      {
        label: "trois confirmations cochees autorisent l'affichage du formulaire",
        data: {},
        valid: true,
      },
      {
        label: "confirmation d'identite absente bloque l'affichage du formulaire",
        data: {
          confirmeIdentite: false,
        },
        valid: false,
      },
      {
        label: "confirmation de situation absente bloque l'affichage du formulaire",
        data: {
          confirmeSituation: false,
        },
        valid: false,
      },
      {
        label: "confirmation d'effraction absente bloque l'affichage du formulaire",
        data: {
          confirmeEffraction: false,
        },
        valid: false,
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
  {
    kind: "workflow",
    section: "Informations generales",
    champDemande: "Parcours rendez-vous seul",
    obligatoire: "Selon le cas",
    precision: "Un dommage vehicule ou batiment avec constat deja etabli oriente vers la prise de rendez-vous sans creation de pre-plainte.",
    examples: [
      {
        label: "dommage vehicule avec constat active le parcours rendez-vous seul",
        data: {
          typeIncident: "degat-delit",
          typeDommage: "dommage-vehicule",
          constatPresent: true,
        },
        valid: true,
      },
      {
        label: "dommage vehicule sans constat conserve le parcours pre-plainte",
        data: {
          typeIncident: "degat-delit",
          typeDommage: "dommage-vehicule",
          constatPresent: false,
        },
        valid: false,
      },
    ],
  },
];
