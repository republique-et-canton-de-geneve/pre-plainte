<template>
  <div class="inputs-fields">
    <BaseRadioGroup
      v-model="volDansVehicule"
      :label="t('incidentTypes.volDansVehicule')"
      required
      :options="[
        { label: t('common.oui'), value: true },
        { label: t('common.non'), value: false }
      ]"
      :error-messages="volDansVehiculeError"
    />
    <v-alert
      v-if="volDansVehicule === true"
      type="warning"
      variant="tonal"
      density="comfortable"
      class="mb-4"
      :icon="mobile ? false : undefined"
    >
      {{ t("incidentTypes.volDansVehiculeWarning") }}
    </v-alert>
    <template v-for="(obj, index) in brouillon.objetsVolesValides" :key="`obj-vol-${index}`">
      <div v-if="editingIndex === index" :ref="definirDraftPanelRef">
        <VolObjetVoleDraftPanel
          :brouillon="brouillonPourDraftPanel"
          :active-prefixes="activePrefixes"
          :objet-index="index"
        />
      </div>
      <VolObjetVoleResumeSheet
        v-else
        :obj="obj"
        :index="index"
        :libelle-resume-champ-absent="libelleResumeChampAbsent"
        :show-ajouter-autre-button="editingIndex === null && index === dernierIndexObjetValide"
        @modifier="ouvrirDialogModifierObjet"
        @supprimer="ouvrirDialogSupprimerObjet"
        @ajouter-autre="scrollVersSaisieObjet"
      />
    </template>

    <div v-if="afficherFicheSaisieNouvelObjet && editingIndex === null" :ref="definirDraftPanelRef">
      <VolObjetVoleDraftPanel :brouillon="brouillonPourDraftPanel" :active-prefixes="activePrefixes" />
    </div>
    <BaseRadioGroup
      v-model="avezVousDegradation"
      :label="t('dommages.questionDegradation')"
      required
      :options="[
        { label: t('common.oui'), value: true },
        { label: t('common.non'), value: false },
      ]"
      :error-messages="avezVousDegradationError"
    />

    <v-dialog v-model="dialogConfirmationObjetVolOuvert" max-width="440">
      <v-card v-if="dialogConfirmationObjetVol">
        <v-card-title class="text-h6 py-4">{{ titreDialogConfirmationObjetVol }}</v-card-title>
        <v-card-text class="text-body-1">{{ messageDialogConfirmationObjetVol }}</v-card-text>
        <v-card-actions class="py-3">
          <v-spacer />
          <v-btn variant="text" @click="fermerDialogConfirmationObjetVol">
            {{ t("common.annuler") }}
          </v-btn>
          <v-btn
            :color="dialogConfirmationObjetVol.action === 'supprimer' ? 'error' : 'primary'"
            variant="flat"
            @click="confirmerDialogObjetVol"
          >
            {{ libelleConfirmerDialogObjetVol }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, reactive, ref, watch, onMounted, toRaw } from "vue";
import { useField, useFormContext } from "vee-validate";
import { useI18n } from "vue-i18n";
import { useDisplay } from "vuetify";
import {
  AUTRE_OPTION,
  CATEGORIES_OBJETS,
  EMPTY_VALUE_EM_DASH,
  NUMERO_IMEI_MAX_LENGTH,
  RIPOL,
  TEXT_FIELD_MAX_LENGTH,
  TEXTAREA_MAX_LENGTH,
  VEHICULE_CATEGORIES_AVEC_VIN,
  VOL_OBJET_CATEGORIE
} from "@/constants/constant";
import VolObjetVoleResumeSheet from "./VolObjetVoleResumeSheet.vue";
import VolObjetVoleDraftPanel from "./VolObjetVoleDraftPanel.vue";
import type { VolObjetVoleDraftBrouillon } from "@/types/volObjetVoleBrouillon.types";
import { RipolService } from "@/services/ripolService";
import { filterNationalities, sortRipolByLabelFr } from "@/utils/helpers/ripolHelpers.ts";
import type { RipolSelection, Ripol } from "@/types/ripol.interface";
import type { PrePlainteFormFields, VolObjetFormSnapshot } from "@/types/pre-plainte.interface";
import BaseRadioGroup from "@/components/radio/BaseRadioGroup.vue";
import { checkLength, validerNumeroPlaque, validerPlaqueVehicule } from "@/utils/helpers/volObjetVolHelpers";

