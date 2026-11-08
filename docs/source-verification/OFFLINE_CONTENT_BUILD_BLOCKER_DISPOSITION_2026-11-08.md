# Offline Content Build — Blocker Disposition

**Updated:** 2026-11-08  
**Workstream:** A4 offline content and search  
**Target branch:** `feat/a4-offline-study-suite`  
**Distribution boundary:** No raw scripture, generated verse database, or full-text search index may be committed to the public repository.

## Source baseline

The 2008 Maale Full Bible (MDYTWF / mdyeth) archive has recorded integrity and structural verification:

- Archive: `mdyeth_usfm.zip`
- Size: 1,809,863 bytes
- SHA-256: `4d08dbde11d34c33c630986a59282bcf85bfdd21799c20f97f3b93ef39a32d11`
- 66 USFM files; 66/66 per-file hashes matched
- 66 unique book IDs; 1,189 chapters
- 30,506 verse-marker lines; 31,102 represented verse spans
- ZIP CRC, UTF-8/NFC and control-character checks: PASS
- GPG signature cryptographically valid; key trust UNDEFINED

The archive must be re-hashed and staged files revalidated immediately before any build. The evidence does not authenticate the signer identity, independently authenticate the rights-holder response, or establish linguistic quality.

## Blocker register

| ID | Gate | Status | Resolution / required evidence |
|---|---|---|---|
| CB-01 | Exact source identity and integrity | VERIFIED WITH CAVEATS | Reconfirm archive SHA-256 and all staged file hashes before build. Preserve source manifest and archive provenance. |
| CB-02 | Rights and attribution | PARTIAL | Owner authorized intake. Record CC BY-SA 4.0 and required attribution. Original email headers/signatory authority remain unauthenticated; images have separate restrictions. Do not claim independent legal authentication. |
| CB-03 | Canonical passage identity | ACCEPTED — IMPLEMENTATION PENDING | ADR-A4-001 accepted: canon ID `maale-bible:canon:66-v1`, uppercase USFM three-letter book IDs validated against a frozen 66-book registry, and explicit edition-specific versification mappings. Implement registry, schema, source mappings and tests; do not claim cross-edition equivalence or silently map split/merged verses. |
| CB-04 | USFM parser qualification | NOT RUN | Implement marker-aware parsing; distinguish verse text from headings, footnotes, cross-references and formatting markers. Reconcile book/chapter/verse/span counts against the approved manifest. |
| CB-05 | Deterministic SQLite content database | NOT RUN | Build outside the public repository. Fix schema/parser versions, row ordering, encoding, pragmas and metadata policy; exclude nondeterministic timestamps. Rebuild twice and compare artifact hashes. |
| CB-06 | FTS5 index and tokenizer | PARTIAL / NOT QUALIFIED | ADR-A4-003 remains Proposed. Qualify bundled SQLite FTS5 against the Android runtime matrix. Maale and Amharic tokenization require representative expected-token corpora and specialist review. |
| CB-07 | Android resource integration | NOT RUN | Integrate through a versioned local resource/import contract. Verify app-private storage, atomic install/rollback, offline launch and edition/source provenance. |
| CB-08 | Language quality | PENDING | Independent Maale/Ethiopic-script specialist review remains required. No linguistic sign-off is claimed. |
| CB-09 | Public repository content boundary | CONTROL REQUIREMENT | CI/repository scans must confirm no scripture text, archive, generated verse DB or full-text index is staged or committed. Only code, schemas, metadata, provenance and synthetic fixtures may be public. |
| CB-10 | Exact-HEAD consolidated verification | NOT RUN | Run parser, import, DB, FTS, Android compatibility, boundary and deterministic-rebuild checks against one exact final commit SHA. |

## Safe implementation sequence

1. Reconfirm the exact archive and staged-file hashes.
2. Implement and test a marker-aware USFM parser using synthetic fixtures first.
3. Keep edition-native references for imported source records until ADR-A4-001 receives an Owner-approved canonical identity decision.
4. Build the content DB in private/local staging; include source ID, edition ID, source file, source file hash, parser version and source reference on imported records.
5. Build the FTS5 index separately from immutable display text; record tokenizer/normalizer versions and clearly label unqualified Maale/Amharic behavior.
6. Rebuild twice in clean staging and compare deterministic outputs.
7. Integrate the resource through the offline Android resource contract and verify installation, rollback and offline search.
8. Run exact-HEAD consolidated verification and a repository content-boundary scan.

## Current release disposition

**NOT READY FOR CONTENT-INTEGRATION CLOSURE.** The source archive is ready for controlled processing after re-hash, but canonical identity implementation/registry fixture, parser qualification, database generation, tokenizer qualification, Android integration and exact-HEAD verification remain open. No database or index build is represented as complete by this record.

No merge, release signing, deployment or publication is authorized by this blocker record.
