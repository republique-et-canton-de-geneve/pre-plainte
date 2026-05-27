# Mapping des codes RIPOL pour myAbi

## Objectif

Documenter la manière dont les codes RIPOL du domaine sont transformés avant l'envoi vers myAbi via le flux eCH-0051 / SEP.

Le mapping est porté par le module `pre-plainte-infrastructure`, principalement dans :

- `ch.ge.police.infrastructure.ech051.mapper`
- `ch.ge.police.infrastructure.ech051.Ech051Constants`
- `ch.ge.police.infrastructure.ech051.Ech051Builder`
- `ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentPayload`
- `ch.ge.police.infrastructure.ech051.dto.Ech0051DocumentXml`

## Format envoyé

Les champs codifiés sont représentés dans le payload intermédiaire par `RipolReference` ou `RipolLocation`.

Une référence RIPOL contient :

- `code` : code fonctionnel envoyé dans `sourceID`
- `label` : libellé envoyé dans `marking`
- `sourceTable` : table de référence attendue par myAbi
- `source` : origine du code, généralement `RIPOL`

Dans le XML eCH-0051, ces valeurs deviennent une structure de ce type :

```xml
<eCH-0051:marking xml:lang="fr">Suisse</eCH-0051:marking>
<eCH-0051:sourceID source="RIPOL" sourceTable="EXT_GPNATI">8100</eCH-0051:sourceID>
```

Le sexe est le seul cas construit avec `source="ISO"` via `RipolReferenceBuilder.ofIso`.

## Règles générales

- Si le code source est absent, la référence n'est pas envoyée.
- Les codes sélectionnés dans l'IHM sont conservés tels quels dans le domaine, puis repris dans les mappers eCH-0051.
- Les libellés sont utilisés pour les balises `marking` et restent distincts du code transmis dans `sourceID`.
- Le nom de table envoyé à myAbi vient de `Ech051Constants.RipolSourceTables`, sauf pour quelques lieux construits directement dans `SuisseEpoliceAddressMapper`.
- Les codes métier constants, par exemple les rôles de personne ou les types de crime, sont centralisés dans `Ech051Constants`.

## Personnes

| Donnée métier | Mapper | Source du code | Table myAbi | Source |
| --- | --- | --- | --- | --- |
| Sexe | `SuisseEpolicePersonMapper.buildSexReference` | `InfosPersonne.getGenreCode()` | `ISO5218` | `ISO` |
| Nationalité | `SuisseEpolicePersonMapper.buildNationalityReference` | `InfosPersonne.getNationaliteCode()` | `EXT_GPNATI` | `RIPOL` |
| Lieu d'origine | `SuisseEpolicePersonMapper.buildPlaceOfOrigin` | `InfosPersonne.getLieuOrigineCode()` | `EXT_GDE_HEIMATORT` | n/a, `RipolLocation` |

Le lieu d'origine est envoyé comme `RipolLocation` avec `code`, `label`, `sourceTable` et `zipCode`. Le `zipCode` reprend actuellement le code du lieu d'origine.

## Adresses et lieux

| Donnée métier | Mapper | Source du code | Table myAbi |
| --- | --- | --- | --- |
| Localité d'adresse | `SuisseEpoliceAddressMapper.buildAddressPlace` | `Adresse.localiteCode()` | `PTT_ORT` |
| Pays d'adresse | `SuisseEpoliceAddressMapper.buildAddressCountry` | `Adresse.paysCode()` | `EXT_GPNATI` |
| Type de lieu de l'événement | `SuisseEpoliceEventMapper.buildLocalityReference` | `IncidentBase.getTypeLieu().code()` | `OERTLICHKEIT` |

Pour une localité d'adresse, le `zipCode` est alimenté avec le NPA quand il est présent. Si seul le NPA est connu, le code reste `null`, le libellé et le `zipCode` reprennent le NPA.

Pour les cybercrimes transactionnels, le pays de l'action est forcé à `9999 / inconnu` avec la table `EXT_GPNATI`.

## Événements

| Donnée myAbi | Mapper | Code envoyé | Table myAbi |
| --- | --- | --- | --- |
| Type de crime vol | `SuisseEpoliceEventMapper.buildTypeOfCrimeReference` | `1000139080` | `STRAFB_HANDL` |
| Type de crime dommage matériel | `SuisseEpoliceEventMapper.buildTypeOfCrimeReference` | `1000144061` | `STRAFB_HANDL` |
| Type de crime cyber commande frauduleuse | `SuisseEpoliceEventMapper.buildTypeOfCrimeReference` | `1000147050` | `STRAFB_HANDL` |
| Type de crime cyber fausse annonce | `SuisseEpoliceEventMapper.buildTypeOfCrimeReference` | `1000146003` | `STRAFB_HANDL` |
| Type de crime cyber achat non reçu | `SuisseEpoliceEventMapper.buildTypeOfCrimeReference` | `1000146100` | `STRAFB_HANDL` |
| Mode opératoire dégradations | `SuisseEpoliceEventMapper.buildModeOperandiReference` | `4000236` | `MODUS_OPERANDI` |
| Mode opératoire tags / graffiti | `SuisseEpoliceEventMapper.buildModeOperandiReference` | `4000900` | `MODUS_OPERANDI` |
| Mode opératoire cyber commande frauduleuse | `SuisseEpoliceEventMapper.buildModeOperandiReference` | `6166537` | `MODUS_OPERANDI` |
| Mode opératoire cyber fausse annonce | `SuisseEpoliceEventMapper.buildModeOperandiReference` | `6163016` | `MODUS_OPERANDI` |
| Mode opératoire cyber achat non reçu | `SuisseEpoliceEventMapper.buildModeOperandiReference` | `6166530` | `MODUS_OPERANDI` |

