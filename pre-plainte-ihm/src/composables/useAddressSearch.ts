import { computed, ref } from "vue";
import { useQuery } from "@tanstack/vue-query";
import { searchAddresses } from "@/services/addresseService.ts";

const SEARCH_DEBOUNCE_MS = 300;
const MIN_SEARCH_LENGTH = 3;
const DEFAULT_COUNTRY = "CH";
const DEFAULT_LIMIT = 10;
const MINUTES_TO_MILLISECONDS = 60 * 1000;
const CACHE_DURATION_MINUTES = 5;
const GC_TIME_MS = CACHE_DURATION_MINUTES * MINUTES_TO_MILLISECONDS;
const RETRY_COUNT = 1;
const RETRY_DELAY_MS = 1000;

export function useAddressSearch(instanceId = "default") {
  const searchText = ref("");
  const selectedCountry = ref(DEFAULT_COUNTRY);
  const debounceTimeout = ref<number | undefined>(undefined);

  const debouncedSearch = (query: string) => {
    clearTimeout(debounceTimeout.value);
    debounceTimeout.value = globalThis.setTimeout(() => {
      searchText.value = query.trim();
    }, SEARCH_DEBOUNCE_MS);
  };

  const queryKey = computed(() => ["addresses", instanceId, searchText.value, selectedCountry.value]);
  const isSearchEnabled = computed(() => searchText.value.length >= MIN_SEARCH_LENGTH);

  const {
    data: addresses,
    isLoading,
    isError,
    error,
    isFetching,
  } = useQuery({
    queryKey,
    queryFn: () =>
      searchAddresses({
        searchText: searchText.value,
        limit: DEFAULT_LIMIT,
        country: selectedCountry.value,
      }),
    enabled: isSearchEnabled,
    staleTime: 0,
    gcTime: GC_TIME_MS,
    refetchOnWindowFocus: false,
    refetchOnMount: false,
    retry: RETRY_COUNT,
    retryDelay: RETRY_DELAY_MS,
  });

  const search = (query: string) => {
    if (query == null) {
      return;
    }

    const normalized = query.trim();

    if (normalized.length >= MIN_SEARCH_LENGTH) {
      debouncedSearch(normalized);
      return;
    }

    clearTimeout(debounceTimeout.value);
    searchText.value = "";
  };

  const setCountry = (country: string) => {
    selectedCountry.value = country;
  };

  const reset = () => {
    clearTimeout(debounceTimeout.value);
    searchText.value = "";
  };

  return {
    addresses: computed(() => addresses.value ?? []),
    isLoading: computed(() => isLoading.value || isFetching.value),
    isError: computed(() => isError.value),
    error: computed(() => error.value),
    search,
    setCountry,
    reset,
    searchText: computed(() => searchText.value),
    selectedCountry: computed(() => selectedCountry.value),
  };
}
