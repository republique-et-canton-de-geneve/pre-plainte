import { describe, expect, it } from "vitest";
import {
  filterCompatibleCreneaux,
  filterServicesByIncident,
  findClosestAvailableDate,
  formatCreneauLieu,
  getRendezVousWarning,
  isSameSelectedDate,
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

describe("formatCreneauLieu", () => {
  const ressourceRdvSeule = "RDV";
  const ressourceRdvAvecPoste = "RDV - Poste de police de Carouge";
  const posteCarouge = "Poste de police de Carouge";
  const serviceCornavin = "Pré-plainte pour vol - Cornavin";

  it("affiche le poste apres le prefixe RDV", () => {
    expect(formatCreneauLieu(ressourceRdvAvecPoste)).toBe(posteCarouge);
  });

  it("utilise le serviceName si la ressource vaut seulement RDV", () => {
    expect(formatCreneauLieu(ressourceRdvSeule, serviceCornavin)).toBe(serviceCornavin);
  });

  it("conserve le premier segment si ce n'est pas RDV", () => {
    expect(formatCreneauLieu("Poste 1 - Salle A")).toBe("Poste");
  });

  it("retire l'index technique de la ressource eSirius", () => {
    expect(formatCreneauLieu("Plainpalais 1")).toBe("Plainpalais");
    expect(formatCreneauLieu("Pâquis 2")).toBe("Pâquis");
    expect(formatCreneauLieu("Servette 1")).toBe("Servette");
  });
});

describe("findClosestAvailableDate", () => {
  const dateSelectionnee = "2026-07-15";
  const dateProcheAvant = "2026-07-10";
  const dateProcheApres = "2026-07-20";

  it("conserve la date si elle reste disponible", () => {
    expect(findClosestAvailableDate(dateSelectionnee, [dateProcheAvant, dateSelectionnee, dateProcheApres])).toBe(
      dateSelectionnee,
    );
  });

  it("choisit la date la plus proche si la selection n'est plus disponible", () => {
    expect(findClosestAvailableDate(dateSelectionnee, [dateProcheAvant, dateProcheApres])).toBe(dateProcheAvant);
  });
});

describe("isSameSelectedDate", () => {
  const dateIso = "2026-07-15";

  it("filtre correctement une date ISO", () => {
    expect(isSameSelectedDate("20260715 10:00", dateIso)).toBe(true);
    expect(isSameSelectedDate("20260716 10:00", dateIso)).toBe(false);
  });
});
