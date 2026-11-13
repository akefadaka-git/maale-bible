import sqlite3, tempfile, unittest, zipfile
from pathlib import Path
from scripture_importer import ImportErrorClosed, create_database, parse_usfm, safe_archive_members

class ParserTests(unittest.TestCase):
    def test_chapters_verses_headings_inline_and_notes(self):
        src=(r"\id GEN Maale"+"\n"+r"\h Genesis"+"\n"+r"\c 1"+"\n"+r"\s1 Heading"+"\n"+
             r"\p"+"\n"+r"\v 1 In the \add beginning\add* God created."+"\n"+
             r'\v 2 Word \w visible|lemma="visible" strong="H000"\w* text. \f + \ft ignored note\f*'+"\n").encode()
        bid,verses,heads=parse_usfm(src,"fixture.usfm")
        self.assertEqual(bid,"GEN"); self.assertEqual([v["label"] for v in verses],["1","2"])
        self.assertEqual(verses[0]["text"],"In the beginning God created.")
        self.assertIn("visible",verses[1]["text"]); self.assertNotIn("ignored note",verses[1]["text"])
        self.assertEqual(heads[0]["text"],"Heading")
    def test_unknown_marker_fails_closed(self):
        with self.assertRaisesRegex(ImportErrorClosed,"unsupported USFM marker"):
            parse_usfm(b"\\id GEN\n\\c 1\n\\v 1 text\n\\unknown value\n","bad.usfm")
    def test_missing_id_and_verse_before_chapter_rejected(self):
        with self.assertRaisesRegex(ImportErrorClosed,"missing"): parse_usfm(b"\\c 1\n\\v 1 text\n","missing.usfm")
        with self.assertRaisesRegex(ImportErrorClosed,"before chapter"): parse_usfm(b"\\id GEN\n\\v 1 text\n","early.usfm")
    def test_invalid_utf8_and_non_nfc_rejected(self):
        with self.assertRaisesRegex(ImportErrorClosed,"invalid UTF-8"): parse_usfm(b"\\id GEN\n\xff","bad-utf8.usfm")
        with self.assertRaisesRegex(ImportErrorClosed,"not NFC"): parse_usfm("\\id GEN\n\\c 1\n\\v 1 e\u0301\n".encode(),"nfc.usfm")
    def test_verse_range_label_preserved(self):
        _,verses,_=parse_usfm(b"\\id GEN\n\\c 1\n\\v 3-4 combined text\n","range.usfm")
        self.assertEqual(verses[0]["label"],"3-4")
    def test_alternate_verse_label_is_not_added_to_display_text(self):
        _,verses,_=parse_usfm(b"\\id GEN\n\\c 1\n\\v 1\\va 1a\\va* Main verse text\n","alt.usfm")
        self.assertEqual(verses[0]["text"],"Main verse text")

class PackageTests(unittest.TestCase):
    def test_zip_traversal_rejected(self):
        with tempfile.TemporaryDirectory() as d:
            path=Path(d)/"bad.zip"
            with zipfile.ZipFile(path,"w") as z: z.writestr("../escape.usfm","x")
            with zipfile.ZipFile(path) as z:
                with self.assertRaisesRegex(ImportErrorClosed,"unsafe archive path"): safe_archive_members(z)
    def test_edition_aware_separate_sqlite_schema(self):
        with tempfile.TemporaryDirectory() as d:
            path=Path(d)/"package.sqlite"
            books=[{"book_id":"GEN","ordinal":1,"path":"GEN.usfm","sha256":"a"*64,"chapters":[1]}]
            verses=[{"book_id":"GEN","chapter":1,"label":"1","ordinal":1,"text":"Fixture text","path":"GEN.usfm","line":3}]
            create_database(path,{"archive_sha256":"b"*64,"archive_bytes":123},books,verses,[])
            db=sqlite3.connect(f"file:{path}?mode=ro",uri=True)
            try:
                row=db.execute("SELECT edition_id,book_id,chapter_number,verse_label,text FROM verses").fetchone()
                self.assertEqual(row,("MDYTWF-2008-MAALE-FULL","GEN",1,"1","Fixture text"))
                self.assertEqual(db.execute("SELECT value FROM package_metadata WHERE key='release_eligible'").fetchone()[0],"false")
            finally: db.close()
            with self.assertRaises(ImportErrorClosed): create_database(path,{},books,verses,[])
if __name__=="__main__": unittest.main()
