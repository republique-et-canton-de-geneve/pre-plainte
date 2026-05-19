<template>
  <div>
    <v-form v-if="currentStep === 1" v-bind="form" @submit.prevent="onSubmitCode">
      <h1 class="mb-4 d-none d-md-block">{{ t("modification.titre") }}</h1>

      <v-card class="pa-2 pa-md-12 mb-4 mb-md-12">
        <h2 class="pre-plainte-main-card-title mb-1">{{ t("modification.titreSaisieCode") }}</h2>
        <p class="text-body-2 text-grey-darken-1 mb-8">{{ t("modification.information") }}</p>
        <v-text-field
          :label="t('modification.libelleCode')"
          v-model="rdvCode"
          :error-messages="rdvCodeError"
          :placeholder="t('modification.texteIndicationCode')"
          persistent-placeholder
          variant="outlined"
          @update:modelValue="(val) => (rdvCode = val.toUpperCase())"
        />
        <div class="d-flex align-center mt-4 mb-4">
          <Captcha
            :key="captchaKey"
            v-model="captchaToken"
            :sitekey="captchaSiteKey"
            api-endpoint="https://eu.frcapi.com/api/v2"
            @reset="resetCaptcha"
          />
        </div>

        <v-alert v-if="alertMessage" :type="alertType" class="mb-4" closable @click:close="alertMessage = ''">
          {{ alertMessage }}
        </v-alert>

        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn type="submit" variant="flat" color="primary" :loading="isSubmitting" :disabled="isSubmitDisabled">
            {{ t("modification.actionContinuer") }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-form>

    <div v-if="currentStep === 2">
      <h1 class="mb-4 d-none d-md-block">{{ t("modification.titreSelectionCreneau") }}</h1>

      <v-sheet class="pa-2 pa-md-8 mb-4" rounded="lg" elevation="1">
        <v-alert type="info" class="mb-6" v-if="currentAppointment && currentAppointment.beginDate">
          {{ t("modification.infoCreneauActuel") }}: <strong> {{ formattedCurrentAppointment }}</strong>
        </v-alert>

        <AppointmentFilters
          v-model:date-souhaitee="dateSouhaitee"
          v-model:poste="poste"
          :services-disponibles="servicesDisponibles"
          :dates-disponibles="datesDisponibles"
          :premiere-date-dispo="premiereDateDispo"
          :derniere-date-dispo="derniereDateDispo"
        />

        <LeafletMap
          :services="servicesDisponibles"
          :selected-service="poste"
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
              Retour
            </v-btn>
            <v-btn
              variant="flat"
              color="primary"
              class="w-100"
              :disabled="isCreneauButtonDisabled"
              :loading="isUpdating || prePlainteIsLoading"
              @click="onSelectCreneau"
            >
              {{ t("modification.actionChoisirCreneau") }}
            </v-btn>
          </div>
        </div>
      </v-sheet>

      <v-row class="mt-4 d-none d-md-flex" align="center" justify="end">
        <v-col cols="12" md="auto" class="d-flex">
          <v-btn variant="outlined" color="primary" class="me-4" @click="$emit('cancel')">
            {{ t("common.precedent") }}
          </v-btn>
          <v-btn
            variant="flat"
            color="primary"
            :disabled="isCreneauButtonDisabled"
            :loading="isUpdating || prePlainteIsLoading"
            @click="onSelectCreneau"
          >
            {{ t("modification.actionChoisirCreneau") }}
          </v-btn>
        </v-col>
      </v-row>
    </div>

    <v-dialog v-model="confirmDialog" max-width="480">
      <v-card>
        <v-card-title class="text-h3 py-4">{{ t("modification.dialogueConfirmation.titre") }}</v-card-title>
        <v-card-text>{{ t("modification.dialogueConfirmation.message") }}</v-card-text>
        <v-card-actions class="py-4">
          <v-spacer />
          <v-btn variant="text" @click="confirmDialog = false">
            {{ t("modification.dialogueConfirmation.annuler") }}
          </v-btn>
          <v-btn color="primary" variant="flat" :loading="isUpdating || prePlainteIsLoading" @click="handleConfirm">
            {{ t("modification.dialogueConfirmation.confirmer") }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script lang="ts" setup>
import { computed, nextTick, onMounted, ref, watch } from "vue";
import { toTypedSchema } from "@vee-validate/zod";
import { useField, useForm } from "vee-validate";
import { z } from "zod";
import { storeToRefs } from "pinia";
import { useEsiriusStore } from "@/stores/useEsiriusStore";
import { useCreatePrePlainteStore } from "@/stores/createPrePlainteStore";
import { useI18n } from "vue-i18n";
import Captcha from "@/components/captcha/Captcha.vue";
import LeafletMap from "@/components/map/LeafletMap.vue";
import AppointmentFilters from "@/components/appointment/AppointmentFilters.vue";
import AppointmentTable from "@/components/appointment/AppointmentTable.vue";
import type {Service} from "@/composables/useLeafletMap";
import type {AlertType, Availability, IncidentType, SelectedCreneau} from "@/types/rendez-vous-interface";
import { buildUpdateAppointmentPayload } from "@/utils/helpers/esiriusFormatBuilder";
import { getCaptchaSitekey } from "@/config/config.ts";

const props = defineProps<{
  currentStep: number;
}>();

const emit = defineEmits<{
  "code-verified": [];
  "creneau-selected": [];
  updated: [message: string];
  cancel: [];
}>();

const { t } = useI18n();
const esiriusStore = useEsiriusStore();
const { loading } = storeToRefs(esiriusStore);
const prePlainteStore = useCreatePrePlainteStore();
const { currentAppointment, isLoading: prePlainteIsLoading } = storeToRefs(prePlainteStore);

const form = useForm({
  validationSchema: toTypedSchema(
    z.object({
      rdvCode: z.string().min(6, { message: t("modification.codeObligatoire") }),
    }),
  ),
});

const { value: rdvCode, errorMessage: rdvCodeError } = useField<string>("rdvCode");
const { handleSubmit: handleSubmitCode } = form;
const captchaToken = ref("");
const captchaKey = ref(0);
const alertMessage = ref("");
const alertType = ref<AlertType>("error");

const captchaSiteKey = getCaptchaSitekey() || "";

const { value: dateSouhaitee } = useField<string>("dateSouhaitee");
const poste = ref<Service | null>(null);
const creneauPrefere = ref<number | null>(null);
const page = ref(1);
const itemsParPage = 5;
const selectedCreneau = ref<SelectedCreneau | null>(null);
const pendingCreneau = ref<SelectedCreneau | null>(null);
const confirmDialog = ref(false);
const isUpdating = ref(false);

const isSubmitting = computed(() => loading.value);
const isSubmitDisabled = computed(() => isSubmitting.value || !captchaToken.value);
const isCreneauButtonDisabled = computed(
  () => creneauPrefere.value === null || isUpdating.value || prePlainteIsLoading.value,
);

const formattedCurrentAppointment = computed(() => {
  if (!currentAppointment.value || !currentAppointment.value.beginDate) {
    return "";
  }
  const appointment = currentAppointment.value;
  const beginDate = appointment.beginDate;
  const beginTime = appointment.beginTime;

  const dateParts = beginDate.split("-");
  const formattedDate = dateParts.length === 3 ? `${dateParts[2]}.${dateParts[1]}.${dateParts[0]}` : beginDate;

  const formattedTime = beginTime || "";

  return formattedTime ? `${formattedDate} à ${formattedTime}` : formattedDate;
});

onMounted(async () => {
  await esiriusStore.loadServicesForSite("PPEL");
  await esiriusStore.loadAllAvailabilitiesForPPEL();
});

const showAlert = (message: string, type: AlertType) => {
  alertMessage.value = message;
  alertType.value = type;
};

const resetCaptcha = () => {
  captchaToken.value = "";
  captchaKey.value += 1;
};

const verifyRdvCodeExists = async (code: string): Promise<boolean> => {
  try {
    const appointment = await esiriusStore.getAppointmentByCode(code);
    prePlainteStore.setCurrentAppointment(appointment);
    prePlainteStore.setUserFormData({ ...prePlainteStore.userFormData, codeRdv: code });
    return true;
  } catch {
    showAlert(t("modification.codeInexistant"), "error");
    resetCaptcha();
    return false;
  }
};

const onSubmitCode = handleSubmitCode(async (formValues: { rdvCode: string }) => {
  const code = formValues.rdvCode.trim();
  if (await verifyRdvCodeExists(code)) {
    emit("code-verified");
  }
});

const extractIncidentType = (personalIdentity: string): IncidentType | null => {
  const parts = personalIdentity.split("-");
  const eventTypeChar = parts.length >= 3 ? parts[2].toLowerCase() : "";
  if (eventTypeChar === "c") {
    return "cyber";
  }
  if (eventTypeChar === "d") {
    return "dommage";
  }
  if (eventTypeChar === "v") {
    return "vol";
  }
  return null;
};

const filterServiceByIncidentType = (service: Service, incidentType: IncidentType | null): boolean => {
  if (!incidentType) {
    return true;
  }
  const name = (service.name || "").toLowerCase();
  if (incidentType === "vol") {
    return name.includes("vol");
  }
  if (incidentType === "dommage") {
    return name.includes("dommage");
  }
  if (incidentType === "cyber") {
    return name.includes("cybercrime");
  }
  return true;
};

const servicesDisponibles = computed(() => {
  const servicesAvecDispos = new Set(
    esiriusStore.allAvailabilities
    .filter(a => a.availabilities && a.availabilities.length > 0)
    .map(a => a.serviceId)
  );

  const servicesFiltrables = esiriusStore.services.filter(s => servicesAvecDispos.has(s.key));

  const personalIdentity = currentAppointment.value?.user?.personalIdentity;
  if (!personalIdentity) {
    return servicesFiltrables;
  }

  const incidentType = extractIncidentType(personalIdentity);
  if (!incidentType) {
    return servicesFiltrables;
  }

  return servicesFiltrables.filter(service => filterServiceByIncidentType(service, incidentType));
});

const datesDisponibles = computed(() => {
  const allAvail = esiriusStore.allAvailabilities.flatMap(s => s.availabilities || []);
  const validDates = allAvail.filter(a => a?.beginDateTime).map(a => a.beginDateTime.slice(0, 8));
  return Array.from(new Set(validDates)).map(d => `${d.slice(0, 4)}-${d.slice(4, 6)}-${d.slice(6, 8)}`);
});

const premiereDateDispo = computed(() => {
  const now = new Date();
  now.setHours(now.getHours() + 1);
  const nowDate = now.toISOString().slice(0, 10);

  const futureDates = datesDisponibles.value.filter(d => d >= nowDate);
  return futureDates.length ? futureDates[0] : nowDate;
});

const derniereDateDispo = computed(() => datesDisponibles.value[datesDisponibles.value.length - 1] || "");

const availabilitiesByPoste = computed(() => {
  if (!poste.value) {
    return esiriusStore.allAvailabilities;
  }
  return esiriusStore.allAvailabilities.filter(
    a => a.serviceId === poste.value?.key || a.serviceName?.includes(poste.value?.name || ""),
  );
});

const creneauxFiltres = computed(() => {
  const allAvail = availabilitiesByPoste.value.flatMap(s => s.availabilities || []);

  const minDateTime =
    currentAppointment.value?.beginDate && currentAppointment.value?.beginTime
      ? new Date(`${currentAppointment.value.beginDate}T${currentAppointment.value.beginTime}:00`)
      : (() => {
          const date = new Date();
          date.setHours(date.getHours() + 1);
          return date;
        })();

  const personalIdentity = currentAppointment.value?.user?.personalIdentity;
  const incidentType = personalIdentity ? extractIncidentType(personalIdentity) : null;

  return allAvail.filter((c: Availability) => {
    if (!c?.beginDateTime) {
      return false;
    }

    const dateStr = `${c.beginDateTime.slice(0, 4)}-${c.beginDateTime.slice(4, 6)}-${c.beginDateTime.slice(6, 8)}T${c.beginDateTime.slice(9)}:00`;
    const dateCreneau = new Date(dateStr);
    if (dateCreneau <= minDateTime) {
      return false;
    }

    const dateCreneauJour = `${c.beginDateTime.slice(0, 4)}-${c.beginDateTime.slice(4, 6)}-${c.beginDateTime.slice(6, 8)}`;
    if (dateSouhaitee.value && dateCreneauJour !== dateSouhaitee.value) {
      return false;
    }

    if (incidentType) {
      const service = esiriusStore.services.find(s => s.key === c.serviceId);
      return service ? filterServiceByIncidentType(service, incidentType) : false;
    }

    return true;
  });
});

const totalPages = computed(() => Math.ceil(creneauxFiltres.value.length / itemsParPage));
const creneauxPagines = computed(() => {
  const start = (page.value - 1) * itemsParPage;
  return creneauxFiltres.value.slice(start, start + itemsParPage);
});

watch(poste, () => {
  page.value = 1;
  creneauPrefere.value = null;
});

watch(
  () => currentAppointment.value?.user?.personalIdentity,
  () => {
    const services = servicesDisponibles.value;
    if (poste.value && !services.some(s => s.key === poste.value?.key)) {
      poste.value = null;
    }
  },
);

const onSuggestNearest = (service: Service) => {
  poste.value = service;
};

const onSelectCreneau = () => {
  if (creneauPrefere.value === null || isUpdating.value) {
    return;
  }

  const c = creneauxPagines.value[creneauPrefere.value];
  if (!c) {
    showAlert(t("modification.messageErreur"), "error");
    return;
  }

  const rawDate = `${c.beginDateTime.slice(0, 4)}-${c.beginDateTime.slice(4, 6)}-${c.beginDateTime.slice(6, 8)}`;
  const heureDebut = c.beginDateTime.substring(9).trim();
  const heureFin = c.endDateTime.substring(9).trim();

  pendingCreneau.value = {
    id: Date.now().toString(),
    date: rawDate,
    dateAffichee: `${rawDate.split("-").reverse().join(".")}`,
    heureDebut,
    heureFin,
    lieu: c.resource?.name || "-",
    serviceId: c.serviceId,
    siteCode: c.siteCode,
    resource: c.resource,
    beginDateTime: c.beginDateTime,
    endDateTime: c.endDateTime,
  };

  confirmDialog.value = true;
};

const handleConfirm = async () => {
  if (!pendingCreneau.value) {
    return;
  }
  confirmDialog.value = false;
  selectedCreneau.value = pendingCreneau.value;
  prePlainteStore.setUserFormData({ ...prePlainteStore.userFormData, selectedCreneau: pendingCreneau.value });
  pendingCreneau.value = null;

  isUpdating.value = true;
  prePlainteIsLoading.value = true;
  try {
    await handleUpdateAppointment();
  } catch (error) {
    isUpdating.value = false;
    prePlainteIsLoading.value = false;
  }
};
const formatDateTimeFrench = (dateString: string): string => {
  if (!dateString) {
    return "";
  }
  try {
    const dateParts = dateString.split("-");
    let date: Date;
    if (dateParts.length === 3) {
      const [year, month, day] = dateParts.map(Number);
      date = new Date(year, month - 1, day);
    } else {
      date = new Date(dateString);
    }
    const options: Intl.DateTimeFormatOptions = {
      weekday: "long",
      day: "numeric",
      month: "long",
      year: "numeric",
    };
    const formatted = date.toLocaleDateString("fr-FR", options);
    return formatted.charAt(0).toUpperCase() + formatted.slice(1);
  } catch {
    return dateString;
  }
};

const handleUpdateAppointment = async (): Promise<void> => {
  if (!currentAppointment.value || !selectedCreneau.value) {
    showAlert(t("modification.messageErreur"), "error");
    return;
  }

  try {
    const payload = buildUpdateAppointmentPayload(currentAppointment.value, selectedCreneau.value);
    await esiriusStore.updateAppointment(payload);

    const creneau = selectedCreneau.value;
    const codeRdv = rdvCode.value || currentAppointment.value.codeRDV || "—";
    const dateLisible = formatDateTimeFrench(creneau.date);

    const message = t("modification.messageConfirmation", {
      date: dateLisible,
      heureDebut: creneau.heureDebut,
      heureFin: creneau.heureFin,
      lieu: creneau.lieu,
      code: codeRdv,
    });

    isUpdating.value = false;
    prePlainteIsLoading.value = false;
    prePlainteStore.isLoading = false;
    await nextTick();
    prePlainteStore.setStep(3);
    emit("updated", message);
  } catch (error: unknown) {
    const errorMsg = error instanceof Error ? error.message : t("modification.messageErreur");
    showAlert(errorMsg, "error");
    isUpdating.value = false;
    prePlainteIsLoading.value = false;
    throw error;
  }
};
</script>
