# Maale Bible API

This directory contains the server-side API foundation within the canonical
akefadaka-git/maale-bible repository. It is separate from the Android app
module and does not replace the existing offline-first client.

## Local development

Requires Python 3.12 or 3.13.

    python -m venv .venv
    # Activate the environment, then:
    python -m pip install -e ".[test]"
    pytest
    uvicorn maale_bible_api.main:app --app-dir src --host 127.0.0.1 --port 8000

The only route currently enabled is GET /health/live. It is a process
liveness check, not a readiness check and not proof that a database or
authentication subsystem is operational.

## Security boundary

No registration, login, OTP, password, token, account or session endpoint is
enabled in this foundation slice. Do not add production secrets, SMS provider
credentials, database passwords, signing keys or real user data to this tree.
Authentication routes must remain disabled until subsequent slices add
reviewed persistence, unique constraints, Argon2id password handling,
single-use registration OTPs, abuse controls, rotating sessions and tests.

PostgreSQL is the intended production relational database. SQLite may be used
only for isolated tests where its behavior is not treated as proof of
PostgreSQL concurrency or locking guarantees.
