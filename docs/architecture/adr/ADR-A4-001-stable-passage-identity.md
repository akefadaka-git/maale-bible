# ADR-A4-001 — Stable Passage Identity

- **Status:** Accepted for the current offline release by Owner direction on 2026-11-08.
- **Decision scope:** This decision establishes the application's internal identity contract. It does not certify linguistic correctness or assert that different editions share identical versification.

## Context

Personal annotations must survive edition switching and edition removal. The Maale source is a 66-book USFM Full Bible. Other language editions may use different book naming, canon scope, chapter divisions, verse boundaries, or versification conventions. Edition-native USFM references alone are therefore insufficient as the cross-edition identity model.

## Accepted canonical identity

Use a versioned, project-owned canon namespace:

- **Canon ID:** `maale-bible:canon:66-v1`
- **Book identity:** the canonical USFM three-letter book identifier, normalized to uppercase and validated against the frozen 66-book registry for this canon version (for example, `GEN`, `EXO`, `MAT`, `REV`).
- **Passage coordinate:** `canon_id + book_id + chapter_number + verse_number + optional_segment`.
- **Edition identity:** a separate immutable edition/source identifier. Never encode translation name, localized book title, display language or source file path into the canonical passage key.
- **Canonical key serialization:** `maale-bible:canon:66-v1/<BOOK>/<CHAPTER>/<VERSE>[/<SEGMENT>]`, with positive decimal chapter/verse numbers and an optional stable segment token. Display references are localized separately.

The 66-book registry is versioned data, not inferred from alphabetical ordering or translated names. Changing canon membership or book identity requires a new canon version and an explicit migration; do not mutate v1 in place.

## Edition versification and mapping

Every edition must declare its own versification profile and map its edition-native passage units to canonical passage coordinates. Preserve the source-native reference and source markers alongside every mapping.

Allowed mapping cardinalities and states:

- `EXACT`: one edition unit maps to one canonical unit.
- `SPLIT`: one edition unit maps to multiple canonical units.
- `MERGED`: multiple edition units map to one canonical unit.
- `PARTIAL`: only a documented portion maps.
- `UNMAPPED`: no defensible mapping exists.

Mapping records must identify the edition, source-native book/chapter/verse reference, canonical target(s), mapping state, provenance, and review status. Never silently use nearest-verse, ordinal, string, or display-label matching. Unmapped or ambiguous passages remain readable under their edition-native reference; annotation transfer must disclose the limitation and require an explicit user choice where needed.

## Maale v1 source handling

For the selected 2008 Maale Full Bible (MDYTWF / mdyeth), import the USFM book IDs and verse markers as edition-native source references. Map them to the v1 canonical coordinates only where the source reference and the frozen 66-book registry support an exact, reviewable correspondence. Record mapping provenance and validation status. Do not assume that a matching USFM verse number proves semantic or versification equivalence with another edition.

## Stable data model requirements

At minimum, separate these concepts:

1. `canon_registry`: immutable canon ID/version and its ordered book registry.
2. `canonical_book`: canon ID, USFM book ID, canonical order and registry metadata.
3. `canonical_passage`: canonical book/chapter/verse/segment identity.
4. `edition`: immutable edition ID, language, title, publisher/rights metadata and source provenance.
5. `edition_passage`: edition-native passage coordinate and source file/hash provenance.
6. `passage_mapping`: explicit edition-native-to-canonical mapping with state, cardinality, review status and provenance.

Use composite unique constraints and foreign keys to prevent duplicate or orphaned identities. Keep source text outside the canonical identity tables and preserve it byte/text-faithfully according to the parser contract.

## Consequences and limitations

- Annotation identity is independent of translation text and display labels.
- This project-owned namespace avoids claiming adoption of an external versification standard that has not been validated against the selected corpus.
- Canonical v1 provides stable internal coordinates; it does not automatically establish cross-edition alignment.
- The initial Maale edition can use exact source-native references as import coordinates while mappings are reviewed.
- Specialist review remains required for Maale language, Ethiopic script, verse segmentation and any non-exact mapping.
- Adding deuterocanonical/apocryphal books or another canon scope requires a separately versioned canon registry and explicit Owner decision.

## Rejected

- Localized display strings as primary keys.
- Edition-specific verse IDs as the only identity for user annotations.
- Treating all editions as if they share one versification.
- Silent nearest-verse or ordinal fallback.
- Mutating the v1 66-book registry to accommodate a different canon.

## Verification required

- Registry contains exactly the approved 66 unique USFM book IDs, with no missing or unexpected IDs.
- Canonical key serialization is deterministic and round-trips without loss.
- Positive chapter/verse constraints and segment validation are enforced.
- Unique and foreign-key constraints reject duplicate/orphan identities.
- Tests cover EXACT, SPLIT, MERGED, PARTIAL, UNMAPPED and ambiguous mappings.
- Edition removal does not delete canonical identities or user annotations.
- Switching editions never rewrites the user's canonical annotation anchor.
- Unmapped/ambiguous references are disclosed; no nearest-verse fallback occurs.
- Source-to-edition and edition-to-canonical mappings retain auditable provenance.

**Acceptance boundary:** The identity contract is accepted. The exact 66-book registry fixture, Maale source mapping table, parser correctness and cross-edition mappings still require implementation and verification. This ADR acceptance is not a claim that those implementation gates have passed.