const TEXTE_VIDE = "";
const NUMERO_IMEI_REGEX = /^\d{15}$/;

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
const { mobile } = useDisplay();

const libelleResumeChampAbsent = computed(() => EMPTY_VALUE_EM_DASH);
const { setFieldValue, setFieldError } = useFormContext<PrePlainteFormFields>();

const { value: volDansVehicule, errorMessage: volDansVehiculeError } = useField<boolean | undefined>("volDansVehicule");
const { value: avezVousDegradation, errorMessage: avezVousDegradationError } = useField<boolean | null>("avezVousDegradation");

const { value: categorieObjet, errorMessage: categorieObjetError } = useField<string>("categorieObjet");
const { value: sousCategorie, errorMessage: sousCategorieError } = useField<string>("sousCategorie");
const { value: objetsVolesValides } = useField<VolObjetFormSnapshot[]>("objetsVolesValides");

const afficherFicheSaisieNouvelObjet = ref((objetsVolesValides.value?.length ?? 0) === 0);

const { value: typeObjet, errorMessage: typeObjetError } = useField<RipolSelection | null>("typeObjet");
const { value: fabricant, errorMessage: fabricantError } = useField<RipolSelection | null>("fabricant");
const { value: fabricantAutre, errorMessage: fabricantAutreError } = useField<string>("fabricantAutre");
const { value: modele, errorMessage: modeleError } = useField<RipolSelection | null>("modele");
const { value: modeleAutre, errorMessage: modeleAutreError } = useField<string>("modeleAutre");
const { value: couleur, errorMessage: couleurError } = useField<RipolSelection | null>("couleur");
const { value: couleurSecondaire } = useField<RipolSelection | null>("couleurSecondaire");
const { value: valeurReelle, errorMessage: valeurReelleError } = useField<string>("valeurReelle");
const { value: gravure, errorMessage: gravureError } = useField<string>("gravure");
const { value: numeroSerie, errorMessage: numeroSerieError } = useField("numeroSerie");
const { value: numeroSerieInconnu } = useField<boolean>("numeroSerieInconnu");
const { value: numeroIMEI, errorMessage: numeroIMEIError } = useField("numeroIMEI");
const { value: numeroIMEIInconnu } = useField<boolean>("numeroIMEIInconnu");
const { value: justificationAbsenceIMEI, errorMessage: justificationAbsenceIMEIError } = useField<string>("justificationAbsenceIMEI");
const { value: isVehicle } = useField<boolean>("isVehicle");

const { value: numeroCadre } = useField<string>("numeroCadre");
const { value: numeroCadreInconnu } = useField<boolean>("numeroCadreInconnu");
const { value: vin } = useField<string>("vin");
const { value: vinInconnu } = useField<boolean>("vinInconnu");
const { value: velofinderId } = useField<string>("velofinderId");
const { value: dateAchat } = useField<string>("dateAchat");
const { value: plaqueNumero, errorMessage: plaqueNumeroError } = useField<string>("plaqueNumero");
const { value: plaqueInconnu } = useField<boolean>("plaqueInconnu");
const { value: plaquePays, errorMessage: plaquePaysError } = useField<RipolSelection | null>("plaquePays");
const { value: plaqueCanton, errorMessage: plaqueCantonError } = useField<RipolSelection | null>("plaqueCanton");
const { value: assuranceAucune } = useField<boolean>("assuranceAucune");
const { value: assureurAutre } = useField<string>("assureurAutre");
const { value: numeroAssurance } = useField<string>("numeroAssurance");
const { value: numeroVignette } = useField<string>("numeroVignette");
const { value: numeroMaster } = useField<string>("numeroMaster");

const editingIndex = ref<number | null>(null);
const draftPanelRef = ref<HTMLElement | null>(null);
const definirDraftPanelRef = (element: unknown) => {
  draftPanelRef.value = element instanceof HTMLElement ? element : null;
};
const isRestoring = ref(false);

const categorieOptions = computed(() =>
  CATEGORIES_OBJETS.map(cat => ({
    value: cat.value,
    label: t(cat.labelKey),
  })),
);

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

