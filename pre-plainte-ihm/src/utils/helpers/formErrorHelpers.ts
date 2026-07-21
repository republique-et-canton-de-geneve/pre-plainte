export type FormValidationErrorItem = {
  path: string;
  message: string;
};

const VOL_OBJET_BROUILLON_ERROR_ROOTS = new Set([
  "categorieObjet",
  "sousCategorie",
  "typeObjet",
  "fabricant",
  "fabricantAutre",
  "modele",
  "modeleAutre",
  "couleur",
  "couleurSecondaire",
  "gravure",
  "valeurReelle",
  "numeroSerie",
  "numeroSerieInconnu",
  "numeroIMEI",
  "numeroIMEIInconnu",
  "justificationAbsenceIMEI",
  "numeroCadre",
  "numeroCadreInconnu",
  "vin",
  "vinInconnu",
  "velofinderId",
  "dateAchat",
  "plaqueNumero",
  "plaqueInconnu",
  "plaquePays",
  "plaqueCanton",
  "assureurAutre",
  "numeroAssurance",
  "numeroVignette",
  "numeroMaster",
  "assuranceAucune",
]);

export function isVolObjetBrouillonErrorPath(path: string): boolean {
  const root = path.split(".")[0] ?? path;
  return VOL_OBJET_BROUILLON_ERROR_ROOTS.has(root);
}

export function excludeVolObjetBrouillonErrors(items: FormValidationErrorItem[]): FormValidationErrorItem[] {
  return items.filter(item => !isVolObjetBrouillonErrorPath(item.path));
}

export function collectValidationErrorItems(errors: unknown): FormValidationErrorItem[] {
  if (!errors || typeof errors !== "object") {
    return [];
  }

  const record = errors as Record<string, unknown>;
  const source =
    record.errors && typeof record.errors === "object" && !Array.isArray(record.errors)
      ? (record.errors as Record<string, unknown>)
      : record;

  const items: FormValidationErrorItem[] = [];

  for (const [key, value] of Object.entries(source)) {
    if (key === "values" || key === "results" || key === "evt") {
      continue;
    }
    collectItems(value, key, items);
  }

  const seen = new Set<string>();
  return items.filter(item => {
    const token = `${item.path}::${item.message}`;
    if (seen.has(token)) {
      return false;
    }
    seen.add(token);
    return true;
  });
}

export function flattenValidationErrorMessages(errors: unknown): string[] {
  return [...new Set(collectValidationErrorItems(errors).map(item => item.message))];
}

function collectItems(value: unknown, path: string, items: FormValidationErrorItem[]): void {
  if (typeof value === "string") {
    collectStringItem(value, path, items);
    return;
  }

  if (Array.isArray(value)) {
    collectArrayItems(value, path, items);
    return;
  }

  if (value && typeof value === "object") {
    collectObjectItems(value as Record<string, unknown>, path, items);
  }
}

function collectStringItem(value: string, path: string, items: FormValidationErrorItem[]): void {
  const message = value.trim();
  if (message) {
    items.push({ path, message });
  }
}

function collectArrayItems(value: unknown[], path: string, items: FormValidationErrorItem[]): void {
  for (const [index, item] of value.entries()) {
    if (typeof item === "string") {
      collectStringItem(item, path, items);
    } else {
      collectItems(item, `${path}.${index}`, items);
    }
  }
}

function collectObjectItems(value: Record<string, unknown>, path: string, items: FormValidationErrorItem[]): void {
  for (const [nestedKey, nested] of Object.entries(value)) {
    collectItems(nested, path ? `${path}.${nestedKey}` : nestedKey, items);
  }
}
