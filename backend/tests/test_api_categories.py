import uuid
import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_list_categories(client: AsyncClient):
    response = await client.get("/api/v1/categories")
    assert response.status_code == 200
    categories = response.json()
    assert isinstance(categories, list)
    assert len(categories) >= 9
    for cat in categories:
        assert "id" in cat
        assert "name" in cat
        assert "expense_count" in cat
        assert isinstance(cat["expense_count"], int)


@pytest.mark.asyncio
async def test_create_category_and_duplicate(client: AsyncClient):
    unique_name = f"TestCategory_{uuid.uuid4().hex[:6]}"
    
    # Create category
    response = await client.post("/api/v1/categories", json={"name": unique_name})
    assert response.status_code == 201
    created = response.json()
    assert created["name"] == unique_name
    cat_id = created["id"]

    # Try duplicate category name -> 409 Conflict
    dup_resp = await client.post("/api/v1/categories", json={"name": unique_name})
    assert dup_resp.status_code == 409

    # Clean up
    del_resp = await client.delete(f"/api/v1/categories/{cat_id}")
    assert del_resp.status_code == 200


@pytest.mark.asyncio
async def test_rename_category(client: AsyncClient):
    init_name = f"InitName_{uuid.uuid4().hex[:6]}"
    new_name = f"NewName_{uuid.uuid4().hex[:6]}"

    # Create category
    res = await client.post("/api/v1/categories", json={"name": init_name})
    assert res.status_code == 201
    cat_id = res.json()["id"]

    # Rename
    patch_res = await client.patch(f"/api/v1/categories/{cat_id}", json={"name": new_name})
    assert patch_res.status_code == 200
    assert patch_res.json()["name"] == new_name

    # Rename non-existent -> 404
    non_existent_id = uuid.uuid4()
    bad_patch = await client.patch(f"/api/v1/categories/{non_existent_id}", json={"name": "Ghost"})
    assert bad_patch.status_code == 404

    # Clean up
    await client.delete(f"/api/v1/categories/{cat_id}")


@pytest.mark.asyncio
async def test_category_safe_delete_flow(client: AsyncClient):
    # Create category A and category B
    cat_a_res = await client.post("/api/v1/categories", json={"name": f"CatA_{uuid.uuid4().hex[:6]}"})
    cat_b_res = await client.post("/api/v1/categories", json={"name": f"CatB_{uuid.uuid4().hex[:6]}"})
    cat_a_id = cat_a_res.json()["id"]
    cat_b_id = cat_b_res.json()["id"]

    # Add expense to Cat A
    exp_res = await client.post(
        "/api/v1/expenses",
        json={
            "title": "Expense in Cat A",
            "category_id": cat_a_id,
            "amount": "150.00",
            "expense_date": "2026-08-20",
        },
    )
    assert exp_res.status_code == 201
    exp_id = exp_res.json()["id"]

    # Attempt to delete Cat A without reassign -> 409 Conflict
    del_conflict = await client.delete(f"/api/v1/categories/{cat_a_id}")
    assert del_conflict.status_code == 409
    assert "Cannot delete category" in del_conflict.json()["detail"]

    # Attempt self-reassignment -> 400 Bad Request
    self_reassign = await client.delete(
        f"/api/v1/categories/{cat_a_id}",
        params={"reassign_to": cat_a_id},
    )
    assert self_reassign.status_code == 400

    # Attempt reassign to non-existent category -> 404 Not Found
    ghost_reassign = await client.delete(
        f"/api/v1/categories/{cat_a_id}",
        params={"reassign_to": str(uuid.uuid4())},
    )
    assert ghost_reassign.status_code == 404

    # Delete Cat A with reassign to Cat B -> 200 OK
    valid_del = await client.delete(
        f"/api/v1/categories/{cat_a_id}",
        params={"reassign_to": cat_b_id},
    )
    assert valid_del.status_code == 200

    # Verify expense now belongs to Cat B
    check_exp = await client.get(f"/api/v1/expenses/{exp_id}")
    assert check_exp.status_code == 200
    assert check_exp.json()["category_id"] == cat_b_id

    # Clean up expense and Cat B
    await client.delete(f"/api/v1/expenses/{exp_id}")
    await client.delete(f"/api/v1/categories/{cat_b_id}")
