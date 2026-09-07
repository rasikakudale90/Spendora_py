"""add goals table

Revision ID: b2c3d4e5f6a7
Revises: a1b2c3d4e5f6
Create Date: 2026-09-07 13:20:00.000000

"""
from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects.postgresql import UUID


# revision identifiers, used by Alembic.
revision: str = 'b2c3d4e5f6a7'
down_revision: Union[str, None] = 'a1b2c3d4e5f6'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table(
        'goals',
        sa.Column('id', UUID(as_uuid=True), primary_key=True, server_default=sa.text('gen_random_uuid()')),
        sa.Column('user_id', UUID(as_uuid=True), sa.ForeignKey('users.id', ondelete='CASCADE'), nullable=False, index=True),
        sa.Column('name', sa.String(length=100), nullable=False),
        sa.Column('target_amount', sa.Numeric(precision=12, scale=2), nullable=False),
        sa.Column('current_amount', sa.Numeric(precision=12, scale=2), server_default=sa.text('0.00'), nullable=False),
        sa.Column('target_date', sa.Date(), nullable=True),
        sa.Column('category', sa.String(length=50), server_default='Savings', nullable=False),
        sa.Column('color', sa.String(length=30), server_default='emerald', nullable=False),
        sa.Column('status', sa.String(length=20), server_default='active', nullable=False),
        sa.Column('notes', sa.Text(), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.Column('updated_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.CheckConstraint('target_amount > 0', name='ck_goals_target_amount_positive'),
        sa.CheckConstraint('current_amount >= 0', name='ck_goals_current_amount_non_negative'),
    )
    op.create_index('ix_goals_user_id_status', 'goals', ['user_id', 'status'])
    op.create_index('ix_goals_target_date', 'goals', ['target_date'])


def downgrade() -> None:
    op.drop_index('ix_goals_target_date', table_name='goals')
    op.drop_index('ix_goals_user_id_status', table_name='goals')
    op.drop_table('goals')
