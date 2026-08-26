from decimal import Decimal
from app.services.budget_service import BudgetService


def test_budget_calculation_on_track():
    # Instantiate with None session since _calculate_budget_status is pure logic
    service = BudgetService(session=None)  # type: ignore

    budget = Decimal("10000.00")
    spent = Decimal("5000.00")

    remaining, pct, status_val = service._calculate_budget_status(budget, spent)

    assert remaining == Decimal("5000.00")
    assert pct == 50.0
    assert status_val == "on_track"


def test_budget_calculation_near_limit():
    service = BudgetService(session=None)  # type: ignore

    budget = Decimal("10000.00")
    spent = Decimal("8500.00")  # 85% > 80% threshold

    remaining, pct, status_val = service._calculate_budget_status(budget, spent)

    assert remaining == Decimal("1500.00")
    assert pct == 85.0
    assert status_val == "near_limit"


def test_budget_calculation_over_budget():
    service = BudgetService(session=None)  # type: ignore

    budget = Decimal("10000.00")
    spent = Decimal("11200.50")

    remaining, pct, status_val = service._calculate_budget_status(budget, spent)

    assert remaining == Decimal("-1200.50")
    assert pct == 112.01
    assert status_val == "over_budget"
