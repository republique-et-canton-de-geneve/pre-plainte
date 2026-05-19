import type { PrePlainteFormFields } from "@/types/pre-plainte.interface";
import { FichierMapper } from "@/utils/mappers/fichier.mapper.ts";
import { AdresseMapper } from "@/utils/mappers/adresse.mapper.ts";
import { isBlank } from "@/utils/validations/field-validation.utils";
import { toRaw } from "vue";
import { toIsoDate } from "@/utils/helpers/dateHelpers.ts";
import { RIPOL } from "@/constants/constant";

/**
 * Mapper pour les informations personnelles
 */
export class PersonneMapper {
  /**
   * Construit les informations personnelles pour le backend
   */
  static async buildInformationsPersonnelles(form: PrePlainteFormFields) {
    const lien = form.lienAvecPersonne;

    const personneAdresse = AdresseMapper.buildAdresse(
      form.adresse,
      form.adressePostale ?? "",
      form.npa,
      form.localite,
      form.pays,
    );

    const result: any = {
      lienAvecPersonne: lien,
      nom: form.nom,
      prenom: form.prenom,
      email: form.email,
      telephone: form.telephone,
      adresse: personneAdresse,
      dateNaissance: toIsoDate(form.dateNaissance),
      genre: form.genre,
      nationalite: form.nationalite,
      lieuOrigine: form.lieuOrigine?.code ? form.lieuOrigine : undefined,
    };

    await this.addOptionalFields(result, form);

    if (lien === "TIERS") {
      const hasTiersData = !isBlank(form.tiersNom) && !isBlank(form.tiersPrenom);

      if (hasTiersData) {
        await this.addTiersInfo(result, form);
      }
    } else if (lien === "ENTREPRISE") {
      await this.addOrganisationInfo(result, form);
    }

    return result;
  }

  /**
   * Ajoute les champs optionnels à la personne
   */
  private static async addOptionalFields(result: any, form: PrePlainteFormFields): Promise<void> {
    if (!isBlank(form.nomNaissance)) {
      result.nomNaissance = form.nomNaissance;
    }

    if (!isBlank(form.typeDocumentIdentite)) {
      result.typeDocumentIdentite = form.typeDocumentIdentite;
    }

    if (!isBlank(form.numeroDocumentIdentite)) {
      result.numeroDocumentIdentite = form.numeroDocumentIdentite;
    }

    if (!isBlank(form.titreSejour)) {
      result.titreSejour = form.titreSejour;
    }
  }

  /**
   * Ajoute les informations du tiers
   */
  private static async addTiersInfo(result: any, form: PrePlainteFormFields): Promise<void> {
    if (isBlank(form.tiersNom) || isBlank(form.tiersPrenom)) {
      return;
    }

    const tiersAdresse = AdresseMapper.buildAdresse(
      form.tiersAdresse ?? "",
      form.tiersAdressePostale ?? "",
      form.tiersNpa ?? "",
      form.tiersLocalite ?? "",
      form.tiersPays ?? RIPOL.PAYS_SUISSE,
    );

    result.tiers = {
      nom: form.tiersNom,
      prenom: form.tiersPrenom,
      genre: form.tiersGenre,
      nationalite: form.tiersNationalite,
      dateNaissance: toIsoDate(form.tiersDateNaissance),
      adresse: tiersAdresse,
      telephone: form.tiersTelephone,
      email: form.tiersEmail,
    };

    if (form.tiersTypeDocumentIdentite != null) {
      result.tiers.typeDocumentIdentite = form.tiersTypeDocumentIdentite;
    }
    if (!isBlank(form.tiersNumeroDocumentIdentite)) {
      result.tiers.numeroDocumentIdentite = form.tiersNumeroDocumentIdentite;
    }

    if (!isBlank(form.typeRepresentation)) {
      result.typeRepresentation = form.typeRepresentation;
    }
  }

  /**
   * Ajoute les informations de l'organisation
   */
  private static async addOrganisationInfo(result: any, form: PrePlainteFormFields): Promise<void> {
    const organisationAdresse = AdresseMapper.buildAdresse(
      form.organisationAdresse ?? "",
      form.organisationAdressePostale ?? "",
      form.organisationNpa ?? "",
      form.organisationLocalite ?? "",
      form.organisationPays ?? RIPOL.PAYS_SUISSE,
    );

    result.organisation = {
      nom: form.organisationNom,
      telephone: form.organisationTelephone,
      email: form.organisationEmail,
      adresse: organisationAdresse,
    };

    if (!isBlank(form.postePersonneMorale)) {
      result.postePersonneMorale = form.postePersonneMorale;

      const rawFile = form.justificatifPersonneMorale?.[0] ? toRaw(form.justificatifPersonneMorale[0]) : null;

      if (rawFile) {
        result.justificatifPersonneMorale = await FichierMapper.fileToDTO(rawFile);
      }
    }
  }
}
