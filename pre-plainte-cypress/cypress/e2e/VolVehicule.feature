Feature: Vol d'objet - véhicule

  Context:
    Etant donné que je suis sur la section vol de véhicule

  Scenario: 1 - BVA - aucun type de véhicule renseigné
    Quand je valide l'objet volé
    Alors le message "Le type de l'objet est requis" s'affiche

  Scenario: 2 - BVA - fabricant "Autre" vide puis renseigné avec un caractère
    Quand je renseigne le type de véhicule "Voiture"
    Et je sélectionne "Autre (préciser)" dans l'autocomplétion "Marque"
    Et je valide l'objet volé
    Alors le message "Le champ est requis" s'affiche sous le champ "Précisez le fabricant"
    Quand je saisis "A" dans le champ "Précisez le fabricant"
    Et je valide l'objet volé
    Alors le message "Le modèle est requis" s'affiche

  Scenario: 3 - BVA - modèle "Autre" vide puis renseigné avec un caractère
    Quand je renseigne le type de véhicule "Voiture"
    Et je sélectionne "Toyota" dans l'autocomplétion "Marque"
    Et je sélectionne "Autre (préciser)" dans l'autocomplétion "Modèle"
    Et je valide l'objet volé
    Alors le message "Le champ est requis" s'affiche sous le champ "Précisez le modèle"
    Quand je saisis "B" dans le champ "Précisez le modèle"
    Et je valide l'objet volé
    Alors l'objet volé est enregistré
