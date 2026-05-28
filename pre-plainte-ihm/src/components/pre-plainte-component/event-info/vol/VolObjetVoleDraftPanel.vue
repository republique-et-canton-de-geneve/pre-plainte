<template>
  <div>
    <h3 class="text-h5 text-md-h5 mb-4">{{ t("incidentTypes.titreBlocAjoutObjetVole") }}</h3>
    <v-sheet class="pa-4 mb-4 objet-vole-brouillon">
      <p class="text-subtitle-1 font-weight-medium mb-4">
        {{ t("incidentTypes.objetVoleNumero", { n: (brouillon.objetsVolesValides?.length ?? 0) + 1 }) }}
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
          v-model="brouillon.plaqueNumero"
          :label="requiredLabel(t('incidentTypes.plaqueNumero'))"
          :error-messages="brouillon.plaqueNumeroError"
          :hint="t('incidentTypes.hintPlaqueNumero')"
          variant="outlined"
          persistent-hint
          class="my-4"
        />
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
          :hint="t('incidentTypes.hintModeleAutre')"
          variant="outlined"
          persistent-hint
          class="my-4"
        />
        <RipolAutocomplete
          v-model="brouillon.couleur"
          :key="brouillon.colourKey"
          :label="t('incidentTypes.couleur')"
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
        :hint="t('incidentTypes.hintGravure')"
        variant="outlined"
        persistent-hint
        class="my-4"
      />
      <v-text-field
        v-if="brouillon.categorieObjet && brouillon.categorieObjet !== VOL_OBJET_CATEGORIE.PLAQUE"
        :label="t('incidentTypes.valeurReelle')"
        v-model="brouillon.valeurReelle"
        type="number"
        class="my-4"
        :error-messages="brouillon.valeurReelleError"
        variant="outlined"
        :hint="t('incidentTypes.hintValeurReelle')"
        persistent-hint
      />

      <template
        v-if="
          brouillon.categorieObjet &&
          brouillon.categorieObjet !== VOL_OBJET_CATEGORIE.VEHICULE &&
          brouillon.categorieObjet !== VOL_OBJET_CATEGORIE.PLAQUE
        "
      >
        <v-text-field
          :label="brouillon.numeroSerieInconnu ? t('incidentTypes.numeroSerie') : requiredLabel(t('incidentTypes.numeroSerie'))"
          v-model="brouillon.numeroSerie"
          :disabled="brouillon.numeroSerieInconnu"
          class="my-4"
          :error-messages="brouillon.numeroSerieError"
          variant="outlined"
          :hint="t('incidentTypes.hintNumeroSerie')"
          persistent-hint
        />
        <v-checkbox
          v-model="brouillon.numeroSerieInconnu"
          :label="t('incidentTypes.numeroSerieInconnu')"
          class="my-4"
          hide-details
        />

        <template v-if="brouillon.hasImei">
          <v-text-field
            :label="brouillon.numeroIMEIInconnu ? t('incidentTypes.numeroImei') : requiredLabel(t('incidentTypes.numeroImei'))"
            v-model="brouillon.numeroIMEI"
            :disabled="brouillon.numeroIMEIInconnu"
            :error-messages="brouillon.numeroIMEIError"
            class="my-4"
            variant="outlined"
            :hint="t('incidentTypes.hintNumeroImei')"
            persistent-hint
            maxlength="15"
            inputmode="numeric"
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
            v-model="brouillon.numeroIMEIInconnu"
            :label="t('incidentTypes.numeroIMEIInconnu')"
            class="my-4"
            hide-details
          />
          <template v-if="brouillon.numeroIMEIInconnu">
            <v-alert type="info" class="my-4" density="comfortable" :icon="mobile ? false : undefined">
              <div class="text-body-2 text-md-body-1">
                {{ t("incidentTypes.warningAbsenceImei") }}
              </div>
            </v-alert>
            <v-textarea
              v-model="brouillon.justificationAbsenceIMEI"
              :label="t('incidentTypes.justificationAbsenceIMEI')"
              :hint="t('incidentTypes.hintJustificationAbsenceIMEI')"
              variant="outlined"
              persistent-hint
              class="my-4"
              rows="2"
            />
          </template>
        </template>
      </template>

      <v-textarea
        v-if="brouillon.categorieObjet && brouillon.categorieObjet !== VOL_OBJET_CATEGORIE.PLAQUE"
        v-model="brouillon.descriptionObjet"
        clearable
        :label="t('incidentTypes.descriptionComplementaireObjet')"
        :placeholder="t('incidentTypes.descriptionObjetPlaceholder')"
        :error-messages="brouillon.descriptionObjetError"
        class="my-4"
        variant="outlined"
        :hint="t('incidentTypes.hintDescriptionObjet')"
        persistent-hint
        rows="3"
      />

      <div v-if="brouillon.categorieObjet" class="d-flex justify-start mt-2">
        <v-btn color="primary" variant="outlined" @click="brouillon.validerObjetVole">
          {{ t("incidentTypes.validerObjetVole") }}
        </v-btn>
      </div>
    </v-sheet>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import { useDisplay } from "vuetify/framework";
import { VOL_OBJET_CATEGORIE } from "@/constants/constant";
import AccessibleVSelect from "@/components/accessibility/AccessibleVSelect.vue";
import RipolAutocomplete from "@/components/ripol/RipolAutocomplete.vue";
import VehiculeDetailsField from "@/components/pre-plainte-component/event-info/VehiculeDetailsField.vue";
import type { VolObjetVoleDraftBrouillon } from "@/types/volObjetVoleBrouillon.types";
import { requiredLabel } from "@/utils/helpers/labelHelpers";

defineProps<{
  brouillon: VolObjetVoleDraftBrouillon;
  activePrefixes: readonly string[];
}>();

const { t } = useI18n();
const { mobile } = useDisplay();
</script>

<style scoped>
.white-space {
  max-width: 300px;
  white-space: normal;
}
</style>
