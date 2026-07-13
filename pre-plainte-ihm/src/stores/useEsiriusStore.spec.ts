import { beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import { useEsiriusStore } from "@/stores/useEsiriusStore";
import { EsiriusService } from "@/services/esiriusService";

vi.mock("@/services/esiriusService", () => ({
  EsiriusService: {
    getServiceListBySiteCode: vi.fn(),
    getAvailability: vi.fn(),
  },
}));

const serviceCornavin = {
  key: "VOL-CORNAVIN",
  name: "Pré-plainte pour vol - Cornavin",
  existAvailabilities: true,
};

const servicePaquis = {
  key: "VOL-PAQUIS",
  name: "Pré-plainte pour vol - Pâquis",
  existAvailabilities: true,
};

const servicePlainpalais = {
  key: "VOL-PLAINPALAIS",
  name: "Pré-plainte pour vol - Plainpalais",
  existAvailabilities: true,
};

const creneau = {
  beginDateTime: "20260720 10:00",
  endDateTime: "20260720 10:30",
  resource: { key: "RDV", name: "RDV - Poste" },
};

describe("useEsiriusStore.loadAllAvailabilitiesForPPEL", () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    vi.clearAllMocks();
  });

  it("conserve les postes valides quand un appel disponibilites echoue", async () => {
    const store = useEsiriusStore();
    store.services = [serviceCornavin, servicePaquis, servicePlainpalais];

    vi.mocked(EsiriusService.getAvailability).mockImplementation(async (_site, serviceId) => {
      if (serviceId === servicePaquis.key) {
        throw new Error("timeout eSirius");
      }
      return [creneau];
    });

    await store.loadAllAvailabilitiesForPPEL("2026-07-13T10:00:00", 15);

    expect(store.allAvailabilities.map(item => item.serviceId).sort()).toEqual([
      serviceCornavin.key,
      servicePlainpalais.key,
    ]);
    expect(store.loading).toBe(false);
    expect(store.errorMessage).toBe("");
  });

  it("conserve les postes valides quand un poste n'a aucune disponibilite", async () => {
    const store = useEsiriusStore();
    store.services = [serviceCornavin, servicePaquis, servicePlainpalais];

    vi.mocked(EsiriusService.getAvailability).mockImplementation(async (_site, serviceId) => {
      if (serviceId === servicePlainpalais.key) {
        return [];
      }
      return [creneau];
    });

    await store.loadAllAvailabilitiesForPPEL("2026-07-13T10:00:00", 15);

    expect(store.allAvailabilities.map(item => item.serviceId).sort()).toEqual([
      serviceCornavin.key,
      servicePaquis.key,
    ]);
    expect(store.loading).toBe(false);
  });
});
