<template>
  <AccessibleVSelect
    v-if="showCategorieSelect"
    :model-value="categorieObjet"
    :label="t('categoriesObjets.titre')"
    :items="categorieObjetOptions"
    item-title="label"
    item-value="value"
    class="mb-4"
    variant="outlined"
    disabled
    :hint="t('categoriesObjets.hint')"
    persistent-hint
  />

  <AccessibleVSelect
    v-model="sousCategorie"
    :label="t('sousCategories.titre')"
    required
    :items="computedSubCategorieOptions"
    item-title="label"
    item-value="value"
    class="mb-4"
    variant="outlined"
    :error-messages="sousCategorieError"
    :hint="t('sousCategories.hint')"
    persistent-hint
  />

  <RipolAutocomplete
    v-model="typeObjet"
    :key="objetTypeKey"
    :label="t('incidentTypes.typeObjet')"
    required
    :fetch-fn="fetchFilteredObjectTypes"
    :error-messages="typeObjetError"
    :hint="t('incidentTypes.hintTypeObjet')"
    :preload="true"
    :min-search-length="0"
    class="mb-8"
  />

  <RipolAutocomplete
    v-if="typeObjet && hasBrands"
    v-model="fabricant"
    :key="brandKey"
    :label="t('incidentTypes.fabricant')"
    :fetch-fn="fetchBrandsWithAutre"
    :error-messages="fabricantError"
    :hint="t('incidentTypes.hintFabricant')"
    :preload="true"
    :min-search-length="0"
    class="mb-2"
  />

  <v-text-field
    v-if="isAutreFabricant"
    v-model="fabricantAutre"
    :label="t('incidentTypes.fabricantAutre')"
    :hint="t('incidentTypes.hintFabricantAutre')"
    variant="outlined"
    persistent-hint
    class="mb-4"
  />

  <RipolAutocomplete
    v-if="fabricant && hasModels"
    v-model="modele"
    :key="modelKey"
    :label="t('incidentTypes.modele')"
    :fetch-fn="fetchModelsWithAutre"
    :error-messages="modeleError"
    :hint="t('incidentTypes.hintModele')"
    :preload="true"
    :min-search-length="0"
    class="mb-2"
  />

  <v-text-field
    v-if="isAutreModele"
    v-model="modeleAutre"
    :label="t('incidentTypes.modeleAutre')"
    :hint="t('incidentTypes.hintModeleAutre')"
    variant="outlined"
    persistent-hint
    class="mb-8"
  />

  <RipolAutocomplete
    v-model="couleur"
    :key="colourKey"
    :label="t('incidentTypes.couleur')"
    :fetch-fn="fetchColours"
    :error-messages="couleurError"
    :hint="t('incidentTypes.hintCouleur')"
    :preload="true"
    :min-search-length="0"
    class="mb-4"
  />

  <RipolAutocomplete
    v-model="couleurSecondaire"
    :key="`${colourKey}-2`"
    :label="t('incidentTypes.couleurSecondaire')"
    :fetch-fn="fetchColours"
    :hint="t('incidentTypes.hintCouleurSecondaire')"
    :preload="true"
    :min-search-length="0"
    class="mb-8"
  />

  <template v-if="isVeloCategory">
    <v-text-field
      :label="t('incidentTypes.numeroCadre')"
      v-model="numeroCadre"
      :disabled="numeroCadreInconnu"
      class="mb-2"
      variant="outlined"
      :hint="t('incidentTypes.hintNumeroCadre')"
      persistent-hint
    />
    <v-checkbox v-model="numeroCadreInconnu" :label="t('incidentTypes.numeroCadreInconnu')" class="mb-6" hide-details />
  </template>

  <template v-if="hasVin">
    <v-text-field
      :label="t('incidentTypes.vin')"
      v-model="vin"
      :disabled="vinInconnu"
      class="mb-2"
      :error-messages="vinError"
      variant="outlined"
      :hint="t('incidentTypes.hintVin')"
      persistent-hint
    />
    <v-checkbox v-model="vinInconnu" :label="t('incidentTypes.vinInconnu')" class="mb-6" hide-details />
  </template>

  <v-text-field
    v-if="isVeloCategory"
    :label="t('incidentTypes.velofinderId')"
    v-model="velofinderId"
    class="mb-8"
    variant="outlined"
    :hint="t('incidentTypes.hintVelofinderId')"
    persistent-hint
  />

  <v-text-field
    :label="t('incidentTypes.dateAchat')"
    v-model="dateAchat"
    type="text"
    placeholder="JJ.MM.AAAA"
    class="mb-8"
    :error-messages="dateAchatError"
    variant="outlined"
    prepend-inner-icon="mdi-calendar"
    :hint="t('incidentTypes.hintDateAchat')"
    persistent-hint
    @input="onDateAchatInput"
  />

  <template v-if="hasPlateNumber">
    <h5 class="mb-2 text-subtitle-1">{{ t("incidentTypes.plaqueImmatriculation") }}</h5>
    <RipolAutocomplete
      v-if="!plaqueInconnu"
      v-model="plaquePays"
      :label="t('incidentTypes.plaquePays')"
      :fetch-fn="fetchFilteredNationalities"
      :hint="t('incidentTypes.hintPlaquePays')"
      :preload="true"
      :min-search-length="0"
      class="mb-4"
    />

    <RipolAutocomplete
      v-if="plaquePays && isSwissPlate && !plaqueInconnu"
      v-model="plaqueCanton"
      :label="t('incidentTypes.plaqueCanton')"
      :fetch-fn="RipolService.searchCantons"
      :hint="t('incidentTypes.hintPlaqueCanton')"
      :preload="true"
      :min-search-length="0"
      class="mb-8"
    />

    <v-text-field
      :label="isPlaqueObligatoire ? requiredLabel(t('incidentTypes.plaqueNumero')) : t('incidentTypes.plaqueNumero')"
      v-model="plaqueNumero"
      :disabled="plaqueInconnu"
      class="mb-2"
      variant="outlined"
      :hint="isPlaqueObligatoire ? t('incidentTypes.hintPlaqueNumeroObligatoire') : t('incidentTypes.hintPlaqueNumero')"
      persistent-hint
    />
    <v-checkbox
      v-if="!isPlaqueObligatoire"
      v-model="plaqueInconnu"
      :label="t('incidentTypes.plaqueInconnu')"
      class="mb-4"
      hide-details
    />
  </template>
