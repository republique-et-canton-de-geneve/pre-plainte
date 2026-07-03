import { z } from "zod";
import type { ComposerTranslation } from "vue-i18n";
import { isUrlWebAvecDomaine } from "@/utils/validations/field-validation.utils";

function addPathIssue(ctx: z.RefinementCtx, path: string, message: string) {
  ctx.addIssue({
    code: z.ZodIssueCode.custom,
    path: [path],
    message,
  });
}

function isBlankTrimmed(v: unknown): boolean {
  return typeof v !== "string" || v.trim().length === 0;
}

function requireTrimmedString(data: Record<string, any>, ctx: z.RefinementCtx, path: string, message: string): void {
  if (isBlankTrimmed(data[path])) {
    addPathIssue(ctx, path, message);
  }
}

function requireSelectedBoolean(
  value: unknown,
  ctx: z.RefinementCtx,
  path: string,
  message: string,
): void {
  if (value === undefined || value === null) {
    addPathIssue(ctx, path, message);
  }
}

function validateOptionalUrlField(value: unknown, ctx: z.RefinementCtx, path: string, invalidMessage: string): void {
  const raw = typeof value === "string" ? value.trim() : "";
  if (!raw) {
    return;
  }
  if (!isUrlWebAvecDomaine(raw)) {
    addPathIssue(ctx, path, invalidMessage);
  }
}

function validateContrevenant(data: Record<string, any>, ctx: z.RefinementCtx, t: ComposerTranslation): void {
  requireTrimmedString(data, ctx, "prenomContrevenant", t("validation.prenomContrevenantRequis"));
  requireTrimmedString(data, ctx, "nomContrevenant", t("validation.nomContrevenantRequis"));
  validateOptionalUrlField(data.siteWebContrevenant, ctx, "siteWebContrevenant", t("validation.siteWebContrevenantFormat"));
  requireSelectedBoolean(
    data.moyenPaiementNumeriqueDebite,
    ctx,
    "moyenPaiementNumeriqueDebite",
    t("validation.moyenPaiementNumeriqueDebiteRequis"),
  );
}

function requireNonEmptyFileArray(
  value: unknown,
  ctx: z.RefinementCtx,
  path: string,
  message: string,
): void {
  if (!Array.isArray(value) || value.length === 0) {
    addPathIssue(ctx, path, message);
  }
}

function validateIdentite(data: Record<string, any>, ctx: z.RefinementCtx, t: ComposerTranslation): void {
  requireSelectedBoolean(
    data.copieIdentiteTransmiseAuteur,
    ctx,
    "copieIdentiteTransmiseAuteur",
    t("validation.copieIdentiteTransmiseAuteurRequise"),
  );
  if (data.copieIdentiteTransmiseAuteur) {
    if (data.copieIdentiteTransmiseAuteurDocumentIndisponible) {
      requireTrimmedString(
        data,
        ctx,
        "raisonAbsenceCopieIdentiteTransmiseAuteur",
        t("validation.raisonAbsenceCopieIdentiteTransmiseAuteurRequise"),
      );
    } else {
      requireNonEmptyFileArray(
        data.copieIdentiteTransmiseAuteurDocument,
        ctx,
        "copieIdentiteTransmiseAuteurDocument",
        t("validation.copieIdentiteTransmiseAuteurDocumentRequise"),
      );
    }
  }

  requireSelectedBoolean(
    data.copieIdentiteAuteurTransmise,
    ctx,
    "copieIdentiteAuteurTransmise",
    t("validation.copieIdentiteAuteurTransmiseRequise"),
  );
  if (data.copieIdentiteAuteurTransmise) {
    if (data.copieIdentiteAuteurDocumentIndisponible) {
      requireTrimmedString(
        data,
        ctx,
        "raisonAbsenceCopieIdentiteAuteur",
        t("validation.raisonAbsenceCopieIdentiteAuteurRequise"),
      );
    } else {
      requireNonEmptyFileArray(
        data.copieIdentiteAuteurDocument,
        ctx,
        "copieIdentiteAuteurDocument",
        t("validation.copieIdentiteAuteurDocumentRequise"),
      );
    }
  }
}

export function validateCommandeFrauduleuseCybercrime(
  data: Record<string, any>,
  ctx: z.RefinementCtx,
  t: ComposerTranslation,
): void {
  if (data.typeIncident !== "cybercrime" || data.typeCybercrime !== "commande-frauduleuse") {
    return;
  }
  validateContrevenant(data, ctx, t);
  validateIdentite(data, ctx, t);
}
