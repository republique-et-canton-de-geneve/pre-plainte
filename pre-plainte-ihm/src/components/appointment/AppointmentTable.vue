<template>
  <div class="mt-5">
    <h3 id="rdv-table-title" class="pre-plainte-main-card-title text-h4 text-md-h5" style="font-weight: 500">
      {{ t("rendezVous.tableTitre") }}
    </h3>
    <p class="text-body-4 mt-1 mb-0">
      {{ t("rendezVous.rdvSelectionne") }}
    </p>
  </div>

  <v-radio-group v-model="creneauPrefere" color="primary" class="mt-5">
    <v-table class="d-none d-md-table" aria-labelledby="rdv-table-title">
      <caption class="sr-only">
        {{
          t("rendezVous.tableTitre")
        }}
      </caption>

      <thead>
        <tr>
          <th scope="col">
            <span class="sr-only">{{ t("rendezVous.tableChoix") }}</span>
          </th>
          <th scope="col">{{ t("rendezVous.tableDate") }}</th>
          <th scope="col">{{ t("rendezVous.tableDebut") }}</th>
          <th scope="col">{{ t("rendezVous.tableFin") }}</th>
          <th scope="col">{{ t("rendezVous.tableLieu") }}</th>
        </tr>
      </thead>

      <tbody>
        <tr v-if="esiriusStore.loading">
          <td colspan="5" class="text-center py-6">
            <v-progress-circular indeterminate color="primary" size="32" />
            <div class="mt-2 text-body-2">{{ t("common.donneeEnCoursDeChargement") }}</div>
          </td>
        </tr>
        <tr v-else-if="esiriusStore.errorMessage">
          <td colspan="5" class="text-center py-6">
            <v-alert type="error" variant="text" density="compact">
              {{ t("common.error.serverDown") }}
            </v-alert>
          </td>
        </tr>
        <tr v-else-if="!creneauxPagines.length">
          <td colspan="5" class="text-center py-6">
            <v-alert type="info" variant="text" density="compact">
              {{ t("common.error.creneauIndisponible") }}
            </v-alert>
          </td>
        </tr>
        <tr v-else v-for="(creneau, index) in creneauxPagines" :key="index">
          <td><v-radio :value="index" hide-details :aria-label="getCreneauRadioAriaLabel(creneau)" /></td>
          <td>{{ formatDate(creneau.beginDateTime) }}</td>
          <td>{{ formatHeure(creneau.beginDateTime) }}</td>
          <td>{{ formatHeure(creneau.endDateTime) }}</td>
          <td>{{ formatLieu(creneau.resource?.name) }}</td>
        </tr>
      </tbody>
    </v-table>

    <v-sheet v-if="mobile" class="d-md-none" :class="['mx-n2', 'px-2']" color="transparent">
      <v-sheet v-if="esiriusStore.loading" class="text-center py-6" color="transparent">
        <v-progress-circular indeterminate color="primary" size="32" />
        <div class="mt-2 text-body-2">{{ t("common.donneeEnCoursDeChargement") }}</div>
      </v-sheet>

      <v-alert v-else-if="esiriusStore.errorMessage" type="error" variant="text" density="comfortable" class="my-4">
        {{ t("common.error.serverDown") }}
      </v-alert>

      <v-alert v-else-if="!creneauxPagines.length" type="info" variant="text" density="comfortable" class="my-4">
        {{ t("common.error.creneauIndisponible") }}
      </v-alert>

      <v-list v-else class="pa-0 bg-transparent" density="comfortable">
        <v-list-item v-for="(creneau, index) in creneauxPagines" :key="index" class="pa-0 mb-2">
          <v-card
            variant="outlined"
            rounded="lg"
            class="w-100 border-grey-lighten-1"
            bg-color="primary"
            :bg-opacity="0.04"
            elevation="0"
            @click="creneauPrefere = index"
          >
            <v-card-item class="pa-2">
              <div class="d-flex align-center">
                <v-radio
                  :value="index"
                  hide-details
                  @click.stop
                  class="me-3"
                  :aria-label="getCreneauRadioAriaLabel(creneau)"
                />
                <div class="flex-grow-1">
                  <div class="text-body-1 font-weight-medium mb-1 text-primary">
                    {{ formatHeure(creneau.beginDateTime) }} - {{ formatHeure(creneau.endDateTime) }}
                  </div>
                  <div class="text-body-2 text-medium-emphasis mb-1">
                    {{ formatDate(creneau.beginDateTime) }}
                  </div>
                  <div class="text-body-2 text-medium-emphasis">
                    {{ formatLieu(creneau.resource?.name) }}
                  </div>
                </div>
              </div>
            </v-card-item>
          </v-card>
        </v-list-item>
      </v-list>
    </v-sheet>
  </v-radio-group>

  <div class="d-flex justify-end mt-4">
    <v-pagination
      v-model="page"
      :length="totalPages"
      :total-visible="mobile ? 3 : 4"
      :density="mobile ? 'compact' : 'comfortable'"
      :show-first-last-page="!mobile"
      prev-icon="mdi-chevron-left"
      next-icon="mdi-chevron-right"
      first-icon="mdi-page-first"
      last-icon="mdi-page-last"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { useDisplay } from "vuetify";
