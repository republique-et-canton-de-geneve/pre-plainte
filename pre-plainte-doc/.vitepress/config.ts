import { defineConfig } from "vitepress";

export default defineConfig({
  title: "Pre-plainte",
  description: "Documentation des tests frontend et des regles metier du formulaire",
  lang: "fr-CH",
  base: process.env.VITEPRESS_BASE ?? "/",
  themeConfig: {
    nav: [
      { text: "Accueil", link: "/" },
      { text: "Regles metier", link: "/regles-metier-formulaire" },
      { text: "Couverture Cypress", link: "/couverture-cypress" },
      { text: "API", link: "/api/swagger-api" },
    ],
    sidebar: [
      {
        text: "Tests frontend",
        items: [
          { text: "Vue d'ensemble", link: "/" },
          { text: "Regles metier du formulaire", link: "/regles-metier-formulaire" },
          { text: "Couverture Cypress", link: "/couverture-cypress" },
          { text: "ROI tests et documentation", link: "/roi-tests-documentation" },
        ],
      },
      {
        text: "Guides",
        items: [
          { text: "Ajout d'un champ", link: "/guides/ajout-champ" },
          { text: "Mapping codes RIPOL myAbi", link: "/guides/mapping-codes-ripol-myabi" },
        ],
      },
      {
        text: "API",
        items: [
          { text: "Swagger / OpenAPI", link: "/api/swagger-api" },
        ],
      },
      {
        text: "Architecture",
        items: [
          { text: "CI/CD", link: "/architecture/ci-cd" },
          { text: "Communication eSirius rendez-vous", link: "/architecture/communication-esirius-rendez-vous" },
          { text: "Control-M S3 NAS", link: "/architecture/control-m-s3-nas" },
          { text: "Stockage S3", link: "/architecture/stockage-s3" },
        ],
      },
      {
        text: "ADR",
        items: [
          { text: "ADR 001 - Ajout d'un champ", link: "/adr/adr-001-ajout-champ" },
        ],
      },
    ],
    search: {
      provider: "local",
    },
    outline: {
      label: "Sur cette page",
    },
    docFooter: {
      prev: "Page precedente",
      next: "Page suivante",
    },
    lastUpdated: {
      text: "Derniere mise a jour",
    },
  },
});
