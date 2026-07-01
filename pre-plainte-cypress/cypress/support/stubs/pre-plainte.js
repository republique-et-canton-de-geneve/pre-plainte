export const stubSoumissionPrePlainteOk = (demandeId = "PPL-2026-0001") => {
  cy.intercept("POST", "**/api/preplainte/soumission", {
    statusCode: 200,
    headers: { "content-type": "text/plain" },
    body: demandeId,
  }).as("submitPrePlainte");
};

export const stubSoumissionPrePlainteErreur = () => {
  cy.intercept("POST", "**/api/preplainte/soumission", {
    statusCode: 500,
    headers: { "content-type": "text/plain" },
    body: "Erreur serveur",
  }).as("submitPrePlainte");
};

export const stubRepriseBrouillon = (demandeId, brouillon) => {
  cy.intercept("GET", `**/api/preplainte/draft/${demandeId}`, {
    statusCode: 200,
    headers: { "content-type": "application/json" },
    body: brouillon,
  }).as("getBrouillonPrePlainte");
};
