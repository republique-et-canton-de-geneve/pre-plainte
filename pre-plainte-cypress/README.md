# Pre-plainte Cypress : Tests d'Intégration

Ce module exécute des scénarios de test écrits par le métier pour simuler le
remplissage du questionnaire de manière automatique et contrôler les résultats de
chaque scénario.

## Structure

La configuration de cet outil repose sur plusieurs composants :

* [Cypress][cypress-doc] : Un framework JavaScript open-source pour tester
  l'application web.
* [Cucumber][cucumber-doc] : Permet d'écrire et de coder des tests BDD (Behavior
  Driven Development).
* [cypress-cucumber-preprocessor][cypress-cucumber-pp] : Intègre les tests Cucumber
  avec Cypress en utilisant la syntaxe [Gherkin][gherkin-doc].


## Tests Cypress + Cucumber

Ce projet utilise des [commandes personnalisées][cypress-custom-commands] et des
correspondances de phrases Cucumber définies
dans [cypress/e2e/cucumber-grammar.js](cypress/e2e/step_definitions). Les tests
comprennent des vérifications et des interactions telles que :

- Vérifier l'affichage ou non des questions.
- Vérifier le libellé des questions.
- Vérifier si une question est obligatoire ou non.
- Vérifier les réponses possibles aux questions.
- Vérifier l'état d'un bouton.
- Interagir avec les éléments, comme remplir un champ texte, cocher une checkbox,
  sélectionner la valeur d'une liste et cliquer sur un bouton.
- Manipuler des données avec un tableau DataTable de Cucumber.

## Exécution


Pour ouvrir l'interface graphique de Cypress, utilisez la commande suivante :

```bash
npm run cy:open
```

## Astuces

Si l'installation de Cypress échoue, vous pouvez l'installer manuellement en
téléchargeant le binaire
depuis [https://download.cypress.io/desktop](https://download.cypress.io/desktop).
Assurez-vous de renseigner la variable d'environnement `CYPRESS_INSTALL_BINARY` avant
de lancer l'installation :

```bash
export CYPRESS_INSTALL_BINARY=/home/$USER/Downloads/cypress.zip
npm install
```

Si la méthode de la variable d'environement ne fonctionne pas, on peut extraire le dossier Cypress dans AppData\Local\Cypress\Cache\{{version}}

Il est possible que le lancement des tests ne fonctionne pas avec Chrome et Edge, il faut dans ce cas utiliser Electron.



[cypress-doc]: https://docs.cypress.io/

[cypress-custom-commands]: https://docs.cypress.io/api/cypress-api/custom-commands.html

[cypress-cucumber-pp]: https://www.npmjs.com/package/cypress-cucumber-preprocessor

[cucumber-doc]: https://cucumber.io/docs/cucumber/

[cucumber-DataTable]: https://github.com/cucumber/cucumber-js/blob/master/src/models/data_table.ts

[gherkin-doc]: https://cucumber.io/docs/gherkin/reference/


