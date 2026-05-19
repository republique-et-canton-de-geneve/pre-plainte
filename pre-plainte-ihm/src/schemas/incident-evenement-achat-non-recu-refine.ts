import { z } from "zod";
import type { ComposerTranslation } from "vue-i18n";
import { isUrlWebAvecDomaine } from "@/utils/validations/field-validation.utils";

function addPathIssue(ctx: z.RefinementCtx, path: string[], message: string) {
  ctx.addIssue({
    code: z.ZodIssueCode.custom,
    path,
    message,
  });
}

function isBlankTrimmed(v: unknown): boolean {
  return typeof v !== "string" || v.trim().length === 0;
}

function requireTrimmedString(data: Record<string, any>, ctx: z.RefinementCtx, path: string, message: string): void {
  if (isBlankTrimmed(data[path])) {
    addPathIssue(ctx, [path], message);
  }
}

function requireSelectedBoolean(
  value: unknown,
  ctx: z.RefinementCtx,
  path: string,
  message: string,
): void {
  if (value === undefined || value === null) {
    addPathIssue(ctx, [path], message);
  }
}

function validateEmailIfPresent(
  email: unknown,
  ctx: z.RefinementCtx,
  path: string,
  requiredMessage: string,
  invalidMessage: string,
): void {
  const raw = typeof email === "string" ? email.trim() : "";
  if (!raw) {
    addPathIssue(ctx, [path], requiredMessage);
    return;
  }
  if (!z.string().email().safeParse(raw).success) {
    addPathIssue(ctx, [path], invalidMessage);
  }
}

function validatePlateformeIdIfPresent(value: unknown, ctx: z.RefinementCtx, path: string, invalidMessage: string): void {
  const raw = typeof value === "string" ? value.trim() : "";
  if (!raw) {
    return;
  }
  if (!isUrlWebAvecDomaine(raw)) {
    addPathIssue(ctx, [path], invalidMessage);
  }
}

function requireNonEmptyFileArray(
  value: unknown,
  ctx: z.RefinementCtx,
  path: string,
  message: string,
): void {
  if (!Array.isArray(value) || value.length === 0) {
    addPathIssue(ctx, [path], message);
  }
}

function validateSiteWebEntreprise(value: unknown, ctx: z.RefinementCtx, path: string, invalidMessage: string): void {
  const raw = typeof value === "string" ? value.trim() : "";
  if (!raw) {
    return;
  }
  if (!isUrlWebAvecDomaine(raw)) {
    addPathIssue(ctx, [path], invalidMessage);
  }
}

function validateAchatNonRecuDates(data: Record<string, any>, ctx: z.RefinementCtx, t: ComposerTranslation): void {
  requireTrimmedString(data, ctx, "datePremierContact", t("validation.datePremierContactRequise"));
  requireTrimmedString(data, ctx, "heurePremierContact", t("validation.heureDebutEvenementRequise"));
  requireTrimmedString(data, ctx, "dateDernierContact", t("validation.dateDernierContactRequise"));
  requireTrimmedString(data, ctx, "heureDernierContact", t("validation.heureFinEvenementRequise"));
}

function validateAchatNonRecuDescription(data: Record<string, any>, ctx: z.RefinementCtx, t: ComposerTranslation): void {
  requireTrimmedString(data, ctx, "montantDelitAchatLigne", t("validation.montantDelitAchatLigneRequis"));
  requireTrimmedString(data, ctx, "descriptionCybercrime", t("validation.descriptionCybercrimeRequise"));
  requireTrimmedString(data, ctx, "articleNonLivreDescription", t("validation.articleNonLivreDescriptionRequise"));
}

function validateAchatNonRecuVendeur(data: Record<string, any>, ctx: z.RefinementCtx, t: ComposerTranslation): void {
  requireTrimmedString(data, ctx, "prenomVendeur", t("validation.prenomVendeurRequis"));
  requireTrimmedString(data, ctx, "nomVendeur", t("validation.nomVendeurRequis"));

  if (!data.emailVendeurInconnu) {
    validateEmailIfPresent(
      data.emailVendeur,
      ctx,
      "emailVendeur",
      t("validation.emailVendeurRequis"),
      t("validation.emailVendeurFormat"),
    );
  }

  if (!data.telephoneVendeurInconnu) {
    requireTrimmedString(data, ctx, "telephoneVendeur", t("validation.telephoneVendeurRequis"));
  }
}

