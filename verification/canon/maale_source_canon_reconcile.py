#!/usr/bin/env python3
"""Reconcile the authorized 2008 Maale source metadata with canon 66-v1.

The manifest contains metadata and file hashes only; no scripture text.
This check verifies book identity/order and recorded structural diagnostics.
It does not claim linguistic review or cross-edition versification alignment.
"""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
MANIFEST = ROOT / "content/manifests/maale-2008-source-manifest.json"
REGISTRY = ROOT / "content/canon/66-v1/registry.json"
APPROVED = ROOT / "content/manifests/candidate-book-inventory.json"


def validate(manifest, registry, approved):
    books = registry["books"]
    expected = [book["usfm_id"] for book in books]
    approved_ids = [book["usfm_id"] for book in approved["books"]]
    source_files = manifest["source_files"]
    source_ids = [item["book_id"] for item in source_files]
    paths = [item["path"] for item in source_files]
    structural = manifest["structural_validation"]

    assert registry["canon_id"] == "maale-bible:canon:66-v1"
    assert registry["status"] == "FROZEN" and registry["version"] == 1
    assert len(expected) == 66 and [b["order"] for b in books] == list(range(1, 67))
    assert approved_ids == expected, "Approved A2 inventory differs from frozen canon"
    assert len(source_files) == 66, "Expected exactly 66 source USFM files"
    assert len(set(source_ids)) == 66, "Duplicate source book IDs"
    assert set(source_ids) == set(expected), (
        f"Source/canon mismatch: missing={sorted(set(expected)-set(source_ids))}; "
        f"unexpected={sorted(set(source_ids)-set(expected))}"
    )
    assert len(set(paths)) == 66, "Duplicate source file paths"
    assert [item["canonical_order"] for item in source_files] == list(range(1, 67))
    assert source_ids == expected, "Source manifest is not emitted in frozen canonical order"
    for item, book in zip(source_files, books):
        assert item["book_id"] == book["usfm_id"]
        assert item["path"].endswith(".usfm") and book["usfm_id"] in item["path"]
        assert isinstance(item["bytes"], int) and item["bytes"] > 0
        assert re.fullmatch(r"[0-9a-f]{64}", item["sha256"])

    assert manifest["source"]["edition_id"] == "MDYTWF / mdyeth"
    assert manifest["source"]["archive_sha256"] == (
        "4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11"
    )
    assert manifest["canonical_identity"]["canon_id"] == registry["canon_id"]
    assert manifest["canonical_identity"]["book_count"] == 66
    assert structural["usfm_file_count"] == 66
    assert structural["unique_book_ids"] == 66
    assert structural["missing_book_ids"] == []
    assert structural["unexpected_book_ids"] == []
    assert structural["duplicate_book_ids"] == []
    assert structural["filename_book_id_mismatches"] == []
    assert structural["chapter_count"] == 1189
    assert structural["chapter_sequences_contiguous_from_one"] is True
    assert structural["chapters_without_verse_markers"] == 0
    assert structural["verse_marker_lines"] == 30506
    assert structural["represented_verse_spans_including_ranges"] == 31102
    assert structural["verse_range_marker_instances"] == 238
    assert structural["verse_order_or_overlap_anomalies"] == 0
    assert structural["utf8_valid_files"] == 66
    assert structural["unicode_replacement_characters"] == 0
    assert structural["non_nfc_files"] == 0
    assert structural["unexpected_control_characters"] == 0
    assert manifest["governance"]["raw_scripture_committed"] is False
    return {
        "source_book_ids": len(source_ids),
        "registry_book_ids": len(expected),
        "ordered_identity_parity": "PASS — exact 66/66 ordered IDs",
        "approved_inventory_parity": "PASS — exact ordered match",
        "per_file_metadata": "PASS — unique paths, positive sizes, SHA-256 format",
        "chapter_structure": "PASS — 1,189 chapters; contiguous from 1; no empty chapters",
        "verse_structure": "PASS — 30,506 markers; 31,102 represented spans; 238 ranges; no order/overlap anomalies",
        "encoding": "PASS — UTF-8 66/66; zero replacement characters, non-NFC files or unexpected controls",
        "scope": "Metadata and structural evidence only; no scripture text or cross-edition mapping asserted",
        "linguistic_review": "DEFERRED — Owner-accepted residual risk"
    }


def main():
    manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
    registry = json.loads(REGISTRY.read_text(encoding="utf-8"))
    approved = json.loads(APPROVED.read_text(encoding="utf-8"))
    result = validate(manifest, registry, approved)
    print(json.dumps({"status": "PASS", "canon_id": registry["canon_id"], **result}, indent=2))


if __name__ == "__main__":
    main()
