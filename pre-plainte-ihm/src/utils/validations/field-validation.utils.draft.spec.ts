import { afterEach, beforeEach, describe, expect, it } from "vitest";
import {
  STORAGE_KEYS,
  SESSION_KEYS,
  clearStorageData,
  hasPersistedDraft,
  loadLastLocalSavedAt,
  markDraftPromptHandledForSession,
  saveFormData,
  saveCurrentStep,
  saveLastLocalSavedAt,
  shouldOfferDraftResume,
  wasDraftPromptHandledThisSession,
} from "@/utils/validations/field-validation.utils";
import { getInitialFormData } from "@/utils/form/initial-form-data";

describe("reprise de brouillon local", () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
  });

  afterEach(() => {
    localStorage.clear();
    sessionStorage.clear();
  });

  it("ne detecte pas de brouillon quand l'etape est 1 et les donnees sont vides", () => {
    saveCurrentStep(1);
    saveFormData(getInitialFormData());

    expect(hasPersistedDraft()).toBe(false);
    expect(shouldOfferDraftResume()).toBe(false);
  });

  it("detecte un brouillon quand l'etape est superieure a 1", () => {
    saveCurrentStep(3);
    saveFormData(getInitialFormData());

    expect(hasPersistedDraft()).toBe(true);
    expect(shouldOfferDraftResume()).toBe(true);
  });

  it.each([
    { email: "anne@example.org" },
    { nom: "Martin" },
    { prenom: "Anne" },
    { typeIncident: "vol" },
  ])("detecte un brouillon a l'etape 1 quand des donnees significatives sont presentes (%j)", partial => {
    saveCurrentStep(1);
    saveFormData({
      ...getInitialFormData(),
      ...partial,
    });

    expect(hasPersistedDraft()).toBe(true);
    expect(shouldOfferDraftResume()).toBe(true);
  });

  it("detecte un brouillon quand des objets voles valides sont presents", () => {
    saveCurrentStep(1);
    saveFormData({
      ...getInitialFormData(),
      objetsVolesValides: [{ typeObjet: { code: "1", label: "Telephone" } }] as never[],
    });

    expect(hasPersistedDraft()).toBe(true);
  });

  it("detecte un brouillon quand des objets degrades valides sont presents", () => {
    saveCurrentStep(1);
    saveFormData({
      ...getInitialFormData(),
      objetsDegradesValides: [{ typeObjet: { code: "1", label: "Telephone" } }] as never[],
    });

    expect(hasPersistedDraft()).toBe(true);
  });

  it("memorise le traitement du dialogue pour la session courante", () => {
    expect(wasDraftPromptHandledThisSession()).toBe(false);

    markDraftPromptHandledForSession();

    expect(wasDraftPromptHandledThisSession()).toBe(true);
    expect(sessionStorage.getItem(SESSION_KEYS.DRAFT_PROMPT_HANDLED)).toBe("1");
  });

  it("n'offre plus la reprise une fois le dialogue traite dans la session", () => {
    saveCurrentStep(4);
    expect(shouldOfferDraftResume()).toBe(true);

    markDraftPromptHandledForSession();

    expect(shouldOfferDraftResume()).toBe(false);
  });

  it("persiste et recharge la date de derniere sauvegarde locale", () => {
    const savedAt = new Date("2026-07-20T10:15:00.000Z");

    saveLastLocalSavedAt(savedAt);

    expect(localStorage.getItem(STORAGE_KEYS.LAST_SAVED_AT)).toBe(savedAt.toISOString());
    expect(loadLastLocalSavedAt()?.toISOString()).toBe(savedAt.toISOString());
  });

  it("retourne null si la date de sauvegarde est absente ou invalide", () => {
    expect(loadLastLocalSavedAt()).toBeNull();

    localStorage.setItem(STORAGE_KEYS.LAST_SAVED_AT, "pas-une-date");

    expect(loadLastLocalSavedAt()).toBeNull();
  });

  it("efface la date de sauvegarde avec clearStorageData sans toucher la session", () => {
    saveLastLocalSavedAt(new Date("2026-07-20T10:15:00.000Z"));
    markDraftPromptHandledForSession();

    clearStorageData();

    expect(localStorage.getItem(STORAGE_KEYS.LAST_SAVED_AT)).toBeNull();
    expect(sessionStorage.getItem(SESSION_KEYS.DRAFT_PROMPT_HANDLED)).toBe("1");
  });
});
