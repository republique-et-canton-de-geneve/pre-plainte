import { getApiBaseUrl } from "@/config/config";
import { getUserFacingApiErrorMessage } from "@/utils/api-http-user-message";
import type { Ripol } from "@/types/ripol.interface";

const backendUrl = getApiBaseUrl() || "";
const baseUrl = `${backendUrl}/api/ripol`;

const cache = new Map<string, { data: Ripol[]; timestamp: number }>();
const CACHE_DURATION = 24 * 60 * 60 * 1000;
const endpointLieuOrigine = "lieux-origine";

const getCacheKey = (endpoint: string, params?: Record<string, string>): string =>
  params ? `${endpoint}:${JSON.stringify(params)}` : endpoint;

const normalizeForSearch = (value: string): string =>
  value
    .normalize("NFD")
    .replaceAll(/\p{Diacritic}/gu, "")
    .toLocaleLowerCase("fr")
    .trim();

const matchesRipolSearch = (ripol: Ripol, search: string): boolean => {
  const normalizedSearch = normalizeForSearch(search);
  const normalizedLabels = [ripol.labelFr, ripol.labelDe, ripol.code].map(value => normalizeForSearch(value ?? ""));

  return normalizedLabels.some(value => value.includes(normalizedSearch));
};

export class RipolService {
  static async search(endpoint: string, search?: string, params?: Record<string, string>): Promise<Ripol[]> {
    const isCacheable = !search;
    const cacheKey = getCacheKey(endpoint, params);

    if (isCacheable) {
      const cached = cache.get(cacheKey);
      if (cached && Date.now() - cached.timestamp < CACHE_DURATION) {
        return cached.data;
      }
    }

    const queryParams = new URLSearchParams();

    if (search && search.trim().length > 0) {
      queryParams.set("search", search.trim());
    }

    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        queryParams.set(key, value);
      });
    }

    const queryString = queryParams.toString();
    const query = queryString ? `?${queryString}` : "";
    const url = `${baseUrl}/${endpoint}${query}`;

    const response = await fetch(url);

    if (!response.ok) {
      const bodyText = await response.text();
      throw new Error(getUserFacingApiErrorMessage(response.status, bodyText));
    }

    const data = await response.json();

    if (isCacheable) {
      cache.set(cacheKey, { data, timestamp: Date.now() });
    }

    return data;
  }

  static readonly getSexes = (): Promise<Ripol[]> => RipolService.search("sexes");

  static readonly searchNationalities = (search?: string): Promise<Ripol[]> =>
    RipolService.search("nationalities", search);

  static async searchLieuxOrigine(search?: string): Promise<Ripol[]> {
    if (!search || search.trim().length === 0) {
      return RipolService.search(endpointLieuOrigine);
    }

    const results = await RipolService.search(endpointLieuOrigine, search);
    if (results.length > 0) {
      return results;
    }

    const allLieuxOrigine = await RipolService.search(endpointLieuOrigine);
    return allLieuxOrigine.filter(ripol => matchesRipolSearch(ripol, search));
  }

  static readonly searchDocumentTypes = (search?: string): Promise<Ripol[]> =>
    RipolService.search("document-types", search);

  static readonly searchLocationTypes = (search?: string): Promise<Ripol[]> =>
    RipolService.search("location-types", search);

  static readonly searchObjectTypes = (search?: string): Promise<Ripol[]> =>
    RipolService.search("object-types", search);

  static readonly searchVehicleTypes = (search?: string): Promise<Ripol[]> =>
    RipolService.search("vehicle-types", search);

  static readonly searchObjectColours = (search?: string): Promise<Ripol[]> =>
    RipolService.search("object-colours", search);

  static readonly searchVehicleColours = (search?: string): Promise<Ripol[]> =>
    RipolService.search("vehicle-colours", search);

  static readonly searchCantons = (search?: string): Promise<Ripol[]> => RipolService.search("cantons", search);

  static async preload(): Promise<void> {
    await Promise.all([
      RipolService.getSexes(),
      RipolService.searchNationalities(),
      RipolService.searchDocumentTypes(),
      RipolService.searchObjectTypes(),
      RipolService.searchVehicleTypes(),
    ]);
  }
}
