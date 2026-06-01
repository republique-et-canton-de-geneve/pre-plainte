<template>
  <v-form @submit.prevent="onSubmit">
    <h1 class="mb-4 text-h1 text-md-h2 d-none d-md-block">{{ t("titreApplication.prePlainte") }}</h1>
    <v-card class="pa-2 pa-md-6 mb-4">
      <h2 class="pre-plainte-main-card-title mb-4 text-h2">{{ t("informationsEvenement.titre") }}</h2>
      <h3>{{ t("informationsEvenement.typeIncident") }}</h3>
      <AccessibleVSelect
        v-model="typeIncident"
        :label="t('informationsEvenement.typeIncident')"
        required
        :items="[
          { label: t('incidentTypes.vol'), value: 'vol' },
          { label: t('dommages.titre'), value: 'degat-delit' },
          { label: t('cybercrime.titre'), value: 'cybercrime' },
        ]"
        :error-messages="typeIncidentError"
        :hint="t('informationsEvenement.hintTypeIncident')"
        persistent-hint
        clearable
        class="mb-8 mt-5"
      />
      <VolForm v-if="typeIncident === 'vol'" />
      <DegatMaterielForm v-if="typeIncident === 'degat-delit'" />

      <div v-if="typeIncident === 'cybercrime'" class="inputs-fields">
        <AccessibleVSelect
          v-model="typeCybercrime"
          :items="typeCybercrimeOptions"
          item-title="label"
          item-value="value"
          :label="t('cybercrime.type')"
          required
          :error-messages="typeCybercrimeError"
          variant="outlined"
          class="mb-8"
          :hint="t('cybercrime.hintType')"
          persistent-hint
          clearable
        />
        <v-textarea
          v-if="showCybercrimeUrlDescriptionAndPieces"
          clearable
          :label="requiredLabel(t('cybercrime.description'))"
          v-model="descriptionCybercrime"
          :error-messages="descriptionCybercrimeError"
          class="mb-8"
          variant="outlined"
          :hint="t('cybercrime.hintDescription')"
          persistent-hint
        />
        <CybercrimeCommandeFrauduleuseForm v-if="typeCybercrime === TYPE_CYBERCRIME_COMMANDE_FRAUDULEUSE" />
        <CybercrimeAchatNonRecuForm v-if="typeCybercrime === TYPE_CYBERCRIME_ACHAT_NON_RECU" />
        <CybercrimeFausseAnnonceForm v-if="typeCybercrime === TYPE_CYBERCRIME_FAUSSE_ANNONCE" />
        <div v-if="showCybercrimeUrlDescriptionAndPieces" class="mb-8">
          <PieceJointe v-model="justificatifsPaiement" :label="t('cybercrime.justificatifsPaiement')" />
        </div>
        <div v-if="showCybercrimeUrlDescriptionAndPieces" class="mb-8">
          <PieceJointe v-model="copiesEcran" :label="t('cybercrime.copiesEcran')" />
        </div>
        <div class="mb-8">
          <PieceJointe v-model="autresDocuments" :label="t('cybercrime.autresDocuments')" />
        </div>
      </div>

      <div v-if="typeIncidentError" class="mb-4">
        <v-alert type="error" variant="tonal" class="mb-8" :icon="mobile ? false : undefined">
          {{ typeIncidentError }}
        </v-alert>
      </div>

      <template v-if="typeIncident === 'vol' || typeIncident === 'degat-delit' || typeCybercrime === TYPE_CYBERCRIME_COMMANDE_FRAUDULEUSE">
        <v-row class="mb-4" align="center" dense>
          <v-col cols="12" md="6">
            <v-text-field
              :label="requiredLabel(t('informationsEvenement.dateDebutEvenement'))"
              v-model="dateDebutEvenement"
              :error-messages="dateDebutEvenementError"
              type="text"
              placeholder="JJ.MM.AAAA"
              variant="outlined"
              prepend-inner-icon="mdi-calendar"
              :hint="t('informationsEvenement.hintDateDebutEvenement')"
              persistent-hint
              @input="onDateDebutEvenementInput"
            />
          </v-col>
          <v-col cols="12" md="6" class="mt-4 mt-md-0">
            <v-text-field
              :label="requiredLabel(t('informationsEvenement.heureDebutEvenement'))"
              v-model="heureDebutEvenement"
              :error-messages="heureDebutEvenementError"
              type="text"
              placeholder="HH:MM"
              variant="outlined"
              :hint="t('informationsEvenement.hintHeureDebutEvenement')"
              persistent-hint
              @input="onHeureDebutEvenementInput"
            />
          </v-col>
        </v-row>

        <v-row class="mb-4" align="center" dense>
          <v-col cols="12" md="6">
            <v-text-field
              :label="requiredLabel(t('informationsEvenement.dateFinEvenement'))"
              v-model="dateFinEvenement"
              :error-messages="dateFinEvenementError"
              type="text"
              placeholder="JJ.MM.AAAA"
              variant="outlined"
              prepend-inner-icon="mdi-calendar"
              :hint="t('informationsEvenement.hintDateFinEvenement')"
              persistent-hint
              @input="onDateFinEvenementInput"
            />
          </v-col>
          <v-col cols="12" md="6" class="mt-4 mt-md-0">
            <v-text-field
              :label="requiredLabel(t('informationsEvenement.heureFinEvenement'))"
              v-model="heureFinEvenement"
              :error-messages="heureFinEvenementError"
              type="text"
              placeholder="HH:MM"
              variant="outlined"
              :hint="t('informationsEvenement.hintHeureFinEvenement')"
              persistent-hint
              @input="onHeureFinEvenementInput"
            />
          </v-col>
        </v-row>
      </template>

      <template v-if="typeCybercrime === TYPE_CYBERCRIME_ACHAT_NON_RECU || typeCybercrime === TYPE_CYBERCRIME_FAUSSE_ANNONCE">
        <v-row class="mb-4 mt-4" align="center" dense>
          <v-col cols="12" md="6">
            <v-text-field
              :label="requiredLabel(t('cybercrime.datePremierContact'))"
              v-model="datePremierContact"
              type="text"
              placeholder="JJ.MM.AAAA"
              :error-messages="datePremierContactError"
              variant="outlined"
              prepend-inner-icon="mdi-calendar"
              :hint="t('cybercrime.hintDatePremierContact')"
              persistent-hint
              @input="onDatePremierContactInput"
            />
          </v-col>
          <v-col cols="12" md="6" class="mt-4 mt-md-0">
            <v-text-field
              :label="requiredLabel(t('cybercrime.heurePremierContact'))"
              v-model="heurePremierContact"
              :error-messages="heurePremierContactError"
              type="text"
              placeholder="HH:MM"
              variant="outlined"
              :hint="t('cybercrime.hintHeurePremierContact')"
              persistent-hint
              @input="onHeurePremierContactInput"
            />
          </v-col>
        </v-row>
        <v-row class="mb-4" align="center" dense>
          <v-col cols="12" md="6">
            <v-text-field
              :label="requiredLabel(t('cybercrime.dateDernierContact'))"
              v-model="dateDernierContact"
              type="text"
              placeholder="JJ.MM.AAAA"
              :error-messages="dateDernierContactError"
              variant="outlined"
              prepend-inner-icon="mdi-calendar"
              :hint="t('cybercrime.hintDateDernierContact')"
              persistent-hint
              @input="onDateDernierContactInput"
            />
          </v-col>
          <v-col cols="12" md="6" class="mt-4 mt-md-0">
            <v-text-field
              :label="requiredLabel(t('cybercrime.heureDernierContact'))"
              v-model="heureDernierContact"
              :error-messages="heureDernierContactError"
              type="text"
              placeholder="HH:MM"
              variant="outlined"
              :hint="t('cybercrime.hintHeureDernierContact')"
              persistent-hint
              @input="onHeureDernierContactInput"
            />
          </v-col>
        </v-row>
      </template>

      <div v-if="typeIncident === 'vol'" class="mb-8">
        <PieceJointe v-model="fichiers" :label="t('dommages.fichiers')" />
      </div>
      <AdresseEvent v-if="typeIncident !== 'cybercrime'" />

      <div class="d-md-none mt-4">
        <div class="pre-plainte-mobile-step-actions d-flex flex-column gap-4 mb-2">
          <v-btn variant="outlined" color="primary" class="w-100" @click="handleCancelClick">
            {{ t("common.precedent") }}
          </v-btn>
          <v-btn type="submit" variant="flat" color="primary" class="w-100" @click="onSubmit">
            {{ t("common.continuer") }}
          </v-btn>
        </div>
        <div class="d-flex justify-center">
          <v-btn variant="plain" color="primary" class="pa-0" @click="handleSaveClick">
            {{ t("common.sauvegarder") }}
          </v-btn>
        </div>
      </div>
    </v-card>
    <div class="d-md-none mt-4 d-flex flex-column align-center gap-2">
      <ExitActionsForm :is-mobile="true" />
    </div>

    <v-row class="mt-4 d-none d-md-flex" align="center">
      <v-col cols="12" md="auto" class="d-flex">
        <v-btn variant="plain" color="primary" @click="handleSaveClick">
          {{ t("common.sauvegarder") }}
        </v-btn>
      </v-col>
      <v-spacer />
      <v-col cols="12" md="auto" class="d-flex justify-end">
        <v-btn variant="outlined" color="primary" class="me-4" @click="handleCancelClick">
          {{ t("common.precedent") }}
        </v-btn>
        <v-btn type="submit" variant="flat" color="primary" @click="onSubmit">
          {{ t("common.poursuivre") }}
        </v-btn>
      </v-col>
    </v-row>
  </v-form>
