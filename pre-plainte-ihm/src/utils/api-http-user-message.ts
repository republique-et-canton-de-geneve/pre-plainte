import { i18n } from "@/common/i18n";

const USER_MESSAGE_MIN_LENGTH = 1;
const USER_MESSAGE_MAX_LENGTH = 400;
const HTTP_STATUS_TIMEOUT = 408;
const HTTP_STATUS_UNAUTHORIZED = 401;
const HTTP_STATUS_FORBIDDEN = 403;
const HTTP_STATUS_NOT_FOUND = 404;
const HTTP_STATUS_TOO_MANY_REQUESTS = 429;
const HTTP_STATUS_INTERNAL_SERVER_ERROR = 500;
const HTTP_STATUS_BAD_GATEWAY = 502;
const HTTP_STATUS_SERVICE_UNAVAILABLE = 503;
const HTTP_STATUS_GATEWAY_TIMEOUT = 504;
const HTTP_STATUS_BAD_REQUEST = 400;
const JSON_MESSAGE_PREFIXES = ["{", "["] as const;
const SUSPICIOUS_LOWER_TOKENS = [
  "exception",
  "stacktrace",
  "\tat ",
  "internal server error",
  "nullpointer",
  "error 500",
] as const;
const SUSPICIOUS_RAW_TOKENS = ["<html", "<!doctype"] as const;

const SERVER_ERROR_MESSAGE_BY_STATUS: Record<number, string> = {
  [HTTP_STATUS_BAD_GATEWAY]: "apiErrors.passerelle",
  [HTTP_STATUS_SERVICE_UNAVAILABLE]: "apiErrors.serviceIndisponible",
  [HTTP_STATUS_GATEWAY_TIMEOUT]: "apiErrors.delaiDepasse",
};

const CLIENT_ERROR_MESSAGE_BY_STATUS: Record<number, string> = {
  [HTTP_STATUS_NOT_FOUND]: "apiErrors.nonTrouve",
  [HTTP_STATUS_UNAUTHORIZED]: "apiErrors.accesRefuse",
  [HTTP_STATUS_FORBIDDEN]: "apiErrors.accesRefuse",
  [HTTP_STATUS_TOO_MANY_REQUESTS]: "apiErrors.tropDeRequetes",
  [HTTP_STATUS_TIMEOUT]: "apiErrors.delaiDepasse",
};

function tryParseJsonMessage(bodyText: string): string | null {
  const raw = bodyText.trim();
  const looksLikeJson = JSON_MESSAGE_PREFIXES.some(prefix => raw.startsWith(prefix));
  if (!raw || !looksLikeJson) {
    return null;
  }
  try {
    const o = JSON.parse(raw) as { message?: string; detail?: string; error?: string };
    const messageCandidate = o.message ?? o.detail ?? o.error;
    return typeof messageCandidate === "string" ? messageCandidate.trim() || null : null;
  } catch {
    return null;
  }
}

/** Évite d’afficher des traces techniques ou du HTML brut à l’utilisateur. */
export function isProbablySafeUserFacingMessage(msg: string): boolean {
  const trimmedMessage = msg.trim();
  if (trimmedMessage.length < USER_MESSAGE_MIN_LENGTH || trimmedMessage.length > USER_MESSAGE_MAX_LENGTH) {
    return false;
  }
  const lower = trimmedMessage.toLowerCase();
  const hasSuspiciousLowerToken = SUSPICIOUS_LOWER_TOKENS.some(token => lower.includes(token));
  const hasSuspiciousRawToken = SUSPICIOUS_RAW_TOKENS.some(token => trimmedMessage.includes(token));
  return !hasSuspiciousLowerToken && !hasSuspiciousRawToken;
}

/**
 * Message affiché à l’utilisateur après un échec HTTP (les détails restent dans les logs via logApiHttpFailure).
 */
export function getUserFacingApiErrorMessage(status: number, bodyText: string): string {
  const t = i18n.global.t;
  const parsed = tryParseJsonMessage(bodyText);

  if (status >= HTTP_STATUS_INTERNAL_SERVER_ERROR) {
    const specificServerErrorKey = SERVER_ERROR_MESSAGE_BY_STATUS[status];
    if (specificServerErrorKey) {
      return t(specificServerErrorKey);
    }
    return t("apiErrors.erreurServeur");
  }

  if (parsed && isProbablySafeUserFacingMessage(parsed)) {
    return parsed;
  }

  const specificClientErrorKey = CLIENT_ERROR_MESSAGE_BY_STATUS[status];
  if (specificClientErrorKey) {
    return t(specificClientErrorKey);
  }
  if (status >= HTTP_STATUS_BAD_REQUEST && status < HTTP_STATUS_INTERNAL_SERVER_ERROR) {
    return t("apiErrors.requeteInvalide");
  }

  return t("apiErrors.generique");
}
