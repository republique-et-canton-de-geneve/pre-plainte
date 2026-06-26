<template>
  <div class="inputs-fields">
    <BaseRadioGroup
      v-model="constatPresent"
      :label="t('dommages.constat')"
      required
      :options="[
        { label: t('common.oui'), value: true },
        { label: t('common.non'), value: false },
      ]"
      :error-messages="constatPresent === false ? undefined : constatPresentError"
    />
    <v-alert v-if="constatPresent === false" type="warning" variant="tonal" class="mb-8">
      {{ t("dommages.constatPoliceWarning") }}
    </v-alert>
    <template v-if="constatPresent">
      <v-text-field
        v-model="dateConstat"
        :label="requiredLabel(t('dommages.constatDate'))"
        type="text"
        placeholder="JJ.MM.AAAA"
        :error-messages="dateConstatError"
        class="mb-8"
        variant="outlined"
        prepend-inner-icon="mdi-calendar"
        :hint="t('dommages.hintDateConstat')"
        persistent-hint
        @input="onDateConstatInput"
      />
      <div class="mb-8">
        <PieceJointe v-model="fichiers" :label="t('dommages.constatPoliceFichiers')" required />
        <div v-if="fichiersError" class="text-error text-body-2 mt-2">
          {{ fichiersError }}
        </div>
      </div>
    </template>

    <template v-if="typeDommage === 'dommage-vehicule'">
      <template v-for="(obj, index) in objetsDegradesValides" :key="`obj-deg-${index}`">
        <div v-if="editingIndex === index" :ref="definirDraftPanelRef">
          <DegatVehiculeEndommageDraftPanel
            :objets-count="objetsDegradesValides?.length ?? 0"
            :objet-index="index"
            :sous-categorie="sousCategorie"
            :sous-categorie-error="sousCategorieError"
            :categorie-objet="categorieObjet"
            :sub-categorie-options="subCategorieOptions"
            :active-prefixes="activePrefixes"
            :valeur-reelle="valeurReelle"
            :valeur-reelle-error="valeurReelleError"
            :on-validate="validerVehiculeDommage"
            @update:sous-categorie="sousCategorie = $event"
            @update:valeurReelle="valeurReelle = $event"
          />
        </div>
        <DegatVehiculeEndommageResumeSheet
          v-else
          :obj="obj"
          :index="index"
          :libelle-resume-champ-absent="libelleResumeChampAbsent"
          :show-ajouter-autre-button="editingIndex === null && index === dernierIndexValide"
          @modifier="ouvrirDialogModifier"
          @supprimer="ouvrirDialogSupprimer"
          @ajouter-autre="scrollVersSaisie"
        />
      </template>

      <div v-if="afficherFicheSaisie && editingIndex === null" :ref="definirDraftPanelRef">
        <DegatVehiculeEndommageDraftPanel
          :objets-count="objetsDegradesValides?.length ?? 0"
          :sous-categorie="sousCategorie"
          :sous-categorie-error="sousCategorieError"
          :categorie-objet="categorieObjet"
          :sub-categorie-options="subCategorieOptions"
          :active-prefixes="activePrefixes"
          :valeur-reelle="valeurReelle"
          :valeur-reelle-error="valeurReelleError"
          :on-validate="validerVehiculeDommage"
          @update:sous-categorie="sousCategorie = $event"
          @update:valeurReelle="valeurReelle = $event"
        />
      </div>
    </template>

    <v-text-field
      :label="t('dommages.montantEstime')"
      v-model="montantEstime"
      type="number"
      :error-messages="montantEstimeError"
      class="mb-8"
      variant="outlined"
      :hint="t('dommages.hintMontantEstime')"
      persistent-hint
    />
    <AccessibleVSelect
      :label="t('incidentTypes.devise')"
      v-model="devise"
      :items="deviseOptions"
      :error-messages="deviseError"
      item-title="label"
      item-value="value"
      variant="outlined"
      class="mb-8"
      :hint="t('incidentTypes.hintDevise')"
      persistent-hint
    />
    <AccessibleVSelect
      :label="t('dommages.natureDommage')"
      required
      v-model="naturesDommage"
      :items="natureDommageOptions"
      :error-messages="naturesDommageError"
      item-title="label"
      item-value="value"
      class="mb-8"
      variant="outlined"
      multiple
      chips
      :hint="t('dommages.hintNatureDommage')"
      persistent-hint
      :placeholder="t('dommages.selectionnerNatureDommage')"
    />
    <v-textarea
      :label="requiredLabel(t('dommages.descriptionDommage'))"
      v-model="description"
      :error-messages="descriptionError"
      class="mb-8"
      variant="outlined"
      :hint="t('dommages.hintDescriptionDommage')"
      persistent-hint
    >
      <template #append-inner>
        <v-tooltip location="top">
          <template #activator="{ props }">
            <v-icon v-bind="props" color="primary" size="small"> mdi-information-outline </v-icon>
          </template>
          <div class="white-space">
            {{ t("dommages.descriptionDommageTooltip") }}
          </div>
        </v-tooltip>
      </template>
    </v-textarea>

    <v-dialog v-model="dialogConfirmationOuvert" max-width="440">
      <v-card v-if="dialogConfirmation">
        <v-card-title class="text-h6 py-4">{{ titreDialogConfirmation }}</v-card-title>
        <v-card-text class="text-body-1">{{ messageDialogConfirmation }}</v-card-text>
        <v-card-actions class="py-3">
          <v-spacer />
          <v-btn variant="text" @click="fermerDialogConfirmation">
            {{ t("common.annuler") }}
          </v-btn>
          <v-btn
            :color="dialogConfirmation.action === 'supprimer' ? 'error' : 'primary'"
            variant="flat"
            @click="confirmerDialog"
          >
            {{ libelleConfirmerDialog }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, ref, watch, onMounted, toRaw } from "vue";