</template>

<script setup lang="ts">
import { useCreatePrePlainteStore } from "@/stores/createPrePlainteStore";
import type { PrePlainteFormFields } from "@/types/pre-plainte.interface";
import { toTypedSchema } from "@vee-validate/zod";
import { useField, useForm } from "vee-validate";
import { computed, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useDisplay } from "vuetify";
import { createIncidentSchema } from "@/schemas/incident-evenement.schema.ts";
import type { AddressResult } from "@/types/adresse.interface";
import { useFormErrorScroll } from "@/composables/useFormErrorScroll";
import { useFormReset, resetConditions } from "@/composables/useFormReset";
import CybercrimeAchatNonRecuForm from "@/components/pre-plainte-component/event-info/cybercrime/CybercrimeAchatNonRecuForm.vue";
import CybercrimeCommandeFrauduleuseForm from "@/components/pre-plainte-component/event-info/cybercrime/CybercrimeCommandeFrauduleuseForm.vue";
import CybercrimeFausseAnnonceForm from "@/components/pre-plainte-component/event-info/cybercrime/CybercrimeFausseAnnonceForm.vue";
import VolForm from "@/components/pre-plainte-component/event-info/vol/VolForm.vue";
import DegatMaterielForm from "@/components/pre-plainte-component/event-info/degat/DegatMaterielForm.vue";
import AdresseEvent from "@/components/adresse/AdresseEvent.vue";
import PieceJointe from "@/components/piece-jointe/PieceJointe.vue";
import AccessibleVSelect from "@/components/accessibility/AccessibleVSelect.vue";
import { applyDateMask, applyTimeMask } from "@/utils/helpers/dateHelpers.ts";
import ExitActionsForm from "@/components/actions/ExitActionsForm.vue";
import { isCybercrimeTypeWithoutDetailFields } from "@/constants/constant";
import { requiredLabel } from "@/utils/helpers/labelHelpers";

