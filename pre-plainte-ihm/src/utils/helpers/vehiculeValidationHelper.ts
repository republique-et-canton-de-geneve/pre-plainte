import { RipolService } from "@/services/ripolService.ts";
import { chaineFormulaire, checkLength } from "@/utils/helpers/volObjetVolHelpers.ts";

export interface ValidationLengthRule {
  field: string;
  value: () => string | undefined;
  max: number;
}

interface ValidateFabricantEtModeleParams {
  fabricant: any;
  fabricantAutre: string;
  modele: any;
  modeleAutre: string;
  setFieldError: (field: string, message: string) => void;
  t: (key: string, params?: any) => string;
  champRequisErreur: string;
}

export const validateFabricantEtModele = async ({
  fabricant,
  fabricantAutre,
  modele,
  modeleAutre,
  setFieldError,
  t,
  champRequisErreur,
}: ValidateFabricantEtModeleParams): Promise<boolean> => {
  if (!fabricant?.code) {
    setFieldError("fabricant", t("validation.fabricantRequis"));
    return false;
  }

  if (
    fabricant.code === "AUTRE"
    && !chaineFormulaire(fabricantAutre).trim()
  ) {
    setFieldError("fabricantAutre", t(champRequisErreur));
    return false;
  }

  if (fabricant.code === "AUTRE") {
    return true;
  }

  const models = await RipolService.searchVehicleModels(fabricant.code);

  if (models.length > 0 && !modele?.code) {
    setFieldError("modele", t("validation.modeleRequis"));
    return false;
  }

  if (
    (modele?.code === "AUTRE" || models.length === 0)
    && !chaineFormulaire(modeleAutre).trim()
  ) {
    setFieldError("modeleAutre", t(champRequisErreur));
    return false;
  }

  return true;
};

interface ValidateNumeroCadreEtVinParams {
  isVeloCategory: boolean;
  numeroCadreInconnu: boolean;
  numeroCadre: string;
  hasVin: boolean;
  vinInconnu: boolean;
  vin: string;
  setFieldError: (field: string, message: string) => void;
  t: (key: string) => string;
}

export const validateNumeroCadreEtVin = ({
   isVeloCategory,
   numeroCadreInconnu,
   numeroCadre,
   hasVin,
   vinInconnu,
   vin,
   setFieldError,
   t,
 }: ValidateNumeroCadreEtVinParams): boolean => {
  if (
    isVeloCategory
    && !numeroCadreInconnu
    && !chaineFormulaire(numeroCadre).trim()
  ) {
    setFieldError("numeroCadre", t("validation.numeroCadreRequis"));
    return false;
  }

  if (
    hasVin
    && !vinInconnu
    && !chaineFormulaire(vin).trim()
  ) {
    setFieldError("vin", t("validation.vinRequis"));
    return false;
  }

  return true;
};

export const validateLongueurs = (
  rules: ValidationLengthRule[],
  setFieldError: (field: string, message: string) => void,
  t: (key: string, params?: any) => string,
  validationLongueurMax: string,
): boolean => {
  for (const rule of rules) {
    if (!checkLength(rule.value(), rule.max)) {
      setFieldError(
        rule.field,
        t(validationLongueurMax, { max: rule.max }),
      );
      return false;
    }
  }

  return true;
};