import { useField, useFormContext } from "vee-validate";
import { useI18n } from "vue-i18n";
import {
  DEVISES,
  CATEGORIES_OBJETS,
  VOL_OBJET_CATEGORIE,
  EMPTY_VALUE_EM_DASH,
  TEXT_FIELD_MAX_LENGTH,
  VEHICULE_CATEGORIES_AVEC_VIN,
} from "@/constants/constant";
import AccessibleVSelect from "@/components/accessibility/AccessibleVSelect.vue";
import { applyDateMask } from "@/utils/helpers/dateHelpers.ts";
import type { PrePlainteFormFields, VolObjetFormSnapshot } from "@/types/pre-plainte.interface";
import type { RipolSelection } from "@/types/ripol.interface";
import BaseRadioGroup from "@/components/radio/BaseRadioGroup.vue";
import PieceJointe from "@/components/piece-jointe/PieceJointe.vue";
import DegatVehiculeEndommageDraftPanel from "./DegatVehiculeEndommageDraftPanel.vue";
import DegatVehiculeEndommageResumeSheet from "./DegatVehiculeEndommageResumeSheet.vue";
import { toTranslatedOptions } from "@/utils/helpers/traductionHelper";
import { requiredLabel } from "@/utils/helpers/labelHelpers";
import { checkLength, validerPlaqueVehicule } from "@/utils/helpers/volObjetVolHelpers";
import { RipolService } from "@/services/ripolService.ts";

const TEXTE_VIDE = "";
const CHAMP_REQUIS_ERREUR = "validation.champRequis";
const VALIDATION_LONGUEUR_MAX = "validation.longueurMax";

const chaineFormulaire = (v: unknown) => String(v ?? TEXTE_VIDE);
const texteOuVide = (v: string | undefined | null) => v ?? TEXTE_VIDE;

const cloneRipol = (sel: RipolSelection | null): RipolSelection | null => {
  if (!sel) {
    return null;
  }
  const r = toRaw(sel);
  return { code: r.code, label: r.label };
};

const { t } = useI18n();
const libelleResumeChampAbsent = computed(() => EMPTY_VALUE_EM_DASH);
const { setFieldValue, setFieldError } = useFormContext<PrePlainteFormFields>();

