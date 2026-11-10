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
    # Legacy rows remain NULL and unusable until explicitly reissued. New
    # session-service writes always populate both fields.
    with op.batch_alter_table("auth_sessions") as batch:
        batch.add_column(sa.Column("access_token_hash", sa.String(length=64), nullable=True))
        batch.add_column(sa.Column("access_expires_at", sa.DateTime(timezone=True), nullable=True))
        batch.create_unique_constraint("uq_auth_sessions_access_token_hash", ["access_token_hash"])
        batch.create_check_constraint(
            "ck_auth_session_access_hash_sha256_hex",
            "access_token_hash IS NULL OR length(access_token_hash) = 64",
        )


def downgrade() -> None:
    with op.batch_alter_table("auth_sessions") as batch:
        batch.drop_constraint("ck_auth_session_access_hash_sha256_hex", type_="check")
        batch.drop_constraint("uq_auth_sessions_access_token_hash", type_="unique")
        batch.drop_column("access_expires_at")
        batch.drop_column("access_token_hash")
