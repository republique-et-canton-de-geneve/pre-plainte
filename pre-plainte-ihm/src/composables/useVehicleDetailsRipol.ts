// src/composables/useVehicleDetailsRipol.ts
import { computed, ref, watch, type Ref } from "vue";
import { useField } from "vee-validate";
import {
  AUTRE_OPTION,
  RIPOL,
  VEHICULE_CATEGORIES_AVEC_PLAQUE,
  VEHICULE_CATEGORIES_AVEC_VIN,
  VEHICULE_CATEGORIES_PLAQUE_OBLIGATOIRE,
} from "@/constants/constant";
import { RipolService } from "@/services/ripolService";
import { sortRipolByLabelFr } from "@/utils/helpers/ripolHelpers.ts";
import type { RipolSelection, Ripol } from "@/types/ripol.interface";

type UseVehicleDetailsRipolArgs = {
  sousCategorie: Ref<string>;
  activePrefixes: Ref<readonly string[]>;
};

export function useVehicleDetailsRipol({ sousCategorie, activePrefixes }: UseVehicleDetailsRipolArgs) {
  const { value: typeObjet, errorMessage: typeObjetError } = useField<RipolSelection | null>("typeObjet");
  const { value: fabricant, errorMessage: fabricantError } = useField<RipolSelection | null>("fabricant");
  const { value: fabricantAutre, errorMessage: fabricantAutreError } = useField<string>("fabricantAutre");
  const { value: modele, errorMessage: modeleError } = useField<RipolSelection | null>("modele");
  const { value: modeleAutre, errorMessage: modeleAutreError } = useField<string>("modeleAutre");

  const { value: couleur, errorMessage: couleurError } = useField<RipolSelection | null>("couleur");
  const { value: couleurSecondaire } = useField<RipolSelection | null>("couleurSecondaire");

  const { value: numeroCadre } = useField<string>("numeroCadre");
  const { value: numeroCadreInconnu } = useField<boolean>("numeroCadreInconnu");

  const { value: vin, errorMessage: vinError } = useField<string>("vin");
  const { value: vinInconnu } = useField<boolean>("vinInconnu");

  const { value: velofinderId } = useField<string>("velofinderId");
  const { value: dateAchat, errorMessage: dateAchatError } = useField<string>("dateAchat");

  const { value: plaqueNumero, errorMessage: plaqueNumeroError } = useField<string>("plaqueNumero");
  const { value: plaqueInconnu } = useField<boolean>("plaqueInconnu");
  const { value: plaquePays } = useField<RipolSelection | null>("plaquePays");
  const { value: plaqueCanton } = useField<RipolSelection | null>("plaqueCanton");

  const objetTypeKey = computed(() => `vehicule-objets-${sousCategorie.value}`);
  const brandKey = computed(() => `vehicule-brand-${typeObjet.value?.code ?? ""}`);
  const modelKey = computed(() => `vehicule-model-${fabricant.value?.code ?? ""}`);
  const colourKey = computed(() => `vehicule-colour`);

  const hasBrands = ref(true);
  const hasModels = ref(true);
  const brandsLoading = ref(false);
  const modelsLoading = ref(false);

  const isAutreFabricant = computed(() => fabricant.value?.code === "AUTRE");
  const isAutreModele = computed(() => modele.value?.code === "AUTRE");

  const isSwissPlate = computed(() => plaquePays.value?.code === RIPOL.PAYS_SUISSE);
  const isVeloCategory = computed(() => sousCategorie.value === "velos");

  const hasVin = computed(() =>
    VEHICULE_CATEGORIES_AVEC_VIN.includes(sousCategorie.value) ?? !isVeloCategory.value);

  const hasPlateNumber = computed(() => VEHICULE_CATEGORIES_AVEC_PLAQUE.includes(sousCategorie.value));

  const isPlaqueObligatoire = computed(() => VEHICULE_CATEGORIES_PLAQUE_OBLIGATOIRE.includes(sousCategorie.value));

  const fetchFilteredObjectTypes = async (): Promise<Ripol[]> => {
    const allObjects = await RipolService.searchVehicleTypes();
    const prefixes = activePrefixes.value;

    if (!prefixes || prefixes.length === 0) {
      return allObjects;
    }
    return allObjects.filter(obj => prefixes.some(prefix => obj.code.startsWith(prefix)));
  };

  const fetchBrands = async () => {
    if (!typeObjet.value?.code) {
      return [];
    }
    brandsLoading.value = true;
    try {
      const results = await RipolService.search("brands", undefined, {
        masterValue: typeObjet.value.code,
        masterType: RIPOL.MASTER_TYPE_VEHICULES,
      });
      hasBrands.value = results.length > 0;
      return results;
    } finally {
      brandsLoading.value = false;
    }
  };

  const fetchBrandsWithAutre = async () => {
    const brands = await fetchBrands();
    return [...sortRipolByLabelFr(brands), AUTRE_OPTION];
  };

  const fetchModels = async () => {
    if (!fabricant.value?.code) {
      return [];
    }
    modelsLoading.value = true;
    try {
      const results = await RipolService.search("models", undefined, { masterValue: fabricant.value.code });
      hasModels.value = results.length > 0;
      return results;
    } finally {
      modelsLoading.value = false;
    }
  };

  const fetchModelsWithAutre = async () => {
    if (isAutreFabricant.value) {
      return [AUTRE_OPTION];
    }
    const models = await fetchModels();
    return [...models, AUTRE_OPTION];
  };

  const fetchColours = async () => RipolService.searchVehicleColours();

  watch(sousCategorie, () => {
    typeObjet.value = null;
    fabricant.value = null;
    modele.value = null;
    hasBrands.value = true;
    hasModels.value = false;

    plaqueNumero.value = "";
    plaquePays.value = null;
    plaqueCanton.value = null;
    plaqueInconnu.value = false;

    vin.value = "";
    vinInconnu.value = false;

    numeroCadre.value = "";
    numeroCadreInconnu.value = false;
  });

  watch(typeObjet, () => {
    fabricant.value = null;
    modele.value = null;
    hasBrands.value = true;
    hasModels.value = false;
  });

  watch(fabricant, () => {
    modele.value = null;
    hasModels.value = true;
  });

  return {
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
    numeroCadre,
    numeroCadreInconnu,
    vin,
    vinError,
    vinInconnu,
    velofinderId,
    dateAchat,
    dateAchatError,
    plaqueNumero,
    plaqueNumeroError,
    plaqueInconnu,
    plaquePays,
    plaqueCanton,

    objetTypeKey,
    brandKey,
    modelKey,
    colourKey,
    hasBrands,
    hasModels,
    brandsLoading,
    modelsLoading,
    isAutreFabricant,
    isAutreModele,
    isSwissPlate,
    isVeloCategory,
    hasVin,
    hasPlateNumber,
    isPlaqueObligatoire,

    fetchFilteredObjectTypes,
    fetchBrandsWithAutre,
    fetchModelsWithAutre,
    fetchColours,
    RipolService,
  };
}
