/**
 * Logs détaillés des échecs HTTP pour le diagnostic (console navigateur : filtrer "PPL API").
 * Ne journalise pas le corps complet des requêtes (données personnelles).
 */

const MAX_RESPONSE_BODY_CHARS = 8000;

export function summarizePayloadKeys(payload: unknown): Record<string, unknown> {
  if (payload === null || typeof payload !== "object") {
    return { _type: typeof payload };
  }
  const o = payload as Record<string, unknown>;
  const incident = o.incident as Record<string, unknown> | undefined;
  const details = incident?.details as Record<string, unknown> | undefined;
  return {
    topLevelKeys: Object.keys(o),
    typeIncident: details?.typeIncident,
    typeCybercrime: details?.typeCybercrime,
  };
}

export function truncateForLog(text: string, max = MAX_RESPONSE_BODY_CHARS): string {
  if (text.length <= max) {
    return text;
  }
  return `${text.slice(0, max)}\n… [tronqué, ${text.length} caractères au total]`;
}

export function logApiHttpFailure(
  context: string,
  method: string,
  url: string,
  response: Response,
  responseBodyText: string,
  requestHint?: Record<string, unknown>,
): void {
  const entry = {
    context,
    url,
    method,
    status: response.status,
    statusText: response.statusText,
    responseBody: truncateForLog(responseBodyText || "(vide)"),
    ...requestHint,
  };
  console.error("[PPL API] Échec HTTP", entry);
}
