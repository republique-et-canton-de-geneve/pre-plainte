<template>
  <div>
    <h3 v-if="objetIndex === undefined" class="text-h5 text-md-h5 mb-4">
      {{ t("incidentTypes.titreBlocAjoutObjetVole") }}
    </h3>
    <v-sheet class="pa-4 mb-4 objet-vole-brouillon">
      <p class="text-subtitle-1 font-weight-medium mb-4">
        {{ t("incidentTypes.objetVoleNumero", { n: numeroObjet }) }}
      </p>

      <AccessibleVSelect
        v-model="brouillon.categorieObjet"
        :label="t('categoriesObjets.titre')"
        required
        :items="brouillon.categorieOptions"
        item-title="label"
        item-value="value"
        class="my-4"
        variant="outlined"
        :error-messages="brouillon.categorieObjetError"
        :hint="t('categoriesObjets.hint')"
        persistent-hint
      />

      <VehiculeDetailsField
        v-if="brouillon.categorieObjet === VOL_OBJET_CATEGORIE.VEHICULE"
        type-incident="vol"
        v-model:sous-categorie="brouillon.sousCategorie"
        :sous-categorie-error="brouillon.sousCategorieError"
        :categorie-objet="VOL_OBJET_CATEGORIE.VEHICULE"
        :show-categorie-select="false"
        :sub-categorie-options="brouillon.subCategorieOptions"
        :active-prefixes="activePrefixes"
      />

      <template v-if="brouillon.categorieObjet === VOL_OBJET_CATEGORIE.PLAQUE">
        <RipolAutocomplete
          v-model="brouillon.plaquePays"
          :label="t('incidentTypes.plaquePays')"
          required
          :fetch-fn="brouillon.fetchFilteredNationalities"
          :error-messages="brouillon.plaquePaysError"
          :hint="t('incidentTypes.hintPlaquePays')"
          :preload="true"
          :min-search-length="0"
          class="my-4"
        />
        <v-text-field
          :model-value="brouillon.plaqueNumero"
          @update:model-value="onPlaqueInput"
          :label="requiredLabel(t('incidentTypes.plaqueNumero'))"
          :error-messages="brouillon.plaqueNumeroError"
          variant="outlined"
          class="my-4"
        >
          <template #append-inner>
            <v-tooltip location="top">
              <template #activator="{ props }">
                <v-icon v-bind="props" color="primary" size="small"> mdi-information-outline </v-icon>
              </template>
              <div class="white-space">
                {{ t("incidentTypes.plaqueNumeroTooltip") }}
              </div>
            </v-tooltip>
          </template>
        </v-text-field>
      </template>

      <template
        v-if="
          brouillon.categorieObjet &&
          brouillon.categorieObjet !== VOL_OBJET_CATEGORIE.VEHICULE &&
          brouillon.categorieObjet !== VOL_OBJET_CATEGORIE.PLAQUE
        "
      >
        <AccessibleVSelect
          v-if="
            brouillon.subCategorieOptions.length > 0 &&
            brouillon.categorieObjet !== VOL_OBJET_CATEGORIE.TELEPHONE
          "
          v-model="brouillon.sousCategorie"
          :label="t('sousCategories.titre')"
          required
          :items="brouillon.subCategorieOptions"
          item-title="label"
          item-value="value"
          class="my-4"
          variant="outlined"
          :error-messages="brouillon.sousCategorieError"
          :hint="t('sousCategories.hint')"
          persistent-hint
          clearable
        />
        <RipolAutocomplete
          v-model="brouillon.typeObjet"
          :key="brouillon.objetTypeKey"
          :label="t('incidentTypes.typeObjet')"
          required
          :fetch-fn="brouillon.fetchFilteredObjectTypes"
          :error-messages="brouillon.typeObjetError"
          :hint="t('incidentTypes.hintTypeObjet')"
          :preload="true"
          :min-search-length="0"
          :auto-select-when-single-result="true"
          class="my-4"
        />
        <RipolAutocomplete
          v-if="brouillon.typeObjet && brouillon.hasBrands"
          v-model="brouillon.fabricant"
          :key="brouillon.brandKey"
          :label="t('incidentTypes.fabricant')"
          :fetch-fn="brouillon.fetchBrandsWithAutre"
          :error-messages="brouillon.fabricantError"
          :hint="t('incidentTypes.hintFabricant')"
          :preload="true"
          :min-search-length="0"
          class="my-4"
        />
        <v-text-field
          v-if="brouillon.isAutreFabricant"
          v-model="brouillon.fabricantAutre"
          :label="t('incidentTypes.fabricantAutre')"
          :error-messages="brouillon.fabricantAutreError"
          :hint="t('incidentTypes.hintFabricantAutre')"
          variant="outlined"
          persistent-hint
          class="my-4"
        />
        <RipolAutocomplete
          v-if="brouillon.fabricant"
          v-model="brouillon.modele"
          :key="brouillon.modelKey"
          :label="t('incidentTypes.modele')"
          :fetch-fn="brouillon.fetchModelsWithAutre"
          :error-messages="brouillon.modeleError"
          :hint="t('incidentTypes.hintModele')"
          :preload="true"
          :min-search-length="0"
          class="my-4"
          :disabled="!brouillon.hasModels && !brouillon.isAutreFabricant"
          :loading="brouillon.modelsLoading"
        />

        <v-text-field
          v-if="brouillon.isAutreModele"
          v-model="brouillon.modeleAutre"
          :label="t('incidentTypes.modeleAutre')"
          :error-messages="brouillon.modeleAutreError"
          :hint="t('incidentTypes.hintModeleAutre')"
          variant="outlined"
          persistent-hint
          class="my-4"
        />
        <RipolAutocomplete
          v-model="brouillon.couleur"
          :key="brouillon.colourKey"
          :label="requiredLabel(t('incidentTypes.couleur'))"
          :fetch-fn="brouillon.fetchColours"
          :error-messages="brouillon.couleurError"
          :hint="t('incidentTypes.hintCouleur')"
          :preload="true"
          :min-search-length="0"
          class="my-4"
        />
        <RipolAutocomplete
          v-model="brouillon.couleurSecondaire"
          :key="`${brouillon.colourKey}-2`"
          :label="t('incidentTypes.couleurSecondaire')"
          :fetch-fn="brouillon.fetchColours"
          :hint="t('incidentTypes.hintCouleurSecondaire')"
          :preload="true"
          :min-search-length="0"
          class="my-4"
        />
      </template>

      <v-text-field
        v-if="brouillon.categorieObjet && brouillon.isBijouxCategory"
        v-model="brouillon.gravure"
        :label="t('incidentTypes.gravure')"
        :error-messages="brouillon.gravureError"
        :hint="t('incidentTypes.hintGravure')"
        variant="outlined"
        persistent-hint
        class="my-4"
      />
      <v-text-field
        v-if="brouillon.categorieObjet && brouillon.categorieObjet !== VOL_OBJET_CATEGORIE.PLAQUE"
        :label="t('incidentTypes.valeurReelle')"
        :model-value="brouillon.valeurReelle"
        type="number"
        min="0"
        inputmode="decimal"
        class="my-4"
        :error-messages="brouillon.valeurReelleError"
        variant="outlined"
        :hint="t('incidentTypes.hintValeurReelle')"
        persistent-hint
        @keydown="bloquerSaisieValeurNegative"
        @update:model-value="onValeurReelleInput"
      />

      <template
        v-if="
          brouillon.categorieObjet &&
          brouillon.categorieObjet !== VOL_OBJET_CATEGORIE.VEHICULE &&
          brouillon.categorieObjet !== VOL_OBJET_CATEGORIE.PLAQUE
        "
      >
        <v-text-field
          :key="`numero-serie-${brouillon.numeroSerieInconnu ? 'inconnu' : 'connu'}`"
          :label="(!brouillon.numeroSerieRequis || brouillon.numeroSerieInconnu) ? t('incidentTypes.numeroSerie') : requiredLabel(t('incidentTypes.numeroSerie'))"
          :model-value="brouillon.numeroSerie"
          :disabled="brouillon.numeroSerieRequis && brouillon.numeroSerieInconnu"
          class="my-4"
          :error-messages="brouillon.numeroSerieError"
          variant="outlined"
          :hint="t('incidentTypes.hintNumeroSerie')"
          persistent-hint
          @update:model-value="onNumeroSerieInput"
        />
        <v-checkbox
          v-if="brouillon.numeroSerieRequis"
          :model-value="brouillon.numeroSerieInconnu"
          :label="t('incidentTypes.numeroSerieInconnu')"
          class="my-4"
          hide-details
          @update:model-value="onNumeroSerieInconnuChange"
        />

        <template v-if="brouillon.hasImei">
          <v-text-field
            :key="`numero-imei-${brouillon.numeroIMEIInconnu ? 'inconnu' : 'connu'}`"
            :label="brouillon.numeroIMEIInconnu ? t('incidentTypes.numeroImei') : requiredLabel(t('incidentTypes.numeroImei'))"
            :model-value="brouillon.numeroIMEI"
            :disabled="brouillon.numeroIMEIInconnu"
            :error-messages="brouillon.numeroIMEIError"
            class="my-4"
            variant="outlined"
            :maxlength="NUMERO_IMEI_MAX_LENGTH"
            inputmode="numeric"
            @update:model-value="onNumeroIMEIInput"
          >
            <template #append-inner>
              <v-tooltip location="top">
                <template #activator="{ props }">
                  <v-icon v-bind="props" color="primary" size="small"> mdi-information-outline </v-icon>
                </template>
                <div class="white-space">
                  {{ t("incidentTypes.numeroImeiTooltip") }}
                </div>
              </v-tooltip>
            </template>
          </v-text-field>
          <v-checkbox
            :model-value="brouillon.numeroIMEIInconnu"
            :label="t('incidentTypes.numeroIMEIInconnu')"
            class="my-4"
            hide-details
            @update:model-value="onNumeroIMEIInconnuChange"
          />
          <template v-if="brouillon.numeroIMEIInconnu">
            <v-alert type="info" class="my-4" density="comfortable" :icon="mobile ? false : undefined">
              <div class="text-body-2 text-md-body-1">
                {{ t("incidentTypes.warningAbsenceImei") }}
              </div>
            </v-alert>
            <v-textarea
              v-model="brouillon.justificationAbsenceIMEI"
              :label="requiredLabel(t('incidentTypes.justificationAbsenceIMEI'))"
              :hint="t('incidentTypes.hintJustificationAbsenceIMEI')"
              :error-messages="brouillon.justificationAbsenceIMEIError"
              variant="outlined"
              persistent-hint
              class="my-4"
              rows="2"
            />
          </template>
        </template>
      </template>

      <div v-if="brouillon.categorieObjet" class="d-flex justify-start mt-2">
        <v-btn color="primary" variant="outlined" data-cy="objet-vole-valider" @click="brouillon.validerObjetVole">
          {{ t("incidentTypes.validerObjetVole") }}
        </v-btn>
      </div>
    </v-sheet>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { useDisplay } from "vuetify/framework";
