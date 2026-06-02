# language: fr
Fonctionnalité: Vérification email

  Scénario: Un email invalide bloque l'envoi du code
    Etant donné que je suis sur l'étape vérification email avec un challenge valide
    Quand je saisis l'email de vérification "mauvais-mail"
    Alors le bouton d'envoi du code email est désactivé

  Scénario: Vérification email nominale
    Etant donné que je suis sur l'étape vérification email avec un challenge valide
    Quand je saisis l'email de vérification "anne.martin@example.org"
    Alors le bouton d'envoi du code email est actif
    Quand je demande l'envoi du code email
    Alors la demande de code email est envoyée pour "anne.martin@example.org"
    Et la zone OTP email est affichée
    Quand je saisis le code email "123456"
    Et je continue après la vérification email
    Alors la vérification du code email est envoyée pour "anne.martin@example.org" avec le code "123456"
    Et je vois l'étape "Informations personnelles"

  Scénario: Un code email invalide bloque la progression
    Etant donné que je suis sur l'étape vérification email avec un challenge invalide
    Quand je saisis l'email de vérification "anne.martin@example.org"
    Et je demande l'envoi du code email
    Alors la demande de code email est envoyée pour "anne.martin@example.org"
    Quand je saisis le code email "111111"
    Et je continue après la vérification email
    Alors la vérification invalide du code email est envoyée
    Et le message "Le code de vérification de votre adresse e-mail est invalide. Veuillez saisir le code reçu par e-mail." s'affiche
