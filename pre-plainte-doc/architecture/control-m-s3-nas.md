# Control-M S3 vers NAS police

## Objectif

L'application dépose les XML eCH-0051 sur S3 lors de la soumission finale d'une pré-plainte.

Une chaîne Control-M récupère ensuite ces fichiers XML et les déplace vers le NAS police.

## Fonctionnement

- La chaîne récupère les fichiers XML déposés sur S3.
- Elle déplace ces fichiers vers le NAS police.
- Elle tourne toutes les 15 minutes.
- L'arrivée du XML sur le NAS est donc asynchrone par rapport à la soumission de la pré-plainte.

## Environnements

La chaîne Control-M n'est active qu'à partir de l'environnement de recette.

En développement, l'importation dans MyABI doit donc être faite manuellement à partir du XML généré.

## Responsabilités

| Composant | Responsabilité |
| --- | --- |
| Backend pré-plainte | Génère le XML eCH-0051 et le dépose sur S3. |
| S3 | Sert de zone tampon de dépôt. |
| Control-M | Récupère périodiquement les XML et les déplace. |
| NAS police | Reçoit les XML consommés par la chaîne police / MyABI. |

## Points de vigilance

- Un `ech051_export_success` confirme seulement le dépôt S3, pas l'arrivée sur le NAS.
- Un délai jusqu'à 15 minutes est normal avant traitement par Control-M.
- En dev, l'absence d'import automatique MyABI est normale.
- Les XML contiennent des données sensibles et doivent être manipulés comme des documents métier.
