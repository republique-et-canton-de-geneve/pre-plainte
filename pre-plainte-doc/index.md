---
layout: home

hero:
  name: Tests frontend
  text: Regles metier documentees depuis les cas Vitest
  tagline: Une source TypeScript alimente les tests unitaires et la documentation Markdown publiee par VitePress.
  actions:
    - theme: brand
      text: Voir les regles metier
      link: /regles-metier-formulaire
    - theme: alt
      text: Voir le plan Cypress
      link: /plan-cypress

features:
  - title: Source unique
    details: Les regles de `src/test/business-rules` pilotent les tests Vitest et la page Markdown generee.
  - title: Synchronisation visible
    details: Le script `docs:rules` regenere `regles-metier-formulaire.md` avant le lancement ou le build du site.
  - title: Publication statique
    details: VitePress produit un site HTML autonome qui peut etre servi comme artefact de documentation.
---

## Boucle proposee

1. Declarer ou modifier une regle dans `pre-plainte-ihm/src/test/business-rules`.
2. Executer les tests unitaires frontend avec `npm run test:unit` dans `pre-plainte-ihm`.
3. Regenerer et visualiser la documentation avec `npm run docs:dev` dans `pre-plainte-doc`.

## Commandes du POC

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
