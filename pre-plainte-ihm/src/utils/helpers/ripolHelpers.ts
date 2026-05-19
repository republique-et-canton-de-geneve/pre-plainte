import { RipolService } from "@/services/ripolService";
import { GENRES_ORDER, RIPOL } from "@/constants/constant.ts";
import type { Ripol } from "@/types/ripol.interface";

export const fetchGenresSorted = async () => {
  const result = await RipolService.getSexes();

  return [...result].sort(
    (a, b) => GENRES_ORDER.indexOf(a.code) - GENRES_ORDER.indexOf(b.code)
  );
};

const NATIONALITY_PRIORITY_CODES = [
  RIPOL.PAYS_SUISSE,
  RIPOL.PAYS_FRANCE,
  RIPOL.PAYS_ALLEMAGNE,
  RIPOL.PAYS_ITALIE,
  RIPOL.PAYS_AUTRICHE,
] as const;

const EXCLUDED_NATIONALITY_CODES = new Set([RIPOL.PAYS_INTERPOL]);

export const filterNationalities = (data: Ripol[]): Ripol[] =>
  data.filter(n => !EXCLUDED_NATIONALITY_CODES.has(n.code));

const ripolPrimaryLabel = (r: Ripol) => (r.labelFr?.trim() || r.labelDe?.trim() || r.code).trim();

/** Même logique que les pays d’adresse : pays fréquents en tête, puis ordre alphabétique (FR). */
export const sortNationalitiesForSelect = (data: Ripol[]): Ripol[] => {
  const prioritySet = new Set<string>(NATIONALITY_PRIORITY_CODES);
  const priorityItems = NATIONALITY_PRIORITY_CODES.map(code => data.find(r => r.code === code)).filter(
    (r): r is Ripol => r != null,
  );
  const rest = data
    .filter(r => !prioritySet.has(r.code))
    .sort((a, b) => ripolPrimaryLabel(a).localeCompare(ripolPrimaryLabel(b), "fr", { sensitivity: "base" }));
  return filterNationalities([...priorityItems, ...rest]);
};

export const fetchNationalitiesForPersonForm = async (search?: string): Promise<Ripol[]> => {
  const data = await RipolService.searchNationalities(search);
  return sortNationalitiesForSelect(data);
};

/** Tri alphabétique des entrées RIPOL (libellé FR), sans muter le tableau d’origine. */
export const sortRipolByLabelFr = (items: Ripol[]): Ripol[] =>
  [...items].sort((a, b) =>
    ripolPrimaryLabel(a).localeCompare(ripolPrimaryLabel(b), "fr", { sensitivity: "base" }),
  );
