import type { Ref } from "vue";
import L, { type Map as LeafletMap, type Marker, type LatLngExpression } from "leaflet";
import escapeHtml from "escape-html";
import {
  getServiceCoordinates,
  getServiceAddress,
  getUserLocation,
  findNearestStation,
} from "@/utils/helpers/mapHelpers";
import "leaflet/dist/leaflet.css";
import iconShadow from "leaflet/dist/images/marker-shadow.png";
import defaultMarkerIcon from "@/common/assets/mappointer-default.svg";

const DEFAULT_ICON_WIDTH = 32;
const DEFAULT_ICON_HEIGHT = 49;
const DEFAULT_ICON_ANCHOR_X = 16;
const DEFAULT_ICON_ANCHOR_Y = 49;
const SELECTED_ICON_WIDTH = 25;
const SELECTED_ICON_HEIGHT = 41;
const SELECTED_ICON_ANCHOR_X = 12;
const SELECTED_ICON_ANCHOR_Y = 41;
const POPUP_ANCHOR_X = 1;
const POPUP_ANCHOR_Y = -34;
const SHADOW_WIDTH = 41;
const SHADOW_HEIGHT = 41;
const DEFAULT_MIN_ZOOM = 15;
const GROUP_PAD = 0.1;

const ARIA_LABEL_ATTRIBUTE = "aria-label";
const TITLE_ATTRIBUTE = "title";

export interface Service {
  id: number;
  key: string;
  name: string;
  latitude?: number;
  longitude?: number;
}

export interface UseLeafletMapProps {
  center: LatLngExpression;
  zoom: number;
  services: Service[];
  selectedService?: Service | null;
}

type TFunction = (key: string, params?: Record<string, any>) => string;

