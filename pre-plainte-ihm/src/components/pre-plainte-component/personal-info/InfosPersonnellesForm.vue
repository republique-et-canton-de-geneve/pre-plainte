<template>
  <div>
    <h3 class="pre-plainte-main-card-title mb-4 mb-md-6 text-h4 text-md-h3">
      {{ t("informationsPersonnelles.coordonneesPersonneSignalant") }}
    </h3>

    <PhoneInput
      v-model="telephone"
      :label="t('informationsPersonnelles.numeroTelephone')"
      :error-messages="telephoneError"
      :hint="t('informationsPersonnelles.hintTelephone')"
      input-class="mb-4"
      aria-label="Saisir le nom du pays "
      default-country-code="CH"
    />

    <h3 class="pre-plainte-main-card-title mb-4 mb-md-6 text-h4 text-md-h3">
      {{ t("informationsPersonnelles.identitePersonneSignalant") }}
    </h3>

    <v-text-field
      :label="t('informationsPersonnelles.nom')"
      v-model="nom"
      :error-messages="nomError"
      class="mb-8"
      :class="{ 'field-valid': nomMeta.valid }"
      variant="outlined"
      :hint="t('informationsPersonnelles.hintNom')"
      persistent-hint
    />
    <v-text-field
      :label="t('informationsPersonnelles.nomNaissance')"
      v-model="nomNaissance"
      :error-messages="nomNaissanceError"
      class="mb-8"
      variant="outlined"
      :hint="t('informationsPersonnelles.hintNomNaissance')"
      persistent-hint
    />
    <v-text-field
      :label="t('informationsPersonnelles.prenom')"
      v-model="prenom"
      :error-messages="prenomError"
      class="mb-8"
      variant="outlined"
      :hint="t('informationsPersonnelles.hintPrenom')"
      persistent-hint
    />

    <RipolAutocomplete
      v-model="genre"
      :label="t('informationsPersonnelles.genre')"
      :fetch-fn="fetchGenresSorted"
      :display-label="displayGenreLabel"
      :error-messages="genreError"
      :hint="t('informationsPersonnelles.hintGenre')"
      :preload="true"
      :min-search-length="0"
      class="mb-4"
    />

    <RipolAutocomplete
      v-model="nationalite"
      :label="t('informationsPersonnelles.nationalite')"
      :fetch-fn="fetchNationalitiesForPersonForm"
      :error-messages="nationaliteError"
      :hint="t('informationsPersonnelles.hintNationalite')"
      :preload="true"
      :min-search-length="0"
      class="mb-4"
    />

    <RipolAutocomplete
      v-if="showLieuOrigine"
      v-model="lieuOrigine"
      :label="t('informationsPersonnelles.lieuOrigine')"
      :fetch-fn="RipolService.searchLieuxOrigine"
      :error-messages="lieuOrigineError"
      :hint="t('informationsPersonnelles.hintLieuOrigine')"
      :preload="false"
      :min-search-length="2"
      class="mb-4"
    />

    <AccessibleVSelect
      v-if="showTitreSejour"
      :label="t('informationsPersonnelles.titreSejour')"
      v-model="titreSejour"
      :items="titresSejour"
      item-title="label"
      item-value="value"
      :error-messages="titreSejourError"
      class="mb-4"
      variant="outlined"
      :hint="t('informationsPersonnelles.hintTitreSejour')"
      persistent-hint
    />

    <v-text-field
      :label="t('informationsPersonnelles.dateNaissance')"
      v-model="dateNaissance"
      type="text"
      placeholder="JJ.MM.AAAA"
      :error-messages="dateNaissanceError"
      class="mb-8"
      variant="outlined"
      prepend-inner-icon="mdi-calendar"
      :hint="t('informationsPersonnelles.hintDateNaissance')"
      persistent-hint
      @input="onDateNaissanceInput"
    />

    <h4 class="mb-4 mt-6 text-h4">
      {{ t("informationsPersonnelles.adressePersonneSignalant") }}
    </h4>

    <AddressLookup
      instance-id="personne-signalante"
      @address-selected="onAddressSelected"
      @country-changed="onCountryChanged"
    />

    <v-text-field
      :label="t('informationsPersonnelles.adresse')"
      v-model="adresse"
      :error-messages="adresseError"
      :readonly="!!selectedAddress"
      :aria-disabled="!!selectedAddress"
      class="mb-8"
      variant="outlined"
      :hint="adresseHint"
      persistent-hint
    />
    <v-text-field
      :label="t('informationsPersonnelles.adressePostale')"
      v-model="adressePostale"
      :error-messages="adressePostaleError"
      :readonly="!!selectedAddress"
      :aria-disabled="!!selectedAddress"
      class="mb-8"
      variant="outlined"
      :hint="adressePostaleHint"
      persistent-hint
    />
    <v-text-field
      :label="t('informationsPersonnelles.npa')"
      v-model="npa"
      :error-messages="npaError"
      :readonly="!!selectedAddress"
      :aria-disabled="!!selectedAddress"
      class="mb-8"
      variant="outlined"
      :hint="npaHint"
      persistent-hint
    />
    <v-text-field
      :label="t('informationsPersonnelles.localite')"
      v-model="localite"
      :error-messages="localiteError"
      :readonly="!!selectedAddress"
      :aria-disabled="!!selectedAddress"
      class="mb-8"
      variant="outlined"
      :hint="localiteHint"
      persistent-hint
    />

    <h4 class="mb-8 mb-md-4 mt-4 mt-md-6 text-h4 text-md-h4 font-weight-bold">
      {{ t("informationsPersonnelles.documentIdentite") }}
    </h4>
    <AccessibleVSelect
      v-model="typeDocumentIdentite"
      :label="t('informationsPersonnelles.typeDocumentIdentite')"
      :items="typesDocumentIdentite"
      item-title="label"
      item-value="value"
      :error-messages="typeDocumentIdentiteError"
      :hint="t('informationsPersonnelles.hintTypeDocumentIdentite')"
      class="mb-4"
      variant="outlined"
      persistent-hint
    />

    <v-text-field
      v-if="showDocumentNumberField"
      :label="documentNumberLabel"
      v-model="numeroDocumentIdentite"
      :error-messages="numeroDocumentIdentiteError"
      class="mb-8"
      variant="outlined"
      :hint="
        documentNumberLabel === t('informationsPersonnelles.numeroCarteIdentite')
          ? t('informationsPersonnelles.hintNumeroCarteIdentite')
          : t('informationsPersonnelles.hintNumeroPasseport')
      "
      persistent-hint
    />

  </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useField } from "vee-validate";
