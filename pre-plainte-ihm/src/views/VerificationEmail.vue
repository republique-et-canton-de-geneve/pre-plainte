<template>
  <v-form @submit.prevent="onSubmit">
    <h1 class="mb-5 text-h1 text-md-h2 d-none d-md-block">{{ t("titreApplication.prePlainte") }}</h1>
    <v-card class="pa-2 pa-md-8" elevation="1">
      <h2 class="pre-plainte-main-card-title text-h2 mb-4">{{ t("steps.verificationEmail") }}</h2>

      <v-alert type="info" class="mb-6" density="comfortable">
        <div class="text-body-2 text-md-body-1">
          {{ t("emailChallenge.etapeVerificationIntro") }}
        </div>
      </v-alert>

      <v-text-field
        :label="t('informationsPersonnelles.email')"
        v-model="email"
        type="email"
        :error-messages="emailError"
        class="mb-2"
        variant="outlined"
        :hint="t('informationsPersonnelles.hintEmail')"
        persistent-hint
        autocomplete="email"
      />

      <v-btn
        variant="flat"
        color="primary"
        class="mb-4"
        :disabled="!emailValide || codeRequestLoading"
        :loading="codeRequestLoading"
        @click="onRequestCode"
      >
        {{ t("emailChallenge.envoyerCodeSecurite") }}
      </v-btn>

      <v-alert v-if="sendError" type="error" class="mb-4" density="comfortable" closable @click:close="sendError = ''">
        {{ sendError }}
      </v-alert>

      <v-alert v-if="sendInfo" type="info" class="mb-4" density="comfortable" closable @click:close="sendInfo = ''">
        {{ sendInfo }}
      </v-alert>

      <EmailChallengeOtpSection
        v-if="codeSent"
        v-model="confirmationEmail"
        :error-message="confirmationEmailError ?? ''"
        :email="emailDisplay"
        :autofocus="true"
      />

      <v-alert
        v-if="verifyError"
        type="error"
        class="mb-0 mt-4"
        density="comfortable"
        closable
        @click:close="verifyError = ''"
      >
        {{ verifyError }}
      </v-alert>

      <div class="pre-plainte-mobile-step-actions d-md-none mt-4 d-flex flex-column gap-4 mb-2">
        <v-btn variant="outlined" color="primary" class="w-100" @click="emit('cancel')">
          {{ t("common.precedent") }}
        </v-btn>
        <v-btn
          color="primary"
          variant="flat"
          class="w-100"
          :disabled="!canContinue || verifySubmitLoading"
          :loading="verifySubmitLoading"
          type="submit"
        >
          {{ t("common.continuer") }}
        </v-btn>
      </div>
    </v-card>

    <v-row class="mt-4 d-none d-md-flex" align="center" justify="end">
      <v-col cols="12" md="auto" class="d-flex justify-end flex-wrap">
        <v-btn variant="outlined" color="primary" class="me-4" @click="emit('cancel')">
          {{ t("common.precedent") }}
        </v-btn>
        <v-btn
          color="primary"
          variant="flat"
          size="large"
          :disabled="!canContinue || verifySubmitLoading"
          :loading="verifySubmitLoading"
          type="submit"
        >
          {{ t("common.continuer") }}
        </v-btn>
      </v-col>
    </v-row>

    <div class="d-md-none mt-4 d-flex flex-column gap-2">
      <ExitActionsForm :is-mobile="true" />
    </div>
  </v-form>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useField, useForm } from "vee-validate";
import { toTypedSchema } from "@vee-validate/zod";
import type { PrePlainteFormFields } from "@/types/pre-plainte.interface.ts";
import { useCreatePrePlainteStore } from "@/stores/createPrePlainteStore.ts";
import { storeToRefs } from "pinia";
import EmailChallengeOtpSection from "@/components/email/EmailChallengeOtpSection.vue";
import ExitActionsForm from "@/components/actions/ExitActionsForm.vue";
import {
  EmailChallengeTooManyRequestsError,
  requestEmailChallengeCode,
  verifyEmailChallengeCode,
} from "@/services/challengeEmailService";
import type { VerifyStatus } from "@/services/challengeEmailService";
import {
  getEmailChallengeCodeValidationMessage,
  isValidEmailChallengeCodeFormat,
  sanitizeEmailChallengeCodeInput,
} from "@/utils/validations/field-validation.utils";
import { createVerificationEmailPageSchema } from "@/schemas/verification-email-page.schema.ts";
import { isDevEmailChallengeBypassed } from "@/config/dev-flags";
import { EMAIL_CHALLENGE_CODE_LENGTH } from "@/constants/constant";

const devBypassEmail = isDevEmailChallengeBypassed();
const EMAIL_CHALLENGE_GENERIC_ERROR_KEY = "emailChallenge.erreurVerification";

const { t, locale } = useI18n();
const store = useCreatePrePlainteStore();
const { keyChallenge } = storeToRefs(store);
const emit = defineEmits<{ cancel: []; continue: [] }>();

type VerificationStepFields = Pick<PrePlainteFormFields, "email" | "confirmationEmail">;

const verificationEmailPageSchema = computed(() => {
  return createVerificationEmailPageSchema(t);
});

const validationSchema = computed(() => toTypedSchema(verificationEmailPageSchema.value));

const { handleSubmit, validate } = useForm<VerificationStepFields>({
  initialValues: {
    email: store.userFormData.email ?? "",
    confirmationEmail: store.userFormData.confirmationEmail ?? "",
  },
  validationSchema,
});