Ces valeurs ne proviennent pas d'une sélection utilisateur directe. Elles sont déduites du type d'incident ou de la nature du dommage.

## Objets et véhicules

| Donnée métier | Mapper | Source du code | Table myAbi |
| --- | --- | --- | --- |
| Type d'objet | `SuisseEpoliceObjectMapper.buildObjectTypeReference` | `ObjetIncident.getTypeCode()` | `sacheBezeichnung` |
| Marque / fabricant | `SuisseEpoliceObjectMapper.buildBrandReference` | `ObjetIncident.getFabricantCode()` | `sacheMarke` |
| Modèle | `SuisseEpoliceObjectMapper.buildModelReference` | `ObjetIncident.getModeleCode()` | `sacheModell` |
| Couleur objet | `SuisseEpoliceObjectMapper.buildColourReference` | `ObjetIncident.getCouleurCode()` | `sacheFarbe` |
| Couleur secondaire objet | `SuisseEpoliceObjectMapper.buildColourSecondaireReference` | `ObjetIncident.getCouleurSecondaireCode()` | `sacheFarbe` |
| Type de véhicule | `SuisseEpoliceObjectMapper.buildVehicleTypeReference` | `ObjetIncident.getTypeCode()` | `ART_FZ` |
| Couleur véhicule | `SuisseEpoliceObjectMapper.buildVehicleColourReference` | `ObjetIncident.getCouleurCode()` | `FARBE_FZ` |
| Couleur secondaire véhicule | `SuisseEpoliceObjectMapper.buildVehicleColourSecondaireReference` | `ObjetIncident.getCouleurSecondaireCode()` | `FARBE_FZ` |
| Pays de plaque | `SuisseEpoliceObjectMapper.buildPlaquePays` | `ObjetIncident.getPlaquePaysCode()` | `EXT_GPNATI` |
| Canton de plaque | `SuisseEpoliceObjectMapper.buildPlaqueCanton` | `ObjetIncident.getPlaqueCantonCode()` | `SCHILD` |

Le même couple marque / modèle est utilisé pour les objets et les véhicules. Les véhicules changent surtout la table du type et des couleurs.

Pour les dommages matériels sans objet non-véhicule explicite, un objet de secours est créé avec le type `200219 / Dommage matériel` et la table `sacheBezeichnung`.

Pour les cybercrimes nécessitant un objet d'identité, le type d'objet `200300 / carte d'identité` est envoyé avec la table `sacheBezeichnung`.

## Relations

Les rôles utilisés dans les relations sont aussi envoyés comme références codifiées.

| Relation | Code | Libellé | Table myAbi |
| --- | --- | --- | --- |
| Lésé | `1` | `lésé` | `PERSONALIEN_ART` |
| Représentant | `4` | `représentant` | `PERSONALIEN_ART` |
| Personne donnant des renseignements | `7` | `pers. donnant des rens.` | `PERSONALIEN_ART` |
| Personne accusée | `U` | `Personne accusée` | `PERSONALIEN_ART` |
| Objet recherché | `10` | `recherché` | `CODE_SACHE` |
| Véhicule / plaque recherché | `11` | `véhicule/plaque recherché` | `CODE_SACHE` |

Ces valeurs sont déclarées dans `Ech051Constants` et construites dans `SuisseEpoliceRelationsMapper` avec `RipolReferenceBuilder.of`.

## Points de vigilance

- Ajouter une nouvelle référence myAbi dans `Ech051Constants.RipolSourceTables` quand elle correspond à une table stable du référentiel.
- Utiliser `RipolReferenceBuilder.of` pour une référence RIPOL et `RipolReferenceBuilder.ofIso` uniquement pour une référence ISO.
- Vérifier que le champ XML final contient bien `marking`, `sourceID`, `source` et `sourceTable` quand myAbi attend un code référentiel.
- Ne pas confondre les tables objets et véhicules : `sacheFarbe` pour les objets, `FARBE_FZ` pour les véhicules.
- Tester le mapper concerné et, si le XML change, compléter `Ech051BuilderTest`.
