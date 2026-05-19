<template>
  <div v-if="!hidden">
    <p :id="descId" class="text-body-2 mb-1">
      {{ t("common.descriptionCaptcha") }}
    </p>

    <div ref="captchaContainer" class="frc-captcha" :data-test="dataTest" :aria-describedby="descId" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from "vue";
import { useI18n } from "vue-i18n";
import {
  FriendlyCaptchaSDK,
  type WidgetHandle,
  type FRCWidgetCompleteEvent,
  type FRCWidgetErrorEventData,
  type FRCWidgetStateChangeEventData,
} from "@friendlycaptcha/sdk";

const { t } = useI18n();
const descId = "captcha-desc";

interface Props {
  sitekey: string;
  apiEndpoint?: string;
  modelValue?: string;
  hidden?: boolean;
  dataTest?: string;
}

const props = withDefaults(defineProps<Props>(), {
  apiEndpoint: "https://eu.frcapi.com/api/v2",
  modelValue: "",
  hidden: false,
});

const emit = defineEmits<{
  (e: "update:modelValue", value: string): void;
  (e: "solved", value: string): void;
  (e: "error", error: unknown): void;
  (e: "reset"): void;
  (e: "state-change", state: string): void;
}>();

const captchaContainer = ref<HTMLElement>();
const widget = ref<WidgetHandle | null>();

const sdk = new FriendlyCaptchaSDK();

onMounted(() => {
  if (!captchaContainer.value) {
    return;
  }
  widget.value = sdk.createWidget({
    element: captchaContainer.value,
    sitekey: props.sitekey,
    apiEndpoint: props.apiEndpoint,
  });

  captchaContainer.value.addEventListener("frc:widget.complete", e => {
    const token = (e as FRCWidgetCompleteEvent).detail.response;
    emit("update:modelValue", token);
    emit("solved", token);
  });

  captchaContainer.value.addEventListener("frc:widget.error", e => {
    const detail = (e as CustomEvent<FRCWidgetErrorEventData>).detail;
    const err = detail?.error;
    console.error("[PPL captcha] Erreur widget FriendlyCaptcha", {
      error: err instanceof Error ? err.message : err,
      sitekeyPresent: Boolean(props.sitekey),
      apiEndpoint: props.apiEndpoint,
    });
    emit("error", detail?.error);
  });

  captchaContainer.value.addEventListener("frc:widget.statechange", e => {
    emit("state-change", (e as CustomEvent<FRCWidgetStateChangeEventData>).detail.state);
  });

  captchaContainer.value.addEventListener("frc:widget.expire", () => {
    emit("reset");
  });
});

onUnmounted(() => {
  widget.value?.destroy();
  widget.value = null;
});
</script>

<style scoped>
.frc-captcha {
  width: 100%;
  min-height: 80px;
  display: flex;
  justify-content: center;
}
</style>
