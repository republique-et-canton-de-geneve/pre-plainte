import type { Ripol } from "@/types/ripol.interface";

export const DEMARCHE_PLAINTE_URL = "https://www.ge.ch/vol-cambriolage-dommage-propriete/deposer-plainte";

export const EMPTY_VALUE_DISPLAY = "-";
export const EMPTY_VALUE_EM_DASH = "—";

export const STEPS = [
  { titleKey: "steps.informationsGenerales" },
  { titleKey: "steps.verificationEmail" },
  { titleKey: "steps.informationsPersonnelles" },
  { titleKey: "steps.informationsEvenement" },
  { titleKey: "steps.prendreRendezVous" },
  { titleKey: "steps.recapitulatif" },
  { titleKey: "steps.validation" },
] as const;

export const TOTAL_STEPS = 7;

export const LIENS_AVEC_PERSONNE = [
  {
    value: "MOI_MEME",
    labelKey: "informationsPersonnelles.moiMeme",
  },
  {
    value: "TIERS",
    labelKey: "informationsPersonnelles.tiers",
  },
  {
    value: "ENTREPRISE",
    labelKey: "informationsPersonnelles.monEntreprise",
  },
] as const;

export const GENRE_LABEL_KEYS: Record<string, string> = {
  "0": "informationsPersonnelles.nonRenseigne",
  "1": "informationsPersonnelles.masculin",
  "2": "informationsPersonnelles.feminin",
};

export const GENRES_ORDER = ["1", "2", "0"];

export const TYPES_DOCUMENT_IDENTITE = [
  { value: "carte_identite", labelKey: "informationsPersonnelles.carteIdentite" },
  { value: "passeport", labelKey: "informationsPersonnelles.passeport" },
] as const;

export const TITRES_SEJOUR = [
  { value: "permis_b", labelKey: "titreSejour.permisB" },
  { value: "permis_b_refugie", labelKey: "titreSejour.permisBRefugie" },
  { value: "permis_c", labelKey: "titreSejour.permisC" },
  { value: "permis_ci", labelKey: "titreSejour.permisCi" },
  { value: "carte_legitimation", labelKey: "titreSejour.permisLegitimation" },
  { value: "permis_l", labelKey: "titreSejour.permisL" },
  { value: "permis_f_refugie", labelKey: "titreSejour.permisFRefugie" },
  { value: "permis_f_provisoire", labelKey: "titreSejour.permisFProvisoire" },
  { value: "permis_g", labelKey: "titreSejour.permisG" },
  { value: "permis_n", labelKey: "titreSejour.permisN" },
  { value: "permis_s", labelKey: "titreSejour.permisS" },
  { value: "sans_permis", labelKey: "titreSejour.sansPermis" },
  { value: "aucun", labelKey: "titreSejour.aucun" },
] as const;

