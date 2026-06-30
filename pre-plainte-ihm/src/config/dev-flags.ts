export function isDevEmailChallengeBypassed(): boolean {
  if (!import.meta.env.DEV) {
    return false;
  }
  if (import.meta.env.VITE_DEV_SKIP_EMAIL_CHALLENGE === "false") {
    return false;
  }
  return true;
}
