"""
Models package — imports all ORM models so that:
  1. Base.metadata knows about every table (required for Alembic autogenerate).
  2. SQLAlchemy relationship resolution works (forward references resolved).

Import order: Base first, then models with no FK deps, then models with FKs.
"""
from app.models.base import Base, TimestampMixin
from app.models.user import User
from app.models.refresh_token import RefreshToken, PasswordResetToken
from app.models.category import Category
from app.models.expense import Expense, PaymentMode
from app.models.budget import Budget
from app.models.income import Income, IncomeSource, IncomePaymentMode

__all__ = [
    "Base",
    "TimestampMixin",
    "User",
    "RefreshToken",
    "PasswordResetToken",
    "Category",
    "Expense",
    "PaymentMode",
    "Budget",
    "Income",
    "IncomeSource",
    "IncomePaymentMode",
]
