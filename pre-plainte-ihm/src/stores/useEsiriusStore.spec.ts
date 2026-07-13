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

const BEGIN_DATE_TIME = "2026-07-13T10:00:00";
const AVAILABILITY_PERIOD_DAYS = 15;
const TIMEOUT_ESIRIUS_ERROR = "timeout eSirius";
const EXPECTED_RETRY_ATTEMPTS = 2;

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
        throw new Error(TIMEOUT_ESIRIUS_ERROR);
      }
      return [creneau];
    });

    await store.loadAllAvailabilitiesForPPEL(BEGIN_DATE_TIME, AVAILABILITY_PERIOD_DAYS);

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

    await store.loadAllAvailabilitiesForPPEL(BEGIN_DATE_TIME, AVAILABILITY_PERIOD_DAYS);

    expect(store.allAvailabilities.map(item => item.serviceId).sort()).toEqual([
      serviceCornavin.key,
      servicePaquis.key,
    ]);
    expect(store.loading).toBe(false);
  });

  it("retente un appel en erreur puis conserve le poste si le retry reussit", async () => {
    const store = useEsiriusStore();
    store.services = [servicePaquis];
    let attempts = 0;

    vi.mocked(EsiriusService.getAvailability).mockImplementation(async () => {
      attempts += 1;
      if (attempts === 1) {
        throw new Error(TIMEOUT_ESIRIUS_ERROR);
      }
      return [creneau];
    });

    await store.loadAllAvailabilitiesForPPEL(BEGIN_DATE_TIME, AVAILABILITY_PERIOD_DAYS);

    expect(attempts).toBe(EXPECTED_RETRY_ATTEMPTS);
    expect(store.allAvailabilities.map(item => item.serviceId)).toEqual([servicePaquis.key]);
    expect(store.loading).toBe(false);
  });

  it("ignore les payloads d'erreur eSirius renvoyes en HTTP 200", async () => {
    const store = useEsiriusStore();
    store.services = [serviceCornavin, servicePaquis];

    vi.mocked(EsiriusService.getAvailability).mockImplementation(async (_site, serviceId) => {
      if (serviceId === servicePaquis.key) {
        return [{ code: 500, details: "eSirius indisponible" }];
      }
      return [creneau];
    });

    await store.loadAllAvailabilitiesForPPEL(BEGIN_DATE_TIME, AVAILABILITY_PERIOD_DAYS);

    expect(store.allAvailabilities.map(item => item.serviceId)).toEqual([serviceCornavin.key]);
    expect(store.loading).toBe(false);
  });
});
