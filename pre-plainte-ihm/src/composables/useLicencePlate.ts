import { RIPOL } from "@/constants/constant";

const clean = (value: string) =>
  value.toUpperCase().replace(/[^A-Z\d]/g, "");

const formatSwiss = (value: string) => {
  const compact = clean(value);
  const match = compact.match(/^([A-Z]{1,2})(\d*)$/);
  if (!match) {
    return compact;
  }
  const [, letters, numbers] = match;
  return numbers ? `${letters} ${numbers}` : letters;
};

const formatSiv = (value: string) => {
  const compact = clean(value);

  const letters1 = compact.slice(0, 2);
  const numbers = compact.slice(2, 5);
  const letters2 = compact.slice(5);

  let result = letters1;

  if (letters1.length === 2 && numbers.length > 0) {
    result += `-${numbers}`;
  } else {
    result += numbers;
  }

  if (numbers.length === 3 && letters2.length > 0) {
    result += `-${letters2}`;
  } else {
    result += letters2;
  }

  return result;
};

const formatFni = (value: string) => {
  const compact = clean(value);

  const match = compact.match(/^(\d*)([A-Z]*)([\dA-Z]*)$/);
  if (!match) {
    return compact;
  }

  const [, numbers, letters, dept] = match;

  let result = numbers;
  if (letters) {
    result += ` ${letters}`;
  }
  if (dept) {
    result += ` ${dept}`;
  }

  return result.trim();
};

export const formatLicensePlate = (
  value: string,
  countryCode?: string,
) => {
  if (!value) {
    return value;
  }

  const upper = value.toUpperCase();

  if (countryCode === RIPOL.PAYS_SUISSE) {
    return formatSwiss(upper);
  }

  if (countryCode === RIPOL.PAYS_FRANCE) {
    const compact = clean(upper);

    const isSiv = /^[A-Z]{0,2}\d{0,3}[A-Z]{0,2}$/.test(compact);

    return isSiv ? formatSiv(upper) : formatFni(upper);
  }

  return clean(upper);
};
