# ADR-A4-003 — Offline Search and Index Architecture

- **Status:** Proposed for P2 acceptance
- **Context:** Search must work without network and support Maale, Amharic and English editions without changing displayed source text.
- **Decision:** Define an engine-independent local search interface. Evaluate SQLite FTS5 as the initial candidate; isolate tokenizer adapters and store normalized search keys separately from immutable source text.
- **Consequences:** Engine selection is conditional on Android packaging/compatibility tests. Default tokenizers are not assumed adequate for Maale or Amharic.
- **Rejected:** Server search dependency; tokenizing by English-only assumptions; rewriting source text during normalization.
- **Verification:** Android device compatibility, exact/phrase/all/any/prefix correctness, tokenizer test corpus, index rebuild and offline query benchmarks.
- **Open:** Final engine and tokenizer choices require empirical evaluation; no content corpus is authorized yet.
