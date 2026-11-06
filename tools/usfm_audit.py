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
CHAPTER_RE = re.compile(r"^\\c\s+(\d+)(?:\s|$)")
VERSE_RE = re.compile(r"^\\v\s+(\d+)(?:\s|$)")
ID_RE = re.compile(r"^\\id\s+([A-Z0-9]{3})(?:\s|$)")
ETHIOPIC_RANGE = range(0x1200, 0x1380)
REPORT_VERSION = "1.2.0"


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def issue(code: str, message: str, path: str | None = None, line: int | None = None) -> dict[str, Any]:
    result: dict[str, Any] = {"code": code, "message": message}
    if path is not None:
        result["path"] = path
    if line is not None:
        result["line"] = line
    return result


def inspect_usfm(path: Path) -> dict[str, Any]:
    book_ids: list[str] = []
    chapters_by_book: dict[str, list[int]] = {}
    verse_counts: dict[str, int] = {}
    ethiopic_chars = 0
    replacement_chars = 0
    line_count = 0
    issues: list[dict[str, Any]] = []
    current_book: str | None = None
    current_chapter: int | None = None

    try:
        text = path.read_text(encoding="utf-8-sig")
    except UnicodeDecodeError:
        return {
            "path": path.name, "status": "FAIL", "errors": ["File is not valid UTF-8"],
            "issues": [issue("USFM_ENCODING_INVALID_UTF8", "File is not valid UTF-8", path.name)],
            "sha256": sha256_file(path),
        }

    for line_number, line in enumerate(text.splitlines(), start=1):
        line_count += 1
        if "\ufffd" in line:
            replacement_chars += line.count("\ufffd")
        ethiopic_chars += sum(1 for char in line if ord(char) in ETHIOPIC_RANGE)

        match = ID_RE.match(line)
        if match:
            book_id = match.group(1)
            if book_id in book_ids:
                issues.append(issue("USFM_BOOK_ID_DUPLICATE_IN_FILE", "Duplicate book ID marker in file", path.name, line_number))
            book_ids.append(book_id)
            current_book = book_id
            current_chapter = None
            chapters_by_book.setdefault(book_id, [])
            continue

        match = CHAPTER_RE.match(line)
        if match:
            chapter_number = int(match.group(1))
            if current_book is None:
                issues.append(issue("USFM_CHAPTER_BEFORE_BOOK", "Chapter appears before a book ID marker", path.name, line_number))
                current_chapter = None
                continue
            current_chapter = chapter_number
            if chapter_number < 1:
                issues.append(issue("USFM_CHAPTER_NUMBER_INVALID", "Chapter number must be positive", path.name, line_number))
            book_chapters = chapters_by_book.setdefault(current_book, [])
            if chapter_number in book_chapters:
                issues.append(issue("USFM_CHAPTER_DUPLICATE_IN_BOOK", "Duplicate chapter number within book", path.name, line_number))
            book_chapters.append(chapter_number)
            continue

        match = VERSE_RE.match(line)
        if match:
            if current_book is None or current_chapter is None:
                issues.append(issue("USFM_VERSE_BEFORE_BOOK_OR_CHAPTER", "Verse appears before a valid book and chapter marker", path.name, line_number))
            else:
                verse_number = int(match.group(1))
                if verse_number < 1:
                    issues.append(issue("USFM_VERSE_NUMBER_INVALID", "Verse number must be positive", path.name, line_number))
                key = f"{current_book}:{current_chapter}"
                verse_counts[key] = verse_counts.get(key, 0) + 1

    if not book_ids:
        issues.append(issue("USFM_BOOK_ID_MISSING", "Missing book ID marker", path.name))
    if not any(chapters_by_book.values()):
        issues.append(issue("USFM_CHAPTER_MARKER_MISSING", "Missing chapter markers", path.name))
    if replacement_chars:
        issues.append(issue("USFM_UNICODE_REPLACEMENT_CHARACTER", f"Found {replacement_chars} Unicode replacement character(s)", path.name))

    return {
        "path": path.name,
        "status": "FAIL" if issues else "PASS",
        "sha256": sha256_file(path),
        "book_ids": book_ids,
        "chapters_by_book": chapters_by_book,
        "verse_marker_counts_by_book_and_chapter": verse_counts,
        "line_count": line_count,
        "ethiopic_codepoint_count": ethiopic_chars,
        "unicode_replacement_character_count": replacement_chars,
        "issues": issues,
        "errors": [item["message"] for item in issues],
    }