const { value: typeDommage } = useField<string>("typeDommage", undefined, {
  keepValueOnUnmount: true,
});
const { value: montantEstime, errorMessage: montantEstimeError } = useField("montantEstime");
const { value: devise, errorMessage: deviseError } = useField("devise");
const { value: naturesDommage, errorMessage: naturesDommageError } = useField<string[]>("naturesDommage", undefined, {
  keepValueOnUnmount: true,
});
const { value: description, errorMessage: descriptionError } = useField("description");
const { value: dateConstat, errorMessage: dateConstatError } = useField<string>("dateConstat");
const { value: constatPresent, errorMessage: constatPresentError } = useField("constatPresent");
const { value: fichiers, errorMessage: fichiersError } = useField<File[]>("fichiers");

const { value: sousCategorie, errorMessage: sousCategorieError } = useField<string>("sousCategorie");
const { value: categorieObjet } = useField<string>("categorieObjet");
const { value: typeObjet } = useField<RipolSelection | null>("typeObjet");
const { value: fabricant } = useField<RipolSelection | null>("fabricant");
const { value: fabricantAutre } = useField<string>("fabricantAutre");
const { value: modele } = useField<RipolSelection | null>("modele");
const { value: modeleAutre } = useField<string>("modeleAutre");
const { value: couleur } = useField<RipolSelection | null>("couleur");
const { value: couleurSecondaire } = useField<RipolSelection | null>("couleurSecondaire");
const { value: valeurReelle, errorMessage: valeurReelleError } = useField<string>("valeurReelle");
const { value: numeroCadre } = useField<string>("numeroCadre");
const { value: numeroCadreInconnu } = useField<boolean>("numeroCadreInconnu");
const { value: vin } = useField<string>("vin");
const { value: vinInconnu } = useField<boolean>("vinInconnu");
const { value: velofinderId } = useField<string>("velofinderId");
const { value: dateAchat } = useField<string>("dateAchat");
const { value: plaqueNumero } = useField<string>("plaqueNumero");
const { value: plaqueInconnu } = useField<boolean>("plaqueInconnu");
const { value: plaquePays } = useField<RipolSelection | null>("plaquePays");
const { value: plaqueCanton } = useField<RipolSelection | null>("plaqueCanton");
const { value: assuranceAucune } = useField<boolean>("assuranceAucune");
const { value: assureurAutre } = useField<string>("assureurAutre");
const { value: numeroAssurance } = useField<string>("numeroAssurance");
const { value: numeroVignette } = useField<string>("numeroVignette");
const { value: numeroMaster } = useField<string>("numeroMaster");
const { value: isVehicle } = useField<boolean>("isVehicle");
const { value: objetsDegradesValides } = useField<VolObjetFormSnapshot[]>("objetsDegradesValides");

const afficherFicheSaisie = ref((objetsDegradesValides.value?.length ?? 0) === 0);
const draftPanelRef = ref<HTMLElement | null>(null);
const definirDraftPanelRef = (element: unknown) => {
  draftPanelRef.value = element instanceof HTMLElement ? element : null;
};
const editingIndex = ref<number | null>(null);
const isRestoring = ref(false);

const isVeloCategory = computed(() => sousCategorie.value === "velos");
const hasVin = computed(() => VEHICULE_CATEGORIES_AVEC_VIN.includes(sousCategorie.value) ?? !isVeloCategory.value);

const deviseOptions = computed(() => toTranslatedOptions(DEVISES, t));
const natureDommageOptions = computed(() => [
  { label: t("dommages.degradations"), value: "degradations" },
  { label: t("dommages.tagsGraffiti"), value: "tags-graffiti" },
  { label: t("dommages.autre"), value: "autre" },
]);

const selectedCategorie = computed(() => CATEGORIES_OBJETS.find(cat => cat.value === categorieObjet.value));

const subCategorieOptions = computed(() => {
  const cat = selectedCategorie.value;
  if (!cat?.subCategories?.length) {
    return [];
  }
  return cat.subCategories.map(sub => ({
    value: sub.value,
    label: t(sub.labelKey),
    prefixes: sub.prefixes,
  }));
});

const selectedSubCategorie = computed(() => {
  const cat = selectedCategorie.value;
  if (!cat?.subCategories) {
    return null;
  }
  return cat.subCategories.find(sub => sub.value === sousCategorie.value);
});

const activePrefixes = computed(() => {
  if (selectedSubCategorie.value) {
    return selectedSubCategorie.value.prefixes;
  }
  return selectedCategorie.value?.prefixes || [];
});

