<template>
  <v-form @submit.prevent="onSubmit">
    <h1 class="mb-5 text-h1 text-md-h2 d-none d-md-block">{{ t("titreApplication.prePlainte") }}</h1>
    <v-card class="pa-2 pa-md-8" elevation="1">
      <h2 class="pre-plainte-main-card-title text-h2 mb-4">{{ t("steps.informationsGenerales") }}</h2>

      <v-alert type="info" class="mb-6" density="comfortable" :icon="mobile ? false : undefined">
        <div class="text-body-2 text-md-body-1">
          {{ t("disclaimer.intro") }}
        </div>
      </v-alert>

      <v-card
        :elevation="isDarkMode ? 2 : 1"
        :variant="isDarkMode ? 'tonal' : 'flat'"
        :class="['confirmation-card', 'mb-4', { 'confirmation-card--selected': confirmeIdentite }]"
        data-cy="confirmation-identite"
        @click="toggleConfirmeIdentite"
      >
        <v-card-text class="d-flex align-center pa-2 pa-md-4">
          <v-checkbox v-model="confirmeIdentite" hide-details class="flex-shrink-0 mr-3" @click.stop />
          <span>
            {{ t("disclaimer.confirmeIdentite") }}
          </span>
        </v-card-text>
      </v-card>

      <v-card
        :elevation="isDarkMode ? 2 : 1"
        :variant="isDarkMode ? 'tonal' : 'flat'"
        :class="['confirmation-card', 'mb-4', { 'confirmation-card--selected': confirmeSituation }]"
        data-cy="confirmation-situation"
        @click="toggleConfirmeSituation"
      >
        <v-card-text class="d-flex align-center pa-2 pa-md-4">
          <v-checkbox v-model="confirmeSituation" hide-details class="flex-shrink-0 mr-3" @click.stop />
          <span>
            {{ t("disclaimer.confirmeSituation") }}
          </span>
        </v-card-text>
      </v-card>

      <v-card
        :elevation="isDarkMode ? 2 : 1"
        :variant="isDarkMode ? 'tonal' : 'flat'"
        :class="['confirmation-card', 'mb-6', { 'confirmation-card--selected': confirmeEffraction }]"
        data-cy="confirmation-effraction"
        @click="toggleConfirmeEffraction"
      >
        <v-card-text class="d-flex align-center pa-2 pa-md-4">
          <v-checkbox
            v-model="confirmeEffraction"
            hide-details
            class="flex-shrink-0 mr-3"
            @click.stop
          />
          <span>
            {{ t("disclaimer.confirmeEffraction") }}
          </span>
        </v-card-text>
      </v-card>

      <div class="d-none d-md-flex justify-end mt-6">
        <v-btn color="primary" variant="flat" size="large" data-cy="confirmer-disclaimer" @click="confirmDisclaimer">
          {{ t("common.confirmer") }}
        </v-btn>
      </div>

      <div class="d-md-none mt-4">
        <v-btn color="primary" variant="flat" class="w-100" data-cy="confirmer-disclaimer" @click="confirmDisclaimer">
          {{ t("common.confirmer") }}
        </v-btn>
      </div>

      <v-alert
        v-if="showDisclaimerWarning"
        type="warning"
        class="mt-4 mb-6"
        density="comfortable"
        :icon="mobile ? false : undefined"
        data-cy="disclaimer-warning"
      >
        {{ t("disclaimer.situationNonEligible") }}
        <a :href="POSTES_POLICE_URL" target="_blank" rel="noopener noreferrer">
          {{ t("disclaimer.postesPolice") }}
        </a>
      </v-alert>

      <div v-if="disclaimerConfirmed" data-cy="contenu-informations-generales">
        <h3>{{ t("informationsEvenement.typeIncident") }}</h3>
        <AccessibleVSelect
          v-model="typeIncident"
          data-cy="type-incident"
          :label="t('informationsEvenement.typeIncident')"
          required
          :items="[
            { label: t('incidentTypes.vol'), value: 'vol' },
            { label: t('dommages.titre'), value: 'degat-delit' },
            { label: t('cybercrime.titre'), value: 'cybercrime' },
          ]"
          :error-messages="typeIncidentError"
          :hint="t('informationsEvenement.hintTypeIncident')"
          persistent-hint
          clearable
          class="mb-8 mt-5"
        />

        <v-alert
          v-if="typeIncident === 'vol'"
          type="warning"
          class="mb-6"
          density="comfortable"
          :icon="mobile ? false : undefined"
        >
          {{ t("disclaimer.warningVolCarte") }}
        </v-alert>

        <AccessibleVSelect
          v-if="typeIncident === 'degat-delit'"
          v-model="typeDommage"
          :label="t('dommages.typeDommage')"
          required
          :items="typeDommageOptions"
          :error-messages="typeDommageError"
          item-title="label"
          item-value="value"
          :hint="t('dommages.hintTypeDommage')"
          persistent-hint
          class="mb-8"
        />

        <BaseRadioGroup
          v-if="showConstatQuestion"
          v-model="constatPresent"
          :label="t('dommages.constat')"
          required
          :options="[
            { label: t('common.oui'), value: true },
            { label: t('common.non'), value: false },
          ]"
          :error-messages="constatPresentError"
        />

        <div v-if="showConstatPhotosUpload" class="mb-8">
          <PieceJointe v-model="fichiers" :label="constatPhotosLabel" />
        </div>

        <v-alert
          v-if="showRendezVousOnlyMessage"
          type="info"
          class="mb-6"
          density="comfortable"
          :icon="mobile ? false : undefined"
        >
          {{ t("dommages.constatRendezVousOnlyInfo") }}
        </v-alert>

        <AccessibleVSelect
          v-if="typeIncident === 'cybercrime'"
          v-model="typeCybercrime"
          :items="typeCybercrimeOptions"
          item-title="label"
          item-value="value"
          :label="t('cybercrime.type')"
          required
          :error-messages="typeCybercrimeError"
          variant="outlined"
          class="mb-8"
          :hint="t('cybercrime.hintType')"
          persistent-hint
          clearable
        />

        <v-alert
          v-if="typeCybercrime === TYPE_CYBERCRIME_AUTRE"
          type="info"
          class="mb-6"
          density="comfortable"
          :icon="mobile ? false : undefined"
        >
          <p>{{ t("disclaimer.cybercrimeAutreIntroduction") }}</p>
          <ul class="ml-4 mt-2">
            <li>{{ t("disclaimer.cybercrimeAutreDocumentChronologique") }}</li>
            <li>{{ t("disclaimer.cybercrimeAutreEchanges") }}</li>
            <li>{{ t("disclaimer.cybercrimeAutrePreuvesPaiement") }}</li>
            <li>{{ t("disclaimer.cybercrimeAutreEtc") }}</li>
          </ul>
        </v-alert>

        <v-alert v-else-if="showCasNonTrouveWarning" type="info" class="mb-6" density="comfortable" :icon="mobile ? false : undefined">
          {{ t("disclaimer.casNonTrouve") }}
          <a :href="POSTES_POLICE_URL" target="_blank" rel="noopener noreferrer">
            {{ t("disclaimer.prendreRendezVousPoste") }}
          </a>
        </v-alert>

        <v-divider v-if="captchaEnabled"></v-divider>
        <div @click.stop v-if="captchaEnabled">
          <Captcha
            :model-value="captchaToken"
            :sitekey="captchaSiteKey"
            @solved="store.setCaptchaToken"
            @reset="store.resetCaptchaToken"
            class="mb-6 mt-6"
          />
        </div>

        <div class="d-none d-md-flex justify-end mt-6">
          <v-btn color="primary" variant="flat" size="large" :disabled="!canContinue" data-cy="continuer-informations-generales" @click="onSubmit">
            {{ t("common.continuer") }}
          </v-btn>
        </div>

        <div class="d-md-none mt-4">
          <v-btn color="primary" variant="flat" class="w-100" :disabled="!canContinue" data-cy="continuer-informations-generales" @click="onSubmit">
            {{ t("common.continuer") }}
          </v-btn>
        </div>
      </div>
    </v-card>

    <div class="d-md-none mt-4 d-flex flex-column gap-2">
      <ExitActionsForm :is-mobile="true" />
    </div>
  </v-form>
