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

    table_names = set(inspect(engine).get_table_names())
    assert {"accounts", "registration_challenges", "auth_sessions", "alembic_version"} <= table_names
    engine.dispose()
