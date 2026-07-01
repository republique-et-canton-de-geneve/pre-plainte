---
layout: home

hero:
  name: Tests frontend
  text: Couverture testee du formulaire citoyen
  tagline: Les regles Vitest documentent les validations fines, les scenarios Cypress documentent les parcours frontend executes de bout en bout.
  actions:
    - theme: brand
      text: Voir la couverture Cypress
      link: /couverture-cypress
    - theme: alt
      text: Voir les regles metier
      link: /regles-metier-formulaire

features:
  - title: Parcours Cypress
    details: Les fichiers `.feature` exposent les parcours citoyens testes et les regles metier couvertes de bout en bout.
  - title: Regles Vitest
    details: Les regles de `src/test/business-rules` pilotent les tests unitaires frontend et la page Markdown generee.
  - title: Documentation publiee
    details: Le build VitePress regenere les pages de couverture avant de produire le site statique servi par GitLab Pages.
---

## Boucle proposee

1. Declarer ou modifier une regle dans `pre-plainte-ihm/src/test/business-rules`.
2. Declarer ou modifier une `Règle:` dans les fichiers `pre-plainte-cypress/cypress/e2e/*.feature`.
3. Regenerer et visualiser la documentation avec `npm run docs:dev` dans `pre-plainte-doc`.

## Commandes

```sh
cd pre-plainte-doc
npm install
npm run docs:dev
```

Pour produire les fichiers statiques :

```sh
cd pre-plainte-doc
npm run docs:build
```
