# language: fr
Fonctionnalité: Informations personnelles

  Règle: Selon le type de représentation (tiers, entreprise), on affiche le formulaire correspondant.

  Règle: Des informations personnelles valides permettent de passer à l'étape suivante.

  Règle: La date de naissance doit correspondre à un âge compris entre 16 et 120 ans

  Règle: Un titre de séjour est obligatoire pour une nationalité non suisse

  Règle: Le numéro du document d'identité est obligatoire

  Règle: Le format du numéro de téléphone est vérifié

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

  Scénario: BVA - date de naissance inférieure à 16 ans
    Etant donné que je suis sur l'étape informations personnelles avec des données invalides "age inferieur a 16 ans"
    Quand je continue après les informations personnelles
    Alors le message "L'âge doit être entre 16 et 120 ans" s'affiche sous le champ "Date de naissance"
    Et je reste sur l'étape informations personnelles

  Scénario: BVA - nationalité non suisse sans titre de séjour
    Etant donné que je suis sur l'étape informations personnelles avec des données invalides "nationalite etrangere sans titre de sejour"
    Quand je continue après les informations personnelles
    Alors le message "Le titre de séjour est requis" s'affiche sous le champ "Titre de séjour"
    Et je reste sur l'étape informations personnelles

  Scénario: BVA - numéro de document manquant
    Etant donné que je suis sur l'étape informations personnelles avec des données invalides "numero de document manquant"
    Quand je continue après les informations personnelles
    Alors le message "Le numéro de document est requis" s'affiche sous le champ "Numéro de carte d'identité"
    Et je reste sur l'étape informations personnelles

  Scénario: BVA - numéro de téléphone invalide
    Etant donné que je suis sur l'étape informations personnelles avec des données invalides "telephone invalide"
    Quand je continue après les informations personnelles
    Alors le message "Veuillez saisir un numéro valide (ex: 078 905 44 34)" s'affiche sous le champ "Numéro de téléphone"
    Et je reste sur l'étape informations personnelles
