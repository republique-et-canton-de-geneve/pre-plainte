export const fieldRoot = (champ) => cy.contains("label", champ).parents(".v-input").first();

export const fieldInput = (champ) => fieldRoot(champ).find("input, textarea").first();

export const fillField = (champ, valeur, options = {}) => {
  fieldInput(champ).type(`{selectall}${valeur}`, { force: true, ...options });
};

export const selectAutocomplete = (champ, valeur) => {
  fieldInput(champ).click({ force: true });
  fieldInput(champ).type(`{selectall}${valeur}`, { force: true });
  cy.contains(".v-list-item-title", valeur).click({ force: true });
};

export const selectNative = (champ, valeur) => {
  cy.contains(".css-fallback-native-select label", champ).parent().find("select").select(valeur, { force: true });
};

export const selectRadio = (question, reponse) => {
  cy.contains("fieldset", question).within(() => {
    cy.contains("label", reponse).click({ force: true });
  });
};
