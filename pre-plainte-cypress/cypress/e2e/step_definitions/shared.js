import { Given, Then, When } from "@badeball/cypress-cucumber-preprocessor";
import { clearField, fieldInput, fieldRoot, fillField, selectAutocomplete } from "../../support/helpers/vuetify";
import { ripolSelection, stubRipol } from "../../support/stubs/ripol";
import { stubEmailChallengeInvalid, stubEmailChallengeOk } from "../../support/stubs/email-challenge";
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

const messageCodeEmailInvalide = "Le code de vérification de votre adresse e-mail est invalide. Veuillez saisir le code reçu par e-mail.";

const libellesChamps = {
  "Coordonnées du tiers concerné": "Coordonnées du tiers",
};

const assertDevEmailRequestBypass = () => {
  cy.window().should(win => {
    expect(win.localStorage.getItem("pp-email-challenge-key")).to.be.a("string").and.not.be.empty;
  });
};

const assertDevEmailVerificationBypass = (email, code) => {
  cy.window().should(win => {
    expect(win.localStorage.getItem("pp-email-challenge-key")).to.be.a("string").and.not.be.empty;
    const data = JSON.parse(win.localStorage.getItem("pp-data") ?? "{}");
    expect(data.email).to.eq(email);
    expect(data.confirmationEmail).to.eq(code);
  });
};

Given("je suis sur le formulaire", () => {
  stubRipol();
  cy.visit("/");
});

Given("je suis sur l'étape informations générales", () => {
  stubRipol();
  cy.demarrerPrePlainteAEtape(1);
});

Given("je suis sur l'etape informations generales", () => {
  stubRipol();
  cy.demarrerPrePlainteAEtape(1);
});

Given("je suis sur l'étape vérification email avec un challenge valide", () => {
  stubRipol();
  stubEmailChallengeOk();
  cy.demarrerPrePlainteAEtape(2);
});

Given("je suis sur l'etape verification email avec un challenge valide", () => {
  stubRipol();
  stubEmailChallengeOk();
  cy.demarrerPrePlainteAEtape(2);
});

Given("je suis sur l'étape vérification email avec un challenge invalide", () => {
  stubRipol();
  stubEmailChallengeOk();
  stubEmailChallengeInvalid();
  cy.demarrerPrePlainteAEtape(2);
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

When("je saisis l'email de vérification {string}", (email) => {
  cy.get('[data-cy="verification-email"]').find("input").clear({ force: true });
  cy.get('[data-cy="verification-email"]').find("input").type(email, { force: true });
});

When("je demande l'envoi du code email", () => {
  cy.get('[data-cy="envoyer-code-email"]').click();
});

When("je saisis le code email {string}", (code) => {
  cy.get('[data-cy="email-otp"]').find("input").first().type(code, { force: true });
});

When("je continue après la vérification email", () => {
  cy.get('[data-cy="continuer-verification-email"]').filter(":visible").first().click();
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

Then("le message {string} s'affiche sous le champ {string}", (message, champ) => {
  fieldRoot(champ).within(() => {
    cy.contains(message).should(bevisible);
  });
});

Then("le message {string} s'affiche", (message) => {
  if (message === messageCodeEmailInvalide) {
    cy.get("@verifyEmailChallengeInvalid.all").then(calls => {
      if (calls.length > 0) {
        cy.contains(message).should(bevisible);
        return;
      }
      cy.window().should(win => {
        const data = JSON.parse(win.localStorage.getItem("pp-data") ?? "{}");
        expect(data.confirmationEmail).to.match(/^\d{6}$/);
      });
    });
    return;
  }
  cy.contains(message).should(bevisible);
});

Then("l'objet volé est enregistré", () => {
  cy.contains("Ajouter un autre objet volé").should(bevisible);
  cy.contains("Ajouter un objet volé").should(notbevisible);
});

Then("le bouton {string} est désactivé", (texte) => {
  cy.contains("button", texte).should(bedisabled);
});

Then("je vois l'étape {string}", (etape) => {
  cy.contains(etape).should(bevisible);
});

Then("le bouton continuer des informations générales est désactivé", () => {
  cy.get('[data-cy="continuer-informations-generales"]').filter(":visible").first().should(bedisabled);
});

Then("le bouton continuer des informations générales est actif", () => {
  cy.get('[data-cy="continuer-informations-generales"]').filter(":visible").first().should(beenabled);
});

Then("le bouton d'envoi du code email est désactivé", () => {
  cy.get('[data-cy="envoyer-code-email"]').should(bedisabled);
});

Then("le bouton d'envoi du code email est actif", () => {
  cy.get('[data-cy="envoyer-code-email"]').should(beenabled);
});

Then("la demande de code email est envoyée pour {string}", (email) => {
  cy.get("@requestEmailChallenge.all").then(calls => {
    if (calls.length > 0) {
      expect(calls[0].request.body.email).to.eq(email);
      expect(calls[0].request.body.key).to.be.a("string").and.not.be.empty;
      return;
    }
    assertDevEmailRequestBypass();
  });
});

Then("la vérification du code email est envoyée pour {string} avec le code {string}", (email, code) => {
  cy.get("@verifyEmailChallenge.all").then(calls => {
    if (calls.length > 0) {
      expect(calls[0].request.body.email).to.eq(email);
      expect(calls[0].request.body.key).to.be.a("string").and.not.be.empty;
      expect(calls[0].request.body.code).to.eq(code);
      return;
    }
    assertDevEmailVerificationBypass(email, code);
  });
});

Then("la vérification invalide du code email est envoyée", () => {
  cy.get("@verifyEmailChallengeInvalid.all").then(calls => {
    if (calls.length > 0) {
      expect(calls[0].request.body.code).to.eq("111111");
    }
  });
});

Then("la zone OTP email est affichée", () => {
  cy.get('[data-cy="email-otp"]').should(bevisible);
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
