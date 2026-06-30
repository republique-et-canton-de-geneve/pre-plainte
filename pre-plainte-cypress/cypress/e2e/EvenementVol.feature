# language: fr
Fonctionnalité: Informations sur l'événement - vol

  Scénario: BVA - date de début de l'événement absente
    Etant donné que je suis sur l'étape informations sur l'événement avec un vol invalide "date de debut absente"
    Quand je continue après les informations sur l'événement
    Alors le message "La date de début de l'événement est requise" s'affiche sous le champ "Date de début de l'événement"
    Et je reste sur l'étape informations sur l'événement

  Scénario: BVA - fin de l'événement avant le début
    Etant donné que je suis sur l'étape informations sur l'événement avec un vol invalide "fin avant debut"
    Quand je continue après les informations sur l'événement
    Alors le message "La fin de l'événement doit être postérieure au début" s'affiche sous le champ "Heure de fin de l'événement"
    Et je reste sur l'étape informations sur l'événement

  Scénario: BVA - dégradations non renseignées
    Etant donné que je suis sur l'étape informations sur l'événement avec un vol invalide "degradation non renseignee"
    Quand je continue après les informations sur l'événement
    Alors le message "Veuillez indiquer si des dégradations ont été constatées" s'affiche
    Et je reste sur l'étape informations sur l'événement

  Scénario: BVA - catégorie d'objet volé absente
    Etant donné que je suis sur l'étape informations sur l'événement avec un vol invalide "categorie objet absente"
    Quand je continue après les informations sur l'événement
    Alors le message "La catégorie d'objet est requise" s'affiche sous le champ "Catégorie d'objet"
    Et je reste sur l'étape informations sur l'événement

  Scénario: Plaque volée - les champs de plaque sont affichés
    Etant donné que je suis sur l'étape informations sur l'événement avec une plaque volée
    Alors les champs "Pays de la plaque, Numéro de plaque" sont affichés
    Et je reste sur l'étape informations sur l'événement
