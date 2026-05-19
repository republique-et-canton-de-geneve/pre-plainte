<template>
  <VPhoneInput
    :model-value="modelValue"
    :label="label"
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
    @update:model-value="onUpdate"
  />
</template>

<script setup lang="ts">
import { VPhoneInput } from "v-phone-input";
import { useI18n } from "vue-i18n";

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

withDefaults(defineProps<Props>(), {
  persistentHint: true,
  defaultCountryCode: "CH",
});

const emit = defineEmits<{
  "update:modelValue": [value: string];
}>();

function onUpdate(value: string) {
  emit("update:modelValue", value);
}

function invalidMessageFn({ example }: { label?: string; example?: string }) {
  return t("validation.telephoneInvalid", { example: example || "078 123 45 67" });
}
</script>

<style>
.v-overlay-container .v-overlay__content:has(.v-phone-input__country__icon) .v-list {
  min-width: 500px;
  max-width: 500px;
}
</style>