export function useLeafletMap(
  containerRef: Ref<HTMLElement | null>,
  props: UseLeafletMapProps,
  emit: {
    (event: "suggest-nearest", service: Service, distance: number): void;
    (event: "update:selectedService", service: Service | null): void;
  },
  t?: TFunction,
) {
  let leafletMap: LeafletMap | null = null;
  let markers: Marker[] = [];
  let selectedMarker: Marker | null = null;
  let observer: MutationObserver | null = null;

  const DefaultIcon = L.icon({
    iconUrl: defaultMarkerIcon,
    shadowUrl: iconShadow,
    iconSize: [DEFAULT_ICON_WIDTH, DEFAULT_ICON_HEIGHT],
    iconAnchor: [DEFAULT_ICON_ANCHOR_X, DEFAULT_ICON_ANCHOR_Y],
    popupAnchor: [POPUP_ANCHOR_X, POPUP_ANCHOR_Y],
    shadowSize: [SHADOW_WIDTH, SHADOW_HEIGHT],
  });

  const RedIcon = L.divIcon({
    className: "custom-red-marker",
    html: `
      <svg width="25" height="41" viewBox="0 0 25 41" xmlns="http://www.w3.org/2000/svg">
        <path d="M12.5 0C5.6 0 0 5.6 0 12.5c0 12.5 12.5 28.5 12.5 28.5s12.5-16 12.5-28.5C25 5.6 19.4 0 12.5 0z" fill="#dc2626"/>
        <circle cx="12.5" cy="12.5" r="6" fill="white"/>
      </svg>
    `,
    iconSize: [SELECTED_ICON_WIDTH, SELECTED_ICON_HEIGHT],
    iconAnchor: [SELECTED_ICON_ANCHOR_X, SELECTED_ICON_ANCHOR_Y],
    popupAnchor: [POPUP_ANCHOR_X, POPUP_ANCHOR_Y],
  });

  L.Marker.prototype.options.icon = DefaultIcon;

  const localizeAria = () => {
    if (!leafletMap) {
      return;
    }
    const root = leafletMap.getContainer();
    if (!root) {
      return;
    }

    const closeLabel = t?.("map.closePopup") ?? "Fermer la fenêtre d'information";
    root.querySelectorAll<HTMLElement>(".leaflet-popup-close-button").forEach(btn => {
      btn.setAttribute(ARIA_LABEL_ATTRIBUTE, closeLabel);
      btn.setAttribute(TITLE_ATTRIBUTE, closeLabel);
    });

    const zoomIn = t?.("map.zoomIn") ?? "Zoomer";
    root.querySelectorAll<HTMLElement>(".leaflet-control-zoom-in").forEach(el => {
      el.setAttribute(ARIA_LABEL_ATTRIBUTE, zoomIn);
      el.setAttribute(TITLE_ATTRIBUTE, zoomIn);
    });

    const zoomOut = t?.("map.zoomOut") ?? "Dézoomer";
    root.querySelectorAll<HTMLElement>(".leaflet-control-zoom-out").forEach(el => {
      el.setAttribute(ARIA_LABEL_ATTRIBUTE, zoomOut);
      el.setAttribute(TITLE_ATTRIBUTE, zoomOut);
    });
  };

  const disableMapFocus = () => {
    const el = containerRef.value;
    if (!el) {
      return;
    }

    el.querySelectorAll<HTMLElement>(
      '.leaflet-container, .leaflet-control, .leaflet-pane, a, button, [tabindex]'
    ).forEach(node => {
      node.setAttribute('tabindex', '-1');
    });
  };

  const observeAndDisableFocus = () => {
    const el = containerRef.value;
    if (!el) {
      return;
    }

    observer = new MutationObserver(() => {
      disableMapFocus();
    });

    observer.observe(el, {
      childList: true,
      subtree: true,
    });

    disableMapFocus();
  };
  const buildPopupHtml = (service: Service) => {
    const address = escapeHtml(getServiceAddress(service));

    const title = t?.("map.popupTitle", { name: service.name }) ?? `Poste de police de ${service.name}`;

    return `
      <div style="text-align:center;">
        <strong>${escapeHtml(title)}</strong><br>
        <small style="color:#666;">${address}</small>
      </div>
    `;
  };

  const initializeMap = () => {
    if (!containerRef.value) {
      return;
    }

    leafletMap = L["map"](containerRef.value, {
      center: props.center,
      zoom: props.zoom,
      zoomControl: true,
      attributionControl: true,
      keyboard: false,
    });

    leafletMap.attributionControl.setPrefix("");

    L.tileLayer("https://{s}.tile.openstreetmap.fr/osmfr/{z}/{x}/{y}.png", {
      maxZoom: 19,
      subdomains: ["a", "b", "c"],
    }).addTo(leafletMap);

    leafletMap.on("popupopen", () => {
      localizeAria();
    });

    addServiceMarkers();
    localizeAria();
    observeAndDisableFocus();

    getUserLocation()
      .then(coords => {
        const nearest = findNearestStation(coords, props.services);
        if (nearest) {
          emit("suggest-nearest", nearest.service, nearest.distance);
        }
      })
      .catch(error => {
        console.warn("Géolocalisation non disponible:", error.message);
      });
  };

  const addServiceMarkers = () => {
    if (!leafletMap) {
      return;
    }

    const m = leafletMap;

    markers.forEach(marker => m.removeLayer(marker));
    markers = [];

    props.services.forEach(service => {
      const coords = getServiceCoordinates(service);
      if (!coords) {
        return;
      }

      const marker = L.marker(coords).addTo(m).bindPopup(buildPopupHtml(service));

      marker.on("click", () => {
        if (selectedMarker) {
          m.removeLayer(selectedMarker);
        }

        selectedMarker = L.marker(coords, { icon: RedIcon })
          .addTo(m)
          .bindPopup(buildPopupHtml(service))
          .openPopup();

        m.setView(coords, Math.max(props.zoom, DEFAULT_MIN_ZOOM));

        emit("update:selectedService", service);
      });

      markers.push(marker);
    });

    if (markers.length > 0) {
      const group = L.featureGroup(markers);
      m.fitBounds(group.getBounds().pad(GROUP_PAD));
    }

    localizeAria();
  };

  const updateSelectedMarker = () => {
    if (!leafletMap || !props.selectedService) {
      return;
    }

    if (selectedMarker) {
      leafletMap.removeLayer(selectedMarker);
    }

    const coords = getServiceCoordinates(props.selectedService);
    if (!coords) {
      return;
    }

    selectedMarker = L.marker(coords, { icon: RedIcon })
      .addTo(leafletMap)
      .bindPopup(buildPopupHtml(props.selectedService))
      .openPopup();

    leafletMap.setView(coords, Math.max(props.zoom, DEFAULT_MIN_ZOOM));
    localizeAria();
  };

  const resetView = () => {
    if (!leafletMap) {
      return;
    }
    const coords = getServiceCoordinates(props.selectedService);
    if (!coords) {
      return;
    }
    leafletMap.setView(coords, Math.max(props.zoom, DEFAULT_MIN_ZOOM));
  };

  const cleanup = () => {
    if (observer) {
      observer.disconnect();
      observer = null;
    }

    if (leafletMap) {
      leafletMap.off();
      leafletMap.remove();
      leafletMap = null;
    }
  };

  return {
    initializeMap,
    addServiceMarkers,
    updateSelectedMarker,
    resetView,
    cleanup,
    localizeAria,
  };
}
