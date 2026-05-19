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

      <v-divider v-if="captchaEnabled"></v-divider>
      <div @click.stop v-if="captchaEnabled">
        <Captcha
          :model-value="captchaToken"
          :sitekey="captchaSiteKey"
          api-endpoint="https://eu.frcapi.com/api/v2"
          @solved="store.setCaptchaToken"
          @reset="store.resetCaptchaToken"
          class="mb-6 mt-6"
        />
      </div>

      <div class="d-none d-md-flex justify-end mt-6">
        <v-btn color="primary" variant="flat" size="large" :disabled="!canContinue" @click="onSubmit">
          {{ t("common.continuer") }}
        </v-btn>
      </div>

      <div class="d-md-none mt-4">
        <v-btn color="primary" variant="flat" class="w-100" :disabled="!canContinue" @click="onSubmit">
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
import { computed } from "vue";
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

const { handleSubmit } = form;

const { value: confirmeIdentite } = useField("confirmeIdentite");
const { value: confirmeSituation } = useField("confirmeSituation");

const canContinue = computed(
  () =>
    confirmeIdentite.value &&
    confirmeSituation.value &&
    (!captchaEnabled || Boolean(captchaToken.value)),
);

const onSubmit = handleSubmit(formValues => {
  if (captchaEnabled && !captchaToken.value) {
    return;
  }
  store.setUserFormData(formValues);
  emit("continue");
});
</script>
