export function validateInternationalPhone(phone: string): boolean {
  if (!phone) {
    return false;
  }

  const cleanPhone = phone.replaceAll(/\D/g, "");

  return /^[1-9]\d{6,14}$/.test(cleanPhone);
}
