import tempfile
import unittest
import zipfile
from pathlib import Path
from stat import S_IFLNK

from tools.archive_preflight import preflight_zip


class ArchivePreflightTests(unittest.TestCase):
    def make_zip(self, directory, entries):
        path = Path(directory) / "synthetic.zip"
        with zipfile.ZipFile(path, "w") as archive:
            for name, data, external_attr in entries:
                info = zipfile.ZipInfo(name)
                if external_attr is not None:
                    info.external_attr = external_attr
                archive.writestr(info, data)
        return path

    def test_valid_synthetic_archive_passes_without_extraction(self):
        with tempfile.TemporaryDirectory() as directory:
            path = self.make_zip(directory, [("fixture.usfm", "SYNTHETIC_PLACEHOLDER", None)])
            report = preflight_zip(path)
            self.assertEqual(report["status"], "PASS")
            self.assertEqual(report["entry_count"], 1)
            self.assertFalse(report["extraction_performed"])
            self.assertFalse(report["content_intake_authorized"])
            self.assertEqual(len(report["archive_sha256"]), 64)

    def test_parent_traversal_path_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            path = self.make_zip(directory, [("../escape.txt", "x", None)])
            report = preflight_zip(path)
            self.assertEqual(report["status"], "FAIL")
            self.assertIn("UNSAFE_PATH", {item["code"] for item in report["errors"]})

    def test_absolute_path_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            path = self.make_zip(directory, [("/absolute.txt", "x", None)])
            report = preflight_zip(path)
            self.assertIn("UNSAFE_PATH", {item["code"] for item in report["errors"]})

    def test_windows_drive_path_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            path = self.make_zip(directory, [("C:/outside.txt", "x", None)])
            report = preflight_zip(path)
            self.assertIn("UNSAFE_PATH", {item["code"] for item in report["errors"]})

    def test_backslash_path_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            path = self.make_zip(directory, [("folder\\escape.txt", "x", None)])
            report = preflight_zip(path)
            self.assertIn("UNSAFE_PATH", {item["code"] for item in report["errors"]})

    def test_casefold_collision_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            path = self.make_zip(directory, [("Books/GEN.usfm", "a", None), ("books/gen.USFM", "b", None)])
            report = preflight_zip(path)
            self.assertIn("DUPLICATE_NORMALIZED_PATH", {item["code"] for item in report["errors"]})

    def test_unicode_normalization_collision_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            path = self.make_zip(directory, [("caf\u00e9.txt", "a", None), ("cafe\u0301.txt", "b", None)])
            report = preflight_zip(path)
            self.assertIn("DUPLICATE_NORMALIZED_PATH", {item["code"] for item in report["errors"]})

    def test_symlink_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            attr = (S_IFLNK | 0o777) << 16
            path = self.make_zip(directory, [("link.usfm", "target", attr)])
            report = preflight_zip(path)
            self.assertIn("SYMLINK_ENTRY", {item["code"] for item in report["errors"]})

    def test_nested_archive_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            path = self.make_zip(directory, [("nested.zip", "not-an-archive", None)])
            report = preflight_zip(path)
            self.assertIn("NESTED_ARCHIVE", {item["code"] for item in report["errors"]})

    def test_file_size_limit_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            path = self.make_zip(directory, [("large.usfm", "0123456789", None)])
            report = preflight_zip(path, max_file_size=5)
            self.assertIn("FILE_SIZE_LIMIT_EXCEEDED", {item["code"] for item in report["errors"]})

    def test_total_size_limit_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            path = self.make_zip(directory, [("one.usfm", "1234", None), ("two.usfm", "5678", None)])
            report = preflight_zip(path, max_total_size=6)
            self.assertIn("TOTAL_SIZE_LIMIT_EXCEEDED", {item["code"] for item in report["errors"]})

    def test_compression_ratio_limit_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "compressed.zip"
            with zipfile.ZipFile(path, "w", compression=zipfile.ZIP_DEFLATED) as archive:
                archive.writestr("repetitive.usfm", "A" * 10000)
            report = preflight_zip(path, max_compression_ratio=2.0)
            self.assertIn("COMPRESSION_RATIO_LIMIT_EXCEEDED", {item["code"] for item in report["errors"]})

    def test_entry_count_limit_fails(self):
        with tempfile.TemporaryDirectory() as directory:
            path = self.make_zip(directory, [("one.usfm", "x", None), ("two.usfm", "y", None)])
            report = preflight_zip(path, max_entries=1)
            self.assertIn("ENTRY_LIMIT_EXCEEDED", {item["code"] for item in report["errors"]})

    def test_invalid_zip_returns_error_without_extraction(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / "broken.zip"
            path.write_bytes(b"not a zip archive")
            report = preflight_zip(path)
            self.assertEqual(report["status"], "ERROR")
            self.assertFalse(report["extraction_performed"])


if __name__ == "__main__":
    unittest.main()
