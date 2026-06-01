import type { PrePlainteDTO, FichierDTO } from "@/types/preplainte-payload-interface";
import type { PrePlainteFormFields, VolObjetFormSnapshot } from "@/types/pre-plainte.interface";
import { AdresseMapper } from "./adresse.mapper.ts";
import { toSafeString, toStringArray } from "@/utils/validations/field-validation.utils";
import { TypeIncident } from "@/types/preplainte-payload-interface";
import { fromIsoDate, splitIsoDateTime } from "@/utils/helpers/dateHelpers.ts";

const FORM_TYPE_INCIDENT = {
  VOL: "vol",
  DOMMAGE: "degat-delit",
  CYBERCRIME: "cybercrime",
} as const;

type IncidentFormType = (typeof FORM_TYPE_INCIDENT)[keyof typeof FORM_TYPE_INCIDENT] | "";

type IncidentContext = {
  isVol: boolean;
  isDom: boolean;
  isCyber: boolean;
};

/**
 * Mapper inverse : Backend DTO vers Frontend Form.
 * Structure découpée (vol/dommage/cyber + adresse événement) comme sur dev,
 * avec mapping cyber étendu (achat non reçu, adresse vendeur structurée) et PJ vol/cyber.
 */
export class ReverseMapper {
  static dtoToForm(dto: PrePlainteDTO, base: PrePlainteFormFields): PrePlainteFormFields {
    const infos = dto?.informationsPersonnelles as any;
    const inc = (dto as any)?.incident;
    const det = inc?.details ?? {};

    const tiers = infos?.tiers ?? {};
    const org = infos?.organisation ?? {};

    const rawTypeIncident = toSafeString(inc?.typeIncident) || toSafeString(det?.typeIncident);
    const typeIncident = this.normalizeTypeIncidentForForm(rawTypeIncident);
    const context = this.buildIncidentContext(typeIncident);
    const debutEvenement = splitIsoDateTime(det?.dateDebutEvent);
    const finEvenement = splitIsoDateTime(det?.dateFinEvent);

    const addrPers = AdresseMapper.fromBackendAdresse(infos?.adresse);
    const addrTiers = AdresseMapper.fromBackendAdresse(tiers?.adresse);
    const addrOrg = AdresseMapper.fromBackendAdresse(org?.adresse);
    const addrEvent = AdresseMapper.fromBackendAdresse(det?.adresseIncident);
    const addrEventSecondaire = AdresseMapper.fromBackendAdresse(det?.adresseIncidentSecondaire);

    return {
      ...base,
      ...this.mapIdentityInfo(infos, base, addrPers),
      ...this.mapTiersInfo(tiers, base, addrTiers),
      ...this.mapOrganisationInfo(org, base, addrOrg),
      ...this.mapIncidentCommonInfo(
        det,
        base,
        typeIncident,
        context,
        debutEvenement,
        finEvenement,
        addrEvent,
        addrEventSecondaire,
      ),
      ...this.mapIncidentSpecificFields(det, base, context),
      ...this.mapRendezVousInfo(base),
    };
  }

  private static buildIncidentContext(typeIncident: IncidentFormType): IncidentContext {
    return {
      isVol: typeIncident === FORM_TYPE_INCIDENT.VOL,
      isDom: typeIncident === FORM_TYPE_INCIDENT.DOMMAGE,
      isCyber: typeIncident === FORM_TYPE_INCIDENT.CYBERCRIME,
    };
  }

  private static normalizeLienAvecPersonneForForm(value: unknown): string {
    const normalized =
      typeof value === "string" || typeof value === "number" || typeof value === "boolean"
        ? toSafeString(value).trim()
        : "";

    if (!normalized) {
      return "";
    }

    return normalized.toUpperCase();
  }

  private static mapIdentityInfo(infos: any, base: PrePlainteFormFields, addrPers: any) {
    return {
      confirmeIdentite: true,
      confirmeSituation: true,
      lienAvecPersonne: this.normalizeLienAvecPersonneForForm(infos?.lienAvecPersonne),
      typeRepresentation: toSafeString(infos?.typeRepresentation, base.typeRepresentation),
      postePersonneMorale: toSafeString(infos?.postePersonneMorale, base.postePersonneMorale),
      nom: toSafeString(infos?.nom, base.nom),
      nomNaissance: toSafeString(infos?.nomNaissance, base.nomNaissance),
      prenom: toSafeString(infos?.prenom, base.prenom),
      genre: infos?.genre ?? base.genre,
      nationalite: infos?.nationalite ?? base.nationalite,
      dateNaissance: infos?.dateNaissance ? fromIsoDate(infos?.dateNaissance) : toSafeString(base.dateNaissance),
      titreSejour: toSafeString(infos?.titreSejour, base.titreSejour),
      ...addrPers,
      telephone: toSafeString(infos?.telephone, base.telephone),
      email: toSafeString(infos?.email, base.email),
      confirmationEmail: toSafeString(base.confirmationEmail),
      typeDocumentIdentite: toSafeString(infos?.typeDocumentIdentite, base.typeDocumentIdentite),
      numeroDocumentIdentite: toSafeString(infos?.numeroDocumentIdentite, base.numeroDocumentIdentite),
      justificatifPersonneMorale: this.toFiles(
        infos?.justificatifPersonneMorale,
        base.justificatifPersonneMorale ?? [],
      ),
    };
  }

