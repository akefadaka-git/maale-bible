#!/usr/bin/env python3
"""Audit an authorized local USFM package without publishing scripture text.

The tool reports structural metadata and hashes only. It never copies source
text into its report. It is not a theological, linguistic, legal, or canon
validator. Python 3.10+; standard library only.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import sys
from pathlib import Path
from typing import Any

BOOK_ID_RE = re.compile(r"^[A-Z0-9]{3}$")
CHAPTER_RE = re.compile(r"^\\c\\s+(\\d+)(?:\\s|$)")
VERSE_RE = re.compile(r"^\\v\\s+(\\d+)(?:\\s|$)")
ID_RE = re.compile(r"^\\id\\s+([A-Z0-9]{3})(?:\\s|$)")
ETHIOPIC_RANGE = range(0x1200, 0x1380)


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def inspect_usfm(path: Path) -> dict[str, Any]:
    book_ids: list[str] = []
    chapters: list[int] = []
    verse_counts: dict[str, int] = {}
    ethiopic_chars = 0
    replacement_chars = 0
    line_count = 0
    errors: list[str] = []
    current_chapter: int | None = None

    try:
        text = path.read_text(encoding="utf-8-sig")
    except UnicodeDecodeError:
        return {"path": path.name, "status": "FAIL", "errors": ["File is not valid UTF-8"], "sha256": sha256_file(path)}

    for line_number, line in enumerate(text.splitlines(), start=1):
        line_count += 1
        if "\ufffd" in line:
            replacement_chars += line.count("\ufffd")
        ethiopic_chars += sum(1 for char in line if ord(char) in ETHIOPIC_RANGE)

        match = ID_RE.match(line)
        if match:
            book_id = match.group(1)
            if book_id in book_ids:
                errors.append(f"Line {line_number}: duplicate book ID marker")
            book_ids.append(book_id)
            continue

        match = CHAPTER_RE.match(line)
        if match:
            current_chapter = int(match.group(1))
            if current_chapter < 1:
                errors.append(f"Line {line_number}: chapter number must be positive")
            if current_chapter in chapters:
                errors.append(f"Line {line_number}: duplicate chapter number")
            chapters.append(current_chapter)
            continue

        match = VERSE_RE.match(line)
        if match:
            if current_chapter is None:
                errors.append(f"Line {line_number}: verse appears before a chapter marker")
            else:
                verse_number = int(match.group(1))
                if verse_number < 1:
                    errors.append(f"Line {line_number}: verse number must be positive")
                key = str(current_chapter)
                verse_counts[key] = verse_counts.get(key, 0) + 1

    if not book_ids:
        errors.append("Missing \\id marker")
    if not chapters:
        errors.append("Missing \\c markers")
    if replacement_chars:
        errors.append(f"Found {replacement_chars} Unicode replacement character(s)")

    return {
        "path": path.name,
        "status": "FAIL" if errors else "PASS",
        "sha256": sha256_file(path),
        "book_ids": book_ids,
        "chapter_numbers": chapters,
        "verse_marker_counts_by_chapter": verse_counts,
        "line_count": line_count,
        "ethiopic_codepoint_count": ethiopic_chars,
        "unicode_replacement_character_count": replacement_chars,
        "errors": errors,
    }


def audit_manifest(manifest_path: Path, source_root: Path) -> dict[str, Any]:
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    if manifest.get("schema_version") != "1.0.0":
        raise ValueError("Unsupported manifest schema_version")

    rights_status = manifest.get("rights", {}).get("status")
    files = manifest.get("files")
    if not isinstance(files, list) or not files:
        raise ValueError("Manifest files must be a non-empty array")

    results: list[dict[str, Any]] = []
    seen_paths: set[str] = set()
    for item in files:
        relative = Path(item["path"])
        if relative.is_absolute() or ".." in relative.parts:
            results.append({"path": item["path"], "status": "FAIL", "errors": ["Unsafe manifest path"]})
            continue
        normalized = relative.as_posix()
        if normalized in seen_paths:
            results.append({"path": normalized, "status": "FAIL", "errors": ["Duplicate manifest path"]})
            continue
        seen_paths.add(normalized)

        target = (source_root / relative).resolve()
        root = source_root.resolve()
        if root not in target.parents:
            results.append({"path": normalized, "status": "FAIL", "errors": ["Path escapes source directory"]})
            continue
        if not target.is_file():
            results.append({"path": normalized, "status": "FAIL", "errors": ["File not found"]})
            continue

        actual_hash = sha256_file(target)
        expected_hash = item.get("sha256", "")
        hash_ok = actual_hash.lower() == expected_hash.lower()
        if item.get("format") == "USFM":
            result = inspect_usfm(target)
        else:
            result = {"path": normalized, "status": "NOT_CHECKED", "sha256": actual_hash, "errors": []}
        result["path"] = normalized
        result["manifest_sha256_match"] = hash_ok
        if not hash_ok:
            result["status"] = "FAIL"
            result.setdefault("errors", []).append("SHA-256 does not match manifest")
        results.append(result)

    failed = any(item["status"] == "FAIL" for item in results)
    rights_approved = rights_status == "APPROVED"
    return {
        "report_version": "1.0.0",
        "source_id": manifest.get("source_id"),
        "rights_status": rights_status,
        "rights_gate": "PASS" if rights_approved else "BLOCKED",
        "content_intake_authorized": rights_approved,
        "structural_audit": "FAIL" if failed else "PASS",
        "overall_status": "FAIL" if failed else ("PASS_WITH_RIGHTS_BLOCKED" if not rights_approved else "PASS"),
        "file_results": results,
        "limitations": [
            "Does not verify translation accuracy, completeness, canon, or linguistic correctness.",
            "Does not establish copyright ownership, license authority, or permission.",
            "Does not authorize committing or distributing source content.",
            "Book/chapter/verse expectations must be independently supplied and reviewed."
        ]
    }


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--manifest", required=True, type=Path)
    parser.add_argument("--source-root", required=True, type=Path)
    parser.add_argument("--output", type=Path, help="Optional JSON report path")
    args = parser.parse_args()

    try:
        report = audit_manifest(args.manifest, args.source_root)
    except (OSError, ValueError, KeyError, json.JSONDecodeError) as exc:
        print(f"AUDIT ERROR: {exc}", file=sys.stderr)
        return 2

    rendered = json.dumps(report, ensure_ascii=False, indent=2) + "\n"
    if args.output:
        args.output.write_text(rendered, encoding="utf-8")
    else:
        print(rendered, end="")
    return 1 if report["overall_status"] == "FAIL" else 0


if __name__ == "__main__":
    raise SystemExit(main())
