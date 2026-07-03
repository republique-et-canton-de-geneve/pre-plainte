import { describe, expect, it } from "vitest";
import { buildEsiriusPayload } from "@/utils/helpers/esiriusFormatBuilder";

const CONSTAT_EMAIL = "constat@example.org";

const creneau = {
  serviceId: "DOMMAGE-1",
  siteCode: "PPEL",
  beginDateTime: "20260704 10:00",
  endDateTime: "20260704 10:30",
  resource: {
    id: 12,
    key: "POSTE-DOMMAGE",
    type: "STATION",
  },
};

describe("format du payload eSirius", () => {
  it("rattache le rendez-vous a la demande via l'identite personnelle", () => {
    const payload = buildEsiriusPayload(
      "PPL-123",
      {
        nom: "Martin",
        prenom: "Anne",
        email: "anne.martin@example.org",
        telephone: "+41 79 123 45 67",
        dateNaissance: "15.04.1985",
      },
      creneau,
    );

    expect(payload.user.personalIdentity).toBe("PPL-123");
    expect(payload.user.fixPhone).toBe("");
    expect(payload.user.phone).toBe("41791234567");
  });

  it("construit un visiteur exploitable pour un parcours rendez-vous seul", () => {
    const payload = buildEsiriusPayload(
      "RDV-123",
      {
        email: CONSTAT_EMAIL,
      },
      creneau,
    );

    expect(payload.user).toMatchObject({
      lastName: CONSTAT_EMAIL,
      firstName: "",
      personalIdentity: "RDV-123",
      email: CONSTAT_EMAIL,
      fixPhone: "",
    });
  });
});
