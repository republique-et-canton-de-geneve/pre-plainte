import type { ComposerTranslation } from "vue-i18n";
import { createInfosPersonnellesSchema } from "@/schemas/infos-personnelles.schema";
import {
  donneesInformationsPersonnellesValides,
  reglesInformationsPersonnelles,
} from "@/test/business-rules/informations-personnelles.rules";
import { describeBusinessRuleSchema } from "@/test/business-rules/business-rule-spec.helpers";

const t = ((key: string) => key) as ComposerTranslation;

describeBusinessRuleSchema(
  "regles metier des informations personnelles",
  createInfosPersonnellesSchema(t),
  reglesInformationsPersonnelles,
  donneesInformationsPersonnellesValides,
);
