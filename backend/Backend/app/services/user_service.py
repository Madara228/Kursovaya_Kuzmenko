from typing import Optional
from uuid import UUID
from sqlalchemy.orm import Session

from app.models.user import User
from app.schemas.user import UserUpdateRequest
from app.core.exceptions import AppException


def get_user_by_id(db: Session, user_id: UUID) -> User:
    """
    Finds a user by ID. Raises 404 if not found or inactive.
    """
    user = db.query(User).filter(User.id == user_id).first()
    if not user:
        raise AppException(
            status_code=404,
            error_name="Not Found",
            message="User not found",
        )
    if not user.is_active:
        raise AppException(
            status_code=403,
            error_name="USER_BLOCKED",
            message="User account is blocked",
        )
    return user


def update_user_profile(db: Session, user: User, update_data: UserUpdateRequest) -> User:
    """
    Updates city and bio fields of the user profile.
    """
    if update_data.city is not None:
        user.city = update_data.city.strip()
    if update_data.bio is not None:
        user.bio = update_data.bio.strip()

    db.add(user)
    db.commit()
    db.refresh(user)
    return user
