import type { CreneauRendezVous } from "@/types/rendez-vous-interface.ts";
import type { RipolSelection } from "@/types/ripol.interface";

export type FichierDTO = {
  nom: string;
  typeMime: string;
  contenuBase64: string;
};

export type RipolCodeDTO = RipolSelection | string | null;

export type AdresseDTO = {
  adresse: string;
  adressePostale: string;
  npa: string;
  localite: string;
  localiteCode?: string;
  pays?: string;
  paysCode?: string;
};

export type InfosPersonneDTO = {
  nom: string;
  nomNaissance?: string;
  prenom: string;
  genre: RipolCodeDTO;
  nationalite: RipolCodeDTO;
  lieuOrigine?: RipolCodeDTO;
  titreSejour?: string;
  dateNaissance: string;
  adresse: AdresseDTO;
  telephone: string;
  email: string;
  typeDocumentIdentite?: string;
  numeroDocumentIdentite?: string;
};

export type TiersDTO = InfosPersonneDTO;

export type InfosOrganisationDTO = {
  nom: string;
  telephone: string;
  email: string;
  adresse: AdresseDTO;
};

export type InformationsPersonnellesDTO = InfosPersonneDTO & {
  lienAvecPersonne: string;
  typeRepresentation?: string;
  postePersonneMorale?: string;
  justificatifPersonneMorale?: FichierDTO;

  tiers?: TiersDTO | null;
  organisation?: InfosOrganisationDTO | null;
};

export enum TypeIncident {
  VOL = "vol",
  DOMMAGE = "dommageMateriel",
  CYBERCRIME = "cybercrime",
}

export type IncidentBaseDTO = {
  typeIncident: TypeIncident;
  dateDebutEvent: string;
  dateFinEvent: string;
  adresseIncident: AdresseDTO;
  typeLieu?: RipolCodeDTO;
  lieuOrigine?: string;
  adresseConnue?: boolean;
  adresseLesee?: boolean;
  isTrajet?: boolean;
};

export type ObjetIncidentDTO = {
  categorieObjet?: string;
  sousCategorie?: string;
  type?: RipolCodeDTO;
  fabricant?: RipolCodeDTO;
  fabricantAutre?: string;
  modele?: RipolCodeDTO;
  modeleAutre?: string;
  couleur?: RipolCodeDTO;
  couleurSecondaire?: RipolCodeDTO;
  numeroSerie?: string;
  numeroSerieInconnu?: boolean;
  numeroCadre?: string;
  numeroCadreInconnu?: boolean;
  numeroIMEI?: string;
  numeroIMEIInconnu?: boolean;
  justificationAbsenceIMEI?: string;
  gravure?: string;
  realValue?: string;
  isVehicle?: boolean;
  purchaseDate?: string;
  vin?: string;
  vinInconnu?: boolean;
  velofinderId?: string;
  plaqueNumero?: string;
  plaqueInconnu?: boolean;
  plaquePays?: RipolCodeDTO;
  plaqueCanton?: RipolCodeDTO;
};

export type IncidentVolDTO = IncidentBaseDTO & {
  typeIncident: TypeIncident.VOL;
  volDansVehicule?: boolean;
  categorieObjet?: string;
  objetsVoles?: ObjetIncidentDTO[];
  avezVousDegradation?: boolean;
  fichiers?: FichierDTO[];
};

export type IncidentDommageDTO = IncidentBaseDTO & {
  typeIncident: TypeIncident.DOMMAGE;
  montantEstime?: number;
  devise?: string;
  typeDommage?: string;
  naturesDommage?: string[];
  description?: string;
  constatPresent?: boolean;
  dateConstat?: string;
  fichiers?: FichierDTO[];
  objetDegrades?: ObjetIncidentDTO[];
};

export type IncidentCyberDTO = IncidentBaseDTO & {
  typeIncident: TypeIncident.CYBERCRIME;
  typeCybercrime?: string;
  descriptionCybercrime?: string;
  datePremierContact?: string;
  dateDernierContact?: string;
  commandeFrauduleuse?: CommandeFrauduleuse;
  achatNonRecu?: AchatNonRecu;
  fausseAnnonce?: FausseAnnonce;
  fichiersCybercrime?: FichierDTO[];
};

export interface CommandeFrauduleuse {
  prestataire?: string;
  dateDecouverte?: string;
  montant?: number;
  assurance?: boolean;
  emailCommandeInconnu?: boolean;
  emailCommande?: string;
  telephoneCommandeInconnu?: boolean;
  telephoneCommande?: string;
  livraisonAdresseLesee?: boolean;
  adresseLivraison?: AdresseDTO;
}

export interface AchatNonRecu {
  montantDelitAchatLigne?: string;
  articleNonLivreDescription?: string;
  prenomVendeur?: string;
  nomVendeur?: string;
  telephoneVendeurInconnu?: boolean,
  telephoneVendeur?: string;
  emailVendeurInconnu?: boolean;
  emailVendeur?: string;
  adresseVendeurInconnue?: boolean;
  adresseVendeur?: AdresseDTO;
  achatViaPlaceMarche?: boolean;
  plateformeUtilisee?: string;
  plateformeAutre?: string;
  plateformeId?: string;
  nomEntrepriseVendeur?: string;
  siteWebEntrepriseVendeur?: string;
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
  preuvePaiementIndisponible?: boolean;
  raisonAbsencePreuvePaiement?: string;
  copieIdentiteTransmiseAuteur?: boolean;
  copieIdentiteAuteurTransmise?: boolean;
}

export interface FausseAnnonce {
  urlComplete?: string;
  titreAnnonce?: string;
  nomBailleur?: string;
  emailBailleurInconnu?: boolean;
  emailBailleur?: string;
  telephoneBailleurInconnu?: boolean;
  telephoneBailleur?: string;
  adresseBienImmobilier?: string;
  montantDemande?: number;
  modePaiementDemande?: string;
}

export type IncidentDetailsDTO = IncidentVolDTO | IncidentDommageDTO | IncidentCyberDTO;

export type IncidentWrapperDTO = {
  typeIncident: IncidentDetailsDTO["typeIncident"];
  details: IncidentDetailsDTO;
};

export type PrePlainteDTO =
  | { informationsPersonnelles: InformationsPersonnellesDTO }
  | { informationsPersonnelles: InformationsPersonnellesDTO; incident: IncidentWrapperDTO }
  | {
      demandeId: string | null;
      informationsPersonnelles: InformationsPersonnellesDTO;
      incident: IncidentWrapperDTO;
      creneauRendezVous: CreneauRendezVous | null;
    };
