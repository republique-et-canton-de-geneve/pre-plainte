Feature: Formulaire "Informations personnelles" – Service de pré-plainte

  Context:
    Etant donné que je suis sur le formulaire

  Scenario: 1 - Affichage conditionnel des champs "Tiers"
    Etant donné que je sélectionne "Tiers" dans le type de personne
    Alors les champs "Nationalité du tiers", "Nom du tiers" sont affichés
    Et les champs "Raison sociale", "Numéro de TVA" sont masqués

  Scenario: 2 - Affichage conditionnel des champs "Entreprise"
    Etant donné que je sélectionne "Entreprise" dans le type de personne
    Alors les champs "Raison sociale", "Numéro de TVA" sont affichés
    Et les champs "Nationalité du tiers", "Nom du tiers" sont masqués

  Scenario: 3 - Contrôle longueur du Nom (personne signalant)
    Quand je saisis "A" dans le champ "Nom" (personne signalant)
    Alors le message "Le nom doit contenir au moins 2 caractères" s'affiche sous le champ "Nom"

  Scenario: 4 - Contrôle longueur du Prénom (personne signalant)
    Quand je saisis "B" dans le champ "Prénom" (personne signalant)
    Alors le message "Le prénom doit contenir au moins 2 caractères" s'affiche sous le champ "Prénom"

  Scenario: 5 - Adresse postale requise (personne signalant)
    Quand je laisse vide le champ "Adresse" (adresse de la personne signalant)
    Alors le message "L'adresse postale est requise" s'affiche sous le champ "Adresse"

  Scenario: 6 - Localité requise (personne signalant)
    Quand je laisse vide le champ "Localité" (adresse de la personne signalant)
    Alors le message "La localité est requise" s'affiche sous le champ "Localité"

  Scenario: 7 - NPA – 4 chiffres requis (personne signalant)
    Quand je saisis "121" dans le champ "NPA" (adresse de la personne signalant)
    Alors le message "NPA doit contenir 4 chiffres" s'affiche sous le champ "NPA"

  Scenario: 8 - Format du téléphone (personne signalant)
    Quand je saisis "079" dans le champ "Numéro de téléphone" (personne signalant)
    Alors le message "Format de téléphone invalide" s'affiche sous le champ "Numéro de téléphone"

  Scenario: 9 - Format de l'email (personne signalant)
    Quand je saisis "mauvais-mail@example" dans le champ "Email" (personne signalant)
    Alors le message "Format d'email invalide" s'affiche sous le champ "Email"

  Scenario: 10 - Format de l'email – confirmation (personne signalant)
    Quand je saisis "mauvais-mail@example" dans le champ "Confirmation email" (personne signalant)
    Alors le message "Format d'email invalide" s'affiche sous le champ "Confirmation email"

  Scenario: 11 - Nationalité requise (tiers concerné)
    Quand je laisse vide le champ "Nationalité" (identité du tiers concerné)
    Alors le message "La nationalité est requise" s'affiche sous le champ "Nationalité"

  Scenario: 12 - Contrôle longueur du Nom (tiers concerné)
    Etant donné que je sélectionne "Tiers" dans le type de personne
    Quand je saisis "C" dans le champ "Nom" (identité du tiers concerné)
    Alors le message "Le nom doit contenir au moins 2 caractères" s'affiche sous le champ "Nom"

  Scenario: 13 - Contrôle longueur du Prénom (tiers concerné)
    Etant donné que je sélectionne "Tiers" dans le type de personne
    Quand je saisis "D" dans le champ "Prénom" (identité du tiers concerné)
    Alors le message "Le prénom doit contenir au moins 2 caractères" s'affiche sous le champ "Prénom"

  Scenario: 14 - Adresse – minimum 5 caractères (tiers concerné)
    Etant donné que je sélectionne "Tiers" dans le type de personne
    Quand je saisis "Rue" dans le champ "Adresse" (coordonnées du tiers concerné)
    Alors le message "L'adresse doit contenir au moins 5 caractères" s'affiche sous le champ "Adresse"

  Scenario: 15 - Localité requise (tiers concerné)
    Etant donné que je sélectionne "Tiers" dans le type de personne
    Quand je laisse vide le champ "Localité" (coordonnées du tiers concerné)
    Alors le message "La localité est requise" s'affiche sous le champ "Localité"

  Scenario: 16 - NPA – 4 chiffres requis (tiers concerné)
    Etant donné que je sélectionne "Tiers" dans le type de personne
    Quand je saisis "12" dans le champ "NPA" (coordonnées du tiers concerné)
    Alors le message "NPA doit contenir 4 chiffres" s'affiche sous le champ "NPA"

  Scenario: 17 - Bouton "Poursuivre" désactivé en cas d'erreurs
    Etant donné que j'ai des champs en erreur
    Alors le bouton "Poursuivre" est désactivé

  Scenario: 18 - Je peux passer à la page suivante
    Etant donné que je renseigne tous les champs obligatoires avec des valeurs valides
      | Section                              | Champ               | Valeur                  |
      | Identité de la personne signalant    | Nom                 | Martin                  |
      | Identité de la personne signalant    | Prénom              | Anne                    |
      | Identité de la personne signalant    | Genre               | Femme                   |
      | Identité de la personne signalant    | Nationalité         | Suisse                  |
      | Adresse de la personne signalant     | Adresse             | rue du Marché 10        |
      | Adresse de la personne signalant     | NPA                 | 1201                    |
      | Adresse de la personne signalant     | Localité            | Genève                  |
      | Coordonnées de la personne signalant | Numéro de téléphone | 0791234567              |
      | Coordonnées de la personne signalant | Email               | anne.martin@example.org |
      | Coordonnées de la personne signalant | Confirmation email  | anne.martin@example.org |
      | Identité du tiers concerné           | Nom                 | Durand                  |
      | Identité du tiers concerné           | Prénom              | Paul                    |
      | Identité du tiers concerné           | Nationalité         | Suisse                  |
      | Coordonnées du tiers concerné        | Adresse             | 20 avenue du Rhône      |
      | Coordonnées du tiers concerné        | NPA                 | 1207                    |
      | Coordonnées du tiers concerné        | Localité            | Genève                  |
      | Coordonnées du tiers concerné        | Numéro de téléphone | 0221234567              |
    Quand je clique sur "Poursuivre"
    Alors je vois l'étape "Informations sur l'événement"