const onDateConstatInput = (e: InputEvent) => {
  applyDateMask(e, dateConstat);
};

const remplirBrouillonDepuisSnapshot = (obj: VolObjetFormSnapshot) => {
  sousCategorie.value = texteOuVide(obj.sousCategorie);
  typeObjet.value = obj.typeObjet ? { ...obj.typeObjet } : null;
  fabricant.value = obj.fabricant ? { ...obj.fabricant } : null;
  fabricantAutre.value = texteOuVide(obj.fabricantAutre);
  modele.value = obj.modele ? { ...obj.modele } : null;
  modeleAutre.value = texteOuVide(obj.modeleAutre);
  couleur.value = obj.couleur ? { ...obj.couleur } : null;
  couleurSecondaire.value = obj.couleurSecondaire ? { ...obj.couleurSecondaire } : null;
  valeurReelle.value = texteOuVide(obj.valeurReelle);
  numeroCadre.value = texteOuVide(obj.numeroCadre);
  numeroCadreInconnu.value = !!obj.numeroCadreInconnu;
  vin.value = texteOuVide(obj.vin);
  vinInconnu.value = !!obj.vinInconnu;
  velofinderId.value = texteOuVide(obj.velofinderId);
  dateAchat.value = texteOuVide(obj.dateAchat);
  plaqueNumero.value = texteOuVide(obj.plaqueNumero);
  plaqueInconnu.value = !!obj.plaqueInconnu;
  plaquePays.value = obj.plaquePays ? { ...obj.plaquePays } : null;
  plaqueCanton.value = obj.plaqueCanton ? { ...obj.plaqueCanton } : null;
  assuranceAucune.value = !!obj.assuranceAucune;
  assureurAutre.value = texteOuVide(obj.assureurAutre) || texteOuVide(obj.assureur?.label);
  numeroAssurance.value = texteOuVide(obj.numeroAssurance);
  numeroVignette.value = texteOuVide(obj.numeroVignette);
  numeroMaster.value = texteOuVide(obj.numeroMaster);
};

const viderChampsVehiculeBrouillon = () => {
  sousCategorie.value = TEXTE_VIDE;
  typeObjet.value = null;
  fabricant.value = null;
  fabricantAutre.value = TEXTE_VIDE;
  modele.value = null;
  modeleAutre.value = TEXTE_VIDE;
  couleur.value = null;
  couleurSecondaire.value = null;
  valeurReelle.value = TEXTE_VIDE;
  numeroCadre.value = TEXTE_VIDE;
  numeroCadreInconnu.value = false;
  vin.value = TEXTE_VIDE;
  vinInconnu.value = false;
  velofinderId.value = TEXTE_VIDE;
  dateAchat.value = TEXTE_VIDE;
  plaqueNumero.value = TEXTE_VIDE;
  plaqueInconnu.value = false;
  plaquePays.value = null;
  plaqueCanton.value = null;
  assuranceAucune.value = false;
  assureurAutre.value = TEXTE_VIDE;
  numeroAssurance.value = TEXTE_VIDE;
  numeroVignette.value = TEXTE_VIDE;
  numeroMaster.value = TEXTE_VIDE;
};

const CHAMPS_ERREUR_BROUILLON = [
  "typeObjet",
  "sousCategorie",
  "fabricant",
  "fabricantAutre",
  "modele",
  "modeleAutre",
  "plaqueNumero",
  "plaquePays",
  "plaqueCanton",
] as const;

const stopRestoringOnNextTick = () => {
  void nextTick(() => {
    isRestoring.value = false;
  });
};

const effacerErreursBrouillon = () => {
  for (const name of CHAMPS_ERREUR_BROUILLON) {
    setFieldError(name, undefined);
  }
};

