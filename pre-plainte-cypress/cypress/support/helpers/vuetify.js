export const fieldRoot = (champ) => cy.contains("label", champ).parents(".v-input").first();

export const fieldInput = (champ) => fieldRoot(champ).find("input, textarea").first();

export const fillField = (champ, valeur, options = {}) => {
  const input = fieldInput(champ);

  input.clear({ force: true });
  input.type(valeur, { force: true, ...options });
};

export const selectAutocomplete = (champ, valeur) => {
  const input = fieldInput(champ);

  input.click({ force: true });
  input.clear({ force: true });
  input.type(valeur, { force: true });
  cy.contains(".v-list-item-title", valeur).click({ force: true });
};
