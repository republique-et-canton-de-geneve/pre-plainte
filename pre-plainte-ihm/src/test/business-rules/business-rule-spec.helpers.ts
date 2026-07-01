import { describe, expect, it } from "vitest";
import type { SafeParseReturnType } from "zod";
import type { BusinessRule, BusinessRuleExample } from "@/test/business-rules/business-rule.types";

type SchemaParser<TData extends Record<string, unknown>> = {
  safeParse(data: TData): SafeParseReturnType<unknown, unknown>;
};

function examplesForRule<TData extends Record<string, unknown>>(rule: BusinessRule<TData>): BusinessRuleExample<TData>[] {
  return rule.examples ?? [
    {
      label: rule.precision,
      data: rule.invalidData,
      valid: false,
      errorPath: rule.errorPath,
      errorMessage: rule.errorMessage,
    },
  ];
}

export function describeBusinessRuleSchema<TData extends Record<string, unknown>>(
  title: string,
  schema: SchemaParser<TData>,
  rules: BusinessRule<TData>[],
  validData: TData,
) {
  describe(title, () => {
    for (const rule of rules) {
      for (const example of examplesForRule(rule)) {
        if (!example.data) {
          continue;
        }

        it(`${rule.champDemande} - ${example.label}`, () => {
          const result = schema.safeParse({
            ...validData,
            ...example.data,
          });

          expect(result.success).toBe(example.valid);

          if (!example.valid && !result.success) {
            expect(result.error.issues).toEqual(
              expect.arrayContaining([
                expect.objectContaining({
                  path: example.errorPath,
                  message: example.errorMessage,
                }),
              ]),
            );
          }
        });
      }
    }
  });
}
