from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session

from app.database import get_db
from app.config import settings
from app.schemas.auth import (
    EsiaLoginRequest,
    TokenResponse,
    UserTokenInfo,
    PartnerLoginRequest,
    PartnerTokenResponse,
)
from app.services.auth_service import authenticate_esia_user, get_or_create_user
from app.core.security import create_access_token, verify_password
from app.core.exceptions import AppException
from app.models.partner import Partner

router = APIRouter(prefix="/auth", tags=["Authentication"])


@router.post(
    "/esia-login",
    response_model=TokenResponse,
    status_code=status.HTTP_200_OK,
    summary="Login via ESIA mock",
    description="Authenticates via ESIA mock credentials, creates user if not existing, returns JWT.",
)
def esia_login(data: EsiaLoginRequest, db: Session = Depends(get_db)):
    # 1. Authenticate in ESIA accounts
    esia_account = authenticate_esia_user(db, data.login, data.password)

    # 2. Get or create user
    user, is_new_user = get_or_create_user(db, esia_account)

    # 3. Create access token (7 days)
    access_token = create_access_token(subject=user.id, role="user")

    return TokenResponse(
        access_token=access_token,
        token_type="bearer",
        expires_in=settings.ACCESS_TOKEN_EXPIRE_SECONDS,
        user=UserTokenInfo(
            id=user.id,
            first_name=user.first_name,
            last_name=user.last_name,
            is_new_user=is_new_user,
        ),
    )


@router.post(
    "/partner-login",
    response_model=PartnerTokenResponse,
    status_code=status.HTTP_200_OK,
    summary="Login for partners",
    description="Authenticates partner by login and password, returns JWT.",
)
def partner_login(data: PartnerLoginRequest, db: Session = Depends(get_db)):
    partner = db.query(Partner).filter(Partner.login == data.login).first()
    if not partner or not verify_password(data.password, partner.password_hash):
        raise AppException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            error_name="Unauthorized",
            message="Invalid partner login or password",
        )

    # Create partner access token (24 hours)
    access_token = create_access_token(subject=partner.id, role="partner")

    return PartnerTokenResponse(
        access_token=access_token,
        token_type="bearer",
        expires_in=settings.PARTNER_ACCESS_TOKEN_EXPIRE_SECONDS,
        partner_id=partner.id,
    )
