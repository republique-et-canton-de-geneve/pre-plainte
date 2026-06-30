import type { BusinessRule } from "./business-rule.types";

export interface InformationsPersonnellesRuleData extends Record<string, unknown> {
  lienAvecPersonne: string;
  typeRepresentation?: string;
  postePersonneMorale?: string;
  justificatifPersonneMorale?: File[];
  nom: string;
  nomNaissance?: string;
  prenom: string;
  adresse: string;
  pays: string;
  genre: { code: string; label: string } | null;
  nationalite: { code: string; label: string } | null;
  titreSejour?: string;
  adressePostale: string;
  npa: string;
  localite: string;
  dateNaissance: string;
  telephone: string;
  typeDocumentIdentite: string;
  numeroDocumentIdentite?: string;
  tiersTypeDocumentIdentite?: string;
  tiersNumeroDocumentIdentite?: string;
  tiersNom?: string;
  tiersPrenom?: string;
  tiersGenre?: { code: string; label: string } | null;
  tiersNationalite?: { code: string; label: string } | null;
  tiersDateNaissance?: string;
  tiersAdresse?: string;
  tiersAdressePostale?: string;
  tiersNpa?: string;
  tiersLocalite?: string;
  tiersPays?: string;
  tiersTelephone?: string;
  tiersEmail?: string;
  tiersConfirmationEmail?: string;
  organisationNom?: string;
  organisationAdresse?: string;
  organisationAdressePostale?: string;
  organisationNpa?: string;
  organisationLocalite?: string;
  organisationPays?: string;
  organisationTelephone?: string;
  organisationEmail?: string;
  organisationConfirmationEmail?: string;
}

const suisse = { code: "CH", label: "Suisse" };
const france = { code: "FR", label: "France" };
const masculin = { code: "1", label: "Masculin" };
const feminin = { code: "2", label: "Feminin" };

function datePourAge(age: number): string {
  const today = new Date();
  const date = new Date(today.getFullYear() - age, today.getMonth(), today.getDate());
  const day = String(date.getDate()).padStart(2, "0");
  const month = String(date.getMonth() + 1).padStart(2, "0");

  return `${day}.${month}.${date.getFullYear()}`;
}

export const donneesInformationsPersonnellesValides: InformationsPersonnellesRuleData = {
  lienAvecPersonne: "MOI_MEME",
  nom: "Dupont",
  prenom: "Jean",
  adresse: "Rue du Rhone 10",
  pays: "CH",
  genre: masculin,
  nationalite: suisse,
  adressePostale: "10",
  npa: "1204",
  localite: "Geneve",
  dateNaissance: "01.01.1990",
  telephone: "+41789054434",
  typeDocumentIdentite: "carte_identite",
  numeroDocumentIdentite: "ID123456",
};

