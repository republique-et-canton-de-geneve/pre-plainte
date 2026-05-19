<template>
  <v-card class="pa-2 pa-md-6 mb-4">
    <h3 class="pre-plainte-main-card-title mb-4 mb-md-6 text-h4 text-md-h3">
      {{ t("informationsPersonnelles.identiteTiersConcerne") }}
    </h3>
    <v-text-field
      :label="t('informationsPersonnelles.nom')"
      v-model="tiersNom"
      :error-messages="tiersNomError"
      class="mb-8"
      variant="outlined"
      :hint="t('informationsPersonnelles.hintNomTiers')"
      persistent-hint
      required
    />
    <v-text-field
      :label="t('informationsPersonnelles.prenom')"
      v-model="tiersPrenom"
      :error-messages="tiersPrenomError"
      class="mb-8"
      variant="outlined"
      :hint="t('informationsPersonnelles.hintPrenomTiers')"
      persistent-hint
      required
    />

    <RipolAutocomplete
      v-model="tiersGenre"
      :label="t('informationsPersonnelles.genre')"
      :fetch-fn="fetchGenresSorted"
      :display-label="displayGenreLabel"
      :error-messages="tiersGenreError"
      :hint="t('informationsPersonnelles.hintGenreTiers')"
      :preload="true"
      :min-search-length="0"
      class="mb-4"
    />

    <RipolAutocomplete
      v-model="tiersNationalite"
      :label="t('informationsPersonnelles.nationalite')"
      :fetch-fn="fetchNationalitiesForPersonForm"
      :error-messages="tiersNationaliteError"
      :hint="t('informationsPersonnelles.hintNationaliteTiers')"
      :preload="true"
      :min-search-length="0"
      class="mb-4"
    />
    <v-text-field
      :label="t('informationsPersonnelles.dateNaissance')"
      v-model="tiersDateNaissance"
      type="text"
      placeholder="JJ.MM.AAAA"
      :error-messages="tiersDateNaissanceError"
      class="mb-8"
      variant="outlined"
      prepend-inner-icon="mdi-calendar"
      :hint="t('informationsPersonnelles.hintDateNaissanceTiers')"
      persistent-hint
      required
      @input="onTiersDateNaissanceInput"
    />
    <h4 class="mb-8 mb-md-4 mt-4 mt-md-6 text-h4 text-md-h4 font-weight-bold">
      {{ t("informationsPersonnelles.adresseTiersConcerne") }}
    </h4>

    <AddressLookup
      instance-id="tiers-concerne"
      @address-selected="onTiersAddressSelected"
      @country-changed="onTiersCountryChanged"
    />

    <v-text-field
      :label="t('informationsPersonnelles.adresse')"
      v-model="tiersAdresse"
      :error-messages="tiersAdresseError"
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
      v-model="tiersAdressePostale"
      :error-messages="tiersAdressePostaleError"
      :readonly="!!selectedAddress"
      :aria-disabled="!!selectedAddress"
      class="mb-8"
      variant="outlined"
      :hint="adressePostaleHint"
      persistent-hint
      required
    />
    <v-text-field
      :label="t('informationsPersonnelles.npa')"
      v-model="tiersNpa"
      :error-messages="tiersNpaError"
      :readonly="!!selectedAddress"
      :aria-disabled="!!selectedAddress"
      class="mb-8"
      variant="outlined"
      :hint="npaHint"
      persistent-hint
      required
    />
    <v-text-field
      :label="t('informationsPersonnelles.localite')"
      v-model="tiersLocalite"
      :error-messages="tiersLocaliteError"
      :readonly="!!selectedAddress"
      :aria-disabled="!!selectedAddress"
      class="mb-8"
      variant="outlined"
      :hint="localiteHint"
      persistent-hint
      required
    />

    <h4 class="mb-8 mb-md-4 mt-4 mt-md-6 text-h4 text-md-h4 font-weight-bold">
      {{ t("informationsPersonnelles.documentIdentiteTiers") }}
    </h4>
    <AccessibleVSelect
      v-model="tiersTypeDocumentIdentite"
      :label="t('informationsPersonnelles.typeDocumentIdentite')"
      :items="typesDocumentIdentite"
      item-title="label"
      item-value="value"
      :error-messages="tiersTypeDocumentIdentiteError"
      :hint="t('informationsPersonnelles.hintTypeDocumentIdentiteTiers')"
      class="mb-4"
      variant="outlined"
      persistent-hint
    />

    <v-text-field
      v-if="showDocumentNumberFieldTiers"
      :label="documentNumberLabelTiers"
      v-model="tiersNumeroDocumentIdentite"
      :error-messages="tiersNumeroDocumentIdentiteError"
      class="mb-8"
      variant="outlined"
      :hint="
        documentNumberLabelTiers === t('informationsPersonnelles.numeroCarteIdentite')
          ? t('informationsPersonnelles.hintNumeroCarteIdentiteTiers')
          : t('informationsPersonnelles.hintNumeroPasseportTiers')
      "
      persistent-hint
    />

    <h4 class="mb-8 mb-md-4 mt-4 mt-md-6 text-h4 text-md-h4 font-weight-bold">
      {{ t("informationsPersonnelles.coordonneesTiers") }}
    </h4>
    <PhoneInput
      v-model="tiersTelephone"
      :label="t('informationsPersonnelles.numeroTelephone')"
      :error-messages="tiersTelephoneError"
      :hint="t('informationsPersonnelles.hintTelephoneTiers')"
      input-class="mb-8"
      default-country-code="CH"
      :required="true"
    />
    <v-text-field
      :label="t('informationsPersonnelles.email')"
      v-model="tiersEmail"
      type="email"
      :error-messages="tiersEmailError"
      class="mb-8"
      variant="outlined"
      :hint="t('informationsPersonnelles.hintEmailTiers')"
      persistent-hint
      required
    />
    <v-text-field
      :label="t('informationsPersonnelles.confirmationEmail')"
      v-model="tiersConfirmationEmail"
      type="email"
      :error-messages="tiersConfirmationEmailError"
      class="mb-8"
      variant="outlined"
      :hint="t('informationsPersonnelles.hintConfirmationEmailTiers')"
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
import RipolAutocomplete from "@/components/ripol/RipolAutocomplete.vue";
import type { AddressResult } from "@/types/adresse.interface";
import type { RipolSelection } from "@/types/ripol.interface";
import type { CountrySelection } from "@/types/country.types";
import { GENRE_LABEL_KEYS, TYPES_DOCUMENT_IDENTITE } from "@/constants/constant.ts";
import { toTranslatedOptions } from "@/utils/helpers/traductionHelper";
import { fetchGenresSorted, fetchNationalitiesForPersonForm } from "@/utils/helpers/ripolHelpers.ts";
import AccessibleVSelect from "@/components/accessibility/AccessibleVSelect.vue";
import { applyDateMask } from "@/utils/helpers/dateHelpers.ts";
import { createHintAdresse } from "@/utils/helpers/adresseHelpers.ts";

