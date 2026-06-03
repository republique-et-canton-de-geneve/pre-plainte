export const stubEmailChallengeVerificationOk = () => {
  cy.intercept("POST", "**/api/email-challenges/verify", {
    statusCode: 200,
    body: {
      success: true,
      status: "SUCCESS",
      remainingAttempts: null,
    },
  }).as("verifyEmailChallenge");
};
