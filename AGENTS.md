# AGENTS.md

## Contexte projet

Ce projet est une application de pré-plainte avec :

- Backend Spring Boot
- Frontend Vue 3 / TypeScript / Vuetify
- Architecture hexagonale côté backend
- Tests frontend avec Cypress
- Validation frontend avec vee-validate et zod
- Internationalisation avec vue-i18n
- CI/CD GitLab avec analyse Sonar

Le code généré doit rester cohérent avec l’architecture existante, les conventions du projet et les exigences de qualité déjà en place.

## Règles générales

- Répondre et documenter en français.
- Ne pas ajouter de commentaires dans le code généré.
- Ne pas introduire de dépendance sans nécessité claire.
- Préférer des changements ciblés, simples et maintenables.
- Respecter le style existant du fichier modifié.
- Ne pas renommer massivement des classes, méthodes, composants ou fichiers sans raison forte.
- Ne pas modifier les contrats API sans besoin explicite.
- Ne pas ajouter de logique métier dans les controllers ou composants Vue quand elle peut être isolée.
- Éviter les solutions “magiques” ou trop génériques.
- Garder une forte lisibilité du code.
- Prendre en compte SonarQube et éviter les patterns qui déclenchent des warnings évidents.
- Quand une correction touche des tests existants, adapter ou ajouter les tests nécessaires.

## Backend Spring Boot

### Architecture

Le backend suit une architecture hexagonale.

Respecter autant que possible cette séparation :

- `core/domain` : modèles métier, règles métier, exceptions métier
- `core/application` : cas d’usage, services applicatifs, ports
- `infrastructure/adapter/out` : adapters sortants, S3, SQLite, clients externes, stockage
- `ui/controller` : controllers REST, DTO d’entrée/sortie, validation HTTP
- `infrastructure/config` : configuration Spring

Les controllers doivent rester fins :

- Validation HTTP simple
- Mapping éventuel vers le domaine
- Appel au use case
- Construction de réponse HTTP

La logique métier doit aller dans le domaine ou l’application.

### Ports et adapters

Quand une fonctionnalité dépend d’un système externe :

- Créer ou utiliser un port dans le coeur applicatif
- Implémenter l’adapter dans l’infrastructure
- Ne pas injecter directement un client technique dans un use case

Exemples de systèmes externes :

- S3
- SQLite RIPOL
- NotifyService
- API externe
- Filesystem
- Mail

### DTO et domaine

- Ne pas polluer les DTO existants si une information peut être passée proprement via header, paramètre HTTP ou contexte technique.
- Éviter d’ajouter des champs transverses comme `language` dans un DTO métier si ce champ ne fait pas partie de la pré-plainte.
- Garder les mappings explicites.
- Éviter `any` côté Java évidemment, et éviter les objets trop génériques quand un type clair est possible.

### Logging

Utiliser des logs structurés et utiles.

Privilégier des messages avec des clés explicites, par exemple :

```java
log.info("event=preplainte_draft_saved demandeId={}", demandeId);
