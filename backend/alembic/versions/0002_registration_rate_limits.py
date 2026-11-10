"""Add privacy-preserving registration rate-limit buckets.

Revision ID: 0002_registration_rate_limits
Revises: 0001_auth_identity_sessions
Create Date: 2026-11-10
"""
from alembic import op
import sqlalchemy as sa

revision = "0002_registration_rate_limits"
down_revision = "0001_auth_identity_sessions"
branch_labels = None
depends_on = None


def upgrade() -> None:
    op.create_table(
        "registration_rate_limit_buckets",
        sa.Column("id", sa.Uuid(), nullable=False),
        sa.Column("scope", sa.String(length=64), nullable=False),
        sa.Column("subject_hash", sa.String(length=64), nullable=False),
        sa.Column("window_started_at", sa.DateTime(timezone=True), nullable=False),
        sa.Column("request_count", sa.Integer(), server_default="0", nullable=False),
        sa.Column("blocked_until", sa.DateTime(timezone=True), nullable=True),
        sa.Column("updated_at", sa.DateTime(timezone=True), nullable=False),
        sa.CheckConstraint("request_count >= 0", name="ck_registration_rate_limit_count_nonnegative"),
        sa.PrimaryKeyConstraint("id"),
        sa.UniqueConstraint("scope", "subject_hash", name="uq_registration_rate_limit_subject"),
    )
    op.create_index(
        "ix_registration_rate_limit_blocked_until",
        "registration_rate_limit_buckets",
        ["blocked_until"],
    )


def downgrade() -> None:
    op.drop_index(
        "ix_registration_rate_limit_blocked_until",
        table_name="registration_rate_limit_buckets",
    )
    op.drop_table("registration_rate_limit_buckets")
