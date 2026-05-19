import type { AddressResult, AddressSearchParams } from "@/types/adresse.interface";
import { API_URLS } from "@/constants/constant";
import { getUserFacingApiErrorMessage } from "@/utils/api-http-user-message";

const GENEVA_ADDRESS_API_BASE = API_URLS.GENEVA_ADDRESS_API;

const MIN_SEARCH_LENGTH = 3;
const DEFAULT_COUNTRY = "CH";
const DEFAULT_LIMIT = 10;
const FALLBACK_STREET_NUMBERS = ["1", "5", "10"];
const TYPOGRAPHIC_APOSTROPHES_REGEX = /[\u2018\u2019\u0060\u00B4]/g;
const MULTIPLE_SPACES_REGEX = /\s+/g;
const LEADING_STREET_NUMBER_REGEX = /^\s*\d+[a-zA-Z]?\b/;
const LEADING_RESULT_STREET_NUMBER_REGEX = /^\s*\d+[a-zA-Z]?\s+/;

const SCORE_EXACT_LABEL = 200;
const SCORE_EXACT_STREET = 180;
const SCORE_STARTS_WITH_LABEL = 120;
const SCORE_STARTS_WITH_STREET = 100;
const SCORE_STARTS_WITH_LABEL_WITHOUT_NUMBER = 90;
const SCORE_INCLUDES_LABEL = 70;
const SCORE_INCLUDES_STREET = 60;
const SCORE_INCLUDES_LABEL_WITHOUT_NUMBER = 50;
const SCORE_STREET_NUMBER_PENALTY = 5;

const normalizeSearchText = (value: string): string =>
  value.normalize("NFC").trim().replaceAll(TYPOGRAPHIC_APOSTROPHES_REGEX, "'").replaceAll(MULTIPLE_SPACES_REGEX, " ");

const hasStreetNumber = (value: string): boolean => LEADING_STREET_NUMBER_REGEX.test(value);

const buildUrl = (country: string | undefined, searchText: string, limit: number): string => {
  const url = new URL(country ?? DEFAULT_COUNTRY, `${GENEVA_ADDRESS_API_BASE}/`);
  url.searchParams.append("searchText", searchText);
  url.searchParams.append("limit", limit.toString());
  return url.toString();
};

const fetchAddresses = async (searchText: string, limit: number, country?: string): Promise<AddressResult[]> => {
  const response = await fetch(buildUrl(country, searchText, limit));

  if (!response.ok) {
    const bodyText = await response.text();
    throw new Error(getUserFacingApiErrorMessage(response.status, bodyText));
  }

  return response.json();
};

const buildSearchQueries = (searchText: string): string[] => {
  const normalized = normalizeSearchText(searchText);

  if (normalized.length < MIN_SEARCH_LENGTH) {
    return [];
  }

  const queries = [normalized];

  if (!hasStreetNumber(normalized)) {
    for (const streetNumber of FALLBACK_STREET_NUMBERS) {
      queries.push(`${streetNumber} ${normalized}`);
    }
  }

  return [...new Set(queries)];
};

const getAddressKey = (address: AddressResult): string =>
  [
    address.label ?? "",
    address.street ?? "",
    address.streetNumber ?? "",
    address.postalCode ?? "",
    address.locality ?? "",
  ]
    .join("|")
    .toLowerCase();

const getNormalizedAddressFields = (address: AddressResult, originalSearchText: string) => {
  const input = normalizeSearchText(originalSearchText).toLowerCase();
  const label = (address.label ?? "").toLowerCase();
  const street = (address.street ?? "").toLowerCase();
  const labelWithoutNumber = label.replace(LEADING_RESULT_STREET_NUMBER_REGEX, "");

  return {
    input,
    label,
    street,
    labelWithoutNumber,
  };
};

const scoreExactMatches = (label: string, street: string, input: string): number => {
  let score = 0;

  if (label === input) {
    score += SCORE_EXACT_LABEL;
  }

  if (street === input) {
    score += SCORE_EXACT_STREET;
  }

  return score;
};

const scoreStartsWithMatches = (label: string, street: string, labelWithoutNumber: string, input: string): number => {
  let score = 0;

  if (label.startsWith(input)) {
    score += SCORE_STARTS_WITH_LABEL;
  }

  if (street.startsWith(input)) {
    score += SCORE_STARTS_WITH_STREET;
  }

  if (labelWithoutNumber.startsWith(input)) {
    score += SCORE_STARTS_WITH_LABEL_WITHOUT_NUMBER;
  }

  return score;
};

const scoreIncludesMatches = (label: string, street: string, labelWithoutNumber: string, input: string): number => {
  let score = 0;

  if (label.includes(input)) {
    score += SCORE_INCLUDES_LABEL;
  }

  if (street.includes(input)) {
    score += SCORE_INCLUDES_STREET;
  }

  if (labelWithoutNumber.includes(input)) {
    score += SCORE_INCLUDES_LABEL_WITHOUT_NUMBER;
  }

  return score;
};

const scoreStreetNumberPenalty = (input: string, address: AddressResult): number =>
  !hasStreetNumber(input) && address.streetNumber ? -SCORE_STREET_NUMBER_PENALTY : 0;

const scoreAddress = (address: AddressResult, originalSearchText: string): number => {
  const { input, label, street, labelWithoutNumber } = getNormalizedAddressFields(address, originalSearchText);

  return (
    scoreExactMatches(label, street, input) +
    scoreStartsWithMatches(label, street, labelWithoutNumber, input) +
    scoreIncludesMatches(label, street, labelWithoutNumber, input) +
    scoreStreetNumberPenalty(input, address)
  );
};

const dedupeAndSortAddresses = (addresses: AddressResult[], originalSearchText: string): AddressResult[] => {
  const uniqueAddresses = Array.from(new Map(addresses.map(address => [getAddressKey(address), address])).values());

  return uniqueAddresses.sort((a, b) => scoreAddress(b, originalSearchText) - scoreAddress(a, originalSearchText));
};

const getImmediateResults = async (
  queries: string[],
  normalizedSearchText: string,
  limit: number,
  country: string | undefined,
): Promise<AddressResult[] | null> => {
  const collectedResults: AddressResult[] = [];

  for (const query of queries) {
    const results = await fetchAddresses(query, limit, country);

    if (query === normalizedSearchText && results.length > 0) {
      return dedupeAndSortAddresses(results, normalizedSearchText).slice(0, limit);
    }

    collectedResults.push(...results);
  }

  return dedupeAndSortAddresses(collectedResults, normalizedSearchText).slice(0, limit);
};

export const searchAddresses = async (params: AddressSearchParams): Promise<AddressResult[]> => {
  const { searchText, limit = DEFAULT_LIMIT, country } = params;
  const normalizedSearchText = normalizeSearchText(searchText);

  if (normalizedSearchText.length < MIN_SEARCH_LENGTH) {
    return [];
  }

  const queries = buildSearchQueries(normalizedSearchText);

  try {
    return (await getImmediateResults(queries, normalizedSearchText, limit, country)) ?? [];
  } catch {
    return [];
  }
};
