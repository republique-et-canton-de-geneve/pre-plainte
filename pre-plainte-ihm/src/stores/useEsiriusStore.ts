import { defineStore } from "pinia";
import { EsiriusService } from "@/services/esiriusService";

const PPEL_CODE = "PPEL";
const BAD_REQUEST_ERROR = 400;
const MILLISECONDS_IN_MINUTE = 60000;
const ISO_SLICE_LENGTH = 19;
const AVAILABILITY_FETCH_CONCURRENCY = 3;
const AVAILABILITY_FETCH_RETRIES = 2;

export const useEsiriusStore = defineStore("esirius", {
  state: () => ({
    sites: [] as any[],
    services: [] as any[],
    availabilities: [] as any[],
    allAvailabilities: [] as any[],
    filteredServicesList: [] as any[],
    currentAppointment: null as any,
    loading: false,
    error: null as string | null,
    errorMessage: "",
    availabilitiesLoadId: 0,
  }),

  getters: {
    hasError: state => !!state.errorMessage || !!state.error,
    isLoading: state => state.loading,

    filteredServices: state => (typeIncident: string) => {
      if (!typeIncident) {
        return state.services;
      }
      const normalized = typeIncident.toLowerCase().trim();
      return state.services.filter(s => s.name.toLowerCase().includes(`pré-plainte pour ${normalized}`));
    },
  },

  actions: {
    async loadServicesForSite(siteCode = PPEL_CODE) {
      this.startLoading();
      try {
        this.services = await EsiriusService.getServiceListBySiteCode(siteCode);
      } catch (err: any) {
        this.handleError(err);
      } finally {
        this.stopLoading();
      }
    },

    async loadAllAvailabilitiesForPPEL(begin?: string, period = 15) {
      const loadId = ++this.availabilitiesLoadId;
      this.startLoading();

      if (!begin) {
        const now = new Date();
        now.setHours(now.getHours() + 1);
        begin = new Date(now.getTime() - now.getTimezoneOffset() * MILLISECONDS_IN_MINUTE)
          .toISOString()
          .slice(0, ISO_SLICE_LENGTH);
      }

      try {
        const services =
          this.services.length > 0
            ? this.services
            : await EsiriusService.getServiceListBySiteCode(PPEL_CODE);

        if (loadId !== this.availabilitiesLoadId) {
          return;
        }

        if (this.services.length === 0) {
          this.services = services;
        }

        const availableServices = services.filter((s: any) => s.existAvailabilities);
        const results = await runWithConcurrency(
          availableServices,
          AVAILABILITY_FETCH_CONCURRENCY,
          service => fetchServiceAvailabilities(service, begin!, String(period)),
        );

        if (loadId !== this.availabilitiesLoadId) {
          return;
        }

        this.allAvailabilities = results.filter(
          (result): result is ServiceAvailabilities => result !== null,
        );
      } catch (err: any) {
        if (loadId === this.availabilitiesLoadId) {
          this.handleError(err);
        }
      } finally {
        if (loadId === this.availabilitiesLoadId) {
          this.stopLoading();
        }
      }
    },

    async createAppointment(payload: Record<string, any>) {
      this.startLoading();
      try {
        const result = await EsiriusService.createAppointment(payload);
        this.currentAppointment = result;
        return result;
      } catch (err: any) {
        this.handleError(err);
        throw err;
      } finally {
        this.stopLoading();
      }
    },

    async getAppointmentByCode(codeRdv: string) {
      this.startLoading();
      try {
        const result = await EsiriusService.getAppointmentByCode(codeRdv);
        if (result?.code && result.code >= BAD_REQUEST_ERROR) {
          throw new Error(result?.message ?? result?.details);
        }
        this.currentAppointment = result;
        return result;
      } catch (err: any) {
        this.handleError(err);
        throw err;
      } finally {
        this.stopLoading();
      }
    },

    async cancelAppointment(codeRdv: string) {
      this.startLoading();
      try {
        const result = await EsiriusService.cancelAppointment(codeRdv);
        if (result?.code && result.code >= BAD_REQUEST_ERROR) {
          throw new Error(result?.message ?? result?.details);
        }
        return result;
      } catch (err: any) {
        this.handleError(err);
        throw err;
      } finally {
        this.stopLoading();
      }
    },

    async updateAppointment(payload: Record<string, any>) {
      this.startLoading();
      try {
        const result = await EsiriusService.updateAppointment(payload);
        if (result?.code && result.code >= BAD_REQUEST_ERROR) {
          throw new Error(result?.message ?? result?.details);
        }
        this.currentAppointment = result;
        return result;
      } catch (err: any) {
        this.handleError(err);
        throw err;
      } finally {
        this.stopLoading();
      }
    },

    startLoading() {
      this.loading = true;
      this.error = null;
      this.errorMessage = "";
    },
    stopLoading() {
      this.loading = false;
    },
    handleError(err: any) {
      const msg = err?.message;
      this.error = msg;
      this.errorMessage = msg;
    },
  },
});

interface ServiceAvailabilities {
  serviceName: string;
  serviceId: string;
  availabilities: any[];
}

async function fetchServiceAvailabilities(
  service: any,
  begin: string,
  period: string,
): Promise<ServiceAvailabilities | null> {
  for (let attempt = 0; attempt <= AVAILABILITY_FETCH_RETRIES; attempt++) {
    try {
      const availabilities = await EsiriusService.getAvailability(
        PPEL_CODE,
        service.key,
        begin,
        period,
      );

      if (isEsiriusErrorPayload(availabilities)) {
        if (attempt < AVAILABILITY_FETCH_RETRIES) {
          continue;
        }
        return null;
      }

      if (Array.isArray(availabilities) && availabilities.length > 0) {
        return {
          serviceName: service.name,
          serviceId: service.key,
          availabilities,
        };
      }

      return null;
    } catch {
      if (attempt >= AVAILABILITY_FETCH_RETRIES) {
        return null;
      }
    }
  }

  return null;
}

function isEsiriusErrorPayload(availabilities: unknown): boolean {
  if (!Array.isArray(availabilities) || availabilities.length === 0) {
    return false;
  }

  return availabilities.every(
    (item: any) =>
      item &&
      typeof item === "object" &&
      "code" in item &&
      !("beginDateTime" in item),
  );
}

async function runWithConcurrency<T, R>(
  items: T[],
  concurrency: number,
  worker: (item: T) => Promise<R>,
): Promise<R[]> {
  if (items.length === 0) {
    return [];
  }

  const results = new Array<R>(items.length);
  let nextIndex = 0;

  const runNext = async () => {
    while (nextIndex < items.length) {
      const currentIndex = nextIndex;
      nextIndex += 1;
      results[currentIndex] = await worker(items[currentIndex]);
    }
  };

  await Promise.all(
    Array.from({ length: Math.min(concurrency, items.length) }, () => runNext()),
  );

  return results;
}
