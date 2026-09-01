"""add users and tenancy

Revision ID: a1b2c3d4e5f6
Revises: f7e8d9c0b1a2
Create Date: 2026-08-31 19:40:00.000000

"""
from typing import Sequence, Union
import uuid

from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects.postgresql import UUID


# revision identifiers, used by Alembic.
revision: str = 'a1b2c3d4e5f6'
down_revision: Union[str, None] = 'f7e8d9c0b1a2'
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    # 1. Create users table
    op.create_table(
        'users',
        sa.Column('id', UUID(as_uuid=True), primary_key=True, server_default=sa.text('gen_random_uuid()')),
        sa.Column('email', sa.String(length=255), nullable=False),
        sa.Column('hashed_password', sa.String(length=255), nullable=True),
        sa.Column('full_name', sa.String(length=100), nullable=True),
        sa.Column('avatar_url', sa.String(length=500), nullable=True),
        sa.Column('is_active', sa.Boolean(), server_default=sa.text('true'), nullable=False),
        sa.Column('is_verified', sa.Boolean(), server_default=sa.text('false'), nullable=False),
        sa.Column('auth_provider', sa.String(length=20), server_default='email', nullable=False),
        sa.Column('google_id', sa.String(length=255), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.Column('updated_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
    )
    op.create_index('ix_users_email', 'users', ['email'], unique=True)
    op.create_index('ix_users_google_id', 'users', ['google_id'], unique=True)

    # 2. Create refresh_tokens table
    op.create_table(
        'refresh_tokens',
        sa.Column('id', UUID(as_uuid=True), primary_key=True, server_default=sa.text('gen_random_uuid()')),
        sa.Column('user_id', UUID(as_uuid=True), sa.ForeignKey('users.id', ondelete='CASCADE'), nullable=False),
        sa.Column('token_hash', sa.String(length=64), nullable=False),
        sa.Column('expires_at', sa.DateTime(timezone=True), nullable=False),
        sa.Column('revoked', sa.Boolean(), server_default=sa.text('false'), nullable=False),
        sa.Column('user_agent', sa.String(length=255), nullable=True),
        sa.Column('ip_address', sa.String(length=45), nullable=True),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
    )
    op.create_index('ix_refresh_tokens_user_id', 'refresh_tokens', ['user_id'])
    op.create_index('ix_refresh_tokens_token_hash', 'refresh_tokens', ['token_hash'], unique=True)

    # 3. Create password_reset_tokens table
    op.create_table(
        'password_reset_tokens',
        sa.Column('id', UUID(as_uuid=True), primary_key=True, server_default=sa.text('gen_random_uuid()')),
        sa.Column('user_id', UUID(as_uuid=True), sa.ForeignKey('users.id', ondelete='CASCADE'), nullable=False),
        sa.Column('token_hash', sa.String(length=64), nullable=False),
        sa.Column('expires_at', sa.DateTime(timezone=True), nullable=False),
        sa.Column('used', sa.Boolean(), server_default=sa.text('false'), nullable=False),
        sa.Column('created_at', sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
    )
    op.create_index('ix_password_reset_tokens_user_id', 'password_reset_tokens', ['user_id'])
    op.create_index('ix_password_reset_tokens_token_hash', 'password_reset_tokens', ['token_hash'], unique=True)

    # 4. Insert baseline legacy user if needed for backfilling existing rows
    conn = op.get_bind()
    default_user_id = str(uuid.uuid4())
    conn.execute(
        sa.text(
            "INSERT INTO users (id, email, full_name, is_active, is_verified, auth_provider) "
            "VALUES (:id, 'demo@spendora.local', 'Spendora Demo User', true, true, 'email') "
            "ON CONFLICT (email) DO NOTHING"
        ),
        {"id": default_user_id}
    )
    # Fetch effective default user ID
    res = conn.execute(sa.text("SELECT id FROM users WHERE email = 'demo@spendora.local'")).fetchone()
    if res:
        effective_user_id = str(res[0])
    else:
        effective_user_id = default_user_id

    # 5. Add user_id to categories
    op.add_column('categories', sa.Column('user_id', UUID(as_uuid=True), sa.ForeignKey('users.id', ondelete='CASCADE'), nullable=True))
    op.create_index('ix_categories_user_id', 'categories', ['user_id'])
    # Replace global unique constraint on name with composite unique index
    try:
        op.drop_constraint('categories_name_key', 'categories', type_='unique')
    except Exception:
        pass
    op.execute(
        "CREATE UNIQUE INDEX uix_categories_name_user ON categories (name, coalesce(user_id, '00000000-0000-0000-0000-000000000000'::uuid))"
    )

    # 6. Add user_id to expenses
    op.add_column('expenses', sa.Column('user_id', UUID(as_uuid=True), nullable=True))
    op.execute(f"UPDATE expenses SET user_id = '{effective_user_id}' WHERE user_id IS NULL")
    op.alter_column('expenses', 'user_id', nullable=False)
    op.create_foreign_key('fk_expenses_user_id', 'expenses', 'users', ['user_id'], ['id'], ondelete='CASCADE')
    op.create_index('ix_expenses_user_id', 'expenses', ['user_id'])
    op.create_index('ix_expenses_user_id_date', 'expenses', ['user_id', 'expense_date'])
    op.create_index('ix_expenses_user_category', 'expenses', ['user_id', 'category_id'])

    # 7. Add user_id to budgets and rebuild unique indexes
    op.add_column('budgets', sa.Column('user_id', UUID(as_uuid=True), nullable=True))
    op.execute(f"UPDATE budgets SET user_id = '{effective_user_id}' WHERE user_id IS NULL")
    op.alter_column('budgets', 'user_id', nullable=False)
    op.create_foreign_key('fk_budgets_user_id', 'budgets', 'users', ['user_id'], ['id'], ondelete='CASCADE')
    op.create_index('ix_budgets_user_id', 'budgets', ['user_id'])

    try:
        op.drop_index('uix_budgets_overall_period_type_start', table_name='budgets')
        op.drop_index('uix_budgets_category_period_type_start', table_name='budgets')
    except Exception:
        pass

    op.create_index(
        'uix_budgets_user_overall_period_type_start',
        'budgets',
        ['user_id', 'period_type', 'period_start'],
        unique=True,
        postgresql_where=sa.text("scope = 'overall'"),
    )
    op.create_index(
        'uix_budgets_user_category_period_type_start',
        'budgets',
        ['user_id', 'category_id', 'period_type', 'period_start'],
        unique=True,
        postgresql_where=sa.text("scope = 'category'"),
    )

    # 8. Add user_id to incomes
    op.add_column('incomes', sa.Column('user_id', UUID(as_uuid=True), nullable=True))
    op.execute(f"UPDATE incomes SET user_id = '{effective_user_id}' WHERE user_id IS NULL")
    op.alter_column('incomes', 'user_id', nullable=False)
    op.create_foreign_key('fk_incomes_user_id', 'incomes', 'users', ['user_id'], ['id'], ondelete='CASCADE')
    op.create_index('ix_incomes_user_id', 'incomes', ['user_id'])
    op.create_index('ix_incomes_user_id_date', 'incomes', ['user_id', 'income_date'])


def downgrade() -> None:
    # Incomes
    op.drop_index('ix_incomes_user_id_date', table_name='incomes')
    op.drop_index('ix_incomes_user_id', table_name='incomes')
    op.drop_constraint('fk_incomes_user_id', 'incomes', type_='foreignkey')
    op.drop_column('incomes', 'user_id')

    # Budgets
    op.drop_index('uix_budgets_user_category_period_type_start', table_name='budgets')
    op.drop_index('uix_budgets_user_overall_period_type_start', table_name='budgets')
    op.create_index(
        'uix_budgets_overall_period_type_start',
        'budgets',
        ['period_type', 'period_start'],
        unique=True,
        postgresql_where=sa.text("scope = 'overall'"),
    )
    op.create_index(
        'uix_budgets_category_period_type_start',
        'budgets',
        ['category_id', 'period_type', 'period_start'],
        unique=True,
        postgresql_where=sa.text("scope = 'category'"),
    )
    op.drop_index('ix_budgets_user_id', table_name='budgets')
    op.drop_constraint('fk_budgets_user_id', 'budgets', type_='foreignkey')
    op.drop_column('budgets', 'user_id')

    # Expenses
    op.drop_index('ix_expenses_user_category', table_name='expenses')
    op.drop_index('ix_expenses_user_id_date', table_name='expenses')
    op.drop_index('ix_expenses_user_id', table_name='expenses')
    op.drop_constraint('fk_expenses_user_id', 'expenses', type_='foreignkey')
    op.drop_column('expenses', 'user_id')

    # Categories
    op.execute('DROP INDEX IF EXISTS uix_categories_name_user')
    op.create_unique_constraint('categories_name_key', 'categories', ['name'])
    op.drop_index('ix_categories_user_id', table_name='categories')
    op.drop_column('categories', 'user_id')

    # Drop tokens and users
    op.drop_table('password_reset_tokens')
    op.drop_table('refresh_tokens')
    op.drop_table('users')
