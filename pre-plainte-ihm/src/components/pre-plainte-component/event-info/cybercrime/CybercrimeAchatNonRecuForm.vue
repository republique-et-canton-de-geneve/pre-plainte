<template>
  <div class="inputs-fields">
    <v-text-field
      :label="requiredLabel(t('cybercrime.montantDelitAchatLigne'))"
      v-model="montantDelitAchatLigne"
      type="number"
      min="0"
      :error-messages="montantDelitAchatLigneError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintMontantDelitAchatLigne')"
      persistent-hint
    />
    <v-textarea
      :label="requiredLabel(t('cybercrime.descriptionCybercrime'))"
      v-model="descriptionCybercrime"
      :error-messages="descriptionCybercrimeError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintDescription')"
      persistent-hint
    />
    <v-textarea
      :label="requiredLabel(t('cybercrime.articleNonLivreDescription'))"
      v-model="articleNonLivreDescription"
      :error-messages="articleNonLivreDescriptionError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintArticleNonLivreDescription')"
      persistent-hint
    />
    <v-text-field
      :label="requiredLabel(t('cybercrime.prenomVendeur'))"
      v-model="prenomVendeur"
      :error-messages="prenomVendeurError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintPrenomVendeur')"
      persistent-hint
    />
    <v-text-field
      :label="requiredLabel(t('cybercrime.nomVendeur'))"
      v-model="nomVendeur"
      :error-messages="nomVendeurError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintNomVendeur')"
      persistent-hint
    />
    <PhoneInput
      v-model="telephoneVendeur"
      :label="t('cybercrime.telephoneVendeur')"
      :error-messages="telephoneVendeurError"
      :hint="t('cybercrime.hintTelephoneVendeur')"
      input-class="mb-2"
      default-country-code="CH"
      :disabled="telephoneVendeurInconnu"
      :required="!telephoneVendeurInconnu"
    />
    <v-checkbox
      v-model="telephoneVendeurInconnu"
      class="mt-0 mb-4"
      :label="t('cybercrime.telephoneInconnu')"
      hide-details
    />
    <v-text-field
      :label="emailVendeurInconnu ? t('cybercrime.emailVendeur') : requiredLabel(t('cybercrime.emailVendeur'))"
      v-model="emailVendeur"
      :error-messages="emailVendeurError"
      class="mb-2"
      variant="outlined"
      :hint="t('cybercrime.hintEmailVendeur')"
      persistent-hint
      :disabled="emailVendeurInconnu"
    />
    <v-checkbox
      v-model="emailVendeurInconnu"
      class="mt-0 mb-4"
      :label="t('cybercrime.emailInconnu')"
      hide-details
    />
    <h3 class="text-h6 mb-2">{{ t("cybercrime.adresseVendeur") }}</h3>
    <v-fade-transition>
      <div v-if="!adresseVendeurInconnue" key="adresse-vendeur-hint" class="adresse-vendeur-transition mb-4">
        <v-alert type="info" variant="tonal" density="comfortable" class="mb-0">
          {{ t("cybercrime.hintAdresseVendeurSiConnue") }}
        </v-alert>
      </div>
    </v-fade-transition>
    <v-checkbox
      v-model="adresseVendeurInconnue"
      class="mt-0 mb-4"
      :label="t('cybercrime.adresseVendeurInconnue')"
      hide-details
    />
    <v-expand-transition>
      <div v-if="!adresseVendeurInconnue" key="adresse-vendeur-fields" class="adresse-vendeur-transition">
        <AdresseEventFields
          instance-id="vendeur-achat-non-recu"
          v-model:adresse="vendeurAdresse"
          v-model:adressePostale="vendeurAdressePostale"
          v-model:npa="vendeurNpa"
          v-model:localite="vendeurLocalite"
          v-model:localiteCode="vendeurLocaliteCode"
          v-model:pays="vendeurPays"
          :adresse-error="vendeurAdresseError"
          :adresse-postale-error="vendeurAdressePostaleError"
          :npa-error="vendeurNpaError"
          :localite-error="vendeurLocaliteError"
          :mark-required="false"
          field-class="mb-8"
        />
      </div>
    </v-expand-transition>
    <BaseRadioGroup
      v-model="achatViaPlaceMarche"
      :label="t('cybercrime.titrePlaceMarche')"
      required
      :options="[
        { label: t('common.oui'), value: true },
        { label: t('common.non'), value: false }
      ]"
      :error-messages="achatViaPlaceMarcheError"
      :hint="t('cybercrime.hintTitrePlaceMarche')"
    />
    <AccessibleVSelect
      v-if="achatViaPlaceMarche"
      :label="t('cybercrime.plateformeUtilisee')"
      required
      v-model="plateforme"
      :items="plateformeOptions"
      item-title="label"
      item-value="value"
      :error-messages="plateformeError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintPlateformeUtilisee')"
      persistent-hint
    />
    <v-text-field
      v-if="achatViaPlaceMarche && plateforme === 'autre'"
      :label="requiredLabel(t('cybercrime.plateformeUtiliseeAutre'))"
      v-model="plateformeAutre"
      :error-messages="plateformeAutreError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintPlateformeUtiliseeAutre')"
      persistent-hint
    />
    <v-text-field
      v-if="achatViaPlaceMarche"
      :label="requiredLabel(t('cybercrime.plateformeId'))"
      v-model="plateformeId"
      :error-messages="plateformeIdError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintPlateformeId')"
      persistent-hint
    />
    <v-text-field
      v-if="!achatViaPlaceMarche"
      :label="requiredLabel(t('cybercrime.nomEntrepriseVendeur'))"
      v-model="nomEntrepriseVendeur"
      :error-messages="nomEntrepriseVendeurError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintNomEntrepriseVendeur')"
      persistent-hint
    />
    <v-text-field
      v-if="!achatViaPlaceMarche"
      :label="requiredLabel(t('cybercrime.siteWebEntrepriseVendeur'))"
      v-model="siteWebEntrepriseVendeur"
      :error-messages="siteWebEntrepriseVendeurError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintSiteWebEntrepriseVendeur')"
      persistent-hint
    />
    <h3 class="text-h6 mb-4">{{ t("cybercrime.telechargerAnnonceTitre") }}</h3>
    <div class="mb-8">
      <PieceJointe
        v-model="annonceDocument"
        :label="t('cybercrime.telechargerAnnonceTitre')"
        :multiple="false"
        :show-title="false"
        :required="!annonceDocumentIndisponible"
      />
      <v-checkbox
        v-model="annonceDocumentIndisponible"
        class="mt-0 mb-4"
        :label="t('cybercrime.documentNonDisponible')"
        hide-details
      />
      <v-textarea
        v-if="annonceDocumentIndisponible"
        :label="requiredLabel(t('cybercrime.raisonAbsenceAnnonce'))"
        v-model="raisonAbsenceAnnonce"
        :error-messages="raisonAbsenceAnnonceError"
        class="mt-2"
        variant="outlined"
        rows="4"
        auto-grow
      />
    </div>
    <AccessibleVSelect
      :label="t('cybercrime.moyenPaiement')"
      required
      v-model="moyenPaiement"
      :items="moyenPaiementOptions"
      item-title="label"
      item-value="value"
      :error-messages="moyenPaiementError"
      class="mb-4"
      variant="outlined"
      :hint="t('cybercrime.hintMoyenPaiement')"
      persistent-hint
    />
    <v-alert v-if="moyenPaiement" type="info" variant="tonal" class="mb-8" :icon="false">
      <div class="d-flex align-center" style="gap: 10px">
        <v-icon>{{ moyenPaiementIcon }}</v-icon>
        <span>{{ t("cybercrime.modePaiementSelectionne") }}: {{ moyenPaiementLabel }}</span>
      </div>
    </v-alert>
    <v-text-field
      v-if="moyenPaiement === 'autre'"
      :label="requiredLabel(t('cybercrime.moyenPaiementAutre'))"
      v-model="moyenPaiementAutre"
      :error-messages="moyenPaiementAutreError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintMoyenPaiementAutre')"
      persistent-hint
    />
    <v-text-field
      v-if="moyenPaiement === 'iban'"
      :label="requiredLabel(t('cybercrime.ibanBeneficiaire'))"
      v-model="ibanBeneficiaire"
      :error-messages="ibanBeneficiaireError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintIbanBeneficiaire')"
      persistent-hint
    />
    <v-text-field
      v-if="moyenPaiement === 'paypal'"
      :label="requiredLabel(t('cybercrime.comptePaypalBeneficiaire'))"
      v-model="comptePaypalBeneficiaire"
      :error-messages="comptePaypalBeneficiaireError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintComptePaypalBeneficiaire')"
      persistent-hint
    />
    <v-text-field
      v-if="moyenPaiement === 'twint'"
      :label="requiredLabel(t('cybercrime.numeroTwintBeneficiaire'))"
      v-model="numeroTwintBeneficiaire"
      :error-messages="numeroTwintBeneficiaireError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintNumeroTwintBeneficiaire')"
      persistent-hint
    />
    <v-text-field
      v-if="moyenPaiement === 'crypto'"
      :label="requiredLabel(t('cybercrime.adresseWalletCrypto'))"
      v-model="adresseWalletCrypto"
      :error-messages="adresseWalletCryptoError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintAdresseWalletCrypto')"
      persistent-hint
    />
    <v-text-field
      v-if="moyenPaiement === 'crypto'"
      :label="requiredLabel(t('cybercrime.hashTransactionCrypto'))"
      v-model="hashTransactionCrypto"
      :error-messages="hashTransactionCryptoError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintHashTransactionCrypto')"
      persistent-hint
    />
    <v-text-field
      :label="t('cybercrime.societeBeneficiaire')"
      v-model="societeBeneficiaire"
      :error-messages="societeBeneficiaireError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintSocieteBeneficiaire')"
      persistent-hint
    />
    <v-text-field
      :label="t('cybercrime.nomBeneficiaire')"
      v-model="nomBeneficiaire"
      :error-messages="nomBeneficiaireError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintNomBeneficiaire')"
      persistent-hint
    />
    <v-text-field
      :label="t('cybercrime.prenomBeneficiaire')"
      v-model="prenomBeneficiaire"
      :error-messages="prenomBeneficiaireError"
      class="mb-8"
      variant="outlined"
      :hint="t('cybercrime.hintPrenomBeneficiaire')"
      persistent-hint
    />
    <v-text-field
      :label="requiredLabel(t('cybercrime.dateOperation'))"
      v-model="dateOperation"
      type="text"
      placeholder="JJ.MM.AAAA"
      :error-messages="dateOperationError"
      class="mb-8"
      variant="outlined"
      prepend-inner-icon="mdi-calendar"
      :hint="t('cybercrime.hintDateOperation')"
      persistent-hint
      @input="onDateOperationInput"
    />
    <h3 class="text-h6 mb-4">{{ t("cybercrime.telechargerPreuvePaiement") }}</h3>
    <div class="mb-8">
      <PieceJointe
        v-model="preuvePaiementDocument"
        :label="t('cybercrime.telechargerPreuvePaiement')"
        :multiple="false"
        :show-title="false"
        accept=".pdf"
        :max-file-size-bytes="maxPieceJointe5Mo"
        :required="!preuvePaiementIndisponible"
      />
      <v-checkbox
        v-model="preuvePaiementIndisponible"
        class="mt-0 mb-4"
        :label="t('cybercrime.documentNonDisponible')"
        hide-details
      />
      <v-textarea
        v-if="preuvePaiementIndisponible"
        :label="requiredLabel(t('cybercrime.raisonAbsencePreuvePaiement'))"
        v-model="raisonAbsencePreuvePaiement"
        :error-messages="raisonAbsencePreuvePaiementError"
        class="mt-2 mb-4"
        variant="outlined"
        rows="4"
        auto-grow
      />
    </div>
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
        required
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
        required
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { useField } from "vee-validate";
import { computed, watch } from "vue";
import { useI18n } from "vue-i18n";
import PhoneInput from "@/components/phone/PhoneInput.vue";
import AccessibleVSelect from "@/components/accessibility/AccessibleVSelect.vue";
import AdresseEventFields from "@/components/adresse/AdresseEventFields.vue";
import PieceJointe from "@/components/piece-jointe/PieceJointe.vue";
import { applyDateMask } from "@/utils/helpers/dateHelpers.ts";
import { MAX_PIECE_JOINTE_SINGLE_5MO, MOYEN_PAIEMENT } from "@/constants/constant";
import BaseRadioGroup from "@/components/radio/BaseRadioGroup.vue";
import { resetFieldsOnCondition, resetFieldsOnToggle, resetFilesOnCondition } from "@/utils/helpers/formHelpers.ts";
import { requiredLabel } from "@/utils/helpers/labelHelpers";

