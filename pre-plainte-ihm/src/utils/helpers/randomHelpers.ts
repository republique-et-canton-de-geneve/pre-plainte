const UUID_FALLBACK_RADIX = 36;
const UUID_RANDOM_BYTE_MASK = 15;
const UUID_RANDOM_BYTE_DIVISOR = 4;
let fallbackUuidCounter = 0;

export function generateUuid(): string {
  if (typeof crypto !== "undefined" && typeof crypto.randomUUID === "function") {
    return crypto.randomUUID();
  }

  if (typeof crypto !== "undefined" && typeof crypto.getRandomValues === "function") {
    return "10000000-1000-4000-8000-100000000000".replaceAll(/[018]/g, c =>
      (Number(c) ^ (crypto.getRandomValues(new Uint8Array(1))[0] & (UUID_RANDOM_BYTE_MASK >> (Number(c) / UUID_RANDOM_BYTE_DIVISOR)))).toString(16),
    );
  }

  fallbackUuidCounter += 1;
  return `fallback-${Date.now().toString(UUID_FALLBACK_RADIX)}-${fallbackUuidCounter.toString(UUID_FALLBACK_RADIX)}`;
}
