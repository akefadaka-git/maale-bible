# Security Policy

## Scope

During A2 this repository contains documentation and local source-audit tooling. No production application or backend is in scope.

## Reporting

Do not publish sensitive rights-holder correspondence, personal contact details, credentials, private download links, or unpublished source files in public issues. Report security concerns through the repository owner's private contact channel.

## Source-content handling

- Never commit scripture source archives, extracted text, audio, or private licensing evidence.
- Keep authorized source packages in access-controlled local storage until a separate content-ingestion approval exists.
- Do not place credentials or access tokens in manifests, reports, source control, or issue comments.
- Audit reports should contain file paths, hashes, structural counts and findings only; do not include verse text.

## Tool limitations

The USFM audit tool is a preliminary structural control. It is not a sandbox for untrusted files and should be run on trusted, locally acquired packages in an appropriately restricted environment. Review source packages for malware independently.
