# language: fr
Fonctionnalité: Parcours nominal complet

  Scénario: Soumettre une pré-plainte nominale pour un vol simple
    Etant donné que je démarre un parcours nominal complet
    Quand je coche la confirmation d'identité
    Et je coche la confirmation de situation
    Et je clique sur le bouton continuer des informations générales
    Alors je vois l'étape "Vérification de votre adresse e-mail"
    Quand je renseigne et je vérifie mon adresse e-mail
    Alors je vois l'étape "Informations personnelles"
    Quand je renseigne les informations personnelles nominales pour moi-même
    Et je continue après les informations personnelles
    Alors je vois l'étape "Informations sur l'événement"
    Quand je renseigne un vol simple nominal
    Alors je vois l'étape "Sélection du poste de police"
    Quand je sélectionne le premier créneau disponible
    Et je continue après le rendez-vous
    Alors le récapitulatif du parcours nominal est affiché
    Quand je soumets la pré-plainte
    Alors la pré-plainte est soumise
    Et le rendez-vous est créé
    Et je vois la validation finale
