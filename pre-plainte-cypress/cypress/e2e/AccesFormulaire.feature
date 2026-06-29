# language: fr
Fonctionnalité: Accès au formulaire

  Scénario: Démarrer une pré-plainte
    Etant donné que je suis sur l'étape informations générales
    Alors le bouton continuer des informations générales est désactivé
    Etant donné que je coche la confirmation d'identité
    Alors le bouton continuer des informations générales est désactivé
    Etant donné que je coche la confirmation de situation
    Quand je sélectionne le type d'incident "Vol"
    Alors le bouton continuer des informations générales est actif
    Quand je clique sur le bouton continuer des informations générales
    Alors je vois l'étape "Vérification de votre adresse e-mail"
