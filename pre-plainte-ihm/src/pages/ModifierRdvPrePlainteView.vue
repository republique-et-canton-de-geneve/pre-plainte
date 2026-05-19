<template>
  <v-container fluid>
    <h1 class="mb-4 text-h1 text-md-h2 d-md-none">{{ t("modification.titre") }}</h1>
    <div class="d-flex d-md-none justify-space-between align-center mb-4">
      <v-chip color="primary" variant="outlined" size="large">
        {{ t("common.etape") }} {{ step > 3 ? 3 : step }} {{ t("common.sur") }} 3
      </v-chip>
      <div class="d-flex align-center gap-2">
        <div v-for="i in 3" :key="i" class="step-dot" :class="{ active: i <= step, completed: i < step }"></div>
      </div>
    </div>
    <StepLoadingSkeleton v-if="isLoading" :step="step" />
    <v-row v-else>
      <v-col cols="12" md="9">
        <ModifierRendezVousForm
          v-if="step <= 2"
          :key="`step-${step}`"
          :current-step="step"
          @code-verified="handleCodeVerified"
          @creneau-selected="handleCreneauSelected"
          @updated="handleUpdated"
          @cancel="handleCancel"
        />
        <ValidationCard
          v-if="step === 3"
          :key="`step-${step}`"
          :custom-message="successMessage"
          :custom-title="t('modification.titrePersonnalise')"
          @restart="handleRestart"
        />
      </v-col>

      <v-col md="3" class="d-none d-md-block">
        <StepperPrePlainte :custom-steps="modificationSteps" :show-actions="false" />
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
import ModifierRendezVousForm from "@/components/ModifierRendezVousForm.vue";
import { computed, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";
import ExitActionsForm from "@/components/actions/ExitActionsForm.vue";

const store = useCreatePrePlainteStore();
const { step, isLoading } = storeToRefs(store);
const successMessage = ref("");
const { t } = useI18n();
const modificationSteps = computed(() => [
  { title: t("modification.etapes.code") },
  { title: t("modification.etapes.creneau") },
  { title: t("modification.etapes.validation") },
]);

const handleCodeVerified = () => {
  store.setStep(2);
};

const handleCreneauSelected = () => {
  store.setStep(3);
};

const handleUpdated = (message: string) => {
  successMessage.value = message;
  isLoading.value = false;
  store.setStep(3);
};

const handleCancel = () => {
  if (step.value > 1) {
    store.setStep(step.value - 1);
  }
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
