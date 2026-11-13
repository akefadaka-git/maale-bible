import tempfile
import unittest
from pathlib import Path

from localization_audit import audit


class LocalizationAuditTest(unittest.TestCase):
    def test_reports_missing_extra_and_placeholder_mismatch(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            base = root / "base.xml"
            locale = root / "locale.xml"
            base.write_text(
                '<resources><string name="hello">Hello</string>'
                '<string name="count">%1$d books</string></resources>',
                encoding="utf-8",
            )
            locale.write_text(
                '<resources><string name="hello">ሰላም</string>'
                '<string name="count">መጻሕፍት</string>'
                '<string name="legacy">old</string></resources>',
                encoding="utf-8",
            )
            result = audit(base, locale)
            self.assertEqual(result["base_count"], 2)
            self.assertEqual(result["locale_count"], 3)
            self.assertEqual(result["matched_count"], 2)
            self.assertEqual(result["missing_keys"], [])
            self.assertEqual(result["locale_only_keys"], ["legacy"])
            self.assertEqual(
                result["placeholder_mismatches"],
                [{"key": "count", "base": ["%1$d"], "locale": []}],
            )

    def test_detects_duplicate_resource_names(self):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            base = root / "base.xml"
            locale = root / "locale.xml"
            base.write_text(
                '<resources><string name="same">One</string>'
                '<string name="same">Two</string></resources>',
                encoding="utf-8",
            )
            locale.write_text(
                '<resources><string name="same">ሓደ</string></resources>',
                encoding="utf-8",
            )
            result = audit(base, locale)
            self.assertEqual(result["duplicate_base_keys"], ["same"])


if __name__ == "__main__":
    unittest.main()
