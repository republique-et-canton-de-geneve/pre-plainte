import { EMAIL_CHALLENGE_CODE_LENGTH } from "@/constants/constant";
import { isValidEmailChallengeCodeFormat } from "@/utils/validations/field-validation.utils";

export interface EmailVerificationContinueState {
  hasSendError: boolean;
  devBypassEmail: boolean;
  emailValide: boolean;
  telephoneValide?: boolean;
  codeSent: boolean;
  confirmationEmail?: string | null;
}

export function canContinueEmailVerification(state: EmailVerificationContinueState): boolean {
  if (state.telephoneValide === false) {
    return false;
  }

  if (!state.hasSendError && state.devBypassEmail && state.emailValide) {
    return true;
  }

  return (
    !state.hasSendError &&
    state.codeSent &&
    state.emailValide &&
    isValidEmailChallengeCodeFormat(String(state.confirmationEmail ?? ""))
  );
}

export function shouldResetEmailChallenge(
  nextEmail: string,
  codeSent: boolean,
  challengeEmailSnapshot: string,
): boolean {
  return codeSent && Boolean(challengeEmailSnapshot) && nextEmail.trim() !== challengeEmailSnapshot;
}

export function resolveDevBypassConfirmation(confirmation: string): string {
  if (isValidEmailChallengeCodeFormat(confirmation)) {
    return confirmation;
  }
  return "0".repeat(EMAIL_CHALLENGE_CODE_LENGTH);
}
