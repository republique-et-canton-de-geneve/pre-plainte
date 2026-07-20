import { defineStore } from "pinia";
import { EsiriusService } from "@/services/esiriusService";

const PPEL_CODE = "PPEL";
const BAD_REQUEST_ERROR = 400;
const MILLISECONDS_IN_MINUTE = 60000;
const ISO_SLICE_LENGTH = 19;
const AVAILABILITY_FETCH_RETRIES = 3;
const AVAILABILITY_RETRY_DELAY_MS = 300;
const DEFAULT_AVAILABILITY_PERIOD_DAYS = 15;
const DEFAULT_BEGIN_OFFSET_HOURS = 1;
const FETCH_STATUS_OK = "ok";
const FETCH_STATUS_EMPTY = "empty";
const FETCH_STATUS_ERROR = "error";

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

    async loadAllAvailabilitiesForPPEL(begin?: string, period = DEFAULT_AVAILABILITY_PERIOD_DAYS) {
      this.availabilitiesLoadId += 1;
      const loadId = this.availabilitiesLoadId;
      this.startLoading();

      const beginDateTime = begin ?? buildDefaultBeginDateTime();

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
        const periodAsString = String(period);
        const results = await fetchAvailabilitiesSequentially(
          availableServices,
          beginDateTime,
          periodAsString,
        );

        if (loadId !== this.availabilitiesLoadId) {
          return;
        }

        const recovered = await recoverFailedAvailabilities(
          availableServices,
          results,
          beginDateTime,
          periodAsString,
        );

        if (loadId !== this.availabilitiesLoadId) {
          return;
        }

        this.allAvailabilities = recovered.flatMap(result => (result ? [result] : []));
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

type AvailabilityFetchResult =
  | { status: typeof FETCH_STATUS_OK; value: ServiceAvailabilities }
  | { status: typeof FETCH_STATUS_EMPTY }
  | { status: typeof FETCH_STATUS_ERROR };

function buildDefaultBeginDateTime(): string {
  const now = new Date();
  now.setHours(now.getHours() + DEFAULT_BEGIN_OFFSET_HOURS);
  return new Date(now.getTime() - now.getTimezoneOffset() * MILLISECONDS_IN_MINUTE)
    .toISOString()
    .slice(0, ISO_SLICE_LENGTH);
}

async function fetchAvailabilitiesSequentially(
  services: any[],
  begin: string,
  period: string,
): Promise<AvailabilityFetchResult[]> {
  const results: AvailabilityFetchResult[] = [];
  for (const service of services) {
    results.push(await fetchServiceAvailabilities(service, begin, period));
  }
  return results;
}

async function fetchServiceAvailabilities(
  service: any,
  begin: string,
  period: string,
): Promise<AvailabilityFetchResult> {
  for (let attempt = 0; attempt <= AVAILABILITY_FETCH_RETRIES; attempt++) {
    if (attempt > 0) {
      await wait(AVAILABILITY_RETRY_DELAY_MS * attempt);
    }

    const result = await tryFetchServiceAvailabilities(service, begin, period);
    if (result.status !== FETCH_STATUS_ERROR || attempt >= AVAILABILITY_FETCH_RETRIES) {
      return result;
    }
  }

  return { status: FETCH_STATUS_ERROR };
}

async function tryFetchServiceAvailabilities(
  service: any,
  begin: string,
  period: string,
): Promise<AvailabilityFetchResult> {
  try {
    const availabilities = await EsiriusService.getAvailability(
      PPEL_CODE,
      service.key,
      begin,
      period,
    );

    if (isEsiriusErrorPayload(availabilities)) {
      return { status: FETCH_STATUS_ERROR };
    }

    if (Array.isArray(availabilities) && availabilities.length > 0) {
      return {
        status: FETCH_STATUS_OK,
        value: {
          serviceName: service.name,
          serviceId: service.key,
          availabilities,
        },
      };
    }

    return { status: FETCH_STATUS_EMPTY };
  } catch {
    return { status: FETCH_STATUS_ERROR };
  }
}

async function recoverFailedAvailabilities(
  services: any[],
  results: AvailabilityFetchResult[],
  begin: string,
  period: string,
): Promise<Array<ServiceAvailabilities | null>> {
  const recovered: Array<ServiceAvailabilities | null> = results.map(result =>
    result.status === FETCH_STATUS_OK ? result.value : null,
  );

  const failedIndexes = results.flatMap((result, index) =>
    result.status === FETCH_STATUS_ERROR ? [index] : [],
  );

  if (failedIndexes.length === 0) {
    return recovered;
  }

  await wait(AVAILABILITY_RETRY_DELAY_MS);

  for (const index of failedIndexes) {
    const retryResult = await fetchServiceAvailabilities(services[index], begin, period);
    if (retryResult.status === FETCH_STATUS_OK) {
      recovered[index] = retryResult.value;
    }
  }

  return recovered;
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

function wait(ms: number): Promise<void> {
  return new Promise(resolve => {
    globalThis.setTimeout(resolve, ms);
  });
}
