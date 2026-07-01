# language: fr
Fonctionnalité: Informations sur l'événement - dommage

  Règle: Les informations du constat de police sont affichées lorsqu'un dommage avec constat présent est déclaré.

  Règle: Le fichier du constat de police est obligatoire lorsqu'un constat est présent.

  Scénario: Dommage avec constat présent - le fichier du constat est obligatoire
    Etant donné que je suis sur l'étape informations sur l'événement avec un dommage et un constat présent
    Alors les champs "Date du constat, Constat de police" sont affichés
    Quand je continue après les informations sur l'événement
    Alors le message "Veuillez télécharger le constat de police" s'affiche
    Et je reste sur l'étape informations sur l'événement
