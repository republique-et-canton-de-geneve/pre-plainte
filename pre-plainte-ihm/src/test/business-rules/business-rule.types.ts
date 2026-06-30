export type Obligation = "Oui" | "Non" | "Selon le cas";

export interface BusinessRuleExample<TData extends Record<string, unknown> = Record<string, unknown>> {
  label: string;
  data: Partial<TData>;
  valid: boolean;
  errorPath?: string[];
  errorMessage?: string;
}

export interface BusinessRule<TData extends Record<string, unknown> = Record<string, unknown>> {
  section: string;
  champDemande: string;
  obligatoire: Obligation;
  precision: string;
  examples?: BusinessRuleExample<TData>[];
  invalidData?: Partial<TData>;
  errorPath?: string[];
  errorMessage?: string;
}
