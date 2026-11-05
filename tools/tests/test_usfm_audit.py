import json
import tempfile
import unittest
from pathlib import Path

from tools.usfm_audit import audit_manifest, inspect_usfm


class UsfmAuditTests(unittest.TestCase):
    def test_synthetic_usfm_structure_and_ethiopic_codepoints(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "synthetic.usfm"
            path.write_text("\\id GEN Synthetic fixture\n\\c 1\n\\v 1 SYNTHETIC_FIXTURE ሀ\n", encoding="utf-8")
            result = inspect_usfm(path)
            self.assertEqual(result["status"], "PASS")
            self.assertEqual(result["book_ids"], ["GEN"])
            self.assertEqual(result["chapters_by_book"], {"GEN": [1]})
            self.assertEqual(result["verse_marker_counts_by_book_and_chapter"], {"GEN:1": 1})
            self.assertEqual(result["ethiopic_codepoint_count"], 1)

    def test_verse_before_chapter_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "synthetic.usfm"
            path.write_text("\\id GEN Synthetic fixture\n\\v 1 PLACEHOLDER\n", encoding="utf-8")
            result = inspect_usfm(path)
            self.assertEqual(result["status"], "FAIL")
            self.assertTrue(any("before a valid book and chapter" in error for error in result["errors"]))

    def test_same_chapter_number_is_valid_in_different_books(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "multi-book.usfm"
            path.write_text(
                "\\id GEN Synthetic Genesis fixture\n\\c 1\n\\v 1 PLACEHOLDER\n"
                "\\id EXO Synthetic Exodus fixture\n\\c 1\n\\v 1 PLACEHOLDER\n",
                encoding="utf-8",
            )
            result = inspect_usfm(path)
            self.assertEqual(result["status"], "PASS")
            self.assertEqual(result["chapters_by_book"], {"GEN": [1], "EXO": [1]})
            self.assertEqual(
                result["verse_marker_counts_by_book_and_chapter"],
                {"GEN:1": 1, "EXO:1": 1},
            )

    def test_duplicate_chapter_within_same_book_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "duplicate-chapter.usfm"
            path.write_text(
                "\\id GEN Synthetic fixture\n\\c 1\n\\v 1 PLACEHOLDER\n"
                "\\c 1\n\\v 2 PLACEHOLDER\n",
                encoding="utf-8",
            )
            result = inspect_usfm(path)
            self.assertEqual(result["status"], "FAIL")
            self.assertTrue(any("duplicate chapter number in book GEN" in error for error in result["errors"]))

    def test_chapter_before_book_id_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "chapter-before-id.usfm"
            path.write_text("\\c 1\n\\v 1 PLACEHOLDER\n", encoding="utf-8")
            result = inspect_usfm(path)
            self.assertEqual(result["status"], "FAIL")
            self.assertTrue(any("chapter appears before a book ID" in error for error in result["errors"]))

    def test_manifest_hash_and_rights_gate(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "source"
            source.mkdir()
            usfm = source / "fixture.usfm"
            usfm.write_text("\\id GEN Synthetic fixture\n\\c 1\n\\v 1 PLACEHOLDER\n", encoding="utf-8")
            import hashlib
            digest = hashlib.sha256(usfm.read_bytes()).hexdigest()
            manifest = {
                "schema_version": "1.0.0",
                "source_id": "synthetic-test-only",
                "rights": {"status": "UNVERIFIED"},
                "files": [{"path": "fixture.usfm", "sha256": digest, "format": "USFM"}]
            }
            manifest_path = root / "manifest.json"
            manifest_path.write_text(json.dumps(manifest), encoding="utf-8")
            report = audit_manifest(manifest_path, source)
            self.assertEqual(report["report_version"], "1.1.0")
            self.assertEqual(report["structural_audit"], "PASS")
            self.assertEqual(report["rights_gate"], "BLOCKED")
            self.assertFalse(report["content_intake_authorized"])


if __name__ == "__main__":
    unittest.main()
