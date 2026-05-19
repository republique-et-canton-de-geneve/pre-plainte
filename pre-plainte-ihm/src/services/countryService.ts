import type { Country } from "@/types/country.types";
import { RipolService } from "@/services/ripolService";
import { RIPOL } from "@/constants/constant";
import { filterNationalities } from "@/utils/helpers/ripolHelpers.ts";

export class CountryService {
  private static countries: Country[] = [];
  private static isLoaded = false;

  static async getCountries(): Promise<Country[]> {
    if (!this.isLoaded) {
      const ripolCountries = await RipolService.searchNationalities();
      this.countries = filterNationalities(ripolCountries)
        .map(country => ({
          code: country.code,
          name: country.labelFr || country.labelDe,
        }))
        .sort((a: Country, b: Country) => a.name.localeCompare(b.name, "fr"));

      this.isLoaded = true;
    }

    return this.countries;
  }

  static getCountryByCode(code: string): Country | undefined {
    return this.countries.find(country => country.code === code);
  }

  static getIsoCodeForAddressSearch(ripolCode: string): string | null {
    if (ripolCode === RIPOL.PAYS_SUISSE) {
      return "CH";
    }
    if (ripolCode === RIPOL.PAYS_FRANCE) {
      return "FR";
    }
    return null;
  }

  static canSearchAddresses(ripolCode: string): boolean {
    return ripolCode === RIPOL.PAYS_SUISSE || ripolCode === RIPOL.PAYS_FRANCE;
  }
}
