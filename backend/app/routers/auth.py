"""
Auth router: Endpoints for user registration, authentication, Google OAuth,
token rotation, session revocation, and password recovery.
"""
from typing import Annotated, Optional

from fastapi import APIRouter, BackgroundTasks, Cookie, Depends, HTTPException, Request, Response, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import settings
from app.core.database import get_db
from app.core.dependencies import get_current_user
from app.core.limiter import limiter
from app.models.user import User
from app.schemas.user import (
    AuthSuccessResponse,
    GoogleAuthRequest,
    PasswordChangeRequest,
    PasswordResetConfirm,
    PasswordResetRequest,
    UserLogin,
    UserRegister,
    UserRegisterResponse,
    UserResponse,
)
from app.services.auth_service import AuthService
from app.services.email_service import send_password_reset_email, send_welcome_registration_email

router = APIRouter(prefix="/auth", tags=["Authentication"])

REFRESH_COOKIE_NAME = "spendora_refresh_token"
REFRESH_COOKIE_PATH = "/api/v1/auth"


def set_refresh_cookie(response: Response, raw_token: str) -> None:
    """Sets the long-lived refresh token in an HttpOnly, Secure, SameSite cookie."""
    response.set_cookie(
        key=REFRESH_COOKIE_NAME,
        value=raw_token,
        httponly=True,
        secure=settings.COOKIE_SECURE,
        samesite=settings.COOKIE_SAMESITE,
        max_age=settings.REFRESH_TOKEN_EXPIRE_DAYS * 86400,
        path=REFRESH_COOKIE_PATH,
    )


def clear_refresh_cookie(response: Response) -> None:
    """Clears the refresh token cookie."""
    response.delete_cookie(
        key=REFRESH_COOKIE_NAME,
        path=REFRESH_COOKIE_PATH,
        httponly=True,
        secure=settings.COOKIE_SECURE,
        samesite=settings.COOKIE_SAMESITE,
    )


def get_client_metadata(request: Request) -> tuple[Optional[str], Optional[str]]:
    user_agent = request.headers.get("user-agent")
    ip_address = request.client.host if request.client else None
    return user_agent, ip_address


@router.post(
    "/register",
    response_model=UserRegisterResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Register new user account",
)
@limiter.limit("10/minute")
async def register(
    data: UserRegister,
    request: Request,
    background_tasks: BackgroundTasks,
    session: Annotated[AsyncSession, Depends(get_db)],
):
    service = AuthService(session)
    user = await service.register(data)
    await session.commit()

    # Dispatch welcome registration email asynchronously
    background_tasks.add_task(send_welcome_registration_email, data.email, data.full_name)

    return UserRegisterResponse(
        message="Account created successfully. Please sign in with your credentials.",
        user=UserResponse.model_validate(user),
    )


@router.post(
    "/login",
    response_model=AuthSuccessResponse,
    status_code=status.HTTP_200_OK,
    summary="Sign in with email and password",
)
@limiter.limit("15/minute")
async def login(
    data: UserLogin,
    request: Request,
    response: Response,
    session: Annotated[AsyncSession, Depends(get_db)],
):
    user_agent, ip_address = get_client_metadata(request)
    service = AuthService(session)
    user, access_token, raw_refresh_token = await service.authenticate_password(
        email=data.email,
        password=data.password,
        user_agent=user_agent,
        ip_address=ip_address,
    )
    await session.commit()
    set_refresh_cookie(response, raw_refresh_token)
    return AuthSuccessResponse(
        user=UserResponse.model_validate(user),
        access_token=access_token,
        token_type="bearer",
        expires_in=settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60,
    )


@router.post(
    "/google",
    response_model=AuthSuccessResponse,
    status_code=status.HTTP_200_OK,
    summary="Sign in or register with Google OAuth 2.0 / OpenID Connect",
)
async def google_sign_in(
    data: GoogleAuthRequest,
    request: Request,
    response: Response,
    session: Annotated[AsyncSession, Depends(get_db)],
):
    user_agent, ip_address = get_client_metadata(request)
    service = AuthService(session)
    user, access_token, raw_refresh_token = await service.authenticate_google(
        credential=data.credential,
        user_agent=user_agent,
        ip_address=ip_address,
    )
    await session.commit()
    set_refresh_cookie(response, raw_refresh_token)
    return AuthSuccessResponse(
        user=UserResponse.model_validate(user),
        access_token=access_token,
        token_type="bearer",
        expires_in=settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60,
    )


