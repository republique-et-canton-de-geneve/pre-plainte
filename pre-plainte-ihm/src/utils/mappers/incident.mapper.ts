import type { PrePlainteFormFields } from "@/types/pre-plainte.interface";
import type {
  IncidentCyberDTO,
  IncidentDetailsDTO,
  IncidentDommageDTO,
  IncidentVolDTO,
  ObjetIncidentDTO,
} from "@/types/preplainte-payload-interface";
import { TypeIncident } from "@/types/preplainte-payload-interface";
import { AdresseMapper } from "./adresse.mapper";
import { isBlank, parseNumber } from "@/utils/validations/field-validation.utils";
import { FichierMapper } from "@/utils/mappers/fichier.mapper.ts";
import { toRaw } from "vue";
import type { RipolSelection } from "@/types/ripol.interface.ts";
import { RIPOL, VEHICULE_CATEGORIES_AVEC_PLAQUE, VEHICULE_CATEGORIES_AVEC_VIN } from "@/constants/constant.ts";
import { buildIsoDateTime, toIsoDate } from "@/utils/helpers/dateHelpers.ts";

type IncidentKind = "vol" | "degat-delit" | "cybercrime";

type VolObjetFormSnapshot = {
  categorieObjet?: string;
  isVehicle?: boolean;
  descriptionObjet?: string;
  [key: string]: unknown;
};

export class IncidentMapper {
  static isAutre(code?: string) {
    return code === "AUTRE";
  }

  static hasImei(typeObjet: RipolSelection | null): boolean {
    const code = typeObjet?.code;
    if (!code) {
      return false;
    }
    return code.startsWith(RIPOL.PREFIX_TELEPHONE_MOBILE) || code.startsWith(RIPOL.PREFIX_TABLETTE);
  }

  static isVehiculeAvecVin(sousCategorie: string): boolean {
    return VEHICULE_CATEGORIES_AVEC_VIN.includes(sousCategorie);
  }

  static isVehiculeAvecPlaque(sousCategorie: string): boolean {
    return VEHICULE_CATEGORIES_AVEC_PLAQUE.includes(sousCategorie);
  }

  static async buildIncidentDetails(form: PrePlainteFormFields): Promise<IncidentDetailsDTO> {
    const incidentType = form.typeIncident as IncidentKind;
    const buildBase = () => this.buildBaseIncident(form);

    const builders = {
      vol: async () => this.buildVolIncident(form, buildBase()),
      "degat-delit": async () => this.buildDommageIncident(form, buildBase()),
      cybercrime: async () => this.buildCybercrimeIncident(form, buildBase()),
    } as const;

    const builder = builders[incidentType];
    if (!builder) {
      throw new Error(`Type d'incident non supporté: ${form.typeIncident}`);
    }

    return builder();
  }

  private static buildBaseIncident(form: PrePlainteFormFields) {
    const buildAdresseSafe = (
      adresse: string | undefined,
      adressePostale: string | undefined,
      npa: string | undefined,
      localite: string | undefined,
      pays: string | undefined
    ) => AdresseMapper.buildAdresse(adresse ?? "", adressePostale ?? "", npa ?? "", localite ?? "", pays);

    const buildDateSafe = (date: string, heure: string) => buildIsoDateTime(date, heure) ?? "";

    const adresseIncident = buildAdresseSafe(
      form.adresseEvenement,
      form.adressePostaleEvenement,
      form.npaEvenement,
      form.localiteEvenement,
      form.paysEvenement
    );

    const adresseIncidentSecondaire = buildAdresseSafe(
      form.adresseEvenementSecondaire,
      form.adressePostaleEvenementSecondaire,
      form.npaEvenementSecondaire,
      form.localiteEvenementSecondaire,
      form.paysEvenementSecondaire
    );

    return {
      dateDebutEvent: buildDateSafe(form.dateDebutEvenement, form.heureDebutEvenement),
      dateFinEvent: buildDateSafe(form.dateFinEvenement, form.heureFinEvenement),
      adresseIncident,
      adresseIncidentSecondaire: this.hasAdresse(adresseIncidentSecondaire) ? adresseIncidentSecondaire : undefined,
      typeLieu: form.typeLieu ?? undefined,
      lieuOrigine: form.lieuOrigine?.code ?? undefined,
      adresseConnue: form.adresseConnue ?? undefined,
      adresseLesee: form.adresseLesee ?? undefined,
      isTrajet: form.isTrajet ?? undefined,
    };
  }