const isVehicleCategory = computed(() => selectedCategorie.value?.useVehicleTypes === true);
const isVeloCategory = computed(() => sousCategorie.value === "velos");
const hasVin = computed(() => VEHICULE_CATEGORIES_AVEC_VIN.includes(sousCategorie.value) ?? !isVeloCategory.value);

const objetTypeKey = computed(() => `objets-${categorieObjet.value}-${sousCategorie.value}`);
const brandKey = computed(() => `brand-${typeObjet.value?.code ?? TEXTE_VIDE}`);
const modelKey = computed(() => `model-${fabricant.value?.code ?? TEXTE_VIDE}`);
const colourKey = computed(() => `couleur-${isVehicleCategory.value ? "vehicle" : "object"}`);

const hasBrands = ref(true);
const hasModels = ref(true);
const modelsLoading = ref(false);

const fetchFilteredObjectTypes = async (): Promise<Ripol[]> => {
  const cat = selectedCategorie.value;
  const prefixes = activePrefixes.value;
  const allObjects = cat?.useVehicleTypes
    ? await RipolService.searchVehicleTypes()
    : await RipolService.searchObjectTypes();
  if (!prefixes?.length) {
    return allObjects;
  }
  return allObjects.filter(obj => prefixes.some(prefix => obj.code.startsWith(prefix)));
};

const fetchBrands = async () => {
  if (!typeObjet.value?.code) {
    return [];
  }
  const isVehicle = selectedCategorie.value?.useVehicleTypes === true;
  const results = isVehicle
    ? await RipolService.searchVehicleBrands(undefined, typeObjet.value.code)
    : await RipolService.search("brands", undefined, {
        masterValue: typeObjet.value.code,
        masterType: RIPOL.MASTER_TYPE_OBJETS,
      });
  hasBrands.value = results.length > 0;
  return results;
};

const fetchBrandsWithAutre = async () => {
  const brands = await fetchBrands();
  return [...sortRipolByLabelFr(brands), AUTRE_OPTION];
};

const fetchModels = async () => {
  if (!fabricant.value?.code) {
    return [];
  }
  const isVehicle = selectedCategorie.value?.useVehicleTypes === true;
  modelsLoading.value = true;
  try {
    const results = isVehicle
      ? await RipolService.searchVehicleModels(fabricant.value.code)
      : await RipolService.search("models", undefined, {
          masterValue: fabricant.value.code,
        });
    hasModels.value = results.length > 0;
    return results;
  } finally {
    modelsLoading.value = false;
  }
};

const isAutreFabricant = computed(() => fabricant.value?.code === "AUTRE");

const fetchModelsWithAutre = async () => {
  if (isAutreFabricant.value) {
    return [AUTRE_OPTION];
  }
  const models = await fetchModels();
  return [...models, AUTRE_OPTION];
};

const fetchColours = async () => {
  if (isVehicleCategory.value) {
    return RipolService.searchVehicleColours();
  }
  return RipolService.searchObjectColours();
};

const fetchFilteredNationalities = async (search?: string) => {
  const data = await RipolService.searchNationalities(search);
  return filterNationalities(data);
};

const isBijouxCategory = computed(() => categorieObjet.value === VOL_OBJET_CATEGORIE.BIJOUX);
const isPlaqueCategory = computed(() => categorieObjet.value === VOL_OBJET_CATEGORIE.PLAQUE);
const suisseSelection = { code: RIPOL.PAYS_SUISSE, label: "Suisse" };
const appliquerPaysPlaqueDefaut = () => {
  if (isPlaqueCategory.value && !plaquePays.value?.code) {
    plaquePays.value = { ...suisseSelection };
  }
};

const hasImei = computed(() => {
  const code = typeObjet.value?.code;
  if (!code) {
    return false;
  }
  return code.startsWith(RIPOL.PREFIX_TELEPHONE_MOBILE) || code.startsWith(RIPOL.PREFIX_TABLETTE);
});

const isAutreModele = computed(() => modele.value?.code === "AUTRE");

