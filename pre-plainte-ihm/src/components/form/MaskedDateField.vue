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
              @keydown.enter.prevent="menuProps.onClick?.($event)"
              @keydown.space.prevent="menuProps.onClick?.($event)"
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
const DATE_DIGIT_LIMIT = 8;
const DATE_PART_PAD_LENGTH = 2;
const DATE_PART_PAD_CHAR = "0";
const DAY_DIGITS_END = DATE_PART_PAD_LENGTH;
const MONTH_DIGITS_END = 4;
const ISO_DATE_LENGTH = 10;
const NON_DIGIT_PATTERN = /\D/g;
const DATE_SEPARATOR = ".";
const OPTIONAL_PROP_DEFAULT = null;

const props = withDefaults(
  defineProps<{
    modelValue?: string | null;
    label: string;
    errorMessages?: string | string[] | null;
    hint?: string | null;
    persistentHint?: boolean;
    disabled?: boolean;
    name?: string | null;
    fieldClass?: string | null;
  }>(),
  {
    modelValue: "",
    errorMessages: OPTIONAL_PROP_DEFAULT,
    hint: OPTIONAL_PROP_DEFAULT,
    persistentHint: false,
    disabled: false,
    name: OPTIONAL_PROP_DEFAULT,
    fieldClass: OPTIONAL_PROP_DEFAULT,
  },
);

const emit = defineEmits<{
  "update:modelValue": [value: string];
}>();

const { t } = useI18n();
const menuOpen = ref(false);

function padDatePart(value: number): string {
  return String(value).padStart(DATE_PART_PAD_LENGTH, DATE_PART_PAD_CHAR);
}

const maxIso = computed(() => {
  const now = new Date();
  return `${now.getFullYear()}-${padDatePart(now.getMonth() + 1)}-${padDatePart(now.getDate())}`;
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
    return value.slice(0, ISO_DATE_LENGTH);
  }
  if (value instanceof Date && !Number.isNaN(value.getTime())) {
    return `${value.getFullYear()}-${padDatePart(value.getMonth() + 1)}-${padDatePart(value.getDate())}`;
  }
  if (Array.isArray(value) && value.length > 0) {
    return normalizePickerValue(value[0]);
  }
  return undefined;
}

function applyDateMaskFromString(raw: string): string {
  const digits = raw.replaceAll(NON_DIGIT_PATTERN, "").slice(0, DATE_DIGIT_LIMIT);
  if (digits.length <= DAY_DIGITS_END) {
    return digits;
  }
  if (digits.length <= MONTH_DIGITS_END) {
    return `${digits.slice(0, DAY_DIGITS_END)}${DATE_SEPARATOR}${digits.slice(DAY_DIGITS_END)}`;
  }
  return `${digits.slice(0, DAY_DIGITS_END)}${DATE_SEPARATOR}${digits.slice(DAY_DIGITS_END, MONTH_DIGITS_END)}${DATE_SEPARATOR}${digits.slice(MONTH_DIGITS_END)}`;
}
</script>

<style scoped>
.masked-date-field__icon {
  cursor: pointer;
}
</style>
