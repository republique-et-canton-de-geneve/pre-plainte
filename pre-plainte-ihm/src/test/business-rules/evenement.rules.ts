import type { BusinessRule } from "./business-rule.types";

export interface EvenementRuleData extends Record<string, unknown> {
  typeIncident?: string;
  dateDebutEvenement?: string;
  heureDebutEvenement?: string;
  dateFinEvenement?: string;
  heureFinEvenement?: string;
  adresseLesee?: boolean | null;
  adresseConnue?: boolean | null;
  isTrajet?: boolean | null;
  typeLieu?: { code: string; label: string } | null;
  adresseEvenement?: string;
  adressePostaleEvenement?: string;
  npaEvenement?: string;
  localiteEvenement?: string;
  paysEvenement?: string;
  volDansVehicule?: boolean | null;
  categorieObjet?: string;
  objetsVolesValides?: unknown[];
  sousCategorie?: string;
  typeObjet?: { code: string; label: string } | null;
  fabricant?: { code: string; label: string } | null;
  modele?: { code: string; label: string } | null;
  couleur?: { code: string; label: string } | null;
  plaqueInconnu?: boolean;
  plaquePays?: { code: string; label: string } | null;
  plaqueCanton?: { code: string; label: string } | null;
  plaqueNumero?: string;
  numeroSerie?: string;
  numeroSerieInconnu?: boolean;
  numeroIMEI?: string;
  numeroIMEIInconnu?: boolean;
  avezVousDegradation?: boolean | null;
  typeDommage?: string;
  naturesDommage?: string[];
  description?: string;
  constatPresent?: boolean | null;
  dateConstat?: string;
  fichiers?: File[];
  typeCybercrime?: string;
  descriptionCybercrime?: string;
  prestataire?: string;
  dateDecouverte?: string;
  montant?: string;
  assurance?: boolean | null;
  emailCommandeInconnu?: boolean;
  emailCommande?: string;
  telephoneCommandeInconnu?: boolean;
  telephoneCommande?: string;
  livraisonAdresseLesee?: boolean | null;
  datePremierContact?: string;
  heurePremierContact?: string;
  dateDernierContact?: string;
  heureDernierContact?: string;
  montantDelitAchatLigne?: string;
  articleNonLivreDescription?: string;
  prenomVendeur?: string;
  nomVendeur?: string;
  emailVendeurInconnu?: boolean;
  emailVendeur?: string;
  telephoneVendeurInconnu?: boolean;
  telephoneVendeur?: string;
  achatViaPlaceMarche?: boolean | null;
  plateforme?: string;
  plateformeId?: string;
  annonceDocumentIndisponible?: boolean;
  raisonAbsenceAnnonce?: string;
  moyenPaiement?: string;
  ibanBeneficiaire?: string;
  dateOperation?: string;
  preuvePaiementIndisponible?: boolean;
  raisonAbsencePreuvePaiement?: string;
  copieIdentiteTransmiseAuteur?: boolean | null;
  copieIdentiteAuteurTransmise?: boolean | null;
  urlComplete?: string;
  titreAnnonce?: string;
  nomBailleur?: string;
  emailBailleurInconnu?: boolean;
  emailBailleur?: string;
  telephoneBailleurInconnu?: boolean;
  telephoneBailleur?: string;
  adresseBienImmobilier?: string;
  montantDemande?: string;
  modePaiementDemande?: string;
}

const typeLieuPublic = { code: "PUBLIC", label: "Lieu public" };
const telephoneMobile = { code: "713103", label: "Telephone mobile" };
const noir = { code: "11", label: "Noir" };
const suisse = { code: "8100", label: "Suisse" };
const geneve = { code: "GE", label: "Geneve" };

export const donneesEvenementValides: EvenementRuleData = {
  typeIncident: "vol",
  dateDebutEvenement: "01.01.2024",
  heureDebutEvenement: "10:00",
  dateFinEvenement: "01.01.2024",
  heureFinEvenement: "11:00",
  adresseLesee: true,
  paysEvenement: "8100",
  volDansVehicule: false,
  categorieObjet: "plaque",
  plaqueInconnu: true,
  avezVousDegradation: false,
};

