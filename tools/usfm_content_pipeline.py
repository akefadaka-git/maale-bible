#!/usr/bin/env python3
"""Build a private, provenance-preserving SQLite/FTS5 database from an authorized USFM ZIP.

The tool emits no scripture text to stdout/stderr. Keep the source archive and
resulting database outside the public repository. Python 3.10+, stdlib only.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import sqlite3
import stat
import sys
import zipfile
from dataclasses import dataclass
from pathlib import Path, PurePosixPath
from typing import Iterable
MARKER_RE = re.compile(r"\\([+]?[A-Za-z0-9-]+)(\*)?")
ID_RE = re.compile(r"^\\id\s+([A-Z0-9]{3})(?:\s|$)")
CHAPTER_RE = re.compile(r"^\\c\s+(\d+)(?:\s|$)")
VERSE_RE = re.compile(r"^\\v\s+(\d+[a-z]?)(?:-(\d+[a-z]?))?(?:\s+|$)(.*)$")
EXCLUDED_BLOCKS = ("f", "x", "fig")
HEADING_MARKERS = {
    "d", "h", "imt1", "ip", "iot", "io1", "io2", "is2", "mt1", "mt2",
    "mr", "ms1", "r", "s1", "s2", "sp", "toc1", "toc2", "toc3",
}
PARAGRAPH_MARKERS = {"p", "m", "q1", "q2", "b", "ili"}
KNOWN_MARKERS = (
    HEADING_MARKERS | PARAGRAPH_MARKERS | {"id", "c", "v", "f", "x", "fig",
    "fr", "fq", "ft", "xo", "xt", "add"}
)

@dataclass(frozen=True)
class VerseRecord:
    book_id: str
    chapter: int
    verse_ref: str
    display_text: str
    search_text: str


def sha256_bytes(data: bytes) -> str:
    return hashlib.sha256(data).hexdigest()


def _strip_excluded(text: str) -> str:
    for marker in EXCLUDED_BLOCKS:
        opening = len(re.findall(rf"\\{marker}(?![A-Za-z0-9*])", text))
        closing = len(re.findall(rf"\\{marker}\*", text))
        if opening != closing:
            raise ValueError("unbalanced excluded USFM block")
        text = re.sub(
            rf"\\{marker}\b.*?\\{marker}\*",
            " ",
            text,
            flags=re.DOTALL,
        )
    return text


def _plain_text(text: str) -> str:
    text = _strip_excluded(text)
    text = MARKER_RE.sub("", text)
    text = re.sub(r"[ \t]+", " ", text)
    return text.strip()


def parse_usfm(text: str, expected_book_id: str) -> list[VerseRecord]:
    """Conservative verse parser; unsupported markers fail closed."""
    # Remove complete note, cross-reference and figure blocks across line boundaries.
    text = _strip_excluded(text)
    current_book: str | None = None
    current_chapter: int | None = None
    current_ref: str | None = None
    current_parts: list[str] = []
    records: list[VerseRecord] = []
    seen_refs: set[tuple[int, str]] = set()
    last_verse_by_chapter: dict[int, tuple[tuple[int, str], str]] = {}

    def flush() -> None:
        nonlocal current_ref, current_parts
        if current_book is not None and current_chapter is not None and current_ref is not None:
            display = "\n".join(part for part in current_parts if part).strip()
            records.append(VerseRecord(
                current_book, current_chapter, current_ref, display,
                __import__("unicodedata").normalize("NFKC", display).casefold(),
            ))
        current_ref = None
        current_parts = []

    for line_number, raw_line in enumerate(text.lstrip("\ufeff").splitlines(), start=1):
        line = raw_line.strip()
        if not line:
            continue
        match = ID_RE.match(line)
        if match:
            flush()
            if current_book is not None:
                raise ValueError(f"multiple book IDs in one file at line {line_number}")
            current_book = match.group(1)
            if current_book != expected_book_id:
                raise ValueError(f"book ID does not match manifest at line {line_number}")
            continue
        match = CHAPTER_RE.match(line)
        if match:
            flush()
            if current_book is None:
                raise ValueError(f"chapter before book ID at line {line_number}")
            chapter = int(match.group(1))
            if chapter < 1:
                raise ValueError(f"invalid chapter number at line {line_number}")
            if current_chapter is not None and chapter != current_chapter + 1:
                raise ValueError(f"non-contiguous chapter sequence at line {line_number}")
            current_chapter = chapter
            continue
        match = VERSE_RE.match(line)
        if match:
            flush()
            if current_book is None or current_chapter is None:
                raise ValueError(f"verse before book/chapter at line {line_number}")
            start_ref, end_ref, body = match.groups()
            verse_ref = start_ref if end_ref is None else f"{start_ref}-{end_ref}"
            key = (current_chapter, verse_ref)
            if key in seen_refs:
                raise ValueError(f"duplicate verse reference at line {line_number}")
            start_match = re.fullmatch(r"(\d+)([a-z]?)", start_ref)
            end_token = end_ref or start_ref
            end_match = re.fullmatch(r"(\d+)([a-z]?)", end_token)
            start_key = (int(start_match.group(1)), start_match.group(2))
            end_key = (int(end_match.group(1)), end_match.group(2))
            if end_key < start_key:
                raise ValueError(f"descending verse range at line {line_number}")
            previous = last_verse_by_chapter.get(current_chapter)
            if previous and start_key <= previous[0]:
                raise ValueError(f"non-increasing verse sequence at line {line_number}")
            last_verse_by_chapter[current_chapter] = (end_key, verse_ref)
            seen_refs.add(key)
            current_ref = verse_ref
            cleaned = _plain_text(body)
            if cleaned:
                current_parts.append(cleaned)
            continue

        markers = MARKER_RE.findall(line)
        marker_names = [name.lstrip("+") for name, _ in markers]
        unknown = sorted(set(marker_names) - KNOWN_MARKERS)
        if unknown:
            raise ValueError(f"unsupported USFM marker at line {line_number}")
        if marker_names:
            first = marker_names[0]
            if first in HEADING_MARKERS or first in {"id", "c", "v", "f", "x", "fig"}:
                continue
            if first in PARAGRAPH_MARKERS:
                remainder = MARKER_RE.sub("", line, count=1)
                cleaned = _plain_text(remainder)
                if current_ref is not None and cleaned:
                    current_parts.append(cleaned)
                continue
            # Footnote/cross-reference fragments are excluded by _plain_text.
            cleaned = _plain_text(line)
            if current_ref is not None and cleaned:
                current_parts.append(cleaned)
            continue

        cleaned = _plain_text(line)
        if current_ref is not None and cleaned:
            current_parts.append(cleaned)

    flush()
    if current_book != expected_book_id:
        raise ValueError("missing or mismatched book ID")
    if not records:
        raise ValueError("book contains no verse records")
    return records


def _safe_zip_preflight(archive: zipfile.ZipFile, expected_paths: set[str]) -> list[str]:
    names: set[str] = set()
    usfm_names: set[str] = set()
    total = 0
    for info in archive.infolist():
        name = info.filename
        path = PurePosixPath(name)
        mode = (info.external_attr >> 16) & 0xFFFF
        if (not name or path.is_absolute() or ".." in path.parts or
                "\\" in name or ":" in name or name in names):
            raise ValueError("unsafe or duplicate ZIP member path")
        names.add(name)
        if info.flag_bits & 0x1:
            raise ValueError("encrypted ZIP member")
        file_type = stat.S_IFMT(mode)
        if file_type not in (0, stat.S_IFREG, stat.S_IFDIR):
            raise ValueError("special ZIP member")
        if name.lower().endswith((".zip", ".7z", ".rar", ".tar", ".gz")):
            raise ValueError("nested archive member")
        total += info.file_size
        if info.file_size > 10_000_000 or total > 20_000_000:
            raise ValueError("ZIP expansion limit exceeded")
        if info.compress_size and info.file_size / info.compress_size > 1000:
            raise ValueError("ZIP compression ratio limit exceeded")
        if name.lower().endswith(".usfm"):
            usfm_names.add(name)
    if usfm_names != expected_paths:
        raise ValueError("USFM member inventory does not match source manifest")
    corrupt = archive.testzip()
    if corrupt is not None:
        raise ValueError("ZIP CRC validation failed")
    return sorted(names)


def _create_database(output: Path, edition_id: str, source_sha: str,
                     books: list[dict], rows: list[tuple]) -> dict:
    if output.exists():
        raise FileExistsError("refusing to overwrite an existing database")
    output.parent.mkdir(parents=True, exist_ok=True)
    db = sqlite3.connect(output)
    try:
        db.execute("PRAGMA page_size=4096")
        db.execute("PRAGMA journal_mode=DELETE")
        db.execute("PRAGMA synchronous=FULL")
        db.execute("PRAGMA foreign_keys=ON")
        db.execute("CREATE TABLE metadata (key TEXT PRIMARY KEY, value TEXT NOT NULL)")
        db.execute("""CREATE TABLE books (
            book_order INTEGER PRIMARY KEY,
            book_id TEXT NOT NULL UNIQUE,
            canonical_name TEXT NOT NULL
        )""")
        db.execute("""CREATE TABLE verses (
            id INTEGER PRIMARY KEY,
            edition_id TEXT NOT NULL,
            book_id TEXT NOT NULL REFERENCES books(book_id),
            chapter INTEGER NOT NULL CHECK(chapter > 0),
            verse_ref TEXT NOT NULL,
            source_path TEXT NOT NULL,
            source_sha256 TEXT NOT NULL,
            parser_version TEXT NOT NULL,
            display_text TEXT NOT NULL,
            search_text TEXT NOT NULL,
            UNIQUE(edition_id, book_id, chapter, verse_ref)
        )""")
        db.execute("""CREATE VIRTUAL TABLE verse_fts USING fts5(
            search_text, content='verses', content_rowid='id',
            tokenize='unicode61 remove_diacritics 0'
        )""")
        metadata = {
            "schema_version": SCHEMA_VERSION,
            "parser_version": PARSER_VERSION,
            "edition_id": edition_id,
            "source_archive_sha256": source_sha,
            "fts_tokenizer": "unicode61 remove_diacritics 0",
            "tokenizer_qualification": "ENGINEERING_BASELINE_NOT_LINGUISTICALLY_QUALIFIED",
        }
        db.executemany("INSERT INTO metadata(key,value) VALUES (?,?)", sorted(metadata.items()))
        db.executemany(
            "INSERT INTO books(book_order,book_id,canonical_name) VALUES (?,?,?)",
            [(b["order"], b["usfm_id"], b["canonical_name"]) for b in books],
        )
        db.executemany("""INSERT INTO verses(
            edition_id,book_id,chapter,verse_ref,source_path,source_sha256,
            parser_version,display_text,search_text
        ) VALUES (?,?,?,?,?,?,?,?,?)""", rows)
        db.execute("INSERT INTO verse_fts(verse_fts) VALUES ('rebuild')")
        db.execute("PRAGMA user_version=1")
        db.commit()
        db.execute("VACUUM")
    finally:
        db.close()
    check = sqlite3.connect(output)
    try:
        count = check.execute("SELECT count(*) FROM verses").fetchone()[0]
        fts_count = check.execute("SELECT count(*) FROM verse_fts").fetchone()[0]
        if count != len(rows) or fts_count != count:
            raise ValueError("database/FTS row-count reconciliation failed")
    finally:
        check.close()
    return {"verse_records": len(rows), "fts_records": len(rows),
            "database_bytes": output.stat().st_size,
            "database_sha256": sha256_bytes(output.read_bytes())}


def build_from_archive(archive_path: Path, manifest_path: Path,
                       registry_path: Path, output_path: Path) -> dict:
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    registry = json.loads(registry_path.read_text(encoding="utf-8"))
    source = manifest["source"]
    expected_archive_sha = source["archive_sha256"]
    archive_bytes = archive_path.read_bytes()
    actual_archive_sha = sha256_bytes(archive_bytes)
    if actual_archive_sha != expected_archive_sha:
        raise ValueError("source archive SHA-256 mismatch")
    if len(archive_bytes) != source.get("archive_bytes"):
        raise ValueError("source archive byte-size mismatch")
    repository_root = Path.cwd().resolve()
    if output_path.resolve().is_relative_to(repository_root):
        raise ValueError("private database output must be outside the repository")
    source_files = manifest["source_files"]
    expected = {item["path"]: item for item in source_files}
    if len(expected) != len(source_files) or not expected:
        raise ValueError("invalid or duplicate source manifest paths")
    books = registry["books"]
    book_ids = [book["usfm_id"] for book in books]
    if len(book_ids) != len(set(book_ids)) or len(book_ids) != len(source_files):
        raise ValueError("canonical registry/source inventory cardinality mismatch")
    if set(book_ids) != {item["book_id"] for item in source_files}:
        raise ValueError("canonical registry/source book IDs mismatch")

    rows: list[tuple] = []
    observed_books: set[str] = set()
    with zipfile.ZipFile(archive_path, "r") as archive:
        members = _safe_zip_preflight(archive, set(expected))
        for book in books:
            item = next(x for x in source_files if x["book_id"] == book["usfm_id"])
            info = archive.getinfo(item["path"])
            data = archive.read(info)
            if len(data) != item["bytes"] or sha256_bytes(data) != item["sha256"]:
                raise ValueError("source member hash or size mismatch")
            text = data.decode("utf-8-sig", errors="strict")
            parsed = parse_usfm(text, item["book_id"])
            observed_books.add(item["book_id"])
            for record in parsed:
                rows.append((
                    source["edition_id"], record.book_id, record.chapter,
                    record.verse_ref, item["path"], item["sha256"],
                    PARSER_VERSION, record.display_text, record.search_text,
                ))
    if observed_books != set(book_ids):
        raise ValueError("not all canonical books were parsed")
    chapter_count = len({(row[1], row[2]) for row in rows})
    represented_spans = 0
    verse_range_count = 0
    for row in rows:
        match = re.fullmatch(r"(\d+)[a-z]?(?:-(\d+)[a-z]?)?", row[3])
        start = int(match.group(1))
        end = int(match.group(2) or match.group(1))
        represented_spans += end - start + 1
        verse_range_count += int(match.group(2) is not None)
    structural = manifest.get("structural_validation", {})
    if chapter_count != structural.get("chapter_count"):
        raise ValueError("chapter count does not match validated source baseline")
    if len(rows) != structural.get("verse_marker_lines"):
        raise ValueError("verse-marker count does not match validated source baseline")
    if represented_spans != structural.get("represented_verse_spans_including_ranges"):
        raise ValueError("represented verse-span count does not match validated source baseline")
    if verse_range_count != structural.get("verse_range_marker_instances"):
        raise ValueError("verse-range count does not match validated source baseline")
    db_report = _create_database(output_path, source["edition_id"],
                                 expected_archive_sha, books, rows)
    return {
        "status": "PASS",
        "source_id": source["edition_id"],
        "archive_bytes": len(archive_bytes),
        "archive_sha256": actual_archive_sha,
        "zip_member_count": len(members),
        "usfm_file_count": len(source_files),
        "book_count": len(observed_books),
        "chapter_count": chapter_count,
        "verse_record_count": len(rows),
        "represented_verse_spans": represented_spans,
        "verse_range_count": verse_range_count,
        "database": db_report,
        "scripture_text_emitted": False,
        "tokenizer_qualification": "NOT_LINGUISTICALLY_QUALIFIED",
    }


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--archive", required=True, type=Path)
    parser.add_argument("--manifest", required=True, type=Path)
    parser.add_argument("--registry", required=True, type=Path)
    parser.add_argument("--output", required=True, type=Path)
    parser.add_argument("--report", type=Path)
    args = parser.parse_args()
    try:
        result = build_from_archive(args.archive, args.manifest, args.registry, args.output)
    except (OSError, ValueError, KeyError, zipfile.BadZipFile, sqlite3.Error,
            UnicodeDecodeError, json.JSONDecodeError) as exc:
        print(f"CONTENT PIPELINE FAILED: {type(exc).__name__}", file=sys.stderr)
        return 2
    rendered = json.dumps(result, ensure_ascii=False, indent=2) + "\n"
    if args.report:
        args.report.write_text(rendered, encoding="utf-8")
    else:
        print(rendered, end="")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