import { NUMERO_IMEI_MAX_LENGTH, VOL_OBJET_CATEGORIE } from "@/constants/constant";
import AccessibleVSelect from "@/components/accessibility/AccessibleVSelect.vue";
import RipolAutocomplete from "@/components/ripol/RipolAutocomplete.vue";
import VehiculeDetailsField from "@/components/pre-plainte-component/event-info/VehiculeDetailsField.vue";
import type { VolObjetVoleDraftBrouillon } from "@/types/volObjetVoleBrouillon.types";
import { requiredLabel } from "@/utils/helpers/labelHelpers";
import { formatLicensePlate } from "@/composables/useLicencePlate.ts";

const props = defineProps<{
  brouillon: VolObjetVoleDraftBrouillon;
  activePrefixes: readonly string[];
  objetIndex?: number;
}>();

const { t } = useI18n();
const { mobile } = useDisplay();
const numeroObjet = computed(() =>
  props.objetIndex === undefined
    ? (props.brouillon.objetsVolesValides?.length ?? 0) + 1
    : props.objetIndex + 1,
);

defineEmits<{
  "update:brouillon": [value: VolObjetVoleDraftBrouillon];
}>();

const onPlaqueInput = (value: string) => {
  props.brouillon.plaqueNumero = formatLicensePlate(
    value,
    props.brouillon.plaquePays?.code,
  );
};

