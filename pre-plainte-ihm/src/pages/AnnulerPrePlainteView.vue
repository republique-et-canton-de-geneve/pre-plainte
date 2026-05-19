<template>
  <v-container fluid>
    <h1 class="mb-4 text-h1 text-md-h2 d-md-none">{{ t("annulation.titre") }}</h1>
    <div class="d-flex d-md-none justify-space-between align-center mb-4">
      <v-chip color="primary" variant="outlined" size="large">
        {{ t("common.etape") }} {{ step > 2 ? 2 : step }} {{ t("common.sur") }} 4
      </v-chip>
      <div class="d-flex align-center gap-2">
        <div v-for="i in 2" :key="i" class="step-dot" :class="{ active: i <= step, completed: i < step }"></div>
      </div>
    </div>
    <StepLoadingSkeleton v-if="isLoading" :step="step" />
    <v-row v-else>
      <v-col cols="12" md="9">
        <AnnulerPrePlainteForm v-if="step === 1" :key="`step-${step}`" @cancelled="handleCancelled" />
        <ValidationCard
          v-if="step === 2"
          :key="`step-${step}`"
          :custom-message="successMessage"
          :custom-title="t('annulation.titrePersonnalise')"
          @restart="handleRestart"
        />
      </v-col>

      <v-col md="3" class="d-none d-md-block">
        <StepperPrePlainte :custom-steps="annulationSteps" :show-actions="false" />
      </v-col>
    </v-row>

    <div class="d-md-none mt-4 d-flex flex-column align-center gap-2">
      <ExitActionsForm :is-mobile="true" />
    </div>
  </v-container>
</template>

<script setup lang="ts">
import { storeToRefs } from "pinia";
import { useCreatePrePlainteStore } from "@/stores/createPrePlainteStore.ts";
import StepperPrePlainte from "@/components/stepper/StepperPrePlainte.vue";
import StepLoadingSkeleton from "@/components/loading/StepLoadingSkeleton.vue";
import ValidationCard from "@/views/ValidationCard.vue";
import AnnulerPrePlainteForm from "@/components/AnnulerPrePlainteForm.vue";
import { computed, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import ExitActionsForm from "@/components/actions/ExitActionsForm.vue";

const store = useCreatePrePlainteStore();
const { step, isLoading } = storeToRefs(store);
const successMessage = ref("");
const { t } = useI18n();
const annulationSteps = computed(() => [
  { titleKey: "annulation.etapes.code" },
  { titleKey: "annulation.etapes.confirmation" },
]);

const handleCancelled = (message: string) => {
  successMessage.value = message;
  store.setStep(2);
};

const handleRestart = () => {
  successMessage.value = "";
  store.setStep(1);
};

onMounted(() => {
  store.setStep(1);
});
</script>

<style scoped>
.gap-2 {
  gap: 8px;
}
</style>