const remplirBrouillonDepuisSnapshot = (obj: VolObjetFormSnapshot) => {
  categorieObjet.value = obj.categorieObjet;
  sousCategorie.value = texteOuVide(obj.sousCategorie);
  typeObjet.value = obj.typeObjet ? { ...obj.typeObjet } : null;
  fabricant.value = obj.fabricant ? { ...obj.fabricant } : null;
  fabricantAutre.value = texteOuVide(obj.fabricantAutre);
  modele.value = obj.modele ? { ...obj.modele } : null;
  modeleAutre.value = texteOuVide(obj.modeleAutre);
  couleur.value = obj.couleur ? { ...obj.couleur } : null;
  couleurSecondaire.value = obj.couleurSecondaire ? { ...obj.couleurSecondaire } : null;
  gravure.value = texteOuVide(obj.gravure);
  valeurReelle.value = texteOuVide(obj.valeurReelle);
  numeroSerie.value = texteOuVide(obj.numeroSerie);
  numeroSerieInconnu.value = !!obj.numeroSerieInconnu;
  numeroIMEI.value = texteOuVide(obj.numeroIMEI);
  numeroIMEIInconnu.value = !!obj.numeroIMEIInconnu;
  justificationAbsenceIMEI.value = texteOuVide(obj.justificationAbsenceIMEI);
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
  appliquerPaysPlaqueDefaut();
  assuranceAucune.value = !!obj.assuranceAucune;
  assureurAutre.value = texteOuVide(obj.assureurAutre) || texteOuVide(obj.assureur?.label);
  numeroAssurance.value = texteOuVide(obj.numeroAssurance);
  numeroVignette.value = texteOuVide(obj.numeroVignette);
  numeroMaster.value = texteOuVide(obj.numeroMaster);
};

