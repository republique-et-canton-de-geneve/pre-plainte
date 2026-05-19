import { Given, Then, When } from "@badeball/cypress-cucumber-preprocessor";

const bevisible = "be.visible";
const notbevisible = "not.be.visible";
const bedisabled = "be.disabled";

Given("je suis sur le formulaire", () => {
  cy.visit("/");
});

Then("je vois {string} dans la page", (texte) => {
  cy.contains(texte).should(bevisible);
});

Given('que je sélectionne {string} dans le type de personne', (type) => {
  cy.get('[data-cy="type-personne"]').select(type);
});

Then('les champs {string} sont affichés', (liste) => {
  liste.split(",").forEach((champ) => {
    cy.contains(champ.trim()).should(bevisible);
  });
});

Then('les champs {string} sont masqués', (liste) => {
  liste.split(",").forEach((champ) => {
    cy.contains(champ.trim()).should(notbevisible);
  });
});

When('je saisis {string} dans le champ {string}*', (valeur, champ) => {
  cy.contains("label", champ).parent().find("input, textarea, select").clear().type(valeur);
});

When('je laisse vide le champ {string}*', (champ) => {
  cy.contains("label", champ).parent().find("input, textarea").clear();
});

Then('le message {string} s\'affiche sous le champ {string}', (message, champ) => {
  cy.contains("label", champ).parent().within(() => {
    cy.contains(message).should(bevisible);
  });
});

When('je clique sur {string}', (texte) => {
  cy.contains("button", texte).click();
});

Then('le bouton {string} est désactivé', (texte) => {
  cy.contains("button", texte).should(bedisabled);
});

Then('je vois l\'étape {string}', (etape) => {
  cy.contains(etape).should(bevisible);
});

Given("que je renseigne tous les champs obligatoires avec des valeurs valides", (dataTable) => {
  dataTable.hashes().forEach(({ Champ, Valeur }) => {
    cy.contains("label", Champ).parent().find("input, textarea, select").clear().type(Valeur);
  });
});

Given("que j'ai des champs en erreur", () => {
  cy.contains("label", "Nom").parent().find("input").clear().type("A");
});
