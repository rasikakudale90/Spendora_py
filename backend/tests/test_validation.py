import pytest
from datetime import date, timedelta
from decimal import Decimal
from pydantic import ValidationError

from app.schemas.expense import ExpenseCreate
from app.schemas.budget import BudgetCreate
from app.schemas.category import CategoryCreate


def test_expense_create_valid():
    expense = ExpenseCreate(
        title="Grocery shopping",
        category_id="a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
        amount=Decimal("450.50"),
        expense_date=date.today(),
        notes="Supermarket visit",
        payment_mode="UPI",
    )
    assert expense.title == "Grocery shopping"
    assert expense.amount == Decimal("450.50")


def test_expense_future_date_rejected():
    with pytest.raises(ValidationError) as exc:
        ExpenseCreate(
            title="Future ticket",
            category_id="a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
            amount=Decimal("100.00"),
            expense_date=date.today() + timedelta(days=1),
        )
    assert "Expense date cannot be in the future" in str(exc.value)


def test_expense_zero_or_negative_amount_rejected():
    with pytest.raises(ValidationError):
        ExpenseCreate(
            title="Zero amount",
            category_id="a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
            amount=Decimal("0.00"),
            expense_date=date.today(),
        )

    with pytest.raises(ValidationError):
        ExpenseCreate(
            title="Negative amount",
            category_id="a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
            amount=Decimal("-50.00"),
            expense_date=date.today(),
        )


def test_budget_create_overall_valid():
    budget = BudgetCreate(
        scope="overall",
        amount=Decimal("25000.00"),
        period_month=date(2026, 8, 15),
    )
    # Normalized to 1st of the month
    assert budget.period_month == date(2026, 8, 1)
    assert budget.category_id is None


def test_budget_create_category_requires_category_id():
    with pytest.raises(ValidationError) as exc:
        BudgetCreate(
            scope="category",
            amount=Decimal("5000.00"),
            period_month=date(2026, 8, 1),
            category_id=None,
        )
    assert "category_id is required" in str(exc.value)


def test_budget_create_overall_forbids_category_id():
    with pytest.raises(ValidationError) as exc:
        BudgetCreate(
            scope="overall",
            category_id="a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11",
            amount=Decimal("5000.00"),
            period_month=date(2026, 8, 1),
        )
    assert "category_id must be null" in str(exc.value)


def test_category_create_empty_name_rejected():
    with pytest.raises(ValidationError):
        CategoryCreate(name="")