  private static buildObjetIncidentDTO(form: PrePlainteFormFields, opts: { isVehicle: boolean }): ObjetIncidentDTO {
    const isVehicle = opts.isVehicle;
    const imeiAllowed = this.hasImei(form.typeObjet);
    const vehiculeAvecVin = !!form.sousCategorie && this.isVehiculeAvecVin(form.sousCategorie);
    const objetAvecPlaque =
      form.categorieObjet === "plaque" ||
      (!!form.sousCategorie && this.isVehiculeAvecPlaque(form.sousCategorie));

    return {
      categorieObjet: this.toOptionalString(form.categorieObjet),
      sousCategorie: this.toOptionalString(form.sousCategorie),
      type: form.typeObjet?.code ? form.typeObjet : undefined,
      fabricant: form.fabricant?.code ? form.fabricant : undefined,
      fabricantAutre: this.resolveFabricantAutre(form),
      modele: form.modele?.code ? form.modele : undefined,
      modeleAutre: this.resolveModeleAutre(form),
      couleur: form.couleur?.code ? form.couleur : undefined,
      couleurSecondaire: form.couleurSecondaire?.code ? form.couleurSecondaire : undefined,
      numeroSerie: this.resolveNumeroSerie(form, isVehicle),
      numeroSerieInconnu: this.resolveNumeroSerieInconnu(form, isVehicle),
      numeroCadre: this.resolveNumeroCadre(form, isVehicle),
      numeroCadreInconnu: this.resolveNumeroCadreInconnu(form, isVehicle),
      numeroIMEI: this.resolveNumeroIMEI(form, imeiAllowed),
      numeroIMEIInconnu: this.resolveNumeroIMEIInconnu(form, imeiAllowed),
      justificationAbsenceIMEI: this.resolveJustificationAbsenceIMEI(form, imeiAllowed),
      gravure: this.resolveGravure(form),
      description: this.toOptionalString(form.descriptionObjet),
      realValue: this.toOptionalString(form.valeurReelle),
      isVehicle,
      purchaseDate: this.resolvePurchaseDate(form, isVehicle),
      vin: this.resolveVin(form, vehiculeAvecVin),
      vinInconnu: this.resolveVinInconnu(form, vehiculeAvecVin),
      velofinderId: this.resolveVelofinderId(form),
      plaqueNumero: this.resolvePlaqueNumero(form, objetAvecPlaque),
      plaqueInconnu: this.resolvePlaqueInconnu(form, objetAvecPlaque),
      plaquePays: this.resolvePlaquePays(form, objetAvecPlaque),
      plaqueCanton: this.resolvePlaqueCanton(form, objetAvecPlaque),
    };
  }

  private static resolveFabricantAutre(form: PrePlainteFormFields) {
    if (!this.isAutre(form.fabricant?.code)) {
      return undefined;
    }
    return this.toOptionalString(form.fabricantAutre);
  }

  private static resolveModeleAutre(form: PrePlainteFormFields) {
    if (!this.isAutre(form.modele?.code)) {
      return undefined;
    }
    return this.toOptionalString(form.modeleAutre);
  }

  private static resolveNumeroSerie(form: PrePlainteFormFields, isVehicle: boolean) {
    if (isVehicle || form.numeroSerieInconnu) {
      return undefined;
    }
    return this.toOptionalString(form.numeroSerie);
  }

  private static resolveNumeroSerieInconnu(form: PrePlainteFormFields, isVehicle: boolean) {
    if (isVehicle) {
      return undefined;
    }
    return form.numeroSerieInconnu ?? undefined;
  }

  private static resolveNumeroCadre(form: PrePlainteFormFields, isVehicle: boolean) {
    if (!isVehicle || form.numeroCadreInconnu) {
      return undefined;
    }
    return this.toOptionalString(form.numeroCadre);
  }

  private static resolveNumeroCadreInconnu(form: PrePlainteFormFields, isVehicle: boolean) {
    if (!isVehicle) {
      return undefined;
    }
    return form.numeroCadreInconnu ?? undefined;
  }

