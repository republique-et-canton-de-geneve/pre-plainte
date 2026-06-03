# language: fr
Fonctionnalité: Informations personnelles

  Scénario: Affichage conditionnel des champs Tiers
    Etant donné que je suis sur l'étape informations personnelles
    Et que je sélectionne "Tiers" dans le type de personne
    Alors les champs "Identité du tiers concerné, Coordonnées du tiers concerné" sont affichés
    Et les champs "Informations de l'organisation, Nom de l'organisation" sont masqués

  Scénario: Affichage conditionnel des champs Entreprise
    Etant donné que je suis sur l'étape informations personnelles
    Et que je sélectionne "Entreprise" dans le type de personne
    Alors les champs "Informations de l'organisation, Nom de l'organisation" sont affichés
    Et les champs "Identité du tiers concerné, Coordonnées du tiers concerné" sont masqués

  Scénario: Informations personnelles nominales pour soi-même
    Etant donné que je suis sur l'étape informations personnelles
    Quand je renseigne les informations personnelles nominales pour moi-même
    Et je continue après les informations personnelles
    Alors aucune erreur de champ obligatoire n'est affichée
    Et je vois l'étape "Informations sur l'événement"