const { t } = useI18n();

const { value: tiersNom, errorMessage: tiersNomError } = useField("tiersNom");
const { value: tiersPrenom, errorMessage: tiersPrenomError } = useField("tiersPrenom");
const { value: tiersAdresse, errorMessage: tiersAdresseError } = useField("tiersAdresse");
const { value: tiersPays } = useField("tiersPays");
if (!tiersPays.value) {
  tiersPays.value = RIPOL.PAYS_SUISSE;
}
const { value: tiersGenre, errorMessage: tiersGenreError } = useField<RipolSelection | null>("tiersGenre");
const { value: tiersNationalite, errorMessage: tiersNationaliteError } = useField<RipolSelection | null>(
  "tiersNationalite",
);
const { value: tiersAdressePostale, errorMessage: tiersAdressePostaleError } = useField("tiersAdressePostale");
const { value: tiersNpa, errorMessage: tiersNpaError } = useField("tiersNpa");
const { value: tiersLocalite, errorMessage: tiersLocaliteError } = useField("tiersLocalite");
const { value: tiersDateNaissance, errorMessage: tiersDateNaissanceError } = useField<string>("tiersDateNaissance");
const { value: tiersTelephone, errorMessage: tiersTelephoneError } = useField<string>("tiersTelephone");
const { value: tiersEmail, errorMessage: tiersEmailError } = useField("tiersEmail");
const { value: tiersConfirmationEmail, errorMessage: tiersConfirmationEmailError } = useField("tiersConfirmationEmail");
const { value: tiersTypeDocumentIdentite, errorMessage: tiersTypeDocumentIdentiteError } =
  useField("tiersTypeDocumentIdentite");
