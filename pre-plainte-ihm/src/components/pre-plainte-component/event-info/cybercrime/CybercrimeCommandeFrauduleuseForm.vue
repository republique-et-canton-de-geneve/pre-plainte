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

    <BaseRadioGroup
      v-model="moyenPaiementNumeriqueDebite"
      :label="t('cybercrime.moyenPaiementNumeriqueDebite')"
      required
      :options="[
        { label: t('common.oui'), value: true },
        { label: t('common.non'), value: false }
      ]"
      :error-messages="moyenPaiementNumeriqueDebiteError"
      :hint="t('cybercrime.hintMoyenPaiementNumeriqueDebite')"
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

    <v-text-field
      :label="requiredLabel(t('cybercrime.prenomContrevenant'))"
      v-model="prenomContrevenant"
      :error-messages="prenomContrevenantError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintPrenomContrevenant')"
      persistent-hint
    />

    <v-text-field
      :label="requiredLabel(t('cybercrime.nomContrevenant'))"
      v-model="nomContrevenant"
      :error-messages="nomContrevenantError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintNomContrevenant')"
      persistent-hint
    />

    <v-text-field
      :label="t('cybercrime.siteWebContrevenant')"
      v-model="siteWebContrevenant"
      :error-messages="siteWebContrevenantError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintSiteWebContrevenant')"
      persistent-hint
    />

    <h3 class="text-h6 mb-6">{{ t("cybercrime.adresseContrevenant") }}</h3>
    <p class="text-body-2 mb-6">{{ t("cybercrime.hintAdresseContrevenant") }}</p>
    <AdresseEventFields
      instance-id="contrevenant-commande-frauduleuse"
      v-model:adresse="contrevenantAdresse"
      v-model:adressePostale="contrevenantAdressePostale"
      v-model:npa="contrevenantNpa"
      v-model:localite="contrevenantLocalite"
      v-model:localiteCode="contrevenantLocaliteCode"
      v-model:pays="contrevenantPays"
      :adresse-error="contrevenantAdresseError"
      :adresse-postale-error="contrevenantAdressePostaleError"
      :npa-error="contrevenantNpaError"
      :localite-error="contrevenantLocaliteError"
      :mark-required="false"
      field-class="mb-8"
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

    <BaseRadioGroup
      v-model="copieIdentiteTransmiseAuteur"
      :label="t('cybercrime.copieIdentiteTransmiseAuteur')"
      required
      :options="[
        { label: t('common.oui'), value: true },
        { label: t('common.non'), value: false }
      ]"
      :error-messages="copieIdentiteTransmiseAuteurError"
      :hint="t('cybercrime.hintCopieIdentiteTransmiseAuteur')"
    />
    <div v-if="copieIdentiteTransmiseAuteur" class="mb-8">
      <PieceJointe
        v-model="copieIdentiteTransmiseAuteurDocument"
        :label="t('cybercrime.telechargerCopieIdentiteTransmiseAuteur')"
        :multiple="false"
        :required="!copieIdentiteTransmiseAuteurDocumentIndisponible"
        :error-messages="copieIdentiteTransmiseAuteurDocumentError"
      />
      <v-checkbox
        v-model="copieIdentiteTransmiseAuteurDocumentIndisponible"
        class="mt-0 mb-4"
        :label="t('cybercrime.documentNonDisponible')"
        hide-details
      />
      <v-textarea
        v-if="copieIdentiteTransmiseAuteurDocumentIndisponible"
        :label="requiredLabel(t('cybercrime.raisonAbsenceCopieIdentiteTransmiseAuteur'))"
        v-model="raisonAbsenceCopieIdentiteTransmiseAuteur"
        :error-messages="raisonAbsenceCopieIdentiteTransmiseAuteurError"
        class="mt-2 mb-4"
        variant="outlined"
        rows="4"
      />
    </div>

    <BaseRadioGroup
      v-model="copieIdentiteAuteurTransmise"
      :label="t('cybercrime.copieIdentiteAuteurTransmise')"
      required
      :options="[
        { label: t('common.oui'), value: true },
        { label: t('common.non'), value: false }
      ]"
      :error-messages="copieIdentiteAuteurTransmiseError"
      :hint="t('cybercrime.hintCopieIdentiteAuteurTransmise')"
    />
    <div v-if="copieIdentiteAuteurTransmise" class="mb-8">
      <PieceJointe
        v-model="copieIdentiteAuteurDocument"
        :label="t('cybercrime.telechargerCopieIdentiteAuteurTransmise')"
        :multiple="false"
        :required="!copieIdentiteAuteurDocumentIndisponible"
        :error-messages="copieIdentiteAuteurDocumentError"
      />
      <v-checkbox
        v-model="copieIdentiteAuteurDocumentIndisponible"
        class="mt-0 mb-4"
        :label="t('cybercrime.documentNonDisponible')"
        hide-details
      />
      <v-textarea
        v-if="copieIdentiteAuteurDocumentIndisponible"
        :label="requiredLabel(t('cybercrime.raisonAbsenceCopieIdentiteAuteur'))"
        v-model="raisonAbsenceCopieIdentiteAuteur"
        :error-messages="raisonAbsenceCopieIdentiteAuteurError"
        class="mt-2 mb-4"
        variant="outlined"
        rows="4"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useField } from "vee-validate";
import { useI18n } from "vue-i18n";
import { applyDateMask } from "@/utils/helpers/dateHelpers.ts";
import BaseRadioGroup from "@/components/radio/BaseRadioGroup.vue";
import PhoneInput from "@/components/phone/PhoneInput.vue";
import AdresseEventFields from "@/components/adresse/AdresseEventFields.vue";
import PieceJointe from "@/components/piece-jointe/PieceJointe.vue";
import { resetFieldsOnCondition, resetFieldsOnToggle, resetFilesOnCondition } from "@/utils/helpers/formHelpers.ts";
import { requiredLabel } from "@/utils/helpers/labelHelpers";