  private static mapIncidentCommonInfo(
    det: any,
    base: PrePlainteFormFields,
    typeIncident: IncidentFormType,
    context: IncidentContext,
    debutEvenement: ReturnType<typeof splitIsoDateTime>,
    finEvenement: ReturnType<typeof splitIsoDateTime>,
    addrEvent: ReturnType<typeof AdresseMapper.fromBackendAdresse>,
    addrEventSecondaire: ReturnType<typeof AdresseMapper.fromBackendAdresse>,
  ) {
    const incidentAddressSource = context.isVol || context.isDom;

    return {
      typeIncident,
      dateDebutEvenement: toSafeString(debutEvenement.date, base.dateDebutEvenement),
      heureDebutEvenement: toSafeString(debutEvenement.heure, base.heureDebutEvenement),
      dateFinEvenement: toSafeString(finEvenement.date, base.dateFinEvenement),
      heureFinEvenement: toSafeString(finEvenement.heure, base.heureFinEvenement),
      adresseLesee: incidentAddressSource ? det?.adresseLesee : base.adresseLesee,
      typeLieu: incidentAddressSource ? det?.typeLieu : base.typeLieu,
      adresseConnue: incidentAddressSource ? det?.adresseConnue : base.adresseConnue,
      isTrajet: incidentAddressSource ? det?.isTrajet : base.isTrajet,
      adresseEvenement: incidentAddressSource ? addrEvent.adresse : base.adresseEvenement,
      adressePostaleEvenement: incidentAddressSource ? addrEvent.adressePostale : base.adressePostaleEvenement,
      npaEvenement: incidentAddressSource ? addrEvent.npa : base.npaEvenement,
      localiteEvenement: incidentAddressSource ? addrEvent.localite : base.localiteEvenement,
      paysEvenement: incidentAddressSource ? addrEvent.pays : base.paysEvenement,
      lieuOrigine: incidentAddressSource ? det?.lieuOrigine : base.lieuOrigine,
      adresseEvenementSecondaire: incidentAddressSource ? addrEventSecondaire.adresse : base.adresseEvenementSecondaire,
      adressePostaleEvenementSecondaire: incidentAddressSource
        ? addrEventSecondaire.adressePostale
        : base.adressePostaleEvenementSecondaire,
      npaEvenementSecondaire: incidentAddressSource ? addrEventSecondaire.npa : base.npaEvenementSecondaire,
      localiteEvenementSecondaire: incidentAddressSource
        ? addrEventSecondaire.localite
        : base.localiteEvenementSecondaire,
      paysEvenementSecondaire: incidentAddressSource ? addrEventSecondaire.pays : base.paysEvenementSecondaire,
    };
  }

  private static mapRendezVousInfo(base: PrePlainteFormFields) {
    return {
      preferenceRendezVous: base.preferenceRendezVous,
      dateSouhaitee: base.dateSouhaitee,
      creneauPrefere: base.creneauPrefere,
      dateAlternative: base.dateAlternative,
      commentairesRendezVous: base.commentairesRendezVous,
      modeContactPrefere: base.modeContactPrefere,
      codeRdv: base.codeRdv,
      selectedCreneau: base.selectedCreneau ?? null,
    };
  }

  private static mapTiersInfo(tiers: any, base: PrePlainteFormFields, addrTiers: any) {
    return {
      tiersNom: toSafeString(tiers?.nom, base.tiersNom),
      tiersPrenom: toSafeString(tiers?.prenom, base.tiersPrenom),
      tiersGenre: tiers?.genre ?? base.tiersGenre,
      tiersNationalite: tiers?.nationalite ?? base.tiersNationalite,
      tiersDateNaissance: tiers?.dateNaissance
        ? fromIsoDate(tiers?.dateNaissance)
        : toSafeString(base.tiersDateNaissance),
      tiersAdresse: addrTiers.adresse,
      tiersAdressePostale: addrTiers.adressePostale,
      tiersNpa: addrTiers.npa,
      tiersLocalite: addrTiers.localite,
      tiersPays: addrTiers.pays,
      tiersTelephone: toSafeString(tiers?.telephone, base.tiersTelephone),
      tiersEmail: toSafeString(tiers?.email, base.tiersEmail),
      tiersConfirmationEmail: toSafeString(base.tiersConfirmationEmail),
      tiersTypeDocumentIdentite: toSafeString(tiers?.typeDocumentIdentite, base.tiersTypeDocumentIdentite),
      tiersNumeroDocumentIdentite: toSafeString(tiers?.numeroDocumentIdentite, base.tiersNumeroDocumentIdentite),
    };
  }

  private static mapOrganisationInfo(org: any, base: PrePlainteFormFields, addrOrg: any) {
    return {
      organisationNom: toSafeString(org?.nom, base.organisationNom),
      organisationAdresse: addrOrg.adresse,
      organisationAdressePostale: addrOrg.adressePostale,
      organisationNpa: addrOrg.npa,
      organisationLocalite: addrOrg.localite,
      organisationPays: addrOrg.pays,
      organisationTelephone: toSafeString(org?.telephone, base.organisationTelephone),
      organisationEmail: toSafeString(org?.email, base.organisationEmail),
      organisationConfirmationEmail: toSafeString(base.organisationConfirmationEmail),
    };
  }

  private static mapIncidentSpecificFields(det: any, base: PrePlainteFormFields, context: IncidentContext) {
    return {
      ...this.mapVolOrDamageObjectFields(det, base, context),
      ...this.mapDamageFields(det, base, context),
      ...this.mapCybercrimeFields(det, base, context),
    };
  }