const bloquerSaisieValeurNegative = (event: KeyboardEvent) => {
  if (event.key === "-" || event.key === "e" || event.key === "E" || event.key === "+") {
    event.preventDefault();
  }
};

const onValeurReelleInput = (value: string | number | null) => {
  const raw = value == null ? "" : String(value);
  if (!raw.trim()) {
    props.brouillon.valeurReelle = "";
    return;
  }
  const parsed = Number(raw);
  props.brouillon.valeurReelle = Number.isFinite(parsed) && parsed < 0 ? "" : raw;
};

const onNumeroSerieInput = (value: string | null) => {
  props.brouillon.numeroSerie = value ?? "";
};

const onNumeroSerieInconnuChange = (checked: boolean | null) => {
  const isChecked = !!checked;
  props.brouillon.numeroSerieInconnu = isChecked;
  if (isChecked) {
    props.brouillon.numeroSerie = "";
  }
};

const onNumeroIMEIInput = (value: string | null) => {
  props.brouillon.numeroIMEI = value ?? "";
};

const onNumeroIMEIInconnuChange = (checked: boolean | null) => {
  const isChecked = !!checked;
  props.brouillon.numeroIMEIInconnu = isChecked;
  if (isChecked) {
    props.brouillon.numeroIMEI = "";
    return;
  }
  props.brouillon.justificationAbsenceIMEI = "";
};
</script>

<style scoped>
.white-space {
  max-width: 300px;
  white-space: normal;
}
</style>
