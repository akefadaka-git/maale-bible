# ADR-A4-004 — Local Personal Data and Backup/Restore

- **Status:** Proposed for P2 acceptance
- **Context:** This release has no accounts or cloud sync; user-created records must remain local and be recoverable through user-controlled backup.
- **Decision:** Store personal records in Room/app-private storage. Provide an explicitly user-initiated, versioned backup with manifest and per-entry integrity; validate restore in staging before merge/replace.
- **Consequences:** Backup excludes licensed scripture payloads by default and stores stable references. Missing references are reported. Merge is the default; replace requires explicit confirmation.
- **Rejected:** Automatic cloud backup; silent overwrite; bundling licensed corpus content in personal backups.
- **Security recommendation:** Encrypt backup archives using Android Keystore-backed key management, subject to recovery/usability validation. Whole-database encryption/app lock remains an open decision.
- **Verification:** Round-trip, corruption, incompatible version, interrupted restore, conflict and missing-reference tests.
