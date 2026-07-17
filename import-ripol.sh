#!/usr/bin/env bash
# Transforme les 2 CSV RIPOL fournis en base SQLite dbppel3.
#
# Usage:
#   ./scripts/ripol/import-ripol.sh /chemin/vers/dossier/csv
#   ./scripts/ripol/import-ripol.sh /chemin/TBINCIDENTCODE.csv /chemin/TBLOCALIZATION_V1.csv
#
# Options via variables d'environnement:
#   DRY_RUN=1        génère dbppel3.new sans remplacer
#   NO_BACKUP=1      ne sauvegarde pas dbppel3.backup
#   SKIP_TESTS=1     ne lance pas les tests Maven

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
if [[ -d "${SCRIPT_DIR}/../../pre-plainte-rest" ]]; then
  REPO_ROOT="$(cd "${SCRIPT_DIR}/../.." && pwd)"
elif [[ -d "${SCRIPT_DIR}/pre-plainte-rest" ]]; then
  REPO_ROOT="${SCRIPT_DIR}"
else
  REPO_ROOT="$(pwd)"
fi
PYTHON_SCRIPT="${SCRIPT_DIR}/import_csv_to_sqlite.py"
if [[ ! -f "${PYTHON_SCRIPT}" && -f "${SCRIPT_DIR}/import-ripol.py" ]]; then
  PYTHON_SCRIPT="${SCRIPT_DIR}/import-ripol.py"
fi

if ! command -v python3 >/dev/null 2>&1 && ! command -v python >/dev/null 2>&1; then
  echo "Erreur: Python 3 requis." >&2
  exit 1
fi

PYTHON=python3
if ! command -v python3 >/dev/null 2>&1; then
  PYTHON=python
fi

if [[ ! -f "${PYTHON_SCRIPT}" ]]; then
  echo "Erreur: fichier introuvable : ${PYTHON_SCRIPT}" >&2
  exit 1
fi

if grep -q "strict=False" "${PYTHON_SCRIPT}"; then
  echo "Erreur: ${PYTHON_SCRIPT} contient encore zip(..., strict=False)." >&2
  echo "Ce paramètre n'est pas supporté par Python < 3.10." >&2
  exit 1
fi

ARGS=()
if [[ "${DRY_RUN:-0}" == "1" ]]; then
  ARGS+=(--dry-run)
fi
if [[ "${NO_BACKUP:-0}" == "1" ]]; then
  ARGS+=(--no-backup)
fi

if [[ $# -eq 1 ]]; then
  ARGS+=(--input-dir "$1")
elif [[ $# -eq 2 ]]; then
  ARGS+=(--incident-csv "$1" --localization-csv "$2")
else
  echo "Usage: $0 <dossier_csv>" >&2
  echo "   ou: $0 <TBINCIDENTCODE.csv> <TBLOCALIZATION.csv>" >&2
  exit 1
fi

echo "=== Import RIPOL CSV -> SQLite ==="
echo "Python : $($PYTHON --version 2>&1)"
echo "Script : ${PYTHON_SCRIPT}"
"${PYTHON}" "${PYTHON_SCRIPT}" "${ARGS[@]}"

if [[ "${DRY_RUN:-0}" == "1" ]] || [[ "${SKIP_TESTS:-0}" == "1" ]]; then
  exit 0
fi

if ! command -v mvn >/dev/null 2>&1; then
  echo "Maven non trouvé — tests ignorés."
  exit 0
fi

echo ""
echo "=== Tests régression RIPOL ==="
cd "${REPO_ROOT}"
mvn test -pl pre-plainte-infrastructure,pre-plainte-rest \
  -Dtest=SqliteRipolAdapterTest,RipolControllerTest -q

echo ""
echo "Terminé."
