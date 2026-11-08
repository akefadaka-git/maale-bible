# ADR-A4-002 — Resource Integrity and Rights Gate

- **Status:** Proposed for P2 acceptance
- **Context:** Offline editions and study resources are immutable packages with independent rights and provenance.
- **Decision:** Require a versioned manifest, licence/attribution metadata, payload hashes, compatibility metadata and a fail-closed lifecycle. Stage and validate resources before atomically activating them.
- **Consequences:** A valid hash proves integrity relative to the manifest, not publisher authenticity. Signature trust is a separate policy. Unresolved rights prevent activation.
- **Rejected:** Direct extraction into active storage; treating a hash as proof of licence or authenticity; partial install marked successful.
- **Verification:** State transition tests, malformed archive/path traversal tests, hash mismatch tests, interrupted install and low-storage rollback tests.
- **Open:** Package signature trust policy and supported archive format require further evidence.
- **Governance:** No import or content intake until separately authorized by the Owner.