const { t } = useI18n();

const maxPieceJointe5Mo = MAX_PIECE_JOINTE_SINGLE_5MO;

const { value: montantDelitAchatLigne, errorMessage: montantDelitAchatLigneError } = useField("montantDelitAchatLigne");
const { value: descriptionCybercrime, errorMessage: descriptionCybercrimeError } = useField("descriptionCybercrime");
const { value: articleNonLivreDescription, errorMessage: articleNonLivreDescriptionError } = useField("articleNonLivreDescription");

const { value: prenomVendeur, errorMessage: prenomVendeurError } = useField("prenomVendeur");
const { value: nomVendeur, errorMessage: nomVendeurError } = useField("nomVendeur");
const { value: telephoneVendeur, errorMessage: telephoneVendeurError } = useField<string>("telephoneVendeur");
const { value: telephoneVendeurInconnu } = useField<boolean>("telephoneVendeurInconnu");
const { value: emailVendeurInconnu } = useField<boolean>("emailVendeurInconnu");
const { value: emailVendeur, errorMessage: emailVendeurError } = useField<string>("emailVendeur");
const { value: adresseVendeurInconnue } = useField<boolean>("adresseVendeurInconnue");
const { value: vendeurAdresse, errorMessage: vendeurAdresseError } = useField<string>("vendeurAdresse");
const { value: vendeurAdressePostale, errorMessage: vendeurAdressePostaleError } = useField<string>("vendeurAdressePostale");
const { value: vendeurNpa, errorMessage: vendeurNpaError } = useField<string>("vendeurNpa");
const { value: vendeurLocalite, errorMessage: vendeurLocaliteError } = useField<string>("vendeurLocalite");
const { value: vendeurLocaliteCode } = useField<string>("vendeurLocaliteCode");
const { value: vendeurPays } = useField<string>("vendeurPays");

