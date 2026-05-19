/**
 * Utilitaires de formatage, validation et gestion des formulaires
 */

import type { PrePlainteFormFields } from "@/types/pre-plainte.interface";
import { EMAIL_CHALLENGE_CODE_LENGTH, EMAIL_CHALLENGE_CODE_MAX_DIGITS, TOTAL_STEPS } from "@/constants/constant.ts";

const HOSTNAME_MIN_LENGTH = 3;

/**
 * Code de vérification e-mail : uniquement des chiffres, sans espaces ni autres caractères.
 */
export const sanitizeEmailChallengeCodeInput = (value: string): string =>
  value.replaceAll(/\D/g, "").slice(0, EMAIL_CHALLENGE_CODE_MAX_DIGITS);

/** Code e-mail : exactement {@link EMAIL_CHALLENGE_CODE_LENGTH} chiffres (après normalisation). */
export function isValidEmailChallengeCodeFormat(value: string | null | undefined): boolean {
  const v = sanitizeEmailChallengeCodeInput(String(value ?? ""));
  return v.length === EMAIL_CHALLENGE_CODE_LENGTH;
}

/**
 * Message d’erreur i18n pour le champ code e-mail, ou `undefined` si la valeur est valide.
 */
export function getEmailChallengeCodeValidationMessage(
  value: string | null | undefined,
  t: (key: string, params?: Record<string, unknown>) => string,
): string | undefined {
  const v = sanitizeEmailChallengeCodeInput(String(value ?? ""));
  if (!v) {
    return t("validation.codeEmailRequis");
  }
  if (v.length !== EMAIL_CHALLENGE_CODE_LENGTH) {
    return t("validation.codeEmailLongueur", { n: EMAIL_CHALLENGE_CODE_LENGTH });
  }
  return undefined;
}

function hasWebHostnameWithDot(raw: string): boolean {
  const v = raw.trim();
  if (!v) {
    return false;
  }
  try {
    const withProto = /^https?:\/\//i.test(v) ? v : `https://${v.replace(/^\/+/, "")}`;
    const host = new URL(withProto).hostname;
    return Boolean(host && host.length >= HOSTNAME_MIN_LENGTH && host.includes("."));
  } catch {
    return false;
  }
}

/**
 * URL de site web avec nom de domaine (pas un numéro d’annonce seul à chiffres).
 */
export function isUrlWebAvecDomaine(raw: string | null | undefined): boolean {
  const v = typeof raw === "string" ? raw.trim() : "";
  if (!v || /^\d+$/.test(v)) {
    return false;
  }
  return hasWebHostnameWithDot(v);
}

/**
 * Vérifie si une valeur est vide ou nulle
 */
export const isBlank = (v?: string | null | undefined): v is null | undefined | "" => !v?.trim();

/**
 * Convertit une valeur en chaîne avec fallback
 */
export const toSafeString = (v: string | number | boolean | null | undefined, fallback = ""): string =>
  v == null ? fallback : String(v);

/**
 * Convertit une valeur en tableau de chaînes
 */
export const toStringArray = (v: readonly (string | number | boolean)[] | null | undefined): string[] =>
  Array.isArray(v) ? v.map(String) : [];

/**
 * Parse un nombre
 */
export const parseNumber = (value: string | number | null | undefined): number | undefined => {
  if (value == null || value === "") {
    return undefined;
  }
  const parsed = Number(value);
  return Number.isNaN(parsed) ? undefined : parsed;
};

/**
 * Normalise un nom (majuscules, espaces)
 */
export const normalizeName = (name?: string): string =>
  (name ?? "").trim().replaceAll(/\s+/g, " ").toLocaleUpperCase("fr-CH");

/**
 * Applique la normalisation des noms aux champs appropriés du formulaire
 * @param data - Les données du formulaire
 * @returns Les données avec les noms normalisés
 */
export const normalizeFormNames = <T extends Partial<PrePlainteFormFields>>(data: T): T => ({
  ...data,
  nom: data.nom === undefined ? data.nom : normalizeName(data.nom),
  tiersNom: data.tiersNom === undefined ? data.tiersNom : normalizeName(data.tiersNom),
  nomNaissance: data.nomNaissance === undefined ? data.nomNaissance : normalizeName(data.nomNaissance),
});

/**
 * Clés utilisées pour le localStorage
 */
