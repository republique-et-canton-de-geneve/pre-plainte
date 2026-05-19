<template>
  <div>
    <v-alert type="info" class="mb-10" :icon="mobile ? false : undefined">
      {{ t("informationsPersonnelles.infoRepresentation") }}
    </v-alert>
    <accessible-v-select
      :label="t('informationsPersonnelles.typeRepresentation')"
      :items="representationOptions"
      v-model="typeRepresentation"
      :error-messages="typeRepresentationError"
      item-title="label"
      item-value="value"
      class="mb-8"
      variant="outlined"
    />
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useField } from "vee-validate";
import { useI18n } from "vue-i18n";
import { REPRESENTATION_OPTIONS } from "@/constants/constant";
import { useDisplay } from "vuetify";
import AccessibleVSelect from "@/components/accessibility/AccessibleVSelect.vue";
import { toTranslatedOptions } from "@/utils/helpers/traductionHelper";

const { t } = useI18n();
const { mobile } = useDisplay();

const { value: typeRepresentation, errorMessage: typeRepresentationError } = useField<string | undefined>(
  "typeRepresentation",
);

const representationOptions = computed(() => toTranslatedOptions(REPRESENTATION_OPTIONS, t));
</script>
