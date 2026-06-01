<template>
  <div>
    <v-sheet class="pa-4 mb-4 objet-vole-resume">
      <div class="d-flex flex-wrap align-center justify-space-between gap-2 mb-3">
        <span class="text-subtitle-1 font-weight-medium">
          {{ t("incidentTypes.objetVoleNumero", { n: index + 1 }) }}
        </span>
        <div class="d-flex gap-2">
          <v-btn variant="text" color="primary" size="small" @click="emit('modifier', index)">
            {{ t("common.modifier") }}
          </v-btn>
          <v-btn variant="text" color="error" size="small" @click="emit('supprimer', index)">
            {{ t("incidentTypes.supprimerObjetVole") }}
          </v-btn>
        </div>
      </div>
      <v-row dense>
        <v-col cols="12" md="6">
          <div class="text-caption text-medium-emphasis">{{ t("categoriesObjets.titre") }}</div>
          <div class="text-body-2">{{ libelleCategorieVolObjet(obj.categorieObjet, t) }}</div>
        </v-col>
        <v-col v-if="obj.sousCategorie" cols="12" md="6">
          <div class="text-caption text-medium-emphasis">{{ t("sousCategories.titre") }}</div>
          <div class="text-body-2">
            {{ libelleSousCategorieVolObjet(obj.sousCategorie, obj.categorieObjet, t) }}
          </div>
        </v-col>
        <v-col v-if="obj.typeObjet" cols="12" md="6">
          <div class="text-caption text-medium-emphasis">{{ t("incidentTypes.typeObjet") }}</div>
          <div class="text-body-2">{{ obj.typeObjet.label }}</div>
        </v-col>
        <v-col v-if="obj.fabricant" cols="12" md="6">
          <div class="text-caption text-medium-emphasis">{{ t("incidentTypes.fabricant") }}</div>
          <div class="text-body-2">{{ libelleFabricantResumeVolObjet(obj) }}</div>
        </v-col>
        <v-col v-if="obj.modele" cols="12" md="6">
          <div class="text-caption text-medium-emphasis">{{ t("incidentTypes.modele") }}</div>
          <div class="text-body-2">{{ libelleModeleResumeVolObjet(obj) }}</div>
        </v-col>
        <v-col v-if="obj.couleur" cols="12" md="6">
          <div class="text-caption text-medium-emphasis">{{ t("incidentTypes.couleur") }}</div>
          <div class="text-body-2">{{ obj.couleur.label }}</div>
        </v-col>
        <v-col v-if="obj.couleurSecondaire" cols="12" md="6">
          <div class="text-caption text-medium-emphasis">{{ t("incidentTypes.couleurSecondaire") }}</div>
          <div class="text-body-2">{{ obj.couleurSecondaire.label }}</div>
        </v-col>
        <v-col v-if="obj.valeurReelle" cols="12" md="6">
          <div class="text-caption text-medium-emphasis">{{ t("incidentTypes.valeurReelle") }}</div>
          <div class="text-body-2">{{ obj.valeurReelle }} CHF</div>
        </v-col>
        <template v-if="obj.categorieObjet === VOL_OBJET_CATEGORIE.VEHICULE">
          <v-col v-if="obj.sousCategorie === 'velos' && (obj.numeroCadre || obj.numeroCadreInconnu)" cols="12" md="6">
            <div class="text-caption text-medium-emphasis">{{ t("incidentTypes.numeroCadre") }}</div>
            <div class="text-body-2">
              {{ obj.numeroCadreInconnu ? t("incidentTypes.numeroCadreInconnu") : obj.numeroCadre }}
            </div>
          </v-col>
          <template v-if="obj.sousCategorie && VEHICULE_CATEGORIES_AVEC_VIN.includes(obj.sousCategorie)">
            <v-col cols="12" md="6">
              <div class="text-caption text-medium-emphasis">{{ t("incidentTypes.vin") }}</div>
              <div class="text-body-2">
                {{ obj.vinInconnu ? t("incidentTypes.vinInconnu") : obj.vin || libelleResumeChampAbsent }}
              </div>
            </v-col>
          </template>
          <template v-if="obj.sousCategorie && VEHICULE_CATEGORIES_AVEC_PLAQUE.includes(obj.sousCategorie)">
            <v-col v-if="obj.plaqueNumero && !obj.plaqueInconnu" cols="12" md="6">
              <div class="text-caption text-medium-emphasis">{{ t("incidentTypes.plaqueNumero") }}</div>
              <div class="text-body-2">{{ obj.plaqueNumero }}</div>
            </v-col>
            <v-col v-if="obj.plaqueInconnu" cols="12" md="6">
              <div class="text-caption text-medium-emphasis">{{ t("incidentTypes.plaqueInconnu") }}</div>
              <div class="text-body-2">{{ t("common.oui") }}</div>
            </v-col>
            <v-col v-if="obj.plaquePays && !obj.plaqueInconnu" cols="12" md="6">
              <div class="text-caption text-medium-emphasis">{{ t("incidentTypes.plaquePays") }}</div>
              <div class="text-body-2">{{ obj.plaquePays.label }}</div>
            </v-col>
            <v-col
              v-if="obj.plaqueCanton && !obj.plaqueInconnu && obj.plaquePays?.code === RIPOL.PAYS_SUISSE"
              cols="12"
              md="6"
            >
              <div class="text-caption text-medium-emphasis">{{ t("incidentTypes.plaqueCanton") }}</div>
              <div class="text-body-2">{{ obj.plaqueCanton.label }}</div>
            </v-col>
          </template>
          <v-col v-if="obj.dateAchat" cols="12" md="6">
            <div class="text-caption text-medium-emphasis">{{ t("incidentTypes.dateAchat") }}</div>
            <div class="text-body-2">{{ obj.dateAchat }}</div>
          </v-col>
        </template>
        <template v-else-if="obj.categorieObjet === VOL_OBJET_CATEGORIE.PLAQUE">
          <v-col cols="12" md="6">
            <div class="text-caption text-medium-emphasis">{{ t("incidentTypes.plaquePays") }}</div>
            <div class="text-body-2">{{ obj.plaquePays?.label || libelleResumeChampAbsent }}</div>
          </v-col>
          <v-col cols="12" md="6">
            <div class="text-caption text-medium-emphasis">{{ t("incidentTypes.plaqueNumero") }}</div>
            <div class="text-body-2">{{ obj.plaqueNumero || libelleResumeChampAbsent }}</div>
          </v-col>
        </template>
        <v-col v-else cols="12" md="6">
          <div class="text-caption text-medium-emphasis">{{ t("incidentTypes.numeroSerie") }}</div>
          <div class="text-body-2">
            {{
              obj.numeroSerieInconnu
                ? t("incidentTypes.numeroSerieInconnu")
                : obj.numeroSerie || libelleResumeChampAbsent
            }}
          </div>
        </v-col>
        <template v-if="hasImeiPourSnapshotVol(obj)">
          <v-col cols="12" md="6">
            <div class="text-caption text-medium-emphasis">{{ t("incidentTypes.numeroImei") }}</div>
            <div class="text-body-2">
              {{
                obj.numeroIMEIInconnu
                  ? t("incidentTypes.numeroIMEIInconnu")
                  : obj.numeroIMEI || libelleResumeChampAbsent
              }}
            </div>
          </v-col>
          <v-col v-if="obj.numeroIMEIInconnu && obj.justificationAbsenceIMEI" cols="12" md="6">
            <div class="text-caption text-medium-emphasis">
              {{ t("incidentTypes.justificationAbsenceIMEI") }}
            </div>
            <div class="text-body-2">{{ obj.justificationAbsenceIMEI }}</div>
          </v-col>
        </template>
      </v-row>
    </v-sheet>
    <v-btn
      v-if="showAjouterAutreButton"
      variant="outlined"
      color="primary"
      class="mb-4"
      block
      @click="emit('ajouter-autre')"
    >
      {{ t("incidentTypes.ajouterAutreObjetVole") }}
    </v-btn>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from "vue-i18n";
import type { VolObjetFormSnapshot } from "@/types/pre-plainte.interface";
import { RIPOL, VOL_OBJET_CATEGORIE, VEHICULE_CATEGORIES_AVEC_PLAQUE, VEHICULE_CATEGORIES_AVEC_VIN } from "@/constants/constant";
import {
  hasImeiPourSnapshotVol,
  libelleCategorieVolObjet,
  libelleFabricantResumeVolObjet,
  libelleModeleResumeVolObjet,
  libelleSousCategorieVolObjet,
} from "@/utils/helpers/volObjetVolHelpers";

defineProps<{
  obj: VolObjetFormSnapshot;
  index: number;
  libelleResumeChampAbsent: string;
  showAjouterAutreButton: boolean;
}>();

const emit = defineEmits<{
  (e: "modifier", index: number): void;
  (e: "supprimer", index: number): void;
  (e: "ajouter-autre"): void;
}>();

const { t } = useI18n();
</script>