@router.post(
    "/refresh",
    response_model=AuthSuccessResponse,
    status_code=status.HTTP_200_OK,
    summary="Rotate refresh token and issue new access token",
)
async def refresh_tokens(
    request: Request,
    response: Response,
    session: Annotated[AsyncSession, Depends(get_db)],
    spendora_refresh_token: Annotated[Optional[str], Cookie()] = None,
):
    if not spendora_refresh_token:
        # Fallback check from Authorization or headers if needed
        spendora_refresh_token = request.cookies.get(REFRESH_COOKIE_NAME)

    if not spendora_refresh_token:
        clear_refresh_cookie(response)
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Refresh token missing or expired",
        )

    user_agent, ip_address = get_client_metadata(request)
    service = AuthService(session)
    user, access_token, new_raw_refresh_token = await service.refresh_tokens(
        raw_refresh_token=spendora_refresh_token,
        user_agent=user_agent,
        ip_address=ip_address,
    )
    await session.commit()
    set_refresh_cookie(response, new_raw_refresh_token)
    return AuthSuccessResponse(
        user=UserResponse.model_validate(user),
        access_token=access_token,
        token_type="bearer",
        expires_in=settings.ACCESS_TOKEN_EXPIRE_MINUTES * 60,
    )


@router.post(
    "/logout",
    status_code=status.HTTP_200_OK,
    summary="Revoke current session refresh token and clear cookie",
)
async def logout(
    response: Response,
    session: Annotated[AsyncSession, Depends(get_db)],
    spendora_refresh_token: Annotated[Optional[str], Cookie()] = None,
):
    service = AuthService(session)
    if spendora_refresh_token:
        await service.logout(spendora_refresh_token)
        await session.commit()
    clear_refresh_cookie(response)
    return {"message": "Logged out successfully"}


@router.post(
    "/logout-all",
    status_code=status.HTTP_200_OK,
    summary="Revoke all active sessions across all devices for the current user",
)
async def logout_all(
    response: Response,
    current_user: Annotated[User, Depends(get_current_user)],
    session: Annotated[AsyncSession, Depends(get_db)],
):
    service = AuthService(session)
    await service.logout_all(current_user.id)
    await session.commit()
    clear_refresh_cookie(response)
    return {"message": "Logged out of all devices successfully"}


@router.get(
    "/me",
    response_model=UserResponse,
    status_code=status.HTTP_200_OK,
    summary="Get current authenticated user profile",
)
async def get_me(
    current_user: Annotated[User, Depends(get_current_user)],
):
    return current_user


@router.post(
    "/forgot-password",
    status_code=status.HTTP_200_OK,
    summary="Request a password reset link",
)
@limiter.limit("5/minute")
async def forgot_password(
    data: PasswordResetRequest,
    request: Request,
    background_tasks: BackgroundTasks,
    session: Annotated[AsyncSession, Depends(get_db)],
):
    service = AuthService(session)
    reset_token = await service.forgot_password(data.email)
    await session.commit()

    if reset_token:
        background_tasks.add_task(send_password_reset_email, data.email, reset_token)

    response_payload = {"message": "If this email is registered, password reset instructions have been sent."}
    # For dev/test environments, also surface the token
    if reset_token and (settings.JWT_SECRET_KEY.startswith("spendora-") or not settings.COOKIE_SECURE):
        response_payload["dev_reset_token"] = reset_token
    return response_payload


@router.post(
    "/reset-password",
    status_code=status.HTTP_200_OK,
    summary="Reset password using a valid reset token",
)
async def reset_password(
    data: PasswordResetConfirm,
    session: Annotated[AsyncSession, Depends(get_db)],
):
    service = AuthService(session)
    await service.reset_password(data.token, data.new_password)
    await session.commit()
    return {"message": "Password reset successfully. You can now log in with your new password."}


@router.post(
    "/change-password",
    status_code=status.HTTP_200_OK,
    summary="Change password for the authenticated user",
)
async def change_password(
    data: PasswordChangeRequest,
    current_user: Annotated[User, Depends(get_current_user)],
    session: Annotated[AsyncSession, Depends(get_db)],
):
    service = AuthService(session)
    await service.change_password(
        user_id=current_user.id,
        current_password=data.current_password,
        new_password=data.new_password,
    )
    await session.commit()
    return {"message": "Password changed successfully"}