const { t } = useI18n();

const { value: prestataire, errorMessage: prestataireError } = useField("prestataire");
const { value: dateDecouverte, errorMessage: dateDecouverteError } = useField<string>("dateDecouverte");
const { value: montant, errorMessage: montantError } = useField("montant");
const { value: assurance, errorMessage: assuranceError } = useField("assurance");
const { value: moyenPaiementNumeriqueDebite, errorMessage: moyenPaiementNumeriqueDebiteError } =
  useField("moyenPaiementNumeriqueDebite");
const { value: emailCommandeInconnu } = useField<boolean>("emailCommandeInconnu");
const { value: emailCommande, errorMessage: emailCommandeError } = useField("emailCommande");
const { value: telephoneCommandeInconnu } = useField<boolean>("telephoneCommandeInconnu");
const { value: telephoneCommande, errorMessage: telephoneCommandeError } = useField<string>("telephoneCommande");
const { value: prenomContrevenant, errorMessage: prenomContrevenantError } = useField("prenomContrevenant");
const { value: nomContrevenant, errorMessage: nomContrevenantError } = useField("nomContrevenant");
const { value: siteWebContrevenant, errorMessage: siteWebContrevenantError } = useField("siteWebContrevenant");
const { value: contrevenantAdresse, errorMessage: contrevenantAdresseError } = useField<string>("contrevenantAdresse");
const { value: contrevenantAdressePostale, errorMessage: contrevenantAdressePostaleError } =
  useField<string>("contrevenantAdressePostale");
const { value: contrevenantNpa, errorMessage: contrevenantNpaError } = useField<string>("contrevenantNpa");
const { value: contrevenantLocalite, errorMessage: contrevenantLocaliteError } = useField<string>("contrevenantLocalite");
const { value: contrevenantLocaliteCode } = useField<string>("contrevenantLocaliteCode");
const { value: contrevenantPays } = useField<string>("contrevenantPays");
const { value: livraisonAdresseLesee, errorMessage: livraisonAdresseLeseeError } = useField("livraisonAdresseLesee");
const { value: livraisonAdresse, errorMessage: livraisonAdresseError } = useField<string>("livraisonAdresse");
const { value: livraisonAdressePostale, errorMessage: livraisonAdressePostaleError } = useField<string>("livraisonAdressePostale");
const { value: livraisonNpa, errorMessage: livraisonNpaError } = useField<string>("livraisonNpa");
const { value: livraisonLocalite, errorMessage: livraisonLocaliteError } = useField<string>("livraisonLocalite");
const { value: livraisonLocaliteCode } = useField<string>("livraisonLocaliteCode");
const { value: livraisonPays } = useField<string>("livraisonPays");

const { value: copieIdentiteTransmiseAuteur, errorMessage: copieIdentiteTransmiseAuteurError } =
  useField<boolean>("copieIdentiteTransmiseAuteur");
const { value: copieIdentiteTransmiseAuteurDocument, errorMessage: copieIdentiteTransmiseAuteurDocumentError } =
  useField<File[]>("copieIdentiteTransmiseAuteurDocument");
const { value: copieIdentiteTransmiseAuteurDocumentIndisponible } =
  useField<boolean>("copieIdentiteTransmiseAuteurDocumentIndisponible");
const { value: raisonAbsenceCopieIdentiteTransmiseAuteur, errorMessage: raisonAbsenceCopieIdentiteTransmiseAuteurError } =
  useField<string>("raisonAbsenceCopieIdentiteTransmiseAuteur");
const { value: copieIdentiteAuteurTransmise, errorMessage: copieIdentiteAuteurTransmiseError } =
  useField<boolean>("copieIdentiteAuteurTransmise");
const { value: copieIdentiteAuteurDocument, errorMessage: copieIdentiteAuteurDocumentError } =
  useField<File[]>("copieIdentiteAuteurDocument");
const { value: copieIdentiteAuteurDocumentIndisponible } = useField<boolean>("copieIdentiteAuteurDocumentIndisponible");
const { value: raisonAbsenceCopieIdentiteAuteur, errorMessage: raisonAbsenceCopieIdentiteAuteurError } =
  useField<string>("raisonAbsenceCopieIdentiteAuteur");

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

resetFilesOnCondition(copieIdentiteTransmiseAuteur, [copieIdentiteTransmiseAuteurDocument], isYes => !isYes);

resetFieldsOnToggle(
  copieIdentiteTransmiseAuteurDocumentIndisponible,
  () => {
    copieIdentiteTransmiseAuteurDocument.value = [];
  },
  () => {
    raisonAbsenceCopieIdentiteTransmiseAuteur.value = "";
  },
);

resetFieldsOnToggle(
  copieIdentiteTransmiseAuteur,
  () => {},
  () => {
    copieIdentiteTransmiseAuteurDocumentIndisponible.value = false;
    raisonAbsenceCopieIdentiteTransmiseAuteur.value = "";
  },
);

resetFilesOnCondition(copieIdentiteAuteurTransmise, [copieIdentiteAuteurDocument], isYes => !isYes);

resetFieldsOnToggle(
  copieIdentiteAuteurDocumentIndisponible,
  () => {
    copieIdentiteAuteurDocument.value = [];
  },
  () => {
    raisonAbsenceCopieIdentiteAuteur.value = "";
  },
);

resetFieldsOnToggle(
  copieIdentiteAuteurTransmise,
  () => {},
  () => {
    copieIdentiteAuteurDocumentIndisponible.value = false;
    raisonAbsenceCopieIdentiteAuteur.value = "";
  },
);
</script>
