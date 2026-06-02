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
    Alors la demande de code email est prise en compte
    Quand je saisis le code email "123456"
    Et je continue après la vérification email
    Alors je vois l'étape "Informations personnelles"
