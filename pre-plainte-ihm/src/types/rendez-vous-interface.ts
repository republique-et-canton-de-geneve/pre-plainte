export interface PostePolice {
  lieu: string;
  coordonees: string;
}

export interface CreneauRendezVous {
  id: string;
  date: string;
  heureDebut: string;
  heureFin: string;
  lieu: string;
  codeRdv?: string;
}

export type IncidentType = "cyber" | "dommage" | "vol";

export type AlertType = "success" | "error";

export interface AvailabilityResource {
  id: number;
  key: string;
  name: string;
  type: string;
  station?: Record<string, unknown>;
}

export interface Availability {
  beginDateTime: string;
  endDateTime: string;
  serviceId: string;
  siteCode: string;
  resource?: AvailabilityResource;
}

export interface SelectedCreneau {
  id: string;
  date: string;
  dateAffichee: string;
  heureDebut: string;
  heureFin: string;
  lieu: string;
  serviceId: string;
  siteCode: string;
  resource?: AvailabilityResource;
  beginDateTime: string;
  endDateTime: string;
}
