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

type VehicleDetailsFields = ReturnType<typeof useVehicleDetailsFields>;

type VehicleDetailsWatchersContext = {
  fields: VehicleDetailsFields;
  sousCategorie: Ref<string>;
  hasPlateNumber: Ref<boolean>;
  resetVehicleCaches: () => void;
  appliquerPaysVehiculeDefaut: () => void;
  hasBrands: Ref<boolean>;
  hasModels: Ref<boolean>;
  allBrandsCache: Ref<Ripol[] | null>;
  allModelsCache: Ref<Ripol[] | null>;
};

function useVehicleDetailsFields() {
  const { value: typeObjet, errorMessage: typeObjetError } = useField<RipolSelection | null>("typeObjet");
  const { value: fabricant, errorMessage: fabricantError } = useField<RipolSelection | null>("fabricant");
  const { value: fabricantAutre, errorMessage: fabricantAutreError } = useField<string>("fabricantAutre");
  const { value: modele, errorMessage: modeleError } = useField<RipolSelection | null>("modele");
  const { value: modeleAutre, errorMessage: modeleAutreError } = useField<string>("modeleAutre");

  const { value: couleur, errorMessage: couleurError } = useField<RipolSelection | null>("couleur");
  const { value: couleurSecondaire } = useField<RipolSelection | null>("couleurSecondaire");

  const { value: numeroCadre, errorMessage: numeroCadreError } = useField<string>("numeroCadre");
  const { value: numeroCadreInconnu } = useField<boolean>("numeroCadreInconnu");

  const { value: vin, errorMessage: vinError } = useField<string>("vin");
  const { value: vinInconnu } = useField<boolean>("vinInconnu");

  const { value: velofinderId, errorMessage: velofinderIdError } = useField<string>("velofinderId");
  const { value: dateAchat, errorMessage: dateAchatError } = useField<string>("dateAchat");

  const { value: plaqueNumero, errorMessage: plaqueNumeroError } = useField<string>("plaqueNumero");
  const { value: plaqueInconnu } = useField<boolean>("plaqueInconnu");
  const { value: plaquePays, errorMessage: plaquePaysError } = useField<RipolSelection | null>("plaquePays");
  const { value: plaqueCanton, errorMessage: plaqueCantonError } = useField<RipolSelection | null>("plaqueCanton");

  const { value: assuranceAucune } = useField<boolean>("assuranceAucune");
  const { value: assureurAutre, errorMessage: assureurAutreError } = useField<string>("assureurAutre");
  const { value: numeroAssurance, errorMessage: numeroAssuranceError } = useField<string>("numeroAssurance");
  const { value: numeroVignette, errorMessage: numeroVignetteError } = useField<string>("numeroVignette");
  const { value: numeroMaster, errorMessage: numeroMasterError } = useField<string>("numeroMaster");

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
    numeroCadreError,
    numeroCadreInconnu,
    vin,
    vinError,
    vinInconnu,
    velofinderId,
    velofinderIdError,
    dateAchat,
    dateAchatError,
    plaqueNumero,
    plaqueNumeroError,
    plaqueInconnu,
    plaquePays,
    plaquePaysError,
    plaqueCanton,
    plaqueCantonError,
    assuranceAucune,
    assureurAutre,
    assureurAutreError,
    numeroAssurance,
    numeroAssuranceError,
    numeroVignette,
    numeroVignetteError,
    numeroMaster,
    numeroMasterError,
  };
}

function createVehicleDetailsResult<TState extends Record<string, unknown>>(
  fields: VehicleDetailsFields,
  state: TState,
): VehicleDetailsFields & TState {
  return {
    ...fields,
    ...state,
  };
}

function setupVehicleDetailsWatchers({
  fields,
  sousCategorie,
  hasPlateNumber,
  resetVehicleCaches,
  appliquerPaysVehiculeDefaut,
  hasBrands,
  hasModels,
  allBrandsCache,
  allModelsCache,
}: VehicleDetailsWatchersContext) {
  watch(sousCategorie, () => {
    fields.typeObjet.value = null;
    fields.fabricant.value = null;
    fields.modele.value = null;
    hasBrands.value = true;
    hasModels.value = false;
    resetVehicleCaches();

    fields.plaqueNumero.value = "";
    fields.plaquePays.value = null;
    fields.plaqueCanton.value = null;
    fields.plaqueInconnu.value = false;

    fields.vin.value = "";
    fields.vinInconnu.value = false;

    fields.numeroCadre.value = "";
    fields.numeroCadreInconnu.value = false;

    appliquerPaysVehiculeDefaut();
  });

  watch(hasPlateNumber, show => {
    if (show) {
      appliquerPaysVehiculeDefaut();
    }
  });

  watch(fields.typeObjet, () => {
    fields.fabricant.value = null;
    fields.modele.value = null;
    hasBrands.value = true;
    hasModels.value = false;
    allBrandsCache.value = null;
    allModelsCache.value = null;
  });

  watch(fields.fabricant, () => {
    fields.modele.value = null;
    hasModels.value = true;
    allModelsCache.value = null;
  });

  watch(fields.assuranceAucune, isNone => {
    if (!isNone) {
      return;
    }
    fields.assureurAutre.value = "";
    fields.numeroAssurance.value = "";
    fields.numeroVignette.value = "";
    fields.numeroMaster.value = "";
  });
}

