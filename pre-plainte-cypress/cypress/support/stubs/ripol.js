export const ripol = (code, labelFr, overrides = {}) => ({
  code,
  labelFr,
  labelDe: labelFr,
  groupeType: "",
  ...overrides,
});

export const ripolSelection = (code, label) => ({ code, label });

const defaultRipolData = {
  vehicleTypes: [
    ripol("010", "Voiture"),
    ripol("060", "Moto"),
  ],
  brands: [
    ripol("TOYOTA", "Toyota"),
    ripol("AUTRE", "Autre (préciser)"),
  ],
  models: [
    ripol("COROLLA", "Corolla"),
    ripol("AUTRE", "Autre (préciser)"),
  ],
  vehicleColours: [
    ripol("NOIR", "Noir"),
  ],
  nationalities: [
    ripol("8100", "Suisse"),
    ripol("2500", "France"),
  ],
  locationTypes: [
    ripol("RUE", "Rue"),
  ],
  sexes: [
    ripol("2", "Féminin"),
    ripol("1", "Masculin"),
  ],
  documentTypes: [
    ripol("carte_identite", "Carte d'identite"),
    ripol("passeport", "Passeport"),
  ],
  objectTypes: [
    ripol("010101", "Sac"),
    ripol("020101", "Telephone mobile"),
  ],
  objectColours: [
    ripol("NOIR", "Noir"),
  ],
  cantons: [
    ripol("GE", "Geneve"),
  ],
};

export const stubRipol = (overrides = {}) => {
  const data = { ...defaultRipolData, ...overrides };

  cy.intercept("GET", "**/api/config", {
    backendUrl: "",
    captchaEnabled: "false",
    captchaSitekey: "",
  }).as("getConfig");

  cy.intercept("GET", "**/api/ripol/vehicle-types*", data.vehicleTypes).as("getRipolVehicleTypes");
  cy.intercept("GET", "**/api/ripol/brands*", data.brands).as("getRipolBrands");
  cy.intercept("GET", "**/api/ripol/vehicle-brands*", data.brands).as("getRipolVehicleBrands");
  cy.intercept("GET", "**/api/ripol/models*", data.models).as("getRipolModels");
  cy.intercept("GET", "**/api/ripol/vehicle-models*", data.models).as("getRipolVehicleModels");
  cy.intercept("GET", "**/api/ripol/vehicle-colours*", data.vehicleColours).as("getRipolVehicleColours");
  cy.intercept("GET", "**/api/ripol/nationalities*", data.nationalities).as("getRipolNationalities");
  cy.intercept("GET", "**/api/ripol/location-types*", data.locationTypes).as("getRipolLocationTypes");
  cy.intercept("GET", "**/api/ripol/sexes*", data.sexes).as("getRipolSexes");
  cy.intercept("GET", "**/api/ripol/document-types*", data.documentTypes).as("getRipolDocumentTypes");
  cy.intercept("GET", "**/api/ripol/object-types*", data.objectTypes).as("getRipolObjectTypes");
  cy.intercept("GET", "**/api/ripol/object-colours*", data.objectColours).as("getRipolObjectColours");
  cy.intercept("GET", "**/api/ripol/cantons*", data.cantons).as("getRipolCantons");
};
