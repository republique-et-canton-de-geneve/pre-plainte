<template>
  <v-form @submit.prevent="onSubmit">
    <h1 class="mb-4 text-h1 text-md-h2 d-none d-md-block">{{ t("titreApplication.prePlainte") }}</h1>
    <v-card class="pa-2 pa-md-6 mb-4">
      <FormErrorSummary
        :items="formErrorItemsAffiches"
        :summary-message="objetVoleIncomplet ? t('incidentTypes.objetVoleInformationsManquantes') : undefined"
      />
      <h2 class="pre-plainte-main-card-title mb-4 text-h4 text-md-h2">{{ t("informationsEvenement.titre") }}</h2>
      <VolForm v-if="typeIncident === 'vol'" ref="volFormRef" />
      <DegatMaterielForm v-if="typeIncident === 'degat-delit'" ref="degatMaterielFormRef" />

      <div v-if="typeIncident === 'cybercrime'" class="inputs-fields">
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
        <div v-if="showAutresDocumentsCybercrime" class="mb-8">
          <PieceJointe v-model="autresDocuments" :label="t('cybercrime.autresDocuments')" />
        </div>
      </div>

      <template v-if="typeIncident === 'vol' || typeIncident === 'degat-delit' || typeCybercrime === TYPE_CYBERCRIME_COMMANDE_FRAUDULEUSE">
        <v-row class="mb-4" align="center" dense>
          <v-col cols="12" md="6">
            <MaskedDateField
              v-model="dateDebutEvenement"
              :label="requiredLabel(dateDebutEvenementLabel)"
              :error-messages="dateDebutEvenementError"
              :hint="dateDebutEvenementHint"
              persistent-hint
              name="dateDebutEvenement"
            />
          </v-col>
          <v-col cols="12" md="6" class="mt-4 mt-md-0">
            <MaskedTimeField
              v-model="heureDebutEvenement"
              :label="requiredLabel(heureDebutEvenementLabel)"
              :error-messages="heureDebutEvenementError"
              :hint="heureDebutEvenementHint"
              persistent-hint
              name="heureDebutEvenement"
            />
          </v-col>
        </v-row>

        <v-row class="mb-4" align="center" dense>
          <v-col cols="12" md="6">
            <MaskedDateField
              v-model="dateFinEvenement"
              :label="requiredLabel(dateFinEvenementLabel)"
              :error-messages="dateFinEvenementError"
              :hint="dateFinEvenementHint"
              persistent-hint
              name="dateFinEvenement"
            />
          </v-col>
          <v-col cols="12" md="6" class="mt-4 mt-md-0">
            <MaskedTimeField
              v-model="heureFinEvenement"
              :label="requiredLabel(heureFinEvenementLabel)"
              :error-messages="heureFinEvenementError"
              :hint="heureFinEvenementHint"
              persistent-hint
              name="heureFinEvenement"
            />
          </v-col>
        </v-row>
      </template>

      <template v-if="typeCybercrime === TYPE_CYBERCRIME_ACHAT_NON_RECU || typeCybercrime === TYPE_CYBERCRIME_FAUSSE_ANNONCE">
        <v-row class="mb-4 mt-4" align="center" dense>
          <v-col cols="12" md="6">
            <MaskedDateField
              v-model="datePremierContact"
              :label="requiredLabel(t('cybercrime.datePremierContact'))"
              :error-messages="datePremierContactError"
              :hint="t('cybercrime.hintDatePremierContact')"
              persistent-hint
              name="datePremierContact"
            />
          </v-col>
          <v-col cols="12" md="6" class="mt-4 mt-md-0">
            <MaskedTimeField
              v-model="heurePremierContact"
              :label="requiredLabel(t('cybercrime.heurePremierContact'))"
              :error-messages="heurePremierContactError"
              :hint="t('cybercrime.hintHeurePremierContact')"
              persistent-hint
              name="heurePremierContact"
            />
          </v-col>
        </v-row>
        <v-row class="mb-4" align="center" dense>
          <v-col cols="12" md="6">
            <MaskedDateField
              v-model="dateDernierContact"
              :label="requiredLabel(t('cybercrime.dateDernierContact'))"
              :error-messages="dateDernierContactError"
              :hint="t('cybercrime.hintDateDernierContact')"
              persistent-hint
              name="dateDernierContact"
            />
          </v-col>
          <v-col cols="12" md="6" class="mt-4 mt-md-0">
            <MaskedTimeField
              v-model="heureDernierContact"
              :label="requiredLabel(t('cybercrime.heureDernierContact'))"
              :error-messages="heureDernierContactError"
              :hint="t('cybercrime.hintHeureDernierContact')"
              persistent-hint
              name="heureDernierContact"
            />
          </v-col>
        </v-row>
      </template>

      <div v-if="showEventFilesUpload" class="mb-8">
        <PieceJointe
          v-model="fichiers"
          :label="eventFilesUploadLabel"
          :subtitle="typeIncident === 'degat-delit' ? t('dommages.photosRecommandees') : ''"
        />
      </div>
      <AdresseEvent v-if="typeIncident !== 'cybercrime'" />

      <div class="d-md-none mt-4">
        <div class="pre-plainte-mobile-step-actions pre-plainte-mobile-step-actions--sticky d-flex flex-column gap-2">
          <v-btn variant="outlined" color="primary" class="w-100" @click="handleCancelClick">
            {{ t("common.precedent") }}
          </v-btn>
          <v-btn type="submit" variant="flat" color="primary" class="w-100" :loading="isSubmitting">
            {{ t("common.continuer") }}
          </v-btn>
          <v-btn variant="plain" color="primary" class="w-100" @click="handleSaveClick">
            {{ t("common.sauvegarder") }}
          </v-btn>
        </div>
      </div>
    </v-card>
    <div class="d-md-none mt-4 d-flex flex-column align-center gap-2">
      <ExitActionsForm :is-mobile="true" />
    </div>

    <v-row class="mt-4 d-none d-md-flex" align="center">
      <v-col cols="12" md="auto" class="d-flex flex-column">
        <v-btn variant="plain" color="primary" @click="handleSaveClick">
          {{ t("common.sauvegarder") }}
        </v-btn>
      </v-col>
      <v-spacer />
      <v-col cols="12" md="auto" class="d-flex justify-end">
        <v-btn variant="outlined" color="primary" class="me-4" @click="handleCancelClick">
          {{ t("common.precedent") }}
        </v-btn>
          <v-btn type="submit" variant="flat" color="primary" data-cy="continuer-evenement" :loading="isSubmitting">
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
import { computed, nextTick, onBeforeUnmount, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { createIncidentSchema } from "@/schemas/incident-evenement.schema.ts";
import type { AddressResult } from "@/types/adresse.interface";
import { useFormErrorScroll } from "@/composables/useFormErrorScroll";
import { useFormReset, resetConditions } from "@/composables/useFormReset";
import { collectValidationErrorItems, excludeVolObjetBrouillonErrors, type FormValidationErrorItem } from "@/utils/helpers/formErrorHelpers";
import FormErrorSummary from "@/components/form/FormErrorSummary.vue";
import MaskedDateField from "@/components/form/MaskedDateField.vue";
import MaskedTimeField from "@/components/form/MaskedTimeField.vue";
import CybercrimeAchatNonRecuForm from "@/components/pre-plainte-component/event-info/cybercrime/CybercrimeAchatNonRecuForm.vue";
import CybercrimeCommandeFrauduleuseForm from "@/components/pre-plainte-component/event-info/cybercrime/CybercrimeCommandeFrauduleuseForm.vue";
import CybercrimeFausseAnnonceForm from "@/components/pre-plainte-component/event-info/cybercrime/CybercrimeFausseAnnonceForm.vue";
import VolForm from "@/components/pre-plainte-component/event-info/vol/VolForm.vue";
import DegatMaterielForm from "@/components/pre-plainte-component/event-info/degat/DegatMaterielForm.vue";
import AdresseEvent from "@/components/adresse/AdresseEvent.vue";
import PieceJointe from "@/components/piece-jointe/PieceJointe.vue";
import ExitActionsForm from "@/components/actions/ExitActionsForm.vue";
import { isCybercrimeTypeWithoutDetailFields } from "@/constants/constant";
import { TYPE_INCIDENT } from "@/utils/incident-fields";
import { requiredLabel } from "@/utils/helpers/labelHelpers";
import { requiresConstatQuestion } from "@/utils/workflows/disclaimer-workflow";

const { t, locale } = useI18n();
const emit = defineEmits<{ cancel: []; continue: []; save: [] }>();
const store = useCreatePrePlainteStore();
const { scrollToFormErrorSummary } = useFormErrorScroll();
const submitErrorItems = ref<FormValidationErrorItem[]>([]);
const objetVoleIncomplet = ref(false);
const isSubmitting = ref(false);

const formErrorItemsAffiches = computed(() => {
  if (!objetVoleIncomplet.value) {
    return submitErrorItems.value;
  }
  return excludeVolObjetBrouillonErrors(submitErrorItems.value);
});

const DEGAT_DELIT = "degat-delit";

type FormulaireAvecBrouillon = {
  validerBrouillonAvantNavigation: () => boolean | Promise<boolean>;
};

const volFormRef = ref<FormulaireAvecBrouillon | null>(null);
const degatMaterielFormRef = ref<FormulaireAvecBrouillon | null>(null);

const nationalitePersonneLesee =
  store.userFormData.tiersNationalite?.code || store.userFormData.nationalite?.code || "";

const validationSchema = computed(() => toTypedSchema(createIncidentSchema(t, nationalitePersonneLesee)));

const form = useForm<PrePlainteFormFields>({
  initialValues: store.userFormData,
  validationSchema,
});

const { setFieldValue, values, errors, validate } = form;

const { value: typeIncident } = useField<string>("typeIncident");
const { value: typeDommage } = useField<string>("typeDommage");

const openFromRecap = localStorage.getItem("pp-open-section");
switch (openFromRecap) {
  case "vol":
    typeIncident.value = "vol";
    break;
  case "degat":
    typeIncident.value = DEGAT_DELIT;
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

const { value: typeCybercrime } = useField<string>("typeCybercrime");
const { value: descriptionCybercrime, errorMessage: descriptionCybercrimeError } = useField("descriptionCybercrime");
const { value: fichiers } = useField<File[]>("fichiers");
const { value: justificatifsPaiement } = useField<File[]>("justificatifsPaiement");
const { value: copiesEcran } = useField<File[]>("copiesEcran");
const { value: autresDocuments } = useField<File[]>("autresDocuments");
const { value: copieIdentiteAuteurTransmise } = useField<boolean | null>("copieIdentiteAuteurTransmise");

const TYPE_CYBERCRIME_COMMANDE_FRAUDULEUSE = "commande-frauduleuse";
const TYPE_CYBERCRIME_ACHAT_NON_RECU = "achat-non-recu";
const TYPE_CYBERCRIME_FAUSSE_ANNONCE = "fausse-annonce";

const isCommandeFrauduleuse = computed(() => typeCybercrime.value === TYPE_CYBERCRIME_COMMANDE_FRAUDULEUSE);
const dateDebutEvenementLabel = computed(() =>
  t(isCommandeFrauduleuse.value ? "informationsEvenement.dateCommande" : "informationsEvenement.dateDebutEvenement"),
);
const heureDebutEvenementLabel = computed(() =>
  t(isCommandeFrauduleuse.value ? "informationsEvenement.heureCommande" : "informationsEvenement.heureDebutEvenement"),
);
const dateFinEvenementLabel = computed(() =>
  t(
    isCommandeFrauduleuse.value
      ? "informationsEvenement.dateLivraisonOuDecouverte"
      : "informationsEvenement.dateFinEvenement",
  ),
);
const heureFinEvenementLabel = computed(() =>
  t(
    isCommandeFrauduleuse.value
      ? "informationsEvenement.heureLivraisonOuDecouverte"
      : "informationsEvenement.heureFinEvenement",
  ),
);
const dateDebutEvenementHint = computed(() =>
  t(
    isCommandeFrauduleuse.value
      ? "informationsEvenement.hintDateCommande"
      : "informationsEvenement.hintDateDebutEvenement",
  ),
);
const heureDebutEvenementHint = computed(() =>
  t(
    isCommandeFrauduleuse.value
      ? "informationsEvenement.hintHeureCommande"
      : "informationsEvenement.hintHeureDebutEvenement",
  ),
);
const dateFinEvenementHint = computed(() =>
  t(
    isCommandeFrauduleuse.value
      ? "informationsEvenement.hintDateLivraisonOuDecouverte"
      : "informationsEvenement.hintDateFinEvenement",
  ),
);
const heureFinEvenementHint = computed(() =>
  t(
    isCommandeFrauduleuse.value
      ? "informationsEvenement.hintHeureLivraisonOuDecouverte"
      : "informationsEvenement.hintHeureFinEvenement",
  ),
);

const showCybercrimeUrlDescriptionAndPieces = computed(() => {
  const ty = typeCybercrime.value;
  return !!ty && ty !== TYPE_CYBERCRIME_ACHAT_NON_RECU && !isCybercrimeTypeWithoutDetailFields(ty);
});

const showAutresDocumentsCybercrime = computed(() => {
  if (typeIncident.value !== TYPE_INCIDENT.CYBERCRIME) {
    return false;
  }
  const ty = typeCybercrime.value;
  if (!ty || isCybercrimeTypeWithoutDetailFields(ty)) {
    return false;
  }
  if (ty === TYPE_CYBERCRIME_COMMANDE_FRAUDULEUSE || ty === TYPE_CYBERCRIME_ACHAT_NON_RECU) {
    return copieIdentiteAuteurTransmise.value === true;
  }
  return true;
});

const showEventFilesUpload = computed(() => {
  if (typeIncident.value === TYPE_INCIDENT.CYBERCRIME) {
    return false;
  }
  return typeIncident.value !== DEGAT_DELIT || !requiresConstatQuestion(typeDommage.value);
});

const eventFilesUploadLabel = computed(() =>
  typeIncident.value === TYPE_INCIDENT.VOL
    ? t("incidentTypes.fichiersObjetsVoles")
    : t("dommages.fichiers"),
);

useFormReset(form, resetConditions.eventInfo, () => {
  selectedEventAddress.value = null;
  eventSearchText.value = "";
});

watch(copieIdentiteAuteurTransmise, received => {
  if (received !== true) {
    autresDocuments.value = [];
  }
});

watch(
  typeCybercrime,
  cybercrimeType => {
    if (typeIncident.value !== TYPE_INCIDENT.CYBERCRIME) {
      return;
    }

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
      setFieldValue("prenomContrevenant", "");
      setFieldValue("nomContrevenant", "");
      setFieldValue("siteWebContrevenant", "");
      setFieldValue("contrevenantAdresse", "");
      setFieldValue("contrevenantAdressePostale", "");
      setFieldValue("contrevenantNpa", "");
      setFieldValue("contrevenantLocalite", "");
      setFieldValue("contrevenantLocaliteCode", "");
      setFieldValue("contrevenantPays", "");
      setFieldValue("moyenPaiementNumeriqueDebite", null);
    }

    if (cybercrimeType !== TYPE_CYBERCRIME_ACHAT_NON_RECU && cybercrimeType !== TYPE_CYBERCRIME_COMMANDE_FRAUDULEUSE) {
      setFieldValue("copieIdentiteTransmiseAuteur", null);
      setFieldValue("copieIdentiteTransmiseAuteurDocument", []);
      setFieldValue("copieIdentiteTransmiseAuteurDocumentIndisponible", false);
      setFieldValue("raisonAbsenceCopieIdentiteTransmiseAuteur", "");
      setFieldValue("copieIdentiteAuteurTransmise", null);
      setFieldValue("copieIdentiteAuteurDocument", []);
      setFieldValue("copieIdentiteAuteurDocumentIndisponible", false);
      setFieldValue("raisonAbsenceCopieIdentiteAuteur", "");
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
      setFieldValue("numeroTransactionPaypal", "");
      setFieldValue("numeroTwintBeneficiaire", "");
      setFieldValue("typeCryptoMonnaie", "");
      setFieldValue("montantUnitesCrypto", "");
      setFieldValue("adresseWalletExpediteur", "");
      setFieldValue("adresseWalletCrypto", "");
      setFieldValue("hashTransactionCrypto", "");
      setFieldValue("societeBeneficiaire", "");
      setFieldValue("nomBeneficiaire", "");
      setFieldValue("prenomBeneficiaire", "");
      setFieldValue("dateOperation", "");
      setFieldValue("preuvePaiementDocument", []);
      setFieldValue("preuvePaiementIndisponible", false);
      setFieldValue("raisonAbsencePreuvePaiement", "");
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
  },
  { immediate: true },
);

watch(
  typeIncident,
  incident => {
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

    if (incident !== DEGAT_DELIT) {
      setFieldValue("typeDommage", "");
      setFieldValue("montantEstime", "");
      setFieldValue("devise", "");
      setFieldValue("naturesDommage", []);
      setFieldValue("description", "");
      setFieldValue("dateConstat", "");
      setFieldValue("constatPresent", null);
    }
  },
  { immediate: true },
);

watch(locale, () => {
  form.validate();
});

watch(
  () => [typeIncident.value, typeCybercrime.value] as const,
  ([incident, ty]) => {
    if (incident === "cybercrime" && isCybercrimeTypeWithoutDetailFields(ty)) {
      setFieldValue("typeCybercrime", "");
    }
  },
  { immediate: true },
);

const afficherErreursEtRemonter = async (source: unknown = errors.value) => {
  submitErrorItems.value = collectValidationErrorItems(source);
  await scrollToFormErrorSummary();
};

const validerBrouillonActif = async (): Promise<boolean> => {
  if (typeIncident.value === "vol") {
    return (await volFormRef.value?.validerBrouillonAvantNavigation()) ?? true;
  }
  if (typeIncident.value === DEGAT_DELIT) {
    return (await degatMaterielFormRef.value?.validerBrouillonAvantNavigation()) ?? true;
  }
  return true;
};

const onSubmit = async () => {
  const brouillonOk = await validerBrouillonActif();
  await nextTick();

  const { valid } = await validate();
  await nextTick();

  if (!brouillonOk || !valid) {
    objetVoleIncomplet.value = typeIncident.value === "vol" && !brouillonOk;
    await afficherErreursEtRemonter(errors.value);
    return;
  }

  objetVoleIncomplet.value = false;
  submitErrorItems.value = [];
  isSubmitting.value = true;
  try {
    store.setUserFormData(values as PrePlainteFormFields);
    emit("continue");
  } finally {
    isSubmitting.value = false;
  }
};

const persistCurrentValues = () => {
  store.setUserFormData(values as PrePlainteFormFields);
};

onBeforeUnmount(() => {
  persistCurrentValues();
});

const handleCancelClick = () => {
  persistCurrentValues();
  emit("cancel");
};

const handleSaveClick = () => {
  persistCurrentValues();
  emit("save");
};
</script>
