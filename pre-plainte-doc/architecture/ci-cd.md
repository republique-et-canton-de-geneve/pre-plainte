# CI/CD

## Objectif

La CI/CD du projet est portée par le fichier `gitlab-ci.yaml` à la racine du dépôt.

Elle sert à :

- récupérer les sources depuis le dépôt GitHub de référence ;
- construire le frontend Vue ;
- construire le backend Spring Boot avec Maven ;
- préparer et construire l'image container ;
- lancer les analyses qualité et sécurité ;
- permettre un déploiement manuel en dev OpenShift ;
- supporter le workflow de release par tag pour publier l'image dans Nexus.

## Déclenchement du pipeline

Le pipeline GitLab ne se déclenche pas sur chaque push de branche.

Les règles `workflow` autorisent uniquement :

- un tag GitLab avec `CI_COMMIT_TAG` ;
- un déclenchement manuel depuis l'interface GitLab avec `CI_PIPELINE_SOURCE == "web"` ;
- un déclenchement par API avec `CI_PIPELINE_SOURCE == "api"` ;
- un déclenchement planifié avec `CI_PIPELINE_SOURCE == "schedule"`.

Tous les autres déclenchements sont ignorés.

## Sources utilisées

Le pipeline GitLab récupère les sources depuis GitHub via le composant `get-remote-sources`.

La variable principale est :

```yaml
REMOTE_URL: github.com/republique-et-canton-de-geneve/pre-plainte.git
```

Le job `checkout_remote_ref` choisit ensuite la référence GitHub à construire :

1. si `SOURCE_REF` est renseigné, cette valeur est utilisée ;
2. sinon, si le pipeline GitLab est lancé sur un tag, le tag `CI_COMMIT_TAG` est utilisé ;
3. sinon, la branche `main` est utilisée.

Le job récupère les tags GitHub, fait le checkout de la référence retenue, supprime le dossier `.git`, puis publie les sources dans un artefact `work/`.

Cette étape est importante : les jobs suivants ne travaillent pas directement sur le dépôt GitLab, mais sur les sources préparées dans `work/`.

## Stages

Le pipeline déclare les stages suivants :

| Stage | Rôle |
| --- | --- |
| `get_sources` | Récupération des sources distantes par composant GitLab partagé. |
| `source_ref` | Sélection de la branche ou du tag GitHub à builder. |
| `prepare` | Construction du frontend. |
| `build` | Construction Maven et préparation du contexte container. |
| `container` | Construction et publication de l'image snapshot ou release. |
| `quality` | Analyses qualité et tests complémentaires. |
| `deploy` | Déploiement manuel en dev OpenShift. |

## Composants GitLab inclus

Le pipeline importe plusieurs composants partagés.

### Vérification des variables

Le composant `git/check-variables@main` est inclus, mais son job est désactivé avec `job_enabled: false`.

### Récupération des sources distantes

Le composant `opensource/get-remote-sources@main` récupère le dépôt défini par `REMOTE_URL` et place les sources dans `work/`.

### Analyse Sonar backend

Le composant `sonar/analyze-with-maven-and-build-dependency@1.1.6` crée le job `sonar_backend`.

Configuration principale :

- clé projet Sonar : `ch.ge.police:11729-PRE-PLAINTE-backend` ;
- exclusions : `**/test/**` ;
- dépendance de build : `build_app` ;
- exécution depuis le dossier `work`.

### Analyse Sonar frontend

Le composant `sonar/analyze-with-sonar-scanner@1.1.6` crée le job `sonar_frontend`.

Configuration principale :

- clé projet Sonar : `ch.ge.police:11729-PRE-PLAINTE-frontend` ;
- base d'analyse : `work` ;
- dossier de travail Sonar : `.scannerwork-frontend` ;
- exclusions : code Java, tests et ressources mail ;
- règles ignorées explicitement pour le Dockerfile et certains imports TypeScript/Vue.

Le job est raccordé aux artefacts de `checkout_remote_ref`.

### Nexus IQ backend

Le composant `nexus-iq/analyze-with-maven@1.1.4` crée le job `nexus_iq_backend`.

Il analyse l'application `11729-PRE-PLAINTE-backend` et dépend de `build_app`.

### Nexus IQ frontend

Le composant `nexus-iq/analyze-with-evaluate@1.1.4` crée le job `nexus_iq_frontend`.

