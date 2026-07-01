import type { ComposerTranslation } from "vue-i18n";
import { createIncidentSchema } from "@/schemas/incident-evenement.schema";
import { donneesEvenementValides, reglesEvenement } from "@/test/business-rules/evenement.rules";
import { describeBusinessRuleSchema } from "@/test/business-rules/business-rule-spec.helpers";

const t = ((key: string) => key) as ComposerTranslation;

describeBusinessRuleSchema(
  "regles metier des informations sur l'evenement",
  createIncidentSchema(t, "CH"),
  reglesEvenement,
  donneesEvenementValides,
);
