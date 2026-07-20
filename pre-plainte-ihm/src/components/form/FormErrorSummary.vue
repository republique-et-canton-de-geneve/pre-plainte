<template>
  <v-alert
    v-if="messages.length > 0"
    type="error"
    variant="tonal"
    density="comfortable"
    class="mb-4"
    role="alert"
    data-cy="form-error-summary"
  >
    <div class="text-body-2 font-weight-medium mb-2">
      {{ t("common.erreursFormulaire", messages.length, { count: messages.length }) }}
    </div>
    <ul class="form-error-summary__list pl-4 mb-0">
      <li v-for="(message, index) in visibleMessages" :key="`${index}-${message}`" class="text-body-2">
        {{ message }}
      </li>
    </ul>
    <p v-if="messages.length > maxVisible" class="text-body-2 mt-2 mb-0">
      {{ t("common.erreursFormulaireAutres", messages.length - maxVisible, { count: messages.length - maxVisible }) }}
    </p>
  </v-alert>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";

const props = withDefaults(
  defineProps<{
    messages: string[];
    maxVisible?: number;
  }>(),
  {
    maxVisible: 5,
  },
);

const { t } = useI18n();

const visibleMessages = computed(() => props.messages.slice(0, props.maxVisible));
</script>

<style scoped>
.form-error-summary__list {
  list-style: disc;
}
</style>
