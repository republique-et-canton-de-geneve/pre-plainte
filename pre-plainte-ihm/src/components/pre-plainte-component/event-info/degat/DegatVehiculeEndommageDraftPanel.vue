<template>
  <div>
    <h3 class="text-h5 text-md-h5 mb-4">{{ t("dommages.titreBlocAjoutVehiculeEndommage") }}</h3>
    <v-sheet class="pa-4 mb-4 objet-degrade-brouillon">
      <p class="text-subtitle-1 font-weight-medium mb-4">
        {{ t("dommages.vehiculeEndommageNumero", { n: objetsCount + 1 }) }}
      </p>

      <VehiculeDetailsField
        :sous-categorie="sousCategorie"
        @update:sous-categorie="emit('update:sousCategorie', $event)"
        :sous-categorie-error="sousCategorieError"
        :categorie-objet="categorieObjet"
        :show-categorie-select="true"
        :sub-categorie-options="subCategorieOptions"
        :active-prefixes="activePrefixes"
      />

      <v-text-field
        :label="t('incidentTypes.valeurReelle')"
        :model-value="valeurReelle"
        @update:model-value="emit('update:valeurReelle', $event)"
        type="number"
        class="my-4"
        :error-messages="valeurReelleError"
        variant="outlined"
        :hint="t('incidentTypes.hintValeurReelle')"
        persistent-hint
      />

      <v-textarea
        clearable
        :label="requiredLabel(t('incidentTypes.descriptionObjet'))"
        :placeholder="t('incidentTypes.descriptionObjetPlaceholder')"
        :model-value="descriptionObjet"
        @update:model-value="emit('update:descriptionObjet', $event)"
        :error-messages="descriptionObjetError"
        class="my-4"
        variant="outlined"
        :hint="t('incidentTypes.hintDescriptionObjet')"
        persistent-hint
      />

      <div class="d-flex justify-start mt-2">
        <v-btn color="primary" variant="outlined" @click="onValidate">
          {{ t("dommages.validerVehiculeEndommage") }}
        </v-btn>
      </div>
    </v-sheet>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import VehiculeDetailsField from "@/components/pre-plainte-component/event-info/VehiculeDetailsField.vue";
import { requiredLabel } from "@/utils/helpers/labelHelpers";

defineProps<{
  objetsCount: number;
  sousCategorie: string;
  sousCategorieError?: string;
  categorieObjet: string;
  subCategorieOptions: Array<{ value: string; label: string; prefixes: readonly string[] }>;
  activePrefixes: readonly string[];
  valeurReelle: string;
  valeurReelleError?: string;
  descriptionObjet: string;
  descriptionObjetError?: string;
  onValidate: () => void;
}>();

const emit = defineEmits<{
  (e: "update:sousCategorie", value: string): void;
  (e: "update:valeurReelle", value: any): void;
  (e: "update:descriptionObjet", value: any): void;
}>();

const { t } = useI18n();
</script>
