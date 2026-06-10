import { RIPOL } from "@/constants/constant";

const SIV_LETTERS1_START = 0;
const SIV_LETTERS1_END = 2;
const SIV_NUMBERS_START = 2;
const SIV_NUMBERS_END = 5;
const SIV_LETTERS2_START = 5;
const SIV_LETTERS1_LENGTH = SIV_LETTERS1_END - SIV_LETTERS1_START;
const SIV_NUMBERS_LENGTH = SIV_NUMBERS_END - SIV_NUMBERS_START;

const clean = (value: string) =>
  value.toUpperCase().replaceAll(/[^A-Z\d]/g, "");

const formatSwiss = (value: string) => {
  const compact = clean(value);
  const match = (/^([A-Z]{1,2})(\d*)$/).exec(compact);
  if (!match) {
    return compact;
  }
  const [, letters, numbers] = match;
  return numbers ? `${letters} ${numbers}` : letters;
};

const formatSiv = (value: string) => {
  const compact = clean(value);

  const letters1 = compact.slice(SIV_LETTERS1_START, SIV_LETTERS1_END);
  const numbers = compact.slice(SIV_NUMBERS_START, SIV_NUMBERS_END);
  const letters2 = compact.slice(SIV_LETTERS2_START);

  let result = letters1;

  if (letters1.length === SIV_LETTERS1_LENGTH && numbers.length > 0) {
    result += `-${numbers}`;
  } else {
    result += numbers;
  }

  if (numbers.length === SIV_NUMBERS_LENGTH && letters2.length > 0) {
    result += `-${letters2}`;
  } else {
    result += letters2;
  }

  return result;
};

const formatFni = (value: string) => {
  const compact = clean(value);

  const match = (/^(\d+)([A-Z]+)?([A-Z\d]+)$/).exec(compact);
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
