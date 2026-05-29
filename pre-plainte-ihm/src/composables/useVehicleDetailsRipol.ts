// src/composables/useVehicleDetailsRipol.ts
import { computed, ref, watch, type Ref } from "vue";
import { useField } from "vee-validate";
import {
  AUTRE_OPTION,
  RIPOL,
  VEHICLE_INSURERS_FALLBACK,
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

  const { value: plaqueNumero } = useField<string>("plaqueNumero");
  const { value: plaqueInconnu } = useField<boolean>("plaqueInconnu");
  const { value: plaquePays } = useField<RipolSelection | null>("plaquePays");
  const { value: plaqueCanton } = useField<RipolSelection | null>("plaqueCanton");

  const { value: assuranceAucune } = useField<boolean>("assuranceAucune");
  const { value: assureur } = useField<RipolSelection | null>("assureur");
  const { value: assureurAutre, errorMessage: assureurAutreError } = useField<string>("assureurAutre");
  const { value: numeroAssurance } = useField<string>("numeroAssurance");
  const { value: numeroVignette } = useField<string>("numeroVignette");
  const { value: numeroMaster } = useField<string>("numeroMaster");

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

  const isAutreFabricant = computed(() => fabricant.value?.code === "AUTRE");
  const isAutreModele = computed(() => modele.value?.code === "AUTRE");
  const isAutreAssureur = computed(() => assureur.value?.code === "AUTRE");

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
      const results = await RipolService.searchVehicleBrands(typeObjet.value.code);
      allBrandsCache.value = sortRipolByLabelFr(results);
      hasBrands.value = allBrandsCache.value.length > 0;
      return allBrandsCache.value;
    } finally {
      brandsLoading.value = false;
    }
  };

  const loadAllModels = async (): Promise<Ripol[]> => {
    if (isAutreFabricant.value || !fabricant.value?.code || fabricant.value.code === "AUTRE") {
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
    if (!vehicleColoursCache) {
      vehicleColoursCache = await RipolService.searchVehicleColours();
    }
    return vehicleColoursCache;
  };

  const fetchVehicleInsurersWithAutre = async (search?: string): Promise<RipolSelection[]> => {
    let insurers = await RipolService.searchVehicleInsurers(search);
    if (insurers.length === 0) {
      const query = search?.trim().toLowerCase() ?? "";
      insurers = query
        ? VEHICLE_INSURERS_FALLBACK.filter(item => {
            const labels = [item.labelFr, item.labelDe, item.code].map(v => (v ?? "").toLowerCase());
            return labels.some(label => label.includes(query));
          })
        : [...VEHICLE_INSURERS_FALLBACK];
    }
    return [...sortRipolByLabelFr(insurers), AUTRE_OPTION];
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

  watch(sousCategorie, () => {
    typeObjet.value = null;
    fabricant.value = null;
    modele.value = null;
    hasBrands.value = true;
    hasModels.value = false;
    resetVehicleCaches();

    plaqueNumero.value = "";
    plaquePays.value = null;
    plaqueCanton.value = null;
    plaqueInconnu.value = false;

    vin.value = "";
    vinInconnu.value = false;

    numeroCadre.value = "";
    numeroCadreInconnu.value = false;

    appliquerPaysVehiculeDefaut();
  });

  watch(hasPlateNumber, show => {
    if (show) {
      appliquerPaysVehiculeDefaut();
    }
  });

  watch(typeObjet, () => {
    fabricant.value = null;
    modele.value = null;
    hasBrands.value = true;
    hasModels.value = false;
    allBrandsCache.value = null;
    allModelsCache.value = null;
  });

  watch(fabricant, () => {
    modele.value = null;
    hasModels.value = true;
    allModelsCache.value = null;
  });

  watch(assuranceAucune, isNone => {
    if (!isNone) {
      return;
    }
    assureur.value = null;
    assureurAutre.value = "";
    numeroAssurance.value = "";
    numeroVignette.value = "";
    numeroMaster.value = "";
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
    plaqueInconnu,
    plaquePays,
    plaqueCanton,
    assuranceAucune,
    assureur,
    assureurAutre,
    assureurAutreError,
    numeroAssurance,
    numeroVignette,
    numeroMaster,

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
    isAutreAssureur,
    isSwissPlate,
    isVeloCategory,
    hasVin,
    hasPlateNumber,
    isPlaqueObligatoire,

    fetchFilteredObjectTypes,
    fetchBrandsWithAutre,
    fetchModelsWithAutre,
    fetchColours,
    fetchVehicleInsurersWithAutre,
    RipolService,
  };
}
