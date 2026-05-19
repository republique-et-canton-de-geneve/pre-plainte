<template>
  <v-row>
    <v-col cols="12" md="6">
      <v-text-field
        id="date-souhaitee"
        v-model="dateSouhaitee"
        :error-messages="dateSouhaiteeError"
        type="text"
        variant="outlined"
        prepend-inner-icon="mdi-calendar"
        :label="t('rendezVous.dateSouhaitee')"
        placeholder="JJ.MM.AAAA"
        :disabled="!datesDisponibles.length"
        @input="onDateSouhaiteeInput"
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
import { applyDateMask } from "@/utils/helpers/dateHelpers.ts";
import { useField } from "vee-validate";

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

const { t } = useI18n();
const esiriusStore = useEsiriusStore();

const traductionTousLesPostes = "rendezVous.tousLesPostes";

const { value: dateSouhaitee, errorMessage: dateSouhaiteeError } = useField<string>("dateSouhaitee");

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

function onDateSouhaiteeInput(e: InputEvent) {
  applyDateMask(e, dateSouhaitee);
  emit("update:dateSouhaitee", dateSouhaitee.value);
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
