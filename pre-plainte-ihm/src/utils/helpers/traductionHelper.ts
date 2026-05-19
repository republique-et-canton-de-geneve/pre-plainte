import { useLocaleStore } from "@/stores/localeStore";

export type I18nOption<T extends string = string> = {
  value: T;
  labelKey: string;
};

export function toTranslatedOptions<T extends string>(items: readonly I18nOption<T>[], t: (key: string) => string) {
  return items.map(item => ({
    value: item.value,
    label: t(item.labelKey),
  }));
}

export function getEmailLanguage(): "fr" | "en" | "de" | "pt" {
  const localeStore = useLocaleStore();
  const locale = localeStore.locale?.toLowerCase();

  switch (locale) {
    case "en":
    case "de":
    case "pt":
      return locale;
    default:
      return "fr";
  }
}