const viderChampsVehiculeVol = () => {
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

const reinitialiserDependancesChangementCategorie = (nouvelleCategorie: string) => {
  sousCategorie.value = TEXTE_VIDE;
  typeObjet.value = null;
  fabricant.value = null;
  modele.value = null;
  couleur.value = null;
  hasBrands.value = true;
  hasModels.value = false;
  numeroSerieInconnu.value = false;
  viderChampsVehiculeVol();
  if (nouvelleCategorie === VOL_OBJET_CATEGORIE.TELEPHONE) {
    sousCategorie.value = VOL_OBJET_CATEGORIE.TELEPHONE_MOBILE;
  }
  if (nouvelleCategorie === VOL_OBJET_CATEGORIE.PLAQUE) {
    appliquerPaysPlaqueDefaut();
  }
};

const viderChampsDetailObjetVol = () => {
  sousCategorie.value = TEXTE_VIDE;
  typeObjet.value = null;
  fabricant.value = null;
  fabricantAutre.value = TEXTE_VIDE;
  modele.value = null;
  modeleAutre.value = TEXTE_VIDE;
  couleur.value = null;
  couleurSecondaire.value = null;
  gravure.value = TEXTE_VIDE;
  valeurReelle.value = TEXTE_VIDE;
  numeroSerie.value = TEXTE_VIDE;
  numeroSerieInconnu.value = false;
  numeroIMEI.value = TEXTE_VIDE;
  numeroIMEIInconnu.value = false;
  justificationAbsenceIMEI.value = TEXTE_VIDE;
  viderChampsVehiculeVol();
};

const CHAMPS_ERREUR_BROUILLON = [
  "typeObjet",
  "numeroSerie",
  "numeroIMEI",
  "categorieObjet",
  "sousCategorie",
  "fabricant",
  "fabricantAutre",
  "modele",
  "modeleAutre",
  "couleur",
  "valeurReelle",
  "plaqueNumero",
  "plaquePays",
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

const buildSnapshotFromDraft = (): VolObjetFormSnapshot => {
  const cat = categorieObjet.value;
  const estVehicule = cat === VOL_OBJET_CATEGORIE.VEHICULE;
  return {
    categorieObjet: cat,
    sousCategorie: sousCategorie.value,
    typeObjet: cloneRipol(typeObjet.value),
    fabricant: cloneRipol(fabricant.value),
    fabricantAutre: chaineFormulaire(fabricantAutre.value),
    modele: cloneRipol(modele.value),
    modeleAutre: chaineFormulaire(modeleAutre.value),
    couleur: cloneRipol(couleur.value),
    couleurSecondaire: cloneRipol(couleurSecondaire.value),
    gravure: chaineFormulaire(gravure.value),
    valeurReelle: chaineFormulaire(valeurReelle.value),
    numeroSerie: chaineFormulaire(numeroSerie.value),
    numeroSerieInconnu: numeroSerieInconnu.value,
    numeroIMEI: chaineFormulaire(numeroIMEI.value),
    numeroIMEIInconnu: numeroIMEIInconnu.value,
    justificationAbsenceIMEI: chaineFormulaire(justificationAbsenceIMEI.value),
    isVehicle: estVehicule,
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
  };
};

const clearDraftChampsObjet = () => {
  isRestoring.value = true;
  viderChampsDetailObjetVol();
  hasBrands.value = true;
  hasModels.value = false;
  stopRestoringOnNextTick();
};

const mettreListeObjetsVoles = (liste: VolObjetFormSnapshot[]) => {
  setFieldValue("objetsVolesValides", liste);
};

const appliquerCategorieEtSousDepuisCodeRipol = (code: string) => {
  const categorie = isVehicle.value
    ? CATEGORIES_OBJETS.find(c => c.useVehicleTypes)
    : CATEGORIES_OBJETS.find(
        c => !c.useVehicleTypes && c.prefixes.some(prefix => code.startsWith(prefix)),
      );
  if (!categorie) {
    return;
  }
  categorieObjet.value = categorie.value;
  const sous = categorie.subCategories?.find(sub =>
    sub.prefixes.some(prefix => code.startsWith(prefix)),
  );
  if (sous) {
    sousCategorie.value = sous.value;
  }
};

const numeroSerieRequis = computed(
  () =>
    categorieObjet.value === VOL_OBJET_CATEGORIE.TELEPHONE ||
    categorieObjet.value === VOL_OBJET_CATEGORIE.INFORMATIQUE ||
    categorieObjet.value === VOL_OBJET_CATEGORIE.PHOTO_VIDEO,
);

const numeroIMEIRequis = computed(
  () =>
    typeObjet.value?.code === RIPOL.CODE_TELEPHONE_MOBILE &&
    !numeroIMEIInconnu.value,
);

const validerBrouillonObjetVole = async (): Promise<boolean> => {
  if (!categorieObjet.value?.trim()) {
    setFieldError("categorieObjet", t("validation.champRequis"));
    return false;
  }

  if (isPlaqueCategory.value) {
    if (!plaquePays.value?.code) {
      setFieldError("plaquePays", t("validation.champRequis"));
      return false;
    }
    return validerNumeroPlaque(
      {
        sousCategorie: sousCategorie.value,
        plaqueInconnu: plaqueInconnu.value,
        plaqueNumero: plaqueNumero.value,
        plaquePays: plaquePays.value,
        plaqueCanton: plaqueCanton.value,
      },
      setFieldError as (field: string, message: string) => void,
      t,
    );
  }

  if (!typeObjet.value?.code) {
    setFieldError("typeObjet", t("validation.typeObjetRequis"));
    return false;
  }

  if (!couleur.value?.code) {
    setFieldError("couleur", t("validation.couleurRequise"));
    return false;
  }

  if (categorieObjet.value === VOL_OBJET_CATEGORIE.VEHICULE) {
    if (!fabricant.value?.code) {
      setFieldError("fabricant", t("validation.fabricantRequis"));
      return false;
    }
    if (fabricant.value.code === "AUTRE" && !chaineFormulaire(fabricantAutre.value).trim()) {
      setFieldError("fabricantAutre", t("validation.champRequis"));
      return false;
    }
    if (fabricant.value.code !== "AUTRE") {
      const models = await RipolService.searchVehicleModels(fabricant.value.code);
      if (models.length > 0 && !modele.value?.code) {
        setFieldError("modele", t("validation.modeleRequis"));
        return false;
      }
      if ((modele.value?.code === "AUTRE" || models.length === 0) && !chaineFormulaire(modeleAutre.value).trim()) {
        setFieldError("modeleAutre", t("validation.champRequis"));
        return false;
      }
    }

    if (isVeloCategory.value && !numeroCadreInconnu.value && !chaineFormulaire(numeroCadre.value).trim()) {
      setFieldError("numeroCadre", t("validation.numeroCadreRequis"));
      return false;
    }

    if (hasVin.value && !vinInconnu.value && !chaineFormulaire(vin.value).trim()) {
      setFieldError("vin", t("validation.vinRequis"));
      return false;
    }

    if (
      !validerPlaqueVehicule(
        {
          sousCategorie: sousCategorie.value,
          plaqueInconnu: plaqueInconnu.value,
          plaqueNumero: plaqueNumero.value,
          plaquePays: plaquePays.value,
          plaqueCanton: plaqueCanton.value,
        },
        setFieldError as (field: string, message: string) => void,
        t,
      )
    ) {
      return false;
    }
  }

  if (numeroSerieRequis.value && !numeroSerieInconnu.value && !chaineFormulaire(numeroSerie.value).trim()) {
    setFieldError("numeroSerie", t("validation.numeroSerieRequis"));
    return false;
  }

  if (numeroIMEIRequis.value && !chaineFormulaire(numeroIMEI.value).trim()) {
    setFieldError("numeroIMEI", t("validation.numeroIMEIRequis", { max: NUMERO_IMEI_MAX_LENGTH }));
    return false;
  }

  const numeroIMEITrim = chaineFormulaire(numeroIMEI.value).trim();
  if (!numeroIMEIInconnu.value && numeroIMEITrim && !NUMERO_IMEI_REGEX.test(numeroIMEITrim)) {
    setFieldError("numeroIMEI", t("validation.numeroIMEIFormat"));
    return false;
  }

  if (!checkLength(fabricantAutre.value, TEXT_FIELD_MAX_LENGTH)) {
    setFieldError("fabricantAutre", t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH }));
    return false;
  }

  if (!checkLength(modeleAutre.value, TEXT_FIELD_MAX_LENGTH)) {
    setFieldError("modeleAutre", t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH }));
    return false;
  }

  if (!checkLength(numeroSerie.value, TEXT_FIELD_MAX_LENGTH)) {
    setFieldError("numeroSerie", t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH }));
    return false;
  }

  if (!checkLength(justificationAbsenceIMEI.value, TEXTAREA_MAX_LENGTH)) {
    setFieldError("justificationAbsenceIMEI", t("validation.longueurMax", { max: TEXTAREA_MAX_LENGTH }));
    return false;
  }

  if (!checkLength(gravure.value, TEXT_FIELD_MAX_LENGTH)) {
    setFieldError("gravure", t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH }));
    return false;
  }

  if (!checkLength(vin.value, TEXT_FIELD_MAX_LENGTH)) {
    setFieldError("vin", t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH }));
    return false;
  }

  if (!checkLength(numeroCadre.value, TEXT_FIELD_MAX_LENGTH)) {
    setFieldError("numeroCadre", t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH }));
    return false;
  }

  if (!checkLength(velofinderId.value, TEXT_FIELD_MAX_LENGTH)) {
    setFieldError("velofinderId", t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH }));
    return false;
  }

  if (!checkLength(assureurAutre.value, TEXT_FIELD_MAX_LENGTH)) {
    setFieldError("assureurAutre", t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH }));
    return false;
  }

  if (!checkLength(numeroAssurance.value, TEXT_FIELD_MAX_LENGTH)) {
    setFieldError("numeroAssurance", t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH }));
    return false;
  }

  if (!checkLength(numeroVignette.value, TEXT_FIELD_MAX_LENGTH)) {
    setFieldError("numeroVignette", t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH }));
    return false;
  }

  if (!checkLength(numeroMaster.value, TEXT_FIELD_MAX_LENGTH)) {
    setFieldError("numeroMaster", t("validation.longueurMax", { max: TEXT_FIELD_MAX_LENGTH }));
    return false;
  }

  return true;
};

