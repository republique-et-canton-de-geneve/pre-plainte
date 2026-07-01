import { readdirSync, readFileSync, writeFileSync } from "node:fs";
import { dirname, join } from "node:path";
import { fileURLToPath } from "node:url";

const currentDirectory = dirname(fileURLToPath(import.meta.url));
const documentationDirectory = join(currentDirectory, "..");
const projectDirectory = join(documentationDirectory, "..");
const cypressFeaturesDirectory = join(projectDirectory, "pre-plainte-cypress", "cypress", "e2e");
const outputPath = join(documentationDirectory, "couverture-cypress.md");

const featureFiles = readdirSync(cypressFeaturesDirectory)
  .filter((fileName) => fileName.endsWith(".feature"))
  .sort((firstFileName, secondFileName) => firstFileName.localeCompare(secondFileName, "fr"));

const parseFeature = (fileName) => {
  const content = readFileSync(join(cypressFeaturesDirectory, fileName), "utf8");
  const lines = content.split(/\r?\n/);
  const feature = {
    fileName,
    title: "",
    rules: [],
    scenarios: [],
  };

  for (const rawLine of lines) {
    const line = rawLine.trim();

    if (!line || line.startsWith("#")) {
      continue;
    }

    if (line.startsWith("Fonctionnalité:")) {
      feature.title = line.replace("Fonctionnalité:", "").trim();
      continue;
    }

    if (line.startsWith("Règle:")) {
      feature.rules.push(line.replace("Règle:", "").trim());
      continue;
    }

    if (line.startsWith("Scénario:")) {
      feature.scenarios.push(line.replace("Scénario:", "").trim());
    }
  }

  return feature;
};

const escapeCell = (value) => value.replaceAll("|", "\\|");
const renderList = (values) => values.map(escapeCell).join("<br><br>");

const renderRules = (rules) => {
  if (rules.length === 0) {
    return "Aucune règle métier documentée dans le fichier `.feature`.";
  }

  return renderList(rules);
};

const renderDocument = (features) => {
  const scenarioCount = features.reduce((total, feature) => total + feature.scenarios.length, 0);

  return [
    "# Couverture Cypress des parcours frontend",
    "",
    "Ce document est généré depuis les scénarios Cypress écrits en Gherkin. Il synthétise les parcours frontend réellement exécutés de bout en bout.",
    "",
    `Scénarios Cypress documentés : ${scenarioCount}.`,
    "",
    ...features.map(renderFeature),
    "",
  ].join("\n");
};

const renderFeature = (feature) => {
  const scenarios = renderList(feature.scenarios);

  return [
    `## ${feature.title}`,
    "",
    "| Parcours testé | Couverture fonctionnelle testée | Exemples testés | Source |",
    "| --- | --- | --- | --- |",
    `| ${scenarios} | ${describeCoverage(feature)} | ${renderRules(feature.rules)} | \`${feature.fileName}\` |`,
    "",
  ].join("\n");
};

const describeCoverage = (feature) => {
  const descriptions = {
    "Accès au formulaire": "Conditions d'accès au formulaire et passage vers la vérification de l'adresse e-mail.",
    "Informations personnelles": "Affichages conditionnels et validations bloquantes de l'étape informations personnelles.",
    "Informations sur l'événement - cybercrime": "Champs et validations spécifiques à un achat non reçu.",
    "Informations sur l'événement - dommage": "Champs et validations spécifiques à un dommage avec constat présent.",
    "Informations sur l'événement - vol": "Validations bloquantes et affichages conditionnels liés à un vol.",
    "Parcours nominal complet": "Soumission de la pré-plainte et gestion des erreurs d'intégration au récapitulatif.",
    "Rendez-vous": "Sélection obligatoire d'un créneau et filtrage des services de rendez-vous.",
    "Reprise de brouillon": "Restauration du parcours citoyen depuis un identifiant de brouillon.",
    "Vol d'objet - véhicule": "Validations propres à l'ajout d'un véhicule volé.",
  };

  return descriptions[feature.title] ?? "Parcours citoyen automatisé de bout en bout.";
};

const features = featureFiles.map(parseFeature);
writeFileSync(outputPath, renderDocument(features), "utf8");
