import { computed } from "vue";
import { useQuery } from "@tanstack/vue-query";
import { CountryService } from "@/services/countryService";
import { RIPOL } from "@/constants/constant";

const QUERY_CONFIG = {
  STALE_TIME: 1000 * 60 * 60 * 24,
  RETRY_COUNT: 2,
} as const;

const PRIORITY_CODES = [
  RIPOL.PAYS_SUISSE,
  RIPOL.PAYS_FRANCE,
  RIPOL.PAYS_ALLEMAGNE,
  RIPOL.PAYS_ITALIE,
  RIPOL.PAYS_AUTRICHE,
];

export function useCountries() {
  const {
    data: countries,
    isLoading,
    isError,
    error,
  } = useQuery({
    queryKey: ["countries"],
    queryFn: () => CountryService.getCountries(),
    staleTime: QUERY_CONFIG.STALE_TIME,
    retry: QUERY_CONFIG.RETRY_COUNT,
  });

  const countryOptions = computed(() => {
    if (!countries.value) {
      return [];
    }

    return countries.value.map(country => ({
      label: country.name,
      value: country.code,
      title: country.name,
    }));
  });

  const prioritizedCountryOptions = computed(() => {
    if (!countries.value) {
      return [];
    }

    const priorityCountries = countries.value
      .filter(c => PRIORITY_CODES.includes(c.code))
      .sort((a, b) => PRIORITY_CODES.indexOf(a.code) - PRIORITY_CODES.indexOf(b.code));

    const otherCountries = countries.value
      .filter(c => !PRIORITY_CODES.includes(c.code))
      .sort((a, b) => a.name.localeCompare(b.name, "fr"));

    return [...priorityCountries, ...otherCountries].map(country => ({
      label: country.name,
      value: country.code,
      title: country.name,
    }));
  });

  const getCountryByCode = (code: string) =>
    countries.value?.find(country => country.code === code);

  const canSearchAddresses = (ripolCode: string): boolean =>
    CountryService.canSearchAddresses(ripolCode);

  const getIsoCodeForAddressSearch = (ripolCode: string): string | null =>
    CountryService.getIsoCodeForAddressSearch(ripolCode);

  return {
    countries,
    countryOptions,
    prioritizedCountryOptions,
    isLoading,
    isError,
    error,
    getCountryByCode,
    canSearchAddresses,
    getIsoCodeForAddressSearch,
  };
}
