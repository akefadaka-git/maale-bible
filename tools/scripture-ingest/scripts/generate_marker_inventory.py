#!/usr/bin/env python3
"""Restricted-workspace-only USFM marker inventory generator. Never emits scripture text."""
import argparse, collections, hashlib, json, pathlib, re
MARKER = re.compile(r"\\([A-Za-z0-9]+|\\*)")
def sha256(path):
    h=hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda:f.read(1024*1024),b""): h.update(chunk)
    return h.hexdigest()
def main():
    ap=argparse.ArgumentParser()
    ap.add_argument("--corpus",required=True,type=pathlib.Path)
    ap.add_argument("--hash-manifest",required=True,type=pathlib.Path)
    ap.add_argument("--output",required=True,type=pathlib.Path)
    a=ap.parse_args()
    manifest=json.loads(a.hash_manifest.read_text(encoding="utf-8"))
    root=a.corpus.resolve(); total=collections.Counter(); by_file={}; seen=set()
    for item in manifest["files"]:
        book=item["book_id"]
        if book in seen: raise SystemExit("DUPLICATE_BOOK_ID")
        seen.add(book)
        p=(root/item["path"]).resolve()
        if not p.is_relative_to(root): raise SystemExit("PATH_ESCAPE")
        if not p.is_file() or p.stat().st_size != item["bytes"] or sha256(p) != item["sha256"]:
            raise SystemExit("FILE_INTEGRITY_MISMATCH")
        text=p.read_bytes().decode("utf-8","strict")
        counts=collections.Counter(MARKER.findall(text))
        total.update(counts)
        by_file[book]={"markerKinds":len(counts),"markerOccurrences":sum(counts.values())}
    if len(by_file)!=66: raise SystemExit("BOOK_INVENTORY_MISMATCH")
    result={"profileVersion":"1.0.0","sourceId":manifest["source_id"],"sourceArchiveSha256":manifest["source_archive_sha256"],"bookCount":len(by_file),"markerCounts":dict(sorted(total.items())),"perBookMetadata":dict(sorted(by_file.items())),"scriptureTextIncluded":False}
    a.output.parent.mkdir(parents=True,exist_ok=True)
    a.output.write_text(json.dumps(result,ensure_ascii=False,sort_keys=True,indent=2)+"\n",encoding="utf-8")
    print("MARKER_INVENTORY_GENERATED_REDACTED")
if __name__=="__main__": main()
