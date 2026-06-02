export const STORAGE_KEYS = [
  "pp-data",
  "pp-step",
  "pp-email-challenge-key",
  "pp-dev-skip-email-challenge",
  "pp-open-section",
  "pre-plainte-locale",
];

export const clearPrePlainteStorage = (storage) => {
  STORAGE_KEYS.forEach(key => storage.removeItem(key));
};

export const setPrePlainteStep = (storage, step, data = {}, options = {}) => {
  clearPrePlainteStorage(storage);
  storage.setItem("pp-step", String(step));
  storage.setItem("pp-data", JSON.stringify(data));

  if (options.emailChallengeKey) {
    storage.setItem("pp-email-challenge-key", options.emailChallengeKey);
  }

  if (options.disableEmailChallengeBypass) {
    storage.setItem("pp-dev-skip-email-challenge", "false");
  }

  if (options.openSection) {
    storage.setItem("pp-open-section", options.openSection);
  }
};
