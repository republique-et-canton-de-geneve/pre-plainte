import { ripolSelection } from "../stubs/ripol";

const EMAIL_VERIFIE = "anne.martin@example.org";
const TYPE_INCIDENT_VOL = "vol";
const DATE_EVENEMENT = "20.05.2026";
const HEURE_DEBUT_EVENEMENT = "10:00";
const HEURE_FIN_EVENEMENT = "11:00";
const PAYS_SUISSE = "8100";
const ADRESSE_RUE = "Rue du Marche 10";
const ADRESSE_NUMERO = "10";
const ADRESSE_NPA = "1201";
const ADRESSE_LOCALITE = "Geneve";
const CODE_ORDINATEUR_PORTABLE = "722100";
const LIBELLE_ORDINATEUR_PORTABLE = "Ordinateur portable";
const CODE_COULEUR_NOIR = "NOIR";
const LIBELLE_COULEUR_NOIR = "Noir";
const DATE_RENDEZ_VOUS = "2026-07-02";
const HEURE_DEBUT_RENDEZ_VOUS = HEURE_DEBUT_EVENEMENT;
const HEURE_FIN_RENDEZ_VOUS = HEURE_FIN_EVENEMENT;
const BEGIN_DATE_TIME_RENDEZ_VOUS = `20260702 ${HEURE_DEBUT_RENDEZ_VOUS}`;
const END_DATE_TIME_RENDEZ_VOUS = `20260702 ${HEURE_FIN_RENDEZ_VOUS}`;
const CRENEAU_PREFERE_RENDEZ_VOUS = `02.07.2026 ${HEURE_DEBUT_RENDEZ_VOUS} - ${HEURE_FIN_RENDEZ_VOUS} @ Poste PPEL`;
const DATE_DEBUT_EVENT_DTO = `2026-05-20T${HEURE_DEBUT_EVENEMENT}:00`;
const DATE_FIN_EVENT_DTO = `2026-05-20T${HEURE_FIN_EVENEMENT}:00`;

export const donneesEmailVerifie = {
  email: EMAIL_VERIFIE,
  confirmationEmail: "123456",
};

export const declarantSuisseValide = {
  ...donneesEmailVerifie,
  lienAvecPersonne: "MOI_MEME",
  telephone: "+41791234567",
  nom: "Martin",
  prenom: "Anne",
  genre: ripolSelection("2", "Féminin"),
  nationalite: ripolSelection(PAYS_SUISSE, "Suisse"),
  lieuOrigine: ripolSelection("6621", "Geneve"),
  dateNaissance: "15.04.1985",
  pays: PAYS_SUISSE,
  adresse: ADRESSE_RUE,
  adressePostale: ADRESSE_NUMERO,
  npa: ADRESSE_NPA,
  localite: ADRESSE_LOCALITE,
  typeDocumentIdentite: "carte_identite",
  numeroDocumentIdentite: "ID1234567",
};

export const objetInformatiqueVoleValide = {
  categorieObjet: "informatique",
  sousCategorie: "ordinateur_portable",
  typeObjet: ripolSelection(CODE_ORDINATEUR_PORTABLE, LIBELLE_ORDINATEUR_PORTABLE),
  fabricant: null,
  fabricantAutre: "",
  modele: null,
  modeleAutre: "",
  couleur: ripolSelection(CODE_COULEUR_NOIR, LIBELLE_COULEUR_NOIR),
  couleurSecondaire: null,
  gravure: "",
  valeurReelle: "",
  numeroSerie: "SN123456",
  numeroSerieInconnu: false,
  numeroIMEI: "",
  numeroIMEIInconnu: false,
  justificationAbsenceIMEI: "",
  isVehicle: false,
};

export const evenementVolSimpleValide = {
  ...declarantSuisseValide,
  typeIncident: TYPE_INCIDENT_VOL,
  dateDebutEvenement: DATE_EVENEMENT,
  heureDebutEvenement: HEURE_DEBUT_EVENEMENT,
  dateFinEvenement: DATE_EVENEMENT,
  heureFinEvenement: HEURE_FIN_EVENEMENT,
  adresseLesee: true,
  adresseConnue: false,
  isTrajet: false,
  paysEvenement: PAYS_SUISSE,
  volDansVehicule: false,
  categorieObjet: "",
  sousCategorie: "",
  typeObjet: null,
  couleur: null,
  numeroSerie: "",
  numeroSerieInconnu: false,
  objetsVolesValides: [objetInformatiqueVoleValide],
  avezVousDegradation: false,
};

export const evenementPlaqueVolee = {
  ...declarantSuisseValide,
  typeIncident: TYPE_INCIDENT_VOL,
  dateDebutEvenement: DATE_EVENEMENT,
  heureDebutEvenement: HEURE_DEBUT_EVENEMENT,
  dateFinEvenement: DATE_EVENEMENT,
  heureFinEvenement: HEURE_FIN_EVENEMENT,
  adresseLesee: true,
  adresseConnue: false,
  isTrajet: false,
  paysEvenement: PAYS_SUISSE,
  volDansVehicule: false,
  categorieObjet: "plaque",
  plaquePays: ripolSelection(PAYS_SUISSE, "Suisse"),
  plaqueCanton: null,
  plaqueNumero: "",
  plaqueInconnu: false,
  objetsVolesValides: [],
  avezVousDegradation: false,
};

