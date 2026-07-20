import "./commands";

beforeEach(() => {
  cy.clearLocalStorage();
});

Cypress.on("uncaught:exception", err => {
  return !err.message.includes("Cannot read properties of null (reading 'code')");
});
