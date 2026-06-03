import {
  CATEGORIES_OBJETS,
  RIPOL,
  VEHICULE_CATEGORIES_AVEC_PLAQUE,
  VEHICULE_CATEGORIES_PLAQUE_OBLIGATOIRE,
} from "@/constants/constant";
import type { PrePlainteFormFields, VolObjetFormSnapshot } from "@/types/pre-plainte.interface";

export type VolObjetVolTranslate = (key: string, ...args: unknown[]) => string;

export function hasImeiPourSnapshotVol(obj: VolObjetFormSnapshot): boolean {
  const code = obj.typeObjet?.code;
  if (!code) {
    return false;
  }
  return code.startsWith(RIPOL.PREFIX_TELEPHONE_MOBILE) || code.startsWith(RIPOL.PREFIX_TABLETTE);
}

type VolObjetAvecPlaque = Pick<
  VolObjetFormSnapshot,
  "isVehicle" | "categorieObjet" | "sousCategorie" | "plaqueInconnu" | "plaqueNumero"
>;

type VolAvecObjetsEffectifs = Pick<
  PrePlainteFormFields,
  "isVehicle" | "categorieObjet" | "sousCategorie" | "plaqueInconnu" | "plaqueNumero" | "objetsVolesValides"
>;

export function getObjetsVolesEffectifs(data: VolAvecObjetsEffectifs): VolObjetAvecPlaque[] {
  const objetsVoles = data.objetsVolesValides ?? [];
  return objetsVoles.length > 0 ? objetsVoles : [data];
}

export function isObjetVoleVehiculeAvecPlaque(objet: VolObjetAvecPlaque): boolean {
  return (
    (objet.isVehicle === true || objet.categorieObjet === "vehicule") &&
    !!objet.sousCategorie &&
    VEHICULE_CATEGORIES_AVEC_PLAQUE.includes(objet.sousCategorie) &&
    objet.plaqueInconnu !== true &&
    !!objet.plaqueNumero?.trim()
  );
}

export function hasVehiculeVoleAvecPlaque(data: VolAvecObjetsEffectifs): boolean {
  return getObjetsVolesEffectifs(data).some(objet => isObjetVoleVehiculeAvecPlaque(objet));
}

export function libelleCategorieVolObjet(value: string, t: VolObjetVolTranslate): string {
  const cat = CATEGORIES_OBJETS.find(c => c.value === value);
  return cat ? t(cat.labelKey) : value;
}

export function libelleSousCategorieVolObjet(sous: string, categorie: string, t: VolObjetVolTranslate): string {
  const c = CATEGORIES_OBJETS.find(x => x.value === categorie);
  const sub = c?.subCategories?.find(s => s.value === sous);
  return sub ? t(sub.labelKey) : sous;
}

const TEXTE_VIDE = "";

export function libelleFabricantResumeVolObjet(obj: VolObjetFormSnapshot): string {
  if (obj.fabricant?.code === "AUTRE" && obj.fabricantAutre) {
    return obj.fabricantAutre;
  }
  return obj.fabricant?.label ?? TEXTE_VIDE;
}

export function libelleModeleResumeVolObjet(obj: VolObjetFormSnapshot): string {
  if (obj.modele?.code === "AUTRE" && obj.modeleAutre) {
    return obj.modeleAutre;
  }
  return obj.modele?.label ?? TEXTE_VIDE;
}

export function affichePlaquePourSousCategorie(sousCategorie?: string): boolean {
  return !!sousCategorie && VEHICULE_CATEGORIES_AVEC_PLAQUE.includes(sousCategorie);
}

export function isPlaqueObligatoirePourSousCategorie(sousCategorie?: string): boolean {
  return !!sousCategorie && VEHICULE_CATEGORIES_PLAQUE_OBLIGATOIRE.includes(sousCategorie);
}

type ChampsPlaqueVehicule = {
  sousCategorie?: string;
  plaqueInconnu?: boolean;
  plaqueNumero?: string;
  plaquePays?: { code?: string } | null;
  plaqueCanton?: { code?: string } | null;
};

export function validerPlaqueVehicule(
  champs: ChampsPlaqueVehicule,
  setFieldError: (field: string, message: string) => void,
  t: VolObjetVolTranslate,
): boolean {
  if (!affichePlaquePourSousCategorie(champs.sousCategorie)) {
    return true;
  }
  if (champs.plaqueInconnu && !isPlaqueObligatoirePourSousCategorie(champs.sousCategorie)) {
    return true;
  }
  if (!champs.plaquePays?.code) {
    setFieldError("plaquePays", t("validation.champRequis"));
    return false;
  }
  if (!champs.plaqueNumero?.trim()) {
    setFieldError("plaqueNumero", t("validation.champRequis"));
    return false;
  }
  if (champs.plaquePays.code === RIPOL.PAYS_SUISSE && !champs.plaqueCanton?.code) {
    setFieldError("plaqueCanton", t("validation.champRequis"));
    return false;
  }
  return true;
}
