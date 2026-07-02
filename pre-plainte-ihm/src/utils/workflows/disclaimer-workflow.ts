export interface DisclaimerContinueState {
  typeIncident?: string | null;
  typeDommage?: string | null;
  constatPresent?: boolean | null;
  typeCybercrime?: string | null;
  confirmeIdentite?: boolean;
  confirmeSituation?: boolean;
  confirmeEffraction?: boolean;
  disclaimerConfirmed?: boolean;
  captchaEnabled?: boolean;
  captchaToken?: string | null;
}

export const TYPE_CYBERCRIME_AUTRE = "autre";
export const DEGAT_DELIT_INCIDENT = "degat-delit";
export const CYBERCRIME_INCIDENT = "cybercrime";
export const DOMMAGE_VEHICULE = "dommage-vehicule";
export const DOMMAGE_PROPRIETE = "dommage-propriete";

export function canContinueDisclaimer(state: DisclaimerContinueState): boolean {
  const typeIncident = state.typeIncident ?? "";
  return hasValidIncident(state, typeIncident) &&
    state.disclaimerConfirmed === true &&
    hasValidCaptcha(state);
}

function hasValidIncident(state: DisclaimerContinueState, typeIncident: string): boolean {
  if (!typeIncident || state.typeCybercrime === TYPE_CYBERCRIME_AUTRE) {
    return false;
  }
  if (typeIncident === DEGAT_DELIT_INCIDENT) {
    return Boolean(state.typeDommage) && hasValidDommageConstat(state);
  }
  if (typeIncident === CYBERCRIME_INCIDENT) {
    return Boolean(state.typeCybercrime);
  }
  return true;
}

function hasValidDommageConstat(state: DisclaimerContinueState): boolean {
  if (!requiresConstatQuestion(state.typeDommage)) {
    return true;
  }
  return state.constatPresent !== undefined && state.constatPresent !== null;
}

export function requiresConstatQuestion(typeDommage?: string | null): boolean {
  return typeDommage === DOMMAGE_VEHICULE || typeDommage === DOMMAGE_PROPRIETE;
}

export function isRendezVousOnlyDommage(state: DisclaimerContinueState): boolean {
  return state.typeIncident === DEGAT_DELIT_INCIDENT &&
    requiresConstatQuestion(state.typeDommage) &&
    state.constatPresent === true;
}

function hasConfirmedDisclaimer(state: DisclaimerContinueState): boolean {
  return state.confirmeIdentite === true && state.confirmeSituation === true && state.confirmeEffraction === true;
}

function hasValidCaptcha(state: DisclaimerContinueState): boolean {
  return state.captchaEnabled !== true || Boolean(state.captchaToken);
}

export function validateDisclaimerConfirmations(
  state: Pick<DisclaimerContinueState, "confirmeIdentite" | "confirmeSituation" | "confirmeEffraction">,
): boolean {
  return hasConfirmedDisclaimer(state);
}

export function shouldResetTypeDommage(typeIncident?: string | null): boolean {
  return typeIncident !== DEGAT_DELIT_INCIDENT;
}

export function shouldResetTypeCybercrime(typeIncident?: string | null): boolean {
  return typeIncident !== CYBERCRIME_INCIDENT;
}