const { t, locale } = useI18n();
const { mobile } = useDisplay();
const emit = defineEmits<{ cancel: []; continue: []; save: [] }>();
const store = useCreatePrePlainteStore();
const { scrollToTopOnConditionalErrors } = useFormErrorScroll();

const nationaliteLabel: string =
  store.userFormData.tiersNationalite?.label || store.userFormData.nationalite?.label || "";
const validationSchema = computed(() => {
  return toTypedSchema(createIncidentSchema(t, nationaliteLabel));
});

const form = useForm<PrePlainteFormFields>({
  initialValues: store.userFormData,
  validationSchema,
});

const { handleSubmit, setFieldValue, values } = form;

const { value: typeIncident, errorMessage: typeIncidentError } = useField<string>("typeIncident");

const openFromRecap = localStorage.getItem("pp-open-section");

switch (openFromRecap) {
  case "vol":
    typeIncident.value = "vol";
    break;
  case "degat":
    typeIncident.value = "degat-delit";
    break;
  case "cyber":
    typeIncident.value = "cybercrime";
    break;
  default:
    break;
}

localStorage.removeItem("pp-open-section");
const { value: dateDebutEvenement, errorMessage: dateDebutEvenementError } = useField<string>("dateDebutEvenement");
const { value: heureDebutEvenement, errorMessage: heureDebutEvenementError } = useField<string>("heureDebutEvenement");
const { value: dateFinEvenement, errorMessage: dateFinEvenementError } = useField<string>("dateFinEvenement");
const { value: heureFinEvenement, errorMessage: heureFinEvenementError } = useField<string>("heureFinEvenement");

const { value: datePremierContact, errorMessage: datePremierContactError } = useField("datePremierContact");
const { value: heurePremierContact, errorMessage: heurePremierContactError } = useField("heurePremierContact");
const { value: dateDernierContact, errorMessage: dateDernierContactError } = useField("dateDernierContact");
const { value: heureDernierContact, errorMessage: heureDernierContactError } = useField("heureDernierContact");

const selectedEventAddress = ref<AddressResult | null>(null);
const eventSearchText = ref("");

