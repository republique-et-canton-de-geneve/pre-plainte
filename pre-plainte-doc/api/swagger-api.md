# Documentation Swagger / OpenAPI des routes backend

## Positionnement

Le backend ne déclare pas actuellement de dépendance `springdoc-openapi` ou Swagger UI. Cette documentation est donc une spécification OpenAPI statique, maintenue dans le module documentaire.

Fichier principal :

- [openapi.yaml](openapi.yaml)

Elle peut être ouverte dans Swagger Editor, Redoc ou tout outil compatible OpenAPI 3.

## Limites assumées

La spécification documente les routes exposées par les controllers Spring Boot et les contrats simples connus.

Certains payloads restent volontairement génériques :

- `PrePlainte` est un objet domaine riche avec sous-types d'incident.
- Les routes eSirius manipulent aujourd'hui des `Map<String, Object>`.
- Les routes SQLite debug retournent des lignes dynamiques dépendantes de la table demandée.

Pour une documentation Swagger générée automatiquement et plus précise, il faudrait introduire une dépendance dédiée comme `springdoc-openapi-starter-webmvc-ui` et annoter les DTO publics. Ce n'est pas fait ici afin de ne pas ajouter de dépendance sans nécessité claire.

## Routes métier

| Méthode | Route | Usage |
| --- | --- | --- |
| `GET` | `/api/config` | Configuration publique consommée par le frontend |
| `POST` | `/api/preplainte/draft` | Enregistrement d'un brouillon et envoi du lien de reprise |
| `GET` | `/api/preplainte/draft/{demandeId}` | Récupération d'un brouillon |
| `POST` | `/api/preplainte/soumission` | Soumission définitive d'une pré-plainte |
| `POST` | `/api/preplainte/pdf` | Génération du PDF récapitulatif |
| `POST` | `/api/email-challenges/request` | Demande d'un code de vérification e-mail |
| `POST` | `/api/email-challenges/verify` | Vérification d'un code e-mail |

## Routes eSirius

| Méthode | Route | Usage |
| --- | --- | --- |
| `GET` | `/api/esirius/sites` | Liste des sites |
| `GET` | `/api/esirius/sites/{siteCode}/listServices` | Liste des services d'un site |
| `GET` | `/api/esirius/sites/{siteCode}/services/{serviceId}/plannings/begins/{begin}/periods/{period}/availabilities` | Disponibilités d'un service |
| `POST` | `/api/esirius/appointments` | Création d'un rendez-vous |
| `GET` | `/api/esirius/appointments/{codeRdv}` | Consultation d'un rendez-vous |
| `PUT` | `/api/esirius/appointments` | Modification d'un rendez-vous |
| `DELETE` | `/api/esirius/appointments/{codeRdv}` | Annulation d'un rendez-vous |

## Routes RIPOL

Les routes RIPOL retournent des listes de codes au format :

```json
{
  "code": "string",
  "labelFr": "string",
  "labelDe": "string",
  "groupeType": "string"
}
```

Routes exposées :

- `GET /api/ripol/codes?groupType=...&search=...`
- `GET /api/ripol/group-types`
- `GET /api/ripol/brands?masterValue=...&masterType=...&search=...`
- `GET /api/ripol/models?masterValue=...&search=...`
- `GET /api/ripol/object-types?search=...`
- `GET /api/ripol/phone-brands?search=...`
- `GET /api/ripol/phone-models?brandCode=...&search=...`
- `GET /api/ripol/vehicle-types?search=...`
- `GET /api/ripol/vehicle-brands?vehicleTypeCode=...&search=...`
- `GET /api/ripol/vehicle-models?brandCode=...&search=...`
- `GET /api/ripol/sexes`
- `GET /api/ripol/nationalities?search=...`
- `GET /api/ripol/communes?search=...`
- `GET /api/ripol/lieux-origine?search=...`
- `GET /api/ripol/document-types?search=...`
- `GET /api/ripol/location-types?search=...`
- `GET /api/ripol/modus-operandi?search=...`
- `GET /api/ripol/object-colours?search=...`
- `GET /api/ripol/vehicle-colours?search=...`
- `GET /api/ripol/cantons?search=...`

## Routes techniques

Les routes suivantes sont documentées car elles existent dans le backend, mais elles ne doivent pas être traitées comme des contrats métier publics.

### SQLite debug

- `GET /api/sqlite/tables`
- `GET /api/sqlite/columns?tableName=...`
- `GET /api/sqlite/rows?tableName=...&limit=...`
- `GET /api/sqlite/grouptypes?tableName=...`
- `GET /api/sqlite/rows/by-grouptype?tableName=...&groupType=...&limit=...`

### S3 debug

Ces routes sont disponibles uniquement si `s3.debug.enabled=true`.

- `GET /api/dev-s3/objects?prefix=...&maxKeys=...&continuationToken=...`
- `GET /api/dev-s3/objects/{key}?asAttachment=true`

## Erreurs

Les erreurs gérées par le backend retournent principalement :

```json
{
  "status": 400
}
```

Selon le cas, le corps peut aussi contenir :

- `errorCode`, par exemple pour les challenges e-mail.
- `errorId`, pour les erreurs techniques non gérées.
