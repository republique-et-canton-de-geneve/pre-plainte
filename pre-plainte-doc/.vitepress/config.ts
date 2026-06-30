import { defineConfig } from "vitepress";

export default defineConfig({
  title: "Pre-plainte",
  description: "Documentation des tests frontend et des regles metier du formulaire",
  lang: "fr-CH",
  base: process.env.VITEPRESS_BASE ?? "/",
  cleanUrls: true,
  themeConfig: {
    nav: [
      { text: "Accueil", link: "/" },
      { text: "Regles metier", link: "/regles-metier-formulaire" },
      { text: "Plan Cypress", link: "/plan-cypress" },
    ],
    sidebar: [
      {
        text: "Tests frontend",
        items: [
          { text: "Vue d'ensemble", link: "/" },
          { text: "Regles metier du formulaire", link: "/regles-metier-formulaire" },
          { text: "Plan Cypress", link: "/plan-cypress" },
          { text: "Liste des champs citoyen", link: "/liste-champs-citoyen" },
        ],
      },
      {
        text: "Guides",
        items: [
          { text: "Ajout d'un champ", link: "/guides/ajout-champ" },
          { text: "Regles metier declaration vol", link: "/guides/regles-metier-declaration-vol" },
        ],
      },
      {
        text: "Architecture",
        items: [
          { text: "CI/CD", link: "/architecture/ci-cd" },
          { text: "Stockage S3", link: "/architecture/stockage-s3" },
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
