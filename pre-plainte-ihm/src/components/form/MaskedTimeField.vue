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
withDefaults(
  defineProps<{
    modelValue?: string | null;
    label: string;
    errorMessages?: string | string[];
    hint?: string;
    persistentHint?: boolean;
    disabled?: boolean;
    name?: string;
  }>(),
  {
    modelValue: "",
    errorMessages: undefined,
    hint: undefined,
    persistentHint: false,
    disabled: false,
    name: undefined,
  },
);

const emit = defineEmits<{
  "update:modelValue": [value: string];
}>();

const onTextInput = (value: unknown) => {
  emit("update:modelValue", applyTimeMaskFromString(String(value ?? "")));
};

function applyTimeMaskFromString(raw: string): string {
  const digits = raw.replace(/\D/g, "").slice(0, 4);
  if (digits.length <= 2) {
    return digits;
  }
  return `${digits.slice(0, 2)}:${digits.slice(2)}`;
}
</script>
