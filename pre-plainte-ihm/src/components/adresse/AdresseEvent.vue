<template>
  <div class="inputs-fields">
    <h3 class="pre-plainte-main-card-title mb-4 text-h5 text-md-h4">{{ t("informationsEvenement.adresseEvenement") }}</h3>

    <BaseRadioGroup
      v-model="adresseLesee"
      :label="t('adresseEvent.adresseCorrespond')"
      required
      :options="[
        { label: t('adresseEvent.adresseTiers'), value: true },
        { label: t('adresseEvent.adresseAutre'), value: false }
      ]"
      :error-messages="adresseLeseeError"
    />

    <v-alert
      v-if="paysEvenementLimiteASuisse"
      type="info"
      variant="tonal"
      density="comfortable"
      class="mb-4"
    >
      {{ t("informationsEvenement.adresseSuisseUniquement") }}
    </v-alert>

    <div v-if="adresseLesee === false">
      <RipolAutocomplete
        v-model="typeLieu"
        :label="t('adresseEvent.typeLieu')"
        required
        :fetch-fn="RipolService.searchLocationTypes"
        :error-messages="typeLieuError"
        :hint="t('adresseEvent.hintTypeLieu')"
        :preload="true"
        :min-search-length="0"
        class="mb-3"
      />

      <BaseRadioGroup
        v-model="adresseConnue"
        :label="t('adresseEvent.adresseConnue')"
        required
        :options="[
        { label: t('common.oui'), value: true },
        { label: t('common.non'), value: false }
      ]"
        :error-messages="adresseConnueError"
      />

      <div v-if="adresseConnue === false" class="mb-4">
        <BaseRadioGroup
          v-model="isTrajet"
          :label="t('adresseEvent.adresseTrajet')"
          required
          :options="[
        { label: t('common.oui'), value: true },
        { label: t('common.non'), value: false }
      ]"
          :error-messages="isTrajetError"
        />
      </div>

      <AdresseEventFields
        v-if="showAdresseEvenement"
        :title="isTrajet ? t('adresseEvent.adresseDepart') : undefined"
        instance-id="incident-start"
        v-model:adresse="adresseEvenement"
        v-model:adressePostale="adressePostaleEvenement"
        v-model:npa="npaEvenement"
        v-model:localite="localiteEvenement"
        v-model:pays="paysEvenement"
        :allowed-country-codes="paysEvenementAutorises"
        :adresse-error="adresseEvenementError"
        :adresse-postale-error="adressePostaleEvenementError"
        :npa-error="npaEvenementError"
        :localite-error="localiteEvenementError"
      />

      <template v-if="adresseConnue === false">
        <AdresseEventFields
          v-if="isTrajet"
          :title="t('adresseEvent.adresseDestination')"
          instance-id="incident-end"
          v-model:adresse="adresseEvenementSecondaire"
          v-model:adressePostale="adressePostaleEvenementSecondaire"
          v-model:npa="npaEvenementSecondaire"
          v-model:localite="localiteEvenementSecondaire"
          v-model:pays="paysEvenementSecondaire"
          :allowed-country-codes="paysEvenementAutorises"
          :adresse-error="adresseEvenementSecondaireError"
          :adresse-postale-error="adressePostaleEvenementSecondaireError"
          :npa-error="npaEvenementSecondaireError"
          :localite-error="localiteEvenementSecondaireError"
        />

        <RipolAutocomplete
          v-if="isTrajet === false"
          v-model="lieuOrigineEvenement"
          :label="t('adresseEvent.lieuOrigine')"
          required
          :fetch-fn="RipolService.searchLieuxOrigine"
          :hint="t('adresseEvent.hintLieuOrigine')"
          :preload="false"
          :min-search-length="2"
          class="mb-3"
          :error-messages="lieuOrigineEvenementError"
        />
      </template>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useField } from "vee-validate";
import RipolAutocomplete from "@/components/ripol/RipolAutocomplete.vue";
import AdresseEventFields from "./AdresseEventFields.vue";
import { RipolService } from "@/services/ripolService";
import type { RipolSelection } from "@/types/ripol.interface";
import { useCreatePrePlainteStore } from "@/stores/createPrePlainteStore";
import { RIPOL } from "@/constants/constant";
import BaseRadioGroup from "@/components/radio/BaseRadioGroup.vue";

const { t } = useI18n();
const store = useCreatePrePlainteStore();

const nationalitePersonneLesee = computed(
  () => store.userFormData.tiersNationalite?.code || store.userFormData.nationalite?.code,
);
const paysEvenementLimiteASuisse = computed(
  () => !!nationalitePersonneLesee.value && nationalitePersonneLesee.value !== RIPOL.PAYS_SUISSE,
);
const paysEvenementAutorises = computed(() =>
  paysEvenementLimiteASuisse.value ? [RIPOL.PAYS_SUISSE] : undefined,
);