  private static resolveNumeroIMEI(form: PrePlainteFormFields, imeiAllowed: boolean) {
    if (!imeiAllowed || form.numeroIMEIInconnu) {
      return undefined;
    }
    return this.toOptionalString(form.numeroIMEI);
  }

  private static resolveNumeroIMEIInconnu(form: PrePlainteFormFields, imeiAllowed: boolean) {
    if (!imeiAllowed) {
      return undefined;
    }
    return form.numeroIMEIInconnu ?? undefined;
  }

  private static resolveJustificationAbsenceIMEI(form: PrePlainteFormFields, imeiAllowed: boolean) {
    if (!imeiAllowed || !form.numeroIMEIInconnu) {
      return undefined;
    }
    return this.toOptionalString(form.justificationAbsenceIMEI);
  }

  private static resolveGravure(form: PrePlainteFormFields) {
    if (form.categorieObjet !== "bijoux") {
      return undefined;
    }
    return this.toOptionalString(form.gravure);
  }

  private static resolvePurchaseDate(form: PrePlainteFormFields, isVehicle: boolean) {
    if (!isVehicle) {
      return undefined;
    }
    return toIsoDate(form.dateAchat);
  }

  private static resolveVin(form: PrePlainteFormFields, vehiculeAvecVin: boolean) {
    if (!vehiculeAvecVin || form.vinInconnu) {
      return undefined;
    }
    return this.toOptionalString(form.vin);
  }

  private static resolveVinInconnu(form: PrePlainteFormFields, vehiculeAvecVin: boolean) {
    if (!vehiculeAvecVin || !form.vinInconnu) {
      return undefined;
    }
    return form.vinInconnu ?? undefined;
  }

  private static resolveVelofinderId(form: PrePlainteFormFields) {
    if (form.categorieObjet !== "velos") {
      return undefined;
    }
    return this.toOptionalString(form.velofinderId);
  }

  private static resolvePlaqueNumero(form: PrePlainteFormFields, vehiculeAvecPlaque: boolean) {
    if (!vehiculeAvecPlaque || form.plaqueInconnu) {
      return undefined;
    }
    return this.toOptionalString(form.plaqueNumero);
  }

  private static resolvePlaqueInconnu(form: PrePlainteFormFields, vehiculeAvecPlaque: boolean) {
    if (!vehiculeAvecPlaque || !form.plaqueInconnu) {
      return undefined;
    }
    return form.plaqueInconnu ?? undefined;
  }

  private static resolvePlaquePays(form: PrePlainteFormFields, vehiculeAvecPlaque: boolean) {
    if (!vehiculeAvecPlaque || form.plaqueInconnu || !form.plaquePays?.code) {
      return undefined;
    }
    return form.plaquePays;
  }

  private static resolvePlaqueCanton(form: PrePlainteFormFields, vehiculeAvecPlaque: boolean) {
    if (!vehiculeAvecPlaque || form.plaqueInconnu) {
      return undefined;
    }
    if (form.plaquePays?.code !== RIPOL.PAYS_SUISSE) {
      return undefined;
    }
    if (!form.plaqueCanton?.code) {
      return undefined;
    }
    return form.plaqueCanton;
  }

  private static async addFilesIfAny<T extends { fichiers?: any[] }>(
    form: PrePlainteFormFields,
    result: T,
  ): Promise<void> {
    const rawFiles = this.extractRawFiles(form.fichiers);
    if (!rawFiles.length) {
      return;
    }

    const dtoFiles = await FichierMapper.filesToDTO(rawFiles);
    result.fichiers = dtoFiles.map(f => ({ ...f }));
  }