export const reglesEvenement: BusinessRule<EvenementRuleData>[] = [
  {
    section: "Informations sur l'evenement",
    champDemande: "Type d'incident",
    obligatoire: "Oui",
    precision: "Le type d'incident doit etre selectionne avant de renseigner les details.",
    examples: [
      {
        label: "type d'incident absent est refuse",
        data: {
          typeIncident: "",
        },
        valid: false,
        errorPath: ["typeIncident"],
        errorMessage: "validation.typeIncidentRequis",
      },
      {
        label: "type vol est accepte avec les champs minimaux",
        data: {},
        valid: true,
      },
    ],
  },
  {
    section: "Informations sur l'evenement",
    champDemande: "Date et heure de l'evenement",
    obligatoire: "Oui",
    precision: "Les dates et heures de debut et de fin sont obligatoires et la fin doit etre posterieure au debut.",
    examples: [
      {
        label: "date de debut absente est refusee",
        data: {
          dateDebutEvenement: "",
        },
        valid: false,
        errorPath: ["dateDebutEvenement"],
        errorMessage: "validation.dateDebutEvenementRequise",
      },
      {
        label: "heure de fin avant heure de debut est refusee",
        data: {
          heureDebutEvenement: "11:00",
          heureFinEvenement: "10:00",
        },
        valid: false,
        errorPath: ["heureFinEvenement"],
        errorMessage: "validation.dateFinEvenementApresDebut",
      },
      {
        label: "chronologie correcte est acceptee",
        data: {
          heureDebutEvenement: "10:00",
          heureFinEvenement: "11:00",
        },
        valid: true,
      },
    ],
  },
  {
    section: "Informations sur l'evenement",
    champDemande: "Adresse de l'evenement",
    obligatoire: "Selon le cas",
    precision: "Obligatoire lorsque l'adresse de l'evenement est connue et differente de l'adresse de la personne lesee.",
    examples: [
      {
        label: "adresse connue trop courte est refusee",
        data: {
          adresseLesee: false,
          adresseConnue: true,
          typeLieu: typeLieuPublic,
          adresseEvenement: "Rue",
          adressePostaleEvenement: "10",
          npaEvenement: "1204",
          localiteEvenement: "Geneve",
        },
        valid: false,
        errorPath: ["adresseEvenement"],
        errorMessage: "validation.adresseEvenementRequise",
      },
      {
        label: "adresse de la personne lesee reutilisee est acceptee",
        data: {
          adresseLesee: true,
        },
        valid: true,
      },
    ],
  },
  {
    section: "Vol",
    champDemande: "Informations de base de l'objet vole",
    obligatoire: "Oui",
    precision: "Un vol doit indiquer si l'objet etait dans un vehicule, la categorie de l'objet et l'existence de degradations.",
    examples: [
      {
        label: "indication vol dans vehicule absente est refusee",
        data: {
          volDansVehicule: null,
        },
        valid: false,
        errorPath: ["volDansVehicule"],
        errorMessage: "validation.volDansVehiculeRequis",
      },
      {
        label: "categorie d'objet absente est refusee",
        data: {
          categorieObjet: "",
        },
        valid: false,
        errorPath: ["categorieObjet"],
        errorMessage: "validation.categorieObjetRequise",
      },
      {
        label: "information sur les degradations absente est refusee",
        data: {
          avezVousDegradation: null,
        },
        valid: false,
        errorPath: ["avezVousDegradation"],
        errorMessage: "validation.degradationsRequis",
      },
    ],
  },
  {
    section: "Informations sur l'evenement",
    champDemande: "Adresse de l'evenement",
    obligatoire: "Oui",
    precision: "L'adresse connue et le type de lieu doivent etre renseignes quand l'adresse n'est pas celle du lese.",
    examples: [
      {
        label: "adresse connue absente est refusee",
        data: {
          adresseLesee: false,
          adresseConnue: null,
        },
        valid: false,
        errorPath: ["adresseConnue"],
        errorMessage: "validation.adresseConnueRequise",
      },
      {
        label: "type de lieu absent est refuse",
        data: {
          adresseLesee: false,
          adresseConnue: true,
          typeLieu: null,
          adresseEvenement: "Rue du Test 1",
          npaEvenement: "1200",
          localiteEvenement: "Geneve",
          paysEvenement: "8100",
        },
        valid: false,
        errorPath: ["typeLieu"],
        errorMessage: "validation.typeLieuRequis",
      },
    ],
  },
  {
    section: "Vol",
    champDemande: "Numero de serie",
    obligatoire: "Selon le cas",
    precision: "Obligatoire pour certains objets comme telephone, informatique ou photo/video sauf si le numero est inconnu.",
    examples: [
      {
        label: "telephone sans numero de serie est refuse",
        data: {
          categorieObjet: "telephone",
          typeObjet: telephoneMobile,
          couleur: noir,
          numeroSerie: "",
          numeroSerieInconnu: false,
          numeroIMEIInconnu: true,
          justificationAbsenceIMEI: "Boite introuvable",
        },
        valid: false,
        errorPath: ["numeroSerie"],
        errorMessage: "validation.numeroSerieRequis",
      },
      {
        label: "telephone avec numero de serie est accepte",
        data: {
          categorieObjet: "telephone",
          typeObjet: telephoneMobile,
          couleur: noir,
          numeroSerie: "SN123456",
          numeroSerieInconnu: false,
          numeroIMEIInconnu: true,
          justificationAbsenceIMEI: "Appareil perdu sans boite",
        },
        valid: true,
      },
    ],
  },
  {
    section: "Vol",
    champDemande: "Numero IMEI",
    obligatoire: "Selon le cas",
    precision: "Obligatoire et compose de 15 chiffres pour un telephone mobile sauf si le numero est inconnu, auquel cas une justification est requise.",
    examples: [
      {
        label: "telephone mobile sans IMEI est refuse",
        data: {
          categorieObjet: "telephone",
          typeObjet: telephoneMobile,
          couleur: noir,
          numeroSerie: "SN123456",
          numeroIMEI: "",
          numeroIMEIInconnu: false,
        },
        valid: false,
        errorPath: ["numeroIMEI"],
        errorMessage: "validation.numeroIMEIRequis",
      },
      {
        label: "IMEI avec moins de 15 chiffres est refuse",
        data: {
          categorieObjet: "telephone",
          typeObjet: telephoneMobile,
          couleur: noir,
          numeroSerie: "SN123456",
          numeroIMEI: "123",
          numeroIMEIInconnu: false,
        },
        valid: false,
        errorPath: ["numeroIMEI"],
        errorMessage: "validation.numeroIMEIFormat",
      },
      {
        label: "IMEI de 15 chiffres est accepte",
        data: {
          categorieObjet: "telephone",
          typeObjet: telephoneMobile,
          couleur: noir,
          numeroSerie: "SN123456",
          numeroIMEI: "123456789012345",
          numeroIMEIInconnu: false,
        },
        valid: true,
      },
      {
        label: "IMEI inconnu sans justification est refuse",
        data: {
          categorieObjet: "telephone",
          typeObjet: telephoneMobile,
          couleur: noir,
          numeroSerie: "SN123456",
          numeroIMEIInconnu: true,
          justificationAbsenceIMEI: "",
        },
        valid: false,
        errorPath: ["justificationAbsenceIMEI"],
        errorMessage: "validation.justificationAbsenceIMEIRequise",
      },
      {
        label: "IMEI inconnu avec justification est accepte",
        data: {
          categorieObjet: "telephone",
          typeObjet: telephoneMobile,
          couleur: noir,
          numeroSerie: "SN123456",
          numeroIMEIInconnu: true,
          justificationAbsenceIMEI: "Boite et facture introuvables",
        },
        valid: true,
      },
    ],
  },
  {
    section: "Vol",
    champDemande: "Plaque d'immatriculation",
    obligatoire: "Selon le cas",
    precision: "Le pays et le numero de plaque sont obligatoires lorsque la plaque est renseignee et non marquee comme inconnue.",
    examples: [
      {
        label: "plaque suisse sans canton est refusee pour un vehicule",
        data: {
          categorieObjet: "vehicule",
          sousCategorie: "voitures",
          typeObjet: { code: "AUTO", label: "Voiture" },
          fabricant: { code: "VW", label: "Volkswagen" },
          modele: { code: "GOLF", label: "Golf" },
          couleur: noir,
          plaqueInconnu: false,
          plaquePays: suisse,
          plaqueCanton: null,
          plaqueNumero: "GE 123456",
        },
        valid: false,
        errorPath: ["plaqueCanton"],
        errorMessage: "validation.plaqueCantonRequis",
      },
      {
        label: "plaque suisse complete est acceptee",
        data: {
          categorieObjet: "vehicule",
          sousCategorie: "voitures",
          typeObjet: { code: "AUTO", label: "Voiture" },
          fabricant: { code: "VW", label: "Volkswagen" },
          modele: { code: "GOLF", label: "Golf" },
          couleur: noir,
          plaqueInconnu: false,
          plaquePays: suisse,
          plaqueCanton: geneve,
          plaqueNumero: "GE 123456",
        },
        valid: true,
      },
    ],
  },
  {
    section: "Dommages materiels",
    champDemande: "Nature du dommage",
    obligatoire: "Oui",
    precision: "Un dommage materiel doit indiquer le type de dommage, au moins une nature et une description.",
    examples: [
      {
        label: "nature du dommage absente est refusee",
        data: {
          typeIncident: "degat-delit",
          typeDommage: "mobilier",
          naturesDommage: [],
          description: "Vitre cassee",
          constatPresent: true,
          dateConstat: "01.01.2024",
          fichiers: [],
        },
        valid: false,
        errorPath: ["naturesDommage"],
        errorMessage: "validation.natureDommageRequis",
      },
    ],
  },
  {
    section: "Dommages materiels",
    champDemande: "Constat de police",
    obligatoire: "Selon le cas",
    precision: "Le constat de police doit etre renseigne pour un dommage vehicule ou batiment. Les fichiers restent optionnels.",
    examples: [
      {
        label: "dommage vehicule sans indication de constat est refuse",
        data: {
          typeIncident: "degat-delit",
          typeDommage: "dommage-vehicule",
          naturesDommage: ["bris"],
          description: "Vitre cassee",
          constatPresent: null,
        },
        valid: false,
        errorPath: ["constatPresent"],
        errorMessage: "validation.constatRequis",
      },
      {
        label: "dommage vehicule sans constat est accepte",
        data: {
          typeIncident: "degat-delit",
          typeDommage: "dommage-vehicule",
          naturesDommage: ["bris"],
          description: "Vitre cassee",
          constatPresent: false,
          fichiers: [],
        },
        valid: true,
      },
      {
        label: "dommage autre sans indication de constat est accepte",
        data: {
          typeIncident: "degat-delit",
          typeDommage: "autre",
          naturesDommage: ["bris"],
          description: "Vitre cassee",
          constatPresent: null,
        },
        valid: true,
      },
    ],
  },
  {
    section: "Cybercriminalite",
    champDemande: "Type de cybercriminalite",
    obligatoire: "Oui",
    precision: "Le type de cybercriminalite est obligatoire lorsqu'un incident de cybercriminalite est declare.",
    examples: [
      {
        label: "type de cybercriminalite absent est refuse",
        data: {
          typeIncident: "cybercrime",
          typeCybercrime: "",
        },
        valid: false,
        errorPath: ["typeCybercrime"],
        errorMessage: "validation.typeCybercrimeRequis",
      },
    ],
  },
  {
    section: "Cybercriminalite - commande frauduleuse",
    champDemande: "Coordonnees de commande",
    obligatoire: "Oui",
    precision: "Une commande frauduleuse doit renseigner le prestataire, la date de decouverte, le montant, l'assurance, les coordonnees de commande et l'adresse de livraison.",
    examples: [
      {
        label: "prestataire absent est refuse",
        data: {
          typeIncident: "cybercrime",
          typeCybercrime: "commande-frauduleuse",
          prestataire: "",
        },
        valid: false,
        errorPath: ["prestataire"],
        errorMessage: "validation.prestataireRequis",
      },
      {
        label: "email de commande invalide est refuse",
        data: {
          typeIncident: "cybercrime",
          typeCybercrime: "commande-frauduleuse",
          prestataire: "Boutique",
          dateDecouverte: "01.01.2024",
          montant: "120",
          assurance: false,
          emailCommandeInconnu: false,
          emailCommande: "email-invalide",
          telephoneCommandeInconnu: true,
          livraisonAdresseLesee: true,
        },
        valid: false,
        errorPath: ["emailCommande"],
        errorMessage: "validation.emailCommandeFormat",
      },
    ],
  },
  {
    section: "Cybercriminalite - achat non recu",
    champDemande: "Paiement",
    obligatoire: "Oui",
    precision: "Un achat non recu doit indiquer le moyen de paiement et les informations propres au moyen choisi.",
    examples: [
      {
        label: "moyen de paiement absent est refuse",
        data: {
          typeIncident: "cybercrime",
          typeCybercrime: "achat-non-recu",
          moyenPaiement: "",
        },
        valid: false,
        errorPath: ["moyenPaiement"],
        errorMessage: "validation.moyenPaiementRequis",
      },
      {
        label: "paiement par IBAN sans beneficiaire est refuse",
        data: {
          typeIncident: "cybercrime",
          typeCybercrime: "achat-non-recu",
          moyenPaiement: "iban",
          ibanBeneficiaire: "",
        },
        valid: false,
        errorPath: ["ibanBeneficiaire"],
        errorMessage: "validation.ibanBeneficiaireRequis",
      },
    ],
  },
  {
    section: "Cybercriminalite - fausse annonce",
    champDemande: "Annonce",
    obligatoire: "Oui",
    precision: "Une fausse annonce doit contenir une URL valide, un titre, le bailleur, ses coordonnees, l'adresse du bien, le montant et le mode de paiement demande.",
    examples: [
      {
        label: "URL d'annonce absente est refusee",
        data: {
          typeIncident: "cybercrime",
          typeCybercrime: "fausse-annonce",
          datePremierContact: "01.01.2024",
          heurePremierContact: "10:00",
          dateDernierContact: "01.01.2024",
          heureDernierContact: "11:00",
          urlComplete: "",
        },
        valid: false,
        errorPath: ["urlComplete"],
        errorMessage: "validation.urlCompleteRequise",
      },
      {
        label: "email du bailleur invalide est refuse",
        data: {
          typeIncident: "cybercrime",
          typeCybercrime: "fausse-annonce",
          datePremierContact: "01.01.2024",
          heurePremierContact: "10:00",
          dateDernierContact: "01.01.2024",
          heureDernierContact: "11:00",
          urlComplete: "https://example.com/annonce",
          titreAnnonce: "Appartement",
          nomBailleur: "Durand",
          emailBailleurInconnu: false,
          emailBailleur: "email-invalide",
          telephoneBailleurInconnu: true,
          adresseBienImmobilier: "Rue du Lac 1",
          montantDemande: "1500",
          modePaiementDemande: "Virement",
        },
        valid: false,
        errorPath: ["emailBailleur"],
        errorMessage: "validation.emailBailleurFormat",
      },
    ],
  },
];
