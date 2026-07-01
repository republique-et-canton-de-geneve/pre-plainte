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

export function canContinueDisclaimer(state: DisclaimerContinueState): boolean {
  const typeIncident = state.typeIncident ?? "";
  return (
    Boolean(typeIncident) &&
    (typeIncident !== "degat-delit" || Boolean(state.typeDommage)) &&
    (typeIncident !== "cybercrime" || Boolean(state.typeCybercrime)) &&
    state.typeCybercrime !== TYPE_CYBERCRIME_AUTRE &&
    state.confirmeIdentite === true &&
    state.confirmeSituation === true &&
    (state.captchaEnabled !== true || Boolean(state.captchaToken))
  );
}

export function shouldResetTypeDommage(typeIncident?: string | null): boolean {
  return typeIncident !== "degat-delit";
}

export function shouldResetTypeCybercrime(typeIncident?: string | null): boolean {
  return typeIncident !== "cybercrime";
}
