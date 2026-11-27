#!/usr/bin/env python3
"""Create an Android offline-content asset and descriptor from verified pipeline output."""
from __future__ import annotations

import argparse
import hashlib
import json
import shutil
from pathlib import Path


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def build_asset(database: Path, report_path: Path, source_manifest_path: Path, output_dir: Path) -> dict:
    report = json.loads(report_path.read_text(encoding="utf-8"))
    source_manifest = json.loads(source_manifest_path.read_text(encoding="utf-8"))
    source = source_manifest["source"]
    structural = source_manifest["structural_validation"]
    edition_id = source.get("canonical_edition_id")
    if not edition_id:
        raise ValueError("source manifest is missing canonical_edition_id")
    if report.get("status") != "PASS" or report.get("source_id") != edition_id:
        raise ValueError("content pipeline report does not match the canonical edition")
    if report.get("archive_sha256") != source.get("archive_sha256"):
        raise ValueError("pipeline archive hash does not match the authorized source manifest")
    if report.get("archive_bytes") != source.get("archive_bytes"):
        raise ValueError("pipeline archive size does not match the authorized source manifest")
    if report.get("book_count") != structural.get("usfm_file_count"):
        raise ValueError("book count does not match source structural baseline")
    if report.get("chapter_count") != structural.get("chapter_count"):
        raise ValueError("chapter count does not match source structural baseline")
    if report.get("verse_record_count") != structural.get("verse_marker_lines"):
        raise ValueError("verse record count does not match source structural baseline")

    database_report = report.get("database", {})
    if not database.is_file():
        raise ValueError("verified SQLite database is missing")
    if database.stat().st_size != database_report.get("database_bytes"):
        raise ValueError("SQLite database byte count does not match pipeline report")
    database_sha = sha256_file(database)
    if database_sha != database_report.get("database_sha256"):
        raise ValueError("SQLite database hash does not match pipeline report")

    descriptor = {
        "editionId": edition_id,
        "sourceArchiveSha256": source["archive_sha256"],
        "databaseSha256": database_sha,
        "databaseBytes": str(database.stat().st_size),
        "schemaVersion": str(report["schema_version"]),
        "parserVersion": str(report["parser_version"]),
        "bookCount": str(report["book_count"]),
        "chapterCount": str(report["chapter_count"]),
        "verseCount": str(report["verse_record_count"]),
        "ftsCount": str(database_report["fts_records"]),
        "ftsTokenizer": report["fts_tokenizer"],
        "tokenizerQualification": report["tokenizer_qualification"],
    }
    output_dir.mkdir(parents=True, exist_ok=True)
    shutil.copyfile(database, output_dir / "content.sqlite")
    (output_dir / "manifest.properties").write_text(
        "".join(f"{key}={value}\n" for key, value in descriptor.items()),
        encoding="utf-8",
    )
    return {
        "status": "PASS",
        "edition_id": edition_id,
        "database_bytes": database.stat().st_size,
        "database_sha256": database_sha,
        "scripture_text_emitted": False,
    }


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--database", required=True, type=Path)
    parser.add_argument("--report", required=True, type=Path)
    parser.add_argument("--source-manifest", required=True, type=Path)
    parser.add_argument("--output-dir", required=True, type=Path)
    args = parser.parse_args()
    try:
        result = build_asset(args.database, args.report, args.source_manifest, args.output_dir)
    except (OSError, ValueError, KeyError, json.JSONDecodeError) as exc:
        parser.exit(2, f"ANDROID CONTENT ASSET FAILED: {type(exc).__name__}\n")
    print(json.dumps(result, indent=2))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
