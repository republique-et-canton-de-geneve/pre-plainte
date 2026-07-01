# Documentation projet

Ce module contient la documentation technique du projet.

## POC VitePress

Le module peut etre expose sous forme de site statique VitePress pour mettre en avant les regles metier couvertes par les tests frontend.

Commandes :

```sh
npm install
npm run docs:dev
npm run docs:build
```

Les commandes `docs:dev` et `docs:build` regenerent `regles-metier-formulaire.md` depuis `pre-plainte-ihm/src/test/business-rules` avant de lancer VitePress.

## Structure

- `guides/` : guides pratiques, par exemple l'ajout d'un champ.
- `architecture/` : documentation d'architecture et d'intégration.
- `api/` : documentation API Swagger / OpenAPI.
- `adr/` : décisions d'architecture.

## Documentation disponible

- [CI/CD](architecture/ci-cd.md)
- [Communication avec eSirius pour la prise de rendez-vous](architecture/communication-esirius-rendez-vous.md)
- [Control-M S3 vers NAS police](architecture/control-m-s3-nas.md)
- [Stockage S3](architecture/stockage-s3.md)
- [Guide technique - Ajout d'un champ](guides/ajout-champ.md)
- [Mapping des codes RIPOL vers MyABI](guides/mapping-codes-ripol-myabi.md)
- [Documentation Swagger / OpenAPI des routes backend](api/swagger-api.md)
- [ADR 001 - Ajout d'un champ](adr/adr-001-ajout-champ.md)

## Objectif

Centraliser la connaissance technique et faciliter :
- l’onboarding
- la maintenance
- l’évolution du projet
