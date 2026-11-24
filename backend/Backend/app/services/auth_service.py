from datetime import date
from typing import Tuple
from sqlalchemy.orm import Session

from app.models.user import EsiaAccount, User
from app.core.security import verify_password
from app.core.exceptions import AppException


def calculate_age(birth_date: date) -> int:
    today = date.today()
    return today.year - birth_date.year - ((today.month, today.day) < (birth_date.month, birth_date.day))


def authenticate_esia_user(db: Session, login: str, password: str) -> EsiaAccount:
    """
    Checks credentials against esia_accounts table.
    Raises AppException(401, 'INVALID_ESIA_CREDENTIALS') if invalid.
    """
    esia_account = db.query(EsiaAccount).filter(EsiaAccount.login == login).first()
    if not esia_account or not verify_password(password, esia_account.password_hash):
        raise AppException(
            status_code=401,
            error_name="INVALID_ESIA_CREDENTIALS",
            message="Invalid ESIA login or password",
        )

    # Check 18+ requirement
    age = calculate_age(esia_account.birth_date)
    if age < 18:
        raise AppException(
            status_code=403,
            error_name="USER_UNDER_18",
            message="User must be at least 18 years old to use the service",
        )

    return esia_account


def get_or_create_user(db: Session, esia_account: EsiaAccount) -> Tuple[User, bool]:
    """
    Finds existing User by esia_id or creates a new one.
    Returns (user, is_new_user).
    """
    user = db.query(User).filter(User.esia_id == esia_account.id).first()
    if user:
        if not user.is_active:
            raise AppException(
                status_code=403,
                error_name="USER_BLOCKED",
                message="User account has been blocked",
            )
        return user, False

    # Create new User from ESIA account data
    new_user = User(
        esia_id=esia_account.id,
        first_name=esia_account.first_name,
        last_name=esia_account.last_name,
        birth_date=esia_account.birth_date,
        gender=esia_account.gender,
        is_active=True,
    )
    db.add(new_user)
    db.commit()
    db.refresh(new_user)
    return new_user, True
