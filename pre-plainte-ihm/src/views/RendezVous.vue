<template>
  <v-form @submit.prevent="onSubmit">
    <h1 class="mb-4 text-h1 text-md-h2 d-none d-md-block">{{ t("steps.prendreRendezVous") }}</h1>
    <v-sheet class="pa-2 pa-md-8" rounded="lg" elevation="1">
      <h2 class="pre-plainte-main-card-title mb-4 mb-md-5 text-h4 text-md-h4 title-mobile">
        {{ t("rendezVous.selectionPoste") }}
      </h2>

      <v-alert
        v-if="rendezVousWarning"
        :type="rendezVousWarning.type"
        class="mb-6"
        density="comfortable"
        :icon="mobile ? false : undefined"
      >
        {{ t(rendezVousWarning.messageKey) }}
      </v-alert>

      <AppointmentFilters
        v-model:dateSouhaitee="dateSouhaitee"
        v-model:poste="poste"
        :services-disponibles="servicesDisponibles"
        :dates-disponibles="datesDisponibles"
      />

      <LeafletMap
        aria-hidden="true"
        v-model:selected-service="poste"
        :services="servicesDisponibles"
        :height="400"
        @suggest-nearest="onSuggestNearest"
      />

      <div ref="creneauxSection">
        <AppointmentTable
          v-model:page="page"
          v-model:creneau-prefere="creneauPrefere"
          :creneaux-pagines="creneauxPagines"
          :total-pages="totalPages"
        />
      </div>

      <div class="d-md-none mt-4">
        <div class="pre-plainte-mobile-step-actions d-flex flex-column gap-4 mb-2">
          <v-btn variant="outlined" color="primary" class="w-100" data-cy="precedent-rendez-vous" @click="$emit('cancel')">
            {{ t("common.precedent") }}
          </v-btn>
          <v-btn type="submit" variant="flat" color="primary" class="w-100" data-cy="continuer-rendez-vous">
            {{ t("common.continuer") }}
          </v-btn>
        </div>
        <div class="d-flex justify-center">
          <v-btn variant="plain" color="primary" class="pa-0" @click="$emit('save')">
            {{ t("common.sauvegarder") }}
          </v-btn>
        </div>
      </div>
    </v-sheet>

    <div class="d-md-none mt-4 d-flex flex-column align-center gap-2">
      <ExitActionsForm :is-mobile="true" />
    </div>

    <v-row class="mt-4 d-none d-md-flex" align="center">
      <v-col cols="12" md="auto" class="d-flex">
        <v-btn variant="plain" color="primary" @click="$emit('save')">
          {{ t("common.sauvegarder") }}
        </v-btn>
      </v-col>
      <v-spacer />
      <v-col cols="12" md="auto" class="d-flex justify-end">
        <v-btn variant="outlined" color="primary" class="me-4" data-cy="precedent-rendez-vous" @click="$emit('cancel')">
          {{ t("common.precedent") }}
        </v-btn>
        <v-btn type="submit" variant="flat" color="primary" data-cy="continuer-rendez-vous">
          {{ t("common.poursuivre") }}
        </v-btn>
      </v-col>
    </v-row>

    <v-alert
      v-model="showCreneauError"
      type="error"
      class="mt-6"
      density="comfortable"
      :icon="mobile ? false : undefined"
      closable
    >
      {{ creneauErrorMessage }}
    </v-alert>
  </v-form>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useField, useForm } from "vee-validate";
import { useEsiriusStore } from "@/stores/useEsiriusStore";
import { useCreatePrePlainteStore } from "@/stores/createPrePlainteStore";
import LeafletMap from "@/components/map/LeafletMap.vue";
import AppointmentFilters from "@/components/appointment/AppointmentFilters.vue";
import AppointmentTable from "@/components/appointment/AppointmentTable.vue";
import { toIsoDate } from "@/utils/helpers/dateHelpers.ts";
import { rendezvousInfoSchema } from "@/schemas/rdv-schema.ts";
import { toTypedSchema } from "@vee-validate/zod";
import { useFormErrorScroll } from "@/composables/useFormErrorScroll.ts";
import { useDisplay } from "vuetify/framework";
import ExitActionsForm from "@/components/actions/ExitActionsForm.vue";
import { hasVehiculeVoleAvecPlaque } from "@/utils/helpers/volObjetVolHelpers.ts";
import { DAY_END, DAY_START, MONTH_END, MONTH_START, TIME_START, YEAR_END, YEAR_START } from "@/constants/constant.ts";
import {
  filterCompatibleCreneaux,
  filterCreneauxByPosteAndDate,
  filterServicesByIncident,
  getRendezVousWarning,
} from "@/utils/workflows/rendez-vous-workflow";

