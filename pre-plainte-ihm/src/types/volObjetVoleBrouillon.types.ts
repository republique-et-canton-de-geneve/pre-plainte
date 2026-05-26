import type { VolObjetFormSnapshot } from "@/types/pre-plainte.interface";
import type { Ripol, RipolSelection } from "@/types/ripol.interface";

/**
 * Contrat minimal du brouillon multi-objet pour {@link VolObjetVoleDraftPanel.vue}.
 * Correspond à l’objet réactif exposé par VolForm (refs / computed déballés par reactive).
 */
export interface VolObjetVoleDraftBrouillon {
  categorieObjet: string;
  categorieObjetError?: string;
  categorieOptions: { value: string; label: string }[];
  sousCategorie: string;
  sousCategorieError?: string;
  subCategorieOptions: { value: string; label: string; prefixes?: string[] }[];
  typeObjet: RipolSelection | null;
  typeObjetError?: string;
  objetTypeKey: string;
  fetchFilteredObjectTypes: () => Promise<Ripol[]>;
  fabricant: RipolSelection | null;
  fabricantError?: string;
  fabricantAutre: string;
  fabricantAutreError?: string;
  hasBrands: boolean;
  brandKey: string;
  fetchBrandsWithAutre: () => Promise<Ripol[]>;
  isAutreFabricant: boolean;
  modele: RipolSelection | null;
  modeleError?: string;
  modelKey: string;
  fetchModelsWithAutre: () => Promise<Ripol[]>;
  hasModels: boolean;
  modelsLoading: boolean;
  isAutreModele: boolean;
  modeleAutre: string;
  modeleAutreError?: string;
  couleur: RipolSelection | null;
  couleurError?: string;
  colourKey: string;
  fetchColours: () => Promise<Ripol[]>;
  couleurSecondaire: RipolSelection | null;
  gravure: string;
  isBijouxCategory: boolean;
  valeurReelle: string;
  valeurReelleError?: string;
  numeroSerie: string;
  numeroSerieError?: string;
  numeroSerieInconnu: boolean;
  hasImei: boolean;
  numeroIMEI: string;
  numeroIMEIError?: string;
  numeroIMEIInconnu: boolean;
  justificationAbsenceIMEI: string;
  descriptionObjet: string;
  descriptionObjetError?: string;
  plaqueNumero: string;
  plaqueNumeroError?: string;
  plaquePays: RipolSelection | null;
  plaquePaysError?: string;
  fetchFilteredNationalities: (search?: string) => Promise<Ripol[]>;
  objetsVolesValides?: VolObjetFormSnapshot[] | null;
  validerObjetVole: () => void;
}
