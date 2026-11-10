from pathlib import Path

from alembic import command
from alembic.config import Config
from sqlalchemy import create_engine, inspect


def test_initial_auth_migration_creates_expected_tables() -> None:
    backend_root = Path(__file__).resolve().parents[1]
    config = Config(str(backend_root / "alembic.ini"))
    engine = create_engine("sqlite+pysqlite:///:memory:")

    with engine.begin() as connection:
        config.attributes["connection"] = connection
        command.upgrade(config, "head")

    inspector = inspect(engine)
    table_names = set(inspector.get_table_names())
    assert {"accounts", "registration_challenges", "auth_sessions", "registration_rate_limit_buckets", "alembic_version"} <= table_names
    session_columns = {column["name"] for column in inspector.get_columns("auth_sessions")}
    assert {"access_token_hash", "access_expires_at", "refresh_token_hash", "expires_at"} <= session_columns
    engine.dispose()
