<template>
  <v-col cols="12" class="pa-0">
    <dl class="w-100">
      <v-row class="ma-0">
        <v-col cols="12" md="4">
          <dt id="lbl-datePremierContact">
            <v-label class="ge-field-label">{{ t("cybercrime.datePremierContact") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-datePremierContact">
            {{ formatDateTimeFrench(toIsoDate(data.datePremierContact)) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-dateDernierContact">
            <v-label class="ge-field-label">{{ t("cybercrime.dateDernierContact") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-dateDernierContact">
            {{ formatDateTimeFrench(toIsoDate(data.dateDernierContact)) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-urlComplete">
            <v-label class="ge-field-label">{{ t("cybercrime.urlComplete") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-urlComplete">
            {{ displayValue(data.urlComplete) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-fa-titreAnnonce">
            <v-label class="ge-field-label">{{ t("cybercrime.titreAnnonce") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-fa-titreAnnonce">
            {{ displayValue(data.titreAnnonce) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-fa-nomBailleur">
            <v-label class="ge-field-label">{{ t("cybercrime.nomBailleur") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-fa-nomBailleur">
            {{ displayValue(data.nomBailleur) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-cf-emailBailleurInconnu">
            <v-label class="ge-field-label">{{ t("cybercrime.emailInconnu") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-cf-emailBailleurInconnu">
            {{ formatOuiNon(data.emailBailleurInconnu, t) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-fa-emailBailleur">
            <v-label class="ge-field-label">{{ t("cybercrime.emailBailleur") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-fa-emailBailleur">
            {{ displayValue(data.emailBailleur) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-cf-telephoneBailleurInconnu">
            <v-label class="ge-field-label">{{ t("cybercrime.telephoneInconnu") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-cf-telephoneBailleurInconnu">
            {{ formatOuiNon(data.telephoneBailleurInconnu, t) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-fa-telephoneBailleur">
            <v-label class="ge-field-label">{{ t("cybercrime.telephoneBailleur") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-fa-telephoneBailleur">
            {{ displayValue(data.telephoneBailleur) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-fa-adresseBienImmobilier">
            <v-label class="ge-field-label">{{ t("cybercrime.adresseBienImmobilier") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-fa-adresseBienImmobilier">
            {{ displayValue(data.adresseBienImmobilier) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-fa-montantDemande">
            <v-label class="ge-field-label">{{ t("cybercrime.montantDemande") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-fa-montantDemande">
            {{ displayValue(data.montantDemande) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-fa-modePaiementDemande">
            <v-label class="ge-field-label">{{ t("cybercrime.modePaiementDemande") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-fa-modePaiementDemande">
            {{ displayValue(data.modePaiementDemande) }}
          </dd>
        </v-col>
      </v-row>
    </dl>
  </v-col>
</template>

<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import { displayValue, formatOuiNon } from "@/utils/helpers/formHelpers.ts";
import { formatDateTimeFrench, toIsoDate } from "@/utils/helpers/dateHelpers.ts";
const props = defineProps<{
  data: any;
}>();
const { t } = useI18n();
</script>
