import { z } from "zod";
import type { TypedSchema } from "vee-validate";

function schemaForPath(schema: z.ZodType, path: string): z.ZodType | undefined {
  let current: z.ZodType | undefined = schema;

  for (const segment of path.replace(/\[(\d+)\]/g, ".$1").split(".").filter(Boolean)) {
    while (current instanceof z.ZodOptional || current instanceof z.ZodNullable || current instanceof z.ZodDefault) {
      current = current.unwrap();
    }

    if (current instanceof z.ZodObject) {
      current = current.shape[segment];
    } else if (current instanceof z.ZodArray && /^\d+$/.test(segment)) {
      current = current.element;
    } else {
      return undefined;
    }

    if (!current) {
      return undefined;
    }
  }

  return current;
}

export function toTypedSchema<TSchema extends z.ZodType>(schema: TSchema): TypedSchema<z.input<TSchema>, z.output<TSchema>> {
  return {
    __type: "VVTypedSchema",
    async parse(values) {
      const result = await schema.safeParseAsync(values);
      if (result.success) {
        return { value: result.data, errors: [] };
      }

      const errors = new Map<string, string[]>();
      for (const issue of result.error.issues) {
        const path = issue.path.reduce<string>((name, segment) =>
          typeof segment === "number" ? `${name}[${segment}]` : name ? `${name}.${segment}` : String(segment), "");
        const messages = errors.get(path) ?? [];
        messages.push(issue.message);
        errors.set(path, messages);
      }

      return { errors: Array.from(errors, ([path, messages]) => ({ path, errors: messages })) };
    },
    cast(values) {
      const result = schema.safeParse(values);
      return (result.success ? result.data : values) as z.input<TSchema>;
    },
    describe(path) {
      const field = path ? schemaForPath(schema, path) : schema;
      return { required: field ? !field.isOptional() : false, exists: !!field };
    },
  };
}
