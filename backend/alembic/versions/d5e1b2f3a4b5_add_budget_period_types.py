"""add budget period types (weekly, monthly, yearly)

Revision ID: d5e1b2f3a4b5
Revises: c4c7c9740267
Create Date: 2026-08-27 16:35:00.000000

"""
import calendar
from datetime import date
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa


# revision identifiers, used by Alembic.
revision: str = 'd5e1b2f3a4b5'
down_revision: Union[str, None] = 'c4c7c9740267'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # 1. Add period_type, period_start, period_end columns
    op.add_column(
        'budgets',
        sa.Column('period_type', sa.String(length=10), server_default='monthly', nullable=False)
    )
    op.add_column(
        'budgets',
        sa.Column('period_start', sa.Date(), nullable=True)
    )
    op.add_column(
        'budgets',
        sa.Column('period_end', sa.Date(), nullable=True)
    )
    
    # Add check constraint on period_type
    op.create_check_constraint(
        'ck_budgets_period_type_valid',
        'budgets',
        "period_type IN ('weekly', 'monthly', 'yearly')"
    )

    # 2. Backfill existing records (period_start = period_month, period_end = end of that month)
    op.execute(
        """
        UPDATE budgets 
        SET period_start = period_month,
            period_end = (period_month + INTERVAL '1 month - 1 day')::date
        WHERE period_start IS NULL
        """
    )

    # Make period_start and period_end NOT NULL after backfill
    op.alter_column('budgets', 'period_start', nullable=False)
    op.alter_column('budgets', 'period_end', nullable=False)

    # 3. Drop old period_month unique indices and create new multi-period indices
    op.drop_index('uix_budgets_overall_period', table_name='budgets')
    op.drop_index('uix_budgets_category_period', table_name='budgets')

    op.create_index(
        'uix_budgets_overall_period_type_start',
        'budgets',
        ['period_type', 'period_start'],
        unique=True,
        postgresql_where=sa.text("scope = 'overall'")
    )
    op.create_index(
        'uix_budgets_category_period_type_start',
        'budgets',
        ['category_id', 'period_type', 'period_start'],
        unique=True,
        postgresql_where=sa.text("scope = 'category'")
    )


def downgrade() -> None:
    op.drop_index('uix_budgets_category_period_type_start', table_name='budgets')
    op.drop_index('uix_budgets_overall_period_type_start', table_name='budgets')

    op.create_index(
        'uix_budgets_category_period',
        'budgets',
        ['category_id', 'period_month'],
        unique=True,
        postgresql_where=sa.text("scope = 'category'")
    )
    op.create_index(
        'uix_budgets_overall_period',
        'budgets',
        ['period_month'],
        unique=True,
        postgresql_where=sa.text("scope = 'overall'")
    )

    op.drop_constraint('ck_budgets_period_type_valid', 'budgets', type_='check')
    op.drop_column('budgets', 'period_end')
    op.drop_column('budgets', 'period_start')
    op.drop_column('budgets', 'period_type')
