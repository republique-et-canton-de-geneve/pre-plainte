import type { AdresseDTO } from "@/types/preplainte-payload-interface";
import { CountryService } from "@/services/countryService";

/**
 * Mapper pour les adresses
 */
export class AdresseMapper {
  static buildAdresse(
    adresse: string,
    adressePostale: string,
    npa: string,
    localite: string,
    paysRipolCode?: string,
    localiteCode?: string,
  ): AdresseDTO {
    const result: AdresseDTO = {
      adresse,
      adressePostale,
      npa,
      localite,
    };

    if (paysRipolCode && paysRipolCode !== "") {
      const country = CountryService.getCountryByCode(paysRipolCode);
      result.paysCode = paysRipolCode;
      result.pays = country?.name || paysRipolCode;
    }
    if (localiteCode && localiteCode !== "") {
      result.localiteCode = localiteCode;
    }

    return result;
  }

  static fromBackendAdresse(backendAdresse?: {
    adresse?: string;
    adressePostale?: string;
    npa?: string;
    localite?: string;
    localiteCode?: string;
    pays?: string;
    paysCode?: string;
  }): AdresseDTO {
    return {
      adresse: backendAdresse?.adresse || "",
      adressePostale: backendAdresse?.adressePostale || "",
      npa: backendAdresse?.npa || "",
      localite: backendAdresse?.localite || "",
      localiteCode: backendAdresse?.localiteCode || "",
      pays: backendAdresse?.pays || "",
      paysCode: backendAdresse?.paysCode || "",
    };
  }
}
