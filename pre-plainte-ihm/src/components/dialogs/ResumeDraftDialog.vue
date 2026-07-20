<template>
  <v-dialog v-model="isOpen" max-width="520" persistent>
    <v-card class="px-3 py-3" color="surface-container-high">
      <v-card-title data-cy="reprise-brouillon-dialog" class="text-h5 font-weight-medium pt-4 px-4">
        {{ t("dialogs.repriseBrouillonTitre") }}
      </v-card-title>

      <v-card-text class="px-4 text-body-1">
        {{
          formattedDateTime
            ? t("dialogs.repriseBrouillonTexte", { datetime: formattedDateTime })
            : t("dialogs.repriseBrouillonTexteSansDate")
        }}
      </v-card-text>

      <v-card-actions class="justify-end flex-wrap ga-2">
        <v-btn data-cy="reprise-brouillon-recommencer" variant="outlined" color="primary" @click="onRestart">
          {{ t("dialogs.repriseBrouillonRecommencer") }}
        </v-btn>
        <v-btn data-cy="reprise-brouillon-continuer" variant="flat" color="primary" @click="onContinue">
          {{ t("dialogs.repriseBrouillonContinuer") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import { useCreatePrePlainteStore } from "@/stores/createPrePlainteStore";

const props = defineProps<{
  savedAt: Date | null;
}>();

const emit = defineEmits<{
  continue: [];
  restart: [];
}>();

const { t, locale } = useI18n();
const store = useCreatePrePlainteStore();
const isOpen = ref(false);

const formattedDateTime = computed(() => {
  if (!props.savedAt) {
    return "";
  }

  const date = props.savedAt;
  const localeCode = locale.value?.startsWith("fr") ? "fr-CH" : locale.value;

  if (localeCode.startsWith("fr")) {
    const weekday = new Intl.DateTimeFormat(localeCode, { weekday: "long" }).format(date);
    const dayMonthYear = new Intl.DateTimeFormat(localeCode, {
      day: "numeric",
      month: "long",
      year: "numeric",
    }).format(date);
    return `${weekday} ${dayMonthYear}`;
  }

  return new Intl.DateTimeFormat(localeCode, {
    weekday: "long",
    day: "numeric",
    month: "long",
    year: "numeric",
  }).format(date);
});

function open() {
  isOpen.value = true;
}

function close() {
  isOpen.value = false;
}

function onContinue() {
  close();
  emit("continue");
}

function onRestart() {
  store.clearAllData();
  close();
  emit("restart");
}

defineExpose({ open, close });
</script>
