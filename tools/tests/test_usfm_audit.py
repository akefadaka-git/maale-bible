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
            self.assertEqual(result["chapter_numbers"], [1])
            self.assertEqual(result["ethiopic_codepoint_count"], 1)

    def test_verse_before_chapter_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "synthetic.usfm"
            path.write_text("\\id GEN Synthetic fixture\n\\v 1 PLACEHOLDER\n", encoding="utf-8")
            result = inspect_usfm(path)
            self.assertEqual(result["status"], "FAIL")
            self.assertTrue(any("before a chapter" in error for error in result["errors"]))

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
            self.assertEqual(report["structural_audit"], "PASS")
            self.assertEqual(report["rights_gate"], "BLOCKED")
            self.assertFalse(report["content_intake_authorized"])


if __name__ == "__main__":
    unittest.main()
