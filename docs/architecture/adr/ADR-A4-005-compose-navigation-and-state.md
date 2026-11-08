# ADR-A4-005 — Compose Navigation and State Ownership

- **Status:** Proposed for P2 acceptance
- **Context:** Forty-two planned screens must remain navigable, accessible and resilient to process recreation.
- **Decision:** Use a single-activity Compose shell with typed routes, screen-level ViewModels/state holders, domain use cases and repository flows. Durable data stays in Room; SavedStateHandle stores only small transient state.
- **Consequences:** Composables avoid direct persistence and business logic. Each route declares loading/empty/error/partial/unavailable states and restoration behavior.
- **Rejected:** SQL/business logic in Composables; large content payloads in saved state; untyped stringly-typed navigation.
- **Verification:** Route coverage for all screen IDs, back-stack tests, process recreation, dynamic text and screen-reader semantics.
- **Open:** Whether feature boundaries merit separate Gradle modules is decided after build complexity and dependency graph evaluation.
