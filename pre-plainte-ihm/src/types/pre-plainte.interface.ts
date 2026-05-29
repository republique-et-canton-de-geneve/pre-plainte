import type { CreneauRendezVous } from "@/types/rendez-vous-interface";
import type { RipolSelection } from "./ripol.interface";

export type VolObjetFormSnapshot = {
  categorieObjet: string;
  sousCategorie?: string;
  typeObjet: RipolSelection | null;
  fabricant: RipolSelection | null;
  fabricantAutre?: string;
  modele: RipolSelection | null;
  modeleAutre?: string;
  couleur: RipolSelection | null;
  couleurSecondaire?: RipolSelection | null;
  gravure?: string;
  valeurReelle?: string;
  numeroSerie: string;
  numeroSerieInconnu?: boolean;
  numeroIMEI?: string;
  numeroIMEIInconnu?: boolean;
  justificationAbsenceIMEI?: string;
  isVehicle?: boolean;
  numeroCadre?: string;
  numeroCadreInconnu?: boolean;
  vin?: string;
  vinInconnu?: boolean;
  velofinderId?: string;
  dateAchat?: string;
  plaqueNumero?: string;
  plaqueInconnu?: boolean;
  plaquePays?: RipolSelection | null;
  plaqueCanton?: RipolSelection | null;
  assuranceAucune?: boolean;
  assureur?: RipolSelection | null;
  assureurAutre?: string;
  numeroAssurance?: string;
  numeroVignette?: string;
  numeroMaster?: string;
};

export interface PrePlainteFormFields {
  /* Informations générales */
  confirmeIdentite: boolean;
  confirmeSituation: boolean;

  /* Informations personnelles */
  lienAvecPersonne: string;
  typeRepresentation?: string;
  postePersonneMorale?: string;
  justificatifPersonneMorale?: File[];

  /* Déclarant */
  nom: string;
  nomNaissance?: string;
  prenom: string;
  genre: RipolSelection | null;
  nationalite: RipolSelection | null;
  dateNaissance: string;
  adresse: string;
  adressePostale?: string;
  npa: string;
  localite: string;
  pays: string;
  titreSejour?: string;
  telephone: string;
  email: string;
  confirmationEmail: string;
  typeDocumentIdentite: string;
  numeroDocumentIdentite?: string;

  /* Tiers */
  tiersNom?: string;
  tiersPrenom?: string;
  tiersGenre?: RipolSelection | null;
  tiersNationalite?: RipolSelection | null;
  tiersDateNaissance?: string;
  tiersAdresse?: string;
  tiersAdressePostale?: string;
  tiersNpa?: string;
  tiersLocalite?: string;
  tiersPays?: string;
  tiersTelephone?: string;
  tiersEmail?: string;
  tiersConfirmationEmail?: string;
  tiersTypeDocumentIdentite?: string;
  tiersNumeroDocumentIdentite?: string;

  /* Organisation */
  organisationNom?: string;
  organisationAdresse?: string;
  organisationAdressePostale?: string;
  organisationNpa?: string;
  organisationLocalite?: string;
  organisationPays?: string;
  organisationTelephone?: string;
  organisationEmail?: string;
  organisationConfirmationEmail?: string;

  /* Incident */
  typeIncident: string;
  dateDebutEvenement: string;
  heureDebutEvenement: string;
  dateFinEvenement: string;
  heureFinEvenement: string;

  /* Commun vol & dommage */
  adresseLesee?: boolean | null;
  typeLieu?: RipolSelection | null;
  adresseConnue?: boolean | null;
  adresseEvenement?: string;
  adressePostaleEvenement?: string;
  npaEvenement?: string;
  localiteEvenement?: string;
  paysEvenement?: string;
  lieuOrigine?: RipolSelection | null;
  isTrajet?: boolean | null;
  adresseEvenementSecondaire?: string;
  adressePostaleEvenementSecondaire?: string;
  npaEvenementSecondaire?: string;
  localiteEvenementSecondaire?: string;
  paysEvenementSecondaire?: string;

