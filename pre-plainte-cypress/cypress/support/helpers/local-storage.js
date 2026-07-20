export const STORAGE_KEYS = [
  "pp-data",
  "pp-step",
  "pp-email-challenge-key",
  "pp-last-saved-at",
  "pre-plainte-locale",
];

export const clearPrePlainteStorage = (storage) => {
  for (const key of STORAGE_KEYS) {
    storage.removeItem(key);
  }
};

export const setPrePlainteStep = (storage, step, data = {}, options = {}) => {
  clearPrePlainteStorage(storage);
  storage.setItem("pp-step", String(step));
  storage.setItem("pp-data", JSON.stringify(data));

  if (options.emailChallengeKey) {
    storage.setItem("pp-email-challenge-key", options.emailChallengeKey);
  }

  if (options.lastSavedAt) {
    const value =
      options.lastSavedAt instanceof Date
        ? options.lastSavedAt.toISOString()
        : String(options.lastSavedAt);
    storage.setItem("pp-last-saved-at", value);
  }
};