  private static mapBackendVolObjetToSnapshot(o: any, incidentCategorie: string): VolObjetFormSnapshot {
    return {
      categorieObjet: toSafeString(o?.categorieObjet) || incidentCategorie,
      sousCategorie: toSafeString(o?.sousCategorie),
      typeObjet: o?.type ?? null,
      fabricant: o?.fabricant ?? null,
      fabricantAutre: toSafeString(o?.fabricantAutre),
      modele: o?.modele ?? null,
      modeleAutre: toSafeString(o?.modeleAutre),
      couleur: o?.couleur ?? null,
      couleurSecondaire: o?.couleurSecondaire ?? null,
      gravure: toSafeString(o?.gravure),
      valeurReelle: toSafeString(o?.realValue),
      numeroSerie: toSafeString(o?.numeroSerie),
      numeroSerieInconnu: !!o?.numeroSerieInconnu,
      numeroIMEI: toSafeString(o?.numeroIMEI),
      numeroIMEIInconnu: !!o?.numeroIMEIInconnu,
      justificationAbsenceIMEI: toSafeString(o?.justificationAbsenceIMEI),
      isVehicle: !!o?.isVehicle,
      numeroCadre: toSafeString(o?.numeroCadre),
      numeroCadreInconnu: !!o?.numeroCadreInconnu,
      vin: toSafeString(o?.vin),
      vinInconnu: !!o?.vinInconnu,
      velofinderId: toSafeString(o?.velofinderId),
      dateAchat: fromIsoDate(o?.purchaseDate),
      plaqueNumero: toSafeString(o?.plaqueNumero),
      plaqueInconnu: !!o?.plaqueInconnu,
      plaquePays: o?.plaquePays ?? null,
      plaqueCanton: o?.plaqueCanton ?? null,
    };
  }

  private static mapVolOrDamageObjectFields(det: any, base: PrePlainteFormFields, context: IncidentContext) {
    const volMulti = this.mapVolMultiObjets(det, base, context);
    if (volMulti) {
      return volMulti;
    }

    const dommageVehiculeMulti = this.mapDommageVehiculeMultiObjets(det, base, context);
    if (dommageVehiculeMulti) {
      return dommageVehiculeMulti;
    }

    return this.mapSingleObjetFields(det, base, context);
  }

  private static mapVolMultiObjets(det: any, base: PrePlainteFormFields, context: IncidentContext) {
    if (!context.isVol) {
      return undefined;
    }
    const list = det?.objetsVoles;
    if (!Array.isArray(list) || list.length === 0) {
      return undefined;
    }

    const incidentCategorie = toSafeString(det?.categorieObjet);
    return {
      ...this.resetObjetFieldsForMulti(),
      volDansVehicule: det?.volDansVehicule,
      categorieObjet: incidentCategorie,
      objetsVolesValides: list.map((o: any) => this.mapBackendVolObjetToSnapshot(o, incidentCategorie)),
      objetsDegradesValides: base.objetsDegradesValides ?? [],
      isVehicle: false,
      avezVousDegradation: det?.avezVousDegradation ?? base.avezVousDegradation,
      fichiers: this.toFiles(det?.fichiers, base.fichiers ?? []),
    };
  }

  private static mapDommageVehiculeMultiObjets(det: any, base: PrePlainteFormFields, context: IncidentContext) {
    if (!context.isDom) {
      return undefined;
    }
    const typeDom = toSafeString(det?.typeDommage);
    const degrades = det?.objetDegrades;
    if (typeDom !== "dommage-vehicule" || !Array.isArray(degrades) || degrades.length === 0) {
      return undefined;
    }

    const categorie = "vehicule";
    return {
      ...this.resetObjetFieldsForMulti(),
      volDansVehicule: base.volDansVehicule,
      categorieObjet: categorie,
      objetsVolesValides: base.objetsVolesValides ?? [],
      objetsDegradesValides: degrades.map((o: any) => this.mapBackendVolObjetToSnapshot(o, categorie)),
      isVehicle: true,
      avezVousDegradation: base.avezVousDegradation,
      fichiers: this.toFiles(det?.fichiers, base.fichiers ?? []),
    };
  }

