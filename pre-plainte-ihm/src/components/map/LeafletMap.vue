<template>
  <div ref="mapContainer" class="leaflet-map" :style="{ height: height + 'px' }"></div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, nextTick } from "vue";
import { useI18n } from "vue-i18n";
import { useLeafletMap, type Service } from "@/composables/useLeafletMap";

const DEFAULT_MAP_HEIGHT = 400;
const DEFAULT_MAP_ZOOM = 12;

const DEFAULT_MAP_CENTER_X = 46.2044;
const DEFAULT_MAP_CENTER_Y =  6.1432;

interface Props {
  height?: number;
  services: Service[];
  selectedService?: Service | null;
  center?: [number, number];
  zoom?: number;
}

const props = withDefaults(defineProps<Props>(), {
  height: DEFAULT_MAP_HEIGHT,
  zoom: DEFAULT_MAP_ZOOM,
  center: () => [DEFAULT_MAP_CENTER_X, DEFAULT_MAP_CENTER_Y],
});

const emit = defineEmits<{
  "suggest-nearest": [service: Service, distance: number];
  "update:selectedService": [service: Service | null];
}>();

const { t, locale } = useI18n();

const mapContainer = ref<HTMLElement | null>(null);

const { initializeMap, addServiceMarkers, updateSelectedMarker, resetView, cleanup, localizeAria } = useLeafletMap(
  mapContainer,
  props,
  emit,
  t,
);

watch(
  () => locale.value,
  () => nextTick(() => localizeAria()),
);

watch(
  () => props.services,
  () => {
    if (mapContainer.value) {
      addServiceMarkers();
    }
  },
  { deep: true },
);

watch(
  () => props.selectedService,
  val => {
    if (!mapContainer.value) {
      return;
    }

    if (val) {
      updateSelectedMarker();
    } else {
      resetView();
    }
  },
  { deep: true },
);

onMounted(() => {
  nextTick(() => {
    initializeMap();
    localizeAria();
  });
});

onUnmounted(() => {
  cleanup();
});
</script>

<style scoped>
.leaflet-map {
  width: 100%;
  border-radius: 10px;
  overflow: hidden;
}

@media (max-width: 959px) {
  .leaflet-map {
    width: 102%;
    margin-left: -1%;
    margin-right: -1%;
  }
}

:deep(.leaflet-popup-content) {
  margin: 8px 12px;
}

:deep(.leaflet-popup-tip) {
  background: white;
}
</style>
