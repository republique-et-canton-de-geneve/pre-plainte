<template>
  <v-card class="pa-2 pa-md-6 mb-4">
    <h3 class="pre-plainte-main-card-title mb-4 mb-md-6 text-h4 text-md-h3">
      {{ t("informationsPersonnelles.identiteOrganisation") }}
    </h3>

    <v-text-field
      :label="requiredLabel(t('informationsPersonnelles.nomOrganisation'))"
      v-model="organisationNom"
      :error-messages="organisationNomError"
      class="mb-8"
      variant="outlined"
      :hint="t('informationsPersonnelles.hintNomOrganisation')"
      persistent-hint
      required
    />

    <h4 class="mb-8 mb-md-4 mt-4 mt-md-6 text-h4 text-md-h4 font-weight-bold">
      {{ t("informationsPersonnelles.adresseOrganisation") }}
    </h4>

    <AddressLookup
      instance-id="organisation"
      @address-selected="onOrganisationAddressSelected"
      @country-changed="onOrganisationCountryChanged"
    />

    <v-text-field
      :label="requiredLabel(t('informationsPersonnelles.adresse'))"
      v-model="organisationAdresse"
      :error-messages="organisationAdresseError"
      :readonly="!!selectedAddress"
      :aria-disabled="!!selectedAddress"
      class="mb-8"
      variant="outlined"
      :hint="adresseHint"
      persistent-hint
      required
    />

    <v-text-field
      :label="t('informationsPersonnelles.adressePostale')"
      v-model="organisationAdressePostale"
      :error-messages="organisationAdressePostaleError"
      :readonly="!!selectedAddress"
      :aria-disabled="!!selectedAddress"
      class="mb-8"
      variant="outlined"
      :hint="adressePostaleHint"
      persistent-hint
    />

    <v-text-field
      :label="requiredLabel(t('informationsPersonnelles.npa'))"
      v-model="organisationNpa"
      :error-messages="organisationNpaError"
      :readonly="!!selectedAddress"
      :aria-disabled="!!selectedAddress"
      class="mb-8"
      variant="outlined"
      :hint="npaHint"
      persistent-hint
      required
    />

    <v-text-field
      :label="requiredLabel(t('informationsPersonnelles.localite'))"
      v-model="organisationLocalite"
      :error-messages="organisationLocaliteError"
      :readonly="!!selectedAddress"
      :aria-disabled="!!selectedAddress"
      class="mb-8"
      variant="outlined"
      :hint="localiteHint"
      persistent-hint
      required
    />

    <h4 class="mb-8 mb-md-4 mt-4 mt-md-6 text-h4 text-md-h4 font-weight-bold">
      {{ t("informationsPersonnelles.coordonneesOrganisation") }}
    </h4>

    <PhoneInput
      v-model="organisationTelephone"
      :label="t('informationsPersonnelles.numeroTelephone')"
      :error-messages="organisationTelephoneError"
      :hint="t('informationsPersonnelles.hintTelephoneOrganisation')"
      input-class="mb-8"
      default-country-code="CH"
      :required="true"
    />

    <v-text-field
      :label="requiredLabel(t('informationsPersonnelles.email'))"
      v-model="organisationEmail"
      type="email"
      :error-messages="organisationEmailError"
      class="mb-8"
      variant="outlined"
      :hint="t('informationsPersonnelles.hintEmailOrganisation')"
      persistent-hint
      required
    />

    <v-text-field
      :label="requiredLabel(t('informationsPersonnelles.confirmationEmail'))"
      v-model="organisationConfirmationEmail"
      type="email"
      :error-messages="organisationConfirmationEmailError"
      class="mb-8"
      variant="outlined"
      :hint="t('informationsPersonnelles.hintConfirmationEmailOrganisation')"
      persistent-hint
      required
    />

    <slot name="buttons"></slot>
  </v-card>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useField } from "vee-validate";
