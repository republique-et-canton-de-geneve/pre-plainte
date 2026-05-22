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

      <AppointmentTable
        v-model:page="page"
        v-model:creneau-prefere="creneauPrefere"
        :creneaux-pagines="creneauxPagines"
        :total-pages="totalPages"
      />

      <div class="d-md-none mt-4">
        <div class="pre-plainte-mobile-step-actions d-flex flex-column gap-4 mb-2">
          <v-btn variant="outlined" color="primary" class="w-100" @click="$emit('cancel')">
            {{ t("common.precedent") }}
          </v-btn>
          <v-btn type="submit" variant="flat" color="primary" class="w-100">
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
        <v-btn variant="outlined" color="primary" class="me-4" @click="$emit('cancel')">
          {{ t("common.precedent") }}
        </v-btn>
        <v-btn type="submit" variant="flat" color="primary">
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
import { computed, onMounted, ref, watch } from "vue";
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

const YEAR_START = 0;
const YEAR_END = 4;
const MONTH_START = 4;
const MONTH_END = 6;
const DAY_START = 6;
const DAY_END = 8;
const HOUR_MINUTE_START = 9;
const VEHICULE_PLAQUE_MAX_RENDEZ_VOUS_HOURS = 24;
const RENDEZ_VOUS_DATE_WINDOW_DAYS = 15;

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
const itemsParPage = 5;

onMounted(async () => {
  await esiriusStore.loadServicesForSite("PPEL");
  await esiriusStore.loadAllAvailabilitiesForPPEL();
});

const servicesDisponibles = computed(() => {
  const incident = (store.userFormData.typeIncident || "").toLowerCase();

  const servicesAvecDispos = new Set(
    esiriusStore.allAvailabilities
      .filter((a: any) => a.availabilities && a.availabilities.length > 0)
      .map((a: any) => a.serviceId),
  );

  const servicesFiltrables = esiriusStore.services.filter((s: any) => servicesAvecDispos.has(s.key));

  if (!incident) {
    return servicesFiltrables;
  }

  return servicesFiltrables.filter((service: any) => {
    const name = (service.name || "").toLowerCase();

    if (incident.includes("vol")) {
      return name.includes("vol");
    }
    if (incident.includes("degat") || incident.includes("dommage")) {
      return name.includes("dommage");
    }
    if (incident.includes("cyber")) {
      return name.includes("cybercrime");
    }

    if (incident.includes("dommage-cybercrime")) {
      return name.includes("dommage") || name.includes("cybercrime");
    }

    return false;
  });
});

const rendezVousWarning = computed(() => {
  if (store.userFormData.typeIncident !== "vol") {
    return null;
  }

  if (aucunCreneauVehiculeAvecPlaque.value) {
    return {
      type: "warning" as const,
      messageKey: "rendezVous.warningVolVehiculePlaqueSansCreneau",
    };
  }

  if (isVehiculeVoleAvecPlaque.value) {
    return {
      type: "error" as const,
      messageKey: "rendezVous.warningVolVehiculePlaque",
    };
  }

  return {
    type: "info" as const,
    messageKey: "rendezVous.warningAutreVol",
  };
});

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

const creneauxCompatiblesIncident = computed(() => {
  const incident = (store.userFormData.typeIncident || "").toLowerCase();
  const allAvail = esiriusStore.allAvailabilities.flatMap((serviceAvailabilities: any) =>
    (serviceAvailabilities.availabilities || []).map((creneau: any) => ({
      ...creneau,
      serviceId: creneau.serviceId ?? serviceAvailabilities.serviceId,
      siteCode: creneau.siteCode ?? "PPEL",
    }))
  );

  const now = new Date();
  now.setHours(now.getHours() + 1);
  const limiteVehiculeAvecPlaque = new Date();
  limiteVehiculeAvecPlaque.setHours(limiteVehiculeAvecPlaque.getHours() + VEHICULE_PLAQUE_MAX_RENDEZ_VOUS_HOURS);

  return allAvail.filter((c: any) => {
    const dateCreneau = parseCreneauDate(c?.beginDateTime);
    if (!dateCreneau || dateCreneau <= now) {
      return false;
    }

    if (!isInRollingAppointmentWindow(dateCreneau)) {
      return false;
    }

    if (isVehiculeVoleAvecPlaque.value && dateCreneau > limiteVehiculeAvecPlaque) {
      return false;
    }

    const service = esiriusStore.services.find((s: any) => s.key === c.serviceId);
    const serviceName = (service?.name || "").toLowerCase();

    return matchIncidentWithService(incident, serviceName);
  });
});