Il analyse l'application `11729-PRE-PLAINTE-frontend`.

### Scan KICS

Le composant `kics/scan@1.0.4` ajoute l'analyse d'infrastructure as code.

### Build container

Le template `container/.gitlab-ci-container.yml` est inclus depuis `$PROJECT_TEMPLATES_GITLABCI_PATH` en version `2.18`.

Le job local `build_image_snapshot_or_release` étend `.build_image_snapshot_or_release`, fourni par ce template. C'est ce template qui porte la logique de build et de publication de l'image container snapshot ou release.

## Variables principales

| Variable | Rôle |
| --- | --- |
| `SOURCE_REF` | Branche ou tag GitHub à builder. Vide signifie tag GitLab si présent, sinon `main`. |
| `SECURITY_MONITORING` | Activation du monitoring sécurité, désactivée par défaut. |
| `DEPLOY_TARGET` | Cible de déploiement. La valeur `dev` rend le job de déploiement dev déclenchable manuellement. |
| `IMAGE_NAME` | Nom de l'image applicative : `ch/ge/police/11729-pre-plainte`. |
| `IMAGE_MAVEN` | Image de build Maven/Node/Git. |
| `IMAGE_CYPRESS` | Image utilisée pour construire le frontend et lancer Cypress. |
| `IMAGE_OPENSHIFT` | Image contenant les outils OpenShift et Helmfile. |
| `OPENSHIFT_URL` | URL de l'API OpenShift calculée avec `$OPENSHIFT_URL_SUFFIX`. |
| `HELMFILE_FILE_PATH` | Chemin du répertoire Helmfile dans le repo d'intégration : `./ci`. |
| `APP_NAME` | Nom applicatif `preplainte`. |

Le cache GitLab conserve :

- `.m2/repository` pour Maven ;
- `.npm-cache` pour npm.

## Jobs applicatifs

### `checkout_remote_ref`

Stage : `source_ref`.

Ce job :

- dépend de `get_remote_sources` ;
- entre dans `work/` ;
- récupère les tags GitHub ;
- choisit `SOURCE_REF`, `CI_COMMIT_TAG` ou `main` ;
- checkout la référence GitHub ;
- supprime `.git` ;
- vérifie la présence de `pom.xml` ;
- publie les sources utiles en artefact.

Les artefacts expirent après un jour.

### `build_ihm`

Stage : `prepare`.

Ce job :

- utilise l'image Cypress ;
- entre dans `work/pre-plainte-ihm` ;
- génère un `.npmrc` pointant vers les repositories Nexus npm ;
- applique les éventuelles variables proxy npm ;
- lance `npm install --no-audit --no-fund --legacy-peer-deps` ;
- lance `npm run build-only` ;
- publie `work/pre-plainte-ihm/dist` en artefact.

Ce job construit uniquement le frontend. Les tests Cypress ne sont pas lancés ici.

### `build_app`

Stage : `build`.

Ce job :

- dépend de `checkout_remote_ref` et `build_ihm` ;
- entre dans `work/` ;
- génère un fichier `.m2-settings.xml` qui force Maven à passer par Nexus ;
- lance Maven avec la phase `clean install` ;
- utilise les options Maven :
  - `--settings .m2-settings.xml` ;
  - `--batch-mode` ;
  - `--errors` ;
  - `--fail-at-end` ;
  - `--show-version` ;
  - `-DinstallAtEnd=true` ;
  - `-DdeployAtEnd=true` ;
  - `-DskipCypress=true` ;
  - `-DskipIhmNpm=true`.

Le frontend ayant déjà été construit par `build_ihm`, Maven ne relance pas le build npm de l'IHM.

Les artefacts publiés contiennent notamment les dossiers `target`, les sources, les `pom.xml` et `sonar-project.properties`.

### `prepare_container_context`

Stage : `build`.

Ce job prépare le contexte utilisé par le build d'image.

Il :

- nettoie la racine du workspace GitLab en conservant `work` et `.git` ;
- copie le contenu de `work/` à la racine ;
- supprime `.git` ;
- vérifie la présence de `argfile.conf` et `Dockerfile` ;
- publie le contexte container en artefact.

Cette étape permet au template container de travailler avec une racine de projet conforme au `Dockerfile`.

### `build_image_snapshot_or_release`

Stage : `container`.

