#!/usr/bin/env python3
"""Verify every Genesis learning-package scripture reference against the pinned 2008 MDYTWF USFM source.

The source archive is downloaded only into the CI workspace, SHA-256 pinned, and never committed.
"""
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
BOOK_FILE = re.compile(r"^\\d{2}-([A-Z0-9]{3})mdyeth\\.usfm$")
CHAPTER_MARKER = re.compile(r"^\\c\\s+(\\d+)")
VERSE_MARKER = re.compile(r"^\\v\\s+(\\d+[a-z]?)(?:-(\\d+[a-z]?))?")
REFERENCE_PART = re.compile(r"^([A-Z0-9]{3})?\\s*(\\d+)(?::(\\d+[a-z]?(?:-\\d+[a-z]?)?))?$")


def sha256(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def verse_number(label: str) -> int:
    match = re.match(r"\\d+", label)
    if not match:
        raise ValueError(f"invalid verse label: {label}")
    return int(match.group())


def parse_usfm(data: bytes, book_id: str) -> dict[int, set[int]]:
    text = data.decode("utf-8", errors="strict")
    chapters: dict[int, set[int]] = {}
    current = None
    for line in text.splitlines():
        cm = CHAPTER_MARKER.match(line)
        if cm:
            current = int(cm.group(1))
            chapters.setdefault(current, set())
            continue
        vm = VERSE_MARKER.match(line)
        if vm and current is not None:
            start = verse_number(vm.group(1))
            end = verse_number(vm.group(2)) if vm.group(2) else start
            if end < start:
                raise ValueError(f"{book_id} {current}: reversed verse range {vm.group(0)}")
            chapters[current].update(range(start, end + 1))
    if not chapters:
        raise ValueError(f"{book_id}: no chapter markers found")
    return chapters


def collect_references(node, output: set[str]) -> None:
    if isinstance(node, dict):
        for key, value in node.items():
            if key in {"references", "cross_references"} and isinstance(value, list):
                output.update(str(item).strip() for item in value if str(item).strip())
            elif key == "range" and isinstance(value, str):
                output.add(value.strip())
            collect_references(value, output)
    elif isinstance(node, list):
        for item in node:
            collect_references(item, output)


def expand_reference(reference: str, books: dict[str, dict[int, set[int]]]) -> list[str]:
    problems: list[str] = []
    inherited_book = None
    for raw_part in reference.split(";"):
        part = raw_part.strip()
        match = REFERENCE_PART.match(part)
        if not match:
            problems.append(f"unparsed reference segment: {part!r}")
            continue
        book_id = match.group(1) or inherited_book
        if not book_id:
            problems.append(f"reference has no book identifier: {part!r}")
            continue
        inherited_book = book_id
        if book_id not in books:
            problems.append(f"book {book_id} is absent from the pinned 2008 source")
            continue
        chapter_start = int(match.group(2))
        chapter_end = chapter_start
        verse_expr = match.group(3)
        if verse_expr is None and "-" in part.split()[-1]:
            try:
                chapter_start, chapter_end = [int(x) for x in part.split()[-1].split("-", 1)]
            except Exception:
                pass
        if chapter_start not in books[book_id] or chapter_end not in books[book_id]:
            problems.append(f"chapter range {book_id} {chapter_start}-{chapter_end} is absent")
            continue
        if verse_expr:
            vm = re.fullmatch(r"(\\d+[a-z]?)(?:-(\\d+[a-z]?))?", verse_expr)
            if not vm:
                problems.append(f"invalid verse expression in {part!r}")
                continue
            vstart = verse_number(vm.group(1))
            vend = verse_number(vm.group(2)) if vm.group(2) else vstart
            if vstart not in books[book_id][chapter_start] or vend not in books[book_id][chapter_start]:
                problems.append(f"verse range {book_id} {chapter_start}:{vstart}-{vend} is absent")
                continue
            absent = [v for v in range(vstart, vend + 1) if v not in books[book_id][chapter_start]]
            if absent:
                problems.append(f"verse(s) {book_id} {chapter_start}:{absent[:12]} absent")
    return problems


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--source-zip", type=Path, required=True)
    args = parser.parse_args()
    archive_bytes = args.source_zip.read_bytes()
    if sha256(archive_bytes) != ARCHIVE_SHA256:
        print("FAIL: source archive SHA-256 does not match the Owner-accepted 2008 baseline.", file=sys.stderr)
        return 1
    try:
        with zipfile.ZipFile(args.source_zip) as archive:
            archive.testzip()
            books = {}
            for name in archive.namelist():
                match = BOOK_FILE.match(Path(name).name)
                if match:
                    book_id = match.group(1)
                    books[book_id] = parse_usfm(archive.read(name), book_id)
            genesis_name = next(name for name in archive.namelist() if Path(name).name == "02-GENmdyeth.usfm")
            genesis_bytes = archive.read(genesis_name)
    except (OSError, zipfile.BadZipFile, UnicodeError, ValueError, StopIteration) as exc:
        print(f"FAIL: unable to parse pinned source archive: {exc}", file=sys.stderr)
        return 1
    if sha256(genesis_bytes) != GENESIS_SHA256:
        print("FAIL: Genesis USFM SHA-256 differs from the Owner-accepted baseline.", file=sys.stderr)
        return 1
    try:
        package = json.loads(PACKAGE.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        print(f"FAIL: cannot read Genesis learning package: {exc}", file=sys.stderr)
        return 1
    refs: set[str] = set()
    collect_references(package.get("locales", {}), refs)
    problems = []
    for reference in sorted(refs):
        problems.extend(f"{reference}: {problem}" for problem in expand_reference(reference, books))
    if problems:
        print("\\n".join(problems), file=sys.stderr)
        print(f"FAIL: {len(problems)} scripture-reference issue(s) across {len(refs)} distinct references.", file=sys.stderr)
        return 1
    print(f"PASS: {len(refs)} distinct references validated against {len(books)} books in pinned MDYTWF source; Genesis SHA-256 matches.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
