import { describe, expect, it } from "vitest";
import {
  collectValidationErrorItems,
  excludeVolObjetBrouillonErrors,
  flattenValidationErrorMessages,
  isVolObjetBrouillonErrorPath,
} from "@/utils/helpers/formErrorHelpers";

const MESSAGE_CHAMP_REQUIS = "Champ requis";
const INVALID_NUMERIC_INPUT = Number.NaN;

describe("formErrorHelpers", () => {
  it("retourne une liste vide pour des entrees non exploitables", () => {
    expect(collectValidationErrorItems(null)).toEqual([]);
    expect(collectValidationErrorItems(undefined)).toEqual([]);
    expect(collectValidationErrorItems("erreur")).toEqual([]);
    expect(collectValidationErrorItems(INVALID_NUMERIC_INPUT)).toEqual([]);
  });

  it("collecte les erreurs d'un enregistrement plat", () => {
    expect(
      collectValidationErrorItems({
        email: "L'adresse e-mail est requise",
        nom: "Le nom est requis",
      }),
    ).toEqual([
      { path: "email", message: "L'adresse e-mail est requise" },
      { path: "nom", message: "Le nom est requis" },
    ]);
  });

  it("utilise l'enveloppe errors et ignore values/results/evt", () => {
    expect(
      collectValidationErrorItems({
        errors: {
          typeLieu: "Le type de lieu est requis",
        },
        values: { typeLieu: null },
        results: {},
        evt: {},
      }),
    ).toEqual([{ path: "typeLieu", message: "Le type de lieu est requis" }]);
  });

  it("aplatit les erreurs imbriquees et les tableaux", () => {
    expect(
      collectValidationErrorItems({
        adresse: {
          npa: "Le NPA est requis",
        },
        objets: ["Premier objet invalide", { plaqueNumero: "La plaque est requise" }],
      }),
    ).toEqual([
      { path: "adresse.npa", message: "Le NPA est requis" },
      { path: "objets", message: "Premier objet invalide" },
      { path: "objets.1.plaqueNumero", message: "La plaque est requise" },
    ]);
  });

  it("deduplique les paires path/message", () => {
    expect(
      collectValidationErrorItems({
        email: [MESSAGE_CHAMP_REQUIS, MESSAGE_CHAMP_REQUIS],
      }),
    ).toEqual([{ path: "email", message: MESSAGE_CHAMP_REQUIS }]);
  });

  it("identifie les chemins d'erreurs du brouillon objet vole", () => {
    expect(isVolObjetBrouillonErrorPath("categorieObjet")).toBe(true);
    expect(isVolObjetBrouillonErrorPath("plaqueNumero")).toBe(true);
    expect(isVolObjetBrouillonErrorPath("objetsVolesValides")).toBe(false);
    expect(isVolObjetBrouillonErrorPath("objetsVolesValides.0.typeObjet")).toBe(false);
    expect(isVolObjetBrouillonErrorPath("dateDebutEvenement")).toBe(false);
    expect(isVolObjetBrouillonErrorPath("typeLieu")).toBe(false);
  });

  it("exclut les erreurs du brouillon objet vole du resume", () => {
    expect(
      excludeVolObjetBrouillonErrors([
        { path: "categorieObjet", message: "La categorie est requise" },
        { path: "objetsVolesValides", message: "Veuillez ajouter au moins un objet" },
        { path: "dateDebutEvenement", message: "La date est requise" },
        { path: "volDansVehicule", message: "Indiquez si l'objet etait dans un vehicule" },
      ]),
    ).toEqual([
      { path: "objetsVolesValides", message: "Veuillez ajouter au moins un objet" },
      { path: "dateDebutEvenement", message: "La date est requise" },
      { path: "volDansVehicule", message: "Indiquez si l'objet etait dans un vehicule" },
    ]);
  });

  it("aplatit les messages uniques", () => {
    expect(
      flattenValidationErrorMessages({
        email: MESSAGE_CHAMP_REQUIS,
        nom: MESSAGE_CHAMP_REQUIS,
        prenom: "Le prenom est requis",
      }),
    ).toEqual([MESSAGE_CHAMP_REQUIS, "Le prenom est requis"]);
  });
});