const { value: adresseEvenement, errorMessage: adresseEvenementError } = useField<string>(
  "adresseEvenement",
  undefined,
  {
    keepValueOnUnmount: true,
  },
);
const { value: adressePostaleEvenement, errorMessage: adressePostaleEvenementError } = useField<string>(
  "adressePostaleEvenement",
  undefined,
  { keepValueOnUnmount: true },
);
const { value: npaEvenement, errorMessage: npaEvenementError } = useField<string>("npaEvenement", undefined, {
  keepValueOnUnmount: true,
});
const { value: localiteEvenement, errorMessage: localiteEvenementError } = useField<string>(
  "localiteEvenement",
  undefined,
  {
    keepValueOnUnmount: true,
  },
);
const { value: paysEvenement } = useField<string>("paysEvenement", undefined, {
  keepValueOnUnmount: true,
});
const { value: lieuOrigineEvenement, errorMessage: lieuOrigineEvenementError } = useField<RipolSelection | null>("lieuOrigineEvenement", undefined, {
  keepValueOnUnmount: true,
});

const { value: typeLieu, errorMessage: typeLieuError } = useField<RipolSelection | null>("typeLieu", undefined, {
  keepValueOnUnmount: true,
});
const { value: adresseConnue, errorMessage: adresseConnueError } = useField<boolean>("adresseConnue", undefined, {
  keepValueOnUnmount: true,
});
const { value: adresseLesee, errorMessage: adresseLeseeError } = useField<boolean>("adresseLesee", undefined, {
  keepValueOnUnmount: true,
});
const { value: isTrajet, errorMessage: isTrajetError } = useField<boolean>("isTrajet", undefined, {
  keepValueOnUnmount: true,
});

const { value: adresseEvenementSecondaire, errorMessage: adresseEvenementSecondaireError } = useField<string>(
  "adresseEvenementSecondaire",
  undefined,
  { keepValueOnUnmount: true },
);
const { value: adressePostaleEvenementSecondaire, errorMessage: adressePostaleEvenementSecondaireError } =
  useField<string>("adressePostaleEvenementSecondaire", undefined, {
    keepValueOnUnmount: true,
  });
const { value: npaEvenementSecondaire, errorMessage: npaEvenementSecondaireError } = useField<string>(
  "npaEvenementSecondaire",
  undefined,
  { keepValueOnUnmount: true },
);
const { value: localiteEvenementSecondaire, errorMessage: localiteEvenementSecondaireError } = useField<string>(
  "localiteEvenementSecondaire",
  undefined,
  { keepValueOnUnmount: true },
);
const { value: paysEvenementSecondaire } = useField<string>("paysEvenementSecondaire", undefined, {
  keepValueOnUnmount: true,
});

function fillFromPersonOrTiers() {
  const pick = (a?: string | null, b?: string | null) => a?.trim() || b?.trim() || "";
  adresseEvenement.value = pick(store.userFormData.tiersAdresse, store.userFormData.adresse);
  npaEvenement.value = pick(store.userFormData.tiersNpa, store.userFormData.npa);
  adressePostaleEvenement.value = pick(store.userFormData.tiersAdressePostale, store.userFormData.adressePostale);
  localiteEvenement.value = pick(store.userFormData.tiersLocalite, store.userFormData.localite);
  paysEvenement.value = pick(store.userFormData.tiersPays, store.userFormData.pays) || "CH";
}

function clearPrimaryAddressFields() {
  adresseEvenement.value = "";
  adressePostaleEvenement.value = "";
  npaEvenement.value = "";
  localiteEvenement.value = "";
  paysEvenement.value = "CH";
}

function clearSecondaryAddressFields() {
  adresseEvenementSecondaire.value = "";
  adressePostaleEvenementSecondaire.value = "";
  npaEvenementSecondaire.value = "";
  localiteEvenementSecondaire.value = "";
  paysEvenementSecondaire.value = "CH";
}

const showAdresseEvenement = computed(
  () => (adresseConnue.value || isTrajet.value) && !adresseLesee.value,
);

watch(adresseConnue, isKnown => {
  if (isKnown) {
    clearSecondaryAddressFields();
    lieuOrigineEvenement.value = null;
  }
});

watch(adresseLesee, value => {
  if (!adresseConnue.value) {
    return;
  }
  if (value) {
    fillFromPersonOrTiers();
  }
  if (!value) {
    clearPrimaryAddressFields();
  }
});

watch(isTrajet, value => {
  if (value) {
    lieuOrigineEvenement.value = null;
  }
  if (!value) {
    clearPrimaryAddressFields();
    clearSecondaryAddressFields();
  }
});
</script>

<style scoped>
.v-field--disabled input,
.v-field--disabled textarea {
  cursor: not-allowed !important;
}
</style>