export const TYPES_PERSONNES_MORALES = [
  { value: "administratrice_administrateur", labelKey: "typePersonneMorale.administratriceAdministrateur" },
  { value: "associee_associe", labelKey: "typePersonneMorale.associeeAssocie" },
  { value: "avocate_avocat", labelKey: "typePersonneMorale.avocateAvocat" },
  { value: "chargee_affaires_charge_affaires", labelKey: "typePersonneMorale.chargeeAffairesChargeAffaires" },
  {
    value: "chargee_communication_charge_communication",
    labelKey: "typePersonneMorale.chargeeCommunicationChargeCommunication",
  },
  { value: "chef_projet", labelKey: "typePersonneMorale.chefProjet" },
  { value: "collaboratrice_collaborateur", labelKey: "typePersonneMorale.collaboratriceCollaborateur" },
  { value: "commercante_commercant", labelKey: "typePersonneMorale.commercanteCommercant" },
  { value: "consultante_consultant", labelKey: "typePersonneMorale.consultanteConsultant" },
  { value: "directrice_directeur", labelKey: "typePersonneMorale.directriceDirecteur" },
  { value: "employee_employe", labelKey: "typePersonneMorale.employeeEmploye" },
  { value: "experte_comptable_expert_comptable", labelKey: "typePersonneMorale.experteComptableExpertComptable" },
  { value: "fiduciaire", labelKey: "typePersonneMorale.fiduciaire" },
  { value: "fondatrice_fondateur", labelKey: "typePersonneMorale.fondatriceFondateur" },
  { value: "gerante_gerant", labelKey: "typePersonneMorale.geranteGerant" },
  { value: "mandataire", labelKey: "typePersonneMorale.mandataire" },
  { value: "membre_comite", labelKey: "typePersonneMorale.membreComite" },
  { value: "membre_direction", labelKey: "typePersonneMorale.membreDirection" },
  { value: "notaire", labelKey: "typePersonneMorale.notaire" },
  { value: "presidente_president", labelKey: "typePersonneMorale.presidentePresident" },
  { value: "proprietaire", labelKey: "typePersonneMorale.proprietaire" },
  {
    value: "representante_legale_representant_legal",
    labelKey: "typePersonneMorale.representanteLegaleRepresentantLegal",
  },
  {
    value: "responsable_administratif_administrative",
    labelKey: "typePersonneMorale.responsableAdministratifAdministrative",
  },
  { value: "responsable_service", labelKey: "typePersonneMorale.responsableService" },
  { value: "responsable_juridique", labelKey: "typePersonneMorale.responsableJuridique" },
  { value: "responsable_rh", labelKey: "typePersonneMorale.responsableRh" },
  { value: "responsable_technique", labelKey: "typePersonneMorale.responsableTechnique" },
  { value: "secretaire", labelKey: "typePersonneMorale.secretaire" },
  { value: "stagiaire", labelKey: "typePersonneMorale.stagiaire" },
  { value: "tresoriere_tresorier", labelKey: "typePersonneMorale.tresoriereTresorier" },
  { value: "vice_presidente_vice_president", labelKey: "typePersonneMorale.vicePresidenteVicePresident" },
] as const;

export const REPRESENTATION_OPTIONS = [
  { value: "legal", labelKey: "representation.legal" },
  { value: "famille", labelKey: "representation.famille" },
  { value: "ami", labelKey: "representation.ami" },
  { value: "curateur", labelKey: "representation.curateur" },
  { value: "autre", labelKey: "representation.autre" },
] as const;

export const VOL_OBJET_CATEGORIE = {
  VEHICULE: "vehicule",
  PLAQUE: "plaque",
  TELEPHONE: "telephone",
  TELEPHONE_MOBILE: "telephone_mobile",
  BIJOUX: "bijoux",
} as const;

/**
 * Catégories d'objets pour le pré-filtrage des types d'objets RIPOL.
 * Chaque catégorie définit les préfixes de codes RIPOL associés.
 * - prefixes: codes commençant par ces valeurs (groupType 183)
 * - useVehicleTypes: si true, utilise le groupType 101 (véhicules)
 */
