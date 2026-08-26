import uuid
from datetime import date, timedelta
from decimal import Decimal
import pytest
from pydantic import ValidationError

from app.models.expense import PaymentMode
from app.schemas.budget import BudgetCreate, BudgetResponse, BudgetListResponse
from app.schemas.category import (
    CategoryCreate,
    CategoryResponse,
    CategoryUpdate,
    CategoryWithCountResponse,
)
from app.schemas.expense import (
    ExpenseCreate,
    ExpenseListResponse,
    ExpenseResponse,
    ExpenseUpdate,
)


# ==========================================
# Category Schemas Unit Tests
# ==========================================

def test_category_create_valid():
    cat = CategoryCreate(name="Groceries")
    assert cat.name == "Groceries"


def test_category_create_empty_name_rejected():
    with pytest.raises(ValidationError):
        CategoryCreate(name="")


def test_category_create_whitespace_only_rejected():
    # Pydantic string validation max_length=50, min_length=1
    with pytest.raises(ValidationError):
        CategoryCreate(name="   " * 20)  # > 50 chars


def test_category_create_too_long_rejected():
    with pytest.raises(ValidationError):
        CategoryCreate(name="A" * 51)


def test_category_update_valid():
    cat_update = CategoryUpdate(name="Supermarket")
    assert cat_update.name == "Supermarket"


def test_category_update_empty_rejected():
    with pytest.raises(ValidationError):
        CategoryUpdate(name="")


# ==========================================
# Expense Schemas Unit Tests
# ==========================================

def test_expense_create_valid():
    expense = ExpenseCreate(
        title="Grocery shopping",
        category_id=uuid.uuid4(),
        amount=Decimal("450.50"),
        expense_date=date.today(),
        notes="Supermarket visit",
        payment_mode=PaymentMode.UPI,
    )
    assert expense.title == "Grocery shopping"
    assert expense.amount == Decimal("450.50")
    assert expense.payment_mode == PaymentMode.UPI


def test_expense_create_future_date_rejected():
    with pytest.raises(ValidationError) as exc:
        ExpenseCreate(
            title="Future ticket",
            category_id=uuid.uuid4(),
            amount=Decimal("100.00"),
            expense_date=date.today() + timedelta(days=1),
        )
    assert "Expense date cannot be in the future" in str(exc.value)


def test_expense_create_zero_or_negative_amount_rejected():
    with pytest.raises(ValidationError):
        ExpenseCreate(
            title="Zero amount",
            category_id=uuid.uuid4(),
            amount=Decimal("0.00"),
            expense_date=date.today(),
        )

    with pytest.raises(ValidationError):
        ExpenseCreate(
            title="Negative amount",
            category_id=uuid.uuid4(),
            amount=Decimal("-50.00"),
            expense_date=date.today(),
        )


def test_expense_create_title_too_long():
    with pytest.raises(ValidationError):
        ExpenseCreate(
            title="A" * 51,
            category_id=uuid.uuid4(),
            amount=Decimal("10.00"),
            expense_date=date.today(),
        )


def test_expense_create_all_payment_modes():
    for mode in ["Cash", "Card", "UPI", "Net Banking", "Other"]:
        exp = ExpenseCreate(
            title="Item",
            category_id=uuid.uuid4(),
            amount=Decimal("20.00"),
            expense_date=date.today(),
            payment_mode=mode,  # type: ignore
        )
        assert exp.payment_mode == PaymentMode(mode)


def test_expense_create_invalid_payment_mode():
    with pytest.raises(ValidationError):
        ExpenseCreate(
            title="Item",
            category_id=uuid.uuid4(),
            amount=Decimal("20.00"),
            expense_date=date.today(),
            payment_mode="Bitcoin",  # type: ignore
        )


def test_expense_update_partial():
    update = ExpenseUpdate(title="New Title", amount=Decimal("99.99"))
    assert update.title == "New Title"
    assert update.amount == Decimal("99.99")
    assert update.category_id is None
    assert update.notes is None


def test_expense_update_invalid_future_date():
    with pytest.raises(ValidationError) as exc:
        ExpenseUpdate(expense_date=date.today() + timedelta(days=2))
    assert "Expense date cannot be in the future" in str(exc.value)


# ==========================================
# Budget Schemas Unit Tests
# ==========================================

def test_budget_create_overall_valid():
    budget = BudgetCreate(
        scope="overall",
        amount=Decimal("25000.00"),
        period_month=date(2026, 8, 15),
    )
    # Normalized to 1st of the month
    assert budget.period_month == date(2026, 8, 1)
    assert budget.category_id is None


def test_budget_create_category_valid():
    cat_id = uuid.uuid4()
    budget = BudgetCreate(
        scope="category",
        category_id=cat_id,
        amount=Decimal("5000.00"),
        period_month=date(2026, 8, 25),
    )
    assert budget.period_month == date(2026, 8, 1)
    assert budget.category_id == cat_id


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
            category_id=uuid.uuid4(),
            amount=Decimal("5000.00"),
            period_month=date(2026, 8, 1),
        )
    assert "category_id must be null" in str(exc.value)


def test_budget_create_negative_amount_rejected():
    with pytest.raises(ValidationError):
        BudgetCreate(
            scope="overall",
            amount=Decimal("-100.00"),
            period_month=date(2026, 8, 1),
        )


def test_budget_create_zero_amount_rejected():
    with pytest.raises(ValidationError):
        BudgetCreate(
            scope="overall",
            amount=Decimal("0.00"),
            period_month=date(2026, 8, 1),
        )
