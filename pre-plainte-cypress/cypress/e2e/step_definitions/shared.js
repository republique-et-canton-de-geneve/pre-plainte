import { Given, Then, When } from "@badeball/cypress-cucumber-preprocessor";
import {
  fieldRoot,
  fillField,
  selectAutocomplete,
  selectNative,
  selectRadio,
} from "../../support/helpers/vuetify";
import { ripol, ripolSelection, stubRipol } from "../../support/stubs/ripol";
import { emailChallengeState, stubEmailChallengeOk } from "../../support/stubs/email-challenge";
import { stubCreationRendezVousIndisponible, stubEsiriusOk } from "../../support/stubs/esirius";
import {
  stubRepriseBrouillon,
  stubSoumissionPrePlainteErreur,
  stubSoumissionPrePlainteOk,
} from "../../support/stubs/pre-plainte";
import {
  brouillonVolSimpleDto,
  declarantSuisseValide,
  donneesEmailVerifie,
  evenementCybercrimeAchatNonRecu,
  evenementDommageAvecConstat,
  evenementPlaqueVolee,
  evenementVolSimpleValide,
  recapitulatifVolSimpleAvecRendezVous,
} from "../../support/data/pre-plainte";

const bevisible = "be.visible";
const bedisabled = "be.disabled";
const beenabled = "be.enabled";
const EMAIL_CHALLENGE_KEY_CYPRESS = "challenge-cypress";
const CONTINUER_RENDEZ_VOUS_SELECTOR = '[data-cy="continuer-rendez-vous"]';
const CONTINUER_VERIFICATION_EMAIL_SELECTOR = '[data-cy="continuer-verification-email"]';
const CONTINUER_INFORMATIONS_GENERALES_SELECTOR = '[data-cy="continuer-informations-generales"]';
const TYPE_PERSONNE_NATIVE_SELECTOR = '[data-cy="type-personne-native"]';
const ARIA_DISABLED_ATTRIBUTE = "aria-disabled";
const DISABLED_ATTRIBUTE_VALUE = "true";
const VUETIFY_DISABLED_BUTTON_CLASS = "v-btn--disabled";
const PAYS_SUISSE = "8100";
const DATE_EVENEMENT = "20.05.2026";
const HEURE_DEBUT_EVENEMENT = "10:00";
const HEURE_FIN_EVENEMENT = "11:00";
const TYPE_INCIDENT_VOL = "vol";
const LIBELLE_ORDINATEUR_PORTABLE = "Ordinateur portable";
const LIBELLE_COULEUR_NOIR = "Noir";
const RIPOL_ORDINATEUR_PORTABLE = ripol("722100", LIBELLE_ORDINATEUR_PORTABLE);
const RIPOL_COULEUR_NOIR = ripol("NOIR", LIBELLE_COULEUR_NOIR);
const SITE_CODE_PPEL = "PPEL";
const LIBELLE_POSTE_PPEL = "Poste PPEL";
const EMAIL_CHALLENGE_TIMEOUT_MS = 15000;
const EMAIL_CHALLENGE_RETRY_DELAY_MS = 500;
const STEP_INFORMATIONS_GENERALES = 1;
const STEP_INFORMATIONS_PERSONNELLES = 3;
const STEP_EVENEMENT = 4;
const STEP_RENDEZ_VOUS = 5;
const STEP_RECAPITULATIF = 6;
const SERVICE_AVAILABILITY_START_OFFSET_DAYS = 2;
const SERVICE_AVAILABILITY_HOUR = 10;
const NOMBRE_OBJETS_VOLES_BROUILLON = 1;