export const CATEGORIES_OBJETS = [
  {
    value: "telephone",
    labelKey: "categoriesObjets.telephone",
    prefixes: ["71"],
    useVehicleTypes: false,
    subCategories: [
      { value: "telephone_mobile", labelKey: "sousCategories.smartphones", prefixes: ["7131"] },
      { value: "telephone_fixe", labelKey: "sousCategories.telephoneFixe", prefixes: ["7130"] },
      { value: "telephone_accessoires", labelKey: "sousCategories.telephoneAccessoires", prefixes: ["714"] },
    ],
  },
  {
    value: "informatique",
    labelKey: "categoriesObjets.informatique",
    prefixes: ["72"],
    useVehicleTypes: false,
    subCategories: [
      { value: "ordinateur_portable", labelKey: "sousCategories.ordinateurPortable", prefixes: ["722"] },
      { value: "ordinateur_fixe", labelKey: "sousCategories.ordinateurFixe", prefixes: ["721"] },
      { value: "accessoires_info", labelKey: "sousCategories.accessoiresInfo", prefixes: ["724", "729"] },
    ],
  },
  {
    value: "bijoux",
    labelKey: "categoriesObjets.bijoux",
    prefixes: ["31", "32", "33", "34", "35", "36", "37", "38", "39"],
    useVehicleTypes: false,
    subCategories: [
      { value: "montres_bracelets", labelKey: "sousCategories.montresBracelets", prefixes: ["313"] },
      { value: "montres", labelKey: "sousCategories.montres", prefixes: ["32", "33"] },
      { value: "accessoires_montres", labelKey: "sousCategories.accessoiresMontres", prefixes: ["34"] },
      { value: "bagues", labelKey: "sousCategories.bagues", prefixes: ["35"] },
      { value: "boucles_oreilles", labelKey: "sousCategories.bouclesOreilles", prefixes: ["36"] },
      { value: "bracelets", labelKey: "sousCategories.bracelets", prefixes: ["37"] },
      { value: "colliers", labelKey: "sousCategories.colliers", prefixes: ["38"] },
    ],
  },
  {
    value: "vehicule",
    labelKey: "categoriesObjets.vehicule",
    prefixes: [],
    useVehicleTypes: true,
    subCategories: [
      { value: "velos", labelKey: "sousCategories.velos", prefixes: ["200", "201"] },
      { value: "motos", labelKey: "sousCategories.motos", prefixes: ["060", "061", "062", "064", "070"] },
      { value: "voitures", labelKey: "sousCategories.voitures", prefixes: ["010", "011", "020"] },
      { value: "camions", labelKey: "sousCategories.camions", prefixes: ["030", "031", "032"] },
      { value: "bateaux", labelKey: "sousCategories.bateaux", prefixes: ["300", "308"] },
      { value: "avions", labelKey: "sousCategories.avions", prefixes: ["400", "401", "403"] },
    ],
  },
  {
    value: "plaque",
    labelKey: "categoriesObjets.plaque",
    prefixes: [],
    useVehicleTypes: false,
    subCategories: [],
  },
  {
    value: "vetements",
    labelKey: "categoriesObjets.vetements",
    prefixes: ["01", "02", "03", "04", "05", "06", "07", "08", "09"],
    useVehicleTypes: false,
    subCategories: [
      { value: "vetements_dame", labelKey: "sousCategories.vetementsDame", prefixes: ["03"] },
      { value: "vetements_homme", labelKey: "sousCategories.vetementsHomme", prefixes: ["04"] },
      { value: "vetements_enfant", labelKey: "sousCategories.vetementsEnfant", prefixes: ["05"] },
      { value: "vetements_sport", labelKey: "sousCategories.vetementsSport", prefixes: ["06"] },
      { value: "chaussures", labelKey: "sousCategories.chaussures", prefixes: ["09"] },
      { value: "accessoires_mode", labelKey: "sousCategories.accessoiresMode", prefixes: ["01"] },
    ],
  },
  {
    value: "bagages",
    labelKey: "categoriesObjets.bagages",
    prefixes: ["11", "12", "13", "14", "15", "16", "17", "18"],
    useVehicleTypes: false,
    subCategories: [
      { value: "valises", labelKey: "sousCategories.valises", prefixes: ["12"] },
      { value: "sacs_dos", labelKey: "sousCategories.sacsDos", prefixes: ["131"] },
      { value: "sacs_main", labelKey: "sousCategories.sacsMain", prefixes: ["15"] },
      { value: "cartables", labelKey: "sousCategories.cartables", prefixes: ["14"] },
      { value: "portefeuilles", labelKey: "sousCategories.portefeuilles", prefixes: ["17", "163"] },
      { value: "etuis", labelKey: "sousCategories.etuis", prefixes: ["16"] },
      { value: "parapluies", labelKey: "sousCategories.parapluies", prefixes: ["18"] },
    ],
  },
  {
    value: "documents",
    labelKey: "categoriesObjets.documents",
    prefixes: ["20", "21", "22", "23", "25", "26", "27", "28"],
    useVehicleTypes: false,
    subCategories: [
      { value: "papiers_identite", labelKey: "sousCategories.papiersIdentite", prefixes: ["2001", "2002", "2003"] },
      { value: "cartes_bancaires", labelKey: "sousCategories.cartesBancaires", prefixes: ["2651", "2652"] },
      { value: "permis", labelKey: "sousCategories.permis", prefixes: ["2005", "2006"] },
      { value: "argent", labelKey: "sousCategories.argent", prefixes: ["231", "232", "233", "234"] },
    ],
  },
  {
    value: "photo_video",
    labelKey: "categoriesObjets.photoVideo",
    prefixes: ["40", "41", "42", "44", "45", "46"],
    useVehicleTypes: false,
    subCategories: [
      { value: "appareils_photo", labelKey: "sousCategories.appareilsPhoto", prefixes: ["40", "41"] },
      { value: "cameras", labelKey: "sousCategories.cameras", prefixes: ["42", "44"] },
      { value: "audio", labelKey: "sousCategories.audio", prefixes: ["45", "46"] },
    ],
  },
  {
    value: "sport_loisirs",
    labelKey: "categoriesObjets.sportLoisirs",
    prefixes: ["67", "68"],
    useVehicleTypes: false,
    subCategories: [
      { value: "camping", labelKey: "sousCategories.camping", prefixes: ["671"] },
      { value: "peche_chasse", labelKey: "sousCategories.pecheChasse", prefixes: ["673", "674"] },
      { value: "jeux_consoles", labelKey: "sousCategories.jeuxConsoles", prefixes: ["675"] },
      { value: "fitness", labelKey: "sousCategories.fitness", prefixes: ["681"] },
      { value: "sports_hiver", labelKey: "sousCategories.sportsHiver", prefixes: ["682"] },
      { value: "sports_nautiques", labelKey: "sousCategories.sportsNautiques", prefixes: ["683"] },
      { value: "escalade", labelKey: "sousCategories.escalade", prefixes: ["684"] },
    ],
  },
  {
    value: "maison",
    labelKey: "categoriesObjets.maison",
    prefixes: ["51", "52", "53", "54", "55", "57", "58", "59"],
    useVehicleTypes: false,
    subCategories: [
      { value: "meubles", labelKey: "sousCategories.meubles", prefixes: ["51"] },
      { value: "electromenager", labelKey: "sousCategories.electromenager", prefixes: ["54"] },
      { value: "chauffage_clim", labelKey: "sousCategories.chauffageClim", prefixes: ["55"] },
      { value: "soins_corporels", labelKey: "sousCategories.soinsCorporels", prefixes: ["57"] },
      { value: "articles_menage", labelKey: "sousCategories.articlesMenage", prefixes: ["53"] },
    ],
  },
  {
    value: "outils",
    labelKey: "categoriesObjets.outils",
    prefixes: ["81", "82", "83", "84"],
    useVehicleTypes: false,
    subCategories: [
      { value: "machines_outils", labelKey: "sousCategories.machinesOutils", prefixes: ["81"] },
      { value: "outillage", labelKey: "sousCategories.outillage", prefixes: ["82"] },
      { value: "appareils_mesure", labelKey: "sousCategories.appareilsMesure", prefixes: ["83"] },
      { value: "serrurerie", labelKey: "sousCategories.serrurerie", prefixes: ["84"] },
    ],
  },
  {
    value: "tous",
    labelKey: "categoriesObjets.tous",
    prefixes: [],
    useVehicleTypes: false,
    subCategories: [],
  },
] as const;

