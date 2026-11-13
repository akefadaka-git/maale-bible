#!/usr/bin/env python3
"""Build a deterministic, separate, read-only Maale scripture SQLite package."""
from __future__ import annotations
import argparse, hashlib, json, os, re, sqlite3, stat, sys, tempfile, unicodedata, zipfile
from pathlib import Path, PurePosixPath

EDITION_ID = "MDYTWF-2008-MAALE-FULL"
LANGUAGE = "mdy"
EXPECTED_ARCHIVE_SHA256 = "4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11"
EXPECTED_ARCHIVE_BYTES = 1_809_863
MAX_ENTRIES, MAX_ENTRY_BYTES, MAX_TOTAL_BYTES, MAX_COMPRESSION_RATIO = 80, 2_000_000, 8_000_000, 200
BOOK_ORDER = ("GEN EXO LEV NUM DEU JOS JDG RUT 1SA 2SA 1KI 2KI 1CH 2CH EZR NEH EST JOB PSA PRO ECC SNG ISA "
"JER LAM EZK DAN HOS JOL AMO OBA JON MIC NAM HAB ZEP HAG ZEC MAL MAT MRK LUK JHN ACT ROM "
"1CO 2CO GAL EPH PHP COL 1TH 2TH 1TI 2TI TIT PHM HEB JAS 1PE 2PE 1JN 2JN 3JN JUD REV").split()
MARKER_RE = re.compile(r"\\(\+?[A-Za-z][A-Za-z0-9-]*)(\*)?")
VERSE_RE = re.compile(r"^\s*([0-9]+[a-z]?(?:-[0-9]+[a-z]?)?)\s*(.*)$", re.DOTALL)
CHAPTER_RE = re.compile(r"^\s*([0-9]+)\s*(.*)$", re.DOTALL)
CHAPTER_MARKERS={"c"}; VERSE_MARKERS={"v"}; BOOK_MARKERS={"id"}
HEADING_MARKERS={"s","s1","s2","s3","s4","r","d","sp","ms","ms1","ms2","ms3"}
ALTERNATE_LABEL_MARKERS={"ca","cp","va","vp"}; NOTE_MARKERS={"f","x"}
INLINE_MARKERS={"add","bd","bdit","bk","dc","em","it","k","nd","no","ord","pn","qt","sc","sig","sls","sup","tl","wj","w","ref","rq","xo","xk","xt","fig","qac","qs","pro","lik","lit","ior","iqt","ph","jmp","fr","ft","fq","fqa","fk","fl","fw","fp","fv","fdc","xq","xot","xop"}
STRUCTURAL_MARKERS=set("""ide h toc1 toc2 toc3 mt mt1 mt2 mt3 mt4 mte mte1 mte2 imt imt1 imt2 imt3 imt4 is is1 is2 is3 ip ipi im imi ipq imq ipr iq iq1 iq2 iq3 iq4 ib ili ili1 ili2 ili3 ili4 io io1 io2 io3 io4 iot iex p m po pr cls pmo pm pmc pmr pi pi1 pi2 pi3 mi nb b q q1 q2 q3 q4 qr qc qa qm qm1 qm2 qm3 qd li li1 li2 li3 li4 lim lim1 lim2 lim3 lim4 lh lf tr th th1 th2 th3 th4 tc tc1 tc2 tc3 tc4 tcr pc ph1 ph2 ph3 ph4 periph z zpa-x""".split())
SUPPORTED_MARKERS=BOOK_MARKERS|CHAPTER_MARKERS|VERSE_MARKERS|HEADING_MARKERS|ALTERNATE_LABEL_MARKERS|NOTE_MARKERS|INLINE_MARKERS|STRUCTURAL_MARKERS

class ImportErrorClosed(ValueError): pass
def sha256(data: bytes) -> str: return hashlib.sha256(data).hexdigest()

