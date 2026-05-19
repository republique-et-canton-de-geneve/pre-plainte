<template>
  <div class="inputs-fields">
    <h3 class="pre-plainte-main-card-title mb-4 text-h3">{{ t("informationsEvenement.adresseEvenement") }}</h3>

    <BaseRadioGroup
      v-model="adresseLesee"
      :label="t('adresseEvent.adresseCorrespond')"
      :options="[
        { label: t('adresseEvent.adresseTiers'), value: true },
        { label: t('adresseEvent.adresseAutre'), value: false }
      ]"
      :error-messages="adresseLeseeError"
    />

    <div v-if="adresseLesee === false">
      <RipolAutocomplete
        v-model="typeLieu"
        :label="t('adresseEvent.typeLieu')"
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
        :adresse-error="adresseEvenementError"
        :adresse-postale-error="adressePostaleEvenementError"
        :npa-error="npaEvenementError"
        :localite-error="localiteEvenementError"
      />

      <RipolAutocomplete
        v-if="showLieuOrigine"
        v-model="lieuOrigine"
        :label="t('adresseEvent.lieuOrigine')"
        :fetch-fn="RipolService.searchLieuxOrigine"
        :hint="t('adresseEvent.hintLieuOrigine')"
        :preload="false"
        :min-search-length="2"
        :show-code="true"
        class="mb-3"
      />

      <AdresseEventFields
        v-if="isTrajet"
        :title="t('adresseEvent.adresseDestination')"
        instance-id="incident-end"
        v-model:adresse="adresseEvenementSecondaire"
        v-model:adressePostale="adressePostaleEvenementSecondaire"
        v-model:npa="npaEvenementSecondaire"
        v-model:localite="localiteEvenementSecondaire"
        v-model:pays="paysEvenementSecondaire"
        :adresse-error="adresseEvenementSecondaireError"
        :adresse-postale-error="adressePostaleEvenementSecondaireError"
        :npa-error="npaEvenementSecondaireError"
        :localite-error="localiteEvenementSecondaireError"
      />
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
const { value: lieuOrigine } = useField<RipolSelection | null>("lieuOrigine", undefined, {
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
  lieuOrigine.value = null;
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

const showLieuOrigine = computed(() => showAdresseEvenement && paysEvenement.value === RIPOL.PAYS_SUISSE);

watch(adresseConnue, isKnown => {
  if (!isKnown) {
    clearPrimaryAddressFields();
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
  if (!value) {
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
