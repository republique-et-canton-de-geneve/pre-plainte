import { Given, Then, When } from "@badeball/cypress-cucumber-preprocessor";
import { clearField, fieldInput, fieldRoot, fillField, selectAutocomplete, selectNative } from "../../support/helpers/vuetify";
import { ripol, ripolSelection, stubRipol } from "../../support/stubs/ripol";
import { stubEmailChallengeVerificationOk } from "../../support/stubs/email-challenge";
import { stubEsiriusOk } from "../../support/stubs/esirius";
import { stubSoumissionPrePlainteOk } from "../../support/stubs/pre-plainte";
import { declarantSuisseValide, donneesEmailVerifie } from "../../support/data/pre-plainte";

const bevisible = "be.visible";
const notbevisible = "not.be.visible";
const bedisabled = "be.disabled";
const beenabled = "be.enabled";

const donneesEvenementVolVehicule = {
  nationalite: ripolSelection("8100", "Suisse"),
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

const valeursTypePersonne = {
  "Moi-même": "MOI_MEME",
  "Moi-meme": "MOI_MEME",
  Tiers: "TIERS",
  Entreprise: "ENTREPRISE",
};

const libellesChamps = {
  "Coordonnées du tiers concerné": "Coordonnées du tiers",
};

const choisirRadio = (question, option) => {
  cy.contains("legend", question)
    .parents("fieldset")
    .first()
    .contains(".v-label", option)
    .click({ force: true });
};

Given("je suis sur le formulaire", () => {
  stubRipol();
  cy.visit("/");
});

Given("je démarre un parcours nominal complet", () => {
  stubRipol({
    objectTypes: [ripol("713100", "Téléphone mobile")],
    brands: [],
    models: [],
    objectColours: [ripol("NOIR", "Noir")],
  });
  stubEmailChallengeVerificationOk();
  stubEsiriusOk();
  stubSoumissionPrePlainteOk();
  cy.demarrerPrePlainteAEtape(1);
});

Given("je suis sur l'étape informations générales", () => {
  stubRipol();
  cy.demarrerPrePlainteAEtape(1);
});

Given("je suis sur l'etape informations generales", () => {
  stubRipol();
  cy.demarrerPrePlainteAEtape(1);
});

Given("je suis sur la section vol de véhicule", () => {
  stubRipol({
    objectTypes: [],
    objectColours: [],
  });
  cy.demarrerPrePlainteAEtape(4, donneesEvenementVolVehicule);
  cy.contains("Informations sur l'événement").should(bevisible);
  cy.contains("Ajouter un objet volé").should(bevisible);
});

Given("je suis sur l'étape {int}", (etape) => {
  stubRipol();
  cy.demarrerPrePlainteAEtape(etape);
});

Given("je suis sur l'étape {int} avec les données", (etape, dataTable) => {
  stubRipol();
  cy.demarrerPrePlainteAEtape(etape, dataTable.rowsHash());
});

Given("je suis sur l'étape informations personnelles", () => {
  stubRipol();
  cy.demarrerPrePlainteAEtape(3, donneesEmailVerifie, { emailChallengeKey: "challenge-cypress" });
});

Given("je suis sur l'etape informations personnelles", () => {
  stubRipol();
  cy.demarrerPrePlainteAEtape(3, donneesEmailVerifie, { emailChallengeKey: "challenge-cypress" });
});

Given("je suis sur l'étape informations personnelles avec un déclarant suisse valide", () => {
  stubRipol();
  cy.demarrerPrePlainteAEtape(3, declarantSuisseValide, { emailChallengeKey: "challenge-cypress" });
});

Given("je suis sur l'etape informations personnelles avec un declarant suisse valide", () => {
  stubRipol();
  cy.demarrerPrePlainteAEtape(3, declarantSuisseValide, { emailChallengeKey: "challenge-cypress" });
});

Given("que je sélectionne {string} dans le type de personne", (type) => {
  cy.get('[data-cy="type-personne-native"]').select(valeursTypePersonne[type] ?? type, { force: true });
});

Given("je sélectionne {string} dans le type de personne", (type) => {
  cy.get('[data-cy="type-personne-native"]').select(valeursTypePersonne[type] ?? type, { force: true });
});

Given("que je coche la confirmation d'identité", () => {
  cy.get('[data-cy="confirmation-identite"]').click("topRight", { force: true });
});

Given("je coche la confirmation d'identité", () => {
  cy.get('[data-cy="confirmation-identite"]').click("topRight", { force: true });
});

Given("que je coche la confirmation de situation", () => {
  cy.get('[data-cy="confirmation-situation"]').click("topRight", { force: true });
});

Given("je coche la confirmation de situation", () => {
  cy.get('[data-cy="confirmation-situation"]').click("topRight", { force: true });
});

Then("je vois {string} dans la page", (texte) => {
  cy.contains(texte).should(bevisible);
});

Then("les champs {string} sont affichés", (liste) => {
  liste.split(",").forEach(champ => {
    const libelle = champ.trim();
    cy.contains(libellesChamps[libelle] ?? libelle).should(bevisible);
  });
});

Then("les champs {string} sont masqués", (liste) => {
  liste.split(",").forEach(champ => {
    cy.contains(champ.trim()).should("not.exist");
  });
});

When("je saisis {string} dans le champ {string}*", (valeur, champ) => {
  fillField(champ, valeur);
});

When("je saisis {string} dans le champ {string}", (valeur, champ) => {
  fillField(champ, valeur);
});

When("je renseigne le type de véhicule {string}", (typeVehicule) => {
  cy.get(".css-fallback-native-select")
    .contains("label", "Sous-catégorie")
    .parent()
    .find("select")
    .select("voitures", { force: true });
  fieldInput("Type de l'objet").click({ force: true });
  fieldInput("Type de l'objet").clear({ force: true });
  fieldInput("Type de l'objet").type(typeVehicule, { force: true });
  cy.contains(".v-list-item-title", typeVehicule).click({ force: true });
});

When("je sélectionne {string} dans l'autocomplétion {string}", (valeur, champ) => {
  selectAutocomplete(champ, valeur);
});

When("je valide l'objet volé", () => {
  cy.get('[data-cy="objet-vole-valider"]').click();
});

When("je laisse vide le champ {string}*", (champ) => {
  clearField(champ);
});

When("je clique sur {string}", (texte) => {
  cy.contains("button", texte).click();
});

When("je clique sur le bouton continuer des informations générales", () => {
  cy.get('[data-cy="continuer-informations-generales"]').filter(":visible").first().click();
});

When("le challenge email est considéré comme vérifié", () => {
  cy.demarrerPrePlainteAEtape(3, donneesEmailVerifie, { emailChallengeKey: "challenge-cypress" });
});

When("je renseigne les informations personnelles nominales pour moi-même", () => {
  cy.get('[data-cy="type-personne-native"]').select("MOI_MEME", { force: true });
  fillField("Numéro de téléphone", "0791234567");
  fillField("Nom", "Martin");
  fillField("Prénom", "Anne");
  selectAutocomplete("Genre", "Féminin");
  selectAutocomplete("Nationalité", "Suisse");
  fillField("Date de naissance", "15.04.1985");
  fillField("Adresse", "Rue du Marche 10");
  fillField("Numéro de rue", "10");
  fillField("NPA", "1201");
  fillField("Localité", "Geneve");
  cy.get('[data-cy="type-document-identite-native"]').select("carte_identite", { force: true });
  fillField("Numéro de carte d'identité", "ID1234567");
});

When("je continue après les informations personnelles", () => {
  cy.get('[data-cy="continuer-informations-personnelles"]').filter(":visible").first().click();
});

When("je renseigne un vol simple nominal", () => {
  cy.get('[data-cy="type-incident-native"]').select("vol", { force: true });
  fillField("Date de début de l'événement", "20.05.2026");
  fillField("Heure de début de l'événement", "10:00");
  fillField("Date de fin de l'événement", "20.05.2026");
  fillField("Heure de fin de l'événement", "11:00");
  choisirRadio("Certains objets que vous allez déclarer ont-ils été volés dans ou sur un véhicule ?", "Non");
  selectNative("Catégorie d'objet", "telephone");
  selectAutocomplete("Type de l'objet", "Téléphone mobile");
  fillField("Numéro de série", "SN123456");
  cy.get('[data-cy="objet-vole-valider"]').click();
  cy.contains("Objet n° 1").should(bevisible);
  choisirRadio("Avez-vous constaté des dégradations liées à ce vol ?", "Non");
  choisirRadio("L'adresse correspond à", "L'adresse de la personne lesée");
  cy.get('[data-cy="continuer-evenement"]').filter(":visible").first().click();
});

When("je sélectionne le premier créneau disponible", () => {
  cy.wait("@getEsiriusServices");
  cy.wait("@getEsiriusAvailabilities");
  cy.get('[data-cy="creneau-row-0"]').should(bevisible);
  cy.get('[data-cy="creneau-radio-0"]').click({ force: true });
});

When("je continue après le rendez-vous", () => {
  cy.get('[data-cy="continuer-rendez-vous"]').filter(":visible").first().click();
});

When("je soumets la pré-plainte", () => {
  cy.get('[data-cy="soumettre-preplainte"]').filter(":visible").first().click();
});

Then("le message {string} s'affiche sous le champ {string}", (message, champ) => {
  fieldRoot(champ).within(() => {
    cy.contains(message).should(bevisible);
  });
});

Then("le message {string} s'affiche", (message) => {
  cy.contains(message).should(bevisible);
});

Then("l'objet volé est enregistré", () => {
  cy.contains("Le champ est requis").should("not.exist");
  cy.contains("Objet n° 1").should(bevisible);
});

Then("aucune erreur de champ obligatoire n'est affichée", () => {
  cy.contains("Le champ est requis").should("not.exist");
});

Then("le bouton {string} est désactivé", (texte) => {
  cy.contains("button", texte).should(bedisabled);
});

Then("je vois l'étape {string}", (etape) => {
  cy.contains(etape).should(bevisible);
});

Then("le récapitulatif du parcours nominal est affiché", () => {
  cy.contains("Validation").should(bevisible);
  cy.contains("Martin").should(bevisible);
  cy.contains("Anne").should(bevisible);
  cy.contains("Téléphone mobile").should(bevisible);
  cy.contains("Poste PPEL").should(bevisible);
});

Then("la pré-plainte est soumise", () => {
  cy.wait("@submitPrePlainte");
});

Then("le rendez-vous est créé", () => {
  cy.wait("@createEsiriusAppointment");
});

Then("je vois la validation finale", () => {
  cy.contains("Votre demande de pré-plainte a bien été reçue").should(bevisible);
});

Then("le bouton continuer des informations générales est désactivé", () => {
  cy.get('[data-cy="continuer-informations-generales"]').filter(":visible").first().should(bedisabled);
});

Then("le bouton continuer des informations générales est actif", () => {
  cy.get('[data-cy="continuer-informations-generales"]').filter(":visible").first().should(beenabled);
});

Given("que je renseigne tous les champs obligatoires avec des valeurs valides", (dataTable) => {
  dataTable.hashes().forEach(({ Champ, Valeur }) => {
    cy.contains("label", Champ).parent().find("input, textarea, select").clear({ force: true });
    cy.contains("label", Champ).parent().find("input, textarea, select").type(Valeur, { force: true });
  });
});

Given("que j'ai des champs en erreur", () => {
  cy.contains("label", "Nom").parent().find("input").clear({ force: true });
  cy.contains("label", "Nom").parent().find("input").type("A", { force: true });
});
