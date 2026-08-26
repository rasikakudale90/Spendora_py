from datetime import date
from decimal import Decimal
from app.services.dashboard_service import DashboardService


def test_dashboard_month_bounds():
    service = DashboardService(session=None)  # type: ignore

    # Non-leap year February
    start, end = service._get_month_bounds(date(2023, 2, 10))
    assert start == date(2023, 2, 1)
    assert end == date(2023, 2, 28)

    # Leap year February
    start, end = service._get_month_bounds(date(2024, 2, 15))
    assert start == date(2024, 2, 1)
    assert end == date(2024, 2, 29)

    # 31-day month
    start, end = service._get_month_bounds(date(2026, 8, 26))
    assert start == date(2026, 8, 1)
    assert end == date(2026, 8, 31)

    # 30-day month
    start, end = service._get_month_bounds(date(2026, 9, 5))
    assert start == date(2026, 9, 1)
    assert end == date(2026, 9, 30)


def test_dashboard_previous_month():
    service = DashboardService(session=None)  # type: ignore

    # Mid-year
    prev = service._get_previous_month(date(2026, 8, 1))
    assert prev == date(2026, 7, 1)

    # Year rollover (January -> previous year December)
    prev = service._get_previous_month(date(2026, 1, 15))
    assert prev == date(2025, 12, 1)
