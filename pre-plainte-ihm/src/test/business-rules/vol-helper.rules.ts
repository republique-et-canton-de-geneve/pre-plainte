import type { BusinessRule } from "./business-rule.types";

export interface VolHelperRuleData extends Record<string, unknown> {
  sousCategorie?: string;
  plaqueInconnu?: boolean;
  plaqueNumero?: string;
  plaquePays?: { code: string; label: string } | null;
  plaqueCanton?: { code: string; label: string } | null;
  isVehicle?: boolean;
  categorieObjet?: string;
  objetsVolesValides?: unknown[];
}

const suisse = { code: "8100", label: "Suisse" };
const geneve = { code: "GE", label: "Geneve" };

export const donneesPlaqueValides: VolHelperRuleData = {
  sousCategorie: "voitures",
  plaqueInconnu: false,
  plaqueNumero: "GE 123456",
  plaquePays: suisse,
  plaqueCanton: geneve,
};

export const reglesVolHelpers: BusinessRule<VolHelperRuleData>[] = [
  {
    kind: "helper",
    section: "Vol",
    champDemande: "Plaque de vehicule",
    obligatoire: "Selon le cas",
    precision: "Une plaque de vehicule suisse requiert un pays, un canton et un numero au format attendu.",
    examples: [
      {
        label: "plaque suisse complete est acceptee",
        data: {},
        valid: true,
      },
      {
        label: "plaque suisse sans canton est refusee",
        data: {
          plaqueCanton: null,
        },
        valid: false,
        errorPath: ["plaqueCanton"],
        errorMessage: "validation.plaqueCantonRequis",
      },
      {
        label: "plaque suisse au mauvais format est refusee",
        data: {
          plaqueNumero: "123",
        },
        valid: false,
        errorPath: ["plaqueNumero"],
        errorMessage: "validation.numeroPlaqueSuisseInvalide",
      },
      {
        label: "plaque inconnue est acceptee pour une categorie sans plaque obligatoire",
        data: {
          sousCategorie: "camions",
          plaqueInconnu: true,
          plaqueNumero: "",
          plaquePays: null,
          plaqueCanton: null,
        },
        valid: true,
      },
    ],
  },
  {
    kind: "helper",
    section: "Rendez-vous",
    champDemande: "Vol de vehicule avec plaque",
    obligatoire: "Selon le cas",
    precision: "Un vol est considere comme vol de vehicule avec plaque si au moins un objet vole effectif est un vehicule avec plaque connue.",
    examples: [
      {
        label: "vehicule avec plaque connue est detecte",
        data: {
          isVehicle: true,
          categorieObjet: "vehicule",
          sousCategorie: "voitures",
          plaqueInconnu: false,
          plaqueNumero: "GE 123456",
        },
        valid: true,
      },
      {
        label: "vehicule avec plaque inconnue n'est pas detecte",
        data: {
          isVehicle: true,
          categorieObjet: "vehicule",
          sousCategorie: "voitures",
          plaqueInconnu: true,
          plaqueNumero: "",
        },
        valid: false,
      },
    ],
  },
];
