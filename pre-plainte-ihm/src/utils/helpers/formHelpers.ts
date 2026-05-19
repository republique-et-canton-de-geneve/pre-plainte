import { type Ref, watch } from "vue";
import { EMPTY_VALUE_DISPLAY } from "@/constants/constant.ts";

export function resetFieldsOnCondition<T>(
  source: Ref<T>,
  fields: Ref[],
  condition: (value: T) => boolean = Boolean,
) {
  watch(source, value => {
    if (condition(value)) {
      fields.forEach(field => {
        field.value = "";
      });
    }
  });
}

export function resetFilesOnCondition<T>(
  source: Ref<T>,
  fields: Ref[],
  condition: (value: T) => boolean = Boolean,
) {
  watch(source, value => {
    if (condition(value)) {
      fields.forEach(field => {
        field.value = [];
      });
    }
  });
}

export function resetFieldsOnToggle<T>(
  source: Ref<T>,
  onTrue: () => void,
  onFalse?: () => void,
) {
  watch(source, value => {
    if (value) {
      onTrue();
    } else {
      onFalse?.();
    }
  });
}

export function displayValue(value: unknown) {
  return value || EMPTY_VALUE_DISPLAY;
}

export function formatOuiNon(
  value: boolean | null | undefined,
  t: (key: string) => string): string {
  if (value === true) {
    return t("common.oui");
  }
  if (value === false) {
    return t("common.non");
  }
  return EMPTY_VALUE_DISPLAY;
}