const buildSnapshotFromDraft = (): VolObjetFormSnapshot => ({
  categorieObjet: VOL_OBJET_CATEGORIE.VEHICULE,
  sousCategorie: sousCategorie.value,
  typeObjet: cloneRipol(typeObjet.value),
  fabricant: cloneRipol(fabricant.value),
  fabricantAutre: chaineFormulaire(fabricantAutre.value),
  modele: cloneRipol(modele.value),
  modeleAutre: chaineFormulaire(modeleAutre.value),
  couleur: cloneRipol(couleur.value),
  couleurSecondaire: cloneRipol(couleurSecondaire.value),
  gravure: "",
  valeurReelle: chaineFormulaire(valeurReelle.value),
  numeroSerie: "",
  numeroSerieInconnu: false,
  numeroIMEI: "",
  numeroIMEIInconnu: false,
  justificationAbsenceIMEI: "",
  isVehicle: true,
  numeroCadre: chaineFormulaire(numeroCadre.value),
  numeroCadreInconnu: numeroCadreInconnu.value,
  vin: chaineFormulaire(vin.value),
  vinInconnu: vinInconnu.value,
  velofinderId: chaineFormulaire(velofinderId.value),
  dateAchat: chaineFormulaire(dateAchat.value),
  plaqueNumero: chaineFormulaire(plaqueNumero.value),
  plaqueInconnu: plaqueInconnu.value,
  plaquePays: cloneRipol(plaquePays.value),
  plaqueCanton: cloneRipol(plaqueCanton.value),
  assuranceAucune: assuranceAucune.value,
  assureur: null,
  assureurAutre: chaineFormulaire(assureurAutre.value),
  numeroAssurance: chaineFormulaire(numeroAssurance.value),
  numeroVignette: chaineFormulaire(numeroVignette.value),
  numeroMaster: chaineFormulaire(numeroMaster.value),
});

const mettreListe = (liste: VolObjetFormSnapshot[]) => {
  setFieldValue("objetsDegradesValides", liste);
};

const clearDraftApresValidation = () => {
  isRestoring.value = true;
  viderChampsVehiculeBrouillon();
  stopRestoringOnNextTick();
};

const validerVehiculeDommage = async (): Promise<boolean> => {
  effacerErreursBrouillon();

  if (!await validateChampsObligatoires()) {
    return false;
  }

  if (!validateLongueurs()) {
    return false;
  }

  mettreAJourListe();
  resetFormulaire();

  return true;
};

const validateChampsObligatoires = async (): Promise<boolean> => {
  if (!sousCategorie.value?.trim()) {
    setFieldError("sousCategorie", t(CHAMP_REQUIS_ERREUR));
    return false;
  }

  if (!typeObjet.value?.code) {
    setFieldError("typeObjet", t("validation.typeObjetRequis"));
    return false;
  }

  if (!await validateFabricantEtModele()) {
    return false;
  }

  if (!couleur.value?.code) {
    setFieldError("couleur", t("validation.couleurRequise"));
    return false;
  }

  if (!validateNumeroCadreEtVin()) {
    return false;
  }

  return validerPlaqueVehicule(
    {
      sousCategorie: sousCategorie.value,
      plaqueInconnu: plaqueInconnu.value,
      plaqueNumero: plaqueNumero.value,
      plaquePays: plaquePays.value,
      plaqueCanton: plaqueCanton.value,
    },
    (field, message) => setFieldError(field as any, message),
    t,
  );
};

const validateFabricantEtModele = async (): Promise<boolean> => {
  if (!fabricant.value?.code) {
    setFieldError("fabricant", t("validation.fabricantRequis"));
    return false;
  }

  if (
    fabricant.value.code === "AUTRE"
    && !chaineFormulaire(fabricantAutre.value).trim()
  ) {
    setFieldError("fabricantAutre", t(CHAMP_REQUIS_ERREUR));
    return false;
  }

  if (fabricant.value.code === "AUTRE") {
    return true;
  }

  const models = await RipolService.searchVehicleModels(fabricant.value.code);

  if (models.length > 0 && !modele.value?.code) {
    setFieldError("modele", t("validation.modeleRequis"));
    return false;
  }

  if (
    (modele.value?.code === "AUTRE" || models.length === 0)
    && !chaineFormulaire(modeleAutre.value).trim()
  ) {
    setFieldError("modeleAutre", t(CHAMP_REQUIS_ERREUR));
    return false;
  }

  return true;
};

