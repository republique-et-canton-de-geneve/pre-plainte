export const stubSoumissionPrePlainteOk = (demandeId = "PPL-2026-0001") => {
  cy.intercept("POST", "**/api/preplainte/soumission", {
    statusCode: 200,
    headers: { "content-type": "application/json" },
    body: demandeId,
  }).as("submitPrePlainte");
};

export const stubSoumissionPrePlainteError = (statusCode = 500, body = { message: "Erreur de soumission" }) => {
  cy.intercept("POST", "**/api/preplainte/soumission", {
    statusCode,
    body,
  }).as("submitPrePlainteError");
};
