#!/usr/bin/env python3
"""Verify the Android metadata-only source manifest matches the frozen JSON manifest."""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
JSON_MANIFEST = ROOT / "content/manifests/maale-2008-source-manifest.json"
KOTLIN_MANIFEST = ROOT / "app/src/main/java/org/maale/bible/data/source/Maale2008SourceManifest.kt"


def main():
    manifest = json.loads(JSON_MANIFEST.read_text(encoding="utf-8"))
    kotlin = KOTLIN_MANIFEST.read_text(encoding="utf-8")
    source = manifest["source"]

    archive = re.search(
        r"SourceArchiveExpectation\(\s*(\d+)L,\s*\"([0-9a-f]{64})\"",
        kotlin,
    )
    assert archive, "Android manifest archive identity is missing"
    assert int(archive.group(1)) == source["archive_bytes"], "Archive byte count differs"
    assert archive.group(2) == source["archive_sha256"], "Archive SHA-256 differs"

    pattern = re.compile(
        r'ExpectedUsfmFile\((\d+),\s*\"([A-Z0-9]{3})\",\s*'
        r'\"([^\"]+)\",\s*(\d+)L,\s*\"([0-9a-f]{64})\"\)'
    )
    actual = [
        (int(order), book_id, path, int(size), digest)
        for order, book_id, path, size, digest in pattern.findall(kotlin)
    ]
    expected = [
        (item["canonical_order"], item["book_id"], item["path"], item["bytes"], item["sha256"])
        for item in manifest["source_files"]
    ]
    assert actual == expected, "Android source manifest differs from approved JSON metadata"
    assert manifest["governance"]["raw_scripture_committed"] is False
    print(json.dumps({
        "status": "PASS",
        "edition_id": source["edition_id"],
        "archive_sha256": source["archive_sha256"],
        "files": len(actual),
        "ordered_metadata_parity": "PASS",
        "raw_scripture_payload": "NOT PRESENT",
    }, indent=2))


if __name__ == "__main__":
    main()
