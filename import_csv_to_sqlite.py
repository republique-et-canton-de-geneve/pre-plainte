#!/usr/bin/env python3
"""
Importe les exports CSV RIPOL (TBINCIDENTCODE + TBLOCALIZATION) dans dbppel3.

Les CSV proviennent typiquement d'un export SQL*Plus Oracle (séparateur ';', en-têtes
entre guillemets, lignes « SQL> … » en préambule).
"""

from __future__ import annotations

import argparse
import csv
import shutil
import sqlite3
import sys
import time
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
DEFAULT_DB_DIR = REPO_ROOT / "pre-plainte-rest" / "src" / "main" / "resources" / "bdd"
DEFAULT_DB_PATH = DEFAULT_DB_DIR / "dbppel3"
BACKUP_SUFFIX = ".backup"
NEW_SUFFIX = ".new"

INCIDENT_DEFAULT = "TBINCIDENTCODE.csv"
LOCALIZATION_DEFAULTS = ("TBLOCALIZATION_V1.csv", "TBLOCALIZATION.csv")

LOCALIZATION_EXTRA_COLUMNS = (
    "CID",
    "VERSIONNR",
    "CREATE_DT",
    "CREATE_USER",
    "UPDATE_DT",
    "UPDATE_USER",
    "TRANSLATIONLOB",
    "LOCALIZED_BY_USER",
)

BATCH_SIZE = 5000
SCRIPT_VERSION = "2026-07-17-no-zip-strict"


def log(message: str) -> None:
    print(message, flush=True)


def resolve_csv_paths(
    input_dir: Path | None,
    incident_csv: Path | None,
    localization_csv: Path | None,
) -> tuple[Path, Path]:
    if incident_csv and localization_csv:
        return incident_csv.resolve(), localization_csv.resolve()

    if input_dir is None:
        raise ValueError("Fournir --input-dir ou les deux fichiers CSV.")

    input_dir = input_dir.resolve()
    incident_path = input_dir / INCIDENT_DEFAULT
    if not incident_path.is_file():
        raise FileNotFoundError(f"Fichier introuvable : {incident_path}")

    localization_path = None
    for name in LOCALIZATION_DEFAULTS:
        candidate = input_dir / name
        if candidate.is_file():
            localization_path = candidate
            break
    if localization_path is None:
        raise FileNotFoundError(
            f"Aucun fichier de localisation trouvé dans {input_dir} "
            f"({', '.join(LOCALIZATION_DEFAULTS)})"
        )

    return incident_path, localization_path


def read_oracle_csv(path: Path) -> tuple[list[str], list[dict[str, str | None]]]:
    raw_lines = path.read_bytes()
    text = None
    for encoding in ("utf-8-sig", "utf-8", "cp1252", "latin-1"):
        try:
            text = raw_lines.decode(encoding)
            break
        except UnicodeDecodeError:
            continue
    if text is None:
        text = raw_lines.decode("utf-8", errors="replace")

    header: list[str] | None = None
    rows: list[dict[str, str | None]] = []

    for line in text.splitlines():
        stripped = line.strip()
        if not stripped or stripped.startswith("SQL>"):
            continue
        if header is None:
            if not stripped.upper().startswith('"ID"') and not stripped.upper().startswith("ID"):
                continue
            reader = csv.reader([stripped], delimiter=";", quotechar='"')
            header = [col.strip().strip('"').upper() for col in next(reader)]
            continue

        reader = csv.reader([stripped], delimiter=";", quotechar='"')
        values = next(reader)
        if len(values) < len(header):
            values.extend([""] * (len(header) - len(values)))
        elif len(values) > len(header):
            values = values[: len(header)]

        row: dict[str, str | None] = {}
        for index in range(len(header)):
            row[header[index]] = normalize_cell(values[index])
        rows.append(row)

    if header is None:
        raise ValueError(f"Aucun en-tête CSV détecté dans {path}")

    return header, rows


def normalize_cell(value: str | None) -> str | None:
    if value is None:
        return None
    cleaned = value.strip()
    if cleaned == "":
        return None
    return cleaned


def sqlite_type_for_column(table: str, column: str) -> str:
    return "TEXT"


def create_table(conn: sqlite3.Connection, table: str, columns: list[str]) -> None:
    column_defs = ", ".join(
        f'"{column}" {sqlite_type_for_column(table, column)}'
        for column in columns
    )
    conn.execute(f'CREATE TABLE "{table}" ({column_defs})')


def insert_rows(
    conn: sqlite3.Connection,
    table: str,
    columns: list[str],
    rows: list[dict[str, str | None]],
) -> int:
    if not rows:
        return 0

    placeholders = ", ".join("?" for _ in columns)
    quoted_columns = ", ".join(f'"{column}"' for column in columns)
    sql = f'INSERT INTO "{table}" ({quoted_columns}) VALUES ({placeholders})'

    inserted = 0
    batch: list[tuple[str | None, ...]] = []
    for row in rows:
        batch.append(tuple(row.get(column) for column in columns))
        if len(batch) >= BATCH_SIZE:
            conn.executemany(sql, batch)
            inserted += len(batch)
            batch.clear()
    if batch:
        conn.executemany(sql, batch)
        inserted += len(batch)
    return inserted


