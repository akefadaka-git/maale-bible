# Maale Language and Script Review Protocol

**Status:** PROCEDURE READY; SPECIALIST EXECUTION DEFERRED — OWNER RISK-ACCEPTED 2026-11-07  
**Target candidate:** 2008 Maale Full Bible — MDYTWF / mdyeth  
**Language identifier:** mdy (candidate metadata; verify against edition evidence)

## 1. Purpose and non-goals

This protocol defines review evidence for encoding, Unicode, Ethiopic-script handling, and Maale linguistic quality. It does not assume that every file uses Ethiopic script, that Unicode code-point presence proves correct orthography, or that automated checks can establish translation accuracy.

The exact source package has been acquired and machine-checked. This protocol uses no scripture text or excerpts in public records.

## 2. Reviewer roles

Record named individuals, qualifications, affiliation/conflict disclosures, review date, and signed findings for:

- Maale language specialist: language identity, orthography, spelling, morphology, lexical consistency, and intelligibility.
- Ethiopic-script / Unicode specialist: script repertoire, combining marks, normalization, punctuation, and rendering-sensitive sequences.
- USFM specialist: marker structure, encoding declaration, and preservation of semantic markup.
- Independent reviewer: reproducibility and adjudication of disputed findings.

One person may cover multiple roles only when their competence is documented; the independent reproducibility review must remain independent of the original validation run.

## 3. Technical checks

After separate Owner authorization for source acquisition and inspection:

1. Preserve original bytes and record archive and per-file SHA-256 values.
2. Detect encoding without silently transcoding; record declared encoding, detected encoding, decoder, and strict-decoding result.
3. Reject undecodable byte sequences and replacement characters introduced by lossy decoding.
4. Report Unicode code points and Unicode names for suspicious or non-printing characters, without emitting scripture text in logs.
5. Record normalization form (NFC/NFD) as an observation. Do not normalize source content in place.
6. Identify unexpected control, format, private-use, unassigned, or bidirectional-control characters for specialist review.
7. Report script distribution by code-point ranges as diagnostics only. Relevant Ethiopic ranges include Ethiopic U+1200–U+137F, Ethiopic Supplement U+1380–U+139F, Ethiopic Extended U+2D80–U+2DDF, and Ethiopic Extended-A U+AB00–U+AB2F. These ranges are not a whitelist and do not establish that the text is correct.
8. Verify that punctuation, word separators, combining marks, and numerals are handled consistently with the approved Maale orthography.
9. Verify round-trip preservation through the selected USFM parser/serializer using byte-level or code-point-level comparisons appropriate to the transformation.

All reports should prefer counts, code-point identifiers, file paths and issue codes over text excerpts. Do not print or commit scripture text.

## 4. Linguistic review

The Maale specialist must assess, against a documented orthography/reference agreed with the Owner:

- Language and dialect identification.
- Orthographic conventions and variant spellings.
- Character and diacritic usage.
- Word segmentation and punctuation.
- Consistency of names, loanwords, and recurring terms.
- Obvious omissions, duplications, or encoding-corruption symptoms.
- Rendering and reading usability on representative target devices, if and when UI work is separately authorized.

The reviewer must distinguish objective defects from editorial preferences and provide reproducible examples in a restricted evidence record. Public repository findings should use redacted issue identifiers and must not reproduce copyrighted verses.

## 5. Acceptance criteria

A review is acceptable only when:

- Reviewer identities and competence are recorded.
- Every source file has a recorded strict-decoding result.
- No unexplained replacement characters or undecodable sequences remain.
- All flagged controls, normalization differences and script anomalies have dispositions.
- Maale orthography and language findings are signed by the named specialist.
- Any unresolved linguistic or script issue is explicitly risk-accepted by the Owner; automated test success is not a substitute.
- Independent rerun reproduces technical diagnostics.
- The final report identifies exact source archive hash and file-manifest revision.

## 6. Current disposition

| Item | Status |
|---|---|
| Protocol and acceptance criteria | COMPLETE |
| Named Maale specialist | MISSING |
| Named Ethiopic-script reviewer | MISSING |
| Real-source encoding diagnostics | PASS — machine diagnostics on 66/66 files |
| Linguistic review | NOT RUN |
| Independent reproducibility | NOT RUN |
| Owner acceptance | ACCEPTED — residual specialist-review risk explicitly deferred (2026-11-07) |

The Owner has accepted specialist review as a deferred residual risk; no specialist sign-off is claimed. Keep `content_intake_authorized=false`. This protocol does not authorize public source publication or third-party asset intake.
