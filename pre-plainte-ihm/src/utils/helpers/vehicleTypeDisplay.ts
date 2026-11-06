import type { Ripol, RipolSelection } from "@/types/ripol.interface";

type VehicleType = Pick<Ripol, "code" | "labelFr"> | RipolSelection;

const SEATED_CYCLOMOTEUR: Ripol = { code: "062", labelFr: "Cyclomoteur avec place assise", labelDe: "", groupeType: "101" };
const STANDING_CYCLOMOTEUR: Ripol = { code: "064", labelFr: "Cyclomoteur sans place assise", labelDe: "", groupeType: "101" };

const UNPLATED_VEHICLE_TYPES: Ripol[] = [SEATED_CYCLOMOTEUR, STANDING_CYCLOMOTEUR];

function normalizeVehicleTypeLabel(label: string): string {
  return label
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .trim();
}

export function vehicleTypeDisplayKey(label: string, code?: string): string | null {
  const normalized = normalizeVehicleTypeLabel(label);
  if (code === SEATED_CYCLOMOTEUR.code || normalized === normalizeVehicleTypeLabel(SEATED_CYCLOMOTEUR.labelFr)) {
    return "sousCategories.veloElectriqueSansPlaque";
  }
  if (code === STANDING_CYCLOMOTEUR.code || normalized === normalizeVehicleTypeLabel(STANDING_CYCLOMOTEUR.labelFr)) {
    return "sousCategories.trottinette";
  }
  return null;
}

export function isUnplatedElectricVehicleType(item: VehicleType): boolean {
  const label = "labelFr" in item ? item.labelFr : item.label;
  return vehicleTypeDisplayKey(label ?? "", item.code) !== null;
}

export function withUnplatedVehicleTypes(items: Ripol[]): Ripol[] {
  const missing = UNPLATED_VEHICLE_TYPES.filter(type => !items.some(item => item.code === type.code));
  const allTypes = [...items, ...missing];
  return [
    ...allTypes.filter(isUnplatedElectricVehicleType),
    ...allTypes.filter(type => !isUnplatedElectricVehicleType(type)),
  ];
}

export function publicVehicleTypeLabel(item: RipolSelection, translate: (key: string) => string): string {
  const key = vehicleTypeDisplayKey(item.label, item.code);
  return key ? translate(key) : item.label;
}