  private static mapSingleObjetFields(det: any, base: PrePlainteFormFields, context: IncidentContext) {
    return {
      volDansVehicule: context.isVol ? det?.volDansVehicule : base.volDansVehicule,
      categorieObjet: context.isVol ? toSafeString(det?.categorieObjet) : base.categorieObjet,
      objetsVolesValides: base.objetsVolesValides ?? [],
      objetsDegradesValides: base.objetsDegradesValides ?? [],
      sousCategorie: toSafeString(this.getFieldSource(det, base, context, "sousCategorie")),
      typeObjet: this.getFieldSource(det, base, context, "type"),
      fabricant: this.getFieldSource(det, base, context, "fabricant"),
      fabricantAutre: toSafeString(this.getFieldSource(det, base, context, "fabricantAutre")),
      modele: this.getFieldSource(det, base, context, "modele"),
      modeleAutre: toSafeString(this.getFieldSource(det, base, context, "modeleAutre")),
      couleur: this.getFieldSource(det, base, context, "couleur"),
      couleurSecondaire: this.getFieldSource(det, base, context, "couleurSecondaire"),
      numeroCadre: toSafeString(this.getFieldSource(det, base, context, "numeroCadre")),
      numeroCadreInconnu: this.getFieldSourceBool(det, base, context, "numeroCadreInconnu"),
      vin: toSafeString(this.getFieldSource(det, base, context, "vin")),
      vinInconnu: this.getFieldSourceBool(det, base, context, "vinInconnu"),
      velofinderId: toSafeString(this.getFieldSource(det, base, context, "velofinderId")),
      dateAchat: fromIsoDate(this.getFieldSource(det, base, context, "purchaseDate") as string | undefined),
      plaqueNumero: toSafeString(this.getFieldSource(det, base, context, "plaqueNumero")),
      plaqueInconnu: this.getFieldSourceBool(det, base, context, "plaqueInconnu"),
      plaquePays: this.getFieldSource(det, base, context, "plaquePays"),
      plaqueCanton: this.getFieldSource(det, base, context, "plaqueCanton"),
      gravure: toSafeString(this.getVolObjectFieldSource(det, base, "gravure")),
      valeurReelle: toSafeString(this.getVolObjectFieldSource(det, base, "realValue")),
      numeroSerie: toSafeString(this.getVolObjectFieldSource(det, base, "numeroSerie")),
      numeroSerieInconnu: this.getVolObjectFieldSourceBool(det, base, "numeroSerieInconnu"),
      numeroIMEI: toSafeString(this.getVolObjectFieldSource(det, base, "numeroIMEI")),
      numeroIMEIInconnu: this.getVolObjectFieldSourceBool(det, base, "numeroIMEIInconnu"),
      justificationAbsenceIMEI: toSafeString(this.getVolObjectFieldSource(det, base, "justificationAbsenceIMEI")),
      isVehicle: this.getVolObjectFieldSourceBool(det, base, "isVehicle"),
      avezVousDegradation: context.isVol ? det?.avezVousDegradation : base.avezVousDegradation,
      fichiers: this.toFiles(det?.fichiers, base.fichiers ?? []),
    };
  }

  private static resetObjetFieldsForMulti() {
    return {
      sousCategorie: "",
      typeObjet: null,
      fabricant: null,
      fabricantAutre: "",
      modele: null,
      modeleAutre: "",
      couleur: null,
      couleurSecondaire: null,
      gravure: "",
      valeurReelle: "",
      numeroSerie: "",
      numeroSerieInconnu: undefined,
      numeroIMEI: "",
      numeroIMEIInconnu: undefined,
      justificationAbsenceIMEI: "",
      numeroCadre: "",
      numeroCadreInconnu: undefined,
      vin: "",
      vinInconnu: undefined,
      velofinderId: "",
      dateAchat: "",
      plaqueNumero: "",
      plaqueInconnu: undefined,
      plaquePays: null,
      plaqueCanton: null,
    };
  }

  private static mapDamageFields(det: any, base: PrePlainteFormFields, context: IncidentContext) {
    if (!context.isDom) {
      return {
        typeDommage: base.typeDommage,
        montantEstime: base.montantEstime,
        devise: base.devise,
        naturesDommage: base.naturesDommage,
        description: base.description,
        dateConstat: base.dateConstat,
        constatPresent: base.constatPresent,
      };
    }

    return {
      typeDommage: toSafeString(det?.typeDommage),
      montantEstime: toSafeString(det?.montantEstime),
      devise: toSafeString(det?.devise),
      naturesDommage: toStringArray(det?.naturesDommage),
      description: toSafeString(det?.description),
      dateConstat: fromIsoDate(det?.dateConstat),
      constatPresent: !!det?.constatPresent,
    };
  }

  private static mapCybercrimeFields(det: any, base: PrePlainteFormFields, context: IncidentContext) {
    if (!context.isCyber) {
      return this.cybercrimeFieldsFromBase(base);
    }

    const commandeFrauduleuse = det?.commandeFrauduleuse ?? {};
    const achatNonRecu = det?.achatNonRecu ?? {};
    const fausseAnnonce = det?.fausseAnnonce ?? {};
    const premierContact = splitIsoDateTime(det?.datePremierContact);
    const dernierContact = splitIsoDateTime(det?.dateDernierContact);
    const descriptionCybercrime = toSafeString(det?.descriptionCybercrime ?? det?.cybercrimeDescription);
    const addrLivraison = AdresseMapper.fromBackendAdresse(commandeFrauduleuse.adresseLivraison);

    return {
      ...this.mapCybercrimeCommonFields(det, base, descriptionCybercrime, premierContact, dernierContact),
      ...this.mapCybercrimeCommandeFrauduleuseFields(det, base, commandeFrauduleuse, addrLivraison),
      ...this.mapCybercrimeAchatNonRecuFields(det, base, achatNonRecu),
      ...this.mapCybercrimeFausseAnnonceFields(det, base, fausseAnnonce),
      ...this.mapCybercrimeFiles(det, base),
    };
  }

  private static mapCybercrimeCommonFields(
    det: any,
    base: PrePlainteFormFields,
    descriptionCybercrime: string,
    premierContact: { date?: string; heure?: string },
    dernierContact: { date?: string; heure?: string },
  ) {
    return {
      typeCybercrime: toSafeString(det?.typeCybercrime ?? base.typeCybercrime),
      descriptionCybercrime,
      datePremierContact: toSafeString(premierContact.date),
      heurePremierContact: toSafeString(premierContact.heure),
      dateDernierContact: toSafeString(dernierContact.date),
      heureDernierContact: toSafeString(dernierContact.heure),
    };
  }

