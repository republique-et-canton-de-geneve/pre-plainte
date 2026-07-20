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
}

export const validateFabricantEtModele = async ({
  fabricant,
  fabricantAutre,
  modele,
  modeleAutre,
  setFieldError,
  t,
}: ValidateFabricantEtModeleParams): Promise<boolean> => {
  let isValid = true;

  if (!fabricant?.code) {
    setFieldError("fabricant", t("validation.fabricantRequis"));
    return false;
  }

  if (fabricant.code === "AUTRE" && !chaineFormulaire(fabricantAutre).trim()) {
    setFieldError("fabricantAutre", t("validation.fabricantAutreRequis"));
    isValid = false;
  }

  if (fabricant.code === "AUTRE") {
    return isValid;
  }

  const models = await RipolService.searchVehicleModels(fabricant.code);

  if (models.length > 0 && !modele?.code) {
    setFieldError("modele", t("validation.modeleRequis"));
    isValid = false;
  }

  if (
    (modele?.code === "AUTRE" || models.length === 0) &&
    !chaineFormulaire(modeleAutre).trim()
  ) {
    setFieldError("modeleAutre", t("validation.modeleAutreRequis"));
    isValid = false;
  }

  return isValid;
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
  let isValid = true;

  if (isVeloCategory && !numeroCadreInconnu && !chaineFormulaire(numeroCadre).trim()) {
    setFieldError("numeroCadre", t("validation.numeroCadreRequis"));
    isValid = false;
  }

  if (hasVin && !vinInconnu && !chaineFormulaire(vin).trim()) {
    setFieldError("vin", t("validation.vinRequis"));
    isValid = false;
  }

  return isValid;
};

export const validateLongueurs = (
  rules: ValidationLengthRule[],
  setFieldError: (field: string, message: string) => void,
  t: (key: string, params?: Record<string, unknown>) => string,
  messageKey: string,
): boolean => {
  let isValid = true;

  for (const rule of rules) {
    if (!checkLength(rule.value(), rule.max)) {
      setFieldError(rule.field, t(messageKey, { max: rule.max }));
      isValid = false;
    }
  }

  return isValid;
};