const { value: achatViaPlaceMarche, errorMessage: achatViaPlaceMarcheError } = useField<boolean>("achatViaPlaceMarche");
const { value: plateforme, errorMessage: plateformeError } = useField("plateforme");
const { value: plateformeAutre, errorMessage: plateformeAutreError } = useField("plateformeAutre");
const { value: plateformeId, errorMessage: plateformeIdError } = useField("plateformeId");
const { value: nomEntrepriseVendeur, errorMessage: nomEntrepriseVendeurError } = useField("nomEntrepriseVendeur");
const { value: siteWebEntrepriseVendeur, errorMessage: siteWebEntrepriseVendeurError } = useField("siteWebEntrepriseVendeur");
const { value: annonceDocument } = useField<File[]>("annonceDocument");
const { value: annonceDocumentIndisponible } = useField<boolean>("annonceDocumentIndisponible");
const { value: raisonAbsenceAnnonce, errorMessage: raisonAbsenceAnnonceError } = useField("raisonAbsenceAnnonce");
const { value: moyenPaiement, errorMessage: moyenPaiementError } = useField("moyenPaiement");
const { value: moyenPaiementAutre, errorMessage: moyenPaiementAutreError } = useField("moyenPaiementAutre");
const { value: ibanBeneficiaire, errorMessage: ibanBeneficiaireError } = useField("ibanBeneficiaire");
const { value: comptePaypalBeneficiaire, errorMessage: comptePaypalBeneficiaireError } = useField("comptePaypalBeneficiaire");
const { value: numeroTwintBeneficiaire, errorMessage: numeroTwintBeneficiaireError } = useField("numeroTwintBeneficiaire");
const { value: adresseWalletCrypto, errorMessage: adresseWalletCryptoError } = useField("adresseWalletCrypto");
const { value: hashTransactionCrypto, errorMessage: hashTransactionCryptoError } = useField("hashTransactionCrypto");
const { value: societeBeneficiaire, errorMessage: societeBeneficiaireError } = useField("societeBeneficiaire");
const { value: nomBeneficiaire, errorMessage: nomBeneficiaireError } = useField("nomBeneficiaire");
const { value: prenomBeneficiaire, errorMessage: prenomBeneficiaireError } = useField("prenomBeneficiaire");
const { value: dateOperation, errorMessage: dateOperationError } = useField("dateOperation");
const { value: preuvePaiementDocument } = useField<File[]>("preuvePaiementDocument");
const { value: preuvePaiementIndisponible } = useField<boolean>("preuvePaiementIndisponible");
const { value: raisonAbsencePreuvePaiement, errorMessage: raisonAbsencePreuvePaiementError } = useField("raisonAbsencePreuvePaiement");
const { value: copieIdentiteTransmiseAuteur, errorMessage: copieIdentiteTransmiseAuteurError } = useField<boolean>("copieIdentiteTransmiseAuteur");
const { value: copieIdentiteTransmiseAuteurDocument } = useField<File[]>("copieIdentiteTransmiseAuteurDocument");
const { value: copieIdentiteAuteurTransmise, errorMessage: copieIdentiteAuteurTransmiseError } = useField<boolean>("copieIdentiteAuteurTransmise");
const { value: copieIdentiteAuteurDocument } = useField<File[]>("copieIdentiteAuteurDocument");

