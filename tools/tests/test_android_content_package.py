import json
import tempfile
import unittest
from pathlib import Path

from tools.build_android_content_asset import build_asset


class AndroidContentAssetTests(unittest.TestCase):
    def test_asset_descriptor_is_derived_from_verified_report(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            database = root / "content.sqlite"
            database.write_bytes(b"synthetic sqlite database bytes")
            import hashlib
            sha = hashlib.sha256(database.read_bytes()).hexdigest()
            source_manifest = root / "source.json"
            source_manifest.write_text(json.dumps({
                "source": {
                    "canonical_edition_id": "mdyeth",
                    "archive_sha256": "a" * 64,
                    "archive_bytes": 100,
                },
                "structural_validation": {
                    "usfm_file_count": 66,
                    "chapter_count": 1189,
                    "verse_marker_lines": 30506,
                },
            }), encoding="utf-8")
            report = root / "report.json"
            report.write_text(json.dumps({
                "status": "PASS",
                "source_id": "mdyeth",
                "archive_sha256": "a" * 64,
                "archive_bytes": 100,
                "book_count": 66,
                "chapter_count": 1189,
                "verse_record_count": 30506,
                "schema_version": "1",
                "parser_version": "0.1.0",
                "fts_tokenizer": "unicode61 remove_diacritics 0",
                "tokenizer_qualification": "ENGINEERING_BASELINE_NOT_LINGUISTICALLY_QUALIFIED",
                "database": {
                    "database_bytes": database.stat().st_size,
                    "database_sha256": sha,
                    "fts_records": 30506,
                },
            }), encoding="utf-8")
            output = root / "assets"
            result = build_asset(database, report, source_manifest, output)
            self.assertEqual(result["edition_id"], "mdyeth")
            properties = dict(
                line.split("=", 1)
                for line in (output / "manifest.properties").read_text(encoding="utf-8").splitlines()
            )
            self.assertEqual(properties["sourceArchiveSha256"], "a" * 64)
            self.assertEqual(properties["databaseSha256"], sha)
            self.assertEqual((output / "content.sqlite").read_bytes(), database.read_bytes())

    def test_rejects_pipeline_report_for_different_source_hash(self):
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            database = root / "content.sqlite"
            database.write_bytes(b"synthetic")
            source_manifest = root / "source.json"
            source_manifest.write_text(json.dumps({
                "source": {"canonical_edition_id": "mdyeth", "archive_sha256": "a" * 64, "archive_bytes": 100},
                "structural_validation": {"usfm_file_count": 66, "chapter_count": 1189, "verse_marker_lines": 30506},
            }), encoding="utf-8")
            report = root / "report.json"
            report.write_text(json.dumps({
                "status": "PASS", "source_id": "mdyeth", "archive_sha256": "b" * 64,
                "archive_bytes": 100, "book_count": 66, "chapter_count": 1189,
                "verse_record_count": 30506, "schema_version": "1", "parser_version": "0.1.0",
                "fts_tokenizer": "unicode61 remove_diacritics 0",
                "tokenizer_qualification": "ENGINEERING_BASELINE_NOT_LINGUISTICALLY_QUALIFIED",
                "database": {"database_bytes": 9, "database_sha256": "0" * 64, "fts_records": 30506},
            }), encoding="utf-8")
            with self.assertRaisesRegex(ValueError, "archive hash"):
                build_asset(database, report, source_manifest, root / "out")


if __name__ == "__main__":
    unittest.main()
