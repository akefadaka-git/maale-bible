import csv,json,re,sqlite3,statistics,time,unicodedata,zipfile,stat
from pathlib import Path
from io import BytesIO

ROOT=Path(__file__).resolve().parents[2]

def validate_zip(data):
    with zipfile.ZipFile(BytesIO(data)) as archive:
        names=set()
        total=0
        for item in archive.infolist():
            name=item.filename.replace(chr(92), "/")
            parts=name.split("/")
            if name.startswith("/") or any(p in ("", ".", "..") for p in parts):
                raise ValueError("unsafe-path")
            if name in names:
                raise ValueError("duplicate-path")
            if stat.S_ISLNK(item.external_attr >> 16):
                raise ValueError("symlink")
            if item.file_size > 32*1024*1024:
                raise ValueError("entry-too-large")
            total += item.file_size
            if total > 128*1024*1024:
                raise ValueError("archive-too-large")
            names.add(name)
        return names

def unicode_contracts():
    fixtures = [
        "ሰላም — synthetic Ethiopic fixture 123",
        "ቤተ ክርስቲያን፣ ፡።፤፥",
        "Maale / Amharic / English የተቀላቀለ",
        "e\u0301 and é",
        "ሀለሐመሠረሰሸቀበተ",
    ]
    checks=[]
    for source in fixtures:
        before=source.encode("utf-8")
        key=unicodedata.normalize("NFC",source)
        assert source.encode("utf-8")==before, "display source mutated"
        assert unicodedata.normalize("NFC",key)==key, "search key is not NFC"
        assert key.encode("utf-8").decode("utf-8")==key
        checks.append({"source_preserved":True,"nfc_idempotent":True,"codepoints":len(source)})
    decomposed="e\u0301"
    assert unicodedata.normalize("NFC",decomposed)=="é"
    assert "፡" in fixtures[1] and "።" in fixtures[1]
    return {"synthetic_fixtures":len(fixtures),"checks":checks,
            "normalization":"NFC idempotence only; no Maale linguistic correctness claim",
            "specialist_review":"DEFERRED"}