  private static async buildVolIncident(
    form: PrePlainteFormFields,
    base: ReturnType<typeof IncidentMapper.buildBaseIncident>,
  ): Promise<IncidentVolDTO> {
    // Compat multi-objets: si un tableau de snapshots est présent, on l'utilise.
    const snapshots: VolObjetFormSnapshot[] =
      ((form as unknown as { objetsVolesValides?: VolObjetFormSnapshot[] }).objetsVolesValides) ?? [];
    const useMultiObjets = snapshots.length > 0;

    let objetsVoles: ObjetIncidentDTO[];
    let categorieObjetIncident: string | undefined;

    if (useMultiObjets) {
      objetsVoles = snapshots.map(s => {
        const merged = {
          ...form,
          ...s,
          descriptionObjet: String(s?.descriptionObjet ?? "").trim(),
        } as PrePlainteFormFields;
        const isVehiculeSnapshot = s?.categorieObjet === "vehicule" || s?.isVehicle === true;
        return this.buildObjetIncidentDTO(merged, { isVehicle: !!isVehiculeSnapshot });
      });
      categorieObjetIncident =
        this.toOptionalString(snapshots[0]?.categorieObjet) || this.toOptionalString(form.categorieObjet);
    } else {
      const objetVole = this.buildObjetIncidentDTO(form, { isVehicle: form.isVehicle ?? false });
      objetsVoles = [objetVole];
      categorieObjetIncident = this.toOptionalString(form.categorieObjet);
    }

    const result: IncidentVolDTO = {
      typeIncident: TypeIncident.VOL,
      ...base,
      volDansVehicule: form.volDansVehicule ?? undefined,
      categorieObjet: categorieObjetIncident,
      objetsVoles,
      avezVousDegradation: form.avezVousDegradation ?? undefined,
    };

    await this.addFilesIfAny(form, result);
    return result;
  }

  private static async buildDommageIncident(
    form: PrePlainteFormFields,
    base: ReturnType<typeof IncidentMapper.buildBaseIncident>,
  ): Promise<IncidentDommageDTO> {
    const result: IncidentDommageDTO = {
      typeIncident: TypeIncident.DOMMAGE,
      ...base,
      montantEstime: parseNumber(form.montantEstime),
      devise: this.toOptionalString(form.devise),
      typeDommage: form.typeDommage || undefined,
      naturesDommage: form.naturesDommage?.length ? form.naturesDommage : undefined,
      description: this.toOptionalString(form.description),
      constatPresent: form.constatPresent ?? undefined,
      dateConstat: toIsoDate(form.dateConstat),
      objetDegrades: this.buildObjetDegrades(form),
    };

    await this.addFilesIfAny(form, result);
    return result;
  }

  private static buildObjetDegrades(form: PrePlainteFormFields) {
    if (form.typeDommage !== "dommage-vehicule") {
      return undefined;
    }
    // Compat multi-objets: si un tableau de snapshots est présent, on l'utilise.
    const snapshots: VolObjetFormSnapshot[] =
      ((form as unknown as { objetsDegradesValides?: VolObjetFormSnapshot[] }).objetsDegradesValides) ?? [];
    if (snapshots.length > 0) {
      return snapshots.map(s => {
        const merged = {
          ...form,
          ...s,
          categorieObjet: "vehicule",
          descriptionObjet: String(s?.descriptionObjet ?? "").trim(),
        } as PrePlainteFormFields;
        return this.buildObjetIncidentDTO(merged, { isVehicle: true });
      });
    }
    return [this.buildObjetIncidentDTO(form, { isVehicle: true })];
  }

  private static async buildCybercrimeIncident(
    form: PrePlainteFormFields,
    base: ReturnType<typeof IncidentMapper.buildBaseIncident>,
  ): Promise<IncidentCyberDTO> {
    const result: IncidentCyberDTO = {
      typeIncident: TypeIncident.CYBERCRIME,
      ...base,
      typeCybercrime: this.toOptionalString(form.typeCybercrime),
      descriptionCybercrime: this.toOptionalString(form.descriptionCybercrime),
      datePremierContact: buildIsoDateTime(form.datePremierContact ?? "", form.heurePremierContact ?? "") ?? undefined,
      dateDernierContact: buildIsoDateTime(form.dateDernierContact ?? "", form.heureDernierContact ?? "") ?? undefined,
    };

    await this.addCybercrimeFilesIfAny(form, result);
    this.addCybercrimeSubObjects(result, form);

    return result;
  }