const enregistrerObjetVole = (snapshot: VolObjetFormSnapshot) => {
  const next = [...(objetsVolesValides.value ?? [])];
  const idx = editingIndex.value;
  const indexValide = idx != null && next[idx] !== undefined;

  if (indexValide) {
    next[idx] = snapshot;
  } else {
    next.push(snapshot);
  }

  mettreListeObjetsVoles(next);
};

const finaliserValidationObjetVole = () => {
  editingIndex.value = null;
  clearDraftChampsObjet();
  afficherFicheSaisieNouvelObjet.value = false;
};
const validerObjetVole = async (): Promise<boolean> => {
  effacerErreursBrouillon();

  const isBrouillonValide = await validerBrouillonObjetVole();

  if (!isBrouillonValide) {
    return false;
  }

  enregistrerObjetVole(buildSnapshotFromDraft());
  finaliserValidationObjetVole();
  return true;
};

const validerBrouillonAvantNavigation = async (): Promise<boolean> => {
  if (!afficherFicheSaisieNouvelObjet.value) {
    return true;
  }
  const brouillonValide = await validerObjetVole();
  if (!brouillonValide) {
    void nextTick(() => draftPanelRef.value?.scrollIntoView({ behavior: "smooth", block: "start" }));
  }
  return brouillonValide;
};