const codeSent = ref(false);
const codeRequestLoading = ref(false);
const sendError = ref("");
const sendInfo = ref("");
const verifyError = ref("");
const verifySubmitLoading = ref(false);
const challengeEmailSnapshot = ref("");

const { value: email, errorMessage: emailError } = useField<string>("email");

const emailDisplay = computed(() => (email.value ?? "").trim());

const emailValide = computed(() => verificationEmailPageSchema.value.safeParse({ email: email.value ?? "" }).success);

const { value: confirmationEmail, errorMessage: confirmationEmailError } = useField<string>(
  "confirmationEmail",
  value => {
    if (!codeSent.value) {
      return true;
    }
    return getEmailChallengeCodeValidationMessage(String(value ?? ""), t) ?? true;
  },
  { validateOnValueUpdate: true },
);

const hasSendError = computed(() => Boolean(sendError.value?.trim()));

function messageForVerifyStatus(status: VerifyStatus): string {
  switch (status) {
    case "INVALID":
      return t("emailChallenge.codeInvalide");
    case "EXPIRED":
      return t("emailChallenge.codeExpire");
    case "LOCKED":
      return t("emailChallenge.tropDeTentatives");
    case "NOT_FOUND":
      return t("emailChallenge.challengeIntrouvable");
    default:
      return t("emailChallenge.erreurVerification");
  }
}

const canContinue = computed(() => {
  if (!hasSendError.value && devBypassEmail && emailValide.value) {
    return true;
  }
  return (
    !hasSendError.value &&
    codeSent.value &&
    emailValide.value &&
    isValidEmailChallengeCodeFormat(String(confirmationEmail.value ?? ""))
  );
});

watch(
  () => (email.value ?? "").trim(),
  newVal => {
    sendError.value = "";
    sendInfo.value = "";
    verifyError.value = "";
    if (!codeSent.value || !challengeEmailSnapshot.value) {
      return;
    }
    if (newVal !== challengeEmailSnapshot.value) {
      codeSent.value = false;
      confirmationEmail.value = "";
      challengeEmailSnapshot.value = "";
    }
  },
);

watch(confirmationEmail, () => {
  verifyError.value = "";
});

watch(locale, () => {
  validate();
});

async function ensureKeyAndSend(): Promise<void> {
  const key = keyChallenge.value ?? crypto.randomUUID();
  if (!keyChallenge.value) {
    store.setKeyChallenge(key);
  }
  await requestEmailChallengeCode(emailDisplay.value, key);
}

const onRequestCode = async () => {
  sendError.value = "";
  sendInfo.value = "";
  verifyError.value = "";
  if (!emailValide.value) {
    return;
  }
  codeRequestLoading.value = true;
  try {
    await ensureKeyAndSend();
    codeSent.value = true;
    challengeEmailSnapshot.value = emailDisplay.value;
  } catch (e) {
    if (e instanceof EmailChallengeTooManyRequestsError) {
      codeSent.value = true;
      challengeEmailSnapshot.value = emailDisplay.value;
      sendInfo.value = e.message;
      return;
    }
    sendError.value = e instanceof Error ? e.message : t(EMAIL_CHALLENGE_GENERIC_ERROR_KEY);
  } finally {
    codeRequestLoading.value = false;
  }
};

onMounted(() => {
  const em = emailDisplay.value;
  if (keyChallenge.value && em) {
    codeSent.value = true;
    challengeEmailSnapshot.value = em;
  }
});

function persistVerifiedEmail(emailTrim: string, confirmation: string) {
  store.setUserFormData({
    ...store.userFormData,
    email: emailTrim,
    confirmationEmail: confirmation,
  });
  emit("continue");
}

function ensureDevBypassKeyChallenge() {
  if (!keyChallenge.value) {
    store.setKeyChallenge(crypto.randomUUID());
  }
}

function resolveDevBypassConfirmation(confirmation: string): string {
  if (isValidEmailChallengeCodeFormat(confirmation)) {
    return confirmation;
  }
  return "0".repeat(EMAIL_CHALLENGE_CODE_LENGTH);
}

function getKeyChallengeOrError(): { ok: true; key: string } | { ok: false; message: string } {
  const key = (keyChallenge.value ?? "").trim();
  if (key) {
    return { ok: true, key };
  }
  return { ok: false, message: t("emailChallenge.challengeIntrouvable") };
}

async function submitEmailVerification(values: VerificationStepFields) {
  if (sendError.value?.trim()) {
    return;
  }
  verifyError.value = "";

  const emailTrim = (values.email ?? "").trim();
  let confirmation = sanitizeEmailChallengeCodeInput(values.confirmationEmail ?? "");

  if (devBypassEmail) {
    ensureDevBypassKeyChallenge();
    persistVerifiedEmail(emailTrim, resolveDevBypassConfirmation(confirmation));
    return;
  }

  const keyResult = getKeyChallengeOrError();
  if (!keyResult.ok) {
    verifyError.value = keyResult.message;
    return;
  }

  verifySubmitLoading.value = true;
  try {
    const result = await verifyEmailChallengeCode(emailTrim, keyResult.key, confirmation);
    if (!result.success) {
      verifyError.value = messageForVerifyStatus(result.status);
      return;
    }
    persistVerifiedEmail(emailTrim, confirmation);
  } catch (e) {
    verifyError.value = e instanceof Error ? e.message : t(EMAIL_CHALLENGE_GENERIC_ERROR_KEY);
  } finally {
    verifySubmitLoading.value = false;
  }
}

const onSubmit = handleSubmit(submitEmailVerification);
</script>
