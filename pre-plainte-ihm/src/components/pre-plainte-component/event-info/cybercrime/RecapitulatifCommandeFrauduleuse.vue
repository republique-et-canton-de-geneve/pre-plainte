<template>
  <v-col cols="12" class="pa-0">
    <dl class="w-100">
      <v-row class="ma-0">
        <v-col cols="12" md="4">
          <dt id="lbl-cf-prestataire">
            <v-label class="ge-field-label">{{ t("cybercrime.prestataire") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-cf-prestataire">
            {{ data.prestataire }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-cf-dateDecouverte">
            <v-label class="ge-field-label">{{ t("cybercrime.dateDecouverte") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-cf-dateDecouverte">
            {{ data.dateDecouverte }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-cf-montantDelit">
            <v-label class="ge-field-label">{{ t("cybercrime.montantDelit") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-cf-montantDelit">
            {{ data.montant }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-cf-assuranceDisponible">
            <v-label class="ge-field-label">{{ t("cybercrime.assuranceDisponible") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-cf-assuranceDisponible">
            {{ formatOuiNon(data.assurance, t) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-cf-emailCommandeInconnu">
            <v-label class="ge-field-label">{{ t("cybercrime.emailInconnu") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-cf-emailCommandeInconnu">
            {{ formatOuiNon(data.emailCommandeInconnu, t) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-cf-emailCommande">
            <v-label class="ge-field-label">{{ t("cybercrime.emailCommande") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-cf-emailCommande">
            {{ displayValue(data.emailCommande) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-cf-telephoneCommandeInconnu">
            <v-label class="ge-field-label">{{ t("cybercrime.telephoneInconnu") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-cf-telephoneCommandeInconnu">
            {{ formatOuiNon(data.telephoneCommandeInconnu, t) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-cf-telephoneCommande">
            <v-label class="ge-field-label">{{ t("cybercrime.telephoneCommande") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-cf-telephoneCommande">
            {{ displayValue(data.telephoneCommande) }}
          </dd>
        </v-col>


        <v-col cols="12" md="4">
          <dt id="lbl-cf-livraisonAdresseLesee">
            <v-label class="ge-field-label">{{ t("cybercrime.livraisonAdresseLesee") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-cf-livraisonAdresseLesee">
            {{ formatOuiNon(data.livraisonAdresseLesee, t) }}
          </dd>
        </v-col>

        <template v-if="!data.livraisonAdresseLesee">
          <v-col cols="12" md="4">
            <dt id="lbl-anr-livraisonAdresse">
              <v-label class="ge-field-label">{{ t("informationsPersonnelles.adresse") }}</v-label>
            </dt>
            <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-livraisonAdresse">
              {{ displayValue(data.livraisonAdresse) }}
            </dd>
          </v-col>

          <v-col cols="12" md="4">
            <dt id="lbl-anr-livraisonAdressePostale">
              <v-label class="ge-field-label">{{ t("informationsPersonnelles.adressePostale") }}</v-label>
            </dt>
            <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-livraisonAdressePostale">
              {{ displayValue(data.livraisonAdressePostale) }}
            </dd>
          </v-col>

          <v-col cols="12" md="4">
            <dt id="lbl-anr-livraisonNpa">
              <v-label class="ge-field-label">{{ t("informationsPersonnelles.npa") }}</v-label>
            </dt>
            <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-livraisonNpa">
              {{ displayValue(data.livraisonNpa) }}
            </dd>
          </v-col>

          <v-col cols="12" md="4">
            <dt id="lbl-anr-livraisonLocalite">
              <v-label class="ge-field-label">{{ t("informationsPersonnelles.localite") }}</v-label>
            </dt>
            <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-livraisonLocalite">
              {{ displayValue(data.livraisonLocalite) }}
            </dd>
          </v-col>

          <v-col cols="12" md="4">
            <dt id="lbl-anr-livraisonPays">
              <v-label class="ge-field-label">{{ t("informationsPersonnelles.pays") }}</v-label>
            </dt>
            <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-livraisonPays">
              {{ livraisonPaysLibelle }}
            </dd>
          </v-col>
        </template>

      </v-row>
    </dl>
  </v-col>
</template>

<script lang="ts" setup>
import { useI18n } from "vue-i18n";
import type { PrePlainteFormFields } from "@/types/pre-plainte.interface";
import { computed } from "vue";
import { CountryService } from "@/services/countryService.ts";
import { displayValue, formatOuiNon } from "@/utils/helpers/formHelpers.ts";
import { EMPTY_VALUE_DISPLAY } from "@/constants/constant.ts";

const props = defineProps<{
  data: Pick<
    PrePlainteFormFields,
    | "prestataire"
    | "dateDecouverte"
    | "montant"
    | "assurance"
    | "emailCommandeInconnu"
    | "emailCommande"
    | "telephoneCommande"
    | "telephoneCommandeInconnu"
    | "livraisonAdresseLesee"
    | "livraisonAdresse"
    | "livraisonAdressePostale"
    | "livraisonNpa"
    | "livraisonLocalite"
    | "livraisonPays"
  >;
}>();

const { t } = useI18n();

const livraisonPaysLibelle = computed(() => {
  const code = props.data?.livraisonPays as string | undefined;
  if (!code) {
    return EMPTY_VALUE_DISPLAY;
  }
  return CountryService.getCountryByCode(code)?.name || code;
});
</script>
