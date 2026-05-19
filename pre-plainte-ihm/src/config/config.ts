let config: Record<string, string> = {};

/**
 * Charge la configuration depuis le backend /api/config
 */
export async function loadConfig(): Promise<void> {
  try {
    const res = await fetch("/api/config");
    if (res.ok) {
      config = await res.json();
    }
  } catch (err) {
    console.error("Erreur lors du chargement de la configuration :", err);
  }
}

export function getApiBaseUrl(): string | undefined {
  return config["backendUrl"];
}

export function getCaptchaSitekey(): string {
  return config["captchaSitekey"];
}

export function isCaptchaEnabled(): boolean {
  return config["captchaEnabled"] === "true";
}
