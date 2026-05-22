<template>
  <div class="inputs-fields">
    <v-text-field
      :label="requiredLabel(t('cybercrime.prestataire'))"
      v-model="prestataire"
      :error-messages="prestataireError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintPrestataire')"
      persistent-hint
    />
    <v-text-field
      :label="requiredLabel(t('cybercrime.dateDecouverte'))"
      type="text"
      placeholder="JJ.MM.AAAA"
      v-model="dateDecouverte"
      :error-messages="dateDecouverteError"
      class="mb-8 mt-4"
      variant="outlined"
      prepend-inner-icon="mdi-calendar"
      :hint="t('cybercrime.hintDateDecouverte')"
      persistent-hint
      @input="onDateDecouverteInput"
    />

    <v-text-field
      :label="requiredLabel(t('cybercrime.montantDelit'))"
      type="number"
      v-model="montant"
      :error-messages="montantError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintMontantDelit')"
      persistent-hint
    />

    <BaseRadioGroup
      v-model="assurance"
      :label="t('cybercrime.assuranceDisponible')"
      required
      :options="[
        { label: t('common.oui'), value: true },
        { label: t('common.non'), value: false }
      ]"
      :error-messages="assuranceError"
      :hint="t('cybercrime.hintAssuranceDisponible')"
    />

    <v-text-field
      :label="emailCommandeInconnu ? t('cybercrime.emailCommande') : requiredLabel(t('cybercrime.emailCommande'))"
      v-model="emailCommande"
      :error-messages="emailCommandeError"
      class="mb-2"
      variant="outlined"
      :hint="t('cybercrime.hintEmailCommande')"
      persistent-hint
      :disabled="emailCommandeInconnu"
    />

    <v-checkbox
      v-model="emailCommandeInconnu"
      class="mt-0 mb-4"
      :label="t('cybercrime.emailInconnu')"
      hide-details
    />

    <PhoneInput
      v-model="telephoneCommande"
      :label="t('cybercrime.telephoneCommande')"
      :error-messages="telephoneCommandeError"
      :hint="t('cybercrime.hintTelephoneCommande')"
      input-class="mb-2"
      default-country-code="CH"
      :disabled="telephoneCommandeInconnu"
      :required="!telephoneCommandeInconnu"
    />

    <v-checkbox
      v-model="telephoneCommandeInconnu"
      class="mt-0 mb-4"
      :label="t('cybercrime.telephoneInconnu')"
      hide-details
    />

    <BaseRadioGroup
      v-model="livraisonAdresseLesee"
      :label="t('cybercrime.livraisonAdresseLesee')"
      required
      :options="[
        { label: t('common.oui'), value: true },
        { label: t('common.non'), value: false }
      ]"
      :error-messages="livraisonAdresseLeseeError"
      :hint="t('cybercrime.hintLivraisonAdresseLesee')"
    />

    <v-expand-transition>
      <div v-if="livraisonAdresseLesee == false" key="adresse-livraison-fields" class="adresse-vendeur-transition">
        <h3 class="text-h6 mb-6">{{ t("cybercrime.hintLivraisonAdresse") }}</h3>
        <AdresseEventFields
          instance-id="livraison-commande-frauduleuse"
          v-model:adresse="livraisonAdresse"
          v-model:adressePostale="livraisonAdressePostale"
          v-model:npa="livraisonNpa"
          v-model:localite="livraisonLocalite"
          v-model:localiteCode="livraisonLocaliteCode"
          v-model:pays="livraisonPays"
          :adresse-error="livraisonAdresseError"
          :adresse-postale-error="livraisonAdressePostaleError"
          :npa-error="livraisonNpaError"
          :localite-error="livraisonLocaliteError"
          :mark-required="false"
          field-class="mb-8"
        />
      </div>
    </v-expand-transition>
  </div>
</template>

<script setup lang="ts">
import { useField } from "vee-validate";
import { useI18n } from "vue-i18n";
import { applyDateMask } from "@/utils/helpers/dateHelpers.ts";
import BaseRadioGroup from "@/components/radio/BaseRadioGroup.vue";
import PhoneInput from "@/components/phone/PhoneInput.vue";
import AdresseEventFields from "@/components/adresse/AdresseEventFields.vue";
import { resetFieldsOnCondition } from "@/utils/helpers/formHelpers.ts";
import { requiredLabel } from "@/utils/helpers/labelHelpers";

const { t } = useI18n();

const { value: prestataire, errorMessage: prestataireError } = useField("prestataire");
const { value: dateDecouverte, errorMessage: dateDecouverteError } = useField<string>("dateDecouverte");
const { value: montant, errorMessage: montantError } = useField("montant");
const { value: assurance, errorMessage: assuranceError } = useField("assurance");
const { value: emailCommandeInconnu } = useField<boolean>("emailCommandeInconnu");
const { value: emailCommande, errorMessage: emailCommandeError } = useField("emailCommande");
const { value: telephoneCommandeInconnu } = useField<boolean>("telephoneCommandeInconnu");
const { value: telephoneCommande, errorMessage: telephoneCommandeError } = useField<string>("telephoneCommande");
const { value: livraisonAdresseLesee, errorMessage: livraisonAdresseLeseeError } = useField("livraisonAdresseLesee");
const { value: livraisonAdresse, errorMessage: livraisonAdresseError } = useField<string>("livraisonAdresse");
const { value: livraisonAdressePostale, errorMessage: livraisonAdressePostaleError } = useField<string>("livraisonAdressePostale");
const { value: livraisonNpa, errorMessage: livraisonNpaError } = useField<string>("livraisonNpa");
const { value: livraisonLocalite, errorMessage: livraisonLocaliteError } = useField<string>("livraisonLocalite");
const { value: livraisonLocaliteCode } = useField<string>("livraisonLocaliteCode");
const { value: livraisonPays } = useField<string>("livraisonPays");

const onDateDecouverteInput = (e: InputEvent) => {
  applyDateMask(e, dateDecouverte);
}

resetFieldsOnCondition(livraisonAdresseLesee, [
  livraisonAdresse,
  livraisonAdressePostale,
  livraisonNpa,
  livraisonLocalite,
  livraisonLocaliteCode,
  livraisonPays,
]);

resetFieldsOnCondition(emailCommandeInconnu, [emailCommande]);
resetFieldsOnCondition(telephoneCommandeInconnu, [telephoneCommande]);
</script>