const creneauxFiltres = computed(() => {
  const idsCreneauxCompatibles = new Set(creneauxCompatiblesIncident.value.map((c: any) => getCreneauKey(c)));
  const allAvail = availabilitiesByPoste.value.flatMap((serviceAvailabilities: any) =>
    (serviceAvailabilities.availabilities || []).map((creneau: any) => ({
      ...creneau,
      serviceId: creneau.serviceId ?? serviceAvailabilities.serviceId,
      siteCode: creneau.siteCode ?? "PPEL",
    }))
  );

  return allAvail.filter((c: any) =>
    idsCreneauxCompatibles.has(getCreneauKey(c)) &&
    isSameSelectedDate(c.beginDateTime, dateSouhaitee.value)
  );
});

const datesDisponibles = computed(() => {
  const validDates = creneauxCompatiblesIncident.value
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

function getCreneauKey(creneau: any): string {
  return `${creneau.serviceId ?? ""}|${creneau.beginDateTime ?? ""}|${creneau.resource?.key ?? ""}`;
}

function parseCreneauDate(beginDateTime?: string): Date | null {
  if (!beginDateTime) {
    return null;
  }

  const dateStr = `${beginDateTime.slice(YEAR_START, YEAR_END)}-${beginDateTime.slice(MONTH_START, MONTH_END)}-${beginDateTime.slice(DAY_START, DAY_END)}T${beginDateTime.slice(HOUR_MINUTE_START)}:00`;

  const date = new Date(dateStr);
  return Number.isNaN(date.getTime()) ? null : date;
}

function isSameSelectedDate(beginDateTime: string, selectedDate?: string): boolean {
  if (!selectedDate) {
    return true;
  }

  const dateCreneauJour = `${beginDateTime.slice(YEAR_START, YEAR_END)}-${beginDateTime.slice(MONTH_START, MONTH_END)}-${beginDateTime.slice(DAY_START, DAY_END)}`;

  return dateCreneauJour === (toIsoDate(selectedDate) ?? selectedDate);
}

function isInRollingAppointmentWindow(date: Date): boolean {
  const start = new Date();
  start.setHours(0, 0, 0, 0);
  const end = new Date(start);
  end.setDate(start.getDate() + RENDEZ_VOUS_DATE_WINDOW_DAYS - 1);
  const dateOnly = new Date(date);
  dateOnly.setHours(0, 0, 0, 0);
  return dateOnly >= start && dateOnly <= end;
}

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

function matchIncidentWithService(incident: string, serviceName: string): boolean {
  if (!incident) {
    return true;
  }

  if (incident.includes("vol")) {
    return serviceName.includes("vol");
  }

  if (incident.includes("degat") || incident.includes("dommage")) {
    return serviceName.includes("dommage");
  }

  if (incident.includes("cyber")) {
    return serviceName.includes("cybercrime");
  }

  if (incident.includes("dommage-cybercrime")) {
    return serviceName.includes("dommage") || serviceName.includes("cybercrime");
  }

  return false;
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
    const heureDebut = c.beginDateTime.substring(HOUR_MINUTE_START).trim();
    const heureFin = c.endDateTime.substring(HOUR_MINUTE_START).trim();
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
