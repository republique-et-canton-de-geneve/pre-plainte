import { getApiBaseUrl } from "@/config/config";
import { getUserFacingApiErrorMessage } from "@/utils/api-http-user-message";

const backendUrl = getApiBaseUrl() || "";
const baseUrl = `${backendUrl}/api/esirius`;

export class EsiriusService {
  static async getServiceListBySiteCode(siteCode: string): Promise<any[]> {
    try {
      const url = `${baseUrl}/sites/${siteCode}/listServices`;
      const response = await fetch(url);
      if (!response.ok) {
        const bodyText = await response.text();
        throw new Error(getUserFacingApiErrorMessage(response.status, bodyText));
      }
      return await response.json();
    } catch (error) {
      throw error;
    }
  }

  static async getAvailability(siteCode: string, serviceId: string, begin: string, period: string): Promise<any[]> {
    try {
      const url = `${baseUrl}/sites/${siteCode}/services/${serviceId}/plannings/begins/${begin}/periods/${period}/availabilities`;
      const response = await fetch(url);
      if (!response.ok) {
        const bodyText = await response.text();
        throw new Error(getUserFacingApiErrorMessage(response.status, bodyText));
      }
      return await response.json();
    } catch (error) {
      throw error;
    }
  }

  static async createAppointment(payload: Record<string, any>): Promise<any> {
    try {
      const url = `${baseUrl}/appointments`;
      const response = await fetch(url, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      if (!response.ok) {
        const bodyText = await response.text();
        throw new Error(getUserFacingApiErrorMessage(response.status, bodyText));
      }
      return await response.json();
    } catch (error) {
      throw error;
    }
  }

  static async getAppointmentByCode(codeRdv: string): Promise<any> {
    try {
      const url = `${baseUrl}/appointments/${codeRdv}`;
      const response = await fetch(url);
      if (!response.ok) {
        const bodyText = await response.text();
        throw new Error(getUserFacingApiErrorMessage(response.status, bodyText));
      }
      return await response.json();
    } catch (error) {
      throw error;
    }
  }

  static async cancelAppointment(codeRdv: string): Promise<any> {
    try {
      const url = `${baseUrl}/appointments/${codeRdv}`;
      const response = await fetch(url, { method: "DELETE" });
      if (!response.ok) {
        const bodyText = await response.text();
        throw new Error(getUserFacingApiErrorMessage(response.status, bodyText));
      }
      return await response.json();
    } catch (error) {
      throw error;
    }
  }

  static async updateAppointment(payload: Record<string, any>): Promise<any> {
    try {
      const url = `${baseUrl}/appointments`;
      const response = await fetch(url, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });
      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(getUserFacingApiErrorMessage(response.status, errorText));
      }
      return await response.json();
    } catch (error) {
      throw error;
    }
  }
}