function validateAchatNonRecuPlateforme(data: Record<string, any>, ctx: z.RefinementCtx, t: ComposerTranslation): void {
  requireSelectedBoolean(data.achatViaPlaceMarche, ctx, "achatViaPlaceMarche", t("validation.achatViaPlaceMarcheRequis"));

  if (data.achatViaPlaceMarche === true) {
    requireTrimmedString(data, ctx, "plateforme", t("validation.plateformeUtiliseeRequise"));
    if (data.plateforme === "autre") {
      requireTrimmedString(data, ctx, "plateformeAutre", t("validation.plateformeAutreRequise"));
    }
    requireTrimmedString(data, ctx, "plateformeId", t("validation.plateformeIdRequis"));
    validatePlateformeIdIfPresent(data.plateformeId, ctx, "plateformeId", t("validation.plateformeUrlOuIdInvalide"));
  }

  if (data.achatViaPlaceMarche === false) {
    requireTrimmedString(data, ctx, "nomEntrepriseVendeur", t("validation.nomEntrepriseVendeurRequis"));
    requireTrimmedString(data, ctx, "siteWebEntrepriseVendeur", t("validation.siteWebEntrepriseVendeurRequis"));
    validateSiteWebEntreprise(
      data.siteWebEntrepriseVendeur,
      ctx,
      "siteWebEntrepriseVendeur",
      t("validation.urlSiteWebEntrepriseInvalide"),
    );
  }
}

function validateAchatNonRecuDocuments(data: Record<string, any>, ctx: z.RefinementCtx, t: ComposerTranslation): void {
  if (!data.annonceDocumentIndisponible) {
    requireNonEmptyFileArray(data.annonceDocument, ctx, "annonceDocument", t("validation.annonceDocumentRequis"));
  } else {
    requireTrimmedString(data, ctx, "raisonAbsenceAnnonce", t("validation.raisonAbsenceAnnonceRequise"));
  }

  requireTrimmedString(data, ctx, "dateOperation", t("validation.dateOperationRequise"));

  if (!data.preuvePaiementIndisponible) {
    requireNonEmptyFileArray(
      data.preuvePaiementDocument,
      ctx,
      "preuvePaiementDocument",
      t("validation.preuvePaiementDocumentRequis"),
    );
  } else {
    requireTrimmedString(
      data,
      ctx,
      "raisonAbsencePreuvePaiement",
      t("validation.raisonAbsencePreuvePaiementRequise"),
    );
  }
}

function validateAchatNonRecuPaiement(
  data: Record<string, any>,
  ctx: z.RefinementCtx,
  addCustomIssue: (c: z.RefinementCtx, path: string, message: string) => void,
  t: ComposerTranslation,
): void {
  if (isBlankTrimmed(data.moyenPaiement)) {
    addCustomIssue(ctx, "moyenPaiement", t("validation.moyenPaiementRequis"));
  }

  if (data.moyenPaiement === "iban") {
    requireTrimmedString(data, ctx, "ibanBeneficiaire", t("validation.ibanBeneficiaireRequis"));
  }
  if (data.moyenPaiement === "paypal") {
    requireTrimmedString(data, ctx, "comptePaypalBeneficiaire", t("validation.comptePaypalBeneficiaireRequis"));
  }
  if (data.moyenPaiement === "twint") {
    requireTrimmedString(data, ctx, "numeroTwintBeneficiaire", t("validation.numeroTwintBeneficiaireRequis"));
  }
  if (data.moyenPaiement === "crypto") {
    requireTrimmedString(data, ctx, "adresseWalletCrypto", t("validation.adresseWalletCryptoRequise"));
    requireTrimmedString(data, ctx, "hashTransactionCrypto", t("validation.hashTransactionCryptoRequis"));
  }
}

function validateAchatNonRecuIdentite(data: Record<string, any>, ctx: z.RefinementCtx, t: ComposerTranslation): void {
  requireSelectedBoolean(
    data.copieIdentiteTransmiseAuteur,
    ctx,
    "copieIdentiteTransmiseAuteur",
    t("validation.copieIdentiteTransmiseAuteurRequise"),
  );
  if (data.copieIdentiteTransmiseAuteur) {
    requireNonEmptyFileArray(
      data.copieIdentiteTransmiseAuteurDocument,
      ctx,
      "copieIdentiteTransmiseAuteurDocument",
      t("validation.copieIdentiteTransmiseAuteurDocumentRequise"),
    );
  }

  requireSelectedBoolean(
    data.copieIdentiteAuteurTransmise,
    ctx,
    "copieIdentiteAuteurTransmise",
    t("validation.copieIdentiteAuteurTransmiseRequise"),
  );
  if (data.copieIdentiteAuteurTransmise) {
    requireNonEmptyFileArray(
      data.copieIdentiteAuteurDocument,
      ctx,
      "copieIdentiteAuteurDocument",
      t("validation.copieIdentiteAuteurDocumentRequise"),
    );
  }
}

export function validateAchatNonRecuCybercrime(
  data: Record<string, any>,
  ctx: z.RefinementCtx,
  addCustomIssue: (c: z.RefinementCtx, path: string, message: string) => void,
  t: ComposerTranslation,
): void {
  if (data.typeCybercrime !== "achat-non-recu") {
    return;
  }

  validateAchatNonRecuDates(data, ctx, t);
  validateAchatNonRecuDescription(data, ctx, t);
  validateAchatNonRecuVendeur(data, ctx, t);
  validateAchatNonRecuPlateforme(data, ctx, t);
  validateAchatNonRecuDocuments(data, ctx, t);
  validateAchatNonRecuPaiement(data, ctx, addCustomIssue, t);
  validateAchatNonRecuIdentite(data, ctx, t);
}
