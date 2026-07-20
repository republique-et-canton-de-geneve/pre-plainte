<template>
  <div class="masked-date-field">
    <v-text-field
      :model-value="modelValue"
      :label="label"
      :error-messages="errorMessages"
      :hint="hint"
      :persistent-hint="persistentHint"
      :disabled="disabled"
      :name="name"
      :data-field="name"
      :class="fieldClass"
      type="text"
      placeholder="JJ.MM.AAAA"
      variant="outlined"
      inputmode="numeric"
      autocomplete="off"
      @update:model-value="onTextInput"
    >
      <template #prepend-inner>
        <v-menu v-model="menuOpen" :close-on-content-click="false" location="bottom">
          <template #activator="{ props: menuProps }">
            <v-icon
              v-bind="menuProps"
              icon="mdi-calendar"
              class="masked-date-field__icon"
              :aria-label="t('common.ouvrirCalendrier')"
              role="button"
              tabindex="0"
            />
          </template>
          <v-date-picker
            :model-value="pickerValue"
            :max="maxIso"
            :min="minIso"
            color="primary"
            hide-header
            @update:model-value="onPick"
          />
        </v-menu>
      </template>
    </v-text-field>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import { fromIsoDate, toIsoDate } from "@/utils/helpers/dateHelpers";

const ANNEE_MIN_ISO = "1900-01-01";

const props = withDefaults(
  defineProps<{
    modelValue?: string | null;
    label: string;
    errorMessages?: string | string[];
    hint?: string;
    persistentHint?: boolean;
    disabled?: boolean;
    name?: string;
    fieldClass?: string;
  }>(),
  {
    modelValue: "",
    errorMessages: undefined,
    hint: undefined,
    persistentHint: false,
    disabled: false,
    name: undefined,
    fieldClass: undefined,
  },
);

const emit = defineEmits<{
  "update:modelValue": [value: string];
}>();

const { t } = useI18n();
const menuOpen = ref(false);

const maxIso = computed(() => {
  const now = new Date();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  const day = String(now.getDate()).padStart(2, "0");
  return `${now.getFullYear()}-${month}-${day}`;
});

const minIso = ANNEE_MIN_ISO;

const pickerValue = computed(() => toIsoDate(props.modelValue ?? "") || undefined);

const onTextInput = (value: unknown) => {
  emit("update:modelValue", applyDateMaskFromString(String(value ?? "")));
};

const onPick = (value: unknown) => {
  const iso = normalizePickerValue(value);
  if (!iso) {
    return;
  }
  emit("update:modelValue", fromIsoDate(iso));
  menuOpen.value = false;
};

function normalizePickerValue(value: unknown): string | undefined {
  if (typeof value === "string" && value) {
    return value.slice(0, 10);
  }
  if (value instanceof Date && !Number.isNaN(value.getTime())) {
    const month = String(value.getMonth() + 1).padStart(2, "0");
    const day = String(value.getDate()).padStart(2, "0");
    return `${value.getFullYear()}-${month}-${day}`;
  }
  if (Array.isArray(value) && value.length > 0) {
    return normalizePickerValue(value[0]);
  }
  return undefined;
}

function applyDateMaskFromString(raw: string): string {
  const digits = raw.replace(/\D/g, "").slice(0, 8);
  if (digits.length <= 2) {
    return digits;
  }
  if (digits.length <= 4) {
    return `${digits.slice(0, 2)}.${digits.slice(2)}`;
  }
  return `${digits.slice(0, 2)}.${digits.slice(2, 4)}.${digits.slice(4)}`;
}
</script>

<style scoped>
.masked-date-field__icon {
  cursor: pointer;
}
</style>
