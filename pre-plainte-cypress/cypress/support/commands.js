import { setPrePlainteStep } from "./helpers/local-storage";

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