const { value: typeCybercrime, errorMessage: typeCybercrimeError } = useField<string>("typeCybercrime");
const { value: descriptionCybercrime, errorMessage: descriptionCybercrimeError } = useField("descriptionCybercrime");
const { value: fichiers } = useField<File[]>("fichiers");
const { value: justificatifsPaiement } = useField<File[]>("justificatifsPaiement");
const { value: copiesEcran } = useField<File[]>("copiesEcran");
const { value: autresDocuments } = useField<File[]>("autresDocuments");

const TYPE_CYBERCRIME_COMMANDE_FRAUDULEUSE = "commande-frauduleuse";
const TYPE_CYBERCRIME_ACHAT_NON_RECU = "achat-non-recu";
const TYPE_CYBERCRIME_FAUSSE_ANNONCE = "fausse-annonce";

const showCybercrimeUrlDescriptionAndPieces = computed(() => {
  const ty = typeCybercrime.value;
  return !!ty && ty !== TYPE_CYBERCRIME_ACHAT_NON_RECU && !isCybercrimeTypeWithoutDetailFields(ty);
});

const createInputHandler = (maskFn: (e: InputEvent, value: any) => void, target: any) => (e: InputEvent) => {
  maskFn(e, target);
};

const onDateDebutEvenementInput = createInputHandler(applyDateMask, dateDebutEvenement);
const onHeureDebutEvenementInput = createInputHandler(applyTimeMask, heureDebutEvenement);
const onDateFinEvenementInput = createInputHandler(applyDateMask, dateFinEvenement);
const onHeureFinEvenementInput = createInputHandler(applyTimeMask, heureFinEvenement);

const onDatePremierContactInput = createInputHandler(applyDateMask, datePremierContact);
const onHeurePremierContactInput = createInputHandler(applyTimeMask, heurePremierContact);
const onDateDernierContactInput = createInputHandler(applyDateMask, dateDernierContact);
const onHeureDernierContactInput = createInputHandler(applyTimeMask, heureDernierContact);

useFormReset(form, resetConditions.eventInfo, () => {
  selectedEventAddress.value = null;
  eventSearchText.value = "";
});

watch(typeCybercrime, cybercrimeType => {
  if (isCybercrimeTypeWithoutDetailFields(cybercrimeType)) {
    setFieldValue("descriptionCybercrime", "");
    setFieldValue("justificatifsPaiement", []);
    setFieldValue("copiesEcran", []);
    setFieldValue("autresDocuments", []);
  }

  if (cybercrimeType !== TYPE_CYBERCRIME_ACHAT_NON_RECU && cybercrimeType !== TYPE_CYBERCRIME_FAUSSE_ANNONCE) {
    setFieldValue("datePremierContact", "");
    setFieldValue("heurePremierContact", "");
    setFieldValue("dateDernierContact", "");
    setFieldValue("heureDernierContact", "");
  }

  if (cybercrimeType !== TYPE_CYBERCRIME_COMMANDE_FRAUDULEUSE) {
    setFieldValue("dateDebutEvenement", "");
    setFieldValue("heureDebutEvenement", "");
    setFieldValue("dateFinEvenement", "");
    setFieldValue("heureFinEvenement", "");
    setFieldValue("prestataire", "");
    setFieldValue("dateDecouverte", "");
    setFieldValue("montant", "");
    setFieldValue("assurance", null);
    setFieldValue("emailCommandeInconnu", false);
    setFieldValue("emailCommande", "");
    setFieldValue("telephoneCommandeInconnu", false);
    setFieldValue("telephoneCommande", "");
    setFieldValue("livraisonAdresseLesee", null);
    setFieldValue("livraisonAdresse", "");
    setFieldValue("livraisonAdressePostale", "");
    setFieldValue("livraisonNpa", "");
    setFieldValue("livraisonLocalite", "");
    setFieldValue("livraisonLocaliteCode", "");
    setFieldValue("livraisonPays", "");
  }

  if (cybercrimeType !== TYPE_CYBERCRIME_ACHAT_NON_RECU) {
    setFieldValue("montantDelitAchatLigne", "");
    setFieldValue("articleNonLivreDescription", "");
    setFieldValue("prenomVendeur", "");
    setFieldValue("nomVendeur", "");
    setFieldValue("telephoneVendeurInconnu", false);
    setFieldValue("telephoneVendeur", "");
    setFieldValue("emailVendeurInconnu", false);
    setFieldValue("emailVendeur", "");
    setFieldValue("adresseVendeurInconnue", false);
    setFieldValue("vendeurAdresse", "");
    setFieldValue("vendeurAdressePostale", "");
    setFieldValue("vendeurNpa", "");
    setFieldValue("vendeurLocalite", "");
    setFieldValue("vendeurLocaliteCode", "");
    setFieldValue("vendeurPays", "");
    setFieldValue("achatViaPlaceMarche", null);
    setFieldValue("plateforme", "");
    setFieldValue("plateformeAutre", "");
    setFieldValue("plateformeId", "");
    setFieldValue("nomEntrepriseVendeur", "");
    setFieldValue("siteWebEntrepriseVendeur", "");
    setFieldValue("annonceDocument", []);
    setFieldValue("annonceDocumentIndisponible", false);
    setFieldValue("raisonAbsenceAnnonce", "");
    setFieldValue("moyenPaiement", "");
    setFieldValue("moyenPaiementAutre", "");
    setFieldValue("ibanBeneficiaire", "");
    setFieldValue("comptePaypalBeneficiaire", "");
    setFieldValue("numeroTwintBeneficiaire", "");
    setFieldValue("adresseWalletCrypto", "");
    setFieldValue("hashTransactionCrypto", "");
    setFieldValue("societeBeneficiaire", "");
    setFieldValue("nomBeneficiaire", "");
    setFieldValue("prenomBeneficiaire", "");
    setFieldValue("dateOperation", "");
    setFieldValue("preuvePaiementDocument", []);
    setFieldValue("preuvePaiementIndisponible", false);
    setFieldValue("raisonAbsencePreuvePaiement", "");
    setFieldValue("copieIdentiteTransmiseAuteur", null);
    setFieldValue("copieIdentiteTransmiseAuteurDocument", []);
    setFieldValue("copieIdentiteAuteurTransmise", null);
    setFieldValue("copieIdentiteAuteurDocument", []);
  }

  if (cybercrimeType !== TYPE_CYBERCRIME_FAUSSE_ANNONCE) {
    setFieldValue("urlComplete", "");
    setFieldValue("titreAnnonce", "");
    setFieldValue("nomBailleur", "");
    setFieldValue("emailBailleurInconnu", false);
    setFieldValue("emailBailleur", "");
    setFieldValue("telephoneBailleurInconnu", false);
    setFieldValue("telephoneBailleur", "");
    setFieldValue("adresseBienImmobilier", "");
    setFieldValue("montantDemande", "");
    setFieldValue("modePaiementDemande", "");
  }
});