</template>

<script setup lang="ts">
import { computed, ref, watch, watchEffect } from "vue";
import { useI18n } from "vue-i18n";
import { useDisplay } from "vuetify/framework";
import { useTheme } from "vuetify";
import { useField, useForm } from "vee-validate";
import type { PrePlainteFormFields } from "@/types/pre-plainte.interface.ts";
import { useCreatePrePlainteStore } from "@/stores/createPrePlainteStore.ts";
import { getCaptchaSitekey, isCaptchaEnabled } from "@/config/config.ts";
import Captcha from "@/components/captcha/Captcha.vue";
import { storeToRefs } from "pinia";
import ExitActionsForm from "@/components/actions/ExitActionsForm.vue";
import AccessibleVSelect from "@/components/accessibility/AccessibleVSelect.vue";
import BaseRadioGroup from "@/components/radio/BaseRadioGroup.vue";
import PieceJointe from "@/components/piece-jointe/PieceJointe.vue";
import { POSTES_POLICE_URL, TYPES_DOMMAGE } from "@/constants/constant.ts";
import { toTranslatedOptions } from "@/utils/helpers/traductionHelper.ts";
import {
  canContinueDisclaimer,
  CYBERCRIME_INCIDENT,
  DEGAT_DELIT_INCIDENT,
  hasConfirmedDisclaimer,
  isRendezVousOnlyDommage,
  requiresConstatQuestion,
  shouldResetTypeCybercrime,
  shouldResetTypeDommage,
  TYPE_CYBERCRIME_AUTRE,
} from "@/utils/workflows/disclaimer-workflow";

