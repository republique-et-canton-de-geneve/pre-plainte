import { afterEach, describe, expect, it } from "vitest";
import { nextTick } from "vue";
import { createPinia, setActivePinia } from "pinia";
import { i18n } from "@/common/i18n";
import { useDocumentTitleSync } from "@/composables/useDocumentTitleSync";
import { useLocaleStore } from "@/stores/localeStore";

describe("useDocumentTitleSync", () => {
  afterEach(async () => {
    useLocaleStore().locale = "fr";
    i18n.global.locale = "fr";
    await nextTick();
  });

  it("affiche le titre de la démarche et le met à jour selon la langue", async () => {
    setActivePinia(createPinia());
    const localeStore = useLocaleStore();
    i18n.global.locale = "fr";
    useDocumentTitleSync();

    expect(document.title).toBe("Déclaration de pré-plainte à la police | ge.ch");

    localeStore.locale = "en";
    i18n.global.locale = "en";
    await nextTick();

    expect(document.title).toBe("Police pre-complaint declaration | ge.ch");
  });
});
