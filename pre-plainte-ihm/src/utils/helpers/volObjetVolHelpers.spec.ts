import { describe, expect, it } from "vitest";
import {
  hasVehiculeVoleAvecPlaque,
  validerPlaqueVehicule,
} from "@/utils/helpers/volObjetVolHelpers";
import {
  donneesPlaqueValides,
  reglesVolHelpers,
} from "@/test/business-rules/vol-helper.rules";

const t = (key: string) => key;

describe("regles metier des helpers vol et plaque", () => {
  reglesVolHelpers.forEach(regle => {
    regle.examples?.forEach(example => {
      it(`${regle.champDemande} - ${example.label}`, () => {
        const data = {
          ...donneesPlaqueValides,
          ...example.data,
        };

        if (regle.champDemande === "Plaque de vehicule") {
          const errors: Record<string, string> = {};
          const result = validerPlaqueVehicule(
            data,
            (field, message) => {
              errors[field] = message;
            },
            t,
          );

          expect(result).toBe(example.valid);

          if (!example.valid) {
            expect(errors[example.errorPath?.[0] ?? ""]).toBe(example.errorMessage);
          }
          return;
        }

        expect(hasVehiculeVoleAvecPlaque({
          ...data,
          objetsVolesValides: data.objetsVolesValides as any[] | undefined,
        } as any)).toBe(example.valid);
      });
    });
  });
});
