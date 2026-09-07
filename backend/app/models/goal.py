"""
Goal ORM model.

Table: goals
- UUID primary key
- user_id: UUID foreign key to users.id
- name: VARCHAR(100), required
- target_amount: NUMERIC(12,2), CHECK > 0
- current_amount: NUMERIC(12,2), default 0.00, CHECK >= 0
- target_date: DATE, nullable
- category: VARCHAR(50), default 'Savings'
- color: VARCHAR(30), default 'emerald'
- status: VARCHAR(20), default 'active' (active, completed, paused)
- notes: TEXT, nullable
"""
from __future__ import annotations

import uuid
from datetime import date
from decimal import Decimal
from typing import Optional

from sqlalchemy import (
    CheckConstraint,
    Date,
    ForeignKey,
    Index,
    Numeric,
    String,
    Text,
    text,
)
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.models.base import Base, TimestampMixin


class Goal(Base, TimestampMixin):
    __tablename__ = "goals"

    __table_args__ = (
        CheckConstraint("target_amount > 0", name="ck_goals_target_amount_positive"),
        CheckConstraint("current_amount >= 0", name="ck_goals_current_amount_non_negative"),
        Index("ix_goals_user_id_status", "user_id", "status"),
        Index("ix_goals_target_date", "target_date"),
    )

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        primary_key=True,
        server_default=text("gen_random_uuid()"),
    )
    user_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("users.id", ondelete="CASCADE"),
        nullable=False,
        index=True,
    )
    name: Mapped[str] = mapped_column(String(100), nullable=False)
    target_amount: Mapped[Decimal] = mapped_column(Numeric(12, 2), nullable=False)
    current_amount: Mapped[Decimal] = mapped_column(
        Numeric(12, 2),
        default=Decimal("0.00"),
        server_default=text("0.00"),
        nullable=False,
    )
    target_date: Mapped[Optional[date]] = mapped_column(Date, nullable=True)
    category: Mapped[str] = mapped_column(
        String(50),
        default="Savings",
        server_default="Savings",
        nullable=False,
    )
    color: Mapped[str] = mapped_column(
        String(30),
        default="emerald",
        server_default="emerald",
        nullable=False,
    )
    status: Mapped[str] = mapped_column(
        String(20),
        default="active",
        server_default="active",
        nullable=False,
    )
    notes: Mapped[Optional[str]] = mapped_column(Text, nullable=True)

    # ── Relationships ─────────────────────────────────────────────────────────
    user: Mapped["User"] = relationship(  # noqa: F821
        "User",
        back_populates="goals",
        lazy="select",
    )

    def __repr__(self) -> str:
        return f"<Goal id={self.id} name={self.name!r} target={self.target_amount} current={self.current_amount} status={self.status!r}>"
