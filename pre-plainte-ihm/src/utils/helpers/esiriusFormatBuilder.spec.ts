import { describe, expect, it } from "vitest";
import { buildEsiriusPayload, resolveEsiriusDemandeReference } from "@/utils/helpers/esiriusFormatBuilder";

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
  it("envoie le numero AEL en minuscules dans fixPhone et laisse personalIdentity vide", () => {
    const payload = buildEsiriusPayload(
      "AEL-PPL-C-7YT77HJZBU",
      {
        nom: "Martin",
        prenom: "Anne",
        email: "anne.martin@example.org",
        telephone: "+41 79 123 45 67",
        dateNaissance: "15.04.1985",
        adresse: "Rue du Test 1",
        npa: "1200",
        localite: "Genève",
        pays: "CH",
      },
      creneau,
    );

    expect(payload.user.personalIdentity).toBeNull();
    expect(payload.user.fixPhone).toBe("ael-ppl-c-7yt77hjzbu");
    expect(payload.user.phone).toBe("+41791234567");
    expect(payload.user.lastName).toBe("Martin");
    expect(payload.user.firstName).toBe("Anne");
    expect(payload.user.address).toEqual({
      line1: "Rue du Test 1",
      line2: "",
      zipCode: "1200",
      city: "Genève",
      country: "suisse",
    });
  });

  it("construit un visiteur exploitable pour un parcours rendez-vous seul", () => {
    const payload = buildEsiriusPayload(
      "AEL-PPL-D-1WO30IY44H",
      {
        nom: "Dupont",
        prenom: "Jean",
        email: CONSTAT_EMAIL,
        telephone: "+41 79 123 45 67",
        pays: "8100",
      },
      creneau,
    );

    expect(payload.user).toMatchObject({
      lastName: "Dupont",
      firstName: "Jean",
      personalIdentity: null,
      email: CONSTAT_EMAIL,
      fixPhone: "ael-ppl-d-1wo30iy44h",
      phone: "+41791234567",
      address: {
        line1: "",
        line2: "",
        zipCode: "",
        city: "",
        country: "",
      },
    });
  });

  it("resout la reference AEL depuis fixPhone si personalIdentity est vide", () => {
    expect(
      resolveEsiriusDemandeReference({
        personalIdentity: null,
        fixPhone: "ael-ppl-v-ezfr5a7xvm",
      }),
    ).toBe("ael-ppl-v-ezfr5a7xvm");
  });
});