def safe_archive_members(zf):
    infos=zf.infolist()
    if len(infos)>MAX_ENTRIES: raise ImportErrorClosed(f"archive has too many entries: {len(infos)}")
    members={}; total=0
    for info in infos:
        name=info.filename; path=PurePosixPath(name)
        if "\\" in name or path.is_absolute() or any(p in ("",".","..") for p in path.parts):
            raise ImportErrorClosed(f"unsafe archive path: {name!r}")
        key=str(path).casefold()
        if key in members: raise ImportErrorClosed(f"duplicate normalized archive path: {name!r}")
        mode=info.external_attr>>16
        file_type = stat.S_IFMT(mode)
        if stat.S_ISLNK(mode) or (file_type and file_type not in (stat.S_IFREG, stat.S_IFDIR)):
            raise ImportErrorClosed(f"unsupported archive entry type: {name!r}")
        if info.flag_bits&1: raise ImportErrorClosed(f"encrypted archive entry: {name!r}")
        if info.file_size>MAX_ENTRY_BYTES: raise ImportErrorClosed(f"archive entry too large: {name!r}")
        total+=info.file_size
        if total>MAX_TOTAL_BYTES: raise ImportErrorClosed("archive uncompressed size exceeds limit")
        if info.file_size and (info.compress_size==0 or info.file_size/info.compress_size>MAX_COMPRESSION_RATIO):
            raise ImportErrorClosed(f"compression ratio exceeds limit: {name!r}")
        members[key]=info
    bad=zf.testzip()
    if bad: raise ImportErrorClosed(f"ZIP CRC failure in {bad!r}")
    return members

def tokenize_line(line):
    out=[]; matches=list(MARKER_RE.finditer(line))
    if not matches: return [(None,line,False)]
    if matches[0].start(): out.append((None,line[:matches[0].start()],False))
    for i,m in enumerate(matches):
        end=matches[i+1].start() if i+1<len(matches) else len(line)
        out.append((m.group(1).lstrip("+"),line[m.end():end],bool(m.group(2))))
    return out

def strip_inline(value,line_no,source):
    out=[]; note_depth=0; cursor=0
    for m in MARKER_RE.finditer(value):
        if not note_depth: out.append(value[cursor:m.start()])
        marker=m.group(1).lstrip("+"); closing=bool(m.group(2))
        if marker not in SUPPORTED_MARKERS: raise ImportErrorClosed(f"{source}:{line_no}: unsupported USFM marker \\{marker}")
        if marker in NOTE_MARKERS: note_depth=0 if closing else 1
        cursor=m.end()
    out.append(value[cursor:] if not note_depth else "")
    return "".join(out)

