import fs from "node:fs";
import path from "node:path";
import process from "node:process";

const featureDir = path.resolve("cypress/e2e");
const errors = [];

const frenchStep = /^\s*(Etant donné|Étant donné|Quand|Alors|Et|Mais)\b/;
const frenchScenario = /^\s*(Scénario|Plan du scénario)\s*:/;
const englishKeyword = /^\s*(Feature|Scenario|Scenario Outline|Background|Context|Given|When|Then|And|But)\s*[: ]/;

function featureFiles(dir) {
  return fs.readdirSync(dir, { withFileTypes: true }).flatMap(entry => {
    const fullPath = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      return featureFiles(fullPath);
    }
    return entry.isFile() && entry.name.endsWith(".feature") ? [fullPath] : [];
  });
}

for (const file of featureFiles(featureDir)) {
  const content = fs.readFileSync(file, "utf8");
  const lines = content.split(/\r?\n/);
  const relative = path.relative(process.cwd(), file);
  const firstContentLine = lines.find(line => line.trim().length > 0);

  if (firstContentLine !== "# language: fr") {
    errors.push(`${relative}: la première ligne non vide doit être "# language: fr".`);
  }

  let currentScenario = null;
  let currentScenarioHasStep = false;

  lines.forEach((line, index) => {
    const lineNumber = index + 1;

    if (englishKeyword.test(line)) {
      errors.push(`${relative}:${lineNumber}: mot-clé Gherkin anglais interdit dans un fichier en français: "${line.trim()}".`);
    }

    if (frenchScenario.test(line)) {
      if (currentScenario && !currentScenarioHasStep) {
        errors.push(`${relative}:${currentScenario.line}: scénario sans step exécutable: "${currentScenario.name}".`);
      }
      currentScenario = { line: lineNumber, name: line.trim() };
      currentScenarioHasStep = false;
      return;
    }

    if (currentScenario && frenchStep.test(line)) {
      currentScenarioHasStep = true;
    }
  });

  if (currentScenario && !currentScenarioHasStep) {
    errors.push(`${relative}:${currentScenario.line}: scénario sans step exécutable: "${currentScenario.name}".`);
  }
}

if (errors.length > 0) {
  console.error(errors.join("\n"));
  process.exit(1);
}

console.log("Validation des fichiers .feature OK");
