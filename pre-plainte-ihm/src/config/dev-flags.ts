/**
 * Contournement du challenge e-mail en développement (équivalent pratique au mode « dev » du captcha).
 * Désactivation : `.env.local` avec `VITE_DEV_SKIP_EMAIL_CHALLENGE=false`.
 * Inactif en production.
 */
export function isDevEmailChallengeBypassed(): boolean {
  if (!import.meta.env.DEV) {
    return false;
  }
  if (import.meta.env.VITE_DEV_SKIP_EMAIL_CHALLENGE === "false") {
    return false;
  }
  return true;
}
