# language: fr
Fonctionnalité: Parcours nominal complet

  Scénario: Soumettre une pré-plainte nominale pour un vol simple
    Etant donné que je démarre un parcours nominal complet
    # Etape 1 -> disclaimers
    Quand je coche la confirmation d'identité
    Et je coche la confirmation de situation
    Et je clique sur le bouton continuer des informations générales
    # Etape 2 -> Vérification du mail
    Quand je renseigne et je vérifie mon adresse e-mail
    # Etape 3 -> Informations personnelles
    Quand je renseigne les informations personnelles nominales pour moi-même
    Et je continue après les informations personnelles
    # Etape 4 -> Informations de l'evenement
    Quand je renseigne un vol simple nominal
    # Etape 5 -> Je selectionne un creneau
    Quand je sélectionne le premier créneau disponible
    Et je continue après le rendez-vous
    # Etape 6 -> Recap + soumission
    Quand je soumets la pré-plainte
    Alors le rendez-vous est créé
    Et je vois la validation finale
