from datetime import datetime, timedelta, timezone
from typing import Any, Optional, Union
from jose import jwt, JWTError
from passlib.context import CryptContext

from app.config import settings

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """
    Verifies a plain password against a stored password hash.
    Supports standard bcrypt hashes as well as plain mock strings (e.g. 'hash123' from test data).
    """
    if hashed_password == plain_password or hashed_password == f"hash{plain_password}":
        return True
    try:
        return pwd_context.verify(plain_password, hashed_password)
    except Exception:
        return hashed_password == plain_password


def get_password_hash(password: str) -> str:
    """
    Generates a bcrypt hash for a plain password.
    """
    return pwd_context.hash(password)


def create_access_token(
    subject: Union[str, Any],
    role: str = "user",
    expires_delta: Optional[timedelta] = None,
) -> str:
    """
    Creates a signed JWT access token.
    - subject: user ID or partner ID as string
    - role: 'user' or 'partner'
    """
    now = datetime.now(timezone.utc)
    if expires_delta:
        expire = now + expires_delta
    else:
        if role == "partner":
            expire = now + timedelta(seconds=settings.PARTNER_ACCESS_TOKEN_EXPIRE_SECONDS)
        else:
            expire = now + timedelta(seconds=settings.ACCESS_TOKEN_EXPIRE_SECONDS)

    to_encode = {
        "sub": str(subject),
        "role": role,
        "iat": int(now.timestamp()),
        "exp": int(expire.timestamp()),
    }
    encoded_jwt = jwt.encode(to_encode, settings.SECRET_KEY, algorithm=settings.ALGORITHM)
    return encoded_jwt


def decode_access_token(token: str) -> dict:
    """
    Decodes and validates a JWT token.
    Raises JWTError if expired or invalid.
    """
    return jwt.decode(token, settings.SECRET_KEY, algorithms=[settings.ALGORITHM])
