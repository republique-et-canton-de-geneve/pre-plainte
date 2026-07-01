# Métriques ROI - Tests frontend et documentation vivante

Cette page donne des métriques simples et vérifiables pour présenter à la hiérarchie le résultat obtenu grâce à l'ouverture du projet, à l'utilisation de l'IA et à la mise en place d'une couverture frontend documentée.

## Chiffres clés

| Indicateur | Avant | Après |
| --- | ---: | ---: |
| Règles métier frontend documentées depuis les tests Vitest | 0 | 30 |
| Règles métier de parcours documentées depuis Cypress | 0 | 29 |
| Scénarios Cypress automatisés | 0 | 24 |
| Domaines fonctionnels couverts par Cypress | 0 | 9 |
| Documentation générée automatiquement depuis les tests | Non | Oui |

## Chiffres mesurés

| Métrique | Valeur |
| --- | ---: |
| Règles et cas métier frontend documentés | 59 |
| Règles Vitest documentées | 30 |
| Règles Cypress documentées | 29 |
| Scénarios Cypress automatisés | 24 |
| Étapes Gherkin Cypress automatisées | 119 |
| Fichiers `.feature` couverts | 9 |

Les 59 règles et cas métier correspondent à la somme des règles documentées depuis les tests unitaires frontend et des règles de parcours Cypress.

## Résultat obtenu

Le résultat ne se limite pas au nombre de tests ajoutés. Les tests et la documentation partagent maintenant les mêmes sources, ce qui rend la couverture visible, contrôlable et maintenable.

| Apport | Effet concret |
| --- | --- |
| Tests Vitest générés depuis les règles métier | Les validations fines du formulaire sont testées automatiquement. |
| Tests Cypress écrits en Gherkin | Les parcours frontend critiques sont rejoués automatiquement. |
| Documentation VitePress générée | Le métier peut lire ce qui est couvert sans lire le code. |
| Règles `Règle:` dans les `.feature` | La couverture Cypress reste lisible et maintenable. |
| Génération au build | La documentation publiée reste synchronisée avec les tests. |

## Estimation du temps équivalent

Ces estimations donnent un ordre de grandeur du temps qu'une réalisation manuelle classique aurait probablement nécessité.

| Poste | Hypothèse prudente | Estimation |
| --- | --- | ---: |
| Conception et écriture de 24 scénarios Cypress | 0,5 à 1 jour par scénario | 12 à 24 j/h |
| Stabilisation des données, stubs et parcours | 0,25 à 0,5 jour par scénario | 6 à 12 j/h |
| Formalisation des règles métier documentées | 59 règles à qualifier, structurer et relire | 5 à 10 j/h |
| Mise en place de la génération documentaire | scripts, intégration VitePress, navigation, build | 2 à 4 j/h |
| Total estimé | hors gains futurs de maintenance | 25 à 50 j/h |

## Gain récurrent sur la non-régression

Chaque campagne manuelle évitée représente aussi un gain récurrent.

| Hypothèse | Calcul | Gain |
| --- | --- | ---: |
| Test manuel prudent | 24 scénarios x 5 minutes | 2 h par campagne |
| Test manuel réaliste | 24 scénarios x 10 minutes | 4 h par campagne |
| 2 campagnes par mois | 2 h à 4 h x 2 x 12 | 48 h à 96 h par an |

Ce gain récurrent ne prend pas en compte le temps évité lors de l'analyse d'anomalies, de la reprise de documentation ou des échanges métier pour clarifier ce qui est réellement couvert.
