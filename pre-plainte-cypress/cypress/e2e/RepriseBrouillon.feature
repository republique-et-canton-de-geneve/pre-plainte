# language: fr
Fonctionnalité: Reprise de brouillon

  Règle: Une pré-plainte sauvegardée est restaurée lorsqu'un demandeId valide est présent dans l'URL.

  Scénario: Reprendre une pré-plainte avec un demandeId dans l'URL
    Etant donné que je reprends un brouillon depuis l'URL
    Alors le brouillon est restauré dans le parcours

  Règle: Une pré-plainte locale en cours propose de reprendre ou de recommencer.

  Scénario: Continuer un brouillon local
    Etant donné qu'un brouillon local est présent
    Alors le dialogue de reprise de brouillon local est affiché
    Quand je choisis de continuer le brouillon local
    Alors je reste sur l'étape des informations personnelles

  Scénario: Recommencer depuis un brouillon local
    Etant donné qu'un brouillon local est présent
    Alors le dialogue de reprise de brouillon local est affiché
    Quand je choisis de recommencer le brouillon local
    Alors je repars depuis les informations générales
