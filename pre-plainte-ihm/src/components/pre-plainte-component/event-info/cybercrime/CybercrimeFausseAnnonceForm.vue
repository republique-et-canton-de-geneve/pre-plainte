<template>
  <div class="inputs-fields">
    <v-text-field
      :label="t('cybercrime.urlComplete')"
      v-model="urlComplete"
      :error-messages="urlCompleteError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintUrlComplete')"
      persistent-hint
    />
    <v-text-field
      :label="t('cybercrime.titreAnnonce')"
      v-model="titreAnnonce"
      :error-messages="titreAnnonceError"
      class="mb-8 mt-4"
      variant="outlined"
      :hint="t('cybercrime.hintTitreAnnonce')"
      persistent-hint
    />

    <v-text-field
      :label="t('cybercrime.nomBailleur')"
      v-model="nomBailleur"
      :error-messages="nomBailleurError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintNomBailleur')"
      persistent-hint
    />

    <v-text-field
      :label="t('cybercrime.emailBailleur')"
      v-model="emailBailleur"
      :error-messages="emailBailleurError"
      class="mb-2"
      variant="outlined"
      :hint="t('cybercrime.hintEmailBailleur')"
      persistent-hint
      :disabled="emailBailleurInconnu"
    />

    <v-checkbox
      v-model="emailBailleurInconnu"
      class="mt-0 mb-4"
      :label="t('cybercrime.emailInconnu')"
      hide-details
    />

    <PhoneInput
      v-model="telephoneBailleur"
      :label="t('cybercrime.telephoneBailleur')"
      :error-messages="telephoneBailleurError"
      :hint="t('cybercrime.hintTelephoneBailleur')"
      input-class="mb-2"
      default-country-code="CH"
      :disabled="telephoneBailleurInconnu"
    />

    <v-checkbox
      v-model="telephoneBailleurInconnu"
      class="mt-0 mb-4"
      :label="t('cybercrime.telephoneInconnu')"
      hide-details
    />

    <v-text-field
      :label="t('cybercrime.adresseBienImmobilier')"
      v-model="adresseBienImmobilier"
      :error-messages="adresseBienImmobilierError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintAdresseBienImmobilier')"
      persistent-hint
    />

    <v-text-field
      :label="t('cybercrime.montantDemande')"
      v-model="montantDemande"
      :error-messages="montantDemandeError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintMontantDemande')"
      persistent-hint
    />

    <v-text-field
      :label="t('cybercrime.modePaiementDemande')"
      v-model="modePaiementDemande"
      :error-messages="modePaiementDemandeError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintModePaiementDemande')"
      persistent-hint
    />
  </div>
</template>

<script setup lang="ts">
import { useField } from "vee-validate";
import { useI18n } from "vue-i18n";
import PhoneInput from "@/components/phone/PhoneInput.vue";
import { resetFieldsOnCondition } from "@/utils/helpers/formHelpers.ts";

const { t } = useI18n();

const { value: urlComplete, errorMessage: urlCompleteError } = useField("urlComplete");
const { value: titreAnnonce, errorMessage: titreAnnonceError } = useField("titreAnnonce");
const { value: nomBailleur, errorMessage: nomBailleurError } = useField("nomBailleur");
const { value: emailBailleurInconnu } = useField<boolean>("emailBailleurInconnu");
const { value: emailBailleur, errorMessage: emailBailleurError } = useField("emailBailleur");
const { value: telephoneBailleurInconnu } = useField<boolean>("telephoneBailleurInconnu");
const { value: telephoneBailleur, errorMessage: telephoneBailleurError } = useField<string>("telephoneBailleur");
const { value: adresseBienImmobilier, errorMessage: adresseBienImmobilierError } = useField("adresseBienImmobilier");
const { value: montantDemande, errorMessage: montantDemandeError } = useField("montantDemande",);
const { value: modePaiementDemande, errorMessage: modePaiementDemandeError } = useField("modePaiementDemande");

resetFieldsOnCondition(emailBailleurInconnu, [emailBailleur]);
resetFieldsOnCondition(telephoneBailleurInconnu, [telephoneBailleur]);
</script>
