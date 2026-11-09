# A3.8 — Atomic registration OTP verification and challenge consumption

**Status:** implementation slice; draft PR only  
**Base:** A3.7 registration challenge creation  
**Scope:** verify registration OTP, decrement failed attempts, enforce expiry, and consume a successful challenge once.

## Behavior

- Unknown, expired, exhausted, already-consumed, and incorrect challenges return the same boolean failure result.
- Incorrect codes decrement `attempts_remaining` by one, using a conditional database update.
- A valid code sets `consumed_at` only if the challenge is still unconsumed, unexpired, and has attempts remaining.
- The initial five-attempt budget and five-minute expiry are inherited from A3.6/A3.7.
- PostgreSQL uses `SELECT ... FOR UPDATE`; the conditional `UPDATE` is the authoritative compare-and-set and protects against replay/stale updates on dialects that ignore row locks.
- No account is created or activated by this operation.
- No public HTTP route, SMS provider, OTP resend, login OTP, password recovery, or phone-change OTP is added.

## Verification required

- Unit tests cover successful single-use consumption, invalid-attempt decrement, exhaustion, expiry, malformed input, weak keys, and timezone validation.
- Exact-HEAD backend CI must pass before this slice can be considered verified.
- PostgreSQL concurrent-verifier integration testing and independent security review remain required before public exposure.
- SQLite unit tests do not prove PostgreSQL row-lock behavior.

## Security boundaries

The OTP HMAC key remains runtime server configuration and is never committed. The raw OTP is supplied only to the verification function and is not persisted or logged. Callers must apply IP/device/identifier rate limits before any public endpoint is enabled. Account activation remains a separate transaction and implementation stage after successful verification.
