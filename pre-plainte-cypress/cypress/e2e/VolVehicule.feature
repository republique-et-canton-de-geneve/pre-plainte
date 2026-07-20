# language: fr
Fonctionnalité: Vol d'objet - véhicule

  Règle: Le type de véhicule est obligatoire pour enregistrer un véhicule volé.

  Règle: Le fabricant doit être précisé lorsque la valeur "Autre" est sélectionnée.

  Règle: Le modèle doit être précisé lorsque la valeur "Autre" est sélectionnée.

  Contexte:
    Etant donné que je suis sur la section vol de véhicule

  Scénario: BVA - aucun type de véhicule renseigné
    Quand je valide l'objet volé
    Alors le message "Le type de l'objet est requis" s'affiche

  Scénario: BVA - fabricant "Autre" vide puis renseigné avec un caractère
    Quand je renseigne le type de véhicule "Voiture"
    Et je sélectionne "Autre (préciser)" dans l'autocomplétion "Marque"
    Et je valide l'objet volé
    Alors le message "Veuillez préciser le fabricant" s'affiche sous le champ "Précisez le fabricant"
    Quand je saisis "A" dans le champ "Précisez le fabricant"
    Et je valide l'objet volé
    Alors aucune erreur de champ obligatoire n'est affichée

  Scénario: BVA - modèle "Autre" vide puis renseigné avec un caractère
    Quand je renseigne le type de véhicule "Voiture"
    Et je sélectionne "Toyota" dans l'autocomplétion "Marque"
    Et je sélectionne "Autre (préciser)" dans l'autocomplétion "Modèle"
    Et je valide l'objet volé
    Alors le message "Veuillez préciser le modèle" s'affiche sous le champ "Précisez le modèle"
    Quand je saisis "B" dans le champ "Précisez le modèle"
    Et je valide l'objet volé
    Alors l'objet volé est enregistré