import { useI18n } from "vue-i18n";
import {
  GENRE_LABEL_KEYS,
  TITRES_SEJOUR,
  TYPES_DOCUMENT_IDENTITE,
  RIPOL,
} from "@/constants/constant";
import { toTranslatedOptions } from "@/utils/helpers/traductionHelper";
import AddressLookup from "@/components/adresse/AddressLookup.vue";
import PhoneInput from "@/components/phone/PhoneInput.vue";
import RipolAutocomplete from "@/components/ripol/RipolAutocomplete.vue";
import type { AddressResult } from "@/types/adresse.interface";
import type { RipolSelection } from "@/types/ripol.interface";
import type { CountrySelection } from "@/types/country.types";
import { RipolService } from "@/services/ripolService";
import { fetchGenresSorted, fetchNationalitiesForPersonForm } from "@/utils/helpers/ripolHelpers.ts";
import AccessibleVSelect from "@/components/accessibility/AccessibleVSelect.vue";
import { applyDateMask } from "@/utils/helpers/dateHelpers.ts";
import { createHintAdresse } from "@/utils/helpers/adresseHelpers.ts";

const { t } = useI18n();

const { value: nom, errorMessage: nomError, meta: nomMeta } = useField("nom");
const { value: nomNaissance, errorMessage: nomNaissanceError } = useField("nomNaissance");
const { value: prenom, errorMessage: prenomError } = useField("prenom");
const { value: adresse, errorMessage: adresseError } = useField("adresse");
const { value: pays } = useField("pays");
const { value: genre, errorMessage: genreError } = useField<RipolSelection | null>("genre");
const { value: nationalite, errorMessage: nationaliteError } = useField<RipolSelection | null>("nationalite");
const { value: lieuOrigine, errorMessage: lieuOrigineError } = useField<RipolSelection | null>("lieuOrigine");
const { value: titreSejour, errorMessage: titreSejourError } = useField("titreSejour");
const { value: adressePostale, errorMessage: adressePostaleError } = useField("adressePostale");
const { value: npa, errorMessage: npaError } = useField("npa");
const { value: localite, errorMessage: localiteError } = useField("localite");
const { value: dateNaissance, errorMessage: dateNaissanceError } = useField<string>("dateNaissance");
const { value: telephone, errorMessage: telephoneError } = useField<string>("telephone");
const { value: typeDocumentIdentite, errorMessage: typeDocumentIdentiteError } = useField("typeDocumentIdentite");
const { value: numeroDocumentIdentite, errorMessage: numeroDocumentIdentiteError } = useField("numeroDocumentIdentite");