def main():
    screens=json.loads((ROOT/"verification/p2/screen-contracts.json").read_text())
    assert len(screens)==42 and len({x["route"] for x in screens})==42
    assert [x["screenId"] for x in screens]==[f"SCR-{i:03d}" for i in range(1,43)]
    req=(ROOT/"docs/archive/superseded-plans/A4_REQUIREMENTS_AND_ACCEPTANCE_MATRIX.md").read_text()
    ids=sorted(set(re.findall(r"\|\s*((?:NFR|LIB|READ|ANNO|SEARCH|REF|STUDY|PLAN|MAP|EXPORT|SET|A11Y|DEFER)-\d+)\s*\|",req)))
    assert ids, "requirement IDs not parsed"
    assert all(x["testId"] and len(x["states"])==6 for x in screens)
    with (ROOT/"docs/verification/P2_V1_REQUIREMENT_TEST_TRACEABILITY_REGISTER.csv").open(encoding="utf-8",newline="") as fh:
        register=list(csv.DictReader(fh))
    assert len(register)==88, f"expected 88 traceability rows, got {len(register)}"
    rids=[x["requirement_id"] for x in register]
    assert len(set(rids))==88 and set(rids)==set(ids), "register IDs must exactly match approved requirements"
    active=[x for x in register if not x["requirement_id"].startswith("DEFER-")]
    deferred=[x for x in register if x["requirement_id"].startswith("DEFER-")]
    assert len(active)==82 and len(deferred)==6
    screen_ids={x["screenId"] for x in screens}
    for row in active:
        assert row["architecture_component"] and row["test_id"]=="TRC-"+row["requirement_id"]
        assert row["screen_routes"], f"active requirement lacks route: {row['requirement_id']}"
        for entry in row["screen_routes"].split("; "):
            sid=entry.split(" ",1)[0]
            assert sid in screen_ids, f"unknown screen {sid} for {row['requirement_id']}"
    for row in deferred:
        assert not row["screen_routes"] and row["test_id"]=="TRC-"+row["requirement_id"]

    fixtures=[("EXACT",1,1),("SPLIT",1,2),("MERGED",2,1),("PARTIAL",1,1),("UNMAPPED",1,0)]
    for status,canonical,edition in fixtures:
        assert (edition>0)==(status!="UNMAPPED")
        if status=="EXACT": assert canonical==edition==1
        if status=="SPLIT": assert canonical==1 and edition>1
        if status=="MERGED": assert canonical>1 and edition==1

    unicode_result=unicode_contracts()

    good=BytesIO()
    with zipfile.ZipFile(good,"w") as z: z.writestr("manifest.json","{}")
    assert "manifest.json" in validate_zip(good.getvalue())
    bad=BytesIO()
    with zipfile.ZipFile(bad,"w") as z: z.writestr("../escape","x")
    try: validate_zip(bad.getvalue()); raise AssertionError("path traversal accepted")
    except ValueError as e: assert str(e)=="unsafe-path"
    duplicate=BytesIO()
    with zipfile.ZipFile(duplicate,"w") as z:
        z.writestr("same.json","one"); z.writestr("same.json","two")
    try: validate_zip(duplicate.getvalue()); raise AssertionError("duplicate path accepted")
    except ValueError as e: assert str(e)=="duplicate-path"

    db=sqlite3.connect(":memory:")
    db.execute("CREATE VIRTUAL TABLE f USING fts5(body)")
    db.executemany("INSERT INTO f(body) VALUES(?)",[(f"synthetic alpha beta row {i}",) for i in range(100000)])
    samples=[]
    for _ in range(30):
        t=time.perf_counter(); db.execute("SELECT rowid FROM f WHERE f MATCH 'alpha beta' LIMIT 20").fetchall(); samples.append((time.perf_counter()-t)*1000)
    host={"records":100000,"p50_ms":round(statistics.median(samples),3),"p95_ms":round(sorted(samples)[28],3),"engine":"host sqlite3; not Android evidence"}
    db.close()

    forbidden={".usfm",".sfm",".usx",".mp3",".m4a",".wav",".ogg",".flac",".epub",".sword",".sqlite",".db",".zip",".7z",".tar"}
    forbidden_dirs={"scripture","bible-text","licensed-content","corpus"}
    found=[]
    for p in ROOT.rglob("*"):
        if not p.is_file() or ".git" in p.parts: continue
        rel=p.relative_to(ROOT)
        if p.suffix.lower() in forbidden: found.append(str(rel))
        if any(part.lower() in forbidden_dirs for part in rel.parts): found.append(str(rel))
    readme=(ROOT/"README.md").read_text(encoding="utf-8")
    assert "content_intake_authorized=false" in readme, "content intake guard missing from README"
    assert not found, "protected-content paths/extensions found: "+", ".join(sorted(set(found)))

    result={
        "screen_contracts":{"count":42,"unique_routes":42,"route_state_contracts":"PASS","actual_app_navigation":"NOT VERIFIED; current shell has three top-level destinations"},
        "requirement_traceability":{"rows":len(register),"active_mapped":len(active),"deferred_excluded":len(deferred),"missing_active_mappings":0,"status":"PASS — register completeness and route references validated; feature tests remain outstanding"},
        "mapping_fixtures":5,
        "unicode":unicode_result,
        "backup_archive_host_checks":"PASS — path traversal and duplicate path rejected; cryptographic verification is Android instrumentation",
        "host_fts_benchmark":host,
        "android_fts_benchmark":"PENDING — P2V7 Android instrumentation required",
        "content_boundary":{"status":"PASS — repository path/extension scan; no protected payload paths found","intake_flag":"false","scope":"static path/extension audit; does not authorize or inspect external source content"},
    }
    out=ROOT/"verification/p2/host-verification-results.json"
    out.write_text(json.dumps(result,indent=2,ensure_ascii=False)+"\n",encoding="utf-8")
    print(json.dumps(result,indent=2,ensure_ascii=False))

if __name__=="__main__": main()
