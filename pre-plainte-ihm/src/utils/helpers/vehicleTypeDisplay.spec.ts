import { describe, expect, it } from "vitest";
import { isUnplatedElectricVehicleType, publicVehicleTypeLabel } from "./vehicleTypeDisplay";

describe("libellés publics des véhicules sans plaque", () => {
  const translate = (key: string) => ({
    "sousCategories.veloElectriqueSansPlaque": "Vélo électrique sans plaque",
    "sousCategories.trottinette": "Trottinette",
  })[key] ?? key;

  it("affiche les noms usuels et conserve les références RIPOL pour myABI", () => {
    const velo = { code: "062", label: "Cyclomoteur avec place assise" };
    const trottinette = { code: "064", label: "Cyclomoteur sans place assise" };

    expect(publicVehicleTypeLabel(velo, translate)).toBe("Vélo électrique sans plaque");
    expect(publicVehicleTypeLabel(trottinette, translate)).toBe("Trottinette");
    expect(velo).toEqual({ code: "062", label: "Cyclomoteur avec place assise" });
    expect(trottinette).toEqual({ code: "064", label: "Cyclomoteur sans place assise" });
  });

  it("rend ces types accessibles parmi les vélos sans changer les autres types", () => {
    expect(isUnplatedElectricVehicleType({ code: "062", labelFr: "Cyclomoteur avec place assise" })).toBe(true);
    expect(isUnplatedElectricVehicleType({ code: "064", labelFr: "Cyclomoteur sans place assise" })).toBe(true);
    expect(isUnplatedElectricVehicleType({ code: "060", labelFr: "Moto" })).toBe(false);
    expect(publicVehicleTypeLabel({ code: "060", label: "Moto" }, translate)).toBe("Moto");
  });
});
