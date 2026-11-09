from __future__ import annotations

from datetime import datetime
from uuid import UUID, uuid4

from sqlalchemy import (
    CheckConstraint,
    DateTime,
    ForeignKey,
    Index,
    String,
    UniqueConstraint,
    Uuid,
    func,
)
from sqlalchemy.orm import Mapped, mapped_column

from maale_bible_api.db.base import Base


class Account(Base):
    __tablename__ = "accounts"
    __table_args__ = (
        UniqueConstraint("username_normalized", name="uq_accounts_username_normalized"),
        UniqueConstraint("mobile_e164", name="uq_accounts_mobile_e164"),
        CheckConstraint(
            "status IN ('pending', 'active', 'disabled')",
            name="ck_accounts_status",
        ),
    )

    id: Mapped[UUID] = mapped_column(Uuid(as_uuid=True), primary_key=True, default=uuid4)
    username_normalized: Mapped[str] = mapped_column(String(30), nullable=False)
    mobile_e164: Mapped[str] = mapped_column(String(16), nullable=False)
    password_hash: Mapped[str] = mapped_column(String(512), nullable=False)
    # Fail closed: a service must explicitly activate an account after OTP verification.
    status: Mapped[str] = mapped_column(String(16), nullable=False, default="pending")
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, server_default=func.now()
    )


class RegistrationChallenge(Base):
    __tablename__ = "registration_challenges"
    __table_args__ = (
        CheckConstraint("attempts_remaining >= 0", name="ck_registration_attempts_nonnegative"),
        CheckConstraint("length(otp_digest) = 64", name="ck_registration_otp_digest_sha256_hex"),
        Index("ix_registration_challenges_mobile_expiry", "mobile_e164", "expires_at"),
        Index("ix_registration_challenges_expiry", "expires_at"),
    )

    id: Mapped[UUID] = mapped_column(Uuid(as_uuid=True), primary_key=True, default=uuid4)
    username_normalized: Mapped[str] = mapped_column(String(30), nullable=False)
    mobile_e164: Mapped[str] = mapped_column(String(16), nullable=False)
    password_hash: Mapped[str] = mapped_column(String(512), nullable=False)
    otp_digest: Mapped[str] = mapped_column(String(64), nullable=False)
    expires_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    attempts_remaining: Mapped[int] = mapped_column(nullable=False, default=5)
    consumed_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, server_default=func.now()
    )


class AuthSession(Base):
    __tablename__ = "auth_sessions"
    __table_args__ = (
        UniqueConstraint("refresh_token_hash", name="uq_auth_sessions_refresh_token_hash"),
        CheckConstraint("length(refresh_token_hash) = 64", name="ck_auth_session_refresh_hash_sha256_hex"),
        Index("ix_auth_sessions_account_active", "account_id", "revoked_at", "expires_at"),
        Index("ix_auth_sessions_token_family", "token_family_id"),
    )

    id: Mapped[UUID] = mapped_column(Uuid(as_uuid=True), primary_key=True, default=uuid4)
    account_id: Mapped[UUID] = mapped_column(
        Uuid(as_uuid=True), ForeignKey("accounts.id", ondelete="CASCADE"), nullable=False
    )
    token_family_id: Mapped[UUID] = mapped_column(Uuid(as_uuid=True), nullable=False, default=uuid4)
    refresh_token_hash: Mapped[str] = mapped_column(String(64), nullable=False)
    expires_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    rotated_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)
    revoked_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True), nullable=True)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True), nullable=False, server_default=func.now()
    )