  private static mapCybercrimeCommandeFrauduleuseFields(
    det: any,
    base: PrePlainteFormFields,
    commandeFrauduleuse: any,
    addrLivraison: ReturnType<typeof AdresseMapper.fromBackendAdresse>,
  ) {
    return {
      ...this.mapCommandeFrauduleuseInfos(det, base, commandeFrauduleuse),
      ...this.mapCommandeFrauduleuseCoordonnees(det, base, commandeFrauduleuse),
      ...this.mapCommandeFrauduleuseAdresse(base, addrLivraison),
    };
  }

  private static mapCommandeFrauduleuseInfos(det: any, base: PrePlainteFormFields, commandeFrauduleuse: any) {
    return {
      prestataire: toSafeString(commandeFrauduleuse?.prestataire ?? det?.prestataire),
      dateDecouverte: toSafeString(
        fromIsoDate(commandeFrauduleuse?.dateDecouverte ?? det?.dateDecouverte) || base.dateDecouverte,
      ),
      montant: toSafeString(commandeFrauduleuse?.montant ?? det?.montant),
      assurance: commandeFrauduleuse?.assurance ?? det?.assurance ?? base.assurance,
    };
  }

  private static mapCommandeFrauduleuseCoordonnees(det: any, base: PrePlainteFormFields, commandeFrauduleuse: any) {
    return {
      emailCommandeInconnu:
        commandeFrauduleuse?.emailCommandeInconnu ?? det?.emailCommandeInconnu ?? base.emailCommandeInconnu,
      emailCommande: toSafeString(commandeFrauduleuse?.emailCommande ?? det?.emailCommande),
      telephoneCommandeInconnu:
        commandeFrauduleuse?.telephoneCommandeInconnu ??
        det?.telephoneCommandeInconnu ??
        base.telephoneCommandeInconnu,
      telephoneCommande: toSafeString(commandeFrauduleuse?.telephoneCommande ?? det?.telephoneCommande),
      livraisonAdresseLesee:
        commandeFrauduleuse?.livraisonAdresseLesee ?? det?.livraisonAdresseLesee ?? base.livraisonAdresseLesee,
    };
  }

  private static mapCommandeFrauduleuseAdresse(
    base: PrePlainteFormFields,
    addrLivraison: ReturnType<typeof AdresseMapper.fromBackendAdresse>,
  ) {
    return {
      livraisonAdresse: addrLivraison.adresse ?? base.livraisonAdresse,
      livraisonAdressePostale: addrLivraison.adressePostale ?? base.livraisonAdressePostale,
      livraisonNpa: addrLivraison.npa ?? base.livraisonNpa,
      livraisonLocalite: addrLivraison.localite ?? base.livraisonLocalite,
      livraisonLocaliteCode: addrLivraison.localiteCode ?? base.livraisonLocaliteCode,
      livraisonPays: addrLivraison.pays ?? base.livraisonPays,
    };
  }

  private static mapCybercrimeAchatNonRecuFields(
    det: any,
    base: PrePlainteFormFields,
    achatNonRecu: any,
  ) {
    return {
      ...this.mapCybercrimeAchatNonRecuContactFields(det, achatNonRecu),
      ...this.mapCybercrimeAchatNonRecuVendeurFields(det, base, achatNonRecu),
      ...this.mapCybercrimeAchatNonRecuPlateformeFields(det, base, achatNonRecu),
      ...this.mapCybercrimeAchatNonRecuPaiementFields(det, base, achatNonRecu),
      ...this.mapCybercrimeAchatNonRecuIdentiteFields(det, base, achatNonRecu),
    };
  }

  private static mapCybercrimeAchatNonRecuContactFields(
    det: any,
    achatNonRecu: any,
  ) {
    return {
      montantDelitAchatLigne: toSafeString(achatNonRecu?.montantDelitAchatLigne ?? det?.montantDelitAchatLigne),
      articleNonLivreDescription: toSafeString(
        achatNonRecu?.articleNonLivreDescription ?? det?.articleNonLivreDescription,
      ),
    };
  }

  private static mapCybercrimeAchatNonRecuVendeurFields(det: any, base: PrePlainteFormFields, achatNonRecu: any) {
    return {
      prenomVendeur: toSafeString(achatNonRecu?.prenomVendeur ?? det?.prenomVendeur),
      nomVendeur: toSafeString(achatNonRecu?.nomVendeur ?? det?.nomVendeur),
      telephoneVendeurInconnu:
        achatNonRecu?.telephoneVendeurInconnu ?? det?.telephoneVendeurInconnu ?? base.telephoneVendeurInconnu,
      telephoneVendeur: toSafeString(achatNonRecu?.telephoneVendeur ?? det?.telephoneVendeur),
      emailVendeurInconnu: achatNonRecu?.emailVendeurInconnu ?? det?.emailVendeurInconnu ?? base.emailVendeurInconnu,
      emailVendeur: toSafeString(achatNonRecu?.emailVendeur ?? det?.emailVendeur),
      adresseVendeurInconnue:
        achatNonRecu?.adresseVendeurInconnue ?? det?.adresseVendeurInconnue ?? base.adresseVendeurInconnue,
      ...this.extractVendeurAdresseFields(achatNonRecu, det, base),
      nomEntrepriseVendeur: toSafeString(achatNonRecu?.nomEntrepriseVendeur ?? det?.nomEntrepriseVendeur),
      siteWebEntrepriseVendeur: toSafeString(achatNonRecu?.siteWebEntrepriseVendeur ?? det?.siteWebEntrepriseVendeur),
    };
  }