  private static async addCybercrimeFilesIfAny(form: PrePlainteFormFields, result: IncidentCyberDTO): Promise<void> {
    const rawFiles = [
      ...this.extractRawFiles(form.justificatifsPaiement),
      ...this.extractRawFiles(form.copiesEcran),
      ...this.extractRawFiles(form.autresDocuments),
    ];

    if (!rawFiles.length) {
      return;
    }

    const dtoFiles = await FichierMapper.filesToDTO(rawFiles);
    result.fichiersCybercrime = dtoFiles.map(f => ({ ...f }));
  }

  private static extractRawFiles(files?: File[]) {
    if (!Array.isArray(files)) {
      return [];
    }
    return files.map(f => toRaw(f));
  }

  private static addCybercrimeSubObjects(result: IncidentCyberDTO, form: PrePlainteFormFields): void {
    const builders = {
      "fausse-annonce": () => this.addFausseAnnonce(result, form),
      "achat-non-recu": () => this.addAchatNonRecu(result, form),
      "commande-frauduleuse": () => this.addCommandeFrauduleuse(result, form),
    } as const;

    const builder = form.typeCybercrime ? builders[form.typeCybercrime as keyof typeof builders] : undefined;
    if (!builder) {
      return;
    }

    builder();
  }

  private static addCommandeFrauduleuse(result: IncidentCyberDTO, form: PrePlainteFormFields) {
    result.commandeFrauduleuse = {
      prestataire: this.toOptionalString(form.prestataire),
      dateDecouverte: toIsoDate(form.dateDecouverte),
      montant: parseNumber(form.montant),
      assurance: form.assurance ?? undefined,
      emailCommandeInconnu: form.emailCommandeInconnu ?? undefined,
      emailCommande: this.toOptionalString(form.emailCommande),
      telephoneCommandeInconnu: form.telephoneCommandeInconnu ?? undefined,
      telephoneCommande: this.toOptionalString(form.telephoneCommande),
      livraisonAdresseLesee: form.livraisonAdresseLesee ?? undefined,
      adresseLivraison:
        form.livraisonAdresseLesee
          ? undefined
          : AdresseMapper.buildAdresse(
            form.livraisonAdresse ?? "",
            form.livraisonAdressePostale ?? "",
            form.livraisonNpa ?? "",
            form.livraisonLocalite ?? "",
            form.livraisonPays,
            form.livraisonLocaliteCode,
          )
    };
  }

  private static addAchatNonRecu(
    result: IncidentCyberDTO,
    form: PrePlainteFormFields
  ) {
    const adresseVendeurDto = this.buildAdresseVendeur(form);

    result.achatNonRecu = {
      ...this.mapAchatNonRecuContacts(form),
      ...this.mapAchatNonRecuVendeur(form, adresseVendeurDto),
      ...this.mapAchatNonRecuPlateforme(form),
      ...this.mapAchatNonRecuPaiement(form),
      ...this.mapAchatNonRecuDocuments(form),
      ...this.mapAchatNonRecuIdentite(form),
    };
  }

  private static buildAdresseVendeur(form: PrePlainteFormFields) {
    if (form.adresseVendeurInconnue === true) {
      return null;
    }

    return AdresseMapper.buildAdresse(
      form.vendeurAdresse ?? "",
      form.vendeurAdressePostale ?? "",
      form.vendeurNpa ?? "",
      form.vendeurLocalite ?? "",
      form.vendeurPays,
      form.vendeurLocaliteCode,
    );
  }

  private static mapAchatNonRecuContacts(form: PrePlainteFormFields) {
    return {
      montantDelitAchatLigne: this.toOptionalString(form.montantDelitAchatLigne),
      articleNonLivreDescription: this.toOptionalString(form.articleNonLivreDescription),
    };
  }

  private static mapAchatNonRecuVendeur(
    form: PrePlainteFormFields,
    adresseVendeurDto: any
  ) {
    const skipAdresse = form.adresseVendeurInconnue === true;
    return {
      prenomVendeur: this.toOptionalString(form.prenomVendeur),
      nomVendeur: this.toOptionalString(form.nomVendeur),
      telephoneVendeurInconnu: form.telephoneVendeurInconnu ?? undefined,
      telephoneVendeur: this.toOptionalString(form.telephoneVendeur),
      emailVendeurInconnu: form.emailVendeurInconnu ?? undefined,
      emailVendeur: this.toOptionalString(form.emailVendeur),
      adresseVendeurInconnue: form.adresseVendeurInconnue ?? undefined,
      adresseVendeur:
        skipAdresse || !adresseVendeurDto || !this.hasAdresse(adresseVendeurDto)
          ? undefined
          : adresseVendeurDto,
      nomEntrepriseVendeur: this.toOptionalString(form.nomEntrepriseVendeur),
      siteWebEntrepriseVendeur: this.toOptionalString(form.siteWebEntrepriseVendeur),
    };
  }