const { t } = useI18n();
const { mobile } = useDisplay();
const theme = useTheme();
const isDarkMode = computed(() => theme.global.current.value.dark);

const store = useCreatePrePlainteStore();
const emit = defineEmits<{ continue: [] }>();

const { captchaToken } = storeToRefs(store);
const captchaSiteKey = getCaptchaSitekey() || "";
const captchaEnabled = isCaptchaEnabled();
const form = useForm<PrePlainteFormFields>({
  initialValues: store.userFormData,
});

const { handleSubmit, setFieldError, setFieldValue } = form;

const { value: confirmeIdentite } = useField("confirmeIdentite");
const { value: confirmeSituation } = useField("confirmeSituation");
const confirmeEffraction = ref<boolean>(false);
const disclaimerConfirmed = ref(false);
const showDisclaimerWarning = ref(false);
const { value: typeIncident, errorMessage: typeIncidentError } = useField<string>("typeIncident");
const { value: typeDommage, errorMessage: typeDommageError } = useField<string>("typeDommage");
const { value: constatPresent, errorMessage: constatPresentError } = useField<boolean | null>("constatPresent");
const { value: typeCybercrime, errorMessage: typeCybercrimeError } = useField<string>("typeCybercrime");
const { value: fichiers } = useField<File[]>("fichiers");

const typeDommageOptions = computed(() => toTranslatedOptions(TYPES_DOMMAGE, t));
const typeCybercrimeOptions = computed(() => [
  { label: t("cybercrime.commandeFrauduleuse"), value: "commande-frauduleuse" },
  { label: t("cybercrime.achatNonRecu"), value: "achat-non-recu" },
  { label: t("cybercrime.fausseAnnonce"), value: "fausse-annonce" },
  { label: t("cybercrime.autre"), value: TYPE_CYBERCRIME_AUTRE },
]);

const showConstatQuestion = computed(
  () => typeIncident.value === DEGAT_DELIT_INCIDENT && requiresConstatQuestion(typeDommage.value),
);

const showRendezVousOnlyMessage = computed(() =>
  isRendezVousOnlyDommage({
    typeIncident: typeIncident.value,
    typeDommage: typeDommage.value,
    constatPresent: constatPresent.value,
  }),
);

