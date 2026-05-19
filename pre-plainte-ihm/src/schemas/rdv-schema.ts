import { z } from "zod";
import type { ComposerTranslation } from "vue-i18n";
import { fromIsoDate, parseDate, toDateOnly } from "@/utils/helpers/dateHelpers.ts";

export function rendezvousInfoSchema(t: ComposerTranslation, premiereDateDispo?: string, derniereDateDispo?: string) {
  return z
    .object({
      dateSouhaitee: z.string().optional(),
    })
    .superRefine((data, ctx) => {
      if (!data.dateSouhaitee) {
        return;
      }

      const date = parseDate(data.dateSouhaitee);
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