def parse_usfm(data,source):
    try: text=data.decode("utf-8",errors="strict")
    except UnicodeDecodeError as exc: raise ImportErrorClosed(f"{source}: invalid UTF-8: {exc}") from exc
    if "\ufffd" in text: raise ImportErrorClosed(f"{source}: replacement character present")
    if unicodedata.normalize("NFC",text)!=text: raise ImportErrorClosed(f"{source}: source is not NFC")
    book_id=None; chapter=None; verses=[]; headings=[]; current_verse=None; current_heading=None; in_note=False; in_alternate_label=False
    for line_no,raw in enumerate(text.splitlines(),1):
        if not raw.strip():
            if current_verse is not None: current_verse["text"]+=" "
            continue
        for marker,payload,closing in tokenize_line(raw):
            if marker is None:
                if in_note: continue
                if current_heading is not None: current_heading["text"]+=payload
                elif current_verse is not None: current_verse["text"]+=payload
                elif payload.strip(): raise ImportErrorClosed(f"{source}:{line_no}: text outside chapter/verse context")
                continue
            if marker not in SUPPORTED_MARKERS: raise ImportErrorClosed(f"{source}:{line_no}: unsupported USFM marker \\{marker}")
            if marker in NOTE_MARKERS: in_note=not closing; continue
            if in_note: continue
            if closing:
                if marker in ALTERNATE_LABEL_MARKERS:
                    in_alternate_label = False
                elif marker not in INLINE_MARKERS:
                    raise ImportErrorClosed(f"{source}:{line_no}: unexpected closing marker \\{marker}*")
                continue
            if marker in BOOK_MARKERS:
                found=payload.strip().split(maxsplit=1)[0] if payload.strip() else ""
                if not re.fullmatch(r"[A-Z0-9]{3}",found): raise ImportErrorClosed(f"{source}:{line_no}: invalid book identifier")
                if book_id is not None and found!=book_id: raise ImportErrorClosed(f"{source}:{line_no}: conflicting book identifier")
                book_id=found; continue
            if marker in CHAPTER_MARKERS:
                m=CHAPTER_RE.match(payload)
                if not m or int(m.group(1))<1: raise ImportErrorClosed(f"{source}:{line_no}: malformed chapter marker")
                chapter=int(m.group(1)); current_verse=None; current_heading=None; continue
            if marker in VERSE_MARKERS:
                if chapter is None: raise ImportErrorClosed(f"{source}:{line_no}: verse before chapter")
                m=VERSE_RE.match(payload)
                if not m: raise ImportErrorClosed(f"{source}:{line_no}: malformed verse marker")
                label,body=m.groups()
                current_heading=None
                current_verse={"chapter":chapter,"label":label,"text":strip_inline(body,line_no,source).strip(),
                               "ordinal":sum(v["chapter"]==chapter for v in verses)+1,"line":line_no}
                verses.append(current_verse); continue
            if marker in HEADING_MARKERS:
                body=strip_inline(payload,line_no,source).strip()
                if body: current_heading={"chapter":chapter or 0,"marker":marker,"text":body,"ordinal":len(headings)+1,"line":line_no}; headings.append(current_heading)
                current_verse=None; continue
            if marker in ALTERNATE_LABEL_MARKERS:\n                in_alternate_label = True if not closing else False\n                continue
            if marker in INLINE_MARKERS:
                visible=payload.split("|",1)[0] if marker=="w" and "|" in payload else payload
                visible=strip_inline(visible,line_no,source)
                if current_verse is not None: current_verse["text"]+=visible
                elif current_heading is not None: current_heading["text"]+=visible
                elif visible.strip(): raise ImportErrorClosed(f"{source}:{line_no}: inline text outside verse/heading")
                continue
            if marker in STRUCTURAL_MARKERS:
                if marker not in {"toc1","toc2","toc3","h","mt","mt1","mt2","mt3","mt4"}: current_heading=None
                continue
    if not book_id: raise ImportErrorClosed(f"{source}: missing \\id marker")
    if not verses: raise ImportErrorClosed(f"{source}: no verse records found")
    for v in verses:
        v["text"]=v["text"].strip()
        if not v["text"]: raise ImportErrorClosed(f"{source}:{v['line']}: empty verse text")
    return book_id,verses,headings

def load_manifest(path):
    try: manifest=json.loads(path.read_text(encoding="utf-8"))
    except (OSError,json.JSONDecodeError) as exc: raise ImportErrorClosed(f"cannot read evidence manifest: {exc}") from exc
    if manifest.get("edition",{}).get("ebible_identifier")!="mdyeth": raise ImportErrorClosed("wrong edition")
    if manifest.get("acquisition",{}).get("archive_sha256")!=EXPECTED_ARCHIVE_SHA256: raise ImportErrorClosed("manifest source hash mismatch")
    if manifest.get("signature_validation",{}).get("clear_signature")!="GOOD_SIGNATURE": raise ImportErrorClosed("signature evidence missing")
    if manifest.get("rights_metadata",{}).get("license")!="CC BY-SA 4.0": raise ImportErrorClosed("licence evidence missing")
    files=manifest.get("files")
    if not isinstance(files,list) or len(files)!=66: raise ImportErrorClosed("manifest must list 66 USFM files")
    ids=[x.get("book_id") for x in files]
    if len(set(ids))!=66 or set(ids)!=set(BOOK_ORDER): raise ImportErrorClosed("manifest book inventory mismatch")
    return manifest

