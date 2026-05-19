<template>
  <v-dialog v-model="isOpen" max-width="500">
    <v-card class="px-3 py-3" color="surface-container-high">
      <v-card-title class="text-h6">
        {{ t("dialogs.sauvegarderTitre") }}
      </v-card-title>
      <v-card-text class="px-4">
        {{ t("dialogs.sauvegarderTexte") }}
      </v-card-text>
      <v-radio-group class="mt-3 ms-3" v-model="enregistrer">
        <v-row>
          <v-col cols="auto">
            <v-radio color="primary" :value="true" :label="t('common.oui')"></v-radio>
          </v-col>
          <v-col cols="auto">
            <v-radio color="primary" :value="false" :label="t('common.non')"></v-radio>
          </v-col>
        </v-row>
      </v-radio-group>
      <v-card-subtitle class="mb-3" v-if="enregistrer == true">
        {{ t("dialogs.sauvegarderEmailTitre") }}
      </v-card-subtitle>
      <v-card-subtitle class="mb-3" v-if="enregistrer == false">
        {{ t("dialogs.sauvegarderPerdues") }}
      </v-card-subtitle>
      <v-text-field
        v-if="enregistrer === true"
        :label="t('dialogs.sauvegarderEmail')"
        v-model="email"
        type="email"
        class="mb-3 px-3"
        variant="outlined"
        :error-messages="emailError ? [emailError] : []"
      />
      <div v-if="enregistrer === true && isEmailValid && email.trim().length > 0 && !isSent" class="px-3 mb-3">
        <Captcha
          :key="captchaKey"
          v-model="captchaToken"
          :sitekey="captchaSiteKey"
          data-test="captcha-brouillon"
          @reset="resetCaptcha"
        />
      </div>
      <v-card-text v-if="isSent === true" class="text-body-3">
        {{ t("dialogs.sauvegarderEmailTexte") }}
      </v-card-text>
      <v-card-actions>
        <v-row class="w-100" align="center">
          <v-col cols="12" md="auto" class="d-flex">
            <v-btn v-if="enregistrer === false" variant="plain" color="primary" @click="erase">
              {{ t("dialogs.sauvegarderEfface") }}
            </v-btn>

            <v-btn v-else-if="enregistrer === null" variant="plain" color="primary" :disabled="true">
              {{ t("dialogs.sauvegarderQuitter") }}
            </v-btn>
          </v-col>

          <v-spacer />

          <v-col cols="12" md="auto" class="d-flex justify-end">
            <v-btn variant="outlined" color="primary" class="me-4" @click="close" :disabled="isLoading">
              {{ isSent === true ? t("dialogs.sauvegarderFermer") : t("dialogs.sauvegarderAnnuler") }}
            </v-btn>

            <v-btn
              v-if="enregistrer === true && isSent === false"
              variant="flat"
              color="primary"
              :disabled="!isEmailValid || email.trim().length === 0 || !captchaToken || isLoading"
              :loading="isLoading"
              @click="send"
            >
              {{ t("dialogs.sauvegarderEnregistrer") }}
            </v-btn>
          </v-col>
        </v-row>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
<script lang="ts" setup>
import router from "@/router";
import { useCreatePrePlainteStore } from "@/stores/createPrePlainteStore";
import { SauvegardeService } from "@/services/sauvegardeService";
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import z from "zod";
import { buildPrePlainteForBackend } from "@/utils/preplainteFormatBuilder";
import Captcha from "@/components/captcha/Captcha.vue";
import { getCaptchaSitekey } from "@/config/config.ts";

const MAX_TIMEOUT = 8000;

const { t } = useI18n();
const isOpen = ref(false);
const enregistrer = ref(null);
const isSent = ref(false);
const isLoading = ref(false);

const email = ref("");
const emailError = ref<string | null>(null);
const captchaToken = ref("");
const captchaKey = ref(0);
const store = useCreatePrePlainteStore();
const schema = z.object({ email: z.string().email(t("dialogs.sauvegarderEmailValide")) });

const captchaSiteKey = getCaptchaSitekey() || "";

function resetCaptcha() {
  captchaToken.value = "";
  captchaKey.value += 1;
}

function open() {
  email.value = store.userFormData.email ?? "";
  isOpen.value = true;
  isSent.value = false;
  isLoading.value = false;
  enregistrer.value = null;
  resetCaptcha();
}

function close() {
  isOpen.value = false;
}

function erase() {
  store.clearAllData();
  router.go(0);
}

async function send() {
  if (!isEmailValid.value || !captchaToken.value) {
    return;
  }

  isLoading.value = true;

  store.setUserFormData({
    ...store.userFormData,
    email: email.value.trim(),
  });

  try {
    await SauvegardeService.sauvegarderPreplainte(
      await buildPrePlainteForBackend(store.userFormData),
      captchaToken.value,
    );
    isSent.value = true;
    setTimeout(() => {
      close();
    }, MAX_TIMEOUT);
  } catch {
    resetCaptcha();
  } finally {
    isLoading.value = false;
  }
}

function validateEmail() {
  if (enregistrer.value !== true || email.value.length === 0) {
    emailError.value = null;
    return true;
  }
  const parsed = schema.safeParse({ email: email.value.trim() });
  emailError.value = parsed.success ? null : (parsed.error.issues[0]?.message ?? null);
  return parsed.success;
}

watch([email], () => validateEmail(), { immediate: true });
const isEmailValid = computed(() => validateEmail());
defineExpose({ open, close });
</script>
