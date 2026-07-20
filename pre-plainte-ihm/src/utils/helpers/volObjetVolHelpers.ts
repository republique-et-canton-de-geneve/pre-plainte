import {
  CATEGORIES_OBJETS,
  RIPOL,
  VEHICULE_CATEGORIES_AVEC_PLAQUE,
  VEHICULE_CATEGORIES_PLAQUE_OBLIGATOIRE,
} from "@/constants/constant";
import type { PrePlainteFormFields, VolObjetFormSnapshot } from "@/types/pre-plainte.interface";

const PLAQUE_SUISSE_REGEX = /^[A-Z]{2}\s\d{1,6}$/;
const PLAQUE_FRANCE_SIV_REGEX = /^[A-Z]{2}-\d{3}-[A-Z]{2}$/;
const PLAQUE_FRANCE_FNI_REGEX = /^\d{1,4}\s[A-Z]{1,3}\s(\d{2,3}|2A|2B)$/;
const PLAQUE_INTERNATIONALE_REGEX = /^[A-Z\d]{1,12}$/;

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
  const isVehicle = objet.isVehicle === true || objet.categorieObjet === "vehicule";
  const isPlaqueCategory = !!objet.sousCategorie && VEHICULE_CATEGORIES_AVEC_PLAQUE.includes(objet.sousCategorie);
  const hasValidPlaque = objet.plaqueInconnu !== true && !!objet.plaqueNumero?.trim();
  return isVehicle && isPlaqueCategory && hasValidPlaque;
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
  if (obj.modeleAutre) {
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
    setFieldError("plaquePays", t("validation.plaquePaysRequise"));
    return false;
  }
  if (champs.plaquePays.code === RIPOL.PAYS_SUISSE && !champs.plaqueCanton?.code) {
    setFieldError("plaqueCanton", t("validation.plaqueCantonRequis"));
    return false;
  }
  return validerNumeroPlaque(champs, setFieldError, t);

}

export function validerNumeroPlaque(
  champs: any,
  setFieldError: (field: string, message: string) => void,
  t: VolObjetVolTranslate,
): boolean {
  if (champs.plaqueNumero?.trim()) {
    const numeroPlaque = champs.plaqueNumero
      .trim()
      .toUpperCase()
      .replaceAll(/\s+/g, " ");

    const paysCode = champs.plaquePays?.code;

    if (paysCode === RIPOL.PAYS_SUISSE) {
      if (!PLAQUE_SUISSE_REGEX.test(numeroPlaque)) {
        setFieldError(
          "plaqueNumero",
          t("validation.numeroPlaqueSuisseInvalide"),
        );
        return false;
      }
    } else if (paysCode === RIPOL.PAYS_FRANCE) {
      const isFrenchPlateValid =
        PLAQUE_FRANCE_SIV_REGEX.test(numeroPlaque) ||
        PLAQUE_FRANCE_FNI_REGEX.test(numeroPlaque);

      if (!isFrenchPlateValid) {
        setFieldError(
          "plaqueNumero",
          t("validation.numeroPlaqueFranceInvalide"),
        );
        return false;
      }
    } else if (!PLAQUE_INTERNATIONALE_REGEX.test(numeroPlaque)) {
      setFieldError(
        "plaqueNumero",
        t("validation.numeroPlaqueInternationaleInvalide"),
      );
      return false;
    }
    return true;
  } else {
    setFieldError("plaqueNumero", t("validation.plaqueNumeroRequise"));
    return false;
  }
}

export function checkLength(value: unknown, max: number): boolean {
  return typeof value !== "string" || value.length <= max;
}

export const chaineFormulaire = (v: string) => v ?? TEXTE_VIDE;

export const texteOuVide = (v: string | undefined | null) => v ?? TEXTE_VIDE;
