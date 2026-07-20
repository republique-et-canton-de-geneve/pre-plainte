import { setPrePlainteStep } from "./helpers/local-storage";
import { applyGeThemeVariables } from "./helpers/ge-theme";

Cypress.Commands.add("demarrerPrePlainteAEtape", (step, data = {}, options = {}) => {
  const visitOptions = options.visitOptions ?? {};
  const originalOnBeforeLoad = visitOptions.onBeforeLoad;

  cy.visit(options.path ?? "/", {
    ...visitOptions,
    onBeforeLoad(win) {
      applyGeThemeVariables(win);
      setPrePlainteStep(win.localStorage, step, data, options);
      if (originalOnBeforeLoad) {
        originalOnBeforeLoad(win);
      }
    },
  });
});