const RENDEZ_VOUS_DATE_WINDOW_DAYS = 15;
const SCROLL_AFTER_SELECT_DELAY_MS = 300;

const { t, locale } = useI18n();
const { mobile } = useDisplay();
const store = useCreatePrePlainteStore();
const esiriusStore = useEsiriusStore();
const { scrollToFirstValidationError } = useFormErrorScroll();
const emit = defineEmits(["save", "cancel", "continue"]);

const { value: dateSouhaitee } = useField<string>("dateSouhaitee");

const poste = ref<any | null>(null);
const creneauPrefere = ref<number | null>(null);
const showCreneauError = ref(false);
const creneauErrorMessage = ref("");
const page = ref(1);
const creneauxSection = ref<HTMLElement | null>(null);
const itemsParPage = 5;

onMounted(async () => {
  await esiriusStore.loadServicesForSite("PPEL");
  await esiriusStore.loadAllAvailabilitiesForPPEL();
});

const servicesDisponibles = computed(() =>
  filterServicesByIncident(esiriusStore.services, esiriusStore.allAvailabilities, store.userFormData.typeIncident)
);

const rendezVousWarning = computed(() => getRendezVousWarning(store.userFormData, aucunCreneauVehiculeAvecPlaque.value));

const isVehiculeVoleAvecPlaque = computed(() => hasVehiculeVoleAvecPlaque(store.userFormData));

const aucunCreneauVehiculeAvecPlaque = computed(() =>
  isVehiculeVoleAvecPlaque.value &&
  !esiriusStore.loading &&
  !esiriusStore.errorMessage &&
  creneauxCompatiblesIncident.value.length === 0
);

const availabilitiesByPoste = computed(() => {
  if (!poste.value) {
    return esiriusStore.allAvailabilities;
  }

  return esiriusStore.allAvailabilities.filter(
    (a: any) => a.serviceId === poste.value.key || a.serviceName?.includes(poste.value.name),
  );
});

const creneauxCompatiblesIncident = computed(() =>
  filterCompatibleCreneaux(esiriusStore.allAvailabilities, esiriusStore.services, store.userFormData)
);

const creneauxFiltres = computed(() => {
  return filterCreneauxByPosteAndDate(
    availabilitiesByPoste.value,
    creneauxCompatiblesIncident.value,
    poste.value,
    dateSouhaitee.value,
  );
});

const datesDisponibles = computed(() => {
  const validDates = creneauxCompatiblesIncident.value
    .filter((creneau: any) => !poste.value || creneau.serviceId === poste.value.key)
    .filter((a: any) => a?.beginDateTime)
    .map((a: any) => a.beginDateTime.slice(YEAR_START, DAY_END));
  return Array.from(new Set(validDates))
    .sort()
    .map(d => `${d.slice(YEAR_START, YEAR_END)}-${d.slice(MONTH_START, MONTH_END)}-${d.slice(DAY_START, DAY_END)}`);
});

const premiereDateDispo = computed(() => formatIsoDate(addDays(new Date(), 0)));

const derniereDateDispo = computed(() => formatIsoDate(addDays(new Date(), RENDEZ_VOUS_DATE_WINDOW_DAYS - 1)));

const validationSchema = computed(() =>
  toTypedSchema(rendezvousInfoSchema(t, premiereDateDispo.value, derniereDateDispo.value)),
);

const { handleSubmit, validate } = useForm({
  validationSchema,
});

function addDays(date: Date, days: number): Date {
  const next = new Date(date);
  next.setHours(0, 0, 0, 0);
  next.setDate(next.getDate() + days);
  return next;
}