import { useEsiriusStore } from "@/stores/useEsiriusStore";

const DATE_MIN_LENGTH = 8;
const DATETIME_MIN_LENGTH = 10;

const YEAR_START_INDEX = 0;
const YEAR_END_INDEX = 4;

const MONTH_START_INDEX = 4;
const MONTH_END_INDEX = 6;

const DAY_START_INDEX = 6;
const DAY_END_INDEX = 8;

const TIME_START_INDEX = 9;

interface Props {
  creneauxPagines: any[];
  totalPages: number;
  page: number;
  creneauPrefere: number | null;
}

interface Emits {
  (e: "update:page", value: number): void;
  (e: "update:creneauPrefere", value: number | null): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const { t } = useI18n();
const { mobile } = useDisplay();
const esiriusStore = useEsiriusStore();

const page = computed({
  get: () => props.page,
  set: (value: number) => emit("update:page", value),
});

const creneauPrefere = computed({
  get: () => props.creneauPrefere,
  set: (value: number | null) => emit("update:creneauPrefere", value),
});

const formatDate = (dateTime?: string) => {
  if (!dateTime) {
    return "-";
  }
  if (dateTime.length >= DATE_MIN_LENGTH && !dateTime.includes("-")) {
    const year = dateTime.slice(YEAR_START_INDEX, YEAR_END_INDEX);
    const month = dateTime.slice(MONTH_START_INDEX, MONTH_END_INDEX);
    const day = dateTime.slice(DAY_START_INDEX, DAY_END_INDEX);
    return `${day}.${month}.${year}`;
  }
  if (dateTime.includes("-")) {
    const [year, month, day] = dateTime.split("-");
    return `${day}.${month}.${year}`;
  }
  return dateTime;
};

const formatHeure = (dateTime?: string) => {
  if (!dateTime || dateTime.length < DATETIME_MIN_LENGTH) {
    return "-";
  }
  const timePart = dateTime.substring(TIME_START_INDEX).trim();
  return timePart.replace(":", "h");
};

const formatLieu = (lieu?: string) => {
  if (!lieu) {
    return "-";
  }
  return lieu.split(" - ")[0].trim();
};

const getCreneauRadioAriaLabel = (creneau: any) => {
  const date = formatDate(creneau?.beginDateTime);
  const debut = formatHeure(creneau?.beginDateTime);
  const fin = formatHeure(creneau?.endDateTime);
  const lieu = formatLieu(creneau?.resource?.name);

  return t("rendezVous.ariaSelectionCreneau", {
    date,
    debut,
    fin,
    lieu,
  });
};
</script>

<style scoped>
.sr-only {
  position: absolute !important;
  width: 1px !important;
  height: 1px !important;
  padding: 0 !important;
  margin: -1px !important;
  overflow: hidden !important;
  clip: rect(0, 0, 0, 0) !important;
  white-space: nowrap !important;
  border: 0 !important;
}

@media (max-width: 959px) {
  .border-grey-lighten-1 {
    border-color: rgba(0, 0, 0, 0.12) !important;
  }
}
</style>
