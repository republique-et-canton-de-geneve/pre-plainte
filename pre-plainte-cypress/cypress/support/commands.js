import { clearPrePlainteStorage, setPrePlainteStep } from "./helpers/local-storage";

Cypress.Commands.add("nettoyerPrePlainteLocalStorage", () => {
  cy.window({ log: false }).then(win => {
    clearPrePlainteStorage(win.localStorage);
  });
});

Cypress.Commands.add("demarrerPrePlainteAEtape", (step, data = {}, options = {}) => {
  const visitOptions = { ...(options.visitOptions ?? {}) };
  const originalOnBeforeLoad = visitOptions.onBeforeLoad;

  cy.visit(options.path ?? "/", {
    ...visitOptions,
    onBeforeLoad(win) {
      setPrePlainteStep(win.localStorage, step, data, options);
      if (originalOnBeforeLoad) {
        originalOnBeforeLoad(win);
      }
    },
  });
});
