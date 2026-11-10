"""Add access-token hashes and expiry to auth sessions.

Revision ID: 0003_auth_access_tokens
Revises: 0002_registration_rate_limits
Create Date: 2026-11-10
"""
from alembic import op
import sqlalchemy as sa

revision = "0003_auth_access_tokens"
down_revision = "0002_registration_rate_limits"
branch_labels = None
depends_on = None


def upgrade() -> None:
    # Existing session rows cannot be trusted to have an access credential.
    # Columns are nullable for migration; new session services always populate
    # both fields. Legacy rows remain unusable until explicitly reissued.
    op.add_column("auth_sessions", sa.Column("access_token_hash", sa.String(length=64), nullable=True))
    op.add_column("auth_sessions", sa.Column("access_expires_at", sa.DateTime(timezone=True), nullable=True))
    op.create_unique_constraint("uq_auth_sessions_access_token_hash", "auth_sessions", ["access_token_hash"])
    op.create_check_constraint(
        "ck_auth_session_access_hash_sha256_hex",
        "auth_sessions",
        "access_token_hash IS NULL OR length(access_token_hash) = 64",
    )


def downgrade() -> None:
    op.drop_constraint("ck_auth_session_access_hash_sha256_hex", "auth_sessions", type_="check")
    op.drop_constraint("uq_auth_sessions_access_token_hash", "auth_sessions", type_="unique")
    op.drop_column("auth_sessions", "access_expires_at")
    op.drop_column("auth_sessions", "access_token_hash")
