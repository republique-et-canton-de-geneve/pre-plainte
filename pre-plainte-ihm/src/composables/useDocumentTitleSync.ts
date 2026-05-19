import { watch } from "vue";
import { storeToRefs } from "pinia";
import { useCreatePrePlainteStore } from "@/stores/createPrePlainteStore";
import { i18n } from "@/common/i18n";
import { STEPS } from "@/constants/constant";

export function useDocumentTitleSync() {
  const store = useCreatePrePlainteStore();
  const { step } = storeToRefs(store);

  const computeTitle = () => {
    const appName = i18n.global.t("titreApplication.titre");
    const stepTitle = STEPS[step.value - 1].titleKey;

    return `${appName} — ${step.value} : ${stepTitle}`;
  };

  const apply = () => {
    document.title = computeTitle();
  };

  watch(step, apply, { immediate: true });

  watch(() => i18n.global.locale, apply, { immediate: true });
}
