"""Create account, registration challenge and auth session tables.

Revision ID: 0001_auth_identity_sessions
Revises:
Create Date: 2026-11-09
"""
from alembic import op
import sqlalchemy as sa

revision = "0001_auth_identity_sessions"
down_revision = None
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.create_table(
        "accounts",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("username_normalized", sa.String(length=30), nullable=False),
        sa.Column("mobile_e164", sa.String(length=16), nullable=False),
        sa.Column("password_hash", sa.String(length=512), nullable=False),
        sa.Column("status", sa.String(length=16), server_default="pending", nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.CheckConstraint("status IN ('pending', 'active', 'disabled')", name="ck_accounts_status"),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint("mobile_e164", name="uq_accounts_mobile_e164"),
        sa.UniqueConstraint("username_normalized", name="uq_accounts_username_normalized"),
    )
    op.create_table(
        "registration_challenges",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("username_normalized", sa.String(length=30), nullable=False),
        sa.Column("mobile_e164", sa.String(length=16), nullable=False),
        sa.Column("password_hash", sa.String(length=512), nullable=False),
        sa.Column("otp_digest", sa.String(length=64), nullable=False),
        sa.Column("expires_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("attempts_remaining", sa.Integer(), server_default="5", nullable=False),
        sa.Column("consumed_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.CheckConstraint("attempts_remaining >= 0", name="ck_registration_attempts_nonnegative"),
        sa.CheckConstraint("length(otp_digest) = 64", name="ck_registration_otp_digest_sha256_hex"),
        sa.PrimaryKeyConstraint("id"),
    )
    op.create_index("ix_registration_challenges_mobile_expiry", "registration_challenges", ["mobile_e164", "expires_at"])
    op.create_index("ix_registration_challenges_expiry", "registration_challenges", ["expires_at"])
    op.create_table(
        "auth_sessions",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("account_id", sa.Uuid(), nullable=False),
        sa.Column("token_family_id", sa.Uuid(), nullable=False),
        sa.Column("refresh_token_hash", sa.String(length=64), nullable=False),
        sa.Column("expires_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("rotated_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("revoked_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.CheckConstraint("length(refresh_token_hash) = 64", name="ck_auth_session_refresh_hash_sha256_hex"),
        sa.ForeignKeyConstraint(["account_id"], ["accounts.id"], ondelete="CASCADE"),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint("refresh_token_hash", name="uq_auth_sessions_refresh_token_hash"),
    )
    op.create_index("ix_auth_sessions_account_active", "auth_sessions", ["account_id", "revoked_at", "expires_at"])
    op.create_index("ix_auth_sessions_token_family", "auth_sessions", ["token_family_id"])


def downgrade() -> None:
    op.drop_index("ix_auth_sessions_token_family", table_name="auth_sessions")
    op.drop_index("ix_auth_sessions_account_active", table_name="auth_sessions")
    op.drop_table("auth_sessions")
    op.drop_index("ix_registration_challenges_expiry", table_name="registration_challenges")
    op.drop_index("ix_registration_challenges_mobile_expiry", table_name="registration_challenges")
    op.drop_table("registration_challenges")
    op.drop_table("accounts")
