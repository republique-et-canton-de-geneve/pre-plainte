<template>
  <div>
    <AccessibleVSelect
      :items="typesPersonnesMorales"
      :label="t('informationsPersonnelles.postePersonneMorale')"
      v-model="postePersonneMorale"
      :error-messages="postePersonneMoraleError"
      item-title="label"
      item-value="value"
      class="mb-4 mt-8"
      variant="outlined"
    />
    <div class="mb-3">
      <PieceJointe
        v-model="justificatifPersonneMorale"
        :label="t('informationsPersonnelles.justificatifPersonneMorale')"
        :max-files="2"
        :max-file-size="MAX_FILE_SIZE"
        :max-total-size="MAX_TOTAL_SIZE_20_MO"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useField } from "vee-validate";
import { useI18n } from "vue-i18n";
import { MAX_FILE_SIZE, MAX_TOTAL_SIZE_20_MO, TYPES_PERSONNES_MORALES } from "@/constants/constant";
import PieceJointe from "@/components/piece-jointe/PieceJointe.vue";
import AccessibleVSelect from "@/components/accessibility/AccessibleVSelect.vue";
import { toTranslatedOptions } from "@/utils/helpers/traductionHelper";

const { t } = useI18n();

const { value: postePersonneMorale, errorMessage: postePersonneMoraleError } = useField<string | undefined>(
  "postePersonneMorale",
);

const { value: justificatifPersonneMorale } = useField<File[]>("justificatifPersonneMorale");

const typesPersonnesMorales = computed(() => toTranslatedOptions(TYPES_PERSONNES_MORALES, t));
</script>
