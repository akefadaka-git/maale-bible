# ADR-A4-005 — Compose Navigation and State Ownership

- **Status:** Proposed for P2 acceptance; implementation verification remains open.
- **Context:** Forty-two planned screens must remain navigable, accessible and resilient to process recreation.
- **Decision:** Use a single-activity Compose shell with typed routes, screen-level ViewModels/state holders, domain use cases and repository flows. Durable local data is backed by the AndroidX bundled SQLite driver through repository/data-source boundaries (ADR-A4-004); SavedStateHandle stores only small transient state.
- **Consequences:** Composables avoid direct persistence and business logic. Each route declares loading/empty/error/partial/unavailable states and restoration behavior. Current application shell implements only Home, Books and Search top-level destinations plus a reader placeholder; the 42-screen route inventory is not evidence that all routes are implemented.
- **Rejected:** SQL/business logic in Composables; large content payloads in saved state; untyped stringly-typed navigation.
- **Verification:** Route coverage for all screen IDs, typed route argument tests, back-stack tests, process recreation, dynamic text and screen-reader semantics. P2-V6 cannot pass on route inventory alone; all 42 routes and required journeys must be implemented and exercised.
- **Open:** Whether feature boundaries merit separate Gradle modules is decided after build complexity and dependency graph evaluation.
