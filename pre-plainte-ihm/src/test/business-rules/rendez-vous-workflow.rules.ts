import type { BusinessRule } from "./business-rule.types";

export interface RendezVousWorkflowRuleData extends Record<string, unknown> {
  typeIncident?: string;
  isVehicle?: boolean;
  categorieObjet?: string;
  sousCategorie?: string;
  plaqueInconnu?: boolean;
  plaqueNumero?: string;
  dateSouhaitee?: string;
  posteKey?: string;
}

export const nowRendezVousWorkflow = new Date("2026-07-01T08:00:00");

export const servicesRendezVousWorkflow = [
  { key: "vol", name: "Pre-plainte pour vol" },
  { key: "dommage", name: "Pre-plainte pour dommage" },
  { key: "cyber", name: "Pre-plainte pour cybercrime" },
];

export const availabilitiesRendezVousWorkflow = [
  {
    serviceId: "vol",
    serviceName: "Pre-plainte pour vol",
    availabilities: [
      { beginDateTime: "20260701T10:00", endDateTime: "20260701T10:30", resource: { key: "r1", name: "Poste 1" } },
      { beginDateTime: "20260702T10:00", endDateTime: "20260702T10:30", resource: { key: "r2", name: "Poste 2" } },
    ],
  },
  {
    serviceId: "dommage",
    serviceName: "Pre-plainte pour dommage",
    availabilities: [
      { beginDateTime: "20260701T11:00", endDateTime: "20260701T11:30", resource: { key: "r3", name: "Poste 3" } },
    ],
  },
  {
    serviceId: "cyber",
    serviceName: "Pre-plainte pour cybercrime",
    availabilities: [
      { beginDateTime: "20260716T10:00", endDateTime: "20260716T10:30", resource: { key: "r4", name: "Poste 4" } },
    ],
  },
];

export const donneesRendezVousWorkflowValides: RendezVousWorkflowRuleData = {
  typeIncident: "vol",
  isVehicle: false,
  categorieObjet: "plaque",
  sousCategorie: "",
  plaqueInconnu: true,
  plaqueNumero: "",
};

export const reglesRendezVousWorkflow: BusinessRule<RendezVousWorkflowRuleData>[] = [
  {
    kind: "workflow",
    section: "Rendez-vous",
    champDemande: "Service propose",
    obligatoire: "Oui",
    precision: "Les services proposes dependent du type d'incident et doivent avoir au moins un creneau disponible.",
    examples: [
      {
        label: "incident vol conserve uniquement le service vol",
        data: {
          typeIncident: "vol",
        },
        valid: true,
        errorPath: ["vol"],
      },
      {
        label: "incident dommage conserve uniquement le service dommage",
        data: {
          typeIncident: "degat-delit",
        },
        valid: true,
        errorPath: ["dommage"],
      },
    ],
  },
  {
    kind: "workflow",
    section: "Rendez-vous",
    champDemande: "Creneaux compatibles",
    obligatoire: "Oui",
    precision: "Les creneaux passes, hors fenetre de rendez-vous ou incompatibles avec l'incident sont exclus.",
    examples: [
      {
        label: "vol sans plaque conserve les deux creneaux vol de la fenetre",
        data: {
          typeIncident: "vol",
        },
        valid: true,
        errorPath: ["2"],
      },
      {
        label: "vol de vehicule avec plaque conserve uniquement les creneaux sous 24 heures",
        data: {
          typeIncident: "vol",
          isVehicle: true,
          categorieObjet: "vehicule",
          sousCategorie: "voitures",
          plaqueInconnu: false,
          plaqueNumero: "GE 123456",
        },
        valid: true,
        errorPath: ["1"],
      },
    ],
  },
  {
    kind: "workflow",
    section: "Rendez-vous",
    champDemande: "Alerte rendez-vous",
    obligatoire: "Selon le cas",
    precision: "Un vol de vehicule avec plaque affiche une alerte specifique selon la disponibilite des creneaux.",
    examples: [
      {
        label: "vol simple affiche l'information generale",
        data: {
          typeIncident: "vol",
        },
        valid: true,
        errorMessage: "rendezVous.warningAutreVol",
      },
      {
        label: "vol de vehicule avec plaque affiche l'alerte d'urgence",
        data: {
          typeIncident: "vol",
          isVehicle: true,
          categorieObjet: "vehicule",
          sousCategorie: "voitures",
          plaqueInconnu: false,
          plaqueNumero: "GE 123456",
        },
        valid: true,
        errorMessage: "rendezVous.warningVolVehiculePlaque",
      },
    ],
  },
];