def create_indexes(conn: sqlite3.Connection) -> None:
    conn.execute(
        "CREATE INDEX IF NOT EXISTS idx_tbin_grouptype_codevalue "
        "ON TBINCIDENTCODE(GROUPTYPE, CODEVALUE)"
    )
    conn.execute(
        "CREATE INDEX IF NOT EXISTS idx_tbin_master "
        "ON TBINCIDENTCODE(MASTERTYPE, MASTERVALUE)"
    )
    conn.execute(
        "CREATE INDEX IF NOT EXISTS idx_tbin_grouptype_usable "
        "ON TBINCIDENTCODE(GROUPTYPE) "
        "WHERE CAST(ACTIVE AS INTEGER) = 1 AND CAST(SELECTABLE AS INTEGER) = 1"
    )
    conn.execute(
        "CREATE INDEX IF NOT EXISTS idx_tbloc_pk_locale "
        "ON TBLOCALIZATION(PK, LOCALE_ID)"
    )


def build_localization_columns(csv_header: list[str]) -> list[str]:
    columns = [col.upper() for col in csv_header]
    for extra in LOCALIZATION_EXTRA_COLUMNS:
        if extra not in columns:
            columns.append(extra)
    return columns


def expand_localization_rows(
    rows: list[dict[str, str | None]],
    columns: list[str],
) -> list[dict[str, str | None]]:
    expanded: list[dict[str, str | None]] = []
    for row in rows:
        normalized = {column: None for column in columns}
        for key, value in row.items():
            normalized[key.upper()] = value
        expanded.append(normalized)
    return expanded


def import_database(
    incident_csv: Path,
    localization_csv: Path,
    output_path: Path,
) -> None:
    log(f"Lecture {incident_csv.name} …")
    incident_header, incident_rows = read_oracle_csv(incident_csv)
    log(f"  -> {len(incident_rows):,} lignes, {len(incident_header)} colonnes")

    log(f"Lecture {localization_csv.name} …")
    localization_header, localization_rows = read_oracle_csv(localization_csv)
    localization_columns = build_localization_columns(localization_header)
    localization_rows = expand_localization_rows(localization_rows, localization_columns)
    log(f"  -> {len(localization_rows):,} lignes, {len(localization_columns)} colonnes")

    if output_path.exists():
        output_path.unlink()

    output_path.parent.mkdir(parents=True, exist_ok=True)

    started = time.time()
    conn = sqlite3.connect(output_path)
    try:
        conn.execute("PRAGMA journal_mode = OFF")
        conn.execute("PRAGMA synchronous = OFF")
        conn.execute("BEGIN")

        create_table(conn, "TBINCIDENTCODE", incident_header)
        create_table(conn, "TBLOCALIZATION", localization_columns)

        incident_count = insert_rows(conn, "TBINCIDENTCODE", incident_header, incident_rows)
        localization_count = insert_rows(
            conn, "TBLOCALIZATION", localization_columns, localization_rows
        )

        create_indexes(conn)
        conn.commit()
    finally:
        conn.close()

    elapsed = time.time() - started
    size_mb = output_path.stat().st_size / (1024 * 1024)
    log(
        f"Base générée : {output_path} "
        f"({incident_count:,} codes, {localization_count:,} traductions, "
        f"{size_mb:.1f} Mo, {elapsed:.1f}s)"
    )


def replace_database(target: Path, generated: Path, backup: bool) -> None:
    if backup and target.exists():
        backup_path = target.with_name(target.name + BACKUP_SUFFIX)
        shutil.copy2(target, backup_path)
        log(f"Sauvegarde : {backup_path}")

    shutil.move(str(generated), str(target))
    log(f"Base remplacée : {target}")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Importe les CSV RIPOL dans la base SQLite dbppel3."
    )
    parser.add_argument("--input-dir", type=Path, help="Dossier contenant les 2 CSV")
    parser.add_argument("--incident-csv", type=Path, help="Chemin vers TBINCIDENTCODE.csv")
    parser.add_argument(
        "--localization-csv",
        type=Path,
        help="Chemin vers TBLOCALIZATION_V1.csv ou TBLOCALIZATION.csv",
    )
    parser.add_argument(
        "--output",
        type=Path,
        default=DEFAULT_DB_PATH,
        help=f"Chemin de sortie SQLite (défaut: {DEFAULT_DB_PATH})",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Génère dbppel3.new sans remplacer la base existante",
    )
    parser.add_argument(
        "--no-backup",
        action="store_true",
        help="Ne crée pas dbppel3.backup avant remplacement",
    )
    return parser.parse_args()


def main() -> int:
    log(f"Version script : {SCRIPT_VERSION}")
    log(f"Fichier Python : {Path(__file__).resolve()}")
    args = parse_args()
    try:
        incident_csv, localization_csv = resolve_csv_paths(
            args.input_dir, args.incident_csv, args.localization_csv
        )
    except (FileNotFoundError, ValueError) as error:
        log(f"Erreur : {error}")
        return 1

    target = args.output.resolve()
    generated = target.with_name(target.name + NEW_SUFFIX)

    try:
        import_database(incident_csv, localization_csv, generated)
    except Exception as error:  # noqa: BLE001 - script CLI
        log(f"Echec import : {error}")
        if generated.exists():
            generated.unlink()
        return 1

    if args.dry_run:
        log(f"Mode dry-run : base disponible sous {generated}")
        return 0

    replace_database(target, generated, backup=not args.no_backup)

    return 0


if __name__ == "__main__":
    sys.exit(main())
