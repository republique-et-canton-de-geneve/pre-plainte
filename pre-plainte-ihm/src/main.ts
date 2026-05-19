import { createApp } from "vue";
import { createPinia } from "pinia";
import { registerVuetifyPlugin } from "@ael/ge-vuetify-theme";
import { VueQueryPlugin, QueryClient } from "@tanstack/vue-query";
import "vuetify/styles";
import "@mdi/font/css/materialdesignicons.css";
import "flag-icons/css/flag-icons.min.css";
import "v-phone-input/dist/v-phone-input.css";
import { createVPhoneInput } from "v-phone-input";

import App from "@/App.vue";
import router from "@/router";
import { i18n } from "@/common/i18n";
import { loadConfig } from "./config/config";
import { RipolService } from "@/services/ripolService";
import { useDocumentTitleSync } from "./composables/useDocumentTitleSync";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 24 * 60 * 60 * 1000, // 24 heures - données RIPOL mises à jour ~1x/mois
      gcTime: 7 * 24 * 60 * 60 * 1000, // 7 jours - garde en mémoire
      retry: 2,
      refetchOnWindowFocus: false,
    },
  },
});

const app = createApp(App);

await loadConfig();

await registerVuetifyPlugin(app, {
  hostUrl: "https://static.app.ge.ch/",
  loadCssFiles: true,
  mode: 3,
  nonce: undefined,
});

const vPhoneInput = createVPhoneInput({
  countryIconMode: "svg",
});

app.use(vPhoneInput);
app.use(createPinia());
app.use(VueQueryPlugin, { queryClient });
app.use(i18n);
app.use(router);
useDocumentTitleSync();

app.mount("#app");

// Préchargement des données RIPOL courantes (en arrière-plan)
RipolService.preload().catch(() => {
  // Ignore les erreurs - les données seront chargées à la demande
});
