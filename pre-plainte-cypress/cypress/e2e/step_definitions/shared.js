import { Given, Then, When } from "@badeball/cypress-cucumber-preprocessor";
import { fieldInput, fieldRoot, fillField, selectAutocomplete } from "../../support/helpers/vuetify";
import { ripol, ripolSelection, stubRipol } from "../../support/stubs/ripol";
import { stubEmailChallengeVerificationOk } from "../../support/stubs/email-challenge";
import { stubEsiriusOk } from "../../support/stubs/esirius";
import { stubSoumissionPrePlainteOk } from "../../support/stubs/pre-plainte";
import { declarantSuisseValide, donneesEmailVerifie } from "../../support/data/pre-plainte";

const bevisible = "be.visible";
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
  Tiers: "TIERS",
  Entreprise: "ENTREPRISE",
};

const libellesChamps = {
  "Coordonnées du tiers concerné": "Coordonnées du tiers",
};

const donneesInformationsPersonnellesInvalides = {
  "age inferieur a 16 ans": {
    dateNaissance: "01.01.2015",
  },
  "nationalite etrangere sans titre de sejour": {
    nationalite: ripolSelection("2500", "France"),
    titreSejour: "",
  },
  "numero de document manquant": {
    numeroDocumentIdentite: "",
  },
  "telephone invalide": {
    telephone: "abc",
  },
};

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

Given("je suis sur la section vol de véhicule", () => {
  stubRipol({
    objectTypes: [],
    objectColours: [],
  });
  cy.demarrerPrePlainteAEtape(4, donneesEvenementVolVehicule);
  cy.contains("Informations sur l'événement").should(bevisible);
  cy.contains("Ajouter un objet volé").should(bevisible);
});

Given("je suis sur l'étape informations personnelles", () => {
  stubRipol();
  cy.demarrerPrePlainteAEtape(3, donneesEmailVerifie, { emailChallengeKey: "challenge-cypress" });
});

Given("je suis sur l'étape informations personnelles avec des données invalides {string}", (casValidation) => {
  const surcharge = donneesInformationsPersonnellesInvalides[casValidation];

  expect(surcharge, `cas de validation ${casValidation}`).to.exist;
  stubRipol();
  cy.demarrerPrePlainteAEtape(
    3,
    {
      ...declarantSuisseValide,
      ...surcharge,
    },
    { emailChallengeKey: "challenge-cypress" },
  );
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
  fieldInput("Type de l'objet").type(`{selectall}${typeVehicule}`, { force: true });
  cy.contains(".v-list-item-title", typeVehicule).click({ force: true });
});

When("je sélectionne {string} dans l'autocomplétion {string}", (valeur, champ) => {
  selectAutocomplete(champ, valeur);
});

When("je valide l'objet volé", () => {
  cy.get('[data-cy="objet-vole-valider"]').click();
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
  cy.window().then(win => {
    const data = JSON.parse(win.localStorage.getItem("pp-data") ?? "{}");
    win.localStorage.setItem("pp-data", JSON.stringify({
      ...data,
      ...declarantSuisseValide,
      typeIncident: "vol",
      dateDebutEvenement: "20.05.2026",
      heureDebutEvenement: "10:00",
      dateFinEvenement: "20.05.2026",
      heureFinEvenement: "11:00",
      volDansVehicule: false,
      avezVousDegradation: false,
      adresseLesee: true,
      adresseEvenement: declarantSuisseValide.adresse,
      adressePostaleEvenement: declarantSuisseValide.adressePostale,
      npaEvenement: declarantSuisseValide.npa,
      localiteEvenement: declarantSuisseValide.localite,
      paysEvenement: declarantSuisseValide.pays,
      objetsVolesValides: [
        {
          categorieObjet: "telephone",
          sousCategorie: "telephone_mobile",
          typeObjet: ripolSelection("713100", "Téléphone mobile"),
          fabricant: null,
          fabricantAutre: "",
          modele: null,
          modeleAutre: "",
          couleur: null,
          couleurSecondaire: null,
          gravure: "",
          valeurReelle: "250",
          numeroSerie: "SN123456",
          numeroSerieInconnu: false,
          numeroIMEI: "",
          numeroIMEIInconnu: true,
          justificationAbsenceIMEI: "Non disponible",
          isVehicle: false,
        },
      ],
    }));
  });
  cy.reload();
  cy.contains("Informations sur l'événement").should(bevisible);
  cy.contains("Téléphone mobile").should(bevisible);
  cy.get('[data-cy="continuer-evenement"]').filter(":visible").first().click();
});

When("je sélectionne le premier créneau disponible", () => {
  cy.wait("@getEsiriusServices");
  cy.wait("@getEsiriusAvailabilities");
  cy.get('[data-cy="creneau-row-0"]')
    .filter(":visible")
    .first()
    .within(() => {
      cy.get('[data-cy="creneau-radio-0"] input[type="radio"]').click({ force: true });
    });
});

When("je continue après le rendez-vous", () => {
  cy.get('[data-cy="continuer-rendez-vous"]').filter(":visible").first().click();
  cy.window().its("localStorage").invoke("getItem", "pp-step").should("eq", "6");
  cy.window().its("localStorage").invoke("getItem", "pp-data").then(data => {
    const parsedData = JSON.parse(data ?? "{}");
    expect(parsedData.selectedCreneau).to.exist;
    expect(parsedData.selectedCreneau.lieu).to.eq("Poste PPEL");
  });
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

Then("je vois l'étape {string}", (etape) => {
  cy.contains(etape).should(bevisible);
});

Then("je reste sur l'étape informations personnelles", () => {
  cy.contains("Informations personnelles").should(bevisible);
  cy.get('[data-cy="continuer-informations-personnelles"]').filter(":visible").first().should(bevisible);
  cy.window().its("localStorage").invoke("getItem", "pp-step").should("eq", "3");
});

Then("le récapitulatif du parcours nominal est affiché", () => {
  cy.contains("#recap-title", "Validation").should(bevisible);
  cy.contains("MARTIN").should(bevisible);
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
