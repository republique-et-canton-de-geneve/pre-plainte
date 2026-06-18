import { getApiBaseUrl } from "@/config/config";
import { getUserFacingApiErrorMessage } from "@/utils/api-http-user-message";
import type { Ripol } from "@/types/ripol.interface";

const backendUrl = getApiBaseUrl() || "";
const baseUrl = `${backendUrl}/api/ripol`;

const cache = new Map<string, { data: Ripol[]; timestamp: number }>();
const searchCache = new Map<string, { data: Ripol[]; timestamp: number }>();
const CACHE_DURATION = 24 * 60 * 60 * 1000;
const SEARCH_CACHE_DURATION = 10 * 60 * 1000;
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

  static async search(endpoint: string, search?: string, params?: Record<string, string>,): Promise<Ripol[]> {
    const cacheKey = getCacheKey(endpoint, params);

    const searchCacheKey = this.buildSearchCacheKey(cacheKey, search);

    const cachedSearchResult = this.getFromSearchCache(searchCacheKey);
    if (cachedSearchResult) {
      return cachedSearchResult;
    }

    const cachedResult = this.getFromCache(cacheKey, search);
    if (cachedResult) {
      return cachedResult;
    }

    const data = await this.fetchData(endpoint, search, params);

    this.storeInCache(cacheKey, searchCacheKey, search, data);

    return data;
  }

  private static storeInCache(cacheKey: string, searchCacheKey: string | null, search: string | undefined, data: Ripol[],): void {
    if (searchCacheKey) {
      searchCache.set(searchCacheKey, {
        data,
        timestamp: Date.now(),
      });
    }

    if (!search) {
      cache.set(cacheKey, {
        data,
        timestamp: Date.now(),
      });
    }
  }

  private static buildSearchCacheKey(cacheKey: string, search?: string,): string | null {
    return search ? `${cacheKey}:search:${search.trim().toLowerCase()}` : null;
  }

  private static getFromSearchCache(searchCacheKey: string | null): Ripol[] | null {
    if (!searchCacheKey) {
      return null;
    }

    const cached = searchCache.get(searchCacheKey);

    if (cached && this.isValidCache(cached, SEARCH_CACHE_DURATION)) {
      return cached.data;
    }

    return null;
  }

  private static getFromCache(cacheKey: string, search?: string): Ripol[] | null {
    if (search) {
      return null;
    }

    const cached = cache.get(cacheKey);

    if (cached && this.isValidCache(cached, CACHE_DURATION)) {
      return cached.data;
    }

    return null;
  }

  private static isValidCache(entry: { timestamp: number }, duration: number): boolean {
    return Date.now() - entry.timestamp < duration;
  }

  private static buildQuery(search?: string, params?: Record<string, string>): string {
    const queryParams = new URLSearchParams();

    const trimmedSearch = search?.trim();
    if (trimmedSearch) {
      queryParams.set("search", trimmedSearch);
    }

    if (params) {
      Object.entries(params).forEach(([key, value]) => {
        queryParams.set(key, value);
      });
    }

    const qs = queryParams.toString();
    return qs ? `?${qs}` : "";
  }

  private static async fetchData(endpoint: string, search?: string, params?: Record<string, string>,): Promise<Ripol[]> {
    const url = `${baseUrl}/${endpoint}${this.buildQuery(search, params)}`;

    const response = await fetch(url);

    if (!response.ok) {
      const bodyText = await response.text();
      throw new Error(getUserFacingApiErrorMessage(response.status, bodyText));
    }

    return response.json();
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

  static readonly searchVehicleBrands = (search?: string, vehicleTypeCode?: string): Promise<Ripol[]> =>
    RipolService.search(
      "vehicle-brands",
      search,
      vehicleTypeCode?.trim() ? { vehicleTypeCode: vehicleTypeCode.trim() } : undefined,
    );

  static readonly searchVehicleModels = (brandCode: string, search?: string): Promise<Ripol[]> =>
    RipolService.search("vehicle-models", search, { brandCode });

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
      RipolService.searchVehicleBrands(),
    ]);
  }
}
