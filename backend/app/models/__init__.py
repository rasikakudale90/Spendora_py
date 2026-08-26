"""
Models package — imports all ORM models so that:
  1. Base.metadata knows about every table (required for Alembic autogenerate).
  2. SQLAlchemy relationship resolution works (forward references resolved).

Import order: Base first, then models with no FK deps, then models with FKs.
"""
from app.models.base import Base, TimestampMixin
from app.models.category import Category
from app.models.expense import Expense, PaymentMode
from app.models.budget import Budget

__all__ = [
    "Base",
    "TimestampMixin",
    "Category",
    "Expense",
    "PaymentMode",
    "Budget",
]
