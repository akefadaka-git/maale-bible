# A3 Study Data Encryption and Cross-Device Recovery Design

Status: design baseline; implementation and independent review required.
Scope: private study metadata only. Scripture content and licensed source assets are out of scope.

## Security objectives
- Encrypt the complete study database at rest, including SQLite pages, WAL/journal files, temporary copies and exported backups.
- Separate the database encryption key (DEK) from the user's recovery credential.
- Permit recovery on a new device without the original Android Keystore.
- Make plaintext-to-encrypted migration atomic and recoverable.
- Fail closed on wrong credentials, corrupted ciphertext, unsupported formats and interrupted migrations.
- Never overwrite the only recoverable copy silently.

## Encrypted database
Adopt SQLCipher for Android; do not implement page encryption manually or treat Android file-level encryption as a substitute.
- Generate a cryptographically random 256-bit DEK per database.
- Open every database connection through SQLCipher only after cutover.
- Wrap the DEK locally with Android Keystore for routine unlock on the current device.
- Verify that WAL, journal, temporary and backup files are protected.
- Keep raw key material out of preferences, logs, analytics, crash reports, intents and backups.
- Pin and review the exact SQLCipher version, native artifacts, license, provenance, ABI coverage and vulnerability status before adoption.

## Cross-device recovery credential
Android Keystore keys are device-bound and cannot be the only recovery mechanism.
- Generate a random high-entropy recovery secret (at least 256 bits); show it once in a user-confirmed recovery flow. A short PIN is not a recovery key.
- Derive a key-encryption key (KEK) from the recovery secret with a reviewed memory-hard KDF, preferably Argon2id, using a random per-backup salt and versioned bounded parameters.
- Wrap the random DEK with AES-256-GCM using a unique nonce and authenticated version/context metadata.
- The portable envelope contains encrypted database bytes, wrapped DEK, salt, nonce, KDF parameters, algorithm identifiers, format/schema versions and integrity metadata. It never contains plaintext DEK or recovery secret.
- On a new device, derive the KEK from the user's recovery secret, authenticate and unwrap the DEK, then open the encrypted database. No originating-device Keystore is required.
- Wrong keys and tampered envelopes must fail without modifying destination data.
- Recovery-key rotation must unlock the old DEK, wrap it under the new recovery secret, verify the new envelope, and only then let the user retire the old key.

## Portable backup format
Use a versioned envelope (proposed identifier: MAALE-STUDY-ENC2). Authenticate all security-relevant header fields as AEAD associated data. Enforce strict payload-size and KDF-parameter bounds before derivation. Reject unknown algorithms and versions by default. Plaintext export must not be the default backup path.

## Plaintext database migration
1. Detect the legacy database and stop all writers.
2. Create a separate encrypted database with a fresh DEK.
3. Copy records through validated application models; never copy raw pages between different database formats.
4. Verify counts, primary keys, model invariants and representative record hashes.
5. Close both databases; fsync encrypted database and required metadata.
6. Atomically switch the active database only after successful verification.
7. Retain a bounded rollback copy until encrypted reopen and post-migration checks pass; then remove plaintext database, WAL, journal and temporary files.
8. On any failure, preserve and reopen the original database. Never delete the source before verifying the encrypted target.
Retention duration and user disclosure require an explicit product decision. Do not claim secure deletion on flash storage.

## Required verification
Cryptography: known-answer tests; wrong-key, tampered header/nonce/wrapped-key/ciphertext tests; nonce uniqueness; unknown-version rejection; prove key material never appears in logs or plaintext files.
Database: inspect database/WAL/journal/temp/export files for plaintext sentinels; prove ordinary SQLite cannot open the encrypted database; test CRUD, migration, process death, low storage, interrupted writes and restart integrity.
Cross-device: restore on a clean emulator/device with no original Keystore; wrong key and damaged backup must preserve destination data; test interrupted key rotation, schema upgrades and recovery UX.
Release: independent cryptographic review, dependency provenance/license/vulnerability review, supported ABI builds, free CI, privacy disclosure, accessibility and threat-model review.

## Current boundary and release blocker
The existing AES-GCM helper uses an Android Keystore key on the originating device. It protects a local backup string but is not cross-device recoverable and does not encrypt the live SQLite database. Treat it as transitional only. The current plaintext database and plaintext JSON export remain production blockers until SQLCipher migration and portable recovery are implemented and verified on the exact release candidate SHA.