export const evenementDommageAvecConstat = {
  ...declarantSuisseValide,
  typeIncident: "degat-delit",
  typeDommage: "dommage-propriete",
  dateDebutEvenement: DATE_EVENEMENT,
  heureDebutEvenement: HEURE_DEBUT_EVENEMENT,
  dateFinEvenement: DATE_EVENEMENT,
  heureFinEvenement: HEURE_FIN_EVENEMENT,
  adresseLesee: true,
  adresseConnue: false,
  isTrajet: false,
  paysEvenement: PAYS_SUISSE,
  constatPresent: true,
  dateConstat: "21.05.2026",
  fichiers: [],
  montantEstime: "500",
  devise: "CHF",
  naturesDommage: ["degradations"],
  description: "Vitre endommagee sur la porte principale",
};

export const evenementCybercrimeAchatNonRecu = {
  ...declarantSuisseValide,
  typeIncident: "cybercrime",
  typeCybercrime: "achat-non-recu",
  datePremierContact: DATE_EVENEMENT,
  heurePremierContact: HEURE_DEBUT_EVENEMENT,
  dateDernierContact: "21.05.2026",
  heureDernierContact: HEURE_FIN_EVENEMENT,
  montantDelitAchatLigne: "250",
  descriptionCybercrime: "Achat paye en ligne mais aucun article recu apres plusieurs relances",
  articleNonLivreDescription: `${LIBELLE_ORDINATEUR_PORTABLE} annonce comme neuf`,
  prenomVendeur: "Paul",
  nomVendeur: "Durand",
  telephoneVendeurInconnu: true,
  telephoneVendeur: "",
  emailVendeurInconnu: true,
  emailVendeur: "",
  adresseVendeurInconnue: true,
  achatViaPlaceMarche: true,
  plateforme: "ricardo",
  plateformeId: "https://example.org/annonce-123",
  annonceDocumentIndisponible: true,
  raisonAbsenceAnnonce: "Annonce supprimee par la plateforme",
  moyenPaiement: "iban",
  ibanBeneficiaire: "",
  dateOperation: DATE_EVENEMENT,
  preuvePaiementIndisponible: true,
  raisonAbsencePreuvePaiement: "Preuve indisponible dans l'immediat",
  copieIdentiteTransmiseAuteur: false,
  copieIdentiteAuteurTransmise: false,
};

export const creneauRendezVousValide = {
  id: "creneau-cypress",
  date: DATE_RENDEZ_VOUS,
  dateAffichee: "02.07.2026",
  heureDebut: HEURE_DEBUT_RENDEZ_VOUS,
  heureFin: HEURE_FIN_RENDEZ_VOUS,
  lieu: "Poste PPEL",
  serviceId: "VOL-1",
  siteCode: "PPEL",
  resource: {
    key: "POSTE-PPEL",
    name: "Poste PPEL",
  },
  beginDateTime: BEGIN_DATE_TIME_RENDEZ_VOUS,
  endDateTime: END_DATE_TIME_RENDEZ_VOUS,
};

export const recapitulatifVolSimpleAvecRendezVous = {
  ...evenementVolSimpleValide,
  dateSouhaitee: DATE_RENDEZ_VOUS,
  creneauPrefere: CRENEAU_PREFERE_RENDEZ_VOUS,
  selectedCreneau: creneauRendezVousValide,
};

export const brouillonVolSimpleDto = {
  informationsPersonnelles: {
    lienAvecPersonne: "MOI_MEME",
    nom: "Martin",
    prenom: "Anne",
    genre: ripolSelection("2", "Feminin"),
    nationalite: ripolSelection(PAYS_SUISSE, "Suisse"),
    dateNaissance: "1985-04-15",
    adresse: {
      adresse: ADRESSE_RUE,
      adressePostale: ADRESSE_NUMERO,
      npa: ADRESSE_NPA,
      localite: ADRESSE_LOCALITE,
      pays: PAYS_SUISSE,
    },
    telephone: "+41791234567",
    email: EMAIL_VERIFIE,
    typeDocumentIdentite: "carte_identite",
    numeroDocumentIdentite: "ID1234567",
  },
  incident: {
    typeIncident: "vol",
    details: {
      typeIncident: TYPE_INCIDENT_VOL,
      dateDebutEvent: DATE_DEBUT_EVENT_DTO,
      dateFinEvent: DATE_FIN_EVENT_DTO,
      adresseLesee: true,
      adresseConnue: false,
      adresseIncident: {
        adresse: ADRESSE_RUE,
        adressePostale: ADRESSE_NUMERO,
        npa: ADRESSE_NPA,
        localite: ADRESSE_LOCALITE,
        pays: PAYS_SUISSE,
      },
      volDansVehicule: false,
      categorieObjet: "informatique",
      objetsVoles: [{
        ...objetInformatiqueVoleValide,
        type: objetInformatiqueVoleValide.typeObjet,
      }],
      avezVousDegradation: false,
    },
  },
};
