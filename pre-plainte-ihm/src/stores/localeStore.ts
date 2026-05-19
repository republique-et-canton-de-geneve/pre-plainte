import { defineStore } from "pinia";
import { ref } from "vue";

export const SUPPORTED_LOCALES = ["fr", "de", "en", "pt"] as const;
export type AppLocale = (typeof SUPPORTED_LOCALES)[number];

const DEFAULT_LOCALE: AppLocale = "fr";
const LOCALE_STORAGE_KEY = "pre-plainte-locale";

function isSupportedLocale(value: unknown): value is AppLocale {
  return typeof value === "string" && SUPPORTED_LOCALES.includes(value as AppLocale);
}

function getStoredLocale(): AppLocale {
  const stored = localStorage.getItem(LOCALE_STORAGE_KEY);
  return isSupportedLocale(stored) ? stored : DEFAULT_LOCALE;
}

export const useLocaleStore = defineStore("locale", () => {
  const locale = ref<AppLocale>(DEFAULT_LOCALE);

  function initLocale() {
    locale.value = getStoredLocale();
  }

  function setLocale(value: AppLocale) {
    locale.value = value;
    localStorage.setItem(LOCALE_STORAGE_KEY, value);
  }

  return {
    locale,
    initLocale,
    setLocale,
  };
});