defineExpose({ validerBrouillonAvantNavigation });

const initBrouillonAuMontage = () => {
  const codeType = typeObjet.value?.code;
  if (codeType) {
    isRestoring.value = true;
    appliquerCategorieEtSousDepuisCodeRipol(codeType);
    stopRestoringOnNextTick();
    return;
  }

  const listeVide = !(objetsVolesValides.value?.length);
  const categorieVide = !categorieObjet.value?.trim();
  if (!listeVide || !categorieVide) {
    appliquerPaysPlaqueDefaut();
    return;
  }
  isRestoring.value = true;
  stopRestoringOnNextTick();
};

watch(
  isVehicleCategory,
  newValue => {
    isVehicle.value = newValue;
  },
  { immediate: true },
);

watch(sousCategorie, () => {
  if (isRestoring.value || isVehicleCategory.value) {
    return;
  }
  typeObjet.value = null;
  fabricant.value = null;
  modele.value = null;
  hasBrands.value = true;
  hasModels.value = false;
});

watch(typeObjet, () => {
  if (isRestoring.value || isVehicleCategory.value) {
    return;
  }
  fabricant.value = null;
  modele.value = null;
  hasBrands.value = true;
  hasModels.value = false;
});

watch(fabricant, () => {
  if (isRestoring.value || isVehicleCategory.value) {
    return;
  }
  modele.value = null;
  hasModels.value = true;
});

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

const brouillon = reactive({
  TEXTE_VIDE,
  isRestoring,
  categorieObjet,
  categorieObjetError,
  sousCategorie,
  sousCategorieError,
  objetsVolesValides,
  categorieOptions,
  subCategorieOptions,
  activePrefixes,
  typeObjet,
  typeObjetError,
  fabricant,
  fabricantError,
  fabricantAutre,
  fabricantAutreError,
  modele,
  modeleError,
  modeleAutre,
  modeleAutreError,
  couleur,
  couleurError,
  couleurSecondaire,
  valeurReelle,
  valeurReelleError,
  gravure,
  gravureError,
  numeroSerie,
  numeroSerieError,
  numeroSerieInconnu,
  numeroSerieRequis,
  numeroIMEI,
  numeroIMEIError,
  numeroIMEIInconnu,
  justificationAbsenceIMEI,
  justificationAbsenceIMEIError,
  plaqueNumero,
  plaqueNumeroError,
  plaquePays,
  plaquePaysError,
  plaqueCanton,
  plaqueCantonError,
  fetchFilteredNationalities,
  objetTypeKey,
  brandKey,
  modelKey,
  colourKey,
  isBijouxCategory,
  isPlaqueCategory,
  hasImei,
  isAutreFabricant,
  isAutreModele,
  hasBrands,
  hasModels,
  modelsLoading,
  fetchFilteredObjectTypes,
  fetchBrandsWithAutre,
  fetchModelsWithAutre,
  fetchColours,
  remplirBrouillonDepuisSnapshot,
  reinitialiserDependancesChangementCategorie,
  viderChampsDetailObjetVol,
  clearDraftChampsObjet,
  validerObjetVole,
  initBrouillonAuMontage,
  mettreListeObjetsVoles,
});

const brouillonPourDraftPanel = brouillon as unknown as VolObjetVoleDraftBrouillon;

const dernierIndexObjetValide = computed(() => {
  const n = brouillon.objetsVolesValides?.length ?? 0;
  return n > 0 ? n - 1 : -1;
});