def create_database(output,edition,books,verses,headings):
    if output.exists(): raise ImportErrorClosed(f"refusing to overwrite existing package: {output}")
    output.parent.mkdir(parents=True,exist_ok=True)
    fd,tmp=tempfile.mkstemp(prefix=output.name+".",suffix=".tmp",dir=output.parent); os.close(fd); tmp=Path(tmp)
    try:
        db=sqlite3.connect(tmp)
        try:
            db.execute("PRAGMA foreign_keys=ON"); db.execute("PRAGMA journal_mode=DELETE"); db.execute("PRAGMA synchronous=FULL")
            db.executescript("""
            CREATE TABLE package_metadata(key TEXT PRIMARY KEY,value TEXT NOT NULL);
            CREATE TABLE books(edition_id TEXT NOT NULL,book_id TEXT NOT NULL,ordinal INTEGER NOT NULL,source_path TEXT NOT NULL,source_sha256 TEXT NOT NULL,PRIMARY KEY(edition_id,book_id),UNIQUE(edition_id,ordinal));
            CREATE TABLE chapters(edition_id TEXT NOT NULL,book_id TEXT NOT NULL,chapter_number INTEGER NOT NULL,PRIMARY KEY(edition_id,book_id,chapter_number),FOREIGN KEY(edition_id,book_id) REFERENCES books(edition_id,book_id));
            CREATE TABLE verses(edition_id TEXT NOT NULL,book_id TEXT NOT NULL,chapter_number INTEGER NOT NULL,verse_label TEXT NOT NULL,ordinal INTEGER NOT NULL,text TEXT NOT NULL,source_path TEXT NOT NULL,source_line INTEGER NOT NULL,PRIMARY KEY(edition_id,book_id,chapter_number,verse_label),FOREIGN KEY(edition_id,book_id,chapter_number) REFERENCES chapters(edition_id,book_id,chapter_number));
            CREATE TABLE headings(edition_id TEXT NOT NULL,book_id TEXT NOT NULL,chapter_number INTEGER NOT NULL,ordinal INTEGER NOT NULL,marker TEXT NOT NULL,text TEXT NOT NULL,source_path TEXT NOT NULL,source_line INTEGER NOT NULL,PRIMARY KEY(edition_id,book_id,ordinal),FOREIGN KEY(edition_id,book_id) REFERENCES books(edition_id,book_id));
            CREATE INDEX verses_lookup ON verses(edition_id,book_id,chapter_number,ordinal);
            """)
            metadata={"package_format":"MAALE-SCRIPTURE-SQLITE-1","edition_id":EDITION_ID,"language":LANGUAGE,
            "source_archive_sha256":edition["archive_sha256"],"source_archive_bytes":str(edition["archive_bytes"]),
            "license":"CC BY-SA 4.0","attribution":"Maale Bible (ጌኤዦ ማፃኣፖ) © 2008 The Word for the World International and the Bible Society of Ethiopia.",
            "signer_trust":"UNDEFINED_IN_ISOLATED_KEYRING","release_eligible":"false","content_is_immutable":"true"}
            db.executemany("INSERT INTO package_metadata VALUES (?,?)",sorted(metadata.items()))
            db.executemany("INSERT INTO books VALUES (?,?,?,?,?)",[(EDITION_ID,b["book_id"],b["ordinal"],b["path"],b["sha256"]) for b in books])
            db.executemany("INSERT INTO chapters VALUES (?,?,?)",[(EDITION_ID,b["book_id"],c) for b in books for c in b["chapters"]])
            db.executemany("INSERT INTO verses VALUES (?,?,?,?,?,?,?,?)",[(EDITION_ID,v["book_id"],v["chapter"],v["label"],v["ordinal"],v["text"],v["path"],v["line"]) for v in verses])
            db.executemany("INSERT INTO headings VALUES (?,?,?,?,?,?,?,?)",[(EDITION_ID,h["book_id"],h["chapter"],h["ordinal"],h["marker"],h["text"],h["path"],h["line"]) for h in headings])
            db.commit(); db.execute("PRAGMA optimize")
        finally: db.close()
        os.chmod(tmp,0o444); os.replace(tmp,output)
    except Exception:
        try: tmp.chmod(0o600); tmp.unlink(missing_ok=True)
        except OSError: pass
        raise

