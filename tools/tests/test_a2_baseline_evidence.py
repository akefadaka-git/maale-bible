import json
import unittest
from pathlib import Path, PurePosixPath

ROOT = Path(__file__).resolve().parents[2]
MANIFEST = ROOT / "content/manifests/maale-source-baseline-manifest.json"
HASHES = ROOT / "content/manifests/maale-usfm-file-hashes.json"
INVENTORY = ROOT / "content/manifests/candidate-book-inventory.json"
CLOSURE = ROOT / "docs/source-verification/A2_FINAL_CLOSURE_RECORD_2026-11-12.md"


class A2BaselineEvidenceTests(unittest.TestCase):
    @classmethod
    def setUpClass(cls):
        cls.manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
        cls.hashes = json.loads(HASHES.read_text(encoding="utf-8"))
        cls.inventory = json.loads(INVENTORY.read_text(encoding="utf-8"))

    def test_baseline_is_owner_accepted_with_explicit_exception(self):
        self.assertEqual(self.manifest["status"], "OWNER_ACCEPTED_WITH_EXPLICIT_SPECIALIST_REVIEW_EXCEPTION")
        self.assertTrue(self.manifest["acceptance"]["owner_accepted"])
        self.assertEqual(self.manifest["acceptance"]["specialist_review_status"], "DEFERRED_OWNER_ACCEPTED_RESIDUAL_RISK")
        self.assertFalse(self.manifest["acceptance"]["specialist_signoff_claimed"])

    def test_content_intake_and_public_scripture_remain_blocked(self):
        governance = self.manifest["governance"]
        self.assertFalse(governance["content_intake_authorized"])
        self.assertTrue(governance["public_repository_must_not_contain_raw_scripture"])
        self.assertTrue(governance["audio_and_third_party_assets_separately_gated"])

    def test_expected_inventory_and_source_hashes_match_exactly(self):
        expected = [item["usfm_id"] for item in self.inventory["books"]]
        actual = [item["book_id"] for item in self.hashes["files"]]
        self.assertEqual(len(expected), 66)
        self.assertEqual(len(set(expected)), 66)
        self.assertEqual(len(actual), 66)
        self.assertEqual(len(set(actual)), 66)
        self.assertEqual(set(expected), set(actual))
        self.assertEqual([item["order"] for item in self.inventory["books"]], list(range(1, 67)))

    def test_baseline_and_hash_manifest_are_consistent_and_metadata_only(self):
        baseline_files = self.manifest["files"]
        hash_files = self.hashes["files"]
        self.assertEqual(len(baseline_files), 66)
        self.assertEqual(len(hash_files), 66)
        baseline_by_path = {item["path"]: item for item in baseline_files}
        hash_by_path = {item["path"]: item for item in hash_files}
        self.assertEqual(set(baseline_by_path), set(hash_by_path))
        for path, item in baseline_by_path.items():
            digest_item = hash_by_path[path]
            self.assertEqual(item["book_id"], digest_item["book_id"])
            self.assertEqual(item["bytes"], digest_item["bytes"])
            self.assertEqual(item["sha256"], digest_item["sha256"])
            self.assertRegex(item["sha256"], r"^[0-9a-f]{64}$")
            self.assertGreater(item["bytes"], 0)
            normalized = PurePosixPath(path)
            self.assertFalse(normalized.is_absolute())
            self.assertNotIn("..", normalized.parts)
            self.assertNotIn("\\", path)
            self.assertNotIn("\x00", path)
            self.assertNotIn("text", item)
            self.assertNotIn("content", item)

    def test_archive_and_signature_evidence_are_pinned(self):
        self.assertEqual(self.manifest["acquisition"]["archive_sha256"], self.hashes["source_archive_sha256"])
        self.assertEqual(self.manifest["acquisition"]["archive_sha256"], "4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11")
        signature = self.manifest["signature_validation"]
        self.assertEqual(signature["clear_signature"], "GOOD_SIGNATURE")
        self.assertEqual(signature["signed_hash_entries"], 68)
        self.assertEqual(signature["usfm_signed_hash_matches"], 66)
        self.assertEqual(signature["gpg_trust_status"], "UNDEFINED_IN_ISOLATED_KEYRING")

    def test_closure_record_preserves_scope_and_exception(self):
        record = CLOSURE.read_text(encoding="utf-8")
        self.assertIn("CLOSED — OWNER-ACCEPTED WITH EXPLICIT SPECIALIST-REVIEW EXCEPTION", record)
        self.assertIn("DEFERRED", record)
        self.assertIn("content_intake_authorized=false", record)
        self.assertIn("UNDEFINED", record)


if __name__ == "__main__":
    unittest.main()
