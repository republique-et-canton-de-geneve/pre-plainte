<template>
  <v-sheet variant="outlined" class="address-lookup-sheet mb-4 w-100" rounded="lg">
    <v-autocomplete
      :model-value="displayValue"
      :items="prioritizedCountryOptions"
      :loading="countriesLoading"
      :label="t('informationsPersonnelles.pays')"
      variant="outlined"
      class="address-lookup-autocomplete mb-4"
      item-title="label"
      item-value="value"
      clearable
      persistent-clear
      hide-no-data
      @update:model-value="onCountryChange"
    >
      <template #prepend-inner>
        <v-icon color="primary">mdi-earth</v-icon>
      </template>

      <template #clear="{ props }">
        <button
          type="button"
          class="v-field__clearable"
          :aria-label="t('informationsPersonnelles.effacerPays')"
          @click="props.onClick"
          @keydown.enter.stop.prevent="props.onClick"
        >
          <v-icon size="small">mdi-close</v-icon>
        </button>
      </template>
    </v-autocomplete>

    <v-autocomplete
      v-if="canSearchAddresses"
      :model-value="selectedAddress"
      ref="addressAcRef"
      :items="addresses"
      :loading="isLoading"
      :label="t('informationsPersonnelles.rechercherAdresse')"
      :placeholder="t('informationsPersonnelles.tapezCaracteres')"
      :aria-describedby="'autocomplete-hint'"
      variant="outlined"
      class="address-lookup-autocomplete"
      clearable
      persistent-clear
      item-title="label"
      return-object
      no-filter
      @update:search="onSearchChange"
      @update:model-value="onAddressSelect"
    >
      <template #prepend-inner>
        <v-icon color="primary">mdi-magnify</v-icon>
      </template>

      <template #item="{ props: itemProps, item }">
        <v-list-item
          v-bind="itemProps"
          :title="item.raw?.label || `${item.raw?.street} ${item.raw?.streetNumber || ''}`"
          :subtitle="`${item.raw?.postalCode || ''} ${item.raw?.locality || ''}`"
        >
          <template #prepend>
            <v-icon color="primary">mdi-map-marker</v-icon>
          </template>
        </v-list-item>
      </template>

      <template #no-data>
        <div class="px-4 py-2 text-grey">
          {{
            searchText.length < 3
              ? t("informationsPersonnelles.tapezCaracteresRechercher")
              : t("informationsPersonnelles.aucuneAdresseTrouvee")
          }}
        </div>
      </template>
      <template #clear="{ props }">
        <button
          type="button"
          class="v-field__clearable"
          :aria-label="t('informationsPersonnelles.effacerAdresse')"
          @click="props.onClick"
          @keydown.enter.stop.prevent="props.onClick"
        >
          <v-icon size="small">mdi-close</v-icon>
        </button>
      </template>
    </v-autocomplete>

    <div id="autocomplete-hint" class="sr-only">
      {{ t("informationsPersonnelles.hintAutocompletion") }}
    </div>

    <div v-if="canSearchAddresses && selectedAddress" class="text-success text-caption mb-2">
      <v-icon size="small" color="success">mdi-check-circle</v-icon>
      {{ t("informationsPersonnelles.adresseSelectionnee") }}
    </div>

    <div v-if="canSearchAddresses && !selectedAddress" class="text-success text-caption mb-2">
      <v-icon size="small" color="success">mdi-check-circle</v-icon>
      {{ t("informationsPersonnelles.rechercheAdresseDisponible") }}
    </div>

    <div v-if="!canSearchAddresses" class="text-info text-caption mb-2">
      <v-icon size="small" color="info">mdi-information</v-icon>
      {{ t("informationsPersonnelles.saisieManuelleRequise") }}
    </div>

    <div v-if="isError" class="text-error text-caption mb-2">
      <v-icon size="small" color="error">mdi-alert-circle</v-icon>
      {{ t("informationsPersonnelles.erreurRechercheAdresses") }}
    </div>

    <div v-if="countriesError" class="text-error text-caption mb-2">
      <v-icon size="small" color="error">mdi-alert-circle</v-icon>
      {{ t("informationsPersonnelles.erreurChargementPays") }}
    </div>
  </v-sheet>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from "vue";

import { useAddressSearch } from "@/composables/useAddressSearch";
import { useCountries } from "@/composables/useCountries";
import { useI18n } from "vue-i18n";
import { RIPOL } from "@/constants/constant";
import type { AddressResult } from "@/types/adresse.interface";
import type { CountrySelection } from "@/types/country.types";

interface Emits {
  (e: "addressSelected", address: AddressResult | null): void;
  (e: "countryChanged", country: CountrySelection): void;
}

const emit = defineEmits<Emits>();
const { t } = useI18n();

const addressAcRef = ref<any>(null);
const { addresses, isLoading, isError, search, setCountry, reset, searchText } = useAddressSearch();

const {
  prioritizedCountryOptions,
  isLoading: countriesLoading,
  isError: countriesError,
  getCountryByCode,
  countries,
  canSearchAddresses: canSearch,
  getIsoCodeForAddressSearch,
} = useCountries();

const selectedAddress = ref<AddressResult | null>(null);
const selectedRipolCode = ref(RIPOL.PAYS_SUISSE);

const displayValue = computed(() => {
  if (!countries.value || countries.value.length === 0) {
    return null;
  }
  return selectedRipolCode.value;
});

watch(
  countries,
  newCountries => {
    if (newCountries && newCountries.length > 0 && selectedRipolCode.value === RIPOL.PAYS_SUISSE) {
      const country = getCountryByCode(RIPOL.PAYS_SUISSE);
      if (country) {
        emit("countryChanged", {
          ripolCode: RIPOL.PAYS_SUISSE,
          name: country.name || "Suisse",
        });
      }
    }
  },
  { immediate: true },
);

const canSearchAddresses = computed(() => canSearch(selectedRipolCode.value));

const onSearchChange = (query: string) => {
  search(query);
};

const onAddressSelect = (address: AddressResult | null) => {
  selectedAddress.value = address;

  if (address) {
    emit("addressSelected", address);
    nextTick(() => {
      const el = addressAcRef.value?.$el as HTMLElement | undefined;
      el?.querySelector("input")?.blur();
    });
  } else {
    emit("addressSelected", null);
  }
};

const onCountryChange = (ripolCode: string) => {
  selectedRipolCode.value = ripolCode;
  const country = getCountryByCode(ripolCode);
  const countryName = country?.name || "Suisse";
  const isoCode = getIsoCodeForAddressSearch(ripolCode);

  if (isoCode) {
    setCountry(isoCode);
  }

  emit("countryChanged", {
    ripolCode,
    name: countryName,
  });

  if (!canSearchAddresses.value) {
    selectedAddress.value = null;
    reset();
  }
};
</script>

<style scoped>
.sr-only{
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}
.address-lookup-sheet {
  box-sizing: border-box;
}
.address-lookup-autocomplete {
  width: 100%;
  max-width: 100%;
}
</style>
