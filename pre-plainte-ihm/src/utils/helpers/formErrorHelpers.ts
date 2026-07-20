export function flattenValidationErrorMessages(errors: unknown): string[] {
  if (!errors || typeof errors !== "object") {
    return [];
  }

  const source =
    "errors" in errors && errors.errors && typeof errors.errors === "object"
      ? (errors.errors as Record<string, unknown>)
      : (errors as Record<string, unknown>);

  const messages: string[] = [];

  for (const value of Object.values(source)) {
    if (typeof value === "string" && value.trim()) {
      messages.push(value.trim());
      continue;
    }

    if (Array.isArray(value)) {
      for (const item of value) {
        if (typeof item === "string" && item.trim()) {
          messages.push(item.trim());
        }
      }
    }
  }

  return [...new Set(messages)];
}
