const DEFAULT_BASE_URL = "http://localhost:8080";
const DEFAULT_CHECK_PATH = "/api/config";
const DEFAULT_TIMEOUT_MS = 30000;
const REQUEST_TIMEOUT_MS = 3000;
const RETRY_INTERVAL_MS = 1000;

const baseUrl = process.env.CYPRESS_BACKEND_URL || process.env.CYPRESS_BASE_URL || DEFAULT_BASE_URL;
const checkPath = process.env.CYPRESS_BACKEND_CHECK_PATH || DEFAULT_CHECK_PATH;
const timeoutMs = Number(process.env.CYPRESS_BACKEND_TIMEOUT_MS || DEFAULT_TIMEOUT_MS);
const checkUrl = new URL(checkPath, baseUrl.endsWith("/") ? baseUrl : `${baseUrl}/`).toString();

const wait = ms => new Promise(resolve => setTimeout(resolve, ms));

async function fetchWithTimeout(url) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), REQUEST_TIMEOUT_MS);

  try {
    return await fetch(url, {
      signal: controller.signal,
      headers: {
        Accept: "application/json",
      },
    });
  } finally {
    clearTimeout(timeout);
  }
}

async function checkBackend() {
  const startedAt = Date.now();
  let lastError = "";

  while (Date.now() - startedAt <= timeoutMs) {
    try {
      const response = await fetchWithTimeout(checkUrl);

      if (response.ok) {
        console.log(`Backend disponible sur ${checkUrl}`);
        return;
      }

      lastError = `statut HTTP ${response.status}`;
    } catch (error) {
      lastError = error instanceof Error ? error.message : String(error);
    }

    await wait(RETRY_INTERVAL_MS);
  }

  console.error(`Backend indisponible sur ${checkUrl} apres ${timeoutMs} ms (${lastError})`);
  process.exit(1);
}

await checkBackend();
