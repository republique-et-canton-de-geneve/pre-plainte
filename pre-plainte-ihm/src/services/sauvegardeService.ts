import { getApiBaseUrl } from "@/config/config";
import type { PrePlainteDTO } from "@/types/preplainte-payload-interface";
import { logApiHttpFailure, summarizePayloadKeys } from "@/utils/api-http-log";
import { getUserFacingApiErrorMessage } from "@/utils/api-http-user-message";
import { getEmailLanguage } from "@/utils/helpers/traductionHelper";

const backendUrl = getApiBaseUrl() || "";
const baseUrl = `${backendUrl}/api/preplainte`;

export class SauvegardeService {
  static async sauvegarderPreplainte(payload: PrePlainteDTO, captchaToken: string): Promise<void> {
    const url = `${baseUrl}/draft`;
    const language = getEmailLanguage();
    const response = await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Captcha-Token": captchaToken,
        "Accept-Language": language,
      },
      body: JSON.stringify(payload),
    });
    if (!response.ok) {
      const errorBody = await response.text();
      logApiHttpFailure("SauvegardeService.sauvegarderPreplainte", "POST", url, response, errorBody, {
        payloadSummary: summarizePayloadKeys(payload),
        captchaHeaderPresent: Boolean(captchaToken && String(captchaToken).trim()),
      });
      throw new Error(getUserFacingApiErrorMessage(response.status, errorBody));
    }
  }

  static async reprendrePreplainte(demandeId: string): Promise<PrePlainteDTO> {
    const url = `${baseUrl}/draft/${demandeId}`;
    const response = await fetch(url, {
      method: "GET",
      headers: { "Content-Type": "application/json" },
    });
    if (!response.ok) {
      const bodyText = await response.text();
      logApiHttpFailure("SauvegardeService.reprendrePreplainte", "GET", url, response, bodyText, { demandeId });
      throw new Error(getUserFacingApiErrorMessage(response.status, bodyText));
    }
    return response.json();
  }
}
