# language: fr
Fonctionnalité: Informations sur l'événement - cybercrime

  Règle: Les champs spécifiques à un achat non reçu sont affichés pour ce type de cybercriminalité.

  Règle: L'IBAN ou le compte du bénéficiaire est obligatoire pour un paiement par IBAN.

  Scénario: Achat non reçu - le paiement par IBAN demande le compte bénéficiaire
    Etant donné que je suis sur l'étape informations sur l'événement avec un cybercrime achat non reçu
    Alors les champs "Montant du délit (CHF), Article non livré, Moyen de paiement utilisé" sont affichés
    Et les champs "IBAN / compte du bénéficiaire" sont affichés
    Quand je continue après les informations sur l'événement
    Alors le message "L'IBAN / compte du bénéficiaire est requis" s'affiche sous le champ "IBAN / compte du bénéficiaire"
    Et je reste sur l'étape informations sur l'événement
