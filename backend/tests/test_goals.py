import pytest
from decimal import Decimal
import uuid
from httpx import ASGITransport, AsyncClient
from app.main import app, lifespan


@pytest.mark.asyncio
async def test_goal_full_lifecycle(client):
    # 1. Create a goal
    goal_payload = {
        "name": "New Laptop M4",
        "target_amount": "120000.00",
        "current_amount": "20000.00",
        "target_date": "2026-12-31",
        "category": "Gadget",
        "color": "cyan",
        "notes": "Workstation upgrade",
    }
    create_resp = await client.post("/api/v1/goals", json=goal_payload)
    assert create_resp.status_code == 201
    goal_data = create_resp.json()
    goal_id = goal_data["id"]
    assert goal_data["name"] == "New Laptop M4"
    assert Decimal(str(goal_data["target_amount"])) == Decimal("120000.00")
    assert Decimal(str(goal_data["current_amount"])) == Decimal("20000.00")
    assert Decimal(str(goal_data["remaining_amount"])) == Decimal("100000.00")
    assert goal_data["progress_percentage"] == 16.7
    assert goal_data["is_completed"] is False
    assert goal_data["runway"] is not None

    # 2. Get single goal
    get_resp = await client.get(f"/api/v1/goals/{goal_id}")
    assert get_resp.status_code == 200
    assert get_resp.json()["id"] == goal_id

    # 3. List goals
    list_resp = await client.get("/api/v1/goals")
    assert list_resp.status_code == 200
    list_data = list_resp.json()
    assert list_data["total_count"] >= 1
    assert Decimal(str(list_data["total_target_amount"])) >= Decimal("120000.00")

    # 4. Deposit funds via contribute endpoint
    contrib_resp = await client.post(
        f"/api/v1/goals/{goal_id}/contribute",
        json={"amount": "30000.00", "action": "deposit", "notes": "Bonus savings"},
    )
    assert contrib_resp.status_code == 200
    contrib_data = contrib_resp.json()
    assert Decimal(str(contrib_data["current_amount"])) == Decimal("50000.00")
    assert Decimal(str(contrib_data["remaining_amount"])) == Decimal("70000.00")
    assert contrib_data["progress_percentage"] == 41.7

    # 5. Withdraw funds via contribute endpoint
    withdraw_resp = await client.post(
        f"/api/v1/goals/{goal_id}/contribute",
        json={"amount": "10000.00", "action": "withdraw"},
    )
    assert withdraw_resp.status_code == 200
    assert Decimal(str(withdraw_resp.json()["current_amount"])) == Decimal("40000.00")

    # 6. Update goal details
    patch_resp = await client.patch(
        f"/api/v1/goals/{goal_id}",
        json={"name": "New Laptop M4 Max", "color": "purple"},
    )
    assert patch_resp.status_code == 200
    assert patch_resp.json()["name"] == "New Laptop M4 Max"
    assert patch_resp.json()["color"] == "purple"

    # 7. Complete the goal with full deposit
    complete_resp = await client.post(
        f"/api/v1/goals/{goal_id}/contribute",
        json={"amount": "80000.00", "action": "deposit"},
    )
    assert complete_resp.status_code == 200
    assert complete_resp.json()["is_completed"] is True
    assert complete_resp.json()["status"] == "completed"
    assert complete_resp.json()["progress_percentage"] == 100.0

    # 8. Delete goal
    del_resp = await client.delete(f"/api/v1/goals/{goal_id}")
    assert del_resp.status_code == 204

    # 9. Verify 404 after deletion
    get_after_del = await client.get(f"/api/v1/goals/{goal_id}")
    assert get_after_del.status_code == 404


@pytest.mark.asyncio
async def test_goal_zero_trust_tenancy(client, client_factory):
    """Ensure User A cannot access or modify User B's goal."""
    # Create goal with client (User A)
    create_resp = await client.post(
        "/api/v1/goals",
        json={"name": "User A Private Goal", "target_amount": "50000.00"},
    )
    assert create_resp.status_code == 201
    goal_id = create_resp.json()["id"]

    # Create authenticated client for User B using client_factory
    user_b_client, _ = await client_factory()

    # User B attempts to access User A's goal -> should return 404
    get_resp = await user_b_client.get(f"/api/v1/goals/{goal_id}")
    assert get_resp.status_code == 404

    # User B attempts to update User A's goal -> 404
    patch_resp = await user_b_client.patch(
        f"/api/v1/goals/{goal_id}",
        json={"name": "Hacked Goal"},
    )
    assert patch_resp.status_code == 404

    # User B attempts to contribute to User A's goal -> 404
    contrib_resp = await user_b_client.post(
        f"/api/v1/goals/{goal_id}/contribute",
        json={"amount": "1000.00"},
    )
    assert contrib_resp.status_code == 404

    # User B attempts to delete User A's goal -> 404
    del_resp = await user_b_client.delete(f"/api/v1/goals/{goal_id}")
    assert del_resp.status_code == 404