export const AUTRE_OPTION: Ripol = {
  code: "AUTRE",
  labelFr: "Autre (préciser)",
  labelDe: "Andere (angeben)",
  groupeType: "",
};

export const VEHICLE_INSURERS_FALLBACK: Ripol[] = [
  { code: "AXA", labelFr: "AXA", labelDe: "AXA", groupeType: "185" },
  { code: "ALLIANCE", labelFr: "Alliance", labelDe: "Alliance", groupeType: "185" },
  { code: "ALLIANZ", labelFr: "Allianz", labelDe: "Allianz", groupeType: "185" },
  { code: "MOBILIAR", labelFr: "Mobiliar", labelDe: "Mobiliar", groupeType: "185" },
  { code: "GENERALI", labelFr: "Generali", labelDe: "Generali", groupeType: "185" },
  { code: "ZURICH", labelFr: "Zurich", labelDe: "Zurich", groupeType: "185" },
  { code: "HELVETIA", labelFr: "Helvetia", labelDe: "Helvetia", groupeType: "185" },
  { code: "BASLER", labelFr: "Basler", labelDe: "Basler", groupeType: "185" },
  { code: "VAUDOISE", labelFr: "Vaudoise", labelDe: "Vaudoise", groupeType: "185" },
  { code: "BALOISE", labelFr: "Baloise", labelDe: "Baloise", groupeType: "185" },
];

