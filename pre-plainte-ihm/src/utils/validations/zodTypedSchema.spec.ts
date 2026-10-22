import { describe, expect, it } from "vitest";
import { z } from "zod";
import { toTypedSchema } from "./zodTypedSchema";

describe("adaptation des schémas Zod pour vee-validate", () => {
  const schema = toTypedSchema(z.object({
    email: z.string().email(),
    personnes: z.array(z.object({ nom: z.string().min(2) })),
    telephone: z.string().optional(),
  }));

  it("associe les erreurs aux champs imbriqués", async () => {
    const result = await schema.parse({ email: "invalide", personnes: [{ nom: "A" }] });

    expect(result.errors).toEqual([
      { path: "email", errors: [expect.any(String)] },
      { path: "personnes[0].nom", errors: [expect.any(String)] },
    ]);
  });

  it("décrit les champs obligatoires et facultatifs", () => {
    expect(schema.describe?.("personnes[0].nom")).toEqual({ required: true, exists: true });
    expect(schema.describe?.("telephone")).toEqual({ required: false, exists: true });
    expect(schema.describe?.("absent")).toEqual({ required: false, exists: false });
  });

  it("applique les transformations lors de l'initialisation du formulaire", () => {
    const transformed = toTypedSchema(z.object({ nom: z.string().trim() }));

    expect(transformed.cast?.({ nom: "  Martin  " })).toEqual({ nom: "Martin" });
  });
});