  sousCategorie?: string;
  typeObjet: RipolSelection | null;
  fabricant: RipolSelection | null;
  fabricantAutre?: string;
  modele: RipolSelection | null;
  modeleAutre?: string;
  couleur: RipolSelection | null;
  couleurSecondaire?: RipolSelection | null;
  numeroCadre?: string;
  numeroCadreInconnu?: boolean;
  vin?: string;
  vinInconnu?: boolean;
  velofinderId?: string;
  dateAchat?: string;
  plaqueNumero?: string;
  plaqueInconnu?: boolean;
  plaquePays?: RipolSelection | null;
  plaqueCanton?: RipolSelection | null;
  assuranceAucune?: boolean;
  assureur?: RipolSelection | null;
  assureurAutre?: string;
  numeroAssurance?: string;
  numeroVignette?: string;
  numeroMaster?: string;

  fichiers: File[];

  /* Vol */
  volDansVehicule?: boolean | null;
  categorieObjet: string;
  gravure?: string;
  valeurReelle?: string;
  numeroSerie: string;
  numeroSerieInconnu?: boolean;
  numeroIMEI?: string;
  numeroIMEIInconnu?: boolean;
  justificationAbsenceIMEI?: string;
  isVehicle?: boolean;
  avezVousDegradation?: boolean | null;
  objetsVolesValides?: VolObjetFormSnapshot[];

  /* Dommage */
  typeDommage: string;
  montantEstime?: string;
  devise?: string;
  naturesDommage?: string[];
  description: string;
  constatPresent?: boolean | null;
  dateConstat: string;
  objetsDegradesValides?: VolObjetFormSnapshot[];

  /* Cybercrime commun */
  typeCybercrime?: string;
  autresDocuments?: File[];

  /* Commun commande frauduleuse & fausse annonce */
  descriptionCybercrime?: string;
  justificatifsPaiement?: File[];
  copiesEcran?: File[];

  /* Commun achat non reçu & fausse annonce */
  datePremierContact?: string;
  heurePremierContact?: string;
  dateDernierContact?: string;
  heureDernierContact?: string;

  /* Commande frauduleuse */
  prestataire?: string;
  dateDecouverte?: string;
  montant?: string;
  assurance?: boolean | null;
  emailCommandeInconnu?: boolean;
  emailCommande?: string;
  telephoneCommandeInconnu?: boolean;
  telephoneCommande?: string;
  livraisonAdresseLesee?: boolean | null;
  livraisonAdresse?: string;
  livraisonAdressePostale?: string;
  livraisonNpa?: string;
  livraisonLocalite?: string;
  livraisonLocaliteCode?: string;
  livraisonPays?: string;

  /* Achat non reçu */
  montantDelitAchatLigne?: string;
  articleNonLivreDescription?: string;
  prenomVendeur?: string;
  nomVendeur?: string;
  telephoneVendeurInconnu?: boolean;
  telephoneVendeur?: string;
  emailVendeurInconnu?: boolean;
  emailVendeur?: string;
  adresseVendeurInconnue?: boolean;
  vendeurAdresse?: string;
  vendeurAdressePostale?: string;
  vendeurNpa?: string;
  vendeurLocalite?: string;
  vendeurLocaliteCode?: string;
  vendeurPays?: string;
  achatViaPlaceMarche?: boolean | null;
  plateforme?: string;
  plateformeAutre?: string;
  plateformeId?: string;
  nomEntrepriseVendeur?: string;
  siteWebEntrepriseVendeur?: string;
  annonceDocument?: File[];
  annonceDocumentIndisponible?: boolean;
  raisonAbsenceAnnonce?: string;
  moyenPaiement?: string;
  moyenPaiementAutre?: string;
  ibanBeneficiaire?: string;
  comptePaypalBeneficiaire?: string;
  numeroTwintBeneficiaire?: string;
  adresseWalletCrypto?: string;
  hashTransactionCrypto?: string;
  societeBeneficiaire?: string;
  nomBeneficiaire?: string;
  prenomBeneficiaire?: string;
  dateOperation?: string;
  preuvePaiementDocument?: File[];
  preuvePaiementIndisponible?: boolean;
  raisonAbsencePreuvePaiement?: string;
  copieIdentiteTransmiseAuteur?: boolean | null;
  copieIdentiteTransmiseAuteurDocument?: File[];
  copieIdentiteAuteurTransmise?: boolean | null;
  copieIdentiteAuteurDocument?: File[];

  /* Fausse annonce */
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

  /* Rendez-vous */
  preferenceRendezVous: string;
  dateSouhaitee: string;
  creneauPrefere: string;
  dateAlternative: string;
  commentairesRendezVous: string;
  modeContactPrefere: string;
  codeRdv?: string;
  /** Créneau de rendez-vous sélectionné (lieu, horaires). */
  selectedCreneau?: CreneauRendezVous | null;
}
