export const stubEmailChallengeOk = () => {
  cy.intercept("POST", "**/api/email-challenges/request", {
    statusCode: 204,
    body: "",
  }).as("requestEmailChallenge");

  cy.intercept("POST", "**/api/email-challenges/verify", {
    statusCode: 200,
    body: {
      success: true,
      status: "SUCCESS",
      remainingAttempts: null,
    },
  }).as("verifyEmailChallenge");
};

export const stubEmailChallengeInvalid = () => {
  cy.intercept("POST", "**/api/email-challenges/verify", {
    statusCode: 200,
    body: {
      success: false,
      status: "INVALID",
      remainingAttempts: 2,
    },
  }).as("verifyEmailChallengeInvalid");
};

export const stubEmailChallengeRequestError = (statusCode = 500, body = { message: "Erreur d'envoi du code" }) => {
  cy.intercept("POST", "**/api/email-challenges/request", {
    statusCode,
    body,
  }).as("requestEmailChallengeError");
};