  private static mapAchatNonRecuPlateforme(form: PrePlainteFormFields) {
    return {
      achatViaPlaceMarche: form.achatViaPlaceMarche ?? undefined,
      plateformeUtilisee: this.toOptionalString(form.plateforme),
      plateformeAutre: this.toOptionalString(form.plateformeAutre),
      plateformeId: this.toOptionalString(form.plateformeId),
    };
  }

  private static mapAchatNonRecuPaiement(form: PrePlainteFormFields) {
    return {
      moyenPaiement: this.toOptionalString(form.moyenPaiement),
      moyenPaiementAutre: this.toOptionalString(form.moyenPaiementAutre),
      ibanBeneficiaire: this.toOptionalString(form.ibanBeneficiaire),
      comptePaypalBeneficiaire: this.toOptionalString(form.comptePaypalBeneficiaire),
      numeroTwintBeneficiaire: this.toOptionalString(form.numeroTwintBeneficiaire),
      adresseWalletCrypto: this.toOptionalString(form.adresseWalletCrypto),
      hashTransactionCrypto: this.toOptionalString(form.hashTransactionCrypto),
      societeBeneficiaire: this.toOptionalString(form.societeBeneficiaire),
      nomBeneficiaire: this.toOptionalString(form.nomBeneficiaire),
      prenomBeneficiaire: this.toOptionalString(form.prenomBeneficiaire),
      dateOperation: toIsoDate(form.dateOperation),
    };
  }

  private static mapAchatNonRecuDocuments(form: PrePlainteFormFields) {
    return {
      annonceDocumentIndisponible: form.annonceDocumentIndisponible ?? undefined,
      raisonAbsenceAnnonce: this.toOptionalString(form.raisonAbsenceAnnonce),
      preuvePaiementIndisponible: form.preuvePaiementIndisponible ?? undefined,
      raisonAbsencePreuvePaiement: this.toOptionalString(form.raisonAbsencePreuvePaiement),
    };
  }

  private static mapAchatNonRecuIdentite(form: PrePlainteFormFields) {
    return {
      copieIdentiteTransmiseAuteur: form.copieIdentiteTransmiseAuteur ?? undefined,
      copieIdentiteAuteurTransmise: form.copieIdentiteAuteurTransmise ?? undefined,
    };
  }

  private static addFausseAnnonce(result: IncidentCyberDTO, form: PrePlainteFormFields) {
    result.fausseAnnonce = {
      urlComplete: this.toOptionalString(form.urlComplete),
      titreAnnonce: this.toOptionalString(form.titreAnnonce),
      nomBailleur: this.toOptionalString(form.nomBailleur),
      emailBailleurInconnu: form.emailBailleurInconnu ?? undefined,
      emailBailleur: this.toOptionalString(form.emailBailleur),
      telephoneBailleurInconnu: form.telephoneBailleurInconnu ?? undefined,
      telephoneBailleur: this.toOptionalString(form.telephoneBailleur),
      adresseBienImmobilier: this.toOptionalString(form.adresseBienImmobilier),
      montantDemande: parseNumber(form.montantDemande),
      modePaiementDemande: this.toOptionalString(form.modePaiementDemande),
    };
  }

  private static toOptionalString(value?: string | null) {
    return isBlank(value) ? undefined : value;
  }

  static hasAdresse(
    adresse?: {
      adresse?: string;
      adressePostale?: string;
      npa?: string;
      localite?: string;
      pays?: string;
      paysCode?: string;
    } | null,
  ): boolean {
    if (!adresse) {
      return false;
    }

    return [
      adresse.adresse,
      adresse.adressePostale,
      adresse.npa,
      adresse.localite,
      adresse.pays,
      adresse.paysCode,
    ].some(value => !isBlank(value));
  }
}
