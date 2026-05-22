<template>
  <v-row>
    <v-col cols="12" md="6">
      <AccessibleVSelect
        id="date-souhaitee"
        v-model="dateSouhaiteeSelection"
        :error-messages="dateSouhaiteeError"
        :items="dateOptions"
        item-title="label"
        item-value="value"
        variant="outlined"
        prepend-inner-icon="mdi-calendar"
        :label="t('rendezVous.dateSouhaitee')"
        :disabled="esiriusStore.loading || !datesDisponibles.length"
        clearable
      />
    </v-col>

    <v-col cols="12" md="6">
      <div class="css-off-fallback">
        <label for="poste-native" class="field-label">
          {{ t("rendezVous.lieu") }}
        </label>
        <select
          id="poste-native"
          :disabled="esiriusStore.loading || servicesDisponibles.length === 0"
          :value="poste?.key ?? ''"
          @change="onPosteNativeChange"
        >
          <option value="">{{ t("rendezVous.tousLesPostes") }}</option>
          <option v-for="s in servicesDisponibles" :key="String(s.key)" :value="String(s.key)">
            {{ s.name }}
          </option>
        </select>
      </div>

      <accessible-v-select
        :label="t('rendezVous.lieu')"
        :items="servicesAvecTous"
        item-title="name"
        item-value="key"
        v-model="poste"
        variant="outlined"
        class="mb-4"
        return-object
        :loading="esiriusStore.loading"
        :disabled="esiriusStore.loading || servicesDisponibles.length === 0"
      />
    </v-col>
  </v-row>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { useEsiriusStore } from "@/stores/useEsiriusStore";
import AccessibleVSelect from "../accessibility/AccessibleVSelect.vue";
import { useField } from "vee-validate";

const RENDEZ_VOUS_DATE_WINDOW_DAYS = 15;

interface Props {
  poste: any;
  servicesDisponibles: any[];
  datesDisponibles: string[];
}

interface Emits {
  (e: "update:dateSouhaitee", value: string): void;
  (e: "update:poste", value: any): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const { t, locale } = useI18n();
const esiriusStore = useEsiriusStore();

const traductionTousLesPostes = "rendezVous.tousLesPostes";

const { value: dateSouhaitee, errorMessage: dateSouhaiteeError } = useField<string>("dateSouhaitee");

const dateSouhaiteeSelection = computed({
  get: () => dateSouhaitee.value || "",
  set: (value: string | null) => {
    const nextValue = value || "";
    dateSouhaitee.value = nextValue;
    emit("update:dateSouhaitee", nextValue);
  },
});

const dateOptions = computed(() => {
  const formatter = new Intl.DateTimeFormat(locale.value, {
    weekday: "long",
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
  });
  const today = new Date();
  today.setHours(0, 0, 0, 0);

  return Array.from({ length: RENDEZ_VOUS_DATE_WINDOW_DAYS }, (_, index) => {
    const date = new Date(today);
    date.setDate(today.getDate() + index);
    return {
      label: formatter.format(date),
      value: formatIsoDate(date),
    };
  });
});

const servicesAvecTous = computed(() => {
  if (!props.servicesDisponibles?.length) {
    return [];
  }
  return [{ key: null, name: t(traductionTousLesPostes) }, ...props.servicesDisponibles];
});

const poste = computed({
  get: () => props.poste,
  set: (value: any) => {
    if (!value || value.key === null) {
      emit("update:poste", null);
    } else {
      emit("update:poste", value);
    }
  },
});

const onPosteNativeChange = (e: Event) => {
  const value = (e.target as HTMLSelectElement).value;

  if (!value) {
    poste.value = { key: null, name: t(traductionTousLesPostes) };
    return;
  }

  const selected = props.servicesDisponibles.find(s => String(s.key) === value);
  poste.value = selected ?? { key: null, name: t(traductionTousLesPostes) };
};

function formatIsoDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}
</script>

<style scoped>
.css-off-fallback {
  display: none;
}

.field-label {
  display: inline-block;
  margin-bottom: 6px;
}
</style>
