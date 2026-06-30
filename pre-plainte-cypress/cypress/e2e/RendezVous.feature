# language: fr
Fonctionnalité: Rendez-vous

  Scénario: BVA - aucun créneau sélectionné
    Etant donné que je suis sur l'étape rendez-vous avec un vol simple valide
    Quand je tente de continuer après le rendez-vous
    Alors le message "Vous n'avez pas sélectionné de créneau horaire parmi ceux disponibles. Veuillez en sélectionner un." s'affiche
    Et je reste sur l'étape rendez-vous

  Scénario: Filtrer les services par type d'incident cybercrime
    Etant donné que je suis sur l'étape rendez-vous avec des services pour chaque type d'incident et un "cybercrime"
    Alors le service de rendez-vous "Service cybercrime" est proposé
    Et le service de rendez-vous "Service vol" n'est pas proposé
    Et le service de rendez-vous "Service dommage" n'est pas proposé
