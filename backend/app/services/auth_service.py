"""
AuthService: Business logic for authentication, Google OAuth verification,
token rotation, session revocation, and password recovery.
"""
import secrets
import uuid
from datetime import datetime, timedelta, timezone
from typing import Optional, Tuple

from fastapi import HTTPException, status
from google.auth.transport import requests as google_requests
from google.oauth2 import id_token as google_id_token
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.config import settings
from app.core.security import (
    create_access_token,
    generate_raw_token,
    get_password_hash,
    hash_token,
    verify_password,
)
from app.models.user import User
from app.repositories.token_repository import TokenRepository
from app.repositories.user_repository import UserRepository
from app.schemas.user import UserRegister


class AuthService:
    def __init__(self, session: AsyncSession):
        self.session = session
        self.user_repo = UserRepository(session)
        self.token_repo = TokenRepository(session)

    async def _issue_token_pair(
        self,
        user: User,
        user_agent: Optional[str] = None,
        ip_address: Optional[str] = None,
    ) -> Tuple[str, str]:
        """Helper to create access token and save hashed refresh token."""
        access_token = create_access_token(user.id, user.email)
        raw_refresh_token = generate_raw_token()
        token_hash = hash_token(raw_refresh_token)
        expires_at = datetime.now(timezone.utc) + timedelta(days=settings.REFRESH_TOKEN_EXPIRE_DAYS)

        await self.token_repo.create_refresh_token(
            user_id=user.id,
            token_hash=token_hash,
            expires_at=expires_at,
            user_agent=user_agent,
            ip_address=ip_address,
        )
        return access_token, raw_refresh_token

    async def register(
        self,
        data: UserRegister,
    ) -> User:
        existing = await self.user_repo.get_by_email(data.email)
        if existing:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="An account with this email address already exists",
            )

        hashed_password = get_password_hash(data.password)
        user = await self.user_repo.create(
            email=data.email,
            hashed_password=hashed_password,
            full_name=data.full_name,
            auth_provider="email",
        )
        return user

    async def authenticate_password(
        self,
        email: str,
        password: str,
        user_agent: Optional[str] = None,
        ip_address: Optional[str] = None,
    ) -> Tuple[User, str, str]:
        user = await self.user_repo.get_by_email(email)
        if not user or not user.hashed_password:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid email or password",
                headers={"WWW-Authenticate": "Bearer"},
            )

        if not verify_password(password, user.hashed_password):
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid email or password",
                headers={"WWW-Authenticate": "Bearer"},
            )

        if not user.is_active:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="User account is deactivated",
            )

        access_token, raw_refresh_token = await self._issue_token_pair(user, user_agent, ip_address)
        return user, access_token, raw_refresh_token

    async def authenticate_google(
        self,
        credential: str,
        user_agent: Optional[str] = None,
        ip_address: Optional[str] = None,
    ) -> Tuple[User, str, str]:
        """
        Verify Google OpenID Connect ID token on the backend.
        Never trust user profile data sent directly from the client.
        """
        try:
            id_info = google_id_token.verify_oauth2_token(
                credential,
                google_requests.Request(),
                settings.GOOGLE_CLIENT_ID if settings.GOOGLE_CLIENT_ID else None,
            )
        except Exception as e:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail=f"Google authentication token validation failed: {str(e)}",
            )

        google_id = id_info.get("sub")
        email = id_info.get("email")
        if not email or not google_id:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Google token is missing required email or subject claims",
            )

        email = email.strip().lower()
        full_name = id_info.get("name")
        avatar_url = id_info.get("picture")

        # Check if user exists by google_id or by email
        user = await self.user_repo.get_by_google_id(google_id)
        if not user:
            user = await self.user_repo.get_by_email(email)
            if user:
                # Link existing account
                user = await self.user_repo.link_google_account(
                    user=user,
                    google_id=google_id,
                    avatar_url=avatar_url,
                    full_name=full_name,
                )
            else:
                # Create brand new user
                user = await self.user_repo.create(
                    email=email,
                    hashed_password=None,
                    full_name=full_name,
                    avatar_url=avatar_url,
                    auth_provider="google",
                    google_id=google_id,
                    is_verified=id_info.get("email_verified", True),
                )

        if not user.is_active:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN,
                detail="User account is deactivated",
            )

        access_token, raw_refresh_token = await self._issue_token_pair(user, user_agent, ip_address)
        return user, access_token, raw_refresh_token

    async def refresh_tokens(
        self,
        raw_refresh_token: str,
        user_agent: Optional[str] = None,
        ip_address: Optional[str] = None,
    ) -> Tuple[User, str, str]:
        """
        Refresh token rotation with reuse detection:
        - If token is missing/invalid: 401
        - If token is found but already revoked (token reuse attempt): revoke ALL user tokens immediately
        - If token is valid: revoke this token, issue a new pair
        """
        token_hash = hash_token(raw_refresh_token)
        stored_token = await self.token_repo.get_refresh_token_by_hash(token_hash)

        if not stored_token:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Invalid or expired refresh token",
            )

        # Token reuse detection
        if stored_token.revoked:
            await self.token_repo.revoke_all_user_tokens(stored_token.user_id)
            await self.session.commit()
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Refresh token reuse detected. All active sessions have been terminated for security.",
            )

        now = datetime.now(timezone.utc)
        if stored_token.expires_at <= now:
            await self.token_repo.revoke_refresh_token(stored_token)
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="Refresh token has expired. Please sign in again.",
            )

        user = await self.user_repo.get_by_id(stored_token.user_id)
        if not user or not user.is_active:
            raise HTTPException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                detail="User no longer active",
            )

        # Rotate: revoke the current token
        await self.token_repo.revoke_refresh_token(stored_token)

        # Issue new token pair
        new_access_token, new_raw_refresh_token = await self._issue_token_pair(
            user, user_agent, ip_address
        )
        return user, new_access_token, new_raw_refresh_token

    async def logout(self, raw_refresh_token: Optional[str]) -> None:
        """Revoke the current session's refresh token."""
        if not raw_refresh_token:
            return
        token_hash = hash_token(raw_refresh_token)
        stored_token = await self.token_repo.get_refresh_token_by_hash(token_hash)
        if stored_token:
            await self.token_repo.revoke_refresh_token(stored_token)

    async def logout_all(self, user_id: uuid.UUID) -> None:
        """Revoke all refresh tokens for the user."""
        await self.token_repo.revoke_all_user_tokens(user_id)

    async def forgot_password(self, email: str) -> Optional[str]:
        """
        Generate a single-use 4-digit OTP valid for 10 minutes.
        Returns the 4-digit OTP string (dispatched via email).
        """
        user = await self.user_repo.get_by_email(email)
        if not user or not user.is_active:
            # Don't leak whether email exists
            return None

        # Generate 4-digit OTP (1000 - 9999)
        otp = f"{secrets.randbelow(9000) + 1000:04d}"
        token_hash = hash_token(f"{user.id}:{otp}")
        expires_at = datetime.now(timezone.utc) + timedelta(minutes=10)

        # Invalidate any previously active reset OTPs for this user
        await self.token_repo.invalidate_user_reset_tokens(user.id)

        await self.token_repo.create_password_reset_token(
            user_id=user.id,
            token_hash=token_hash,
            expires_at=expires_at,
        )
        return otp

    async def verify_otp(self, email: str, otp: str) -> bool:
        """
        Verify if the given 4-digit OTP is valid, unexpired, and unused for the user email.
        """
        user = await self.user_repo.get_by_email(email)
        if not user or not user.is_active:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invalid or expired OTP code",
            )

        token_hash = hash_token(f"{user.id}:{otp.strip()}")
        reset_token = await self.token_repo.get_valid_password_reset_token_by_user(user.id, token_hash)
        if not reset_token:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invalid or expired OTP code",
            )
        return True

    async def reset_password(self, email: str, otp: str, new_password: str) -> None:
        """
        Validate 4-digit OTP and update user's password.
        """
        user = await self.user_repo.get_by_email(email)
        if not user or not user.is_active:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invalid or expired OTP code",
            )

        token_hash = hash_token(f"{user.id}:{otp.strip()}")
        reset_token = await self.token_repo.get_valid_password_reset_token_by_user(user.id, token_hash)
        if not reset_token:
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Invalid or expired OTP code",
            )

        hashed_password = get_password_hash(new_password)
        await self.user_repo.update_password(user, hashed_password)
        await self.token_repo.mark_password_reset_token_used(reset_token)
        # Invalidate all active sessions for security
        await self.token_repo.revoke_all_user_tokens(user.id)

    async def change_password(
        self,
        user_id: uuid.UUID,
        current_password: str,
        new_password: str,
    ) -> None:
        user = await self.user_repo.get_by_id(user_id)
        if not user:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail="User not found",
            )

        if not user.hashed_password or not verify_password(current_password, user.hashed_password):
            raise HTTPException(
                status_code=status.HTTP_400_BAD_REQUEST,
                detail="Current password is incorrect",
            )

        hashed_password = get_password_hash(new_password)
        await self.user_repo.update_password(user, hashed_password)