const validateNumeroCadreEtVin = (): boolean => {
  if (
    isVeloCategory.value
    && !numeroCadreInconnu.value
    && !chaineFormulaire(numeroCadre.value).trim()
  ) {
    setFieldError("numeroCadre", t("validation.numeroCadreRequis"));
    return false;
  }

  if (
    hasVin.value
    && !vinInconnu.value
    && !chaineFormulaire(vin.value).trim()
  ) {
    setFieldError("vin", t("validation.vinRequis"));
    return false;
  }

  return true;
};

const validateLongueurs = (): boolean => {
  if (!checkLength(fabricantAutre.value, TEXT_FIELD_MAX_LENGTH)) {
    setFieldError("fabricantAutre", t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH }));
    return false;
  }

  if (!checkLength(modeleAutre.value, TEXT_FIELD_MAX_LENGTH)) {
    setFieldError("modeleAutre", t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH }));
    return false;
  }

  if (!checkLength(vin.value, TEXT_FIELD_MAX_LENGTH)) {
    setFieldError("vin", t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH }));
    return false;
  }

  if (!checkLength(numeroCadre.value, TEXT_FIELD_MAX_LENGTH)) {
    setFieldError("numeroCadre", t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH }));
    return false;
  }

  if (!checkLength(velofinderId.value, TEXT_FIELD_MAX_LENGTH)) {
    setFieldError("velofinderId", t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH }));
    return false;
  }

  if (!checkLength(assureurAutre.value, TEXT_FIELD_MAX_LENGTH)) {
    setFieldError("assureurAutre", t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH }));
    return false;
  }

  if (!checkLength(numeroAssurance.value, TEXT_FIELD_MAX_LENGTH)) {
    setFieldError("numeroAssurance", t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH }));
    return false;
  }

  if (!checkLength(numeroVignette.value, TEXT_FIELD_MAX_LENGTH)) {
    setFieldError("numeroVignette", t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH }));
    return false;
  }

  if (!checkLength(numeroMaster.value, TEXT_FIELD_MAX_LENGTH)) {
    setFieldError("numeroMaster", t(VALIDATION_LONGUEUR_MAX, { max: TEXT_FIELD_MAX_LENGTH }));
    return false;
  }

  return true;
};

const mettreAJourListe = () => {
  const snapshot = buildSnapshotFromDraft();
  const next = [...(objetsDegradesValides.value ?? [])];

  const idx = editingIndex.value;
  const peutRemplacer =
    idx !== null
    && idx >= 0
    && idx < next.length;

  if (peutRemplacer) {
    next[idx] = snapshot;
  }
  else {
    next.push(snapshot);
  }

  mettreListe(next);
};

const resetFormulaire = () => {
  editingIndex.value = null;
  clearDraftApresValidation();
  afficherFicheSaisie.value = false;
};

const validerBrouillonAvantNavigation = async (): Promise<boolean> => {
  if (typeDommage.value !== "dommage-vehicule" || !afficherFicheSaisie.value) {
    return true;
  }
  const brouillonValide = await validerVehiculeDommage();
  if (!brouillonValide) {
    void nextTick(() => draftPanelRef.value?.scrollIntoView({ behavior: "smooth", block: "start" }));
  }
  return brouillonValide;
};

defineExpose({ validerBrouillonAvantNavigation });
const dernierIndexValide = computed(() => {
  const n = objetsDegradesValides.value?.length ?? 0;
  return n > 0 ? n - 1 : -1;
});

watch(
  () => objetsDegradesValides.value?.length ?? 0,
  n => {
    if (n === 0) {
      afficherFicheSaisie.value = true;
    }
  },
);

watch(
  typeDommage,
  value => {
    if (Array.isArray(value)) {
      typeDommage.value = value[0] ?? "";
      return;
    }
    if (value === "dommage-vehicule") {
      categorieObjet.value = VOL_OBJET_CATEGORIE.VEHICULE;
      isVehicle.value = true;
      const vehCat = CATEGORIES_OBJETS.find(c => c.value === VOL_OBJET_CATEGORIE.VEHICULE);
      const sousValides = new Set<string>(vehCat?.subCategories?.map(s => s.value) ?? []);
      if (sousCategorie.value && !sousValides.has(sousCategorie.value)) {
        sousCategorie.value = "";
        viderChampsVehiculeBrouillon();
      }
      return;
    }

    mettreListe([]);
    editingIndex.value = null;
    afficherFicheSaisie.value = true;
    if (categorieObjet.value === VOL_OBJET_CATEGORIE.VEHICULE) {
      categorieObjet.value = "";
      sousCategorie.value = "";
      viderChampsVehiculeBrouillon();
    }
    isVehicle.value = false;
  },
  { immediate: true },
);

