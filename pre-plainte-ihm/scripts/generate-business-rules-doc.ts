import { existsSync, readFileSync, writeFileSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";
import { reglesDisclaimerWorkflow } from "../src/test/business-rules/disclaimer-workflow.rules";
import { reglesEmailVerificationWorkflow } from "../src/test/business-rules/email-verification-workflow.rules";
import { reglesEvenement } from "../src/test/business-rules/evenement.rules";
import { reglesInformationsPersonnelles } from "../src/test/business-rules/informations-personnelles.rules";
import { reglesRendezVous } from "../src/test/business-rules/rendez-vous.rules";
import { reglesRendezVousWorkflow } from "../src/test/business-rules/rendez-vous-workflow.rules";
import { reglesVerificationEmail } from "../src/test/business-rules/verification-email.rules";
import { reglesVolHelpers } from "../src/test/business-rules/vol-helper.rules";
import type { BusinessRule } from "../src/test/business-rules/business-rule.types";

const currentDir = dirname(fileURLToPath(import.meta.url));
const outputPath = resolve(currentDir, "../../pre-plainte-doc/regles-metier-formulaire.md");
const checkMode = process.argv.includes("--check");

function escapeCell(value: string): string {
  return value.replaceAll("|", "\\|");
}

function groupBySection(rules: BusinessRule[]): Map<string, BusinessRule[]> {
  return rules.reduce((acc, rule) => {
    const sectionRules = acc.get(rule.section) ?? [];
    sectionRules.push(rule);
    acc.set(rule.section, sectionRules);
    return acc;
  }, new Map<string, BusinessRule[]>());
}

function renderSection(section: string, rules: BusinessRule[]): string {
  const rows = rules.map(rule => {
    const examples = rule.examples?.map(example => example.label).join("<br>") ?? "";
    const kind = rule.kind ?? "schema";

    return `| ${escapeCell(rule.champDemande)} | ${kind} | ${rule.obligatoire} | ${escapeCell(rule.precision)} | ${escapeCell(examples)} |`;
  });

  return [
    `## ${section}`,
    "",
    "| Champ demande | Type de regle | Obligatoire | Precision | Exemples testes |",
    "| --- | --- | --- | --- | --- |",
    ...rows,
  ].join("\n");
}

function renderDocument(): string {
  const rules = [
    ...reglesInformationsPersonnelles,
    ...reglesEvenement,
    ...reglesDisclaimerWorkflow,
    ...reglesVerificationEmail,
    ...reglesEmailVerificationWorkflow,
    ...reglesRendezVous,
    ...reglesRendezVousWorkflow,
    ...reglesVolHelpers,
  ];
  const sections = [...groupBySection(rules).entries()]
    .map(([section, rules]) => renderSection(section, rules));

  return [
    "# Regles metier du formulaire",
    "",
    "Ce document est genere depuis les tests unitaires frontend.",
    "",
    sections.join("\n\n"),
    "",
  ].join("\n");
}

const content = renderDocument();

if (checkMode) {
  const existingContent = existsSync(outputPath) ? readFileSync(outputPath, "utf8") : "";
  if (existingContent !== content) {
    console.error(`La documentation generee n'est pas a jour: ${outputPath}`);
    process.exit(1);
  }
  process.exit(0);
}

writeFileSync(outputPath, content, "utf8");
