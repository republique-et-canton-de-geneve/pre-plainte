<template>
  <div class="language-switcher">
    <v-menu location="bottom end" offset="8">
      <template #activator="{ props }">
        <v-btn
          v-bind="props"
          variant="text"
          color="primary"
          rounded="xl"
          class="language-trigger text-none"
          height="40"
        >
          <v-icon icon="mdi-translate" size="18" class="mr-2" />
          <span class="trigger-label">{{ currentLanguage.shortLabel }}</span>
          <v-icon icon="mdi-chevron-down" size="16" class="ml-1" />
        </v-btn>
      </template>

      <v-card min-width="220" rounded="xl" elevation="8">
        <v-list density="comfortable" nav class="py-2">
          <v-list-item
            v-for="item in languageItems"
            :key="item.value"
            :active="item.value === localeStore.locale"
            rounded="lg"
            class="mx-2"
            @click="changeLocale(item.value)"
          >
            <template #prepend>
              <span class="item-code">{{ item.shortLabel }}</span>
            </template>

            <v-list-item-title>{{ item.nativeLabel }}</v-list-item-title>

            <template #append>
              <v-icon v-if="item.value === localeStore.locale" icon="mdi-check" color="primary" size="18" />
            </template>
          </v-list-item>
        </v-list>
      </v-card>
    </v-menu>
  </div>
</template>

<script setup lang="ts">
import { computed } from "vue";
import { useI18n } from "vue-i18n";
import { useLocaleStore, type AppLocale } from "@/stores/localeStore";

type LanguageItem = {
  value: AppLocale;
  nativeLabel: string;
  shortLabel: string;
};

const { locale } = useI18n();
const localeStore = useLocaleStore();

const languageItems: LanguageItem[] = [
  { value: "fr", nativeLabel: "Français", shortLabel: "FR" },
  { value: "en", nativeLabel: "English", shortLabel: "EN" },
  { value: "de", nativeLabel: "Deutsch", shortLabel: "DE" },
  { value: "pt", nativeLabel: "Português", shortLabel: "PT" },
];

const currentLanguage = computed(
  () => languageItems.find(item => item.value === localeStore.locale) ?? languageItems[0],
);

function changeLocale(nextLocale: AppLocale) {
  localeStore.setLocale(nextLocale);
  locale.value = nextLocale;
}
</script>

<style scoped>
.language-switcher {
  display: flex;
  justify-content: flex-end;
}

.language-trigger {
  min-width: auto;
  padding-inline: 12px;
  border: 1px solid rgba(var(--v-theme-primary), 0.2);
  background: rgb(var(--v-theme-surface));
}

.trigger-label {
  font-size: 0.875rem;
  font-weight: 600;
  letter-spacing: 0.04em;
}

.item-code {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  font-size: 0.75rem;
  font-weight: 700;
  color: rgb(var(--v-theme-primary));
}

:deep(.v-list-item--active) {
  background: rgba(var(--v-theme-primary), 0.08);
}
</style>
