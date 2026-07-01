import type { BusinessRule } from "./business-rule.types";

export interface RendezVousRuleData extends Record<string, unknown> {
  dateSouhaitee?: string;
}

export const premiereDateRendezVousDisponible = "2026-07-01";
export const derniereDateRendezVousDisponible = "2026-07-31";

export const donneesRendezVousValides: RendezVousRuleData = {
  dateSouhaitee: "15.07.2026",
};

export const reglesRendezVous: BusinessRule<RendezVousRuleData>[] = [
  {
    section: "Rendez-vous",
    champDemande: "Date souhaitee",
    obligatoire: "Non",
    precision: "La date souhaitee doit etre valide et comprise dans la periode proposee par le service.",
    examples: [
      {
        label: "date au format invalide est refusee",
        data: {
          dateSouhaitee: "2026/07/15",
        },
        valid: false,
        errorPath: ["dateSouhaitee"],
        errorMessage: "validation.formatDateInvalide",
      },
      {
        label: "date avant la premiere disponibilite est refusee",
        data: {
          dateSouhaitee: "30.06.2026",
        },
        valid: false,
        errorPath: ["dateSouhaitee"],
        errorMessage: "validation.dateAvantPremiereDispo",
      },
      {
        label: "date apres la derniere disponibilite est refusee",
        data: {
          dateSouhaitee: "01.08.2026",
        },
        valid: false,
        errorPath: ["dateSouhaitee"],
        errorMessage: "validation.dateApresDerniereDispo",
      },
      {
        label: "date dans la periode disponible est acceptee",
        data: {
          dateSouhaitee: "15.07.2026",
        },
        valid: true,
      },
    ],
  },
];
