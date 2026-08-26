"""
SQLAlchemy declarative base and shared column mixins.

All ORM models inherit from Base (for table registration) and
TimestampMixin (for created_at / updated_at columns).
"""
from datetime import datetime

from sqlalchemy import DateTime, func
from sqlalchemy.orm import DeclarativeBase, Mapped, mapped_column


class Base(DeclarativeBase):
    """
    Project-wide SQLAlchemy declarative base.
    All ORM models must inherit from this class so that
    Base.metadata contains every table (needed by Alembic autogenerate).
    """
    pass


class TimestampMixin:
    """
    Adds created_at and updated_at TIMESTAMPTZ columns to any model.

    - created_at: set by the DB server on INSERT (server_default).
    - updated_at: set by the DB server on INSERT, updated at the ORM
      level on every UPDATE via onupdate=func.now().
    """
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        server_default=func.now(),
        nullable=False,
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        server_default=func.now(),
        onupdate=func.now(),
        nullable=False,
    )