</template>

<script setup lang="ts">
import type { PropType } from "vue";
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import RipolAutocomplete from "@/components/ripol/RipolAutocomplete.vue";
import { useVehicleDetailsRipol } from "@/composables/useVehicleDetailsRipol";
import AccessibleVSelect from "@/components/accessibility/AccessibleVSelect.vue";
import { CATEGORIES_OBJETS } from "@/constants/constant";
import { applyDateMask } from "@/utils/helpers/dateHelpers.ts";
import { filterNationalities } from "@/utils/helpers/ripolHelpers.ts";
import { requiredLabel } from "@/utils/helpers/labelHelpers";

const { t } = useI18n();

type SubCatOption = {
  value: string;
  label: string;
  prefixes?: readonly string[];
};

const props = defineProps({
  sousCategorie: { type: String, required: true },
  sousCategorieError: { type: String },
  categorieObjet: { type: String, default: "vehicule" },
  showCategorieSelect: { type: Boolean, default: false },
  subCategorieOptions: {
    type: Array as PropType<SubCatOption[]>,
    required: false,
    default: null,
  },
  activePrefixes: {
    type: Array as PropType<readonly string[]>,
    required: false,
    default: null,
  },
});

const emit = defineEmits<{ "update:sousCategorie" : [value: string] }>();

const sousCategorie = computed({
  get: () => props.sousCategorie,
  set: (v: string) => emit("update:sousCategorie", v),
});

const categorieObjetOptions = [{ value: "vehicule", label: t("categoriesObjets.vehicule") }];

const selectedCategorie = computed(() => CATEGORIES_OBJETS.find(cat => cat.value === props.categorieObjet));

const computedSubCategorieOptions = computed<SubCatOption[]>(() => {
  if (props.subCategorieOptions && props.subCategorieOptions.length > 0) {
    return props.subCategorieOptions;
  }
  const cat = selectedCategorie.value;
  if (!cat?.subCategories) {
    return [];
  }
  return cat.subCategories.map(sub => ({
    value: sub.value,
    label: t(sub.labelKey),
    prefixes: sub.prefixes,
  }));
});

const computedActivePrefixes = computed<readonly string[]>(() => {
  if (props.activePrefixes && props.activePrefixes.length > 0) {
    return props.activePrefixes;
  }
  const cat = selectedCategorie.value;
  const selectedSub = cat?.subCategories?.find(sub => sub.value === sousCategorie.value);
  return selectedSub?.prefixes || cat?.prefixes || [];
});

const onDateAchatInput = (e: InputEvent) => {
  applyDateMask(e, dateAchat);
}

const fetchFilteredNationalities = async (search?: string) => {
  const data = await RipolService.searchNationalities(search);
  return filterNationalities(data);
};

const {
  typeObjet,
  typeObjetError,
  fabricant,
  fabricantError,
  fabricantAutre,
  modele,
  modeleError,
  modeleAutre,
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

  objetTypeKey,
  brandKey,
  modelKey,
  colourKey,
  hasBrands,
  hasModels,
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
} = useVehicleDetailsRipol({ sousCategorie, activePrefixes: computedActivePrefixes });
</script>