watch(typeIncident, incident => {
  if (incident !== "cybercrime") {
    setFieldValue("typeCybercrime", "");
    setFieldValue("descriptionCybercrime", "");
  }

  if (incident !== "vol") {
    setFieldValue("volDansVehicule", null);
    setFieldValue("typeObjet", null);
    setFieldValue("fabricant", null);
    setFieldValue("modele", null);
    setFieldValue("numeroSerie", "");
    setFieldValue("numeroIMEI", "");
    setFieldValue("avezVousDegradation", null);
  }

  if (incident !== "degat-delit") {
    setFieldValue("typeDommage", "");
    setFieldValue("montantEstime", "");
    setFieldValue("devise", "");
    setFieldValue("naturesDommage", []);
    setFieldValue("description", "");
    setFieldValue("dateConstat", "");
    setFieldValue("constatPresent", null);
  }
});

watch(locale, () => {
  form.validate();
});

/** Types proposés à la saisie (hors cyberharcèlement, rançongiciel, autre — non disponibles pour l’instant). */
const typeCybercrimeOptions = computed(() => [
  { label: t("cybercrime.commandeFrauduleuse"), value: TYPE_CYBERCRIME_COMMANDE_FRAUDULEUSE },
  { label: t("cybercrime.achatNonRecu"), value: TYPE_CYBERCRIME_ACHAT_NON_RECU },
  { label: t("cybercrime.fausseAnnonce"), value: TYPE_CYBERCRIME_FAUSSE_ANNONCE },
]);

watch(
  () => [typeIncident.value, typeCybercrime.value] as const,
  ([incident, ty]) => {
    if (incident === "cybercrime" && isCybercrimeTypeWithoutDetailFields(ty)) {
      setFieldValue("typeCybercrime", "");
    }
  },
  { immediate: true },
);

const onSubmit = handleSubmit(
  formValues => {
    store.setUserFormData(formValues);
    emit("continue");
  },
  errors => {
    scrollToTopOnConditionalErrors(errors);
  },
);

const persistCurrentValues = () => {
  store.setUserFormData(values as PrePlainteFormFields);
};

const handleCancelClick = () => {
  persistCurrentValues();
  emit("cancel");
};

const handleSaveClick = () => {
  persistCurrentValues();
  emit("save");
};
</script>
