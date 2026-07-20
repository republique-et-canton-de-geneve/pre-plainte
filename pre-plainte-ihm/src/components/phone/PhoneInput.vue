<template>
  <VPhoneInput
    :model-value="modelValue"
    :label="displayLabel"
    :error-messages="errorMessages"
    :hint="hint"
    :persistent-hint="persistentHint"
    variant="outlined"
    :class="inputClass"
    :default-country="defaultCountryCode"
    :required="required"
    display-format="national"
    :guess-country="true"
    :invalid-message="invalidMessageFn"
    :enable-searching-country="true"
    autocomplete="tel"
    @update:model-value="onUpdate"
  />
</template>

<script setup lang="ts">
import { VPhoneInput } from "v-phone-input";
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { requiredLabel } from "@/utils/helpers/labelHelpers";

const { t } = useI18n();

interface Props {
  modelValue: string;
  label?: string;
  errorMessages?: string | string[];
  hint?: string;
  persistentHint?: boolean;
  inputClass?: string;
  defaultCountryCode?: string;
  required?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  persistentHint: true,
  defaultCountryCode: "CH",
});

const emit = defineEmits<{
  "update:modelValue": [value: string];
}>();

function onUpdate(value: string) {
  emit("update:modelValue", value);
}

const displayLabel = computed(() => {
  const label = props.label ?? "";
  return props.required ? requiredLabel(label) : label;
});

function invalidMessageFn({ example }: { label?: string; example?: string }) {
  return t("validation.telephoneInvalid", { example: example || "078 123 45 67" });
}
</script>

<style>
.v-overlay-container .v-overlay__content:has(.v-phone-input__country__icon) .v-list {
  min-width: min(500px, calc(100vw - 32px));
  max-width: min(500px, calc(100vw - 32px));
}

.v-phone-input__country__input {
  min-width: 95px !important;
  max-width: 95px !important;
}
</style>
