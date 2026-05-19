<template>
  <v-container fluid>
    <div class="top-bar mb-4">
      <div class="top-bar-left">
        <h1 class="text-h1 text-md-h2 d-md-none mobile-title">
          {{ t("titreApplication.prePlainte") }}
        </h1>

        <div class="d-flex d-md-none justify-space-between align-center mobile-stepper">
          <v-chip color="primary" variant="outlined" size="large">
            {{ t("common.etape") }} {{ step > 6 ? 6 : step }} {{ t("common.sur") }} 6
          </v-chip>
          <div class="d-flex align-center gap-2">
            <div v-for="i in 6" :key="i" class="step-dot" :class="{ active: i <= step, completed: i < step }"></div>
          </div>
        </div>
      </div>

      <LanguageSwitcher />
    </div>

    <StepLoadingSkeleton v-if="isLoading" :step="step" />

    <v-row v-else>
      <v-col cols="12" md="8">
        <Disclaimer v-if="step === 1" :key="`step-${step}`" @continue="handleContinue" />
        <VerificationEmail v-if="step === 2" :key="`step-${step}`" @cancel="handleCancel" @continue="handleContinue" />
        <InfosPersonnelles
          v-if="step === 3"
          :key="`step-${step}`"
          @cancel="handleCancel"
          @continue="handleContinue"
          @save="handleSave"
        />
        <InfosEvenement
          v-if="step === 4"
          :key="`step-${step}`"
          @cancel="handleCancel"
          @continue="handleContinue"
          @save="handleSave"
        />
        <RendezVous
          v-if="step === 5"
          :key="`step-${step}`"
          @cancel="handleCancel"
          @continue="handleContinue"
          @save="handleSave"
        />
        <Recapitulatif v-if="step === 6" :key="`step-${step}`" @continue="handleContinue" @cancel="handleContinue" />
        <ValidationCard v-if="step === 7" :key="`step-${step}`" @restart="handleRestart" />
        <SaveAndQuitForm ref="saveDialog" />
      </v-col>
      <v-col md="4" class="d-none d-md-block">
        <StepperPrePlainte />
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import Disclaimer from "@/views/Disclaimer.vue";
import VerificationEmail from "@/views/VerificationEmail.vue";
import InfosPersonnelles from "@/views/InfosPersonnelles.vue";
import StepperPrePlainte from "@/components/stepper/StepperPrePlainte.vue";
import { useCreatePrePlainteStore } from "@/stores/createPrePlainteStore";
import { storeToRefs } from "pinia";
import InfosEvenement from "@/views/InfosEvenement.vue";
import ValidationCard from "@/views/ValidationCard.vue";
import Recapitulatif from "@/views/Recapitulatif.vue";
import RendezVous from "@/views/RendezVous.vue";
import { nextTick, onMounted, ref, watch } from "vue";
import SaveAndQuitForm from "@/components/dialogs/SaveAndQuitForm.vue";
import { SauvegardeService } from "@/services/sauvegardeService";
import { useRoute, useRouter } from "vue-router";
import StepLoadingSkeleton from "@/components/loading/StepLoadingSkeleton.vue";
import { preplainteDtoToForm } from "@/utils/preplainteFormatBuilder";
import { useI18n } from "vue-i18n";
import LanguageSwitcher from "@/components/actions/LanguageSwitcher.vue";

const { t } = useI18n();
const store = useCreatePrePlainteStore();
const { step, isLoading } = storeToRefs(store);
const saveDialog = ref<InstanceType<typeof SaveAndQuitForm> | null>(null);

const handleCancel = () => {
  store.prevStep();
};

const handleContinue = () => {
  store.nextStep();
};

const handleRestart = () => {
  store.clearAllData();
};

const handleSave = () => {
  saveDialog.value?.open();
};

const handleRecup = async (code: string) => {
  const dto = await SauvegardeService.reprendrePreplainte(code);
  const mapped = preplainteDtoToForm(dto, store.userFormData);
  store.$patch(s => {
    Object.assign(s.userFormData, mapped);
  });
};

async function verifierCodeDanslUrl() {
  const route = useRoute();
  const router = useRouter();
  const demandeId = route.query.demandeId;

  if (typeof demandeId === "string" && demandeId.trim() !== "") {
    await handleRecup(demandeId);
    const { demandeId: _omit, ...rest } = route.query;
    router.replace({ query: { ...rest } });
  }
}

onMounted(async () => {
  await verifierCodeDanslUrl();
});

watch(step, () => {
  nextTick(() => {
    window.scrollTo({ top: 0, behavior: "smooth" });
  });
});
</script>

<style scoped>
.gap-2 {
  gap: 8px;
}

.top-bar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  flex-wrap: wrap;
}

.top-bar-left {
  flex: 1 1 auto;
  min-width: 0;
}

.mobile-title {
  margin-bottom: 28px;
}

.mobile-stepper {
  margin-bottom: 16px;
}

@media (max-width: 959px) {
  .top-bar {
    align-items: flex-start;
  }
}
</style>
