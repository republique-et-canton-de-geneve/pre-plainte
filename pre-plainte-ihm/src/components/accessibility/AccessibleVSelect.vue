<template>
  <div v-bind="$attrs">
    <v-select
      v-model="proxy"
      v-bind="{ ...vuetifyProps, ...$attrs }"
      :label="displayLabel"
      :items="items"
      :item-title="itemTitle"
      :item-value="itemValue"
      :return-object="returnObject"
      :loading="loading"
      :disabled="disabled"
      :readonly="readonly"
      :clearable="clearable"
      :error-messages="errorMessages"
      :hint="hint"
      :persistent-hint="persistentHint"
      :variant="variant"
    >
      <template v-for="(slotName) in $slots" :key="String(slotName)" v-slot:[slotName]="slotProps">
        <slot :name="slotName" v-bind="slotProps" />
      </template>
    </v-select>

    <div v-if="!returnObject" class="css-fallback-native-select">
      <label :id="`${nativeId}-label`" :for="nativeId">
        {{ displayLabel }}
      </label>

      <select
        :id="nativeId"
        :aria-labelledby="`${nativeId}-label`"
        :disabled="disabled || readonly"
        :aria-disabled="disabled || readonly"
        :value="nativeValue"
        @change="onNativeChange"
      >
        <option v-if="clearable" value="">—</option>

        <option v-for="it in normalizedItems" :key="String(it.value)" :value="String(it.value)">
          {{ it.label }}
        </option>
      </select>

      <p v-if="firstError" role="alert">{{ firstError }}</p>
      <p v-if="hint && persistentHint">{{ hint }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, useId } from "vue";
import type { PropType } from "vue";
import { requiredLabel } from "@/utils/helpers/labelHelpers";

defineOptions({ inheritAttrs: false });

type TitleValueAccessor<T> = string | ((item: T) => unknown);

type Primitive = string | number | boolean | null | undefined;

type NormalizedOption = { label: string; value: Primitive };

const EMIT_UPDATE_MODEL_VALUE = "update:modelValue" as const;

const props = defineProps({
  modelValue: { type: null as any, default: null },
  items: { type: Array as PropType<readonly any[]>, required: true },

  label: { type: String, required: true },
  required: { type: Boolean, default: false },

  itemTitle: { type: [String, Function] as PropType<TitleValueAccessor<any>>, default: "label" },
  itemValue: { type: [String, Function] as PropType<TitleValueAccessor<any>>, default: "value" },

  returnObject: { type: Boolean, default: false },

  loading: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  readonly: { type: Boolean, default: false },
  clearable: { type: Boolean, default: false },

  errorMessages: { type: [String, Array] as PropType<string | string[] | undefined>, default: null },
  hint: { type: String, default: null },
  persistentHint: { type: Boolean, default: false },

  variant: {
    type: String as PropType<"outlined" | "filled" | "solo" | "plain" | "underlined">,
    default: "outlined",
  },

  vuetifyProps: { type: Object as PropType<Record<string, unknown>>, default: () => ({}) },

  id: { type: String, default: null },
});

const emit = defineEmits<{
  "update:modelValue": [value: unknown];
}>();

const proxy = computed({
  get: () => {
    if (!props.returnObject && props.modelValue === "") {
      return null;
    }
    return props.modelValue;
  },
  set: (v: unknown) => {
    if (!props.returnObject && (v === null || v === undefined)) {
      emit(EMIT_UPDATE_MODEL_VALUE, "");
      return;
    }
    emit(EMIT_UPDATE_MODEL_VALUE, v);
  },
});

const uid = useId();
const nativeId = computed(() => (props.id ? `${props.id}-native` : `native-${uid}`));
const displayLabel = computed(() => (props.required ? requiredLabel(props.label) : props.label));

const firstError = computed(() => {
  if (!props.errorMessages) {
    return "";
  }
  return Array.isArray(props.errorMessages) ? (props.errorMessages[0] ?? "") : props.errorMessages;
});

function getFromAccessor(item: any, acc: TitleValueAccessor<any>): unknown {
  if (typeof acc === "function") {
    return acc(item);
  }
  return item?.[acc];
}

const normalizedItems = computed<NormalizedOption[]>(() => {
  return props.items.map(it => ({
    label: String(getFromAccessor(it, props.itemTitle) ?? ""),
    value: (getFromAccessor(it, props.itemValue) as Primitive) ?? "",
  }));
});

const nativeValue = computed(() => {
  if (props.modelValue === null || props.modelValue === undefined) {
    return "";
  }
  return String(props.modelValue as Primitive);
});

const onNativeChange = (e: Event) => {
  const raw = (e.target as HTMLSelectElement).value;

  if (props.clearable && raw === "") {
    emit(EMIT_UPDATE_MODEL_VALUE, Array.isArray(props.modelValue) ? [] : "");
    return;
  }

  const match = normalizedItems.value.find(o => String(o.value) === raw);
  emit(EMIT_UPDATE_MODEL_VALUE, match ? match.value : raw);
};
</script>

<style scoped>
.css-fallback-native-select {
  position: absolute;
  left: -9999px;
  width: 1px;
  height: 1px;
  overflow: hidden;
}
</style>
