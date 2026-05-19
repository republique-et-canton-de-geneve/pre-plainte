<template>
  <div :class="containerClasses">
    <QuitForm ref="quitDialog" />
    <v-btn
      variant="outlined"
      color="primary"
      :class="primaryButtonClasses"
      @click="quitDialog?.open()"
    >
      {{ t("common.quitter") }}
    </v-btn>

    <v-btn
      variant="outlined"
      color="primary"
      append-icon="mdi-open-in-new"
      :class="secondaryButtonClasses"
      @click="onDemarche"
    >
      {{ t("common.demarche") }}
    </v-btn>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { useI18n } from "vue-i18n";
import QuitForm from "@/components/dialogs/QuitForm.vue";
import { DEMARCHE_PLAINTE_URL } from "@/constants/constant.ts";

const props = defineProps<{ isMobile?: boolean }>();
const { t } = useI18n();
const quitDialog = ref<InstanceType<typeof QuitForm>>();

const isMobile = computed(() => props.isMobile ?? false);

const containerClasses = computed(() =>
  isMobile.value
    ? "d-md-none d-flex flex-column align-center gap-2 w-100"
    : "d-flex align-center flex-nowrap ga-2",
);

const primaryButtonClasses = computed(() =>
  isMobile.value ? "w-100 mb-4" : "mr-2"
);

const secondaryButtonClasses = computed(() =>
  isMobile.value ? "w-100" : ""
);

const onDemarche = () => {
  window.open(DEMARCHE_PLAINTE_URL, "_blank");
};
</script>