function formatIsoDate(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

const totalPages = computed(() => Math.ceil(creneauxFiltres.value.length / itemsParPage));
const creneauxPagines = computed(() => {
  const start = (page.value - 1) * itemsParPage;
  return creneauxFiltres.value.slice(start, start + itemsParPage);
});

watch(
  () => store.userFormData.typeIncident,
  () => {
    const services = servicesDisponibles.value;
    if (!services.some(s => s.key === poste.value?.key)) {
      poste.value = null;
    }
  },
  { immediate: true },
);

watch([() => store.userFormData.typeIncident, poste], () => {
  page.value = 1;
  creneauPrefere.value = null;
});

watch(poste, () => {
  page.value = 1;
  creneauPrefere.value = null;
});

watch(dateSouhaitee, () => {
  page.value = 1;
  creneauPrefere.value = null;
});

watch(datesDisponibles, dates => {
  const selectedDate = toIsoDate(dateSouhaitee.value) ?? dateSouhaitee.value;
  if (selectedDate && !dates.includes(selectedDate)) {
    dateSouhaitee.value = "";
  }
});

watch(
  [() => poste.value?.key, dateSouhaitee, () => creneauxFiltres.value.length],
  ([posteKey, selectedDate, nombreCreneaux], _, onCleanup) => {
    const selectedIsoDate = toIsoDate(selectedDate) ?? selectedDate;
    if (!posteKey || !selectedIsoDate || nombreCreneaux === 0 || !datesDisponibles.value.includes(selectedIsoDate)) {
      return;
    }

    let cancelled = false;
    const scrollTimeout = globalThis.setTimeout(() => {
      void nextTick(() => {
        if (!cancelled) {
          creneauxSection.value?.scrollIntoView({ behavior: "smooth", block: "start" });
        }
      });
    }, SCROLL_AFTER_SELECT_DELAY_MS);

    onCleanup(() => {
      cancelled = true;
      globalThis.clearTimeout(scrollTimeout);
    });
  },
  { flush: "post" },
);

watch(locale, () => {
  validate();
});

const onSubmit = handleSubmit(
  () => {
    if (isVehiculeVoleAvecPlaque.value && creneauPrefere.value === null) {
      showCreneauError.value = false;
      store.setUserFormData({
        ...store.userFormData,
        dateSouhaitee: "",
        creneauPrefere: "",
        selectedCreneau: null,
        codeRdv: "",
      });
      emit("continue");
      return;
    }

    if (creneauPrefere.value === null) {
      showCreneauError.value = true;
      creneauErrorMessage.value = t("rendezVous.creneauNonSelectionne");
      return;
    }
    const c = creneauxPagines.value[creneauPrefere.value];
    if (!c) {
      showCreneauError.value = true;
      creneauErrorMessage.value = t("rendezVous.creneauNonDisponible");
      return;
    }

    showCreneauError.value = false;

    const rawDate = `${c.beginDateTime.slice(YEAR_START, YEAR_END)}-${c.beginDateTime.slice(MONTH_START, MONTH_END)}-${c.beginDateTime.slice(DAY_START, DAY_END)}`;
    const heureDebut = c.beginDateTime.substring(TIME_START).trim();
    const heureFin = c.endDateTime.substring(TIME_START).trim();
    const dateAffichee = `${rawDate.split("-").reverse().join(".")}`;

    const selectedCreneau = {
      id: Date.now().toString(),
      date: rawDate,
      dateAffichee,
      heureDebut,
      heureFin,
      lieu: c.resource?.name || "-",
      serviceId: c.serviceId,
      siteCode: c.siteCode,
      resource: c.resource,
      beginDateTime: c.beginDateTime,
      endDateTime: c.endDateTime,
    };

    store.setUserFormData({
      ...store.userFormData,
      dateSouhaitee: rawDate,
      creneauPrefere: `${dateAffichee} ${heureDebut} - ${heureFin} @ ${selectedCreneau.lieu}`,
      selectedCreneau,
    });

    emit("continue");
  },
  errors => {
    scrollToFirstValidationError(errors);
  },
);

const onSuggestNearest = (service: any) => {
  poste.value = service;
};
</script>

<style scoped>
.title-mobile {
  font-weight: 400;
}

@media (max-width: 959px) {
  .title-mobile {
    font-weight: 700;
  }
}
</style>
