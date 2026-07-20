import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { createPinia, setActivePinia } from "pinia";
import { nextTick } from "vue";
import { useCreatePrePlainteStore } from "@/stores/createPrePlainteStore";
import { STORAGE_KEYS, saveLastLocalSavedAt } from "@/utils/validations/field-validation.utils";

describe("createPrePlainteStore - sauvegarde locale", () => {
  beforeEach(() => {
    localStorage.clear();
    sessionStorage.clear();
    setActivePinia(createPinia());
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
    localStorage.clear();
    sessionStorage.clear();
  });

  it("initialise lastLocalSavedAt depuis le localStorage", () => {
    const savedAt = new Date("2026-07-20T08:00:00.000Z");
    saveLastLocalSavedAt(savedAt);
    setActivePinia(createPinia());

    const store = useCreatePrePlainteStore();

    expect(store.lastLocalSavedAt?.toISOString()).toBe(savedAt.toISOString());
  });

  it("met a jour lastLocalSavedAt lors d'une modification du formulaire", async () => {
    const store = useCreatePrePlainteStore();
    expect(store.lastLocalSavedAt).toBeNull();

    store.userFormData.email = "anne.martin@example.org";
    await nextTick();
    await vi.runAllTimersAsync();

    expect(store.lastLocalSavedAt).toBeInstanceOf(Date);
    expect(localStorage.getItem(STORAGE_KEYS.LAST_SAVED_AT)).toBe(store.lastLocalSavedAt?.toISOString());
    expect(localStorage.getItem(STORAGE_KEYS.FORM_DATA)).toContain("anne.martin@example.org");
  });

  it("arrete la persistence apres soumission", async () => {
    const store = useCreatePrePlainteStore();
    store.userFormData.email = "avant@example.org";
    await nextTick();
    await vi.runAllTimersAsync();

    store.clearPersistedDataAfterSubmission();
    const savedAtAfterClear = localStorage.getItem(STORAGE_KEYS.LAST_SAVED_AT);

    store.userFormData.email = "apres@example.org";
    await nextTick();
    await vi.runAllTimersAsync();

    expect(localStorage.getItem(STORAGE_KEYS.FORM_DATA)).toBeNull();
    expect(localStorage.getItem(STORAGE_KEYS.LAST_SAVED_AT)).toBe(savedAtAfterClear);
  });
});
