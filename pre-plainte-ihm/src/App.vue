<template>
  <v-app>
    <ge-header-public maxWidth="false" class="sticky-header"></ge-header-public>
    <v-main>
      <v-container fluid class="mt-12 wrapper pa-0 mb-12">
        <v-row justify="center" align="center">
          <v-locale-provider :locale="currentLocale" :messages="vuetifyMessages">
            <router-view :key="route.fullPath" />
          </v-locale-provider>
        </v-row>
      </v-container>
    </v-main>
    <ge-footer maxWidth="false" />
  </v-app>
</template>

<script setup lang="ts">
import { computed, onMounted } from "vue";
import { useRoute } from "vue-router";
import { useTheme } from "vuetify";
import { VLocaleProvider } from "vuetify/components";
import { fr, de, en, pt } from "vuetify/locale";
import { useI18n } from "vue-i18n";
import { useLocaleStore } from "@/stores/localeStore";

const route = useRoute();
const theme = useTheme();
const { locale } = useI18n();
const localeStore = useLocaleStore();

const currentLocale = computed(() => localeStore.locale);

onMounted(() => {
  localeStore.initLocale();
  locale.value = localeStore.locale;
  theme.change(globalThis.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light");
});

globalThis.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", e => {
  const newTheme = e.matches ? "dark" : "light";
  theme.change(newTheme);
});

const vuetifyMessages = {
  fr: {
    ...fr,
    input: {
      ...fr.input,
      clear: "Effacer",
    },
  },
  de: {
    ...de,
  },
  en: {
    ...en,
  },
  pt: {
    ...pt,
  },
};
</script>

<style scoped>
@media (min-width: 960px) {
  .wrapper {
    max-width: 1107px;
    margin: 0 auto;
  }
}

.sticky-header {
  position: sticky;
  top: 0;
  z-index: 1000;
}

@media (max-height: 320px) {
  .sticky-header {
    position: static;
  }
}
</style>

<style>
.v-field__details {
  margin-bottom: 8px !important;
}

.step-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background-color: #e0e0e0;
  transition: all 0.3s ease;
}

.step-dot.active {
  background-color: #3380b1;
  transform: scale(1.2);
}

.step-dot.completed {
  background-color: rgb(var(--v-theme-primary));
}

.pre-plainte-main-card-title {
  padding-left: 10px !important;
  padding-right: 10px !important;
  box-sizing: border-box;
}

@media (max-width: 959px) {
  .step-dot {
    width: 10px;
    height: 10px;
  }

  .v-card h1.pre-plainte-main-card-title,
  .v-card h2.pre-plainte-main-card-title,
  .v-card h3.pre-plainte-main-card-title {
    padding: 15px !important;
    box-sizing: border-box !important;
  }

  .pre-plainte-mobile-step-actions > .v-btn:first-child {
    margin-bottom: 5px !important;
  }
}
</style>
