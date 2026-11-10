import hashlib
import json
import sqlite3
import tempfile
import unittest
import zipfile
from pathlib import Path

from tools.usfm_content_pipeline import build_from_archive, parse_usfm


class UsfmContentPipelineTests(unittest.TestCase):
    def test_parser_keeps_verse_ranges_and_excludes_notes_and_headings(self):
        source = (
            "\\id GEN Synthetic fixture\n"
            "\\c 1\n"
            "\\p\n"
            "\\v 1 SYNTHETIC_ALPHA \\f +\n"
            "\\fr 1:1\n"
            "\\ft SYNTHETIC_PRIVATE_NOTE\\f*\n"
            "\\x +\n"
            "\\xo 1:1\n"
            "\\xt SYNTHETIC_CROSS_REFERENCE\\x*\n"
            "\\q1 SYNTHETIC_CONTINUATION\n"
            "\\s1 SYNTHETIC_HEADING_NOT_VERSE\n"
            "\\v 2-3 SYNTHETIC_BETA \\add SYNTHETIC_EMPHASIS\\add*\n"
        )
        records = parse_usfm(source, "GEN")
        self.assertEqual([r.verse_ref for r in records], ["1", "2-3"])
        self.assertEqual(records[0].display_text, "SYNTHETIC_ALPHA\nSYNTHETIC_CONTINUATION")
        self.assertIn("SYNTHETIC_EMPHASIS", records[1].display_text)
        joined = " ".join(r.display_text for r in records)
        self.assertNotIn("SYNTHETIC_PRIVATE_NOTE", joined)
        self.assertNotIn("SYNTHETIC_CROSS_REFERENCE", joined)
        self.assertNotIn("SYNTHETIC_HEADING_NOT_VERSE", joined)

    def test_parser_fails_closed_on_unbalanced_note_blocks(self):
        source = "\\id GEN Synthetic\n\\c 1\n\\v 1 SYNTHETIC \\f +\n\\ft UNBALANCED\n"
        with self.assertRaisesRegex(ValueError, "unbalanced excluded USFM block"):
            parse_usfm(source, "GEN")

    def test_parser_rejects_unsupported_markers(self):
        source = "\\id GEN Synthetic\n\\c 1\n\\v 1 SYNTHETIC\n\\zunknown VALUE\n"
        with self.assertRaisesRegex(ValueError, "unsupported USFM marker"):
            parse_usfm(source, "GEN")

    def test_parser_rejects_duplicate_and_non_increasing_verse_references(self):
        duplicate = "\\id GEN Synthetic\n\\c 1\n\\v 1 A\n\\v 1 B\n"
        with self.assertRaisesRegex(ValueError, "duplicate verse reference|non-increasing"):
            parse_usfm(duplicate, "GEN")
        backwards = "\\id GEN Synthetic\n\\c 1\n\\v 2 A\n\\v 1 B\n"
        with self.assertRaisesRegex(ValueError, "non-increasing"):
            parse_usfm(backwards, "GEN")

    def test_parser_rejects_book_identity_mismatch(self):
        with self.assertRaisesRegex(ValueError, "book ID does not match"):
            parse_usfm("\\id EXO Synthetic\n\\c 1\n\\v 1 SYNTHETIC\n", "GEN")

    def test_private_database_builds_and_fts_finds_synthetic_content(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            archive_path = root / "synthetic.zip"
            source_files = {
                "01-GEN.usfm": b"\\id GEN Synthetic\n\\c 1\n\\v 1 SYNTHETIC_ALPHA\n",
                "02-EXO.usfm": b"\\id EXO Synthetic\n\\c 1\n\\v 1 SYNTHETIC_BETA\n",
            }
            with zipfile.ZipFile(archive_path, "w", compression=zipfile.ZIP_DEFLATED) as archive:
                for name, data in source_files.items():
                    archive.writestr(name, data)
            manifest = {
                "source": {
                    "edition_id": "synthetic-edition",
                    "archive_sha256": hashlib.sha256(archive_path.read_bytes()).hexdigest(),
                    "archive_bytes": archive_path.stat().st_size,
                },
                "structural_validation": {
                    "chapter_count": 2,
                    "verse_marker_lines": 2,
                    "represented_verse_spans_including_ranges": 2,
                    "verse_range_marker_instances": 0,
                },
                "source_files": [
                    {
                        "path": name,
                        "book_id": name[3:6],
                        "bytes": len(data),
                        "sha256": hashlib.sha256(data).hexdigest(),
                    }
                    for name, data in source_files.items()
                ],
            }
            registry = {
                "books": [
                    {"order": 1, "usfm_id": "GEN", "canonical_name": "Synthetic Genesis"},
                    {"order": 2, "usfm_id": "EXO", "canonical_name": "Synthetic Exodus"},
                ]
            }
            manifest_path = root / "manifest.json"
            registry_path = root / "registry.json"
            output_path = root / "private-content.sqlite"
            second_output_path = root / "private-content-second.sqlite"
            manifest_path.write_text(json.dumps(manifest), encoding="utf-8")
            registry_path.write_text(json.dumps(registry), encoding="utf-8")
            report = build_from_archive(archive_path, manifest_path, registry_path, output_path)
            second_report = build_from_archive(
                archive_path, manifest_path, registry_path, second_output_path
            )
            self.assertEqual(report["database"]["database_sha256"],
                             second_report["database"]["database_sha256"])
            self.assertEqual(report["status"], "PASS")
            self.assertEqual(report["book_count"], 2)
            self.assertEqual(report["verse_record_count"], 2)
            self.assertFalse(report["scripture_text_emitted"])
            db = sqlite3.connect(output_path)
            try:
                found = db.execute(
                    "SELECT count(*) FROM verse_fts WHERE verse_fts MATCH ?",
                    ("SYNTHETIC_ALPHA",),
                ).fetchone()[0]
                self.assertEqual(found, 1)
                metadata = dict(db.execute("SELECT key,value FROM metadata"))
                self.assertEqual(metadata["tokenizer_qualification"], "ENGINEERING_BASELINE_NOT_LINGUISTICALLY_QUALIFIED")
            finally:
                db.close()

    def test_archive_hash_mismatch_fails_before_import(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            archive_path = root / "empty.zip"
            with zipfile.ZipFile(archive_path, "w"):
                pass
            manifest_path = root / "manifest.json"
            registry_path = root / "registry.json"
            manifest_path.write_text(json.dumps({
                "source": {"edition_id": "synthetic", "archive_sha256": "0" * 64},
                "source_files": [{"path": "GEN.usfm", "book_id": "GEN", "bytes": 0, "sha256": "0" * 64}],
            }), encoding="utf-8")
            registry_path.write_text(json.dumps({
                "books": [{"order": 1, "usfm_id": "GEN", "canonical_name": "Synthetic"}]
            }), encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "archive SHA-256 mismatch"):
                build_from_archive(archive_path, manifest_path, registry_path, root / "out.sqlite")


if __name__ == "__main__":
    unittest.main()
