const EMAIL_CHALLENGE_BYPASS_STORAGE_KEY = "pp-dev-skip-email-challenge";

function isEmailChallengeBypassDisabledLocally(): boolean {
  try {
    return localStorage.getItem(EMAIL_CHALLENGE_BYPASS_STORAGE_KEY) === "false";
  } catch {
    return false;
  }
}

export function isDevEmailChallengeBypassed(): boolean {
  if (!import.meta.env.DEV) {
    return false;
  }
  if (import.meta.env.VITE_DEV_SKIP_EMAIL_CHALLENGE === "false") {
    return false;
  }
  if (isEmailChallengeBypassDisabledLocally()) {
    return false;
  }
  return true;
}
