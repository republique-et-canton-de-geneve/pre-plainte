<template>
  <div>
    <h4 v-if="title" class="mb-2 text-h6 text-md-h5">{{ title }}</h4>

    <v-alert
      v-if="optionalAddressInfo"
      type="info"
      variant="tonal"
      density="comfortable"
      class="mb-4"
    >
      {{ optionalAddressInfo }}
    </v-alert>

    <AddressLookup
      :instance-id="instanceId"
      @address-selected="onAddressSelected"
      @country-changed="onCountryChanged"
      :class="fieldClass"
    />

    <v-text-field
      :label="formatLabel(t('informationsPersonnelles.adresse'))"
      v-model="adresse"
      :error-messages="adresseError"
      :disabled="!!selectedAddress"
      :aria-disabled="!!selectedAddress"
      :class="fieldClass"
      variant="outlined"
      :hint="adresseHint"
      persistent-hint
    />

    <v-text-field
      :label="t('informationsPersonnelles.adressePostale')"
      v-model="adressePostale"
      :error-messages="adressePostaleError"
      :disabled="!!selectedAddress"
      :aria-disabled="!!selectedAddress"
      :class="fieldClass"
      variant="outlined"
      :hint="adressePostaleHint"
      persistent-hint
    />

    <v-text-field
      :label="formatLabel(t('informationsPersonnelles.npa'))"
      v-model="npa"
      :error-messages="npaError"
      :disabled="!!selectedAddress"
      :aria-disabled="!!selectedAddress"
      :class="fieldClass"
      variant="outlined"
      :hint="npaHint"
      persistent-hint
      :required="markRequired"
    />

    <v-text-field
      :label="formatLabel(t('informationsPersonnelles.localite'))"
      v-model="localite"
      :error-messages="localiteError"
      :disabled="!!selectedAddress"
      :aria-disabled="!!selectedAddress"
      :class="fieldClass"
      variant="outlined"
      :hint="localiteHint"
      persistent-hint
      :required="markRequired"
    />
  </div>
</template>

<script lang="ts" setup>
import { computed, ref } from "vue";
import { useI18n } from "vue-i18n";
import AddressLookup from "./AddressLookup.vue";
import { RIPOL } from "@/constants/constant";
import type { AddressResult } from "@/types/adresse.interface";
import type { CountrySelection } from "@/types/country.types";
import { createHintAdresse } from "@/utils/helpers/adresseHelpers.ts";
import { requiredLabel } from "@/utils/helpers/labelHelpers";

const { t } = useI18n();

const props = withDefaults(
  defineProps<{
    title?: string;
    instanceId: string;
    adresseError?: string;
    adressePostaleError?: string;
    npaError?: string;
    localiteError?: string;
    markRequired?: boolean;
    fieldClass?: string;
    optionalAddressInfo?: string;
  }>(),
  {
    markRequired: true,
    fieldClass: "mb-3",
  },
);

const formatLabel = (label: string) => (props.markRequired ? requiredLabel(label) : label);

const adresse = defineModel<string>("adresse", { required: true });
const adressePostale = defineModel<string>("adressePostale", { required: true });
const npa = defineModel<string>("npa", { required: true });
const localite = defineModel<string>("localite", { required: true });
const pays = defineModel<string>("pays", { required: true });
const localiteCode = defineModel<string>("localiteCode", { default: "" });

const selectedAddress = ref<AddressResult | null>(null);

const canSearchEventAddresses = computed(() => pays.value === RIPOL.PAYS_SUISSE || pays.value === RIPOL.PAYS_FRANCE);

function onAddressSelected(address: AddressResult | null) {
  selectedAddress.value = address;
  if (address) {
    adresse.value = `${address.street}${address.streetNumber ? " " + address.streetNumber : ""}`;
    adressePostale.value = String(address.streetNumber || "");
    npa.value = String(address.postalCode || "");
    localite.value = address.locality || "";
    localiteCode.value = address.municipality || "";
  } else {
    adresse.value = "";
    adressePostale.value = "";
    npa.value = "";
    localite.value = "";
    localiteCode.value = "";
  }
}

const adresseHint = createHintAdresse({
    manual: "informationsPersonnelles.hintAdresse",
    auto: "informationsPersonnelles.hintAdresseAuto",
    choice: "informationsPersonnelles.hintAdresseAutoOuManuel",
  },
  canSearchEventAddresses,
  selectedAddress,
  t
);

const adressePostaleHint = createHintAdresse({
    manual: "informationsPersonnelles.hintAdressePostale",
    auto: "informationsPersonnelles.hintAdressePostaleAuto",
    choice: "informationsPersonnelles.hintAdressePostaleAutoOuManuel",
  },
  canSearchEventAddresses,
  selectedAddress,
  t
);

const npaHint = createHintAdresse({
    manual: "informationsPersonnelles.hintNPA",
    auto: "informationsPersonnelles.hintNPAAuto",
    choice: "informationsPersonnelles.hintNPAAutoOuManuel",
  },
  canSearchEventAddresses,
  selectedAddress,
  t
);

const localiteHint = createHintAdresse({
    manual: "informationsPersonnelles.hintLocalite",
    auto: "informationsPersonnelles.hintLocaliteAuto",
    choice: "informationsPersonnelles.hintLocaliteAutoOuManuel",
  },
  canSearchEventAddresses,
  selectedAddress,
  t
);

function onCountryChanged(country: CountrySelection) {
  pays.value = country.ripolCode;
}
</script>

<style scoped>
.v-field--disabled input,
.v-field--disabled textarea {
  cursor: not-allowed !important;
}
</style>
