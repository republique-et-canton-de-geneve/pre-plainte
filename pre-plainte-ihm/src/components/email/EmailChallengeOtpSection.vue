<template>
  <v-card
    class="confirmation-card email-challenge-otp-section w-100 mb-8"
    :elevation="isDarkMode ? VUETIFY_CARD_ELEVATION_DARK : VUETIFY_CARD_ELEVATION_DEFAULT"
    :variant="isDarkMode ? 'tonal' : 'flat'"
  >
    <v-card-text v-if="showOtpInput" class="pa-2 pa-md-6">
      <div v-if="email?.trim()" class="text-subtitle-1 text-md-h6 mb-2">
        {{ t("emailChallenge.codeVerificationEnvoyeAdresse") }}
        <span class="font-weight-bold text-primary text-wrap">{{ email.trim() }}</span>
      </div>
      <div class="text-body-2 text-medium-emphasis mb-4">
        {{ t("informationsPersonnelles.codeEmailHint") }}
      </div>

      <div class="d-flex justify-center justify-md-start w-100">
        <v-otp-input
          :model-value="modelValue"
          class="email-challenge-otp-input mt-1 ms-n2"
          :length="EMAIL_CHALLENGE_CODE_LENGTH"
          placeholder="0"
          variant="underlined"
          type="number"
          :autofocus="autofocus"
          :error="!!errorMessage"
          @update:model-value="onUpdate"
        />
      </div>

      <div v-if="errorMessage" class="text-error text-caption mt-2" role="alert">
        {{ errorMessage }}
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { useTheme } from "vuetify";
import {
  EMAIL_CHALLENGE_CODE_LENGTH,
  VUETIFY_CARD_ELEVATION_DARK,
  VUETIFY_CARD_ELEVATION_DEFAULT,
} from "@/constants/constant";
import { sanitizeEmailChallengeCodeInput } from "@/utils/validations/field-validation.utils";

const { t } = useI18n();
const theme = useTheme();
const isDarkMode = computed(() => theme.global.current.value.dark);

withDefaults(
  defineProps<{
    modelValue: string;
    errorMessage?: string;
    email?: string;
    autofocus?: boolean;
    showOtpInput?: boolean;
  }>(),
  {
    errorMessage: "",
    email: "",
    autofocus: true,
    showOtpInput: true,
  },
);

const emit = defineEmits<(e: "update:modelValue", value: string) => void>();

const onUpdate = (v: string | null | undefined) => {
  emit("update:modelValue", sanitizeEmailChallengeCodeInput(String(v ?? "")));
};
</script>
