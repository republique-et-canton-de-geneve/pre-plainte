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
