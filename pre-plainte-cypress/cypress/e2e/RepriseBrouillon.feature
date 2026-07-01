# language: fr
Fonctionnalité: Reprise de brouillon

  Règle: Une pré-plainte sauvegardée est restaurée lorsqu'un demandeId valide est présent dans l'URL.

  Scénario: Reprendre une pré-plainte avec un demandeId dans l'URL
    Etant donné que je reprends un brouillon depuis l'URL
    Alors le brouillon est restauré dans le parcours
