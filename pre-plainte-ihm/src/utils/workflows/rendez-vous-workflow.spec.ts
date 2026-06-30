import { describe, expect, it } from "vitest";
import {
  filterCompatibleCreneaux,
  filterServicesByIncident,
  getRendezVousWarning,
} from "@/utils/workflows/rendez-vous-workflow";
import {
  availabilitiesRendezVousWorkflow,
  donneesRendezVousWorkflowValides,
  nowRendezVousWorkflow,
  reglesRendezVousWorkflow,
  servicesRendezVousWorkflow,
} from "@/test/business-rules/rendez-vous-workflow.rules";

describe("regles metier du workflow rendez-vous", () => {
  reglesRendezVousWorkflow.forEach(regle => {
    regle.examples?.forEach(example => {
      it(`${regle.champDemande} - ${example.label}`, () => {
        const data = {
          ...donneesRendezVousWorkflowValides,
          ...example.data,
          objetsVolesValides: [],
        };

        if (regle.champDemande === "Service propose") {
          const services = filterServicesByIncident(
            servicesRendezVousWorkflow,
            availabilitiesRendezVousWorkflow,
            data.typeIncident,
          );
          expect(services.map(service => service.key)).toEqual([example.errorPath?.[0]]);
          return;
        }

        if (regle.champDemande === "Creneaux compatibles") {
          const creneaux = filterCompatibleCreneaux(
            availabilitiesRendezVousWorkflow,
            servicesRendezVousWorkflow,
            data as any,
            nowRendezVousWorkflow,
          );
          expect(String(creneaux.length)).toBe(example.errorPath?.[0]);
          return;
        }

        const warning = getRendezVousWarning(data as any, false);
        expect(warning?.messageKey).toBe(example.errorMessage);
      });
    });
  });
});
