export interface Ripol {
  code: string;
  labelFr: string;
  labelDe: string;
  groupeType: string;
}

export interface RipolSelection {
  code: string;
  label: string;
}

export type RipolEndpoint =
  | "sexes"
  | "nationalities"
  | "communes"
  | "document-types"
  | "location-types"
  | "object-types"
  | "phone-brands"
  | "phone-models"
  | "vehicle-types"
  | "vehicle-brands"
  | "vehicle-models";

