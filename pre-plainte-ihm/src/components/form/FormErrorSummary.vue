<template>
  <v-alert
    v-if="isVisible"
    type="error"
    variant="tonal"
    density="comfortable"
    class="mb-4 form-error-summary"
    role="alert"
    data-cy="form-error-summary"
  >
    <div v-if="summaryMessage" class="text-body-2 font-weight-medium" :class="{ 'mb-2': items.length > 0 }">
      {{ summaryMessage }}
    </div>
    <template v-if="items.length > 0">
      <div v-if="!summaryMessage" class="text-body-2 font-weight-medium mb-2">
        {{ t("common.erreursFormulaire", items.length, { count: items.length }) }}
      </div>
      <ul class="form-error-summary__list pl-4 mb-0">
        <li v-for="(item, index) in visibleItems" :key="`${item.path}-${index}-${item.message}`" class="text-body-2">
          <button type="button" class="form-error-summary__link" @click="onSelectError(item)">
            {{ item.message }}
          </button>
        </li>
      </ul>
      <p v-if="items.length > maxVisible" class="text-body-2 mt-2 mb-0">
        {{ t("common.erreursFormulaireAutres", items.length - maxVisible, { count: items.length - maxVisible }) }}
      </p>
    </template>
  </v-alert>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import type { FormValidationErrorItem } from "@/utils/helpers/formErrorHelpers";
import { useFormErrorScroll } from "@/composables/useFormErrorScroll";

const props = withDefaults(
  defineProps<{
    items?: FormValidationErrorItem[];
    messages?: string[];
    maxVisible?: number;
    summaryMessage?: string;
  }>(),
  {
    items: () => [],
    messages: () => [],
    maxVisible: 5,
    summaryMessage: undefined,
  },
);

const { t } = useI18n();
const { scrollToValidationError } = useFormErrorScroll();

const items = computed<FormValidationErrorItem[]>(() => {
  if (props.items.length > 0) {
    return props.items;
  }
  return props.messages.map(message => ({ path: "", message }));
});

const visibleItems = computed(() => items.value.slice(0, props.maxVisible));

const isVisible = computed(() => Boolean(props.summaryMessage) || items.value.length > 0);

const onSelectError = async (item: FormValidationErrorItem) => {
  await scrollToValidationError(item.path, item.message);
};
</script>

<style scoped>
.form-error-summary {
  scroll-margin-top: 96px;
}

.form-error-summary__list {
  list-style: disc;
}

.form-error-summary__link {
  all: unset;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 2px;
  color: inherit;
}

.form-error-summary__link:focus-visible {
  outline: 2px solid rgb(var(--v-theme-primary));
  outline-offset: 2px;
}

@media (max-width: 959px) {
  .form-error-summary :deep(.v-alert__content) {
    font-size: 0.875rem;
  }
}
</style>
