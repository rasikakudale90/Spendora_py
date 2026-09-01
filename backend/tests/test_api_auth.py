"""
Comprehensive tests for Spendora Authentication endpoints:
- Registration & Password validation
- Login & JWT Token creation
- Cookie handling
- Refresh token rotation & Reuse attack detection
- Password change & reset lifecycle
- Session logout
"""
import uuid
import pytest
from httpx import AsyncClient


@pytest.mark.asyncio
async def test_register_and_login_flow(unauth_client: AsyncClient):
    unique_email = f"auth_{uuid.uuid4().hex[:8]}@example.com"
    valid_password = "SecurePass123!"

    # 1. Password complexity checks
    weak_res = await unauth_client.post(
        "/api/v1/auth/register",
        json={"email": unique_email, "password": "weak"},
    )
    assert weak_res.status_code == 422

    no_special = await unauth_client.post(
        "/api/v1/auth/register",
        json={"email": unique_email, "password": "Password123"},
    )
    assert no_special.status_code == 422

    # 2. Successful registration
    reg_res = await unauth_client.post(
        "/api/v1/auth/register",
        json={
            "email": unique_email,
            "password": valid_password,
            "full_name": "Spendora Tester",
        },
    )
    assert reg_res.status_code == 201
    data = reg_res.json()
    assert "user" in data
    assert data["user"]["email"] == unique_email.lower()
    assert "access_token" not in data
    assert "spendora_refresh_token" not in reg_res.cookies

    # 3. Duplicate registration rejected
    dup_res = await unauth_client.post(
        "/api/v1/auth/register",
        json={
            "email": unique_email,
            "password": valid_password,
            "full_name": "Duplicate Tester",
        },
    )
    assert dup_res.status_code == 400

    # 4. Login with wrong password
    bad_login = await unauth_client.post(
        "/api/v1/auth/login",
        json={"email": unique_email, "password": "WrongPassword123!"},
    )
    assert bad_login.status_code == 401

    # 5. Successful login
    login_res = await unauth_client.post(
        "/api/v1/auth/login",
        json={"email": unique_email, "password": valid_password},
    )
    assert login_res.status_code == 200
    login_data = login_res.json()
    assert "access_token" in login_data
    assert "spendora_refresh_token" in login_res.cookies


@pytest.mark.asyncio
async def test_refresh_token_rotation_and_reuse_detection(unauth_client: AsyncClient):
    unique_email = f"rotation_{uuid.uuid4().hex[:8]}@example.com"
    password = "StrongPassword123!"

    reg_res = await unauth_client.post(
        "/api/v1/auth/register",
        json={"email": unique_email, "password": password},
    )
    assert reg_res.status_code == 201

    login_res = await unauth_client.post(
        "/api/v1/auth/login",
        json={"email": unique_email, "password": password},
    )
    assert login_res.status_code == 200
    initial_refresh_cookie = login_res.cookies.get("spendora_refresh_token")
    assert initial_refresh_cookie is not None

    # 1. Rotate token
    refresh_res = await unauth_client.post(
        "/api/v1/auth/refresh",
        cookies={"spendora_refresh_token": initial_refresh_cookie},
    )
    assert refresh_res.status_code == 200
    new_refresh_cookie = refresh_res.cookies.get("spendora_refresh_token")
    assert new_refresh_cookie is not None
    assert new_refresh_cookie != initial_refresh_cookie

    # 2. Attempt to reuse initial token (Reuse attack detection)
    # The system must reject and revoke all active sessions for security!
    reuse_res = await unauth_client.post(
        "/api/v1/auth/refresh",
        cookies={"spendora_refresh_token": initial_refresh_cookie},
    )
    assert reuse_res.status_code == 401
    assert "reuse detected" in reuse_res.json()["detail"].lower()

    # 3. Now the newer token should also be revoked due to security wipe
    second_try = await unauth_client.post(
        "/api/v1/auth/refresh",
        cookies={"spendora_refresh_token": new_refresh_cookie},
    )
    assert second_try.status_code == 401


@pytest.mark.asyncio
async def test_password_recovery_and_change(unauth_client: AsyncClient):
    unique_email = f"recovery_{uuid.uuid4().hex[:8]}@example.com"
    old_password = "OldPassword123!"
    new_password = "NewPassword123!"

    reg_res = await unauth_client.post(
        "/api/v1/auth/register",
        json={"email": unique_email, "password": old_password},
    )
    assert reg_res.status_code == 201

    login_res = await unauth_client.post(
        "/api/v1/auth/login",
        json={"email": unique_email, "password": old_password},
    )
    assert login_res.status_code == 200
    access_token = login_res.json()["access_token"]

    # 1. Change password while authenticated
    change_res = await unauth_client.post(
        "/api/v1/auth/change-password",
        json={"current_password": old_password, "new_password": new_password},
        headers={"Authorization": f"Bearer {access_token}"},
    )
    assert change_res.status_code == 200

    # 2. Login with new password
    login_new = await unauth_client.post(
        "/api/v1/auth/login",
        json={"email": unique_email, "password": new_password},
    )
    assert login_new.status_code == 200

    # 3. Forgot password request
    forgot_res = await unauth_client.post(
        "/api/v1/auth/forgot-password",
        json={"email": unique_email},
    )
    assert forgot_res.status_code == 200
    reset_token = forgot_res.json().get("dev_reset_token")
    assert reset_token is not None

    # 4. Reset password using token
    final_password = "FinalPassword123!"
    reset_res = await unauth_client.post(
        "/api/v1/auth/reset-password",
        json={"token": reset_token, "new_password": final_password},
    )
    assert reset_res.status_code == 200

    # 5. Verify login with final password
    login_final = await unauth_client.post(
        "/api/v1/auth/login",
        json={"email": unique_email, "password": final_password},
    )
    assert login_final.status_code == 200


@pytest.mark.asyncio
async def test_me_and_logout(unauth_client: AsyncClient):
    unique_email = f"profile_{uuid.uuid4().hex[:8]}@example.com"
    password = "MyPassword123!"

    reg_res = await unauth_client.post(
        "/api/v1/auth/register",
        json={"email": unique_email, "password": password, "full_name": "Profile User"},
    )
    assert reg_res.status_code == 201

    login_res = await unauth_client.post(
        "/api/v1/auth/login",
        json={"email": unique_email, "password": password},
    )
    assert login_res.status_code == 200
    token = login_res.json()["access_token"]
    refresh_cookie = login_res.cookies.get("spendora_refresh_token")

    # 1. Get Me
    me_res = await unauth_client.get("/api/v1/auth/me", headers={"Authorization": f"Bearer {token}"})
    assert me_res.status_code == 200
    assert me_res.json()["email"] == unique_email.lower()
    assert me_res.json()["full_name"] == "Profile User"

    # 2. Logout
    logout_res = await unauth_client.post(
        "/api/v1/auth/logout",
        cookies={"spendora_refresh_token": refresh_cookie},
    )
    assert logout_res.status_code == 200
    # Cookie should be cleared
    assert logout_res.cookies.get("spendora_refresh_token") == "" or "spendora_refresh_token" not in logout_res.cookies
