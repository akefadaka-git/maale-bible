#!/usr/bin/env python3
"""Verify Genesis learning references against the exact Owner-accepted 2008 MDYTWF USFM archive."""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import sys
import zipfile
from pathlib import Path

ARCHIVE_SHA256 = "4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11"
GENESIS_SHA256 = "cc9b7703d660bd39da8a358f9a3fb27d97f05b04f8851b3cf3e536f9e1f392"
ROOT = Path(__file__).resolve().parents[2]
PACKAGE = ROOT / "docs/content/books/GEN.json"
BOOK_FILE = re.compile(r"^\d{2}-([A-Z0-9]{3})mdyeth\.usfm$")
CHAPTER_MARKER = re.compile(r"^\\c\s+(\d+)")
VERSE_MARKER = re.compile(r"^\\v\s+(\d+[a-z]?)(?:-(\d+[a-z]?))?")
REFERENCE_PART = re.compile(r"^([A-Z0-9]{3})?\s*(\d+)(?:-(\d+))?(?::(\d+[a-z]?)(?:-(?:(\d+):)?(\d+[a-z]?))?)?$")

def digest(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()

def number(label: str) -> int:
    match = re.match(r"\d+", label)
    if not match:
        raise ValueError(f"invalid verse label {label!r}")
    return int(match.group())

def parse_usfm(data: bytes, book_id: str) -> dict[int, set[int]]:
    chapters: dict[int, set[int]] = {}
    current = None
    for line in data.decode("utf-8", errors="strict").splitlines():
        chapter = CHAPTER_MARKER.match(line)
        if chapter:
            current = int(chapter.group(1))
            chapters.setdefault(current, set())
            continue
        verse = VERSE_MARKER.match(line)
        if verse and current is not None:
            start = number(verse.group(1))
            end = number(verse.group(2)) if verse.group(2) else start
            if end < start:
                raise ValueError(f"{book_id} {current}: reversed verse marker {line}")
            chapters[current].update(range(start, end + 1))
    if not chapters:
        raise ValueError(f"{book_id}: no chapter markers")
    return chapters

def collect_references(node, output: set[str]) -> None:
    reference_keys = {"references", "cross_references", "key_passages", "reference", "passage", "range"}
    if isinstance(node, dict):
        for key, value in node.items():
            if key in reference_keys:
                if isinstance(value, str) and value.strip():
                    output.add(value.strip())
                elif isinstance(value, list):
                    output.update(str(item).strip() for item in value if isinstance(item, str) and item.strip())
            collect_references(value, output)
    elif isinstance(node, list):
        for value in node:
            collect_references(value, output)

def validate_one(reference: str, books: dict[str, dict[int, set[int]]]) -> list[str]:
    issues: list[str] = []
    inherited_book = None
    for raw in reference.split(";"):
        part = raw.strip()
        match = REFERENCE_PART.fullmatch(part)
        if not match:
            issues.append(f"unparsed segment {part!r}")
            continue
        book_id = match.group(1) or inherited_book
        if not book_id:
            issues.append(f"missing book identifier in {part!r}")
            continue
        inherited_book = book_id
        if book_id not in books:
            issues.append(f"book {book_id} is absent from pinned source")
            continue
        chapter_start = int(match.group(2))
        chapter_end = int(match.group(3) or chapter_start)
        if chapter_end < chapter_start:
            issues.append(f"reversed chapter range in {part!r}")
            continue
        missing_chapters = [c for c in range(chapter_start, chapter_end + 1) if c not in books[book_id]]
        if missing_chapters:
            issues.append(f"missing chapter(s) {book_id} {missing_chapters[:12]}")
            continue
        verse_start_label, verse_end_chapter_label, verse_end_label = match.group(4), match.group(5), match.group(6)
        if verse_start_label:
            start_verse = number(verse_start_label)
            end_chapter = int(verse_end_chapter_label) if verse_end_chapter_label else chapter_start
            end_verse = number(verse_end_label) if verse_end_label else start_verse
            if end_chapter < chapter_start or end_chapter not in books[book_id] or (end_chapter == chapter_start and end_verse < start_verse):
                issues.append(f"invalid cross-chapter or reversed verse range in {part!r}")
                continue
            if end_chapter == chapter_start:
                missing = [v for v in range(start_verse, end_verse + 1) if v not in books[book_id][chapter_start]]
                if missing:
                    issues.append(f"missing verse(s) {book_id} {chapter_start}:{missing[:12]}")
            else:
                for chapter in range(chapter_start, end_chapter + 1):
                    lo = start_verse if chapter == chapter_start else min(books[book_id][chapter])
                    hi = end_verse if chapter == end_chapter else max(books[book_id][chapter])
                    missing = [v for v in range(lo, hi + 1) if v not in books[book_id][chapter]]
                    if missing:
                        issues.append(f"missing verse(s) {book_id} {chapter}:{missing[:12]}")
    return issues

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--source-zip", type=Path, required=True)
    args = parser.parse_args()
    archive_bytes = args.source_zip.read_bytes()
    if digest(archive_bytes) != ARCHIVE_SHA256:
        print("FAIL: source archive SHA-256 differs from Owner-accepted immutable baseline.", file=sys.stderr)
        return 1
    try:
        with zipfile.ZipFile(args.source_zip) as archive:
            corrupt = archive.testzip()
            if corrupt:
                raise ValueError(f"corrupt ZIP member {corrupt}")
            books = {}
            for name in archive.namelist():
                match = BOOK_FILE.fullmatch(Path(name).name)
                if match:
                    book_id = match.group(1)
                    if book_id in books:
                        raise ValueError(f"duplicate source book {book_id}")
                    books[book_id] = parse_usfm(archive.read(name), book_id)
            genesis_name = next(name for name in archive.namelist() if Path(name).name == "02-GENmdyeth.usfm")
            genesis_bytes = archive.read(genesis_name)
    except (OSError, zipfile.BadZipFile, UnicodeError, ValueError, StopIteration) as exc:
        print(f"FAIL: source archive parsing: {exc}", file=sys.stderr)
        return 1
    if digest(genesis_bytes) != GENESIS_SHA256:
        print("FAIL: Genesis USFM hash differs from Owner-accepted baseline.", file=sys.stderr)
        return 1
    if len(books) != 66:
        print(f"FAIL: expected 66 source books, found {len(books)}.", file=sys.stderr)
        return 1
    try:
        package = json.loads(PACKAGE.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        print(f"FAIL: Genesis package: {exc}", file=sys.stderr)
        return 1
    references: set[str] = set()
    collect_references(package.get("locales", {}), references)
    problems = []
    for reference in sorted(references):
        problems.extend(f"{reference}: {issue}" for issue in validate_one(reference, books))
    if problems:
        print("\n".join(problems), file=sys.stderr)
        print(f"FAIL: {len(problems)} issue(s) across {len(references)} distinct references.", file=sys.stderr)
        return 1
    print(f"PASS: {len(references)} distinct references validated against all {len(books)} books in the pinned 2008 source.")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
