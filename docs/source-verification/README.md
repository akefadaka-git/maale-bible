# Source Verification Procedure

## 1. Preconditions

Do not acquire or process a source package for product use until the rights gate has written approval. Public availability does not establish product permission. Keep any separately authorized source package outside this public repository during A2.

## 2. Intake evidence

For an authorized package, record:
- exact edition and revision;
- publisher and rights holder;
- source URL and retrieval timestamp;
- permission evidence reference;
- package filename, size and SHA-256;
- USFM/USX format and encoding;
- source archive hash and per-file hashes;
- reviewer and chain-of-custody notes.

## 3. Structural checks

The initial audit utility checks:
- UTF-8 readability;
- USFM book ID, chapter and verse markers;
- duplicate book IDs and chapter numbers within each file;
- positive chapter and verse numbers;
- verses appearing only after a chapter marker;
- Unicode replacement characters;
- presence and count of Ethiopic-range code points;
- per-file SHA-256 equality with the manifest.

These checks are intentionally conservative and incomplete. They do not validate book ordering, expected canon, missing chapters/verses, cross-file consistency, verse sequence gaps, USFM marker grammar, footnotes, poetry, tables, character normalization, orthographic correctness, or translation accuracy. Those require later controlled specifications and specialist review.

## 4. Reproducible execution

Use Python 3.10 or later and the standard library. Run synthetic tests before processing any authorized package:

    python -m unittest discover -s tools/tests -v

Then run:

    python tools/usfm_audit.py --manifest MANIFEST.json --source-root SOURCE_DIRECTORY --output audit-report.json

The manifest must contain a SHA-256 for every file. A hash mismatch or malformed file fails the structural audit. If rights status is not APPROVED, the report must retain a BLOCKED rights gate even when all structural checks pass.

## 5. Independent acceptance

A2 acceptance requires:
- rights-holder and license reconciliation;
- approved book inventory and canon decision;
- expected chapter/verse inventory from a trusted reference;
- independent Maale-language and Ethiopic-script review;
- complete reproducible validation report;
- source package and manifest hashes;
- Owner sign-off on the immutable baseline.

Never interpret a green structural audit as permission to publish scripture.
