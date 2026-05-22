import { z } from "zod";
import type { ComposerTranslation } from "vue-i18n";
import { fromIsoDate, parseDate, toDateOnly } from "@/utils/helpers/dateHelpers.ts";

const REGEX_DATE_ISO = /^(\d{4})-(\d{2})-(\d{2})$/;

export function rendezvousInfoSchema(t: ComposerTranslation, premiereDateDispo?: string, derniereDateDispo?: string) {
  return z
    .object({
      dateSouhaitee: z.string().optional(),
    })
    .superRefine((data, ctx) => {
      if (!data.dateSouhaitee) {
        return;
      }

      const date = parseRendezVousDate(data.dateSouhaitee);
      if (!date) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: t("validation.formatDateInvalide"),
          path: ["dateSouhaitee"],
        });
        return;
      }

      const dateOnly = toDateOnly(date);

      if (!premiereDateDispo || !derniereDateDispo) {
        return;
      }

      const premiere = toDateOnly(new Date(premiereDateDispo));
      const derniere = toDateOnly(new Date(derniereDateDispo));

      if (dateOnly < premiere) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: t("validation.dateAvantPremiereDispo", { date: fromIsoDate(premiereDateDispo) }),
          path: ["dateSouhaitee"],
        });
      }

      if (dateOnly > derniere) {
        ctx.addIssue({
          code: z.ZodIssueCode.custom,
          message: t("validation.dateApresDerniereDispo", { date: fromIsoDate(derniereDateDispo) }),
          path: ["dateSouhaitee"],
        });
      }
    });
}

function parseRendezVousDate(value: string): Date | null {
  const dateSaisie = parseDate(value);
  if (dateSaisie) {
    return dateSaisie;
  }

  const match = REGEX_DATE_ISO.exec(value);
  if (!match) {
    return null;
  }

  const [, yearValue, monthValue, dayValue] = match;
  const year = Number(yearValue);
  const month = Number(monthValue);
  const day = Number(dayValue);
  const parsed = new Date(year, month - 1, day);

  if (
    parsed.getFullYear() !== year ||
    parsed.getMonth() !== month - 1 ||
    parsed.getDate() !== day
  ) {
    return null;
  }

  return parsed;
}