watch(
  () => brouillon.objetsVolesValides?.length ?? 0,
  n => {
    if (n === 0) {
      afficherFicheSaisieNouvelObjet.value = true;
    }
  },
);

watch(
  () => brouillon.categorieObjet,
  value => {
    if (brouillon.isRestoring) {
      return;
    }
    brouillon.reinitialiserDependancesChangementCategorie(value);
  },
);

type ActionDialogObjetVol = "modifier" | "supprimer";

const dialogConfirmationObjetVol = ref<{ action: ActionDialogObjetVol; index: number } | null>(null);

const dialogConfirmationObjetVolOuvert = computed({
  get: () => dialogConfirmationObjetVol.value !== null,
  set: (ouvert: boolean) => {
    if (!ouvert) {
      dialogConfirmationObjetVol.value = null;
    }
  },
});

const titreDialogConfirmationObjetVol = computed(() => {
  const d = dialogConfirmationObjetVol.value;
  if (!d) {
    return "";
  }
  return d.action === "modifier"
    ? t("incidentTypes.confirmationModifierObjetVole.titre")
    : t("incidentTypes.confirmationSupprimerObjetVole.titre");
});

const messageDialogConfirmationObjetVol = computed(() => {
  const d = dialogConfirmationObjetVol.value;
  if (!d) {
    return "";
  }
  return d.action === "modifier"
    ? t("incidentTypes.confirmationModifierObjetVole.message")
    : t("incidentTypes.confirmationSupprimerObjetVole.message");
});

const libelleConfirmerDialogObjetVol = computed(() => {
  const d = dialogConfirmationObjetVol.value;
  if (!d) {
    return "";
  }
  return d.action === "modifier"
    ? t("incidentTypes.confirmationModifierObjetVole.confirmer")
    : t("incidentTypes.confirmationSupprimerObjetVole.confirmer");
});

const ouvrirDialogModifierObjet = (index: number) => {
  dialogConfirmationObjetVol.value = { action: "modifier", index };
};

const ouvrirDialogSupprimerObjet = (index: number) => {
  dialogConfirmationObjetVol.value = { action: "supprimer", index };
};

const fermerDialogConfirmationObjetVol = () => {
  dialogConfirmationObjetVol.value = null;
};

const confirmerDialogObjetVol = () => {
  const d = dialogConfirmationObjetVol.value;
  if (!d) {
    return;
  }
  dialogConfirmationObjetVol.value = null;
  if (d.action === "modifier") {
    modifierObjet(d.index);
  } else {
    supprimerObjet(d.index);
  }
};

const modifierObjet = (index: number) => {
  const obj = brouillon.objetsVolesValides?.[index];
  if (!obj) {
    return;
  }
  afficherFicheSaisieNouvelObjet.value = true;
  brouillon.isRestoring = true;
  editingIndex.value = index;
  brouillon.remplirBrouillonDepuisSnapshot(obj);
  stopRestoringOnNextTick();
};

const supprimerObjet = (index: number) => {
  const next = [...(brouillon.objetsVolesValides ?? [])];
  next.splice(index, 1);
  brouillon.mettreListeObjetsVoles(next);
  if (editingIndex.value === index) {
    editingIndex.value = null;
    brouillon.clearDraftChampsObjet();
  } else if (editingIndex.value !== null && editingIndex.value > index) {
    editingIndex.value = editingIndex.value - 1;
  }
};

const scrollVersSaisieObjet = async () => {
  afficherFicheSaisieNouvelObjet.value = true;
  await nextTick();
  draftPanelRef.value?.scrollIntoView({ behavior: "smooth", block: "start" });
};

onMounted(() => {
  brouillon.initBrouillonAuMontage();
  if ((brouillon.objetsVolesValides?.length ?? 0) > 0) {
    afficherFicheSaisieNouvelObjet.value = false;
  }
});
</script>

<style scoped>
fieldset {
  border: 0;
  padding: 0;
  margin: 0;
}
legend {
  padding: 0;
}

:deep(.objet-vole-resume),
:deep(.objet-vole-brouillon) {
  background-color: var(--md-sys-color-surface-container-lowest, #f7fafc);
}
</style>