const createInputHandler = (maskFn: (e: InputEvent, value: any) => void, target: any) => (e: InputEvent) => {
  maskFn(e, target);
};

const onDateOperationInput = createInputHandler(applyDateMask, dateOperation);

const plateformeOptions = computed(() => [
  { label: "Facebook Marketplace", value: "facebook" },
  { label: "Ricardo", value: "ricardo" },
  { label: "Tutti", value: "tutti" },
  { label: "Anibis", value: "anibis" },
  { label: t("common.autre"), value: "autre" },
]);

const moyenPaiementOptions = computed(() => [
  { label: t("cybercrime.compteBancaire"), value: MOYEN_PAIEMENT.IBAN },
  { label: "Paypal", value: MOYEN_PAIEMENT.PAYPAL },
  { label: "Twint", value: MOYEN_PAIEMENT.TWINT },
  { label: t("cybercrime.crypto"), value: MOYEN_PAIEMENT.CRYPTO },
  { label: t("common.autre"), value: MOYEN_PAIEMENT.AUTRE },
]);

const moyenPaiementLabel = computed(() => {
  const selected = moyenPaiementOptions.value.find(opt => opt.value === moyenPaiement.value);
  return selected?.label ?? "";
});

const moyenPaiementIcon = computed(() => {
  switch (moyenPaiement.value) {
    case MOYEN_PAIEMENT.IBAN:
      return "mdi-bank";
    case MOYEN_PAIEMENT.PAYPAL:
      return "mdi-paypal";
    case MOYEN_PAIEMENT.TWINT:
      return "mdi-cellphone";
    case MOYEN_PAIEMENT.CRYPTO:
      return "mdi-currency-btc";
    default:
      return "mdi-cash";
  }
});

