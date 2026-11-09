# ADR-A4-003 — Offline Search and Index Architecture

- **Status:** Proposed; engine and language-specific tokenization qualification remain open.
- **Context:** Search must work without network and support Maale, Amharic and English editions without changing displayed source text. Owner content-intake authorization was granted on 2026-11-08. The 2008 Maale Full Bible source archive has since passed recorded archive-integrity and structural checks and is staged for controlled processing; no generated content database or search index has yet been built.
- **Decision:** Define an engine-independent local search interface. Evaluate bundled SQLite FTS5 as the initial engine candidate. Isolate language/tokenizer adapters and store normalized search keys separately from immutable source text. Search normalization must never rewrite the displayed source text. Keep edition/source provenance on every indexed document and provide deterministic index rebuilds.
- **Tokenizer boundary:** Do not assume the default SQLite tokenizer is adequate for Maale or Amharic. The initial implementation may provide a clearly labelled baseline tokenizer for engineering and synthetic-corpus tests, but language-quality claims require a representative, rights-cleared corpus and specialist review. Preserve a route to a language-specific tokenizer without changing the search API.
- **Consequences:** Engine selection is conditional on Android packaging and compatibility tests on the supported API matrix. FTS5 is a candidate, not yet a fully qualified cross-language production decision. Indexes contain licensed scripture payload and must remain outside the public repository under the current distribution boundary.
- **Rejected:** Server-search dependency for this offline release; English-only tokenization assumptions; rewriting source text during normalization; committing raw scripture or a generated verse/search database to the public repository.
- **Verification:** Android device compatibility; exact, phrase, all/any and prefix correctness; tokenizer test corpus for each supported language; Unicode/Ethiopic normalization tests; deterministic index rebuild; index/source count reconciliation; offline query benchmarks; provenance and content-boundary scans.
- **Open blockers:**
  1. Run the FTS5 compatibility and query-correctness matrix on the exact final Android dependency/runtime configuration.
  2. Build a representative Maale and Amharic tokenizer corpus with expert-reviewed expected tokens; do not claim linguistic adequacy before review.
  3. Benchmark search latency and index size on realistic device/storage profiles.
  4. Verify reproducible rebuilds and source-to-index provenance/count reconciliation.
