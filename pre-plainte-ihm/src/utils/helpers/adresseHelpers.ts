import { computed, type ComputedRef, type Ref } from "vue";

export interface HintKeys {
  manual: string;
  auto: string;
  choice: string;
}

/**
 * Crée un computed pour le hint d'un champ d'adresse
 * @param keys - les clés de traduction {manual, auto, choice}
 * @param canSearch - ref qui indique si l'autocomplétion est possible
 * @param selectedAddress - ref avec l'adresse sélectionnée
 * @param t - fonction de traduction (vue-i18n)
 */
export const createHintAdresse = (
  keys: HintKeys,
  canSearch: Ref<boolean>,
  selectedAddress: Ref,
  t: (key: string) => string
): ComputedRef<string> => {
  return computed(() => {
    if (!canSearch.value) {
      return t(keys.manual);
    }

    if (selectedAddress.value) {
      return t(keys.auto);
    }

    return t(keys.choice);
  });
};
