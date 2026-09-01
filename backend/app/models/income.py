"""
Income ORM model.

Table: incomes
- UUID primary key
- title: max 100 chars, required
- amount: NUMERIC(12,2), CHECK > 0
- income_date: DATE, required
- source: VARCHAR(50), default 'Salary' (e.g. Salary, Freelance, Investment, Business, Rental, Gift, Other)
- payment_mode: VARCHAR(30), nullable
- notes: TEXT, nullable
"""
import uuid
from datetime import date
from decimal import Decimal
from enum import Enum

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


class IncomeSource(str, Enum):
    SALARY = "Salary"
    FREELANCE = "Freelance"
    INVESTMENT = "Investment"
    BUSINESS = "Business"
    RENTAL = "Rental"
    GIFT = "Gift"
    OTHER = "Other"


class IncomePaymentMode(str, Enum):
    BANK_TRANSFER = "Bank Transfer"
    CASH = "Cash"
    UPI = "UPI"
    CHEQUE = "Cheque"
    OTHER = "Other"


class Income(Base, TimestampMixin):
    __tablename__ = "incomes"

    __table_args__ = (
        CheckConstraint("amount > 0", name="ck_incomes_amount_positive"),
        Index("ix_incomes_income_date", "income_date"),
        Index("ix_incomes_source", "source"),
        Index("ix_incomes_user_id_date", "user_id", "income_date"),
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
    title: Mapped[str] = mapped_column(String(100), nullable=False)
    amount: Mapped[Decimal] = mapped_column(Numeric(12, 2), nullable=False)
    income_date: Mapped[date] = mapped_column(Date, nullable=False)
    source: Mapped[str] = mapped_column(String(50), default="Salary", server_default="Salary", nullable=False)
    payment_mode: Mapped[str | None] = mapped_column(String(30), nullable=True)
    notes: Mapped[str | None] = mapped_column(Text, nullable=True)

    # ── Relationships ─────────────────────────────────────────────────────────
    user: Mapped["User"] = relationship(  # noqa: F821
        "User",
        back_populates="incomes",
        lazy="select",
    )

    def __repr__(self) -> str:
        return f"<Income id={self.id} title={self.title!r} amount={self.amount} source={self.source!r}>"