Ce job :

- étend `.build_image_snapshot_or_release` depuis le template container partagé ;
- dépend de `prepare_container_context` ;
- construit l'image applicative ;
- publie l'image comme snapshot ou release selon le contexte du pipeline, notamment la présence d'un tag.

L'image applicative cible est définie par :

```yaml
IMAGE_NAME: ch/ge/police/11729-pre-plainte
```

## Qualité et sécurité

### Sonar

Deux analyses Sonar sont prévues :

- `sonar_backend` pour le backend Maven ;
- `sonar_frontend` pour le frontend Vue/TypeScript.

Ces jobs sont fournis par les composants GitLab partagés.

### Nexus IQ

Deux analyses Nexus IQ sont prévues :

- `nexus_iq_backend` pour les dépendances backend ;
- `nexus_iq_frontend` pour les dépendances frontend.

### KICS

Le scan KICS est ajouté par composant partagé pour détecter des problèmes de configuration infrastructure as code.

### `cypress_tests`

Stage : `quality`.

Ce job est manuel et autorisé à échouer.

Il :

- démarre un service Docker basé sur l'image applicative `latest` ;
- expose ce service sous l'alias `backend` ;
- définit `CYPRESS_BASE_URL=http://backend:8080` ;
- entre dans `work/pre-plainte-cypress` ;
- génère un `.npmrc` pointant vers Nexus npm ;
- installe les dépendances npm ;
- lance `npm run cy:run`.

Comme le job est manuel avec `allow_failure: true`, il ne bloque pas automatiquement le pipeline si personne ne le lance ou s'il échoue.

## Déploiement GitLab en dev

Le job `deploy_to_openshift_dev` est le seul job de déploiement déclaré dans `gitlab-ci.yaml`.

Il :

- est dans le stage `deploy` ;
- utilise l'environnement GitLab `dev` ;
- utilise le `resource_group` `dev` pour éviter plusieurs déploiements concurrents ;
- clone le repo de configuration d'intégration depuis `$INTEGRATION_IMAGE` ;
- se connecte à OpenShift avec `OPENSHIFT_GOCD_TOKEN_DEV` ;
- lance `helmfile -f ./ci/helmfile.yaml --environment dev sync` ;
- redémarre le deployment OpenShift `preplainte` ;
- se déconnecte d'OpenShift.

Ce job n'est disponible que si `DEPLOY_TARGET == "dev"`. Dans ce cas, il est déclenchable manuellement.

## Workflow de release actuel

Le workflow de release ne se limite pas au fichier GitLab CI. Il combine GitHub, GitLab, Nexus, le repo d'intégration et GoCD.

Étapes actuelles :

1. Quand la version est prête à être releasée, créer le tag côté GitHub.
2. Créer le même tag côté GitLab.
3. Déclencher la pipeline GitLab sur ce tag.
4. La pipeline récupère la référence taggée depuis GitHub via `checkout_remote_ref`.
5. La pipeline construit le frontend, le backend et le contexte container.
6. Le job `build_image_snapshot_or_release` publie l'image release dans Nexus via le template container partagé.
7. Mettre à jour le numéro de version dans le repo d'intégration.
8. Lancer le déploiement en recette via GoCD.

Le tag doit exister des deux côtés :

- sur GitHub, car les sources effectivement buildées viennent de `REMOTE_URL` ;
- sur GitLab, car le pipeline utilise `CI_COMMIT_TAG` pour identifier un pipeline de release.

## Points de vigilance

- Si `SOURCE_REF` est renseigné lors d'un lancement manuel, il prend le dessus sur `CI_COMMIT_TAG`.
- Si aucun tag GitLab et aucun `SOURCE_REF` ne sont fournis, la pipeline construit `main`.
- Les artefacts expirent après un jour. Les relances tardives de jobs dépendants peuvent donc échouer si les artefacts ne sont plus disponibles.
- Le build Maven désactive Cypress et le build npm IHM, car ces étapes sont séparées dans la pipeline.
- Le job Cypress est manuel et non bloquant.
- Le déploiement recette n'est pas défini dans `gitlab-ci.yaml` : il se fait via mise à jour du repo d'intégration puis déclenchement GoCD.
- Les variables de credentials et tokens doivent rester dans GitLab CI/CD ou les outils d'exploitation, jamais dans le dépôt.