export const MOYEN_PAIEMENT = {
  IBAN: "iban",
  PAYPAL: "paypal",
  TWINT: "twint",
  CRYPTO: "crypto",
  AUTRE: "autre",
} as const;

/** Non proposés dans le select « type de cybercrime » (parcours IHM à venir). */
export const CYBERCRIME_TYPES_WITHOUT_DETAIL_FIELDS = ["cyberharcelement", "rancongiciel", "autre"] as const;

export function isCybercrimeTypeWithoutDetailFields(code: string | undefined | null | unknown): boolean {
  const c = code == null || code === "" ? "" : String(code).trim().toLowerCase();
  if (!c) {
    return false;
  }
  return (CYBERCRIME_TYPES_WITHOUT_DETAIL_FIELDS as readonly string[]).includes(c);
}

export const VEHICULE_CATEGORIES_AVEC_VIN = ["motos", "voitures", "camions", "scooters"];
export const VEHICULE_CATEGORIES_AVEC_PLAQUE = ["motos", "voitures", "camions", "scooters"];
export const VEHICULE_CATEGORIES_PLAQUE_OBLIGATOIRE = ["motos", "voitures", "scooters"];

export const TYPES_DOMMAGE = [
  { value: "dommage-vehicule", labelKey: "typesDommage.dommageVehicule" },
  { value: "dommage-propriete", labelKey: "typesDommage.dommagePropriete" },
  { value: "autre", labelKey: "typesDommage.autre" },
] as const;

export const DEVISES = [
  { value: "CHF", labelKey: "incidentTypes.chf" },
  { value: "EUR", labelKey: "incidentTypes.euro" },
  { value: "USD", labelKey: "incidentTypes.usd" },
] as const;

const ADRESSE_MIN_CHARS = 5;
export const VALIDATION_LIMITS = {
  NOM_MIN: 2,
  PRENOM_MIN: 2,
  ADRESSE_MIN: ADRESSE_MIN_CHARS,
  DESCRIPTION_INCIDENT_MIN: 20,
  TELEPHONE_MIN: 10,
  NPA_LENGTH_MIN: 4,
};

export const EMAIL_CHALLENGE_CODE_LENGTH = 6;
export const EMAIL_CHALLENGE_CODE_MAX_DIGITS = 12;

/** 1024 × 1024 octets (calcul tailles fichiers en Mo). */
export const BYTES_PER_MEGABYTE = 1024 * 1024;

/** Plafond courant pour une pièce jointe « annonce / preuve » (5 Mo), distinct du max global upload. */
const MAX_PIECE_JOINTE_SINGLE_MO = 5;
export const MAX_PIECE_JOINTE_SINGLE_5MO = MAX_PIECE_JOINTE_SINGLE_MO * BYTES_PER_MEGABYTE;

export const VUETIFY_CARD_ELEVATION_DEFAULT = 1;
export const VUETIFY_CARD_ELEVATION_DARK = 2;
export const VUETIFY_CARD_ELEVATION_DRAG_ACTIVE = 6;

export const RIPOL = {
  CODE_TELEPHONE_MOBILE: "713103",
  PREFIX_TELEPHONE_MOBILE: "7131",
  PREFIX_TABLETTE: "724",
  MASTER_TYPE_OBJETS: "183",
  MASTER_TYPE_VEHICULES: "101",
  ART_MARKE: "ART_MARKE",
  ART_MODELL: "ART_MODELL",
  PAYS_SUISSE: "8100",
  PAYS_FRANCE: "8212",
  PAYS_ALLEMAGNE: "8207",
  PAYS_ITALIE: "8218",
  PAYS_AUTRICHE: "8203",
  PAYS_INTERPOL: "9984",
};

