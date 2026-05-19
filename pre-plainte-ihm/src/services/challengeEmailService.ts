import { getApiBaseUrl } from "@/config/config";
import { isDevEmailChallengeBypassed } from "@/config/dev-flags";
import { logApiHttpFailure } from "@/utils/api-http-log";
import { getUserFacingApiErrorMessage } from "@/utils/api-http-user-message";
import { getEmailLanguage } from "@/utils/helpers/traductionHelper";

const backendUrl = getApiBaseUrl() || "";
const baseUrl = `${backendUrl}/api/email-challenges`;
const HTTP_NOT_FOUND = 404;
const HTTP_TOO_MANY_REQUESTS = 429;

export class EmailChallengeTooManyRequestsError extends Error {
  readonly errorCode = "EMAIL_CHALLENGE_TOO_MANY_REQUESTS" as const;
  constructor(message: string) {
    super(message);
    this.name = "EmailChallengeTooManyRequestsError";
  }
}

function tryParseErrorBody(bodyText: string): { message?: string; errorCode?: string } | null {
  const raw = bodyText.trim();
  if (!raw.startsWith("{")) {
    return null;
  }
  try {
    const o = JSON.parse(raw) as { message?: string; errorCode?: string };
    return typeof o === "object" && o !== null ? o : null;
  } catch {
    return null;
  }
}

export type VerifyStatus = "SUCCESS" | "INVALID" | "EXPIRED" | "LOCKED" | "ALREADY_VERIFIED" | "NOT_FOUND";

export interface VerifyResult {
  success: boolean;
  status: VerifyStatus;
  remainingAttempts?: number | null;
}

export async function requestEmailChallengeCode(email: string, key: string): Promise<void> {
  if (isDevEmailChallengeBypassed()) {
    console.warn("[PPL dev] Challenge e-mail ignoré (mode dev, bypass actif)");
    return;
  }

  const url = `${baseUrl}/request`;
  const language = getEmailLanguage();
  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json", "Accept-Language": language },
    body: JSON.stringify({ email, key }),
  });

  if (!response.ok) {
    const bodyText = await response.text();
    logApiHttpFailure("challengeEmailService.requestEmailChallengeCode", "POST", url, response, bodyText, {
      keyPresent: Boolean(key),
      language,
    });
    if (response.status === HTTP_TOO_MANY_REQUESTS) {
      const parsed = tryParseErrorBody(bodyText);
      const msg = parsed?.message?.trim() || getUserFacingApiErrorMessage(response.status, bodyText);
      throw new EmailChallengeTooManyRequestsError(msg);
    }
    throw new Error(getUserFacingApiErrorMessage(response.status, bodyText));
  }
}

export async function verifyEmailChallengeCode(email: string, key: string, code: string): Promise<VerifyResult> {
  if (isDevEmailChallengeBypassed()) {
    return { success: true, status: "SUCCESS", remainingAttempts: null };
  }

  const url = `${baseUrl}/verify`;
  if (!key?.trim()) {
    return { success: false, status: "NOT_FOUND", remainingAttempts: null };
  }

  const response = await fetch(url, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, key, code }),
  });

  if (response.ok) {
    return (await response.json()) as VerifyResult;
  }

  const bodyText = await response.text();
  logApiHttpFailure("challengeEmailService.verifyEmailChallengeCode", "POST", url, response, bodyText, {
    keyPresent: Boolean(key),
  });

  if (response.status === HTTP_NOT_FOUND) {
    return { success: false, status: "NOT_FOUND", remainingAttempts: null };
  }

  throw new Error(getUserFacingApiErrorMessage(response.status, bodyText));
}
