<template>
  <v-app>
    <ge-header-public maxWidth="false" class="sticky-header"></ge-header-public>
    <v-main>
      <v-container fluid class="mt-12 wrapper pa-0 mb-12">
        <v-locale-provider :locale="currentLocale" :messages="vuetifyMessages">
          <router-view :key="route.fullPath" />
        </v-locale-provider>
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
import { fr, de, en, it, pt } from "vuetify/locale";
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
  it: {
    ...it,
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
.v-container {
  width: 100%;
  padding: 4px !important;
  margin-right: auto;
  margin-left: auto;
}

.v-row > [class*="v-col"] {
  padding: 4px !important;
}

.v-field__details {
  margin-bottom: 8px !important;
}

.v-field input,
.v-field textarea,
.v-select .v-field__input,
.v-autocomplete .v-field__input {
  font-size: 16px !important;
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
  padding-bottom: 12px !important;
  box-sizing: border-box;
  max-width: 100%;
  overflow-wrap: anywhere;
}

.v-main {
  overflow-x: clip;
}

@media (max-width: 959px) {
  .step-dot {
    width: 10px;
    height: 10px;
  }

  .v-main .mb-8 {
    margin-bottom: 16px !important;
  }

  .v-main .v-card,
  .v-main .v-sheet {
    overflow: visible !important;
  }

  .v-card h1.pre-plainte-main-card-title,
  .v-card h2.pre-plainte-main-card-title,
  .v-card h3.pre-plainte-main-card-title {
    padding: 20px 5px 12px 0 !important;
    box-sizing: border-box !important;
  }

  .pre-plainte-mobile-step-actions {
    gap: 8px !important;
  }

  .frc-captcha {
    transform: scale(0.92);
    transform-origin: center top;
    min-height: 64px;
  }
}
</style>
