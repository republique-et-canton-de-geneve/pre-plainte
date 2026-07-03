# language: fr
Fonctionnalité: Informations sur l'événement - dommage

  Règle: La question du constat de police n'est pas affichée dans l'étape informations sur l'événement.

  Règle: Les photos du dommage sont recommandées mais restent optionnelles.

  Scénario: Dommage sans constat - les photos sont recommandées sous la question constat
    Etant donné que je suis sur l'étape informations générales
    Quand je coche la confirmation d'identité
    Et je coche la confirmation de situation
    Et je coche la confirmation d'effraction
    Et je confirme les conditions de pré-plainte
    Et je sélectionne le type d'incident "Dommage"
    Et je sélectionne le type de dommage "Dommage sur un bâtiment (maison, appartement, local commercial...)"
    Et je réponds "Non" à la question "Un constat de police a-t-il été rédigé ?"
    Alors le message "Fortement recommandé" s'affiche

  Scénario: Dommage - le constat n'est pas demandé dans l'événement
    Etant donné que je suis sur l'étape informations sur l'événement avec un dommage valide
    Alors les champs "Date du constat, Constat de police" sont masqués
    Quand je continue après les informations sur l'événement
    Alors je vois l'étape "Date et lieu de rendez-vous"
