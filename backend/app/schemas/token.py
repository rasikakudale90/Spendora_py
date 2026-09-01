"""
Token schemas for JWT Access and Refresh token lifecycle.
"""
from typing import Optional
from pydantic import BaseModel


class TokenResponse(BaseModel):
    """
    Response returned on successful login, registration, or refresh.
    Access token is short-lived; refresh token is returned in an HttpOnly cookie.
    """
    access_token: str
    token_type: str = "bearer"
    expires_in: int = 900  # 15 minutes in seconds


class TokenPayload(BaseModel):
    """
    Decoded JWT claims from access token.
    """
    sub: str  # user_id
    email: str
    type: str = "access"
    exp: Optional[int] = None
    iat: Optional[int] = None
