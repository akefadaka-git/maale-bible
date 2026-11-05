# Contributing to Maale Bible

## A2 contribution boundary

During A2, contributions are limited to project documentation, source metadata, validation tooling, schemas and synthetic tests. Do not submit Bible text, verse excerpts, audio, scans, screenshots containing scripture, or derived content.

## Required practice

- Keep rights evidence separate from assumptions and preliminary research.
- Cite exact source URLs and record retrieval dates in source-register records.
- Never mark a source approved without written evidence from the competent rights holder.
- Do not weaken a rights gate to make a validation command pass.
- Use synthetic fixtures only; clearly label them as synthetic.
- Keep audit output free of scripture text and personal data.
- Add tests for new parser behavior and document known limitations.
- Do not add Android, backend, deployment, or CI implementation during A2.

## Local validation

Run the synthetic test suite from the repository root:

    python -m unittest discover -s tools/tests -v

Run the audit against an authorized local source package only after written rights approval:

    python tools/usfm_audit.py --manifest path/to/manifest.json --source-root path/to/source

A passing structural report is not rights approval or content acceptance.