  private static mapCybercrimeAchatNonRecuPlateformeFields(det: any, base: PrePlainteFormFields, achatNonRecu: any) {
    return {
      achatViaPlaceMarche: achatNonRecu?.achatViaPlaceMarche ?? det?.achatViaPlaceMarche ?? base.achatViaPlaceMarche,
      plateforme: toSafeString(achatNonRecu?.plateformeUtilisee ?? det?.plateforme),
      plateformeAutre: toSafeString(achatNonRecu?.plateformeAutre ?? det?.plateformeAutre),
      plateformeId: toSafeString(achatNonRecu?.plateformeId ?? det?.plateformeId),
      annonceDocumentIndisponible:
        achatNonRecu?.annonceDocumentIndisponible ??
        det?.annonceDocumentIndisponible ??
        base.annonceDocumentIndisponible,
      raisonAbsenceAnnonce: toSafeString(achatNonRecu?.raisonAbsenceAnnonce ?? det?.raisonAbsenceAnnonce),
    };
  }

  private static mapCybercrimeAchatNonRecuPaiementFields(det: any, base: PrePlainteFormFields, achatNonRecu: any) {
    return {
      moyenPaiement: toSafeString(achatNonRecu?.moyenPaiement ?? det?.moyenPaiement),
      moyenPaiementAutre: toSafeString(achatNonRecu?.moyenPaiementAutre ?? det?.moyenPaiementAutre),
      ibanBeneficiaire: toSafeString(achatNonRecu?.ibanBeneficiaire ?? det?.ibanBeneficiaire),
      comptePaypalBeneficiaire: toSafeString(achatNonRecu?.comptePaypalBeneficiaire ?? det?.comptePaypalBeneficiaire),
      numeroTwintBeneficiaire: toSafeString(achatNonRecu?.numeroTwintBeneficiaire ?? det?.numeroTwintBeneficiaire),
      adresseWalletCrypto: toSafeString(achatNonRecu?.adresseWalletCrypto ?? det?.adresseWalletCrypto),
      hashTransactionCrypto: toSafeString(achatNonRecu?.hashTransactionCrypto ?? det?.hashTransactionCrypto),
      societeBeneficiaire: toSafeString(achatNonRecu?.societeBeneficiaire ?? det?.societeBeneficiaire),
      nomBeneficiaire: toSafeString(achatNonRecu?.nomBeneficiaire ?? det?.nomBeneficiaire),
      prenomBeneficiaire: toSafeString(achatNonRecu?.prenomBeneficiaire ?? det?.prenomBeneficiaire),
      dateOperation: fromIsoDate(achatNonRecu?.dateOperation ?? det?.dateOperation),
      preuvePaiementIndisponible:
        achatNonRecu?.preuvePaiementIndisponible ?? det?.preuvePaiementIndisponible ?? base.preuvePaiementIndisponible,
      raisonAbsencePreuvePaiement: toSafeString(
        achatNonRecu?.raisonAbsencePreuvePaiement ?? det?.raisonAbsencePreuvePaiement,
      ),
    };
  }

  private static mapCybercrimeAchatNonRecuIdentiteFields(det: any, base: PrePlainteFormFields, achatNonRecu: any) {
    return {
      copieIdentiteTransmiseAuteur:
        achatNonRecu?.copieIdentiteTransmiseAuteur ??
        det?.copieIdentiteTransmiseAuteur ??
        base.copieIdentiteTransmiseAuteur,
      copieIdentiteAuteurTransmise:
        achatNonRecu?.copieIdentiteAuteurTransmise ??
        det?.copieIdentiteAuteurTransmise ??
        base.copieIdentiteAuteurTransmise,
    };
  }

  private static mapCybercrimeFausseAnnonceFields(det: any, base: PrePlainteFormFields, fausseAnnonce: any) {
    return {
      ...this.mapAnnonceInfos(det, fausseAnnonce),
      ...this.mapAnnonceBailleur(det, base, fausseAnnonce),
      ...this.mapAnnoncePaiement(det, fausseAnnonce),
    };
  }

  private static mapAnnonceInfos(det: any, fausseAnnonce: any) {
    return {
      urlComplete: toSafeString(fausseAnnonce?.urlComplete ?? det?.urlComplete),
      titreAnnonce: toSafeString(fausseAnnonce?.titreAnnonce ?? det?.titreAnnonce),
    };
  }

  private static mapAnnonceBailleur(det: any, base: PrePlainteFormFields, fausseAnnonce: any) {
    return {
      nomBailleur: toSafeString(fausseAnnonce?.nomBailleur ?? det?.nomBailleur),
      emailBailleurInconnu:
        fausseAnnonce?.emailBailleurInconnu ?? det?.emailBailleurInconnu ?? base.emailBailleurInconnu,
      emailBailleur: toSafeString(fausseAnnonce?.emailBailleur ?? det?.emailBailleur),
      telephoneBailleurInconnu:
        fausseAnnonce?.telephoneBailleurInconnu ?? det?.telephoneBailleurInconnu ?? base.telephoneBailleurInconnu,
      telephoneBailleur: toSafeString(fausseAnnonce?.telephoneBailleur ?? det?.telephoneBailleur),
      adresseBienImmobilier: toSafeString(fausseAnnonce?.adresseBienImmobilier ?? det?.adresseBienImmobilier),
    };
  }