const displayGenreLabel = (item: RipolSelection) => {
  const key = GENRE_LABEL_KEYS[item.code];
  return key ? t(key) : item.label;
};

const titresSejour = computed(() => toTranslatedOptions(TITRES_SEJOUR, t));

const typesDocumentIdentite = computed(() => toTranslatedOptions(TYPES_DOCUMENT_IDENTITE, t));

const isNationaliteSuisse = computed(() => nationalite.value?.code === RIPOL.PAYS_SUISSE);

const showTitreSejour = computed(() => !!nationalite.value && !isNationaliteSuisse.value);

const showLieuOrigine = computed(() => isNationaliteSuisse.value);

const showDocumentNumberField = computed(() => !!typeDocumentIdentite.value);

const documentNumberLabel = computed(() => {
  switch (typeDocumentIdentite.value) {
    case "carte_identite":
      return t("informationsPersonnelles.numeroCarteIdentite");
    case "passeport":
      return t("informationsPersonnelles.numeroPasseport");
    default:
      return "";
  }
});

const selectedAddress = ref<AddressResult | null>(null);

const canSearchAddresses = computed(() => pays.value === RIPOL.PAYS_SUISSE || pays.value === RIPOL.PAYS_FRANCE);

const onAddressSelected = (address: AddressResult | null) => {
  selectedAddress.value = address;
  if (address) {
    adresse.value = `${address.street}${address.streetNumber ? " " + address.streetNumber : ""}`;
    adressePostale.value = String(address.streetNumber || "");
    npa.value = String(address.postalCode || "");
    localite.value = address.locality;
  } else {
    adresse.value = "";
    adressePostale.value = "";
    npa.value = "";
    localite.value = "";
  }
};

const adresseHint = createHintAdresse(
  {
    manual: "informationsPersonnelles.hintAdresse",
    auto: "informationsPersonnelles.hintAdresseAuto",
    choice: "informationsPersonnelles.hintAdresseAutoOuManuel",
  },
  canSearchAddresses,
  selectedAddress,
  t,
);

const adressePostaleHint = createHintAdresse(
  {
    manual: "informationsPersonnelles.hintAdressePostale",
    auto: "informationsPersonnelles.hintAdressePostaleAuto",
    choice: "informationsPersonnelles.hintAdressePostaleAutoOuManuel",
  },
  canSearchAddresses,
  selectedAddress,
  t,
);

const npaHint = createHintAdresse(
  {
    manual: "informationsPersonnelles.hintNPA",
    auto: "informationsPersonnelles.hintNPAAuto",
    choice: "informationsPersonnelles.hintNPAAutoOuManuel",
  },
  canSearchAddresses,
  selectedAddress,
  t,
);

const localiteHint = createHintAdresse(
  {
    manual: "informationsPersonnelles.hintLocalite",
    auto: "informationsPersonnelles.hintLocaliteAuto",
    choice: "informationsPersonnelles.hintLocaliteAutoOuManuel",
  },
  canSearchAddresses,
  selectedAddress,
  t,
);

const onCountryChanged = (country: CountrySelection) => {
  pays.value = country.ripolCode;
};

const onDateNaissanceInput = (e: InputEvent) => {
  applyDateMask(e, dateNaissance);
};
</script>

<style scoped>
.v-field--readonly input,
.v-field--readonly textarea {
  cursor: not-allowed !important;
}
</style>
