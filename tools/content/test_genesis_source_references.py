#!/usr/bin/env python3
import unittest
from validate_genesis_source_references import parse_usfm, validate_one

class GenesisReferenceValidatorTests(unittest.TestCase):
    def setUp(self):
        self.books = {
            "GEN": parse_usfm(
                b"\\c 1\n\\v 1 First\n\\v 2-3 Second and third\n\\c 2\n\\v 1-4 Four verses\n\\c 3\n\\v 1-24 Twenty-four verses\n",
                "GEN",
            )
        }

    def test_single_verse_and_verse_range(self):
        self.assertEqual([], validate_one("GEN 1:1-3", self.books))

    def test_chapter_range(self):
        self.assertEqual([], validate_one("GEN 1-3", self.books))

    def test_cross_chapter_verse_range(self):
        self.assertEqual([], validate_one("GEN 1:2-2:4", self.books))

    def test_semicolon_inherits_book_identifier(self):
        self.assertEqual([], validate_one("GEN 1:1; 2:1-4", self.books))

    def test_missing_verse_is_rejected(self):
        self.assertTrue(validate_one("GEN 1:1-4", self.books))

    def test_missing_book_is_rejected(self):
        self.assertTrue(validate_one("EXO 1:1", self.books))

if __name__ == "__main__":
    unittest.main()
