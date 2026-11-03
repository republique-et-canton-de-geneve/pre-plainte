import type { Ripol, RipolSelection } from "@/types/ripol.interface";

type VehicleType = Pick<Ripol, "labelFr"> | RipolSelection;

export function vehicleTypeDisplayKey(label: string): string | null {
  const normalized = label.normalize("NFD").replace(/[\u0300-\u036f]/g, "").toLowerCase().trim();
  if (normalized === "cyclomoteur avec place assise") {
    return "sousCategories.veloElectriqueSansPlaque";
  }
  if (normalized === "cyclomoteur sans place assise") {
    return "sousCategories.trottinette";
  }
  return null;
}

export function isUnplatedElectricVehicleType(item: VehicleType): boolean {
  const label = "labelFr" in item ? item.labelFr : item.label;
  return !!label && vehicleTypeDisplayKey(label) !== null;
}

export function publicVehicleTypeLabel(item: RipolSelection, translate: (key: string) => string): string {
  const key = vehicleTypeDisplayKey(item.label);
  return key ? translate(key) : item.label;
}