resetFieldsOnCondition(emailVendeurInconnu, [emailVendeur]);
resetFieldsOnCondition(telephoneVendeurInconnu, [telephoneVendeur]);

resetFieldsOnCondition(adresseVendeurInconnue, [
  vendeurAdresse,
  vendeurAdressePostale,
  vendeurNpa,
  vendeurLocalite,
  vendeurLocaliteCode,
  vendeurPays,
]);

resetFieldsOnToggle(achatViaPlaceMarche,
  () => {
    nomEntrepriseVendeur.value = "";
    siteWebEntrepriseVendeur.value = "";
  },
  () => {
    plateforme.value = "";
    plateformeAutre.value = "";
    plateformeId.value = "";
  }
);

resetFieldsOnToggle(annonceDocumentIndisponible,
  () => {
    annonceDocument.value = [];
  },
  () => {
    raisonAbsenceAnnonce.value = "";
  }
);

resetFieldsOnToggle(preuvePaiementIndisponible,
  () => {
    preuvePaiementDocument.value = [];
  },
  () => {
    raisonAbsencePreuvePaiement.value = "";
  }
);

resetFilesOnCondition(
  copieIdentiteTransmiseAuteur,
  [copieIdentiteTransmiseAuteurDocument],
  isYes => !isYes
);

resetFilesOnCondition(
  copieIdentiteAuteurTransmise,
  [copieIdentiteAuteurDocument],
  isYes => !isYes
);

watch(moyenPaiement, mode => {
  if (mode !== MOYEN_PAIEMENT.IBAN) {
    ibanBeneficiaire.value = "";
  }
  if (mode !== MOYEN_PAIEMENT.PAYPAL) {
    comptePaypalBeneficiaire.value = "";
  }
  if (mode !== MOYEN_PAIEMENT.TWINT) {
    numeroTwintBeneficiaire.value = "";
  }
  if (mode !== MOYEN_PAIEMENT.CRYPTO) {
    adresseWalletCrypto.value = "";
    hashTransactionCrypto.value = "";
  }
  if (mode !== MOYEN_PAIEMENT.AUTRE) {
    moyenPaiementAutre.value = "";
  }
});
</script>
