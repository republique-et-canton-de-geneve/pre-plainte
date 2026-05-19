<template>
  <v-dialog v-model="isOpen" max-width="500">
    <v-card class="px-3 py-3" color="surface-container-high">
      <v-card-title class="text-h6 font-weight-bold">
        {{ t("dialogs.quitterTitre") }}
      </v-card-title>

      <v-card-text class="px-4">
        {{ t("dialogs.quitterTexte") }}
      </v-card-text>

      <v-card-actions class="justify-end">
        <v-btn variant="outlined" color="primary" @click="close">
          {{ t("common.annuler") }}
        </v-btn>
        <v-btn variant="flat" color="primary" @click="quit">
          {{ t("common.quitter") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useI18n } from "vue-i18n";
import router from "@/router";
import {useCreatePrePlainteStore} from "@/stores/createPrePlainteStore.ts";

const { t } = useI18n();
const isOpen = ref(false);

const store = useCreatePrePlainteStore();

function open() {
  isOpen.value = true;
}

function close() {
  isOpen.value = false;
}

function quit() {
  store.clearAllData();
  if (globalThis.history.length > 1) {
    router.back();
  } else {
    router.replace("/");
  }
}

defineExpose({ open, close });
</script>
