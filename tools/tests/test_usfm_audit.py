import hashlib
import json
import tempfile
import unittest
from pathlib import Path

from tools.usfm_audit import audit_manifest, inspect_usfm


class UsfmAuditTests(unittest.TestCase):
    def test_synthetic_usfm_structure_and_ethiopic_codepoints(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "synthetic.usfm"
            path.write_text("\\id GEN Synthetic fixture\n\\c 1\n\\v 1 PLACEHOLDER ሀ\n", encoding="utf-8")
            result = inspect_usfm(path)
            self.assertEqual(result["status"], "PASS")
            self.assertEqual(result["book_ids"], ["GEN"])
            self.assertEqual(result["chapters_by_book"], {"GEN": [1]})
            self.assertEqual(result["verse_marker_counts_by_book_and_chapter"], {"GEN:1": 1})
            self.assertEqual(result["ethiopic_codepoint_count"], 1)

    def test_verse_before_chapter_has_stable_error_code(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "synthetic.usfm"
            path.write_text("\\id GEN Synthetic fixture\n\\v 1 PLACEHOLDER\n", encoding="utf-8")
            result = inspect_usfm(path)
            self.assertEqual(result["status"], "FAIL")
            self.assertIn("USFM_VERSE_BEFORE_BOOK_OR_CHAPTER", [item["code"] for item in result["issues"]])

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
            self.assertIn("USFM_CHAPTER_DUPLICATE_IN_BOOK", [item["code"] for item in result["issues"]])

    def test_chapter_before_book_id_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "chapter-before-id.usfm"
            path.write_text("\\c 1\n\\v 1 PLACEHOLDER\n", encoding="utf-8")
            result = inspect_usfm(path)
            self.assertEqual(result["status"], "FAIL")
            self.assertIn("USFM_CHAPTER_BEFORE_BOOK", [item["code"] for item in result["issues"]])

    def test_manifest_hash_and_rights_gate(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "source"
            source.mkdir()
            usfm = source / "fixture.usfm"
            usfm.write_text("\\id GEN Synthetic fixture\n\\c 1\n\\v 1 PLACEHOLDER\n", encoding="utf-8")
            digest = hashlib.sha256(usfm.read_bytes()).hexdigest()
            manifest = {
                "schema_version": "1.0.0",
                "source_id": "synthetic-test-only",
                "rights": {"status": "UNVERIFIED"},
                "files": [{"path": "fixture.usfm", "sha256": digest, "format": "USFM", "book_id": "GEN"}],
            }
            manifest_path = root / "manifest.json"
            manifest_path.write_text(json.dumps(manifest), encoding="utf-8")
            report = audit_manifest(manifest_path, source)
            self.assertEqual(report["report_version"], "1.2.0")
            self.assertEqual(report["structural_audit"], "PASS")
            self.assertEqual(report["rights_gate"], "BLOCKED")
            self.assertFalse(report["content_intake_authorized"])
            self.assertEqual(report["package_inventory"]["status"], "NOT_PROVIDED")

    def test_duplicate_book_id_across_package_files_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "source"
            source.mkdir()
            entries = []
            for filename in ("one.usfm", "two.usfm"):
                path = source / filename
                path.write_text("\\id GEN Synthetic fixture\n\\c 1\n\\v 1 PLACEHOLDER\n", encoding="utf-8")
                entries.append({"path": filename, "sha256": hashlib.sha256(path.read_bytes()).hexdigest(), "format": "USFM", "book_id": "GEN"})
            manifest_path = root / "manifest.json"
            manifest_path.write_text(json.dumps({"schema_version": "1.0.0", "source_id": "fixture", "rights": {"status": "UNVERIFIED"}, "files": entries}), encoding="utf-8")
            report = audit_manifest(manifest_path, source)
            self.assertEqual(report["structural_audit"], "FAIL")
            self.assertIn("PACKAGE_BOOK_ID_DUPLICATE_ACROSS_FILES", [item["code"] for file_result in report["file_results"] for item in file_result["issues"]])

    def test_expected_inventory_passes_without_changing_rights_gate(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "source"
            source.mkdir()
            path = source / "gen.usfm"
            path.write_text("\\id GEN Synthetic fixture\n\\c 1\n\\v 1 PLACEHOLDER\n\\c 2\n\\v 1 PLACEHOLDER\n", encoding="utf-8")
            manifest_path = root / "manifest.json"
            manifest_path.write_text(json.dumps({
                "schema_version": "1.0.0", "source_id": "fixture", "rights": {"status": "UNVERIFIED"},
                "files": [{"path": "gen.usfm", "sha256": hashlib.sha256(path.read_bytes()).hexdigest(), "format": "USFM", "book_id": "GEN"}],
            }), encoding="utf-8")
            inventory_path = root / "inventory.json"
            inventory_path.write_text(json.dumps({"schema_version": "1.0.0", "source_reference": "reviewed-reference-fixture", "owner_approval_reference": "owner-approval-fixture", "books": [{"book_id": "GEN", "chapters": [1, 2]}]}), encoding="utf-8")
            report = audit_manifest(manifest_path, source, inventory_path)
            self.assertEqual(report["package_inventory"]["status"], "PASS")
            self.assertEqual(report["rights_gate"], "BLOCKED")
            self.assertFalse(report["content_intake_authorized"])

    def test_expected_inventory_mismatch_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "source"
            source.mkdir()
            path = source / "gen.usfm"
            path.write_text("\\id GEN Synthetic fixture\n\\c 1\n\\v 1 PLACEHOLDER\n", encoding="utf-8")
            manifest_path = root / "manifest.json"
            manifest_path.write_text(json.dumps({
                "schema_version": "1.0.0", "source_id": "fixture", "rights": {"status": "UNVERIFIED"},
                "files": [{"path": "gen.usfm", "sha256": hashlib.sha256(path.read_bytes()).hexdigest(), "format": "USFM"}],
            }), encoding="utf-8")
            inventory_path = root / "inventory.json"
            inventory_path.write_text(json.dumps({"schema_version": "1.0.0", "source_reference": "fixture-reference", "owner_approval_reference": "owner-approval-fixture", "books": [{"book_id": "GEN", "chapters": [1, 2]}]}), encoding="utf-8")
            report = audit_manifest(manifest_path, source, inventory_path)
            self.assertEqual(report["structural_audit"], "FAIL")
            self.assertIn("INVENTORY_CHAPTER_MISSING", [item["code"] for item in report["issues"]])

    def test_invalid_inventory_is_reported_and_fails_closed(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "source"
            source.mkdir()
            path = source / "gen.usfm"
            path.write_text("\\id GEN Synthetic fixture\n\\c 1\n\\v 1 PLACEHOLDER\n", encoding="utf-8")
            manifest_path = root / "manifest.json"
            manifest_path.write_text(json.dumps({
                "schema_version": "1.0.0", "source_id": "fixture", "rights": {"status": "UNVERIFIED"},
                "files": [{"path": "gen.usfm", "sha256": hashlib.sha256(path.read_bytes()).hexdigest(), "format": "USFM"}],
            }), encoding="utf-8")
            inventory_path = root / "inventory.json"
            inventory_path.write_text(json.dumps({"schema_version": "1.0.0", "source_reference": "fixture-reference", "owner_approval_reference": "", "books": [{"book_id": "GEN", "chapters": [1]}]}), encoding="utf-8")
            report = audit_manifest(manifest_path, source, inventory_path)
            self.assertEqual(report["package_inventory"]["status"], "INVALID")
            self.assertEqual(report["structural_audit"], "FAIL")
            self.assertIn("INVENTORY_OWNER_APPROVAL_REFERENCE_MISSING", [item["code"] for item in report["issues"]])


if __name__ == "__main__":
    unittest.main()
