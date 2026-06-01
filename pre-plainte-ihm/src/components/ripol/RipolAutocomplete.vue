<template>
  <v-autocomplete
    v-model="selectedValue"
    v-model:search="searchQuery"
    :items="items"
    :loading="isLoading"
    :label="displayLabel"
    :error-messages="displayedErrors"
    :disabled="disabled"
    :hint="hint"
    :persistent-hint="persistentHint"
    :clearable="clearable"
    :rules="rules"
    :item-title="getDisplayLabel"
    item-value="code"
    return-object
    variant="outlined"
    :no-filter="disableVuetifyFilter"
    :no-data-text="noDataText"
    :value-comparator="compareByCode"
  >
    <template #item="{ props: itemProps, item }">
      <v-list-item v-bind="itemProps" :title="getDisplayLabel(item.raw)" :subtitle="showCode ? item.raw.code : undefined" />
    </template>

    <template #selection="{ item }">
      {{ getDisplayLabel(item.raw) }}
    </template>
  </v-autocomplete>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from "vue";
import { useI18n } from "vue-i18n";
import type { Ripol, RipolSelection } from "@/types/ripol.interface";
import { useRipolLoading } from "@/composables/useRipolLoading";
import { requiredLabel } from "@/utils/helpers/labelHelpers";
import { filterRipolSelections } from "@/utils/helpers/ripolFilterHelpers";

const { t } = useI18n();
const { startLoading, stopLoading } = useRipolLoading();

const LOCAL_FILTER_THRESHOLD = 300;
const MAX_VISIBLE_ITEMS = 100;

interface Props {
  modelValue: RipolSelection | null;
  label: string;
  fetchFn: (search?: string) => Promise<Ripol[]>;
  errorMessages?: string | string[];
  loading?: boolean;
  disabled?: boolean;
  hint?: string;
  persistentHint?: boolean;
  clearable?: boolean;
  rules?: ((v: RipolSelection | null) => boolean | string)[];
  preload?: boolean;
  showCode?: boolean;
  minSearchLength?: number;
  debounceMs?: number;
  displayLabel?: (item: RipolSelection) => string;
  autoSelectWhenSingleResult?: boolean;
  required?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  clearable: true,
  preload: false,
  showCode: false,
  minSearchLength: 2,
  debounceMs: 300,
  autoSelectWhenSingleResult: false,
});

const emit = defineEmits<{
  "update:modelValue": [value: RipolSelection | null];
}>();

const items = ref<RipolSelection[]>(props.modelValue ? [props.modelValue] : []);
const allItems = ref<RipolSelection[]>([]);
const useLocalFilter = ref(false);
const fetchLoading = ref(false);
const isLoading = computed(() => props.loading === true || fetchLoading.value);
const initialLoadDone = ref(false);
const searchQuery = ref("");
const disableVuetifyFilter = computed(() => useLocalFilter.value || !props.preload);
let debounceTimeout: ReturnType<typeof setTimeout> | null = null;
let fetchGeneration = 0;

const getDisplayLabel = (item: RipolSelection) => {
  const raw = props.displayLabel ? props.displayLabel(item) : item.label;
  return typeof raw === "string" ? raw.trim() : raw;
};

const displayLabel = computed(() => (props.required ? requiredLabel(props.label) : props.label));

const displayedErrors = computed(() => {
  if (props.preload && !initialLoadDone.value) {
    return undefined;
  }
  return props.errorMessages;
});

const noDataText = computed(() => {
  if (isLoading.value) {
    return t("ripol.chargementEnCours");
  }
  if (props.preload) {
    return t("ripol.aucunResultat");
  }
  return searchQuery.value.length < props.minSearchLength
    ? t("ripol.tapezPourRechercher", { min: props.minSearchLength })
    : t("ripol.aucunResultat");
});

const selectedValue = computed({
  get: () => props.modelValue,
  set: (value: RipolSelection | null) => {
    emit("update:modelValue", value);
  },
});

const mapRipolToSelection = (ripol: Ripol): RipolSelection => {
  const labelFr = ripol.labelFr?.trim() ?? "";
  const labelDe = ripol.labelDe?.trim() ?? "";
  const label = resolveRipolLabel(labelFr, labelDe, ripol.code);
  return {
    code: ripol.code,
    label,
  };
};

function resolveRipolLabel(labelFr: string, labelDe: string, fallbackCode: string): string {
  if (labelFr.length > 0) {
    return labelFr.trim();
  }
  if (labelDe.length > 0) {
    return labelDe.trim();
  }
  return String(fallbackCode).trim();
}

const compareByCode = (currentValue: RipolSelection | null, itemValue: RipolSelection | null): boolean => {
  if (!currentValue && !itemValue) {
    return true;
  }
  if (!currentValue || !itemValue) {
    return false;
  }
  return currentValue.code === itemValue.code;
};

const withSelectedValue = (visible: RipolSelection[]): RipolSelection[] => {
  if (props.modelValue && !visible.some(item => item.code === props.modelValue?.code)) {
    return [props.modelValue, ...visible];
  }
  return visible;
};

const applyLocalFilter = (search?: string) => {
  const filtered = filterRipolSelections(allItems.value, search);
  items.value = withSelectedValue(filtered.slice(0, MAX_VISIBLE_ITEMS));
};

const setItemsFromFetch = (newItems: RipolSelection[]) => {
  if (props.autoSelectWhenSingleResult && !props.modelValue && newItems.length === 1) {
    emit("update:modelValue", newItems[0]);
  }

  if (newItems.length > LOCAL_FILTER_THRESHOLD) {
    useLocalFilter.value = true;
    allItems.value = newItems;
    applyLocalFilter(searchQuery.value);
    return;
  }

  useLocalFilter.value = false;
  allItems.value = [];
  items.value = withSelectedValue(newItems);
};

const fetchItems = async (search?: string) => {
  const generation = ++fetchGeneration;
  fetchLoading.value = true;
  startLoading();
  try {
    const results = await props.fetchFn(search);
    if (generation !== fetchGeneration) {
      return;
    }
    setItemsFromFetch(results.map(ripol => mapRipolToSelection(ripol)));
  } catch (error) {
    if (generation === fetchGeneration) {
      console.error("Erreur lors de la recherche RIPOL:", error);
      useLocalFilter.value = false;
      allItems.value = [];
      items.value = props.modelValue ? [props.modelValue] : [];
    }
  } finally {
    if (generation !== fetchGeneration) {
      return;
    }
    fetchLoading.value = false;
    stopLoading();
    if (props.preload) {
      initialLoadDone.value = true;
    }
  }
};

watch(searchQuery, search => {
  if (useLocalFilter.value) {
    applyLocalFilter(search);
    return;
  }

  if (props.preload) {
    return;
  }

  if (debounceTimeout) {
    clearTimeout(debounceTimeout);
  }

  if (!search || search.length < props.minSearchLength) {
    if (!props.modelValue) {
      items.value = [];
    }
    return;
  }

  if (props.modelValue && search.trim() === (props.modelValue.label ?? "").trim()) {
    return;
  }

  debounceTimeout = setTimeout(() => {
    fetchItems(search);
  }, props.debounceMs);
});

watch(
  () => props.modelValue,
  newValue => {
    if (newValue && !items.value.some(item => item.code === newValue.code)) {
      items.value = [newValue, ...items.value];
    }
  },
  { immediate: true }
);

onMounted(() => {
  if (props.modelValue && !items.value.some(item => item.code === props.modelValue?.code)) {
    items.value = [props.modelValue];
  }

  if (props.preload) {
    fetchItems();
  }
});
</script>
