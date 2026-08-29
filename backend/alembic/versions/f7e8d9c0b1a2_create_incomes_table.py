"""create incomes table

Revision ID: f7e8d9c0b1a2
Revises: e6f2a3b4c5d6
Create Date: 2026-08-29 17:11:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects.postgresql import UUID


# revision identifiers, used by Alembic.
revision: str = 'f7e8d9c0b1a2'
down_revision: Union[str, None] = 'e6f2a3b4c5d6'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table(
        'incomes',
        sa.Column('id', UUID(as_uuid=True), primary_key=True, server_default=sa.text('gen_random_uuid()')),
        sa.Column('title', sa.String(length=100), nullable=False),
        sa.Column('amount', sa.Numeric(precision=12, scale=2), nullable=False),
        sa.Column('income_date', sa.Date(), nullable=False),
        sa.Column('source', sa.String(length=50), nullable=False, server_default='Salary'),
        sa.Column('payment_mode', sa.String(length=30), nullable=True),
        sa.Column('notes', sa.Text(), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=False),
        sa.Column('updated_at', sa.DateTime(timezone=True), server_default=sa.text('now()'), nullable=False),
        sa.CheckConstraint('amount > 0', name='ck_incomes_amount_positive'),
    )

    op.create_index('ix_incomes_income_date', 'incomes', ['income_date'])
    op.create_index('ix_incomes_source', 'incomes', ['source'])


def downgrade() -> None:
    op.drop_index('ix_incomes_source', table_name='incomes')
    op.drop_index('ix_incomes_income_date', table_name='incomes')
    op.drop_table('incomes')
