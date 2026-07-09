const UUID_FALLBACK_RADIX = 36;
const UUID_RANDOM_BYTE_MASK = 15;
const UUID_RANDOM_BYTE_DIVISOR = 4;
const AEL_DEMANDE_ID_PREFIX = "AEL-PPL-";
const AEL_DEMANDE_ID_RANDOM_LENGTH = 10;
const AEL_DEMANDE_ID_ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
let fallbackUuidCounter = 0;

const TYPE_INCIDENT_AEL_CODE: Record<string, string> = {
  vol: "V",
  "degat-delit": "D",
  cybercrime: "C",
};

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

function randomAelSuffix(): string {
  if (typeof crypto !== "undefined" && typeof crypto.getRandomValues === "function") {
    const bytes = new Uint8Array(AEL_DEMANDE_ID_RANDOM_LENGTH);
    crypto.getRandomValues(bytes);
    return Array.from(bytes, byte => AEL_DEMANDE_ID_ALPHANUM[byte % AEL_DEMANDE_ID_ALPHANUM.length]).join("");
  }

  return Array.from({ length: AEL_DEMANDE_ID_RANDOM_LENGTH }, () =>
    AEL_DEMANDE_ID_ALPHANUM[Math.floor(Math.random() * AEL_DEMANDE_ID_ALPHANUM.length)],
  ).join("");
}

export function generateAelDemandeId(typeIncident?: string | null): string {
  const incidentCode = typeIncident ? TYPE_INCIDENT_AEL_CODE[typeIncident] : undefined;
  const randomPart = randomAelSuffix();
  return incidentCode
    ? `${AEL_DEMANDE_ID_PREFIX}${incidentCode}-${randomPart}`
    : `${AEL_DEMANDE_ID_PREFIX}${randomPart}`;
}
