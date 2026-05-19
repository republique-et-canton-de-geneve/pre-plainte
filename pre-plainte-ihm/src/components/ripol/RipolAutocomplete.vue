<template>
  <v-autocomplete
    v-model="selectedValue"
    v-model:search="searchQuery"
    :items="items"
    :loading="loading"
    :label="label"
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
    :no-filter="!preload"
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

const { t } = useI18n();
const { startLoading, stopLoading } = useRipolLoading();

interface Props {
  modelValue: RipolSelection | null;
  label: string;
  fetchFn: (search?: string) => Promise<Ripol[]>;
  errorMessages?: string | string[];
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
const loading = ref(false);
const initialLoadDone = ref(false);
const searchQuery = ref("");
let debounceTimeout: ReturnType<typeof setTimeout> | null = null;

const getDisplayLabel = (item: RipolSelection) => {
  const raw = props.displayLabel ? props.displayLabel(item) : item.label;
  return typeof raw === "string" ? raw.trim() : raw;
};


const displayedErrors = computed(() => {
  if (props.preload && !initialLoadDone.value) {
    return undefined;
  }
  return props.errorMessages;
});

const noDataText = computed(() => {
  if (loading.value) {
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

const fetchItems = async (search?: string) => {
  loading.value = true;
  startLoading();
  try {
    const results = await props.fetchFn(search);
    const newItems = results.map(ripol => mapRipolToSelection(ripol));

    if (props.autoSelectWhenSingleResult && !props.modelValue && newItems.length === 1) {
      emit("update:modelValue", newItems[0]);
    }

    if (props.modelValue && !newItems.some(item => item.code === props.modelValue?.code)) {
      newItems.unshift(props.modelValue);
    }

    items.value = newItems;
  } catch (error) {
    console.error("Erreur lors de la recherche RIPOL:", error);
    items.value = props.modelValue ? [props.modelValue] : [];
  } finally {
    loading.value = false;
    stopLoading();
    if (props.preload) {
      initialLoadDone.value = true;
    }
  }
};

watch(searchQuery, search => {
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

  // Éviter de relancer la recherche si le texte correspond à l'élément sélectionné
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


