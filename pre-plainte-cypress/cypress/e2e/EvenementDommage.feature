# language: fr
Fonctionnalité: Informations sur l'événement - dommage

  Règle: La question du constat de police n'est pas affichée dans l'étape informations sur l'événement.

  Règle: Les photos du dommage sont recommandées mais restent optionnelles.

  Scénario: Dommage - les photos sont optionnelles
    Etant donné que je suis sur l'étape informations sur l'événement avec un dommage valide
    Alors les champs "Date du constat, Constat de police" sont masqués
    Et le message "Fortement recommandé" s'affiche
    Quand je continue après les informations sur l'événement
    Alors je vois l'étape "Date et lieu de rendez-vous"