const donneesEvenementVolVehicule = {
  nationalite: ripolSelection(PAYS_SUISSE, "Suisse"),
  typeIncident: TYPE_INCIDENT_VOL,
  dateDebutEvenement: DATE_EVENEMENT,
  heureDebutEvenement: HEURE_DEBUT_EVENEMENT,
  dateFinEvenement: DATE_EVENEMENT,
  heureFinEvenement: HEURE_FIN_EVENEMENT,
  adresseConnue: false,
  adresseLesee: true,
  paysEvenement: PAYS_SUISSE,
  volDansVehicule: false,
  categorieObjet: "vehicule",
  sousCategorie: "",
  typeObjet: null,
  fabricant: {},
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

const selectVisibleOption = (champ, valeur) => {
  fieldRoot(champ).find(".v-field").click({ force: true });
  cy.contains(".v-list-item-title", valeur).click({ force: true });
};

const boutonVisibleActif = ($body, selector) =>
  [...$body.find(selector)].some(element => {
    const button = element;
    return (
      Cypress.$(button).is(":visible") &&
      !button.disabled &&
      button.getAttribute(ARIA_DISABLED_ATTRIBUTE) !== DISABLED_ATTRIBUTE_VALUE &&
      !button.classList.contains(VUETIFY_DISABLED_BUTTON_CLASS)
    );
  });

const etatVerificationEmail = ($body) => {
  const envoyerCodeEmail = $body.find('[data-cy="envoyer-code-email"]').filter(":visible").first();
  const continuerVerificationEmail = $body.find(CONTINUER_VERIFICATION_EMAIL_SELECTOR).filter(":visible").first();

  return {
    otpVisible: $body.find('[data-cy="email-otp"]:visible input:visible').length > 0,
    continuerActif: boutonVisibleActif($body, CONTINUER_VERIFICATION_EMAIL_SELECTOR),
    requestCount: emailChallengeState.requestCount,
    verifyCount: emailChallengeState.verifyCount,
    envoyerDisabled: envoyerCodeEmail.prop("disabled") === true,
    envoyerClasses: envoyerCodeEmail.attr("class") ?? "",
    continuerDisabled: continuerVerificationEmail.prop("disabled") === true,
    continuerClasses: continuerVerificationEmail.attr("class") ?? "",
    alertes: $body
      .find(".v-alert:visible")
      .map((_, element) => Cypress.$(element).text().trim().replaceAll(/\s+/g, " "))
      .get()
      .join(" | "),
  };
};

const verificationEmailPrete = etat => etat.otpVisible || etat.continuerActif;

const messageEtatVerificationEmail = etat =>
  [
    `otpVisible=${etat.otpVisible}`,
    `continuerActif=${etat.continuerActif}`,
    `requestCount=${etat.requestCount}`,
    `verifyCount=${etat.verifyCount}`,
    `envoyerDisabled=${etat.envoyerDisabled}`,
    `envoyerClasses=${etat.envoyerClasses}`,
    `continuerDisabled=${etat.continuerDisabled}`,
    `continuerClasses=${etat.continuerClasses}`,
    `alertes=${etat.alertes}`,
  ].join("; ");

const cliquerEnvoyerCodeEmail = () =>
  cy.get('[data-cy="envoyer-code-email"]', { timeout: EMAIL_CHALLENGE_TIMEOUT_MS })
    .filter(":visible")
    .first()
    .should($button => {
      expect($button[0].disabled).to.eq(false);
      expect($button[0].getAttribute(ARIA_DISABLED_ATTRIBUTE)).not.to.eq(DISABLED_ATTRIBUTE_VALUE);
      expect($button[0].classList.contains(VUETIFY_DISABLED_BUTTON_CLASS)).to.eq(false);
    })
    .click();

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

const donneesEvenementInvalides = {
  "date de debut absente": {
    dateDebutEvenement: "",
  },
  "fin avant debut": {
    heureDebutEvenement: "11:00",
    heureFinEvenement: "10:00",
  },
  "degradation non renseignee": {
    avezVousDegradation: null,
  },
  "categorie objet absente": {
    objetsVolesValides: [],
    categorieObjet: "",
  },
};

const formatDateTimeEsirius = (daysFromNow, hour) => {
  const date = new Date();
  date.setDate(date.getDate() + daysFromNow);
  date.setHours(hour, 0, 0, 0);

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  const time = String(hour).padStart(2, "0");

  return `${year}${month}${day} ${time}:00`;
};

const creneauEsirius = (service, daysFromNow, hour) => ({
  serviceId: service.key,
  siteCode: SITE_CODE_PPEL,
  beginDateTime: formatDateTimeEsirius(daysFromNow, hour),
  endDateTime: formatDateTimeEsirius(daysFromNow, hour + 1),
  resource: {
    key: `POSTE-${service.key}`,
    name: service.name,
  },
});

const assertTexteVisible = (texte) => {
  cy.get("body").should($body => {
    const elementsVisibles = $body
      .find("label, span, p, div, h1, h2, h3, h4, h5, legend, button, td, th")
      .filter((_, element) => Cypress.$(element).is(":visible"))
      .filter((_, element) => (element.textContent ?? "").includes(texte));

    expect(elementsVisibles.length, `texte visible "${texte}"`).to.be.greaterThan(0);
  });
};

Given("je démarre un parcours nominal complet", () => {
  stubRipol({
    objectTypes: [RIPOL_ORDINATEUR_PORTABLE],
    brands: [],
    models: [],
    objectColours: [RIPOL_COULEUR_NOIR],
  });
  stubEmailChallengeOk();
  stubEsiriusOk();
  stubSoumissionPrePlainteOk();
  cy.demarrerPrePlainteAEtape(STEP_INFORMATIONS_GENERALES);
});

Given("je suis sur l'étape informations générales", () => {
  stubRipol();
  cy.demarrerPrePlainteAEtape(STEP_INFORMATIONS_GENERALES);
});

Given("je suis sur la section vol de véhicule", () => {
  stubRipol({
    objectTypes: [],
    objectColours: [],
  });
  cy.demarrerPrePlainteAEtape(STEP_EVENEMENT, donneesEvenementVolVehicule);
  cy.contains("Informations sur l'événement").should(bevisible);
  cy.contains("Ajouter un objet volé").should(bevisible);
});

Given("je suis sur l'étape informations personnelles", () => {
  stubRipol();
  cy.demarrerPrePlainteAEtape(STEP_INFORMATIONS_PERSONNELLES, donneesEmailVerifie, { emailChallengeKey: EMAIL_CHALLENGE_KEY_CYPRESS });
});

Given("je suis sur l'étape informations personnelles avec des données invalides {string}", (casValidation) => {
  const surcharge = donneesInformationsPersonnellesInvalides[casValidation];

  expect(surcharge, `cas de validation ${casValidation}`).to.exist;
  stubRipol();
  cy.demarrerPrePlainteAEtape(
    STEP_INFORMATIONS_PERSONNELLES,
    {
      ...declarantSuisseValide,
      ...surcharge,
    },
    { emailChallengeKey: EMAIL_CHALLENGE_KEY_CYPRESS },
  );
});

Given("je suis sur l'étape informations sur l'événement avec un vol invalide {string}", (casValidation) => {
  const surcharge = donneesEvenementInvalides[casValidation];

  expect(surcharge, `cas de validation ${casValidation}`).to.exist;
  stubRipol({
    objectTypes: [RIPOL_ORDINATEUR_PORTABLE],
    objectColours: [RIPOL_COULEUR_NOIR],
  });
  cy.demarrerPrePlainteAEtape(
    STEP_EVENEMENT,
    {
      ...evenementVolSimpleValide,
      ...surcharge,
    },
    { emailChallengeKey: EMAIL_CHALLENGE_KEY_CYPRESS },
  );
});

Given("je suis sur l'étape rendez-vous avec un vol simple valide", () => {
  stubEsiriusOk();
  cy.demarrerPrePlainteAEtape(
    STEP_RENDEZ_VOUS,
    evenementVolSimpleValide,
    { emailChallengeKey: EMAIL_CHALLENGE_KEY_CYPRESS },
  );
});

Given("je suis sur l'étape informations sur l'événement avec une plaque volée", () => {
  stubRipol({
    cantons: [ripol("GE", "Geneve")],
    nationalities: [ripol(PAYS_SUISSE, "Suisse")],
  });
  cy.demarrerPrePlainteAEtape(
    STEP_EVENEMENT,
    evenementPlaqueVolee,
    { emailChallengeKey: EMAIL_CHALLENGE_KEY_CYPRESS },
  );
});

Given("je suis sur l'étape informations sur l'événement avec un dommage valide", () => {
  stubRipol();
  stubEsiriusOk();
  cy.demarrerPrePlainteAEtape(
    STEP_EVENEMENT,
    evenementDommageAvecConstat,
    { emailChallengeKey: EMAIL_CHALLENGE_KEY_CYPRESS },
  );
});

Given("je suis sur l'étape informations sur l'événement avec un cybercrime achat non reçu", () => {
  stubRipol();
  cy.demarrerPrePlainteAEtape(
    STEP_EVENEMENT,
    evenementCybercrimeAchatNonRecu,
    { emailChallengeKey: EMAIL_CHALLENGE_KEY_CYPRESS },
  );
});

Given("je suis sur l'étape rendez-vous avec des services pour chaque type d'incident et un {string}", (incident) => {
  const services = [
    { key: "VOL-1", name: "Service vol", existAvailabilities: true },
    { key: "DOMMAGE-1", name: "Service dommage", existAvailabilities: true },
    { key: "CYBER-1", name: "Service cybercrime", existAvailabilities: true },
  ];
  const dataParIncident = {
    vol: evenementVolSimpleValide,
    dommage: {
      ...evenementDommageAvecConstat,
      fichiers: [new File(["constat"], "constat.pdf", { type: "application/pdf" })],
    },
    cybercrime: {
      ...evenementCybercrimeAchatNonRecu,
      ibanBeneficiaire: "CH9300762011623852957",
    },
  };

  const data = dataParIncident[incident];

  expect(data, `incident ${incident}`).to.exist;
  stubEsiriusOk({
    services,
    availabilities: services.map((service, index) => ({
      serviceId: service.key,
      serviceName: service.name,
      availabilities: [creneauEsirius(service, index + SERVICE_AVAILABILITY_START_OFFSET_DAYS, SERVICE_AVAILABILITY_HOUR)],
    })),
  });
  cy.demarrerPrePlainteAEtape(
    STEP_RENDEZ_VOUS,
    data,
    { emailChallengeKey: EMAIL_CHALLENGE_KEY_CYPRESS },
  );
});

Given("je suis sur le récapitulatif avec un vol simple et un rendez-vous", () => {
  stubRipol();
  stubEmailChallengeOk();
  stubSoumissionPrePlainteOk();
  stubEsiriusOk();
  cy.demarrerPrePlainteAEtape(
    STEP_RECAPITULATIF,
    recapitulatifVolSimpleAvecRendezVous,
    { emailChallengeKey: EMAIL_CHALLENGE_KEY_CYPRESS },
  );
});

Given("je suis sur le récapitulatif avec une soumission en erreur", () => {
  stubRipol();
  stubEmailChallengeOk();
  stubSoumissionPrePlainteErreur();
  stubEsiriusOk();
  cy.demarrerPrePlainteAEtape(
    STEP_RECAPITULATIF,
    recapitulatifVolSimpleAvecRendezVous,
    { emailChallengeKey: EMAIL_CHALLENGE_KEY_CYPRESS },
  );
});

Given("je suis sur le récapitulatif avec un rendez-vous devenu indisponible", () => {
  stubRipol();
  stubEmailChallengeOk();
  stubSoumissionPrePlainteOk();
  stubCreationRendezVousIndisponible();
  cy.demarrerPrePlainteAEtape(
    STEP_RECAPITULATIF,
    recapitulatifVolSimpleAvecRendezVous,
    { emailChallengeKey: EMAIL_CHALLENGE_KEY_CYPRESS },
  );
});

Given("je reprends un brouillon depuis l'URL", () => {
  stubRipol();
  stubRepriseBrouillon("DRAFT-123", brouillonVolSimpleDto);
  cy.demarrerPrePlainteAEtape(
    STEP_INFORMATIONS_PERSONNELLES,
    {},
    {
      path: "/?demandeId=DRAFT-123",
      emailChallengeKey: EMAIL_CHALLENGE_KEY_CYPRESS,
    },
  );
});

Given("que je sélectionne {string} dans le type de personne", (type) => {
  cy.get(TYPE_PERSONNE_NATIVE_SELECTOR).select(valeursTypePersonne[type] ?? type, { force: true });
});

Given("je sélectionne {string} dans le type de personne", (type) => {
  cy.get(TYPE_PERSONNE_NATIVE_SELECTOR).select(valeursTypePersonne[type] ?? type, { force: true });
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
  for (const champ of liste.split(",")) {
    const libelle = champ.trim();
    assertTexteVisible(libellesChamps[libelle] ?? libelle);
  }
});

Then("les champs {string} sont masqués", (liste) => {
  for (const champ of liste.split(",")) {
    cy.contains(champ.trim()).should("not.exist");
  }
});

When("je saisis {string} dans le champ {string}", (valeur, champ) => {
  fillField(champ, valeur);
});

When("je renseigne le type de véhicule {string}", (typeVehicule) => {
  selectVisibleOption("Catégorie d'objet", "Véhicule");
  cy.wait("@getRipolVehicleTypes");
  fieldRoot("Type de l'objet")
    .should(bevisible)
    .within(() => {
      cy.get("input").first().should("not.be.disabled").click({ force: true }).type(`{selectall}${typeVehicule}`, { force: true });
    });
  cy.contains(".v-list-item-title", typeVehicule, { timeout: 10000 }).should(bevisible).click({ force: true });
});

When("je sélectionne {string} dans l'autocomplétion {string}", (valeur, champ) => {
  selectAutocomplete(champ, valeur);
});

When("je sélectionne la valeur {string} dans la liste {string}", (valeur, champ) => {
  selectNative(champ, valeur);
});

When("je valide l'objet volé", () => {
  cy.get('[data-cy="objet-vole-valider"]').click();
});

When("je clique sur le bouton continuer des informations générales", () => {
  cy.get(CONTINUER_INFORMATIONS_GENERALES_SELECTOR).filter(":visible").first().click();
});

When("je sélectionne le type d'incident {string}", (typeIncident) => {
  const valeursTypeIncident = {
    Vol: TYPE_INCIDENT_VOL,
    Dommage: "degat-delit",
    Cybercrime: "cybercrime",
  };

  selectNative("Type d'incident", valeursTypeIncident[typeIncident] ?? typeIncident);
});

When("je renseigne et je vérifie mon adresse e-mail", () => {
  cy.get('[data-cy="verification-email"]').filter(":visible").first().type(donneesEmailVerifie.email);
  cliquerEnvoyerCodeEmail();
  cy.wait(EMAIL_CHALLENGE_RETRY_DELAY_MS);
  cy.get("body").then($body => {
    const etat = etatVerificationEmail($body);

    if (!verificationEmailPrete(etat) && etat.requestCount === 0) {
      return cliquerEnvoyerCodeEmail();
    }

    return undefined;
  });
  cy.get("body", { timeout: EMAIL_CHALLENGE_TIMEOUT_MS }).should($body => {
    const etat = etatVerificationEmail($body);

    expect(verificationEmailPrete(etat), messageEtatVerificationEmail(etat)).to.eq(true);
  }).then($body => {
    const otpVisible = $body.find('[data-cy="email-otp"]:visible input:visible').length > 0;

    if (otpVisible) {
      cy.get('[data-cy="email-otp"]')
        .filter(":visible")
        .first()
        .find("input:visible")
        .should("have.length", donneesEmailVerifie.confirmationEmail.length)
        .each(($input, index) => {
          cy.wrap($input).type(donneesEmailVerifie.confirmationEmail[index]);
        });
    }
  });
  cy.get(CONTINUER_VERIFICATION_EMAIL_SELECTOR, { timeout: EMAIL_CHALLENGE_TIMEOUT_MS })
    .filter(":visible")
    .first()
    .should($button => {
      expect($button[0].disabled).to.eq(false);
      expect($button[0].getAttribute(ARIA_DISABLED_ATTRIBUTE)).not.to.eq(DISABLED_ATTRIBUTE_VALUE);
      expect($button[0].classList.contains(VUETIFY_DISABLED_BUTTON_CLASS)).to.eq(false);
    })
    .click();
});

When("je renseigne les informations personnelles nominales pour moi-même", () => {
  cy.get(TYPE_PERSONNE_NATIVE_SELECTOR).select("MOI_MEME", { force: true });
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

When("je continue après les informations sur l'événement", () => {
  cy.get('[data-cy="continuer-evenement"]').filter(":visible").first().click();
});

When("je renseigne un vol simple nominal", () => {
  selectRadio("Certains objets que vous allez déclarer", "Non");
  selectVisibleOption("Catégorie d'objet", "Informatique");
  selectVisibleOption("Sous-catégorie", "Ordinateur portable / Tablette");
  cy.wait("@getRipolObjectTypes");
  cy.contains(LIBELLE_ORDINATEUR_PORTABLE).should(bevisible);
  fillField("Numéro de série", "SN123456");
  cy.get('[data-cy="objet-vole-valider"]').click();
  selectRadio("Avez-vous constaté des dégradations", "Non");
  fillField("Date de début de l'événement", DATE_EVENEMENT);
  fillField("Heure de début de l'événement", HEURE_DEBUT_EVENEMENT);
  fillField("Date de fin de l'événement", DATE_EVENEMENT);
  fillField("Heure de fin de l'événement", HEURE_FIN_EVENEMENT);
  selectRadio("L'adresse correspond à", "L'adresse de la personne lesée");
  cy.contains(LIBELLE_ORDINATEUR_PORTABLE).should(bevisible);
  cy.contains("button", /Continuer|Poursuivre/).last().click({ force: true });
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
  cy.get(CONTINUER_RENDEZ_VOUS_SELECTOR).filter(":visible").first().click();
  cy.window().its("localStorage").invoke("getItem", "pp-step").should("eq", String(STEP_RECAPITULATIF));
  cy.window().its("localStorage").invoke("getItem", "pp-data").then(data => {
    const parsedData = JSON.parse(data ?? "{}");
    expect(parsedData.selectedCreneau).to.exist;
    expect(parsedData.selectedCreneau.lieu).to.eq(LIBELLE_POSTE_PPEL);
  });
});

When("je tente de continuer après le rendez-vous", () => {
  cy.get(CONTINUER_RENDEZ_VOUS_SELECTOR).filter(":visible").first().click();
});

When("je soumets la pré-plainte", () => {
  cy.get('[data-cy="soumettre-preplainte"]').filter(":visible").first().click();
  cy.wait("@submitPrePlainte");
});

When("je retourne sélectionner un autre rendez-vous", () => {
  cy.contains("button", "Sélectionner un autre rendez-vous").click();
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

Then("je reste sur le récapitulatif", () => {
  cy.get('[data-cy="soumettre-preplainte"]').filter(":visible").first().should(bevisible);
  cy.window().its("localStorage").invoke("getItem", "pp-step").should("eq", String(STEP_RECAPITULATIF));
});

Then("je reste sur l'étape informations personnelles", () => {
  cy.contains("Informations personnelles").should(bevisible);
  cy.get('[data-cy="continuer-informations-personnelles"]').filter(":visible").first().should(bevisible);
  cy.window().its("localStorage").invoke("getItem", "pp-step").should("eq", String(STEP_INFORMATIONS_PERSONNELLES));
});

Then("je reste sur l'étape informations sur l'événement", () => {
  cy.contains("Informations sur l'événement").should(bevisible);
  cy.get('[data-cy="continuer-evenement"]').filter(":visible").first().should(bevisible);
  cy.window().its("localStorage").invoke("getItem", "pp-step").should("eq", String(STEP_EVENEMENT));
});

Then("je reste sur l'étape rendez-vous", () => {
  cy.get(CONTINUER_RENDEZ_VOUS_SELECTOR).filter(":visible").first().should(bevisible);
  cy.window().its("localStorage").invoke("getItem", "pp-step").should("eq", String(STEP_RENDEZ_VOUS));
});

Then("le service de rendez-vous {string} est proposé", (service) => {
  cy.get("#poste-native option", { timeout: EMAIL_CHALLENGE_TIMEOUT_MS }).should($options => {
    const labels = [...$options].map(option => option.textContent?.trim() ?? "");
    expect(labels).to.include(service);
  });
});

Then("le service de rendez-vous {string} n'est pas proposé", (service) => {
  cy.get("#poste-native option").then($options => {
    const labels = [...$options].map(option => option.textContent?.trim() ?? "");
    expect(labels).not.to.include(service);
  });
});

Then("le récapitulatif du parcours nominal est affiché", () => {
  cy.contains("#recap-title", "Validation").should(bevisible);
  cy.contains("MARTIN").should(bevisible);
  cy.contains("Anne").should(bevisible);
  cy.contains(LIBELLE_ORDINATEUR_PORTABLE).should(bevisible);
  cy.contains(LIBELLE_POSTE_PPEL).should(bevisible);
});

Then("le rendez-vous est créé", () => {
  cy.wait("@createEsiriusAppointment");
});

Then("le rendez-vous est signalé indisponible", () => {
  cy.wait("@createEsiriusAppointment");
  cy.contains("Attention : Le rendez-vous sélectionné n’est plus disponible. Merci de sélectionner un autre rendez-vous et de soumettre à nouveau votre pré-plainte.").should(bevisible);
  cy.contains("button", "Sélectionner un autre rendez-vous").should(bevisible);
});

Then("le brouillon est restauré dans le parcours", () => {
  cy.wait("@getBrouillonPrePlainte");
  cy.window().its("localStorage").should(storage => {
    const data = storage.getItem("pp-data");
    const parsedData = JSON.parse(data ?? "{}");
    expect(parsedData.nom).to.eq("Martin");
    expect(parsedData.prenom).to.eq("Anne");
    expect(parsedData.typeIncident).to.eq("vol");
    expect(parsedData.objetsVolesValides).to.have.length(NOMBRE_OBJETS_VOLES_BROUILLON);
    expect(parsedData.objetsVolesValides[0].typeObjet.label).to.eq(LIBELLE_ORDINATEUR_PORTABLE);
  });
});

Then("je vois la validation finale", () => {
  cy.contains("Votre demande de pré-plainte a bien été reçue").should(bevisible);
});

Then("le bouton continuer des informations générales est désactivé", () => {
  cy.get(CONTINUER_INFORMATIONS_GENERALES_SELECTOR).filter(":visible").first().should(bedisabled);
});

Then("le bouton continuer des informations générales est actif", () => {
  cy.get(CONTINUER_INFORMATIONS_GENERALES_SELECTOR).filter(":visible").first().should(beenabled);
});
