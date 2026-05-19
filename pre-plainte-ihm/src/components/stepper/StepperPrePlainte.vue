<template>
  <div class="sticky-stepper mt-10">
    <v-stepper v-model="step" direction="horizontal" class="custom-stepper d-flex flex-column">
      <template v-for="(item, index) in stepsToRender" :key="index">
        <v-stepper-item color="primary" :title="item.title" :complete="step > index + 1" :value="index + 1" />
        <div v-if="index < stepsToRender.length - 1" class="step-vertical-separator"></div>
      </template>
    </v-stepper>
    <div v-if="showActions" class="actions mt-5 ml-7">
      <ExitActionsForm />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { useCreatePrePlainteStore } from "@/stores/createPrePlainteStore";
import { storeToRefs } from "pinia";
import ExitActionsForm from "@/components/actions/ExitActionsForm.vue";

type StepItem = {
  titleKey: string;
};

type RenderedStepItem = {
  title: string;
};

const props = withDefaults(
  defineProps<{
    customSteps?: StepItem[];
    showActions?: boolean;
  }>(),
  {
    customSteps: () => [],
    showActions: true,
  },
);

const { t } = useI18n();
const plainteStore = useCreatePrePlainteStore();
const { step } = storeToRefs(plainteStore);

const stepsToRender = computed<RenderedStepItem[]>(() => {
  const sourceSteps = props.customSteps.length ? props.customSteps : plainteStore.steps;

  return sourceSteps.map(item => ({
    title: t(item.titleKey),
  }));
});

const showActions = computed(() => props.showActions);
</script>

<style scoped>
.sticky-stepper {
  position: sticky;
  top: 96px;
}

.custom-stepper {
  background-color: transparent !important;
  box-shadow: none !important;
  border-radius: 0 !important;
}

.step-vertical-separator {
  width: 1px;
  height: 22px;
  background-color: #d1d5db;
  margin: 0 35px;
  align-self: left;
}

.actions {
  display: flex;
  gap: 8px;
  flex-wrap: nowrap;
  align-items: center;
}

@media (max-width: 959px) {
  .sticky-stepper {
    position: static;
    background-color: transparent;
    padding: 0;
  }
}
</style>
