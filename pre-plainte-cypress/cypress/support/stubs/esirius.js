const futureBeginDateTime = () => {
  const date = new Date();
  date.setDate(date.getDate() + 2);
  date.setHours(10, 0, 0, 0);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}${month}${day} 10:00`;
};

const futureEndDateTime = () => {
  const date = new Date();
  date.setDate(date.getDate() + 2);
  date.setHours(11, 0, 0, 0);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}${month}${day} 11:00`;
};

export const esiriusServiceVol = {
  key: "VOL-1",
  name: "Service vol",
  siteCode: "PPEL",
  existAvailabilities: true,
};

export const esiriusAvailabilityVol = {
  serviceId: esiriusServiceVol.key,
  serviceName: esiriusServiceVol.name,
  availabilities: [
    {
      serviceId: esiriusServiceVol.key,
      siteCode: "PPEL",
      beginDateTime: futureBeginDateTime(),
      endDateTime: futureEndDateTime(),
      resource: {
        key: "POSTE-PPEL",
        name: "Poste PPEL",
      },
    },
  ],
};

export const stubEsiriusOk = (options = {}) => {
  const services = options.services ?? [esiriusServiceVol];
  const availabilities = options.availabilities ?? [esiriusAvailabilityVol];
  const appointment = options.appointment ?? { codeRdv: "RDV-12345" };

  cy.intercept("GET", "**/api/esirius/sites/*/listServices", services).as("getEsiriusServices");
  cy.intercept("GET", "**/api/esirius/sites/*/services/*/plannings/begins/*/periods/*/availabilities", req => {
    const match = availabilities.find(item => req.url.includes(`/services/${item.serviceId}/`));
    req.reply(match?.availabilities ?? []);
  }).as("getEsiriusAvailabilities");
  cy.intercept("POST", "**/api/esirius/appointments", appointment).as("createEsiriusAppointment");
};

export const stubCreationRendezVousIndisponible = () => {
  cy.intercept("POST", "**/api/esirius/appointments", {
    statusCode: 200,
    body: {},
  }).as("createEsiriusAppointment");
};
