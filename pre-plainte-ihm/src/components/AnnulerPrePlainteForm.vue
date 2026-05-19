<template>
  <v-form v-bind="form" @submit.prevent="onSubmit">
    <h1 class="mb-4 d-none d-md-block">{{ t("annulation.titre") }}</h1>

    <v-card class="pa-2 pa-md-12 mb-4 mb-md-12">
      <h2 class="pre-plainte-main-card-title mb-1">{{ t("annulation.titreSaisieCode") }}</h2>
      <p class="text-body-2 text-grey-darken-1 mb-8">{{ t("annulation.information") }}</p>
      <v-text-field
        :label="t('annulation.libelleCode')"
        v-model="rdvCode"
        :error-messages="rdvCodeError"
        :placeholder="t('annulation.texteIndicationCode')"
        persistent-placeholder
        variant="outlined"
        @update:modelValue="(val) => (rdvCode = val.toUpperCase())"
      />
      <div class="d-flex align-center mb-4">
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
          {{ t("annulation.actionAnnuler") }}
        </v-btn>
      </v-card-actions>
    </v-card>

    <v-dialog v-model="confirmDialog" max-width="480">
      <v-card>
        <v-card-title class="text-h3 py-4">{{ t("annulation.dialogueConfirmation.titre") }}</v-card-title>
        <v-card-text>{{ t("annulation.dialogueConfirmation.message") }}</v-card-text>
        <v-card-actions class="py-4">
          <v-spacer />
          <v-btn variant="text" @click="confirmDialog = false">
            {{ t("annulation.dialogueConfirmation.annuler") }}
          </v-btn>
          <v-btn color="primary" variant="flat" :loading="isSubmitting" @click="handleConfirm">
            {{ t("annulation.dialogueConfirmation.confirmer") }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-form>
</template>

<script lang="ts" setup>
import { computed, ref } from "vue";
import { toTypedSchema } from "@vee-validate/zod";
import { useField, useForm } from "vee-validate";
import { z } from "zod";
import { storeToRefs } from "pinia";
import { useEsiriusStore } from "@/stores/useEsiriusStore";
import { useI18n } from "vue-i18n";
import Captcha from "@/components/captcha/Captcha.vue";
import { getCaptchaSitekey } from "@/config/config.ts";

const emit = defineEmits<{ cancelled: [message: string] }>();
const { t } = useI18n();

const form = useForm({
  validationSchema: toTypedSchema(
    z.object({
      rdvCode: z.string({ message: t("validation.champRequis")}).min(6, { message: t("annulation.codeObligatoire") }),
    }),
  ),
});

const { value: rdvCode, errorMessage: rdvCodeError } = useField<string>("rdvCode");
const { handleSubmit } = form;
const esiriusStore = useEsiriusStore();
const { loading } = storeToRefs(esiriusStore);
const confirmDialog = ref(false);
const pendingCode = ref("");
const captchaToken = ref("");
const captchaKey = ref(0);
const alertMessage = ref("");
const alertType = ref<"success" | "error">("success");

const captchaSiteKey = getCaptchaSitekey() || "";

const isSubmitting = computed(() => loading.value);
const isSubmitDisabled = computed(() => isSubmitting.value || !captchaToken.value);

const showAlert = (message: string, type: "success" | "error") => {
  alertMessage.value = message;
  alertType.value = type;
};

const submitCancellation = async (code: string) => {
  try {
    await esiriusStore.cancelAppointment(code);
    const message = t("annulation.messageSucces", { code });
    showAlert(message, "success");
    emit("cancelled", message);
    resetCaptcha();
  } catch (error) {
    showAlert(t("annulation.messageErreur"), "error");
    resetCaptcha();
  }
};

const onSubmit = handleSubmit(formValues => {
  pendingCode.value = formValues.rdvCode.trim();
  confirmDialog.value = true;
});

const handleConfirm = async () => {
  const code = pendingCode.value;
  if (!code) {
    return;
  }
  confirmDialog.value = false;
  const exists = await verifyRdvCodeExists(code);
  if (!exists) {
    return;
  }
  await submitCancellation(code);
};

const verifyRdvCodeExists = async (code: string) => {
  try {
    await esiriusStore.getAppointmentByCode(code);
    return true;
  } catch {
    showAlert(t("annulation.codeInexistant"), "error");
    resetCaptcha();
    return false;
  }
};

const resetCaptcha = () => {
  captchaToken.value = "";
  captchaKey.value += 1;
};
</script>