export const STORAGE_KEYS = {
  FORM_DATA: "pp-data",
  STEP: "pp-step",
  OPEN_SECTION: "pp-open-section",
  EMAIL_CHALLENGE_KEY: "pp-email-challenge-key",
} as const;

export const saveEmailChallengeKey = (key: string | null | undefined): void => {
  try {
    if (!key?.trim()) {
      localStorage.removeItem(STORAGE_KEYS.EMAIL_CHALLENGE_KEY);
      return;
    }
    localStorage.setItem(STORAGE_KEYS.EMAIL_CHALLENGE_KEY, key);
  } catch (error) {
    console.warn("Erreur lors de la sauvegarde de la clé de challenge e-mail:", error);
  }
};

export const loadEmailChallengeKey = (): string | null => {
  try {
    return localStorage.getItem(STORAGE_KEYS.EMAIL_CHALLENGE_KEY);
  } catch (error) {
    console.warn("Erreur lors du chargement de la clé de challenge e-mail:", error);
    return null;
  }
};

/**
 * Sauvegarde les données du formulaire dans localStorage
 * @param data - Les données à sauvegarder
 */
export const saveFormData = (data: PrePlainteFormFields): void => {
  try {
    const {
      justificatifPersonneMorale,
      justificatifsPaiement,
      copiesEcran,
      autresDocuments,
      fichiers,
      ...rest
    } = data;
    localStorage.setItem(STORAGE_KEYS.FORM_DATA, JSON.stringify(rest));
  } catch (error) {
    console.warn("Erreur lors de la sauvegarde des données:", error);
  }
};

/**
 * Récupère les données du formulaire depuis localStorage
 * @returns Les données sauvegardées ou un objet vide
 */
export const loadFormData = (): Partial<PrePlainteFormFields> => {
  try {
    const savedDataRaw = localStorage.getItem(STORAGE_KEYS.FORM_DATA);
    if (!savedDataRaw) {
      return {};
    }

    const savedData = JSON.parse(savedDataRaw) as Partial<PrePlainteFormFields> & {
      dateDebutEvenement?: string;
    };

    if (!savedData.dateDebutEvenement && savedData.dateDebutEvenement) {
      (savedData as Partial<PrePlainteFormFields>).dateDebutEvenement = savedData.dateDebutEvenement;
      delete savedData.dateDebutEvenement;
    }

    return {
      ...normalizeFormNames(savedData),
      justificatifPersonneMorale: [],
      justificatifsPaiement: [],
      copiesEcran: [],
      autresDocuments: [],
      fichiers: [],
    };
  } catch (error) {
    console.warn("Erreur lors du chargement des données:", error);
    return {};
  }
};

/**
 * Sauvegarde l'étape courante dans localStorage
 * @param step - L'étape à sauvegarder
 */
export const saveCurrentStep = (step: number): void => {
  localStorage.setItem(STORAGE_KEYS.STEP, String(step));
};

/**
 * Récupère l'étape courante depuis localStorage
 * @returns L'étape sauvegardée ou 1 par défaut
 */
export const loadCurrentStep = (): number => {
  const savedStep = Number(localStorage.getItem(STORAGE_KEYS.STEP) || 1);
  return savedStep >= 1 && savedStep <= TOTAL_STEPS ? savedStep : 1;
};

/**
 * Nettoie toutes les données du localStorage
 */
export const clearStorageData = (): void => {
  Object.values(STORAGE_KEYS).forEach(key => {
    localStorage.removeItem(key);
  });
};

/**
 * Valide qu'un numéro d'étape est dans la plage autorisée
 * @param step - Le numéro d'étape à valider
 * @returns true si l'étape est valide
 */
export const isValidStep = (step: number): boolean => step >= 1 && step <= TOTAL_STEPS;

/**
 * Calcule l'étape suivante
 * @param currentStep - L'étape courante
 * @returns L'étape suivante ou null si impossible
 */
export const getNextStep = (currentStep: number): number | null => (currentStep < TOTAL_STEPS ? currentStep + 1 : null);

/**
 * Calcule l'étape précédente
 * @param currentStep - L'étape courante
 * @returns L'étape précédente ou null si impossible
 */
export const getPreviousStep = (currentStep: number): number | null => (currentStep > 1 ? currentStep - 1 : null);
