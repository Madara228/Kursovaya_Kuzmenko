from uuid import UUID
from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.user import User
from app.api.deps import get_current_user
from app.schemas.user import UserProfileResponse, PublicUserProfileResponse, UserUpdateRequest
from app.services.user_service import get_user_by_id, update_user_profile

router = APIRouter(prefix="/users", tags=["Users"])


@router.get(
    "/me",
    response_model=UserProfileResponse,
    status_code=status.HTTP_200_OK,
    summary="Get current user profile",
    description="Returns full profile of the authenticated user, including esia_id and photos.",
)
def get_my_profile(
    current_user: User = Depends(get_current_user),
):
    return current_user


@router.patch(
    "/me",
    response_model=UserProfileResponse,
    status_code=status.HTTP_200_OK,
    summary="Update current user profile",
    description="Updates user profile fields (city, bio).",
)
def update_my_profile(
    update_data: UserUpdateRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    updated_user = update_user_profile(db, current_user, update_data)
    return updated_user


@router.get(
    "/{user_id}",
    response_model=PublicUserProfileResponse,
    status_code=status.HTTP_200_OK,
    summary="Get another user's public profile",
    description="Returns public profile for viewing (confidential esia_id is hidden).",
)
def get_user_profile(
    user_id: UUID,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    target_user = get_user_by_id(db, user_id)
    return target_user
