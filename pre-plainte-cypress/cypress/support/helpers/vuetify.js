export const fieldRoot = (champ) => cy.contains("label", champ).parents(".v-input").first();

export const fieldInput = (champ) => fieldRoot(champ).find("input, textarea").first();

export const fillField = (champ, valeur, options = {}) => {
  fieldInput(champ).clear({ force: true });
  fieldInput(champ).type(valeur, { force: true, ...options });
};

export const clearField = (champ) => {
  fieldInput(champ).clear({ force: true });
};

export const selectAutocomplete = (champ, valeur) => {
  fieldInput(champ).click({ force: true });
  fieldInput(champ).clear({ force: true });
  fieldInput(champ).type(valeur, { force: true });
  cy.contains(".v-list-item-title", valeur).click({ force: true });
};

export const selectNative = (champ, valeur) => {
  cy.contains("label", champ).parents(".v-input").first().find("select").select(valeur, { force: true });
};
