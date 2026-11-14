# Scripture ingestion validator

This is a standalone Java 17 / Maven validation tool. It is not an Android runtime dependency.

## Public synthetic verification

From this directory run:

```sh
mvn --batch-mode --no-transfer-progress test
```

Public CI uses synthetic fixtures only. It must never download, cache, upload or process the protected Maale source.

## Restricted corpus run

Run only in an Owner-approved restricted workspace. First obtain the exact authorized archive and verify its SHA-256 against `content/manifests/maale-p1-source-manifest.v1.json`. Extract through `ArchiveSafetyValidator`, verify every extracted file against `content/manifests/maale-usfm-file-hashes.json`, then run `scripts/generate_marker_inventory.py` against the verified extracted USFM directory. Write the marker inventory only to restricted evidence storage. Do not copy it to this public repository without a separate review of the metadata disclosure risk.

Do not use shell tracing. Do not log file contents. Only error codes, IDs, counts and digests may leave the restricted workspace. Rights/provenance and specialist review remain separate from technical validation.
