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
          <dt id="lbl-anr-montantDelitAchatLigne">
            <v-label class="ge-field-label">{{ t("cybercrime.montantDelitAchatLigne") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-montantDelitAchatLigne">
            {{ displayValue(data.montantDelitAchatLigne) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-anr-articleNonLivreDescription">
            <v-label class="ge-field-label">{{ t("cybercrime.articleNonLivreDescription") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-articleNonLivreDescription">
            {{ displayValue(data.articleNonLivreDescription) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-anr-prenomVendeur">
            <v-label class="ge-field-label">{{ t("cybercrime.prenomVendeur") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-prenomVendeur">
            {{ displayValue(data.prenomVendeur) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-anr-nomVendeur">
            <v-label class="ge-field-label">{{ t("cybercrime.nomVendeur") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-nomVendeur">
            {{ displayValue(data.nomVendeur) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-anr-telephoneVendeurInconnu">
            <v-label class="ge-field-label">{{ t("cybercrime.telephoneInconnu") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-telephoneVendeurInconnu">
            {{ formatOuiNon(data.telephoneVendeurInconnu, t) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-anr-telephoneVendeur">
            <v-label class="ge-field-label">{{ t("cybercrime.telephoneVendeur") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-telephoneVendeur">
            {{ displayValue(data.telephoneVendeur) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-anr-emailVendeurInconnu">
            <v-label class="ge-field-label">{{ t("cybercrime.emailInconnu") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-emailVendeurInconnu">
            {{ formatOuiNon(data.emailVendeurInconnu, t) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-anr-emailVendeur">
            <v-label class="ge-field-label">{{ t("cybercrime.emailVendeur") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-emailVendeur">
            {{ displayValue(data.emailVendeur) }}
          </dd>
        </v-col>

        <template v-if="data.adresseVendeurInconnue">
          <v-col cols="12">
            <dt id="lbl-anr-adresseVendeurInconnue">
              <v-label class="ge-field-label">{{ t("cybercrime.adresseVendeur") }}</v-label>
            </dt>
            <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-adresseVendeurInconnue">
              {{ t("cybercrime.recapAdresseVendeurInconnue") }}
            </dd>
          </v-col>
        </template>
        <template v-else>
          <v-col cols="12" md="4">
            <dt id="lbl-anr-vendeurAdresse">
              <v-label class="ge-field-label">{{ t("informationsPersonnelles.adresse") }}</v-label>
            </dt>
            <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-vendeurAdresse">
              {{ displayValue(data.vendeurAdresse) }}
            </dd>
          </v-col>

          <v-col cols="12" md="4">
            <dt id="lbl-anr-vendeurAdressePostale">
              <v-label class="ge-field-label">{{ t("informationsPersonnelles.adressePostale") }}</v-label>
            </dt>
            <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-vendeurAdressePostale">
              {{ displayValue(data.vendeurAdressePostale) }}
            </dd>
          </v-col>

          <v-col cols="12" md="4">
            <dt id="lbl-anr-vendeurNpa">
              <v-label class="ge-field-label">{{ t("informationsPersonnelles.npa") }}</v-label>
            </dt>
            <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-vendeurNpa">
              {{ displayValue(data.vendeurNpa) }}
            </dd>
          </v-col>

          <v-col cols="12" md="4">
            <dt id="lbl-anr-vendeurLocalite">
              <v-label class="ge-field-label">{{ t("informationsPersonnelles.localite") }}</v-label>
            </dt>
            <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-vendeurLocalite">
              {{ displayValue(data.vendeurLocalite) }}
            </dd>
          </v-col>

          <v-col cols="12" md="4">
            <dt id="lbl-anr-vendeurPays">
              <v-label class="ge-field-label">{{ t("informationsPersonnelles.pays") }}</v-label>
            </dt>
            <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-vendeurPays">
              {{ vendeurPaysLibelle }}
            </dd>
          </v-col>
        </template>

        <template v-if="data.achatViaPlaceMarche">
          <v-col cols="12" md="4">
            <dt id="lbl-anr-plateformeUtilisee">
              <v-label class="ge-field-label">{{ t("cybercrime.plateformeUtilisee") }}</v-label>
            </dt>
            <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-plateformeUtilisee">
              {{ displayValue(data.plateformeAutre || data.plateforme) }}
            </dd>
          </v-col>

          <v-col cols="12" md="4">
            <dt id="lbl-anr-plateformeId">
              <v-label class="ge-field-label">{{ t("cybercrime.plateformeId") }}</v-label>
            </dt>
            <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-plateformeId">
              {{ displayValue(data.plateformeId) }}
            </dd>
          </v-col>
        </template>

        <template v-else>
          <v-col cols="12" md="4">
            <dt id="lbl-anr-nomEntrepriseVendeur">
              <v-label class="ge-field-label">{{ t("cybercrime.nomEntrepriseVendeur") }}</v-label>
            </dt>
            <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-nomEntrepriseVendeur">
              {{ displayValue(data.nomEntrepriseVendeur) }}
            </dd>
          </v-col>

          <v-col cols="12" md="4">
            <dt id="lbl-anr-siteWebEntrepriseVendeur">
              <v-label class="ge-field-label">{{ t("cybercrime.siteWebEntrepriseVendeur") }}</v-label>
            </dt>
            <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-siteWebEntrepriseVendeur">
              {{ displayValue(data.siteWebEntrepriseVendeur) }}
            </dd>
          </v-col>
        </template>

        <template v-if="data.annonceDocumentIndisponible">
          <v-col cols="12" md="4">
            <dt id="lbl-anr-annonceDocumentIndisponible">
              <v-label class="ge-field-label">{{ t("cybercrime.documentNonDisponible") }}</v-label>
            </dt>
            <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-annonceDocumentIndisponible">
              {{ formatOuiNon(data.annonceDocumentIndisponible, t) }}
            </dd>
          </v-col>

          <v-col cols="12" md="4">
            <dt id="lbl-anr-raisonAbsenceAnnonce">
              <v-label class="ge-field-label">{{ t("cybercrime.raisonAbsenceAnnonce") }}</v-label>
            </dt>
            <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-raisonAbsenceAnnonce">
              {{ data.raisonAbsenceAnnonce }}
            </dd>
          </v-col>
        </template>

        <v-col cols="12" md="4">
          <dt id="lbl-anr-moyenPaiement">
            <v-label class="ge-field-label">{{ t("cybercrime.moyenPaiement") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-moyenPaiement">
            {{ displayValue(data.moyenPaiementAutre || data.moyenPaiement) }}
          </dd>
        </v-col>

        <v-col v-if="data.ibanBeneficiaire" cols="12" md="4">
          <dt id="lbl-anr-ibanBeneficiaire">
            <v-label class="ge-field-label">{{ t("cybercrime.ibanBeneficiaire") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-ibanBeneficiaire">
            {{ data.ibanBeneficiaire }}
          </dd>
        </v-col>

        <v-col v-if="data.comptePaypalBeneficiaire" cols="12" md="4">
          <dt id="lbl-anr-comptePaypalBeneficiaire">
            <v-label class="ge-field-label">{{ t("cybercrime.comptePaypalBeneficiaire") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-comptePaypalBeneficiaire">
            {{ data.comptePaypalBeneficiaire }}
          </dd>
        </v-col>

        <v-col v-if="data.numeroTwintBeneficiaire" cols="12" md="4">
          <dt id="lbl-anr-numeroTwintBeneficiaire">
            <v-label class="ge-field-label">{{ t("cybercrime.numeroTwintBeneficiaire") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-numeroTwintBeneficiaire">
            {{ data.numeroTwintBeneficiaire }}
          </dd>
        </v-col>

        <template v-if="data.adresseWalletCrypto">
          <v-col cols="12" md="4">
            <dt id="lbl-anr-adresseWalletCrypto">
              <v-label class="ge-field-label">{{ t("cybercrime.adresseWalletCrypto") }}</v-label>
            </dt>
            <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-adresseWalletCrypto">
              {{ data.adresseWalletCrypto }}
            </dd>
          </v-col>
          <v-col cols="12" md="4">
            <dt id="lbl-anr-hashTransactionCrypto">
              <v-label class="ge-field-label">{{ t("cybercrime.hashTransactionCrypto") }}</v-label>
            </dt>
            <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-hashTransactionCrypto">
              {{ data.hashTransactionCrypto }}
            </dd>
          </v-col>
        </template>

        <v-col cols="12" md="4">
          <dt id="lbl-anr-societeBeneficiaire">
            <v-label class="ge-field-label">{{ t("cybercrime.societeBeneficiaire") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-societeBeneficiaire">
            {{ displayValue(data.societeBeneficiaire) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-anr-nomBeneficiaire">
            <v-label class="ge-field-label">{{ t("cybercrime.nomBeneficiaire") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-nomBeneficiaire">
            {{ displayValue(data.nomBeneficiaire) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-anr-prenomBeneficiaire">
            <v-label class="ge-field-label">{{ t("cybercrime.prenomBeneficiaire") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-prenomBeneficiaire">
            {{ displayValue(data.prenomBeneficiaire) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-anr-dateOperation">
            <v-label class="ge-field-label">{{ t("cybercrime.dateOperation") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-dateOperation">
            {{ displayValue(data.dateOperation) }}
          </dd>
        </v-col>

        <template v-if="data.preuvePaiementIndisponible">
          <v-col cols="12" md="4">
            <dt id="lbl-anr-preuvePaiementIndisponible">
              <v-label class="ge-field-label">{{ t("cybercrime.documentNonDisponible") }}</v-label>
            </dt>
            <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-preuvePaiementIndisponible">
              {{ formatOuiNon(data.preuvePaiementIndisponible, t) }}
            </dd>
          </v-col>

          <v-col cols="12" md="4">
            <dt id="lbl-anr-raisonAbsencePreuvePaiement">
              <v-label class="ge-field-label">{{ t("cybercrime.raisonAbsencePreuvePaiement") }}</v-label>
            </dt>
            <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-raisonAbsencePreuvePaiement">
              {{ data.raisonAbsencePreuvePaiement }}
            </dd>
          </v-col>
        </template>

        <v-col cols="12" md="4">
          <dt id="lbl-anr-copieIdentiteTransmiseAuteur">
            <v-label class="ge-field-label">{{ t("cybercrime.copieIdentiteTransmiseAuteur") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-copieIdentiteTransmiseAuteur">
            {{ formatOuiNon(data.copieIdentiteTransmiseAuteur, t) }}
          </dd>
        </v-col>

        <v-col cols="12" md="4">
          <dt id="lbl-anr-copieIdentiteAuteurTransmise">
            <v-label class="ge-field-label">{{ t("cybercrime.copieIdentiteAuteurTransmise") }}</v-label>
          </dt>
          <dd class="ge-field-value text-body-1" aria-labelledby="lbl-anr-copieIdentiteAuteurTransmise">
            {{ formatOuiNon(data.copieIdentiteAuteurTransmise, t) }}
          </dd>
        </v-col>
      </v-row>
    </dl>
  </v-col>
</template>

<script lang="ts" setup>
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { CountryService } from "@/services/countryService";
import type { PrePlainteFormFields } from "@/types/pre-plainte.interface";
import { formatDateTimeFrench, toIsoDate } from "@/utils/helpers/dateHelpers.ts";
import { EMPTY_VALUE_DISPLAY } from "@/constants/constant.ts";
import { displayValue, formatOuiNon } from "@/utils/helpers/formHelpers.ts";

const props = defineProps<{
  data: PrePlainteFormFields;
}>();

const { t } = useI18n();

const vendeurPaysLibelle = computed(() => {
  const code = props.data?.vendeurPays as string | undefined;
  if (!code) {
    return EMPTY_VALUE_DISPLAY;
  }
  return CountryService.getCountryByCode(code)?.name || code;
});
</script>
