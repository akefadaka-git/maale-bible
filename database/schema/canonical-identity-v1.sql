-- Canonical identity schema v1 (SQLite; Android API 26+ compatible).
-- Metadata/identity only. No scripture text or licensed reference content.
PRAGMA foreign_keys = ON;

CREATE TABLE canon_registry (
  canon_id TEXT PRIMARY KEY NOT NULL,
  version INTEGER NOT NULL CHECK(version > 0),
  status TEXT NOT NULL CHECK(status IN ('FROZEN')),
  book_count INTEGER NOT NULL CHECK(book_count = 66),
  created_by TEXT NOT NULL,
  UNIQUE(canon_id, version)
);

CREATE TABLE canonical_book (
  canon_id TEXT NOT NULL,
  book_id TEXT NOT NULL CHECK(length(book_id) = 3 AND book_id = upper(book_id)),
  canonical_order INTEGER NOT NULL CHECK(canonical_order BETWEEN 1 AND 66),
  canonical_name TEXT NOT NULL,
  testament TEXT NOT NULL CHECK(testament IN ('OT','NT')),
  PRIMARY KEY(canon_id, book_id),
  UNIQUE(canon_id, canonical_order),
  FOREIGN KEY(canon_id) REFERENCES canon_registry(canon_id) ON UPDATE RESTRICT ON DELETE RESTRICT
);

CREATE TABLE canonical_passage (
  canon_id TEXT NOT NULL,
  book_id TEXT NOT NULL,
  chapter_number INTEGER NOT NULL CHECK(chapter_number > 0),
  verse_number INTEGER NOT NULL CHECK(verse_number > 0),
  segment TEXT NOT NULL DEFAULT '' CHECK(segment = '' OR (length(segment) <= 16 AND segment NOT GLOB '*[^A-Za-z0-9_-]*')),
  canonical_key TEXT NOT NULL UNIQUE,
  PRIMARY KEY(canon_id, book_id, chapter_number, verse_number, segment),
  FOREIGN KEY(canon_id, book_id) REFERENCES canonical_book(canon_id, book_id) ON UPDATE RESTRICT ON DELETE RESTRICT,
  CHECK(canonical_key = canon_id || '/' || book_id || '/' || chapter_number || '/' || verse_number ||
    CASE WHEN segment = '' THEN '' ELSE '/' || segment END)
);

CREATE TABLE edition (
  edition_id TEXT PRIMARY KEY NOT NULL,
  language_tag TEXT NOT NULL,
  title TEXT NOT NULL,
  publisher TEXT NOT NULL DEFAULT '',
  rights_statement TEXT NOT NULL DEFAULT '',
  attribution TEXT NOT NULL DEFAULT '',
  source_uri TEXT NOT NULL DEFAULT '',
  source_sha256 TEXT NOT NULL DEFAULT '' CHECK(source_sha256 = '' OR (length(source_sha256) = 64 AND source_sha256 NOT GLOB '*[^0-9a-f]*')),
  versification_profile TEXT NOT NULL,
  metadata_json TEXT NOT NULL DEFAULT '{}'
);

CREATE TABLE edition_passage (
  edition_passage_id TEXT PRIMARY KEY NOT NULL,
  edition_id TEXT NOT NULL,
  source_book_id TEXT NOT NULL,
  source_chapter TEXT NOT NULL,
  source_verse TEXT NOT NULL,
  source_file TEXT NOT NULL,
  source_file_sha256 TEXT NOT NULL CHECK(length(source_file_sha256) = 64 AND source_file_sha256 NOT GLOB '*[^0-9a-f]*'),
  source_marker TEXT NOT NULL DEFAULT '',
  parser_version TEXT NOT NULL,
  UNIQUE(edition_id, source_book_id, source_chapter, source_verse),
  FOREIGN KEY(edition_id) REFERENCES edition(edition_id) ON UPDATE RESTRICT ON DELETE CASCADE
);

CREATE TABLE passage_mapping (
  mapping_id TEXT PRIMARY KEY NOT NULL,
  edition_passage_id TEXT NOT NULL,
  mapping_status TEXT NOT NULL CHECK(mapping_status IN ('EXACT','SPLIT','MERGED','PARTIAL','UNMAPPED')),
  provenance TEXT NOT NULL,
  review_status TEXT NOT NULL CHECK(review_status IN ('UNREVIEWED','REVIEWED','APPROVED','REJECTED')),
  mapping_version TEXT NOT NULL,
  notes TEXT NOT NULL DEFAULT '',
  UNIQUE(mapping_id, mapping_status),
  FOREIGN KEY(edition_passage_id) REFERENCES edition_passage(edition_passage_id) ON UPDATE RESTRICT ON DELETE CASCADE
);

CREATE TABLE passage_mapping_target (
  mapping_id TEXT NOT NULL,
  canon_id TEXT NOT NULL,
  book_id TEXT NOT NULL,
  chapter_number INTEGER NOT NULL,
  verse_number INTEGER NOT NULL,
  segment TEXT NOT NULL DEFAULT '',
  target_order INTEGER NOT NULL CHECK(target_order > 0),
  PRIMARY KEY(mapping_id, target_order),
  UNIQUE(mapping_id, canon_id, book_id, chapter_number, verse_number, segment),
  FOREIGN KEY(mapping_id) REFERENCES passage_mapping(mapping_id) ON UPDATE RESTRICT ON DELETE CASCADE,
  FOREIGN KEY(canon_id, book_id, chapter_number, verse_number, segment)
    REFERENCES canonical_passage(canon_id, book_id, chapter_number, verse_number, segment)
    ON UPDATE RESTRICT ON DELETE RESTRICT
);

CREATE INDEX index_canonical_passage_book_chapter
  ON canonical_passage(canon_id, book_id, chapter_number, verse_number);
CREATE INDEX index_edition_passage_edition
  ON edition_passage(edition_id, source_book_id, source_chapter);
CREATE INDEX index_mapping_edition_passage
  ON passage_mapping(edition_passage_id);
CREATE INDEX index_mapping_target_canonical
  ON passage_mapping_target(canon_id, book_id, chapter_number, verse_number);

-- Frozen registry rows cannot be silently edited or removed.
CREATE TRIGGER canonical_book_no_update
BEFORE UPDATE ON canonical_book BEGIN SELECT RAISE(ABORT, 'canonical book registry is immutable'); END;
CREATE TRIGGER canonical_book_no_delete
BEFORE DELETE ON canonical_book BEGIN SELECT RAISE(ABORT, 'canonical book registry is immutable'); END;
CREATE TRIGGER canon_registry_no_update
BEFORE UPDATE ON canon_registry BEGIN SELECT RAISE(ABORT, 'canon registry is immutable'); END;
CREATE TRIGGER canon_registry_no_delete
BEFORE DELETE ON canon_registry BEGIN SELECT RAISE(ABORT, 'canon registry is immutable'); END;

-- Mapping cardinality is checked when a mapping is finalized by the application:
-- EXACT: exactly one source unit and one target; SPLIT: one source to >=2 targets;
-- MERGED: >=2 source units represented by one target (grouped via shared mapping);
-- PARTIAL: >=1 explicitly documented target; UNMAPPED: zero targets.