  private static mapAnnoncePaiement(det: any, fausseAnnonce: any) {
    return {
      montantDemande: toSafeString(fausseAnnonce?.montantDemande ?? det?.montantDemande),
      modePaiementDemande: toSafeString(fausseAnnonce?.modePaiementDemande ?? det?.modePaiementDemande),
    };
  }

  private static mapCybercrimeFiles(det: any, base: PrePlainteFormFields) {
    return {
      justificatifsPaiement: this.toFiles(det?.justificatifsPaiement, base.justificatifsPaiement ?? []),
      copiesEcran: this.toFiles(det?.copiesEcran, base.copiesEcran ?? []),
      autresDocuments: this.toFiles(det?.autresDocuments ?? det?.fichiersCybercrime, base.autresDocuments ?? []),
    };
  }

  /** Réinitialise les champs cyber depuis le formulaire de base quand l’incident n’est pas un cybercrime. */
  private static cybercrimeFieldsFromBase(base: PrePlainteFormFields): Partial<PrePlainteFormFields> {
    return {
      typeCybercrime: base.typeCybercrime,
      descriptionCybercrime: base.descriptionCybercrime,
      prestataire: base.prestataire,
      dateDecouverte: base.dateDecouverte,
      montant: base.montant,
      assurance: base.assurance,
      emailCommandeInconnu: base.emailCommandeInconnu,
      emailCommande: base.emailCommande,
      telephoneCommandeInconnu: base.telephoneCommandeInconnu,
      telephoneCommande: base.telephoneCommande,
      livraisonAdresseLesee: base.livraisonAdresseLesee,
      livraisonAdresse: base.livraisonAdresse,
      livraisonAdressePostale: base.livraisonAdressePostale,
      livraisonNpa: base.livraisonNpa,
      livraisonLocalite: base.livraisonLocalite,
      livraisonLocaliteCode: base.livraisonLocaliteCode,
      livraisonPays: base.livraisonPays,
      datePremierContact: base.datePremierContact,
      heurePremierContact: base.heurePremierContact,
      dateDernierContact: base.dateDernierContact,
      heureDernierContact: base.heureDernierContact,
      montantDelitAchatLigne: base.montantDelitAchatLigne,
      articleNonLivreDescription: base.articleNonLivreDescription,
      prenomVendeur: base.prenomVendeur,
      nomVendeur: base.nomVendeur,
      telephoneVendeurInconnu: base.telephoneVendeurInconnu,
      telephoneVendeur: base.telephoneVendeur,
      emailVendeurInconnu: base.emailVendeurInconnu,
      emailVendeur: base.emailVendeur,
      adresseVendeurInconnue: base.adresseVendeurInconnue,
      vendeurAdresse: base.vendeurAdresse,
      vendeurAdressePostale: base.vendeurAdressePostale,
      vendeurNpa: base.vendeurNpa,
      vendeurLocalite: base.vendeurLocalite,
      vendeurLocaliteCode: base.vendeurLocaliteCode,
      vendeurPays: base.vendeurPays,
      achatViaPlaceMarche: base.achatViaPlaceMarche,
      plateforme: base.plateforme,
      plateformeAutre: base.plateformeAutre,
      plateformeId: base.plateformeId,
      nomEntrepriseVendeur: base.nomEntrepriseVendeur,
      siteWebEntrepriseVendeur: base.siteWebEntrepriseVendeur,
      annonceDocument: base.annonceDocument,
      annonceDocumentIndisponible: base.annonceDocumentIndisponible,
      raisonAbsenceAnnonce: base.raisonAbsenceAnnonce,
      moyenPaiement: base.moyenPaiement,
      moyenPaiementAutre: base.moyenPaiementAutre,
      ibanBeneficiaire: base.ibanBeneficiaire,
      comptePaypalBeneficiaire: base.comptePaypalBeneficiaire,
      numeroTwintBeneficiaire: base.numeroTwintBeneficiaire,
      adresseWalletCrypto: base.adresseWalletCrypto,
      hashTransactionCrypto: base.hashTransactionCrypto,
      societeBeneficiaire: base.societeBeneficiaire,
      nomBeneficiaire: base.nomBeneficiaire,
      prenomBeneficiaire: base.prenomBeneficiaire,
      dateOperation: base.dateOperation,
      preuvePaiementDocument: base.preuvePaiementDocument,
      preuvePaiementIndisponible: base.preuvePaiementIndisponible,
      raisonAbsencePreuvePaiement: base.raisonAbsencePreuvePaiement,
      copieIdentiteTransmiseAuteur: base.copieIdentiteTransmiseAuteur,
      copieIdentiteTransmiseAuteurDocument: base.copieIdentiteTransmiseAuteurDocument,
      copieIdentiteAuteurTransmise: base.copieIdentiteAuteurTransmise,
      copieIdentiteAuteurDocument: base.copieIdentiteAuteurDocument,
      urlComplete: base.urlComplete,
      titreAnnonce: base.titreAnnonce,
      nomBailleur: base.nomBailleur,
      emailBailleurInconnu: base.emailBailleurInconnu,
      emailBailleur: base.emailBailleur,
      telephoneBailleurInconnu: base.telephoneBailleurInconnu,
      telephoneBailleur: base.telephoneBailleur,
      adresseBienImmobilier: base.adresseBienImmobilier,
      montantDemande: base.montantDemande,
      modePaiementDemande: base.modePaiementDemande,
      justificatifsPaiement: base.justificatifsPaiement,
      copiesEcran: base.copiesEcran,
      autresDocuments: base.autresDocuments,
    };
  }

