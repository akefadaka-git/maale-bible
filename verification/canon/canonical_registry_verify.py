#!/usr/bin/env python3
"""Deterministic parity checks for the frozen canon JSON, Kotlin registry and SQLite migration.

This validates identity metadata and migration guardrails only. It does not establish
Maale translation correctness, edition versification, or source-manifest reconciliation.
"""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
JSON_PATH = ROOT / "content/canon/66-v1/registry.json"
KOTLIN_PATH = ROOT / "app/src/main/java/org/maale/bible/data/CanonicalCanonRegistry.kt"
DB_PATH = ROOT / "app/src/main/java/org/maale/bible/data/LocalStudyDatabase.kt"


def main():
    registry = json.loads(JSON_PATH.read_text(encoding="utf-8"))
    kotlin = KOTLIN_PATH.read_text(encoding="utf-8")
    database = DB_PATH.read_text(encoding="utf-8")
    books = registry["books"]
    assert registry["canon_id"] == "maale-bible:canon:66-v1"
    assert registry["version"] == 1 and registry["status"] == "FROZEN"
    assert registry["book_count"] == len(books) == 66
    assert registry["old_testament_count"] == 39
    assert registry["new_testament_count"] == 27
    assert [b["order"] for b in books] == list(range(1, 67))
    assert len({b["usfm_id"] for b in books}) == 66
    assert all(re.fullmatch(r"[A-Z0-9]{3}", b["usfm_id"]) for b in books)
    assert sum(b["testament"] == "OT" for b in books) == 39
    assert sum(b["testament"] == "NT" for b in books) == 27

    pattern = re.compile(
        r'CanonicalBook\((\d+),\s*"([^"]+)",\s*"([^"]+)",\s*"(OT|NT)"\)'
    )
    typed = [
        {"order": int(m.group(1)), "usfm_id": m.group(2),
         "canonical_name": m.group(3), "testament": m.group(4)}
        for m in pattern.finditer(kotlin)
    ]
    assert typed == books, "Kotlin registry does not exactly match canonical JSON"
    assert "const val CANON_ID = \"maale-bible:canon:66-v1\"" in kotlin
    assert "const val VERSION = 1" in kotlin

    required_sql_contracts = [
        "CHECK(book_count = 66)",
        "UNIQUE(canon_id, canonical_order)",
        "CHECK(length(book_id)=3 AND book_id=upper(book_id))",
        "CREATE TRIGGER canonical_book_no_update",
        "CREATE TRIGGER canonical_book_no_delete",
        "CREATE TRIGGER canon_registry_no_update",
        "CREATE TRIGGER canon_registry_no_delete",
        "CREATE TRIGGER canon_registry_no_insert",
        "CREATE TRIGGER canonical_book_no_insert",
        "CREATE TRIGGER passage_mapping_validate_cardinality",
        "EXACT mapping requires one source and one target",
        "SPLIT mapping requires one source and multiple targets",
        "MERGED mapping requires multiple sources and one target",
        "PARTIAL mapping requires at least one source and target",
        "UNMAPPED mapping requires source provenance and no target",
        "PRAGMA user_version = 3",
    ]
    missing = [item for item in required_sql_contracts if item not in database]
    assert not missing, "SQLite migration missing required contracts: " + ", ".join(missing)

    print(json.dumps({
        "status": "PASS",
        "canon_id": registry["canon_id"],
        "version": registry["version"],
        "books": len(books),
        "old_testament": 39,
        "new_testament": 27,
        "json_kotlin_parity": "PASS — exact ordered entry equality",
        "sqlite_migration_static_contracts": "PASS",
        "source_manifest_reconciliation": "NOT RUN — requires the approved source manifest",
        "linguistic_review": "DEFERRED — not asserted by this technical check"
    }, indent=2))


if __name__ == "__main__":
    main()
