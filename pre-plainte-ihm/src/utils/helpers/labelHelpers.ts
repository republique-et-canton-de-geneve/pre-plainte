export const requiredLabel = (label: string): string => {
  const trimmed = label.trim();
  return trimmed.endsWith("*") ? trimmed : `${trimmed} *`;
};
