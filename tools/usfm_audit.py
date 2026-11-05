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
CHAPTER_RE = re.compile(r"^\\\\c\\s+(\\d+)(?:\\s|$)")
VERSE_RE = re.compile(r"^\\\\v\\s+(\\d+)(?:\\s|$)")
ID_RE = re.compile(r"^\\\\id\\s+([A-Z0-9]{3})(?:\\s|$)")
ETHIOPIC_RANGE = range(0x1200, 0x1380)


def sha256_file(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        for chunk in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def _issue(code: str, message: str, path: str | None = None) -> dict[str, str]:
    item = {"code": code, "message": message}
    if path is not None:
        item["path"] = path
    return item


def inspect_usfm(path: Path) -> dict[str, Any]:
    book_ids: list[str] = []
    chapters_by_book: dict[str, list[int]] = {}
    verse_counts: dict[str, int] = {}
    ethiopic_chars = 0
    replacement_chars = 0
    line_count = 0
    issues: list[dict[str, str]] = []
    current_book: str | None = None
    current_chapter: int | None = None

    try:
        text = path.read_text(encoding="utf-8-sig")
    except UnicodeDecodeError:
        return {
            "path": path.name,
            "status": "FAIL",
            "sha256": sha256_file(path),
            "book_ids": [],
            "chapters_by_book": {},
            "verse_marker_counts_by_book_and_chapter": {},
            "line_count": 0,
            "ethiopic_codepoint_count": 0,
            "unicode_replacement_character_count": 0,
            "issues": [_issue("USFM_INVALID_UTF8", "File is not valid UTF-8", path.name)],
            "errors": ["File is not valid UTF-8"],
        }

    for line_number, line in enumerate(text.splitlines(), start=1):
        line_count += 1
        if "\\ufffd" in line:
            replacement_chars += line.count("\\ufffd")
        ethiopic_chars += sum(1 for char in line if ord(char) in ETHIOPIC_RANGE)

        match = ID_RE.match(line)
        if match:
            book_id = match.group(1)
            if book_id in book_ids:
                issues.append(_issue("USFM_DUPLICATE_BOOK_ID", f"Line {line_number}: duplicate book ID marker {book_id}", path.name))
            book_ids.append(book_id)
            current_book = book_id
            current_chapter = None
            chapters_by_book.setdefault(book_id, [])
            continue

        match = CHAPTER_RE.match(line)
        if match:
            chapter_number = int(match.group(1))
            if current_book is None:
                issues.append(_issue("USFM_CHAPTER_BEFORE_BOOK", f"Line {line_number}: chapter appears before a book ID marker", path.name))
                current_chapter = None
                continue
            current_chapter = chapter_number
            if chapter_number < 1:
                issues.append(_issue("USFM_INVALID_CHAPTER_NUMBER", f"Line {line_number}: chapter number must be positive", path.name))
            book_chapters = chapters_by_book.setdefault(current_book, [])
            if chapter_number in book_chapters:
                issues.append(_issue("USFM_DUPLICATE_CHAPTER", f"Line {line_number}: duplicate chapter number in book {current_book}", path.name))
            book_chapters.append(chapter_number)
            continue

        match = VERSE_RE.match(line)
        if match:
            if current_book is None or current_chapter is None:
                issues.append(_issue("USFM_VERSE_BEFORE_BOOK_OR_CHAPTER", f"Line {line_number}: verse appears before a valid book and chapter marker", path.name))
            else:
                verse_number = int(match.group(1))
                if verse_number < 1:
                    issues.append(_issue("USFM_INVALID_VERSE_NUMBER", f"Line {line_number}: verse number must be positive", path.name))
                key = f"{current_book}:{current_chapter}"
                verse_counts[key] = verse_counts.get(key, 0) + 1

    if not book_ids:
        issues.append(_issue("USFM_MISSING_BOOK_ID", "Missing \\id marker", path.name))
    if not any(chapters_by_book.values()):
        issues.append(_issue("USFM_MISSING_CHAPTER", "Missing \\c markers", path.name))
    if replacement_chars:
        issues.append(_issue("USFM_REPLACEMENT_CHARACTERS", f"Found {replacement_chars} Unicode replacement character(s)", path.name))

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


def _load_expected_inventory(path: Path) -> dict[str, list[int]]:
    inventory = json.loads(path.read_text(encoding="utf-8"))
    if not isinstance(inventory, dict) or inventory.get("schema_version") != "1.0.0":
        raise ValueError("Expected inventory schema_version must be 1.0.0")
    books = inventory.get("books")
    if not isinstance(books, list) or not books:
        raise ValueError("Expected inventory books must be a non-empty array")

    expected: dict[str, list[int]] = {}
    for item in books:
        if not isinstance(item, dict):
            raise ValueError("Each expected inventory book must be an object")
        book_id = item.get("book_id")
        chapters = item.get("chapters")
        if not isinstance(book_id, str) or not BOOK_ID_RE.fullmatch(book_id):
            raise ValueError(f"Invalid expected inventory book_id: {book_id!r}")
        if book_id in expected:
            raise ValueError(f"Duplicate expected inventory book_id: {book_id}")
        if not isinstance(chapters, list) or not chapters:
            raise ValueError(f"Expected chapters for {book_id} must be a non-empty array")
        if any(type(chapter) is not int or chapter < 1 for chapter in chapters):
            raise ValueError(f"Expected chapters for {book_id} must be positive integers")
        if len(chapters) != len(set(chapters)):
            raise ValueError(f"Duplicate expected chapter number for {book_id}")
        expected[book_id] = sorted(chapters)
    return expected


def _compare_inventory(
    actual: dict[str, list[int]], expected: dict[str, list[int]]
) -> list[dict[str, str]]:
    issues: list[dict[str, str]] = []
    for book_id in sorted(set(expected) - set(actual)):
        issues.append(_issue("INVENTORY_MISSING_BOOK", f"Expected book {book_id} was not found"))
    for book_id in sorted(set(actual) - set(expected)):
        issues.append(_issue("INVENTORY_UNEXPECTED_BOOK", f"Unexpected book {book_id} was found"))
    for book_id in sorted(set(actual) & set(expected)):
        actual_chapters = sorted(actual[book_id])
        expected_chapters = expected[book_id]
        for chapter in sorted(set(expected_chapters) - set(actual_chapters)):
            issues.append(_issue("INVENTORY_MISSING_CHAPTER", f"Expected chapter {book_id} {chapter} was not found"))
        for chapter in sorted(set(actual_chapters) - set(expected_chapters)):
            issues.append(_issue("INVENTORY_UNEXPECTED_CHAPTER", f"Unexpected chapter {book_id} {chapter} was found"))
    return issues


def audit_manifest(
    manifest_path: Path,
    source_root: Path,
    expected_inventory_path: Path | None = None,
) -> dict[str, Any]:
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    if manifest.get("schema_version") != "1.0.0":
        raise ValueError("Unsupported manifest schema_version")

    rights_status = manifest.get("rights", {}).get("status")
    files = manifest.get("files")
    if not isinstance(files, list) or not files:
        raise ValueError("Manifest files must be a non-empty array")

    results: list[dict[str, Any]] = []
    package_issues: list[dict[str, str]] = []
    seen_paths: set[str] = set()
    package_chapters: dict[str, list[int]] = {}
    first_book_path: dict[str, str] = {}

    for item in files:
        if not isinstance(item, dict) or not isinstance(item.get("path"), str):
            package_issues.append(_issue("MANIFEST_INVALID_FILE_ENTRY", "Manifest file entry must contain a string path"))
            continue
        raw_path = item["path"]
        relative = Path(raw_path)
        if relative.is_absolute() or ".." in relative.parts or "\\\\" in raw_path or ":" in raw_path:
            results.append({"path": raw_path, "status": "FAIL", "issues": [_issue("MANIFEST_UNSAFE_PATH", "Unsafe manifest path", raw_path)], "errors": ["Unsafe manifest path"]})
            continue
        normalized = relative.as_posix()
        if normalized in seen_paths:
            results.append({"path": normalized, "status": "FAIL", "issues": [_issue("MANIFEST_DUPLICATE_PATH", "Duplicate manifest path", normalized)], "errors": ["Duplicate manifest path"]})
            continue
        seen_paths.add(normalized)

        target = (source_root / relative).resolve()
        root = source_root.resolve()
        if root != target and root not in target.parents:
            results.append({"path": normalized, "status": "FAIL", "issues": [_issue("MANIFEST_PATH_ESCAPE", "Path escapes source directory", normalized)], "errors": ["Path escapes source directory"]})
            continue
        if not target.is_file():
            results.append({"path": normalized, "status": "FAIL", "issues": [_issue("MANIFEST_FILE_MISSING", "File not found", normalized)], "errors": ["File not found"]})
            continue

        actual_hash = sha256_file(target)
        expected_hash = item.get("sha256", "")
        hash_ok = isinstance(expected_hash, str) and actual_hash.lower() == expected_hash.lower()
        if item.get("format") == "USFM":
            result = inspect_usfm(target)
            result["path"] = normalized
            for book_id in result["book_ids"]:
                if book_id in first_book_path:
                    package_issues.append(_issue(
                        "PACKAGE_DUPLICATE_BOOK_ID",
                        f"Book ID {book_id} appears in more than one manifest source entry",
                        normalized,
                    ))
                else:
                    first_book_path[book_id] = normalized
                package_chapters.setdefault(book_id, []).extend(result["chapters_by_book"].get(book_id, []))
            declared_book_id = item.get("book_id")
            if declared_book_id is not None and result["book_ids"] != [declared_book_id]:
                result.setdefault("issues", []).append(_issue(
                    "MANIFEST_BOOK_ID_MISMATCH",
                    f"Manifest book_id {declared_book_id!r} does not match file book IDs {result['book_ids']!r}",
                    normalized,
                ))
        else:
            result = {"path": normalized, "status": "NOT_CHECKED", "sha256": actual_hash, "issues": [], "errors": []}
        result["path"] = normalized
        result["manifest_sha256_match"] = hash_ok
        if not hash_ok:
            result["status"] = "FAIL"
            result.setdefault("issues", []).append(_issue("MANIFEST_HASH_MISMATCH", "SHA-256 does not match manifest", normalized))
        result["errors"] = [issue["message"] for issue in result.get("issues", [])]
        if result.get("issues"):
            result["status"] = "FAIL"
        results.append(result)

    if expected_inventory_path is not None:
        expected = _load_expected_inventory(expected_inventory_path)
        package_issues.extend(_compare_inventory(package_chapters, expected))

    failed = any(item["status"] == "FAIL" for item in results) or bool(package_issues)
    partially_checked = any(item["status"] == "NOT_CHECKED" for item in results)
    rights = manifest.get("rights", {})
    rights_documented = (
        rights.get("status") == "APPROVED"
        and bool(rights.get("evidence_reference"))
        and bool(rights.get("owner_approval_reference"))
    )
    return {
        "report_version": "1.2.0",
        "source_id": manifest.get("source_id"),
        "rights_status": rights_status,
        "rights_gate": "DOCUMENTED_REQUIRES_OWNER_REVIEW" if rights_documented else "BLOCKED",
        "rights_evidence_fields_present": rights_documented,
        "content_intake_authorized": False,
        "structural_audit": "FAIL" if failed else ("PARTIAL" if partially_checked else "PASS"),
        "overall_status": "FAIL" if failed else ("PARTIAL_UNSUPPORTED_FORMAT" if partially_checked else ("STRUCTURE_PASS_RIGHTS_BLOCKED" if not rights_documented else "STRUCTURE_PASS_REQUIRES_OWNER_REVIEW")),
        "expected_inventory_checked": expected_inventory_path is not None,
        "package_book_ids": sorted(package_chapters),
        "package_chapters_by_book": {key: sorted(value) for key, value in sorted(package_chapters.items())},
        "package_issues": package_issues,
        "file_results": results,
        "limitations": [
            "Does not verify translation accuracy, completeness, canon, or linguistic correctness.",
            "An expected inventory is only as authoritative as its separately reviewed reference.",
            "Does not establish copyright ownership, license authority, or permission.",
            "Does not authorize committing or distributing source content.",
            "A structural pass does not close the rights gate or authorize content intake."
        ]
    }


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--manifest", required=True, type=Path)
    parser.add_argument("--source-root", required=True, type=Path)
    parser.add_argument("--expected-inventory", type=Path, help="Optional independently reviewed book/chapter inventory JSON")
    parser.add_argument("--output", type=Path, help="Optional JSON report path")
    args = parser.parse_args()

    try:
        report = audit_manifest(args.manifest, args.source_root, args.expected_inventory)
    except (OSError, ValueError, KeyError, json.JSONDecodeError) as exc:
        print(f"AUDIT ERROR: {exc}", file=sys.stderr)
        return 2

    rendered = json.dumps(report, ensure_ascii=False, indent=2) + "\\n"
    if args.output:
        args.output.write_text(rendered, encoding="utf-8")
    else:
        print(rendered, end="")
    return 1 if report["overall_status"] in {"FAIL", "PARTIAL_UNSUPPORTED_FORMAT"} else 0


if __name__ == "__main__":
    raise SystemExit(main())
