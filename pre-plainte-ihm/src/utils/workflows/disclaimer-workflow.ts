export interface DisclaimerContinueState {
  typeIncident?: string | null;
  typeDommage?: string | null;
  typeCybercrime?: string | null;
  confirmeIdentite?: boolean;
  confirmeSituation?: boolean;
  captchaEnabled?: boolean;
  captchaToken?: string | null;
}

export const TYPE_CYBERCRIME_AUTRE = "autre";
export const DEGAT_DELIT_INCIDENT = "degat-delit";
export const CYBERCRIME_INCIDENT = "cybercrime";

export function canContinueDisclaimer(state: DisclaimerContinueState): boolean {
  const typeIncident = state.typeIncident ?? "";
  return hasValidIncident(state, typeIncident) &&
    hasConfirmedDisclaimer(state) &&
    hasValidCaptcha(state);
}

function hasValidIncident(state: DisclaimerContinueState, typeIncident: string): boolean {
  if (!typeIncident || state.typeCybercrime === TYPE_CYBERCRIME_AUTRE) {
    return false;
  }
  if (typeIncident === DEGAT_DELIT_INCIDENT) {
    return Boolean(state.typeDommage);
  }
  if (typeIncident === CYBERCRIME_INCIDENT) {
    return Boolean(state.typeCybercrime);
  }
  return true;
}

function hasConfirmedDisclaimer(state: DisclaimerContinueState): boolean {
  return state.confirmeIdentite === true && state.confirmeSituation === true;
}

function hasValidCaptcha(state: DisclaimerContinueState): boolean {
  return state.captchaEnabled !== true || Boolean(state.captchaToken);
}

export function shouldResetTypeDommage(typeIncident?: string | null): boolean {
  return typeIncident !== DEGAT_DELIT_INCIDENT;
}

export function shouldResetTypeCybercrime(typeIncident?: string | null): boolean {
  return typeIncident !== CYBERCRIME_INCIDENT;
}