export const API_URLS = {
  GENEVA_ADDRESS_API: "https://ge.ch/teradressews/v1/rest/searchaddress",
  COUNTRIES_API: "https://restcountries.com/v3.1/all?fields=name,cca2,flag,translations",
} as const;

export const SCROLL_CONFIG = {
  FOCUS_DELAY: 300,
  CONDITIONAL_SCROLL_DELAY: 500,
} as const;

export const CONDITIONAL_FIELD_PATTERNS = [
  "vol",
  "dommage",
  "typeDommage",
  "fabricant",
  "modele",
  "montantDommage",
] as const;

export const POLICE_STATIONS = {
  Cornavin: { lat: 46.20999121886902, lng: 6.141808032400459, address: "Place de Cornavin 5, 1201 Genève" },
  Paquis: { lat: 46.20899939628819, lng: 6.145118821804898, address: "Rue de Berne 6, 1201 Genève" },
  Plainpalais: { lat: 46.19344257152362, lng: 6.14392657938177, address: "Rue de Carouge 51, 1205 Genève" },
  Servette: { lat: 46.215273369637636, lng: 6.125447164143647, address: "Route de Meyrin 24B / 24D, 1202 Genève" },
  Carouge: { lat: 46.18377018302642, lng: 6.145298551445798, address: "Rue de la Fontenette 18, 1227 Carouge" },
  LancyOnex: {
    lat: 46.189413471209704,
    lng: 6.113394299522636,
    address: "Route du Pont-Butin 55, 1213 Petit-Lancy",
  },
  CheneBourg: { lat: 46.1935566795742, lng: 6.199764331611382, address: "Rue de Genève 93, 1226 Thônex" },
  Versoix: { lat: 46.27909896323438, lng: 6.167647538289251, address: "Place Charles-David 5, 1290 Versoix" },
  Blandonnet: { lat: 46.22032890620715, lng: 6.0975205706653055, address: "Chemin de Blandonnet 2, 1214 Vernier" },
  Pallanterie: { lat: 46.24669250007614, lng: 6.215145631240152, address: "Route de la Capite 249, 1222 Vésenaz" },
  Navigation: { lat: 46.205436536671016, lng: 6.15627531980645, address: "Quai Gustave-Ador 11, 1207 Genève" },
  Aeroport: { lat: 46.23047212180996, lng: 6.107314945362361, address: "Route de l'Aéroport 21, 1215 Genève 15" },
} as const;

export const POLICE_STATION_NAME_VARIATIONS: { [key: string]: string[] } = {
  Cornavin: ["cornavin", "gare", "montbrillant"],
  Paquis: ["pâquis", "pâqui", "berne"],
  Plainpalais: ["plainpalais", "carouge"],
  Servette: ["servette", "meyrin"],
  Carouge: ["carouge", "fontenette"],
  LancyOnex: ["lancy", "onex", "pont-butin"],
  CheneBourg: ["chêne", "chêne-bourg", "thônex"],
  Versoix: ["versoix", "charles-david"],
  Blandonnet: ["blandonnet", "vernier"],
  Pallanterie: ["pallanterie", "capite", "vésenaz"],
  Navigation: ["navigation", "gustave-ador"],
  Aeroport: ["aéroport", "aéroport de genève", "genève 15"],
} as const;

export const LATITUDE_GE = 46.2044;
export const LONGITUDE_GE = 6.1432;

export const MAX_FILE_SIZE = 10 * BYTES_PER_MEGABYTE;
export const MAX_TOTAL_SIZE_20_MO = 20 * BYTES_PER_MEGABYTE;
const MAX_TOTAL_SIZE_UPLOAD_MO = 70;
export const MAX_TOTAL_SIZE_70_MO = MAX_TOTAL_SIZE_UPLOAD_MO * BYTES_PER_MEGABYTE;
export const MAX_FILES = 10;
export const VALID_SIGNATURES_FILES = {
  pdf: [0x25, 0x50, 0x44, 0x46],
  png: [0x89, 0x50, 0x4e, 0x47],
  jpg: [0xff, 0xd8],
  jpeg: [0xff, 0xd8],
  tif_le: [0x49, 0x49],
  tif_be: [0x4d, 0x4d],
};
