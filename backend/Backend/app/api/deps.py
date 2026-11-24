from uuid import UUID
from fastapi import Depends, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from jose import JWTError
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.user import User
from app.models.partner import Partner
from app.core.security import decode_access_token
from app.core.exceptions import AppException

security_bearer = HTTPBearer(auto_error=False)


def get_current_user(
    credentials: HTTPAuthorizationCredentials = Depends(security_bearer),
    db: Session = Depends(get_db),
) -> User:
    """
    Extracts current authenticated User from JWT token.
    Raises 401 Unauthorized if token is missing or invalid.
    """
    if not credentials:
        raise AppException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            error_name="Unauthorized",
            message="Not authenticated",
        )

    try:
        payload = decode_access_token(credentials.credentials)
        user_id_str: str = payload.get("sub")
        role: str = payload.get("role")
        if not user_id_str or role != "user":
            raise AppException(
                status_code=status.HTTP_401_UNAUTHORIZED,
                error_name="Unauthorized",
                message="Invalid authentication credentials or token role",
            )
        user_id = UUID(user_id_str)
    except (JWTError, ValueError):
        raise AppException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            error_name="Unauthorized",
            message="Could not validate credentials",
        )

    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise AppException(
            status_code=status.HTTP_404_NOT_FOUND,
            error_name="Not Found",
            message="User not found",
        )

    if not user.is_active:
        raise AppException(
            status_code=status.HTTP_403_FORBIDDEN,
            error_name="USER_BLOCKED",
            message="User account is blocked",
        )

    return user


def get_current_partner(
    credentials: HTTPAuthorizationCredentials = Depends(security_bearer),
    db: Session = Depends(get_db),
) -> Partner:
    """
    Extracts current authenticated Partner from JWT token.
    """
    if not credentials:
        raise AppException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            error_name="Unauthorized",
            message="Not authenticated",
        )

    try:
        payload = decode_access_token(credentials.credentials)
        partner_id_str: str = payload.get("sub")
        role: str = payload.get("role")
        if not partner_id_str or role != "partner":
            raise AppException(
                status_code=status.HTTP_403_FORBIDDEN,
                error_name="PARTNER_ACCESS_REQUIRED",
                message="Partner access required",
            )
        partner_id = UUID(partner_id_str)
    except (JWTError, ValueError):
        raise AppException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            error_name="Unauthorized",
            message="Could not validate credentials",
        )

    partner = db.query(Partner).filter(Partner.id == partner_id).first()
    if not partner:
        raise AppException(
            status_code=status.HTTP_404_NOT_FOUND,
            error_name="Not Found",
            message="Partner not found",
        )

    return partner