import { useI18n } from "vue-i18n";
import { RIPOL } from "@/constants/constant";
import AddressLookup from "@/components/adresse/AddressLookup.vue";
import PhoneInput from "@/components/phone/PhoneInput.vue";
import type { AddressResult } from "@/types/adresse.interface";
import type { CountrySelection } from "@/types/country.types";
import { createHintAdresse } from "@/utils/helpers/adresseHelpers.ts";
import { requiredLabel } from "@/utils/helpers/labelHelpers";

const { t } = useI18n();

const { value: organisationNom, errorMessage: organisationNomError } = useField("organisationNom");
const { value: organisationAdresse, errorMessage: organisationAdresseError } = useField("organisationAdresse");
const { value: organisationAdressePostale, errorMessage: organisationAdressePostaleError } = useField("organisationAdressePostale");
const { value: organisationNpa, errorMessage: organisationNpaError } = useField("organisationNpa");
const { value: organisationLocalite, errorMessage: organisationLocaliteError } = useField("organisationLocalite");
const { value: organisationPays } = useField("organisationPays");
const { value: organisationTelephone, errorMessage: organisationTelephoneError } = useField<string>("organisationTelephone");
const { value: organisationEmail, errorMessage: organisationEmailError } = useField("organisationEmail");
const { value: organisationConfirmationEmail, errorMessage: organisationConfirmationEmailError } = useField("organisationConfirmationEmail");

if (!organisationPays.value) {
  organisationPays.value = RIPOL.PAYS_SUISSE;
}

const selectedAddress = ref<AddressResult | null>(null);

const canSearchOrganisationAddresses = computed(() =>
  organisationPays.value === RIPOL.PAYS_SUISSE || organisationPays.value === RIPOL.PAYS_FRANCE
);

const onOrganisationAddressSelected = (address: AddressResult | null) => {
  selectedAddress.value = address;
  if (address) {
    organisationAdresse.value = `${address.street}${address.streetNumber ? " " + address.streetNumber : ""}`;
    organisationAdressePostale.value = String(address.streetNumber || "");
    organisationNpa.value = String(address.postalCode || "");
    organisationLocalite.value = address.locality;
  } else {
    organisationAdresse.value = "";
    organisationAdressePostale.value = "";
    organisationNpa.value = "";
    organisationLocalite.value = "";
  }
};

const adresseHint = createHintAdresse({
    manual: "informationsPersonnelles.hintAdresseOrganisation",
    auto: "informationsPersonnelles.hintAdresseAuto",
    choice: "informationsPersonnelles.hintAdresseAutoOuManuel",
  },
  canSearchOrganisationAddresses,
  selectedAddress,
  t
);

const adressePostaleHint = createHintAdresse({
    manual: "informationsPersonnelles.hintAdressePostaleOrganisation",
    auto: "informationsPersonnelles.hintAdressePostaleAuto",
    choice: "informationsPersonnelles.hintAdressePostaleAutoOuManuel",
  },
  canSearchOrganisationAddresses,
  selectedAddress,
  t
);

const npaHint = createHintAdresse({
    manual: "informationsPersonnelles.hintNPAOrganisation",
    auto: "informationsPersonnelles.hintNPAAuto",
    choice: "informationsPersonnelles.hintNPAAutoOuManuel",
  },
  canSearchOrganisationAddresses,
  selectedAddress,
  t
);

const localiteHint = createHintAdresse({
    manual: "informationsPersonnelles.hintLocaliteOrganisation",
    auto: "informationsPersonnelles.hintLocaliteAuto",
    choice: "informationsPersonnelles.hintLocaliteAutoOuManuel",
  },
  canSearchOrganisationAddresses,
  selectedAddress,
  t
);

const onOrganisationCountryChanged = (country: CountrySelection) => {
  organisationPays.value = country.ripolCode;
};
</script>

<style scoped>
.v-field--readonly input,
.v-field--readonly textarea {
  cursor: not-allowed !important;
}
</style>
