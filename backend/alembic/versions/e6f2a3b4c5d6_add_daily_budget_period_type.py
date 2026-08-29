"""add daily budget period type

Revision ID: e6f2a3b4c5d6
Revises: d5e1b2f3a4b5
Create Date: 2026-08-29 16:45:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = 'e6f2a3b4c5d6'
down_revision: Union[str, None] = 'd5e1b2f3a4b5'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # Drop existing check constraint and replace with one that includes 'daily'
    op.drop_constraint('ck_budgets_period_type_valid', 'budgets', type_='check')
    op.create_check_constraint(
        'ck_budgets_period_type_valid',
        'budgets',
        "period_type IN ('daily', 'weekly', 'monthly', 'yearly')"
    )


def downgrade() -> None:
    op.drop_constraint('ck_budgets_period_type_valid', 'budgets', type_='check')
    op.create_check_constraint(
        'ck_budgets_period_type_valid',
        'budgets',
        "period_type IN ('weekly', 'monthly', 'yearly')"
    )