const { value: tiersNumeroDocumentIdentite, errorMessage: tiersNumeroDocumentIdentiteError } =
  useField("tiersNumeroDocumentIdentite");

const displayGenreLabel = (item: RipolSelection) => {
  const key = GENRE_LABEL_KEYS[item.code];
  return key ? t(key) : item.label;
};

const typesDocumentIdentite = computed(() => toTranslatedOptions(TYPES_DOCUMENT_IDENTITE, t));

const showDocumentNumberFieldTiers = computed(
  () => !!tiersTypeDocumentIdentite.value,
);

const documentNumberLabelTiers = computed(() => {
  switch (tiersTypeDocumentIdentite.value) {
    case "carte_identite":
      return t("informationsPersonnelles.numeroCarteIdentite");
    case "passeport":
      return t("informationsPersonnelles.numeroPasseport");
    default:
      return "";
  }
});

const selectedAddress = ref<AddressResult | null>(null);

const canSearchTiersAddresses = computed(
  () => tiersPays.value === RIPOL.PAYS_SUISSE || tiersPays.value === RIPOL.PAYS_FRANCE,
);

const onTiersAddressSelected = (address: AddressResult | null) => {
  selectedAddress.value = address;
  if (address) {
    tiersAdresse.value = `${address.street}${address.streetNumber ? " " + address.streetNumber : ""}`;
    tiersAdressePostale.value = String(address.streetNumber || "");
    tiersNpa.value = String(address.postalCode || "");
    tiersLocalite.value = address.locality;
  } else {
    tiersAdresse.value = "";
    tiersAdressePostale.value = "";
    tiersNpa.value = "";
    tiersLocalite.value = "";
  }
};

const adresseHint = createHintAdresse(
  {
    manual: "informationsPersonnelles.hintAdresseTiers",
    auto: "informationsPersonnelles.hintAdresseAuto",
    choice: "informationsPersonnelles.hintAdresseAutoOuManuel",
  },
  canSearchTiersAddresses,
  selectedAddress,
  t,
);

const adressePostaleHint = createHintAdresse(
  {
    manual: "informationsPersonnelles.hintAdressePostaleTiers",
    auto: "informationsPersonnelles.hintAdressePostaleAuto",
    choice: "informationsPersonnelles.hintAdressePostaleAutoOuManuel",
  },
  canSearchTiersAddresses,
  selectedAddress,
  t,
);

const npaHint = createHintAdresse(
  {
    manual: "informationsPersonnelles.hintNPATiers",
    auto: "informationsPersonnelles.hintNPAAuto",
    choice: "informationsPersonnelles.hintNPAAutoOuManuel",
  },
  canSearchTiersAddresses,
  selectedAddress,
  t,
);

const localiteHint = createHintAdresse(
  {
    manual: "informationsPersonnelles.hintLocaliteTiers",
    auto: "informationsPersonnelles.hintLocaliteAuto",
    choice: "informationsPersonnelles.hintLocaliteAutoOuManuel",
  },
  canSearchTiersAddresses,
  selectedAddress,
  t,
);

const onTiersCountryChanged = (country: CountrySelection) => {
  tiersPays.value = country.ripolCode;
};

const onTiersDateNaissanceInput = (e: InputEvent) => {
  applyDateMask(e, tiersDateNaissance);
};
</script>

<style scoped>
.v-field--readonly input,
.v-field--readonly textarea {
  cursor: not-allowed !important;
}
</style>