export function useVehicleDetailsRipol({ sousCategorie, activePrefixes }: UseVehicleDetailsRipolArgs) {
  const fields = useVehicleDetailsFields();
  const {
    typeObjet,
    fabricant,
    modele,
    plaquePays,
  } = fields;

  const objetTypeKey = computed(() => `vehicule-objets-${sousCategorie.value}`);
  const brandKey = computed(() => `vehicule-brand-${typeObjet.value?.code ?? ""}`);
  const modelKey = computed(() => `vehicule-model-${fabricant.value?.code ?? ""}`);
  const colourKey = computed(() => `vehicule-colour`);

  const hasBrands = ref(true);
  const hasModels = ref(true);
  const brandsLoading = ref(false);
  const modelsLoading = ref(false);

  const vehicleTypesCache = ref<Ripol[] | null>(null);
  const allBrandsCache = ref<Ripol[] | null>(null);
  const allModelsCache = ref<Ripol[] | null>(null);
  let vehicleColoursCache: Ripol[] | null = null;

  const isAutreFabricant = computed(() => fabricant.value?.code === AUTRE_OPTION.code);
  const isAutreModele = computed(() => modele.value?.code === AUTRE_OPTION.code);

  const isSwissPlate = computed(() => plaquePays.value?.code === RIPOL.PAYS_SUISSE);
  const isVeloCategory = computed(() => sousCategorie.value === "velos");

  const hasVin = computed(() =>
    VEHICULE_CATEGORIES_AVEC_VIN.includes(sousCategorie.value) ?? !isVeloCategory.value);

  const hasPlateNumber = computed(() => VEHICULE_CATEGORIES_AVEC_PLAQUE.includes(sousCategorie.value));

  const isPlaqueObligatoire = computed(() => VEHICULE_CATEGORIES_PLAQUE_OBLIGATOIRE.includes(sousCategorie.value));

  const filterByActivePrefixes = (items: Ripol[]): Ripol[] => {
    const prefixes = activePrefixes.value;
    if (!prefixes?.length) {
      return items;
    }
    return items.filter(obj => prefixes.some(prefix => obj.code.startsWith(prefix)));
  };

  const loadVehicleTypes = async (): Promise<Ripol[]> => {
    if (vehicleTypesCache.value) {
      return vehicleTypesCache.value;
    }
    const allObjects = await RipolService.searchVehicleTypes();
    vehicleTypesCache.value = sortRipolByLabelFr(filterByActivePrefixes(allObjects));
    return vehicleTypesCache.value;
  };

  const loadAllBrands = async (): Promise<Ripol[]> => {
    if (!typeObjet.value?.code) {
      return [];
    }
    if (allBrandsCache.value) {
      return allBrandsCache.value;
    }
    brandsLoading.value = true;
    try {
      const results = await RipolService.searchVehicleBrands(undefined, typeObjet.value.code);
      allBrandsCache.value = sortRipolByLabelFr(results);
      hasBrands.value = allBrandsCache.value.length > 0;
      return allBrandsCache.value;
    } finally {
      brandsLoading.value = false;
    }
  };

  const loadAllModels = async (): Promise<Ripol[]> => {
    if (isAutreFabricant.value || !fabricant.value?.code) {
      return [];
    }
    if (allModelsCache.value) {
      return allModelsCache.value;
    }
    modelsLoading.value = true;
    try {
      const results = await RipolService.searchVehicleModels(fabricant.value.code);
      allModelsCache.value = sortRipolByLabelFr(results);
      hasModels.value = allModelsCache.value.length > 0;
      return allModelsCache.value;
    } finally {
      modelsLoading.value = false;
    }
  };

  const fetchFilteredObjectTypes = async (): Promise<Ripol[]> => loadVehicleTypes();

  const fetchBrandsWithAutre = async (): Promise<Ripol[]> => {
    const brands = await loadAllBrands();
    return [...brands, AUTRE_OPTION];
  };

  const fetchModelsWithAutre = async (): Promise<Ripol[]> => {
    if (isAutreFabricant.value) {
      return [AUTRE_OPTION];
    }
    const models = await loadAllModels();
    return [...models, AUTRE_OPTION];
  };

  const fetchColours = async () => {
    vehicleColoursCache ??= await RipolService.searchVehicleColours();
    return vehicleColoursCache;
  };

  const resetVehicleCaches = () => {
    vehicleTypesCache.value = null;
    allBrandsCache.value = null;
    allModelsCache.value = null;
    vehicleColoursCache = null;
  };

  const appliquerPaysVehiculeDefaut = () => {
    if (hasPlateNumber.value && !plaquePays.value?.code) {
      plaquePays.value = { code: RIPOL.PAYS_SUISSE, label: "Suisse" };
    }
  };

  setupVehicleDetailsWatchers({
    fields,
    sousCategorie,
    hasPlateNumber,
    resetVehicleCaches,
    appliquerPaysVehiculeDefaut,
    hasBrands,
    hasModels,
    allBrandsCache,
    allModelsCache,
  });

  return createVehicleDetailsResult(fields, {
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
  });
}
