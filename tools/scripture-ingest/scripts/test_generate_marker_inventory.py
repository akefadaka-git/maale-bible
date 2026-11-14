import hashlib
import importlib.util
import json
import pathlib
import tempfile
import unittest
from contextlib import redirect_stdout
from io import StringIO
from unittest.mock import patch

SCRIPT = pathlib.Path(__file__).with_name("generate_marker_inventory.py")
SPEC = importlib.util.spec_from_file_location("marker_inventory", SCRIPT)
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


class MarkerInventoryTests(unittest.TestCase):
    def test_counts_real_marker_tokens_and_emits_metadata_only(self):
        with tempfile.TemporaryDirectory() as temp:
            base = pathlib.Path(temp)
            corpus = base / "corpus"
            corpus.mkdir()
            files = []
            for index in range(66):
                book = f"B{index:02d}"
                name = f"{book}.usfm"
                payload = f"\\\\id {book}\n\\\\c 1\n\\\\p\n\\\\v 1 SYNTHETIC_PRIVATE_FIXTURE_TEXT \\*\n".encode()
                path = corpus / name
                path.write_bytes(payload)
                files.append({
                    "path": name,
                    "book_id": book,
                    "bytes": len(payload),
                    "sha256": hashlib.sha256(payload).hexdigest(),
                })
            manifest = base / "hashes.json"
            manifest.write_text(json.dumps({
                "source_id": "SYNTHETIC",
                "source_archive_sha256": "0" * 64,
                "files": files,
            }), encoding="utf-8")
            output = base / "out" / "inventory.json"
            with patch("sys.argv", ["generate_marker_inventory.py", "--corpus", str(corpus),
                                    "--hash-manifest", str(manifest), "--output", str(output)]):
                with redirect_stdout(StringIO()):
                    MODULE.main()
            result = json.loads(output.read_text(encoding="utf-8"))
            self.assertEqual(result["bookCount"], 66)
            self.assertEqual(result["markerCounts"]["id"], 66)
            self.assertEqual(result["markerCounts"]["c"], 66)
            self.assertEqual(result["markerCounts"]["p"], 66)
            self.assertEqual(result["markerCounts"]["v"], 66)
            self.assertEqual(result["markerCounts"]["*"], 66)
            self.assertFalse(result["scriptureTextIncluded"])
            self.assertNotIn("SYNTHETIC_PRIVATE_FIXTURE_TEXT", output.read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
