import hashlib
import json
import tempfile
import unittest
from pathlib import Path

from tools.usfm_audit import audit_manifest, inspect_usfm


def write_usfm(path: Path, book_id: str, chapter_numbers: tuple[int, ...] = (1,)) -> str:
    lines = [f"\\id {book_id} Synthetic fixture"]
    for chapter in chapter_numbers:
        lines.extend((f"\\c {chapter}", "\\v 1 PLACEHOLDER"))
    content = "\n".join(lines) + "\n"
    path.write_text(content, encoding="utf-8")
    return hashlib.sha256(path.read_bytes()).hexdigest()


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
            self.assertEqual(result["issues"], [])

    def test_verse_before_chapter_fails_with_stable_code(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "synthetic.usfm"
            path.write_text("\\id GEN Synthetic fixture\n\\v 1 PLACEHOLDER\n", encoding="utf-8")
            result = inspect_usfm(path)
            self.assertEqual(result["status"], "FAIL")
            self.assertIn("USFM_VERSE_BEFORE_BOOK_OR_CHAPTER", {issue["code"] for issue in result["issues"]})

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
            self.assertEqual(result["verse_marker_counts_by_book_and_chapter"], {"GEN:1": 1, "EXO:1": 1})

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
            self.assertIn("USFM_DUPLICATE_CHAPTER", {issue["code"] for issue in result["issues"]})

    def test_chapter_before_book_id_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "chapter-before-id.usfm"
            path.write_text("\\c 1\n\\v 1 PLACEHOLDER\n", encoding="utf-8")
            result = inspect_usfm(path)
            self.assertEqual(result["status"], "FAIL")
            self.assertIn("USFM_CHAPTER_BEFORE_BOOK", {issue["code"] for issue in result["issues"]})

    def test_manifest_hash_and_rights_gate_remain_blocked(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "source"
            source.mkdir()
            digest = write_usfm(source / "fixture.usfm", "GEN")
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

    def test_duplicate_book_id_across_manifest_files_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "source"
            source.mkdir()
            first_hash = write_usfm(source / "first.usfm", "GEN")
            second_hash = write_usfm(source / "second.usfm", "GEN", (2,))
            manifest = {
                "schema_version": "1.0.0",
                "source_id": "synthetic-test-only",
                "rights": {"status": "UNVERIFIED"},
                "files": [
                    {"path": "first.usfm", "sha256": first_hash, "format": "USFM"},
                    {"path": "second.usfm", "sha256": second_hash, "format": "USFM"},
                ],
            }
            manifest_path = root / "manifest.json"
            manifest_path.write_text(json.dumps(manifest), encoding="utf-8")
            report = audit_manifest(manifest_path, source)
            self.assertEqual(report["structural_audit"], "FAIL")
            self.assertIn("PACKAGE_DUPLICATE_BOOK_ID", {issue["code"] for issue in report["package_issues"]})

    def test_manifest_declared_book_id_must_match_file(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "source"
            source.mkdir()
            digest = write_usfm(source / "fixture.usfm", "GEN")
            manifest = {
                "schema_version": "1.0.0",
                "source_id": "synthetic-test-only",
                "rights": {"status": "UNVERIFIED"},
                "files": [{"path": "fixture.usfm", "sha256": digest, "format": "USFM", "book_id": "EXO"}],
            }
            manifest_path = root / "manifest.json"
            manifest_path.write_text(json.dumps(manifest), encoding="utf-8")
            report = audit_manifest(manifest_path, source)
            codes = {issue["code"] for issue in report["file_results"][0]["issues"]}
            self.assertIn("MANIFEST_BOOK_ID_MISMATCH", codes)
            self.assertEqual(report["structural_audit"], "FAIL")

    def test_expected_inventory_passes_when_book_and_chapters_match(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "source"
            source.mkdir()
            digest = write_usfm(source / "fixture.usfm", "GEN", (1, 2))
            manifest = {
                "schema_version": "1.0.0",
                "source_id": "synthetic-test-only",
                "rights": {"status": "UNVERIFIED"},
                "files": [{"path": "fixture.usfm", "sha256": digest, "format": "USFM"}],
            }
            manifest_path = root / "manifest.json"
            manifest_path.write_text(json.dumps(manifest), encoding="utf-8")
            inventory_path = root / "inventory.json"
            inventory_path.write_text(json.dumps({
                "schema_version": "1.0.0",
                "inventory_id": "synthetic-reference",
                "reference": "synthetic fixture specification",
                "review_status": "APPROVED",
                "books": [{"book_id": "GEN", "chapters": [1, 2]}],
            }), encoding="utf-8")
            report = audit_manifest(manifest_path, source, inventory_path)
            self.assertTrue(report["expected_inventory_checked"])
            self.assertEqual(report["package_issues"], [])
            self.assertEqual(report["structural_audit"], "PASS")

    def test_expected_inventory_detects_missing_and_unexpected_chapters(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            source = root / "source"
            source.mkdir()
            digest = write_usfm(source / "fixture.usfm", "GEN", (1, 3))
            manifest = {
                "schema_version": "1.0.0",
                "source_id": "synthetic-test-only",
                "rights": {"status": "UNVERIFIED"},
                "files": [{"path": "fixture.usfm", "sha256": digest, "format": "USFM"}],
            }
            manifest_path = root / "manifest.json"
            manifest_path.write_text(json.dumps(manifest), encoding="utf-8")
            inventory_path = root / "inventory.json"
            inventory_path.write_text(json.dumps({
                "schema_version": "1.0.0",
                "inventory_id": "synthetic-reference",
                "books": [{"book_id": "GEN", "chapters": [1, 2]}],
            }), encoding="utf-8")
            report = audit_manifest(manifest_path, source, inventory_path)
            codes = {issue["code"] for issue in report["package_issues"]}
            self.assertEqual(codes, {"INVENTORY_MISSING_CHAPTER", "INVENTORY_UNEXPECTED_CHAPTER"})
            self.assertEqual(report["structural_audit"], "FAIL")

    def test_expected_inventory_rejects_duplicate_book_ids(self):
        with tempfile.TemporaryDirectory() as directory:
            inventory_path = Path(directory) / "inventory.json"
            inventory_path.write_text(json.dumps({
                "schema_version": "1.0.0",
                "reference": "synthetic fixture specification",
                "review_status": "APPROVED",
                "books": [
                    {"book_id": "GEN", "chapters": [1]},
                    {"book_id": "GEN", "chapters": [1]},
                ],
            }), encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "Duplicate expected inventory book_id"):
                from tools.usfm_audit import _load_expected_inventory
                _load_expected_inventory(inventory_path)


if __name__ == "__main__":
    unittest.main()
