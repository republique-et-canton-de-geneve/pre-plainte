import { getApiBaseUrl } from "@/config/config";
import { logApiHttpFailure, summarizePayloadKeys } from "@/utils/api-http-log";
import { getUserFacingApiErrorMessage } from "@/utils/api-http-user-message";

const backendUrl = getApiBaseUrl() || "";
const baseUrl = `${backendUrl}/api/preplainte`;

export class PrePlainteService {
  static async submit(payload: Record<string, any>, captchaToken: string | null | undefined): Promise<string | null> {
    const url = `${baseUrl}/soumission`;
    const response = await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "X-Captcha-Token": captchaToken || "",
      },
      body: JSON.stringify(payload),
    });

    if (!response.ok) {
      const bodyText = await response.text();
      logApiHttpFailure("PrePlainteService.submit", "POST", url, response, bodyText, {
        payloadSummary: summarizePayloadKeys(payload),
        captchaHeaderPresent: Boolean(captchaToken && String(captchaToken).trim()),
      });
      throw new Error(getUserFacingApiErrorMessage(response.status, bodyText));
    }

    const contentLength = response.headers.get("content-length");
    if (contentLength === "0") {
      return null;
    }

    const contentType = response.headers.get("content-type") || "";
    if (contentType.includes("application/json")) {
      return response.json();
    }

    return response.text();
  }
}