watch(modele, newVal => {
  if (isRestoring.value || !newVal) {
    return;
  }

  if (modeleAutre.value) {
    modeleAutre.value = "";
  }
});

watch(modeleAutre, newVal => {
  if (isRestoring.value || !newVal) {
    return;
  }

  if (modele.value?.code !== "AUTRE") {
    modele.value = null;
  }
});

type ActionDialog = "modifier" | "supprimer";

const dialogConfirmation = ref<{ action: ActionDialog; index: number } | null>(null);

const dialogConfirmationOuvert = computed({
  get: () => dialogConfirmation.value !== null,
  set: (ouvert: boolean) => {
    if (!ouvert) {
      dialogConfirmation.value = null;
    }
  },
});

const titreDialogConfirmation = computed(() => {
  const d = dialogConfirmation.value;
  if (!d) {
    return "";
  }
  return d.action === "modifier"
    ? t("dommages.confirmationModifierVehiculeEndommage.titre")
    : t("dommages.confirmationSupprimerVehiculeEndommage.titre");
});

const messageDialogConfirmation = computed(() => {
  const d = dialogConfirmation.value;
  if (!d) {
    return "";
  }
  return d.action === "modifier"
    ? t("dommages.confirmationModifierVehiculeEndommage.message")
    : t("dommages.confirmationSupprimerVehiculeEndommage.message");
});

const libelleConfirmerDialog = computed(() => {
  const d = dialogConfirmation.value;
  if (!d) {
    return "";
  }
  return d.action === "modifier"
    ? t("dommages.confirmationModifierVehiculeEndommage.confirmer")
    : t("dommages.confirmationSupprimerVehiculeEndommage.confirmer");
});

const ouvrirDialogModifier = (index: number) => {
  dialogConfirmation.value = { action: "modifier", index };
};

const ouvrirDialogSupprimer = (index: number) => {
  dialogConfirmation.value = { action: "supprimer", index };
};

const fermerDialogConfirmation = () => {
  dialogConfirmation.value = null;
};

const confirmerDialog = () => {
  const d = dialogConfirmation.value;
  if (!d) {
    return;
  }
  dialogConfirmation.value = null;
  if (d.action === "modifier") {
    modifierEntree(d.index);
  } else {
    supprimerEntree(d.index);
  }
};

const modifierEntree = (index: number) => {
  const obj = objetsDegradesValides.value?.[index];
  if (!obj) {
    return;
  }
  afficherFicheSaisie.value = true;
  isRestoring.value = true;
  editingIndex.value = index;
  remplirBrouillonDepuisSnapshot(obj);
  stopRestoringOnNextTick();
};

const supprimerEntree = (index: number) => {
  const next = [...(objetsDegradesValides.value ?? [])];
  next.splice(index, 1);
  mettreListe(next);
  if (editingIndex.value === index) {
    editingIndex.value = null;
    clearDraftApresValidation();
  } else if (editingIndex.value !== null && editingIndex.value > index) {
    editingIndex.value = editingIndex.value - 1;
  }
};

const scrollVersSaisie = async () => {
  afficherFicheSaisie.value = true;
  await nextTick();
  draftPanelRef.value?.scrollIntoView({ behavior: "smooth", block: "start" });
};

onMounted(() => {
  if ((objetsDegradesValides.value?.length ?? 0) > 0) {
    afficherFicheSaisie.value = false;
  }
});
</script>

<style scoped>
.white-space {
  max-width: 300px;
  white-space: normal;
}
fieldset {
  border: 0;
  padding: 0;
  margin: 0;
}

legend {
  padding: 0;
}

:deep(.objet-degrade-resume),
:deep(.objet-degrade-brouillon) {
  background-color: var(--md-sys-color-surface-container-lowest, #f7fafc);
}
</style>