def _validate_inventory(inventory: Any) -> tuple[dict[str, list[int]] | None, list[dict[str, Any]]]:
    issues: list[dict[str, Any]] = []
    if not isinstance(inventory, dict):
        return None, [issue("INVENTORY_ROOT_INVALID", "Expected inventory root must be an object")]
    if inventory.get("schema_version") != "1.0.0":
        issues.append(issue("INVENTORY_SCHEMA_VERSION_UNSUPPORTED", "Expected inventory schema_version must be 1.0.0"))
    if not isinstance(inventory.get("source_reference"), str) or not inventory["source_reference"].strip():
        issues.append(issue("INVENTORY_SOURCE_REFERENCE_MISSING", "Expected inventory requires a non-empty source_reference"))
    if not isinstance(inventory.get("owner_approval_reference"), str) or not inventory["owner_approval_reference"].strip():
        issues.append(issue("INVENTORY_OWNER_APPROVAL_REFERENCE_MISSING", "Expected inventory requires an Owner approval reference"))
    books = inventory.get("books")
    if not isinstance(books, list) or not books:
        issues.append(issue("INVENTORY_BOOKS_INVALID", "Expected inventory books must be a non-empty array"))
        return None, issues

    normalized: dict[str, list[int]] = {}
    for index, item in enumerate(books):
        if not isinstance(item, dict):
            issues.append(issue("INVENTORY_BOOK_ENTRY_INVALID", f"Book entry {index} must be an object"))
            continue
        book_id = item.get("book_id")
        chapters = item.get("chapters")
        if not isinstance(book_id, str) or not BOOK_ID_RE.fullmatch(book_id):
            issues.append(issue("INVENTORY_BOOK_ID_INVALID", f"Book entry {index} has an invalid three-character book_id"))
            continue
        if book_id in normalized:
            issues.append(issue("INVENTORY_BOOK_ID_DUPLICATE", f"Expected inventory repeats book ID {book_id}"))
            continue
        if not isinstance(chapters, list) or not chapters or any(type(value) is not int or value < 1 for value in chapters):
            issues.append(issue("INVENTORY_CHAPTERS_INVALID", f"Expected inventory chapters for {book_id} must be positive integers"))
            continue
        if len(chapters) != len(set(chapters)):
            issues.append(issue("INVENTORY_CHAPTER_DUPLICATE", f"Expected inventory repeats a chapter for {book_id}"))
            continue
        normalized[book_id] = sorted(chapters)
    return (normalized if not issues else None), issues


