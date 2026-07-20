<template>
  <v-text-field
    :model-value="modelValue"
    :label="label"
    :error-messages="errorMessages"
    :hint="hint"
    :persistent-hint="persistentHint"
    :disabled="disabled"
    :name="name"
    :data-field="name"
    type="text"
    placeholder="HH:MM"
    variant="outlined"
    inputmode="numeric"
    autocomplete="off"
    @update:model-value="onTextInput"
  />
</template>

<script setup lang="ts">
const TIME_DIGIT_LIMIT = 4;
const NON_DIGIT_PATTERN = /\D/g;

withDefaults(
  defineProps<{
    modelValue?: string | null;
    label: string;
    errorMessages?: string | string[] | null;
    hint?: string | null;
    persistentHint?: boolean;
    disabled?: boolean;
    name?: string | null;
  }>(),
  {
    modelValue: "",
    errorMessages: null,
    hint: null,
    persistentHint: false,
    disabled: false,
    name: null,
  },
);

const emit = defineEmits<{
  "update:modelValue": [value: string];
}>();

const onTextInput = (value: unknown) => {
  emit("update:modelValue", applyTimeMaskFromString(String(value ?? "")));
};

function applyTimeMaskFromString(raw: string): string {
  const digits = raw.replaceAll(NON_DIGIT_PATTERN, "").slice(0, TIME_DIGIT_LIMIT);
  if (digits.length <= 2) {
    return digits;
  }
  return `${digits.slice(0, 2)}:${digits.slice(2)}`;
}
</script>
