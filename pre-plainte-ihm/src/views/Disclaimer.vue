<template>
  <v-form @submit.prevent="onSubmit">
    <h1 class="mb-5 text-h1 text-md-h2 d-none d-md-block">{{ t("titreApplication.prePlainte") }}</h1>
    <v-card class="pa-2 pa-md-8" elevation="1">
      <h2 class="pre-plainte-main-card-title text-h2 mb-4">{{ t("steps.informationsGenerales") }}</h2>

      <v-alert type="info" class="mb-6" density="comfortable" :icon="mobile ? false : undefined">
        <div class="text-body-2 text-md-body-1">
          {{ t("disclaimer.intro1") }}
          <span class="font-weight-bold">
            {{ t("disclaimer.intro2") }}
          </span>
          {{ t("disclaimer.intro3") }}
        </div>

        <ul class="ml-2 ml-md-4 mt-2 text-body-2 text-md-body-1">
          <li>{{ t("disclaimer.condition1") }}</li>
          <li>{{ t("disclaimer.condition2") }}</li>
          <li>{{ t("disclaimer.condition3") }}</li>
        </ul>

        <div class="font-weight-black text-body-2 text-md-body-1 mt-3 mt-md-4">
          <span class="font-weight-bold">
            {{ t("disclaimer.avantConfirmation") }}
          </span>
        </div>
      </v-alert>

      <v-card
        :elevation="isDarkMode ? 2 : 1"
        :variant="isDarkMode ? 'tonal' : 'flat'"
        :class="['confirmation-card', 'mb-4', { 'confirmation-card--selected': confirmeIdentite }]"
        data-cy="confirmation-identite"
        @click="confirmeIdentite = !confirmeIdentite"
      >
        <v-card-text class="d-flex align-center pa-2 pa-md-4">
          <v-checkbox
            v-model="confirmeIdentite"
            hide-details
            class="flex-shrink-0 mr-3"
            @click.stop
          />
          <span id="confirme-identite-label">
            {{ t("disclaimer.confirmeIdentite") }}
          </span>
        </v-card-text>
      </v-card>

      <v-card
        :elevation="isDarkMode ? 2 : 1"
        :variant="isDarkMode ? 'tonal' : 'flat'"
        :class="['confirmation-card', 'mb-6', { 'confirmation-card--selected': confirmeSituation }]"
        data-cy="confirmation-situation"
        @click="confirmeSituation = !confirmeSituation"
      >
        <v-card-text class="d-flex align-center pa-2 pa-md-4">
          <v-checkbox
            v-model="confirmeSituation"
            hide-details
            class="flex-shrink-0 mr-3"
            @click.stop
          />
          <span id="confirme-situation-label">
            {{ t("disclaimer.confirmeSituation") }}
          </span>
        </v-card-text>
      </v-card>

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

      <v-alert v-else type="info" class="mb-6" density="comfortable" :icon="mobile ? false : undefined">
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
    </v-card>

    <div class="d-md-none mt-4 d-flex flex-column gap-2">
      <ExitActionsForm :is-mobile="true" />
    </div>
  </v-form>
</template>

<script setup lang="ts">
import { computed, watch } from "vue";
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
import { POSTES_POLICE_URL, TYPES_DOMMAGE } from "@/constants/constant.ts";
import { toTranslatedOptions } from "@/utils/helpers/traductionHelper.ts";
import {
  canContinueDisclaimer,
  CYBERCRIME_INCIDENT,
  DEGAT_DELIT_INCIDENT,
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
const { value: typeIncident, errorMessage: typeIncidentError } = useField<string>("typeIncident");
const { value: typeDommage, errorMessage: typeDommageError } = useField<string>("typeDommage");
const { value: constatPresent, errorMessage: constatPresentError } = useField<boolean | null>("constatPresent");
const { value: typeCybercrime, errorMessage: typeCybercrimeError } = useField<string>("typeCybercrime");

const typeDommageOptions = computed(() => toTranslatedOptions(TYPES_DOMMAGE, t));
const typeCybercrimeOptions = computed(() => [
  { label: t("cybercrime.commandeFrauduleuse"), value: "commande-frauduleuse" },
  { label: t("cybercrime.achatNonRecu"), value: "achat-non-recu" },
  { label: t("cybercrime.fausseAnnonce"), value: "fausse-annonce" },
  { label: t("cybercrime.autre"), value: TYPE_CYBERCRIME_AUTRE },
]);

const showConstatQuestion = computed(() =>
  typeIncident.value === DEGAT_DELIT_INCIDENT && requiresConstatQuestion(typeDommage.value),
);

const showRendezVousOnlyMessage = computed(() =>
  isRendezVousOnlyDommage({
    typeIncident: typeIncident.value,
    typeDommage: typeDommage.value,
    constatPresent: constatPresent.value,
  }),
);

const canContinue = computed(
  () => canContinueDisclaimer({
    typeIncident: typeIncident.value,
    typeDommage: typeDommage.value,
    constatPresent: constatPresent.value,
    typeCybercrime: typeCybercrime.value,
    confirmeIdentite: Boolean(confirmeIdentite.value),
    confirmeSituation: Boolean(confirmeSituation.value),
    captchaEnabled,
    captchaToken: captchaToken.value,
  }),
);

const onSubmit = handleSubmit(formValues => {
  setFieldError("typeIncident", typeIncident.value ? undefined : t("validation.typeIncidentRequis"));
  setFieldError(
    "typeDommage",
    typeIncident.value === DEGAT_DELIT_INCIDENT && !typeDommage.value ? t("validation.typeDommageRequis") : undefined,
  );
  setFieldError(
    "constatPresent",
    showConstatQuestion.value && (constatPresent.value === null || constatPresent.value === undefined)
      ? t("validation.constatRequis")
      : undefined,
  );
  setFieldError(
    "typeCybercrime",
    typeIncident.value === CYBERCRIME_INCIDENT && !typeCybercrime.value ? t("validation.typeCybercrimeRequis") : undefined,
  );

  if (!canContinue.value) {
    return;
  }

  if (captchaEnabled && !captchaToken.value) {
    return;
  }
  store.setUserFormData(formValues);
  emit("continue");
});

watch(typeIncident, incident => {
  if (shouldResetTypeDommage(incident)) {
    setFieldValue("typeDommage", "");
    setFieldValue("constatPresent", null);
  }
  if (shouldResetTypeCybercrime(incident)) {
    setFieldValue("typeCybercrime", "");
  }
});

watch(typeDommage, value => {
  if (!requiresConstatQuestion(value)) {
    setFieldValue("constatPresent", null);
  }
});
</script>
