import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import vueDevTools from "vite-plugin-vue-devtools";
import vuetify from "vite-plugin-vuetify";
import VueI18nPlugin from "@intlify/unplugin-vue-i18n/vite";
import { fileURLToPath } from "node:url";
import { dirname, resolve } from "node:path";

const _filename = fileURLToPath(import.meta.url);
const _dirname = dirname(_filename);

export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
    vuetify({ autoImport: true }),
    VueI18nPlugin({
      include: resolve(_dirname, "./src/common/i18n/locales/**"),
    }),
  ],
  build: {
    outDir: "dist",
  },
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
});
