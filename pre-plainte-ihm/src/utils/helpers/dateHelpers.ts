import type { Ref } from "vue";

const REGEX_DATE = /^(\d{2})\.(\d{2})\.(\d{4})$/;
const REGEX_DATE_ISO = /^(\d{4})-(\d{2})-(\d{2})$/;
const REGEX_TIME = /^([01]\d|2[0-3]):([0-5]\d)$/;

const ANNEE_MIN = 1900;
const MIN_DATE = new Date(ANNEE_MIN, 0, 1);
const MAX_DATE = new Date();

const MAX_HOUR = 23;
const MAX_MINUTE = 59;
const TIME_SLICE_LENGTH = 5;
const MIN_DATE_MASK_STEP_1 = 1;
const MIN_DATE_MASK_STEP_2 = 3;
const DATE_PARTS_COUNT = 3;
const TIME_PARTS_COUNT = 2;

const LOCALE_FR = "fr-FR";

export const formatDateTimeFrench = (dateTimeString?: string, longFormat = true) => {
  if (!dateTimeString) {
    return "";
  }

  const date = new Date(dateTimeString);

  if (Number.isNaN(date.getTime())) {
    return dateTimeString;
  }

  if (longFormat) {
    const options: Intl.DateTimeFormatOptions = {
      weekday: "long",
      day: "numeric",
      month: "long",
      year: "numeric",
    };

    return `Le ${date.toLocaleDateString(LOCALE_FR, options)}`;
  }

  return date.toLocaleDateString(LOCALE_FR);
};

/** Vérifie qu’une date/heure (saisie ou ISO) n’est pas antérieure au 01.01.1900 et n'est pas postérieure à la fin du jour courant. */
export function isValidBoundedDate(value?: string): boolean {
  if (!value) {
    return false;
  }

  const date = parseDate(value);
  if (!date) {
    return false;
  }

  return date.getTime() >= MIN_DATE.getTime() && date.getTime() <= MAX_DATE.getTime();
}


export function parseDate(date: string): Date | null {
  const match = REGEX_DATE.exec(date);
  if (!match) {
    return null;
  }

  const [, day, month, year] = match.map(Number);

  const parsed = new Date(year, month - 1, day);

  if (
    parsed.getFullYear() !== year ||
    parsed.getMonth() !== month - 1 ||
    parsed.getDate() !== day
  ) {
    return null;
  }

  return parsed;
}

export function parseTime(time: string): { hour: number; minute: number } | null {
  const match = REGEX_TIME.exec(time);
  if (!match) {
    return null;
  }

  const [, hourStr, minuteStr] = match;
  const hour = Number(hourStr);
  const minute = Number(minuteStr);

  if (hour < 0 || hour > MAX_HOUR || minute < 0 || minute > MAX_MINUTE) {
    return null;
  }

  return { hour, minute };
}

export function calculateAge(birthDate: Date): number {
  const today = new Date();
  const age = today.getFullYear() - birthDate.getFullYear();
  const monthDiff = today.getMonth() - birthDate.getMonth();
  const dayDiff = today.getDate() - birthDate.getDate();
  return monthDiff < 0 || (monthDiff === 0 && dayDiff < 0) ? age - 1 : age;
}

export function toIsoDate(date: string | undefined): string | undefined {
  if (!date) {
    return undefined;
  }

  const match = REGEX_DATE.exec(date);
  if (!match) {
    return undefined;
  }

  const [, day, month, year] = match;
  return `${year}-${month}-${day}`;
}

export function fromIsoDate(date: string | undefined): string {
  if (!date) {
    return "";
  }

  const match = REGEX_DATE_ISO.exec(date);
  if (!match) {
    return "";
  }

  const [, year, month, day] = match;
  return `${day}.${month}.${year}`;
}

export function buildIsoDateTime(date: string, time: string): string | null {
  if (!date || !time) {
    return null;
  }

  const dateParts = date.split(".");
  const timeParts = time.split(":");

  if (!isValidDateParts(dateParts) || !isValidTimeParts(timeParts)) {
    return null;
  }

  const [day, month, year] = dateParts;
  const [hour, minute] = timeParts;

  return `${year}-${month}-${day}T${hour}:${minute}`;
}

function isValidDateParts(parts: string[]): parts is [string, string, string] {
  return parts.length === DATE_PARTS_COUNT && parts.every(Boolean);
}

function isValidTimeParts(parts: string[]): parts is [string, string] {
  return parts.length === TIME_PARTS_COUNT && parts.every(Boolean);
}

export function splitIsoDateTime(value?: string) {
  if (!value) {
    return {
      date: "",
      heure: "",
    };
  }

  const [datePart, timePart] = value.split("T");

  if (!datePart) {
    return { date: "", heure: "" };
  }

  const [year, month, day] = datePart.split("-");

  const date = `${day}.${month}.${year}`;
  const heure = timePart?.slice(0, TIME_SLICE_LENGTH) ?? "";

  return { date, heure };
}

export function applyDateMask(event: InputEvent, model: Ref<string>) {
  const input = event.target as HTMLInputElement;
  const raw = input.value;

  if (event.inputType?.startsWith("delete")) {
    model.value = raw;
    return;
  }

  const digits = raw.replaceAll(/\D/g, "").slice(0, 8);
  let masked = digits;

  if (digits.length > MIN_DATE_MASK_STEP_1) {
    masked = `${digits.slice(0, 2)}.${digits.slice(2)}`;
  }
  if (digits.length > MIN_DATE_MASK_STEP_2) {
    masked = `${digits.slice(0, 2)}.${digits.slice(2, 4)}.${digits.slice(4)}`;
  }

  model.value = masked;
}

export function applyTimeMask(event: InputEvent, model: Ref<string>) {
  const input = event.target as HTMLInputElement;
  const raw = input.value;

  if (event.inputType?.startsWith("delete")) {
    model.value = raw;
    return;
  }

  const digits = raw.replaceAll(/\D/g, "").slice(0, 4);
  let masked = digits;

  if (digits.length > 1) {
    masked = `${digits.slice(0, 2)}:${digits.slice(2)}`;
  }

  model.value = masked;
}

export function toDateOnly(d: Date) {
  return new Date(d.getFullYear(), d.getMonth(), d.getDate());
}