def import_archive(source,manifest_path,output):
    manifest=load_manifest(manifest_path); archive=source.read_bytes()
    if len(archive)!=EXPECTED_ARCHIVE_BYTES: raise ImportErrorClosed(f"archive size mismatch: {len(archive)}")
    digest=sha256(archive)
    if digest!=EXPECTED_ARCHIVE_SHA256: raise ImportErrorClosed(f"archive SHA-256 mismatch: {digest}")
    by_id={x["book_id"]:x for x in manifest["files"]}; books=[]; verses=[]; headings=[]
    with zipfile.ZipFile(source,"r") as zf:
        members=safe_archive_members(zf)
        for ordinal,bid in enumerate(BOOK_ORDER,1):
            rec=by_id[bid]; path=rec["path"]; key=str(PurePosixPath(path)).casefold()
            if key not in members: raise ImportErrorClosed(f"manifest file missing: {path}")
            data=zf.read(members[key]); file_hash=sha256(data)
            if file_hash!=rec["sha256"]: raise ImportErrorClosed(f"USFM SHA-256 mismatch: {path}")
            parsed_id,pv,ph=parse_usfm(data,path)
            if parsed_id!=bid: raise ImportErrorClosed(f"{path}: manifest ID {bid} != USFM ID {parsed_id}")
            chapters=sorted({v["chapter"] for v in pv})
            if chapters!=list(range(1,max(chapters)+1)): raise ImportErrorClosed(f"{path}: non-contiguous chapters")
            if rec.get("chapter_count") is not None and len(chapters)!=rec["chapter_count"]:
                raise ImportErrorClosed(f"{path}: chapter count differs from evidence manifest")
            if rec.get("verse_marker_count") is not None and len(pv)!=rec["verse_marker_count"]:
                raise ImportErrorClosed(f"{path}: verse-marker count differs from evidence manifest")
            books.append({"book_id":bid,"ordinal":ordinal,"path":path,"sha256":file_hash,"chapters":chapters})
            verses.extend({**v,"book_id":bid,"path":path} for v in pv)
            headings.extend({**h,"book_id":bid,"path":path} for h in ph)
    if sum(len(b["chapters"]) for b in books)!=1189 or len(verses)!=30506:
        raise ImportErrorClosed("whole-edition chapter/verse totals differ from the verified intake baseline")
    create_database(output,{"archive_sha256":digest,"archive_bytes":len(archive)},books,verses,headings)
    return {"status":"PRIVATE_CANDIDATE_IMPORTED_NOT_RELEASEABLE","edition_id":EDITION_ID,"archive_sha256":digest,
    "books":len(books),"chapters":sum(len(b["chapters"]) for b in books),"verses":len(verses),"headings":len(headings),
    "output":str(output),"release_eligible":False,"signer_trust":manifest["signature_validation"].get("gpg_trust_status")}

def main():
    p=argparse.ArgumentParser(description=__doc__); p.add_argument("--source",type=Path,required=True)
    p.add_argument("--manifest",type=Path,required=True); p.add_argument("--output",type=Path,required=True); a=p.parse_args()
    try: print(json.dumps(import_archive(a.source,a.manifest,a.output),ensure_ascii=False,indent=2)); return 0
    except (ImportErrorClosed,OSError,zipfile.BadZipFile,sqlite3.Error) as exc:
        print(f"IMPORT REJECTED: {exc}",file=sys.stderr); return 2
if __name__=="__main__": raise SystemExit(main())