  private static extractVendeurAdresseFields(
    achatNonRecu: Record<string, unknown>,
    det: Record<string, unknown>,
    base: PrePlainteFormFields,
  ): Pick<
    PrePlainteFormFields,
    | "vendeurAdresse"
    | "vendeurAdressePostale"
    | "vendeurNpa"
    | "vendeurLocalite"
    | "vendeurLocaliteCode"
    | "vendeurPays"
  > {
    if (achatNonRecu?.adresseVendeurInconnue === true || det?.adresseVendeurInconnue === true) {
      return {
        vendeurAdresse: "",
        vendeurAdressePostale: "",
        vendeurNpa: "",
        vendeurLocalite: "",
        vendeurLocaliteCode: "",
        vendeurPays: "",
      };
    }
    const rawAddr = achatNonRecu?.adresseVendeur ?? det?.adresseVendeur;
    if (rawAddr && typeof rawAddr === "object" && !Array.isArray(rawAddr)) {
      const a = AdresseMapper.fromBackendAdresse(rawAddr as any);
      return {
        vendeurAdresse: a.adresse,
        vendeurAdressePostale: a.adressePostale,
        vendeurNpa: a.npa,
        vendeurLocalite: a.localite,
        vendeurLocaliteCode: a.localiteCode,
        vendeurPays: a.paysCode,
      };
    }
    const legacyLine = typeof rawAddr === "string" ? toSafeString(rawAddr) : "";
    const legacyPays = toSafeString((det as { paysVendeur?: string })?.paysVendeur);
    return {
      vendeurAdresse: legacyLine || base.vendeurAdresse || "",
      vendeurAdressePostale: base.vendeurAdressePostale || "",
      vendeurNpa: base.vendeurNpa || "",
      vendeurLocalite: base.vendeurLocalite || "",
      vendeurLocaliteCode: base.vendeurLocaliteCode || "",
      vendeurPays: legacyPays || base.vendeurPays || "",
    };
  }

  private static getIncidentObject(det: any, context: IncidentContext) {
    if (context.isVol) {
      return det?.objetsVoles?.[0];
    }
    if (context.isDom) {
      return det?.objetDegrades?.[0];
    }
    return undefined;
  }

  private static getFieldSource<T extends object>(
    det: any,
    base: T,
    context: IncidentContext,
    field: keyof T | string,
  ) {
    const incidentObject = this.getIncidentObject(det, context);
    return incidentObject?.[field] ?? base[field as keyof T];
  }

  private static getFieldSourceBool<T extends object>(
    det: any,
    base: T,
    context: IncidentContext,
    field: keyof T | string,
  ): boolean {
    return !!this.getFieldSource(det, base, context, field);
  }

  private static getVolObjectFieldSource<T extends object>(det: any, base: T, field: keyof T | string) {
    return det?.objetsVoles?.[0]?.[field] ?? base[field as keyof T];
  }

  private static getVolObjectFieldSourceBool<T extends object>(det: any, base: T, field: keyof T | string): boolean {
    return !!this.getVolObjectFieldSource(det, base, field);
  }

  private static normalizeTypeIncidentForForm(backendValue: string | undefined): IncidentFormType {
    if (!backendValue) {
      return "";
    }
    if (backendValue === TypeIncident.DOMMAGE) {
      return FORM_TYPE_INCIDENT.DOMMAGE;
    }
    if (backendValue === TypeIncident.VOL) {
      return FORM_TYPE_INCIDENT.VOL;
    }
    if (backendValue === TypeIncident.CYBERCRIME) {
      return FORM_TYPE_INCIDENT.CYBERCRIME;
    }
    return backendValue as IncidentFormType;
  }

  private static toFiles(value: FichierDTO | FichierDTO[] | null | undefined, fallback: File[] = []): File[] {
    if (!value) {
      return fallback;
    }

    const fichiers = Array.isArray(value) ? value : [value];
    const result: File[] = [];

    for (const fichier of fichiers) {
      const file = this.toFile(fichier);
      if (file) {
        result.push(file);
      }
    }

    return result;
  }

  private static toFile(fichier: FichierDTO | null | undefined): File | undefined {
    if (!fichier?.nom || !fichier?.contenuBase64) {
      return undefined;
    }

    const mimeType = this.normalizeMimeType(fichier.typeMime);
    const byteCharacters = atob(fichier.contenuBase64);
    const byteNumbers = Array.from(byteCharacters, character => character.codePointAt(0) ?? 0);
    const byteArray = new Uint8Array(byteNumbers);
    const blob = new Blob([byteArray], { type: mimeType });

    return new File([blob], fichier.nom, { type: mimeType });
  }

  private static normalizeMimeType(typeMime: string | undefined): string {
    const normalized = toSafeString(typeMime).toLowerCase();

    if (!normalized) {
      return "application/octet-stream";
    }
    if (normalized.includes("/")) {
      return normalized;
    }
    if (normalized === "jpg" || normalized === "jpeg") {
      return "image/jpeg";
    }
    if (normalized === "png") {
      return "image/png";
    }
    if (normalized === "pdf") {
      return "application/pdf";
    }

    return "application/octet-stream";
  }
}
