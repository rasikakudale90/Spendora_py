"""
UserRepository: Data access layer for User entities.
"""
import uuid
from typing import Optional

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.models.user import User


class UserRepository:
    def __init__(self, session: AsyncSession):
        self.session = session

    async def get_by_id(self, user_id: uuid.UUID) -> Optional[User]:
        stmt = select(User).where(User.id == user_id)
        result = await self.session.execute(stmt)
        return result.scalar_one_or_none()

    async def get_by_email(self, email: str) -> Optional[User]:
        stmt = select(User).where(User.email == email.strip().lower())
        result = await self.session.execute(stmt)
        return result.scalar_one_or_none()

    async def get_by_google_id(self, google_id: str) -> Optional[User]:
        stmt = select(User).where(User.google_id == google_id)
        result = await self.session.execute(stmt)
        return result.scalar_one_or_none()

    async def create(
        self,
        *,
        email: str,
        hashed_password: Optional[str] = None,
        full_name: Optional[str] = None,
        avatar_url: Optional[str] = None,
        auth_provider: str = "email",
        google_id: Optional[str] = None,
        is_verified: bool = False,
    ) -> User:
        user = User(
            email=email.strip().lower(),
            hashed_password=hashed_password,
            full_name=full_name,
            avatar_url=avatar_url,
            auth_provider=auth_provider,
            google_id=google_id,
            is_verified=is_verified,
        )
        self.session.add(user)
        await self.session.flush()
        await self.session.refresh(user)
        return user

    async def update_password(self, user: User, hashed_password: str) -> User:
        user.hashed_password = hashed_password
        await self.session.flush()
        await self.session.refresh(user)
        return user

    async def link_google_account(
        self,
        user: User,
        google_id: str,
        avatar_url: Optional[str] = None,
        full_name: Optional[str] = None,
    ) -> User:
        user.google_id = google_id
        user.is_verified = True
        if avatar_url and not user.avatar_url:
            user.avatar_url = avatar_url
        if full_name and not user.full_name:
            user.full_name = full_name
        await self.session.flush()
        await self.session.refresh(user)
        return user
