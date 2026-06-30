export const emailChallengeState = {
  requestCount: 0,
  verifyCount: 0,
};

export const stubEmailChallengeOk = () => {
  emailChallengeState.requestCount = 0;
  emailChallengeState.verifyCount = 0;

  cy.intercept("POST", "**/api/email-challenges/request", req => {
    emailChallengeState.requestCount += 1;
    req.reply({
      statusCode: 204,
      body: "",
    });
  }).as("requestEmailChallenge");

  cy.intercept("POST", "**/api/email-challenges/verify", req => {
    emailChallengeState.verifyCount += 1;
    req.reply({
      statusCode: 200,
      body: {
        success: true,
        status: "SUCCESS",
        remainingAttempts: null,
      },
    });
  }).as("verifyEmailChallenge");
};
