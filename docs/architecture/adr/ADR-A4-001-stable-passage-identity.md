# ADR-A4-001 — Stable Passage Identity

- **Status:** Proposed for P2 acceptance
- **Context:** Personal annotations must survive edition switching and edition removal; translations may differ in versification.
- **Decision:** Use a canonical passage value object keyed by canon, canonical book identity, chapter, verse and optional segment. Resolve each edition through explicit versification mappings.
- **Consequences:** Annotation identity is independent of translation text. Some passages may be unavailable or ambiguously mapped in an edition; the UI must disclose that state rather than guess.
- **Rejected:** Localized display strings as primary keys; edition-specific verse IDs as the only annotation identity; silent nearest-verse fallback.
- **Verification:** Unit tests for stable identity, one-to-one mapping, one-to-many mapping, unmapped references and ambiguous mappings.
- **Open:** Exact canonical identifier standard and versification mapping source must be evaluated before implementation.
