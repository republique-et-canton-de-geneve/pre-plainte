import "./commands";

beforeEach(() => {
  cy.clearLocalStorage();
  cy.clearAllSessionStorage();
});

Cypress.on("uncaught:exception", err => {
  return !err.message.includes("Cannot read properties of null (reading 'code')");
});
