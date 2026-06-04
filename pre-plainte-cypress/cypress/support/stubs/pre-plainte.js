export const stubSoumissionPrePlainteOk = (demandeId = "PPL-2026-0001") => {
  cy.intercept("POST", "**/api/preplainte/soumission", {
    statusCode: 200,
    headers: { "content-type": "text/plain" },
    body: demandeId,
  }).as("submitPrePlainte");
};
