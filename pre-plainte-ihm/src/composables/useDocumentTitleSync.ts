import { watch } from "vue";
import { storeToRefs } from "pinia";
import { i18n } from "@/common/i18n";
import { useLocaleStore } from "@/stores/localeStore";

export function useDocumentTitleSync() {
  const { locale } = storeToRefs(useLocaleStore());

  watch(
    locale,
    () => {
      document.title = `${i18n.global.t("titreApplication.prePlainte", 1, { locale: locale.value })} | ge.ch`;
    },
    { immediate: true },
  );
}
