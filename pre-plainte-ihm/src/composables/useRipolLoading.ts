import { ref, computed } from "vue";

const loadingCount = ref(0);

export function useRipolLoading() {
  const isLoading = computed(() => loadingCount.value > 0);

  const startLoading = () => {
    loadingCount.value++;
  };

  const stopLoading = () => {
    loadingCount.value = Math.max(0, loadingCount.value - 1);
  };

  const resetLoading = () => {
    loadingCount.value = 0;
  };

  return {
    isLoading,
    startLoading,
    stopLoading,
    resetLoading,
  };
}

