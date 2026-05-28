import { Given, Then, When } from "@badeball/cypress-cucumber-preprocessor";

const bevisible = "be.visible";
const notbevisible = "not.be.visible";
const bedisabled = "be.disabled";

const ripol = (code, labelFr) => ({ code, labelFr, labelDe: labelFr, groupeType: "" });

const fieldRoot = (champ) => cy.contains("label", champ).parents(".v-input").first();
const fieldInput = (champ) => fieldRoot(champ).find("input, textarea").first();

const stubRipolPourVolVehicule = () => {
  cy.intercept("GET", "**/api/ripol/vehicle-types*", [
    ripol("010", "Voiture"),
    ripol("060", "Moto"),
  ]);
  cy.intercept("GET", "**/api/ripol/brands*", [
    ripol("TOYOTA", "Toyota"),
  ]);
  cy.intercept("GET", "**/api/ripol/models*", [
    ripol("COROLLA", "Corolla"),
  ]);
  cy.intercept("GET", "**/api/ripol/vehicle-colours*", [
    ripol("NOIR", "Noir"),
  ]);
  cy.intercept("GET", "**/api/ripol/nationalities*", [
    ripol("8100", "Suisse"),
  ]);
  cy.intercept("GET", "**/api/ripol/location-types*", [
    ripol("RUE", "Rue"),
  ]);
  cy.intercept("GET", "**/api/ripol/sexes*", [
    ripol("2", "Femme"),
  ]);
  cy.intercept("GET", "**/api/ripol/document-types*", [
    ripol("carte_identite", "Carte d'identité"),
  ]);
  cy.intercept("GET", "**/api/ripol/object-types*", []);
  cy.intercept("GET", "**/api/ripol/object-colours*", []);
  cy.intercept("GET", "**/api/ripol/cantons*", [
    ripol("GE", "Genève"),
  ]);
};

const donneesEvenementVolVehicule = {
  nationalite: { code: "8100", label: "Suisse" },
  typeIncident: "vol",
  dateDebutEvenement: "20.05.2026",
  heureDebutEvenement: "10:00",
  dateFinEvenement: "20.05.2026",
  heureFinEvenement: "11:00",
  adresseConnue: false,
  adresseLesee: true,
  paysEvenement: "8100",
  volDansVehicule: false,
  categorieObjet: "vehicule",
  sousCategorie: "",
  typeObjet: null,
  fabricant: null,
  fabricantAutre: "",
  modele: null,
  modeleAutre: "",
  isVehicle: true,
  avezVousDegradation: false,
  objetsVolesValides: [],
};

Given("je suis sur le formulaire", () => {
  cy.visit("/");
});

Given("je suis sur la section vol de véhicule", () => {
  stubRipolPourVolVehicule();
  cy.visit("/", {
    onBeforeLoad(win) {
      win.localStorage.setItem("pp-step", "4");
      win.localStorage.setItem("pp-data", JSON.stringify(donneesEvenementVolVehicule));
    },
  });
  cy.contains("Informations sur l'événement").should(bevisible);
  cy.contains("Ajouter un objet volé").should(bevisible);
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
  fieldInput(champ).clear({ force: true }).type(valeur, { force: true });
});

When("je renseigne le type de véhicule {string}", (typeVehicule) => {
  cy.get(".css-fallback-native-select")
    .contains("label", "Sous-catégorie")
    .parent()
    .find("select")
    .select("voitures", { force: true });
  fieldInput("Type de l'objet").click({ force: true }).clear({ force: true }).type(typeVehicule, { force: true });
  cy.contains(".v-list-item-title", typeVehicule).click({ force: true });
});

When("je sélectionne {string} dans l'autocomplétion {string}", (valeur, champ) => {
  fieldInput(champ).click({ force: true }).clear({ force: true }).type(valeur, { force: true });
  cy.contains(".v-list-item-title", valeur).click({ force: true });
});

When("je valide l'objet volé", () => {
  cy.contains("button", "Valider l'objet volé").click();
});

When('je laisse vide le champ {string}*', (champ) => {
  cy.contains("label", champ).parent().find("input, textarea").clear();
});

Then('le message {string} s\'affiche sous le champ {string}', (message, champ) => {
  fieldRoot(champ).within(() => {
    cy.contains(message).should(bevisible);
  });
});

Then("le message {string} s'affiche", (message) => {
  cy.contains(message).should(bevisible);
});

Then("l'objet volé est enregistré", () => {
  cy.contains("Ajouter un autre objet volé").should(bevisible);
  cy.contains("Ajouter un objet volé").should(notbevisible);
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
