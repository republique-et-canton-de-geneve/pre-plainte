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

export function resolveApiUrl(path: string): string {
  const normalizedPath = path.startsWith("/") ? path : `/${path}`;
  const backendUrl = getApiBaseUrl()?.trim();

  if (shouldUseRelativeApiUrl(backendUrl)) {
    return normalizedPath;
  }

  return `${backendUrl!.replace(/\/$/, "")}${normalizedPath}`;
}

function shouldUseRelativeApiUrl(backendUrl?: string): boolean {
  if (typeof globalThis.location === "undefined") {
    return !backendUrl;
  }

  const host = globalThis.location.hostname;
  if (host === "localhost" || host === "127.0.0.1") {
    return true;
  }

  if (!backendUrl) {
    return true;
  }

  try {
    return new URL(backendUrl).origin === globalThis.location.origin;
  } catch {
    return true;
  }
}

export function getCaptchaSitekey(): string {
  return config["captchaSitekey"];
}

export function isCaptchaEnabled(): boolean {
  return config["captchaEnabled"] === "true";
}
