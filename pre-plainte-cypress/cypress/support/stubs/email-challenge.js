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
