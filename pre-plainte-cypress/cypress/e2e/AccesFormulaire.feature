# language: fr
Fonctionnalité: Accès au formulaire

  Règle: La confirmation d'identité est obligatoire pour démarrer une pré-plainte.

  Règle: La confirmation de situation est obligatoire pour démarrer une pré-plainte.

  Règle: La confirmation d'effraction est obligatoire pour démarrer une pré-plainte.

  Règle: Le type d'incident est obligatoire pour démarrer une pré-plainte.

  Règle: Des informations générales valides permettent d'accéder à la vérification de l'adresse e-mail.

  Scénario: Démarrer une pré-plainte
    Etant donné que je suis sur l'étape informations générales
    Alors le contenu des informations générales est masqué
    Etant donné que je coche la confirmation d'identité
    Etant donné que je coche la confirmation de situation
    Etant donné que je coche la confirmation d'effraction
    Quand je confirme les conditions de pré-plainte
    Quand je sélectionne le type d'incident "Vol"
    Alors le bouton continuer des informations générales est actif
    Quand je clique sur le bouton continuer des informations générales
    Alors je vois l'étape "Vérification de votre adresse e-mail"
