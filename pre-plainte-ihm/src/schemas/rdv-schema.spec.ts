import type { ComposerTranslation } from "vue-i18n";
import { rendezvousInfoSchema } from "@/schemas/rdv-schema";
import {
  derniereDateRendezVousDisponible,
  donneesRendezVousValides,
  premiereDateRendezVousDisponible,
  reglesRendezVous,
} from "@/test/business-rules/rendez-vous.rules";
import { describeBusinessRuleSchema } from "@/test/business-rules/business-rule-spec.helpers";

const t = ((key: string) => key) as ComposerTranslation;

describeBusinessRuleSchema(
  "regles metier du rendez-vous",
  rendezvousInfoSchema(t, premiereDateRendezVousDisponible, derniereDateRendezVousDisponible),
  reglesRendezVous,
  donneesRendezVousValides,
);
