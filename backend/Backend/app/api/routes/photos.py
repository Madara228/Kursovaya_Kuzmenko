from uuid import UUID
from fastapi import APIRouter, Depends, UploadFile, File, Form, status
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.user import User
from app.api.deps import get_current_user
from app.schemas.user import PhotoResponse, MessageResponse
from app.services.photo_service import (
    save_user_photo,
    set_main_photo,
    delete_user_photo,
)

router = APIRouter(prefix="/photos", tags=["Photos"])


@router.post(
    "",
    response_model=PhotoResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Upload photo",
    description="Uploads a new user photo (max 6 photos). First photo becomes main automatically.",
)
async def upload_photo(
    file: UploadFile = File(...),
    is_main: bool = Form(False),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    photo = await save_user_photo(db, current_user, file, is_main)
    return photo


@router.patch(
    "/{photo_id}/main",
    response_model=MessageResponse,
    status_code=status.HTTP_200_OK,
    summary="Set main photo",
    description="Marks specified photo as main, unsetting previous main photo.",
)
def make_main_photo(
    photo_id: UUID,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    set_main_photo(db, current_user, photo_id)
    return MessageResponse(message="Main photo updated")


@router.delete(
    "/{photo_id}",
    response_model=MessageResponse,
    status_code=status.HTTP_200_OK,
    summary="Delete photo",
    description="Deletes photo from disk and database.",
)
def delete_photo(
    photo_id: UUID,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    delete_user_photo(db, current_user, photo_id)
    return MessageResponse(message="Photo deleted successfully")
