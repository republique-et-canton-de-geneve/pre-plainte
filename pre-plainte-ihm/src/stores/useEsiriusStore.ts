import { defineStore } from "pinia";
import { EsiriusService } from "@/services/esiriusService";

const PPEL_CODE = "PPEL";
const BAD_REQUEST_ERROR = 400;
const MILLISECONDS_IN_MINUTE = 60000;
const ISO_SLICE_LENGTH = 19;

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
    errorMessage: "" as string,
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
      this.startLoading();
      this.allAvailabilities = [];

      if (!begin) {
        const now = new Date();
        now.setHours(now.getHours() + 1);
        begin = new Date(now.getTime() - now.getTimezoneOffset() * MILLISECONDS_IN_MINUTE).toISOString().slice(0, ISO_SLICE_LENGTH);
      }

      try {
        const services = await EsiriusService.getServiceListBySiteCode(PPEL_CODE);
        const availableServices = services.filter((s: any) => s.existAvailabilities);

        const results = await Promise.all(
          availableServices.map(async (service: any) => {
            const availabilities = await EsiriusService.getAvailability(
              PPEL_CODE,
              service.key,
              begin,
              String(period),
            );

            if (Array.isArray(availabilities) && availabilities.length > 0) {
              return {
                serviceName: service.name,
                serviceId: service.key,
                availabilities,
              };
            }
            return null;
          }),
        );

        this.allAvailabilities = results.filter(r => r && r.availabilities.length > 0) as any[];
      } catch (err: any) {
        this.handleError(err);
      } finally {
        this.stopLoading();
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