def audit_manifest(manifest_path: Path, source_root: Path, expected_inventory_path: Path | None = None) -> dict[str, Any]:
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    issues: list[dict[str, Any]] = []
    if not isinstance(manifest, dict):
        raise ValueError("Manifest root must be an object")
    if manifest.get("schema_version") != "1.0.0":
        raise ValueError("Unsupported manifest schema_version")

    rights_status = manifest.get("rights", {}).get("status") if isinstance(manifest.get("rights"), dict) else None
    files = manifest.get("files")
    if not isinstance(files, list) or not files:
        raise ValueError("Manifest files must be a non-empty array")

    results: list[dict[str, Any]] = []
    seen_paths: set[str] = set()
    package_book_paths: dict[str, str] = {}
    package_chapters: dict[str, list[int]] = {}
    package_book_ids: set[str] = set()

    for item in files:
        if not isinstance(item, dict) or not isinstance(item.get("path"), str):
            issues.append(issue("MANIFEST_FILE_ENTRY_INVALID", "Manifest file entry must be an object with a path"))
            continue
        relative = Path(item["path"])
        if relative.is_absolute() or ".." in relative.parts or "\\" in item["path"] or ":" in item["path"]:
            results.append({"path": item["path"], "status": "FAIL", "issues": [issue("MANIFEST_PATH_UNSAFE", "Unsafe manifest path", item["path"])], "errors": ["Unsafe manifest path"]})
            continue
        normalized = relative.as_posix()
        if normalized in seen_paths:
            results.append({"path": normalized, "status": "FAIL", "issues": [issue("MANIFEST_PATH_DUPLICATE", "Duplicate manifest path", normalized)], "errors": ["Duplicate manifest path"]})
            continue
        seen_paths.add(normalized)

        target = (source_root / relative).resolve()
        root = source_root.resolve()
        if root not in target.parents:
            results.append({"path": normalized, "status": "FAIL", "issues": [issue("MANIFEST_PATH_ESCAPES_SOURCE_ROOT", "Path escapes source directory", normalized)], "errors": ["Path escapes source directory"]})
            continue
        if not target.is_file():
            results.append({"path": normalized, "status": "FAIL", "issues": [issue("MANIFEST_FILE_NOT_FOUND", "File not found", normalized)], "errors": ["File not found"]})
            continue

        actual_hash = sha256_file(target)
        expected_hash = item.get("sha256", "")
        hash_ok = isinstance(expected_hash, str) and re.fullmatch(r"[a-fA-F0-9]{64}", expected_hash) is not None and actual_hash.lower() == expected_hash.lower()
        if item.get("format") == "USFM":
            result = inspect_usfm(target)
            for book_id in result.get("book_ids", []):
                if book_id in package_book_paths:
                    if package_book_paths[book_id] != normalized:
                        result.setdefault("issues", []).append(issue("PACKAGE_BOOK_ID_DUPLICATE_ACROSS_FILES", f"Book ID {book_id} occurs in more than one USFM file", normalized))
                else:
                    package_book_paths[book_id] = normalized
                package_book_ids.add(book_id)
            for book_id, chapters in result.get("chapters_by_book", {}).items():
                package_chapters.setdefault(book_id, []).extend(chapters)
            if item.get("book_id") is not None:
                declared = item.get("book_id")
                if declared not in result.get("book_ids", []):
                    result.setdefault("issues", []).append(issue("MANIFEST_BOOK_ID_MISMATCH", "Manifest book_id does not occur in the referenced USFM file", normalized))
        else:
            result = {"path": normalized, "status": "NOT_CHECKED", "sha256": actual_hash, "issues": [], "errors": []}
        result["path"] = normalized
        result["manifest_sha256_match"] = hash_ok
        if not hash_ok:
            result.setdefault("issues", []).append(issue("MANIFEST_SHA256_MISMATCH", "SHA-256 is invalid or does not match manifest", normalized))
        result["errors"] = [entry["message"] for entry in result.get("issues", [])]
        if result.get("issues"):
            result["status"] = "FAIL"
        results.append(result)

    inventory_status = "NOT_PROVIDED"
    inventory_reference = None
    if expected_inventory_path is not None:
        inventory = json.loads(expected_inventory_path.read_text(encoding="utf-8"))
        expected, inventory_issues = _validate_inventory(inventory)
        issues.extend(inventory_issues)
        inventory_reference = inventory.get("source_reference") if isinstance(inventory, dict) else None
        if expected is None:
            inventory_status = "INVALID"
        else:
            inventory_status = "PASS"
            actual_books = set(package_chapters)
            for book_id in sorted(set(expected) - actual_books):
                issues.append(issue("INVENTORY_BOOK_MISSING", f"Expected book {book_id} is absent from package"))
                inventory_status = "FAIL"
            for book_id in sorted(actual_books - set(expected)):
                issues.append(issue("INVENTORY_BOOK_UNEXPECTED", f"Package contains unexpected book {book_id}"))
                inventory_status = "FAIL"
            for book_id in sorted(set(expected) & actual_books):
                actual = set(package_chapters[book_id])
                for chapter in sorted(set(expected[book_id]) - actual):
                    issues.append(issue("INVENTORY_CHAPTER_MISSING", f"Expected chapter {book_id} {chapter} is absent"))
                    inventory_status = "FAIL"
                for chapter in sorted(actual - set(expected[book_id])):
                    issues.append(issue("INVENTORY_CHAPTER_UNEXPECTED", f"Package contains unexpected chapter {book_id} {chapter}"))
                    inventory_status = "FAIL"

    failed_files = any(item["status"] == "FAIL" for item in results)
    partially_checked = any(item["status"] == "NOT_CHECKED" for item in results)
    failed = failed_files or bool(issues)
    rights = manifest.get("rights", {}) if isinstance(manifest.get("rights"), dict) else {}
    rights_documented = (
        rights.get("status") == "APPROVED"
        and bool(rights.get("evidence_reference"))
        and bool(rights.get("owner_approval_reference"))
    )
    structural_audit = "FAIL" if failed else ("PARTIAL" if partially_checked else "PASS")
    return {
        "report_version": REPORT_VERSION,
        "source_id": manifest.get("source_id"),
        "rights_status": rights_status,
        "rights_gate": "DOCUMENTED_REQUIRES_OWNER_REVIEW" if rights_documented else "BLOCKED",
        "rights_evidence_fields_present": rights_documented,
        "content_intake_authorized": False,
        "structural_audit": structural_audit,
        "overall_status": "FAIL" if failed else ("PARTIAL_UNSUPPORTED_FORMAT" if partially_checked else ("STRUCTURE_PASS_RIGHTS_BLOCKED" if not rights_documented else "STRUCTURE_PASS_REQUIRES_OWNER_REVIEW")),
        "package_inventory": {
            "status": inventory_status,
            "reference": inventory_reference,
            "book_count": len(package_book_ids),
            "book_ids": sorted(package_book_ids),
            "chapter_counts_by_book": {key: len(set(value)) for key, value in sorted(package_chapters.items())},
        },
        "file_results": results,
        "issues": issues,
        "limitations": [
            "Does not verify translation accuracy, completeness, canon, or linguistic correctness.",
            "Expected inventory validation is meaningful only when its reference is independently reviewed and Owner-approved.",
            "Does not establish copyright ownership, license authority, or permission.",
            "Does not authorize committing or distributing source content.",
            "A structural pass is not a rights approval or content-intake authorization.",
        ],
    }


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--manifest", required=True, type=Path)
    parser.add_argument("--source-root", required=True, type=Path)
    parser.add_argument("--expected-inventory", type=Path, help="Optional Owner-approved expected book/chapter inventory JSON")
    parser.add_argument("--output", type=Path, help="Optional JSON report path")
    args = parser.parse_args()

    try:
        report = audit_manifest(args.manifest, args.source_root, args.expected_inventory)
    except (OSError, ValueError, KeyError, json.JSONDecodeError) as exc:
        print(f"AUDIT ERROR: {exc}", file=sys.stderr)
        return 2

    rendered = json.dumps(report, ensure_ascii=False, indent=2) + "\n"
    if args.output:
        args.output.write_text(rendered, encoding="utf-8")
    else:
        print(rendered, end="")
    return 1 if report["overall_status"] in {"FAIL", "PARTIAL_UNSUPPORTED_FORMAT"} else 0


if __name__ == "__main__":
    raise SystemExit(main())