const showConstatPhotosUpload = computed(() => showConstatQuestion.value && constatPresent.value === false);
const constatPhotosLabel = computed(() => `${t("dommages.fichiers")} (${t("dommages.photosRecommandees")})`);

const isIncidentSelectionComplete = computed(() => {
  if (typeIncident.value === DEGAT_DELIT_INCIDENT) {
    return Boolean(typeDommage.value);
  }
  if (typeIncident.value === CYBERCRIME_INCIDENT) {
    return Boolean(typeCybercrime.value) && typeCybercrime.value !== TYPE_CYBERCRIME_AUTRE;
  }
  return Boolean(typeIncident.value);
});

const showCasNonTrouveWarning = computed(() => !isIncidentSelectionComplete.value);

const canContinue = computed(() =>
  canContinueDisclaimer({
    typeIncident: typeIncident.value,
    typeDommage: typeDommage.value,
    constatPresent: constatPresent.value,
    typeCybercrime: typeCybercrime.value,
    confirmeIdentite: Boolean(confirmeIdentite.value),
    confirmeSituation: Boolean(confirmeSituation.value),
    confirmeEffraction: Boolean(confirmeEffraction.value),
    disclaimerConfirmed: disclaimerConfirmed.value,
    captchaEnabled,
    captchaToken: captchaToken.value,
  }),
);

function confirmDisclaimer() {
  const confirmations = {
    confirmeIdentite: Boolean(confirmeIdentite.value),
    confirmeSituation: Boolean(confirmeSituation.value),
    confirmeEffraction: Boolean(confirmeEffraction.value),
  };
  const isValid = hasConfirmedDisclaimer(confirmations);

  disclaimerConfirmed.value = isValid;
  showDisclaimerWarning.value = !isValid;
}

function toggleConfirmeIdentite() {
  confirmeIdentite.value = !confirmeIdentite.value;
}

function toggleConfirmeSituation() {
  confirmeSituation.value = !confirmeSituation.value;
}

function toggleConfirmeEffraction() {
  confirmeEffraction.value = !confirmeEffraction.value;
}

const validateIncidentFields = () => {
  setFieldError("typeIncident", typeIncident.value ? undefined : t("validation.typeIncidentRequis"));
  setFieldError("typeDommage", getTypeDommageError());
  setFieldError("constatPresent", getConstatPresentError());
  setFieldError("typeCybercrime", getTypeCybercrimeError());
};

const getTypeDommageError = () =>
  typeIncident.value === DEGAT_DELIT_INCIDENT && !typeDommage.value ? t("validation.typeDommageRequis") : undefined;

const getConstatPresentError = () =>
  showConstatQuestion.value && (constatPresent.value === null || constatPresent.value === undefined)
    ? t("validation.constatRequis")
    : undefined;

const getTypeCybercrimeError = () =>
  typeIncident.value === CYBERCRIME_INCIDENT && !typeCybercrime.value
    ? t("validation.typeCybercrimeRequis")
    : undefined;

const isSubmitAllowed = () => canContinue.value && (!captchaEnabled || Boolean(captchaToken.value));

const onSubmit = handleSubmit(formValues => {
  validateIncidentFields();
  if (!isSubmitAllowed()) {
    return;
  }

  store.setUserFormData(formValues);
  emit("continue");
});

watch(typeIncident, incident => {
  if (shouldResetTypeDommage(incident)) {
    setFieldValue("typeDommage", "");
    setFieldValue("constatPresent", null);
    setFieldError("constatPresent", undefined);
  }
  if (shouldResetTypeCybercrime(incident)) {
    setFieldValue("typeCybercrime", "");
  }
});

watchEffect(() => {
  const confirmations = [confirmeIdentite.value, confirmeSituation.value, confirmeEffraction.value];
  if (confirmations.length === 0) {
    return;
  }
  disclaimerConfirmed.value = false;
  showDisclaimerWarning.value = false;
});

watch(typeDommage, value => {
  if (!requiresConstatQuestion(value)) {
    setFieldValue("constatPresent", null);
    setFieldError("constatPresent", undefined);
  }
});

watch(constatPresent, value => {
  if (value !== false) {
    setFieldValue("fichiers", []);
  }
});
</script>
