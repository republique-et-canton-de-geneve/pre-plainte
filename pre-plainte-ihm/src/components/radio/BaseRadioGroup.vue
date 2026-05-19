<template>
  <fieldset class="mb-4">
    <legend class="text-subtitle-1 mb-2">
      {{ label }}
    </legend>

    <v-radio-group
      :model-value="modelValue"
      @update:model-value="updateValue"
      :error-messages="errorMessages"
      :error="!!errorMessages"
      :hint="hint"
      :persistent-hint="!!hint"
      :class="{ 'mb-4': hint }"
      inline
    >
      <v-radio
        v-for="opt in options"
        :key="String(opt.value)"
        :label="opt.label"
        :value="opt.value"
      />
    </v-radio-group>
  </fieldset>
</template>
<script setup lang="ts">
interface Option<T = any> {
  label: string
  value: T
}

const props = defineProps<{
  modelValue: any
  label: string
  options: Option[]
  errorMessages?: string | string[]
  hint?: string
}>()

const emit = defineEmits<{
  "update:modelValue": [value: any];
}>();


const updateValue = (value: any) => {
  emit("update:modelValue", value)
}
</script>
<style scoped>
fieldset {
  border: 0;
  padding: 0;
  margin: 0;
}
legend {
  padding: 0;
}
</style>