export const reglesInformationsPersonnelles: BusinessRule<InformationsPersonnellesRuleData>[] = [
  {
    section: "Coordonnees et identite du declarant",
    champDemande: "Date de naissance",
    obligatoire: "Oui",
    precision: "Le declarant doit avoir entre 16 et 120 ans.",
    examples: [
      {
        label: "15 ans est refuse",
        data: {
          dateNaissance: datePourAge(15),
        },
        valid: false,
        errorPath: ["dateNaissance"],
        errorMessage: "validation.ageInvalide",
      },
      {
        label: "16 ans est accepte",
        data: {
          dateNaissance: datePourAge(16),
        },
        valid: true,
      },
      {
        label: "119 ans est accepte",
        data: {
          dateNaissance: datePourAge(119),
        },
        valid: true,
      },
      {
        label: "120 ans est accepte",
        data: {
          dateNaissance: datePourAge(120),
        },
        valid: true,
      },
      {
        label: "121 ans est refuse",
        data: {
          dateNaissance: datePourAge(121),
        },
        valid: false,
        errorPath: ["dateNaissance"],
        errorMessage: "validation.ageInvalide",
      },
    ],
  },
  {
    section: "Coordonnees et identite du declarant",
    champDemande: "Titre de sejour",
    obligatoire: "Selon le cas",
    precision: "Obligatoire si la nationalite indiquee n'est pas suisse.",
    examples: [
      {
        label: "nationalite suisse sans titre de sejour est acceptee",
        data: {
          nationalite: suisse,
          titreSejour: "",
        },
        valid: true,
      },
      {
        label: "nationalite non suisse sans titre de sejour est refusee",
        data: {
          nationalite: france,
          titreSejour: "",
        },
        valid: false,
        errorPath: ["titreSejour"],
        errorMessage: "validation.titreSejourRequis",
      },
      {
        label: "nationalite non suisse avec titre de sejour est acceptee",
        data: {
          nationalite: france,
          titreSejour: "permis_b",
        },
        valid: true,
      },
    ],
  },
  {
    section: "Document d'identite du declarant",
    champDemande: "Numero du document d'identite",
    obligatoire: "Selon le cas",
    precision: "Obligatoire pour une carte d'identite ou un passeport. Non demande si les documents sont voles ou perdus.",
    examples: [
      {
        label: "carte d'identite sans numero est refusee",
        data: {
          typeDocumentIdentite: "carte_identite",
          numeroDocumentIdentite: "",
        },
        valid: false,
        errorPath: ["numeroDocumentIdentite"],
        errorMessage: "validation.numeroDocumentRequis",
      },
      {
        label: "passeport sans numero est refuse",
        data: {
          typeDocumentIdentite: "passeport",
          numeroDocumentIdentite: "",
        },
        valid: false,
        errorPath: ["numeroDocumentIdentite"],
        errorMessage: "validation.numeroDocumentRequis",
      },
      {
        label: "carte d'identite avec numero est acceptee",
        data: {
          typeDocumentIdentite: "carte_identite",
          numeroDocumentIdentite: "ID123456",
        },
        valid: true,
      },
      {
        label: "documents voles ou perdus sans numero est accepte",
        data: {
          typeDocumentIdentite: "documents_voles_perdus",
          numeroDocumentIdentite: "",
        },
        valid: true,
      },
    ],
  },
  {
    section: "Coordonnees et identite du declarant",
    champDemande: "Numero de telephone",
    obligatoire: "Oui",
    precision: "Le numero de telephone doit respecter un format international valide.",
    examples: [
      {
        label: "numero vide est refuse",
        data: {
          telephone: "",
        },
        valid: false,
        errorPath: ["telephone"],
        errorMessage: "validation.telephoneFormat",
      },
      {
        label: "numero avec lettres est refuse",
        data: {
          telephone: "abc",
        },
        valid: false,
        errorPath: ["telephone"],
        errorMessage: "validation.telephoneFormat",
      },
      {
        label: "numero suisse au format international est accepte",
        data: {
          telephone: "+41789054434",
        },
        valid: true,
      },
      {
        label: "numero international avec espaces est accepte",
        data: {
          telephone: "+41 78 905 44 34",
        },
        valid: true,
      },
    ],
  },
  {
    section: "Informations sur le tiers concerne",
    champDemande: "Type de representation",
    obligatoire: "Selon le cas",
    precision: "Obligatoire si le citoyen declare pour un tiers.",
    examples: [
      {
        label: "declaration pour soi-meme sans type de representation est acceptee",
        data: {
          lienAvecPersonne: "MOI_MEME",
          typeRepresentation: "",
        },
        valid: true,
      },
      {
        label: "declaration pour un tiers sans type de representation est refusee",
        data: {
          lienAvecPersonne: "TIERS",
          typeRepresentation: "",
          tiersTypeDocumentIdentite: "carte_identite",
          tiersNumeroDocumentIdentite: "T123456",
          tiersNom: "Martin",
          tiersPrenom: "Lea",
          tiersGenre: feminin,
          tiersNationalite: suisse,
          tiersDateNaissance: "01.01.1995",
          tiersAdresse: "Rue du Lac 5",
          tiersAdressePostale: "5",
          tiersNpa: "1201",
          tiersLocalite: "Geneve",
          tiersPays: "CH",
          tiersTelephone: "+41789054435",
          tiersEmail: "tiers@example.com",
          tiersConfirmationEmail: "tiers@example.com",
        },
        valid: false,
        errorPath: ["typeRepresentation"],
        errorMessage: "validation.typeRepresentationRequis",
      },
      {
        label: "declaration pour un tiers avec type de representation est acceptee",
        data: {
          lienAvecPersonne: "TIERS",
          typeRepresentation: "legal",
          tiersTypeDocumentIdentite: "carte_identite",
          tiersNumeroDocumentIdentite: "T123456",
          tiersNom: "Martin",
          tiersPrenom: "Lea",
          tiersGenre: feminin,
          tiersNationalite: suisse,
          tiersDateNaissance: "01.01.1995",
          tiersAdresse: "Rue du Lac 5",
          tiersAdressePostale: "5",
          tiersNpa: "1201",
          tiersLocalite: "Geneve",
          tiersPays: "CH",
          tiersTelephone: "+41789054435",
          tiersEmail: "tiers@example.com",
          tiersConfirmationEmail: "tiers@example.com",
        },
        valid: true,
      },
    ],
  },
  {
    section: "Informations sur l'entreprise concernee",
    champDemande: "Fonction dans l'entreprise",
    obligatoire: "Selon le cas",
    precision: "Obligatoire si le citoyen declare pour une entreprise.",
    examples: [
      {
        label: "declaration pour soi-meme sans fonction dans l'entreprise est acceptee",
        data: {
          lienAvecPersonne: "MOI_MEME",
          postePersonneMorale: "",
        },
        valid: true,
      },
      {
        label: "declaration pour une entreprise sans fonction est refusee",
        data: {
          lienAvecPersonne: "ENTREPRISE",
          postePersonneMorale: "",
          organisationNom: "Entreprise SA",
          organisationAdresse: "Rue de Lausanne 12",
          organisationAdressePostale: "12",
          organisationNpa: "1202",
          organisationLocalite: "Geneve",
          organisationPays: "CH",
          organisationTelephone: "+41789054436",
          organisationEmail: "entreprise@example.com",
          organisationConfirmationEmail: "entreprise@example.com",
        },
        valid: false,
        errorPath: ["postePersonneMorale"],
        errorMessage: "validation.postePersonneMoraleRequis",
      },
      {
        label: "declaration pour une entreprise avec fonction est acceptee",
        data: {
          lienAvecPersonne: "ENTREPRISE",
          postePersonneMorale: "directrice_directeur",
          organisationNom: "Entreprise SA",
          organisationAdresse: "Rue de Lausanne 12",
          organisationAdressePostale: "12",
          organisationNpa: "1202",
          organisationLocalite: "Geneve",
          organisationPays: "CH",
          organisationTelephone: "+41789054436",
          organisationEmail: "entreprise@example.com",
          organisationConfirmationEmail: "entreprise@example.com",
        },
        valid: true,
      },
    ],
  },
];
