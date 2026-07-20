import type { PrePlainteFormFields } from "@/types/pre-plainte.interface";
import { defineStore } from "pinia";
import { reactive, ref, watch } from "vue";
import { STEP_RENDEZ_VOUS, STEPS } from "@/constants/constant";
import {
  normalizeFormNames,
  saveFormData,
  loadFormData,
  saveCurrentStep,
  loadCurrentStep,
  clearStorageData,
  isValidStep,
  getNextStep,
  getPreviousStep,
  loadEmailChallengeKey,
  saveEmailChallengeKey,
  saveLastLocalSavedAt,
  loadLastLocalSavedAt,
} from "@/utils/validations/field-validation.utils";
import { getInitialFormData } from "@/utils/form/initial-form-data";
import { isRendezVousOnlyDommage } from "@/utils/workflows/disclaimer-workflow";

export const useCreatePrePlainteStore = defineStore("createPrePlainte", () => {
  const initialFormData = getInitialFormData();
  const savedData = loadFormData();
  const userFormData = reactive<PrePlainteFormFields>({
    ...initialFormData,
    ...savedData,
    objetsVolesValides: Array.isArray(savedData.objetsVolesValides)
      ? savedData.objetsVolesValides
      : initialFormData.objetsVolesValides,
    objetsDegradesValides: Array.isArray(savedData.objetsDegradesValides)
      ? savedData.objetsDegradesValides
      : initialFormData.objetsDegradesValides,
  });

  const step = ref(loadCurrentStep());
  const isLoading = ref(false);
  const isRdvDataLoading = ref(false);
  const isStoragePersistenceEnabled = ref(true);
  const lastLocalSavedAt = ref<Date | null>(loadLastLocalSavedAt());

  const keyChallenge = ref<string | null>(loadEmailChallengeKey());

  function setKeyChallenge(key: string) {
    keyChallenge.value = key;
  }

  watch(keyChallenge, k => {
    if (isStoragePersistenceEnabled.value) {
      saveEmailChallengeKey(k);
    }
  });

  const setUserFormData = (data: PrePlainteFormFields) => {
    const normalizedData = data.nom ? normalizeFormNames(data) : data;
    Object.assign(userFormData, normalizedData);
  };

  const resetForm = () => {
    isStoragePersistenceEnabled.value = true;
    clearStorageData();
    Object.assign(userFormData, initialFormData);
    keyChallenge.value = null;
  };

  const clearAllData = (keepCurrentStep = false) => {
    isStoragePersistenceEnabled.value = true;
    clearStorageData();
    Object.assign(userFormData, initialFormData);
    keyChallenge.value = null;
    if (!keepCurrentStep) {
      step.value = 1;
    }
  };

  const clearPersistedDataAfterSubmission = () => {
    isStoragePersistenceEnabled.value = false;
    clearStorageData();
    keyChallenge.value = null;
    resetCaptchaToken();
  };

  const isRendezVousOnly = () => isRendezVousOnlyDommage(userFormData);

  const nextStep = () => {
    const next = isRendezVousOnly() && step.value === 2 ? STEP_RENDEZ_VOUS : getNextStep(step.value);
    if (next) {
      isLoading.value = true;
      setTimeout(() => {
        step.value = next;
        isLoading.value = false;
      }, 1000);
      return true;
    }
    return false;
  };

  const prevStep = () => {
    const prev = isRendezVousOnly() && step.value === STEP_RENDEZ_VOUS ? 2 : getPreviousStep(step.value);
    if (prev) {
      step.value = prev;
      return true;
    }
    return false;
  };

  const setStep = (targetStep: number) => {
    if (isValidStep(targetStep)) {
      step.value = targetStep;
      return true;
    }
    return false;
  };

  const captchaToken = ref("");

  const setCaptchaToken = (token: string) => {
    captchaToken.value = token;
  };

  const resetCaptchaToken = () => {
    captchaToken.value = "";
  };

  const currentAppointment = ref<any>(null);

  const setCurrentAppointment = (appointment: any) => {
    currentAppointment.value = appointment;
  };

  const clearCurrentAppointment = () => {
    currentAppointment.value = null;
  };

  const demandeId = ref<string | null>(null);

  const setDemandeId = (id: string | null) => {
    demandeId.value = id;
  };

  watch(step, currentStep => {
    if (isStoragePersistenceEnabled.value) {
      saveCurrentStep(currentStep);
    }
  });
  watch(
    userFormData,
    formData => {
      if (isStoragePersistenceEnabled.value) {
        saveFormData(formData);
        const now = new Date();
        lastLocalSavedAt.value = now;
        saveLastLocalSavedAt(now);
      }
    },
    { deep: true },
  );

  return {
    setUserFormData,
    userFormData,
    step,
    steps: STEPS,
    isLoading,
    isRdvDataLoading,
    lastLocalSavedAt,
    nextStep,
    prevStep,
    setStep,
    resetForm,
    clearAllData,
    clearPersistedDataAfterSubmission,
    captchaToken,
    setCaptchaToken,
    resetCaptchaToken,
    keyChallenge,
    setKeyChallenge,
    currentAppointment,
    setCurrentAppointment,
    clearCurrentAppointment,
    demandeId,
    setDemandeId,
    isRendezVousOnly,
  };
});
