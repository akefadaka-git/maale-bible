import json,re,sqlite3,statistics,time,unicodedata,zipfile
from pathlib import Path
from io import BytesIO
ROOT=Path(__file__).resolve().parents[2]
def main():
    screens=json.loads((ROOT/"verification/p2/screen-contracts.json").read_text())
    assert len(screens)==42 and len({x["route"] for x in screens})==42
    assert [x["screenId"] for x in screens]==[f"SCR-{i:03d}" for i in range(1,43)]
    req=(ROOT/"docs/archive/superseded-plans/A4_REQUIREMENTS_AND_ACCEPTANCE_MATRIX.md").read_text()
    ids=sorted(set(re.findall(r"\|\s*((?:NFR|LIB|READ|ANNO|SEARCH|REF|STUDY|PLAN|MAP|EXPORT|SET|A11Y|DEFER)-\d+)\s*\|",req)))
    assert ids, "requirement IDs not parsed"
    # Screen contracts are a route baseline; requirement-to-route ownership still requires detailed review.
    assert all(x["testId"] and len(x["states"])==6 for x in screens)
    fixtures=[("EXACT",1,1),("SPLIT",1,2),("MERGED",2,1),("PARTIAL",1,1),("UNMAPPED",1,0)]
    for status,canonical,edition in fixtures:
        assert (edition>0)==(status!="UNMAPPED")
        if status=="EXACT": assert canonical==edition==1
        if status=="SPLIT": assert canonical==1 and edition>1
        if status=="MERGED": assert canonical>1 and edition==1
    source="ሰላም እንዴት ነህ — Maale 123"
    key=unicodedata.normalize("NFC",source.casefold())
    assert source=="ሰላም እንዴት ነህ — Maale 123" and unicodedata.normalize("NFC",key)==key
    def zipcheck(data):
        with zipfile.ZipFile(BytesIO(data)) as z:
            names=set()
            for x in z.infolist():
                n=x.filename.replace("\\","/")
                if n.startswith("/") or any(p in ("","..",".") for p in n.split("/")): raise ValueError("unsafe-path")
                if n in names: raise ValueError("duplicate-path")
                names.add(n)
            return names
    good=BytesIO()
    with zipfile.ZipFile(good,"w") as z: z.writestr("manifest.json","{}")
    assert "manifest.json" in zipcheck(good.getvalue())
    bad=BytesIO()
    with zipfile.ZipFile(bad,"w") as z: z.writestr("../escape","x")
    try: zipcheck(bad.getvalue()); raise AssertionError("path traversal accepted")
    except ValueError as e: assert str(e)=="unsafe-path"
    db=sqlite3.connect(":memory:")
    db.execute("CREATE VIRTUAL TABLE f USING fts5(body)")
    db.executemany("INSERT INTO f(body) VALUES(?)",[(f"synthetic alpha beta row {i}",) for i in range(100000)])
    samples=[]
    for _ in range(30):
        t=time.perf_counter(); db.execute("SELECT rowid FROM f WHERE f MATCH 'alpha beta' LIMIT 20").fetchall(); samples.append((time.perf_counter()-t)*1000)
    host={"records":100000,"p50_ms":round(statistics.median(samples),3),"p95_ms":round(sorted(samples)[28],3),"engine":"host sqlite3; not Android evidence"}
    db.close()
    forbidden={".usfm",".sfm",".mp3",".m4a",".wav",".ogg",".epub",".pdf",".tif",".tiff"}
    found=[str(p.relative_to(ROOT)) for p in ROOT.rglob("*") if p.is_file() and ".git" not in p.parts and p.suffix.lower() in forbidden]
    assert not found,found
    result={"screen_contracts":42,"requirement_ids_found":len(ids),"requirement_traceability":"PARTIAL: route contracts exist; every requirement-to-route mapping not yet independently validated","mapping_fixtures":5,"unicode":"PASS synthetic NFC/source immutability only","backup":"PARTIAL: ZIP traversal negative test; no crypto/KDF/restore implementation","host_fts_benchmark":host,"android_fts5":"requires instrumented API matrix","content_boundary":"PASS; no protected payload extensions found","linguistic_review":"DEFERRED"}
    out=ROOT/"verification/p2/host-verification-results.json"; out.write_text(json.dumps(result,indent=2,ensure_ascii=False)+"\n")
    print(json.dumps(result,indent=2,ensure_ascii=False))
if __name__=="__main__": main()
