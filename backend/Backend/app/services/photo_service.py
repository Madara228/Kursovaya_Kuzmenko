import os
import uuid
import aiofiles
from uuid import UUID
from fastapi import UploadFile
from sqlalchemy.orm import Session

from app.config import settings
from app.models.user import User, Photo
from app.core.exceptions import AppException

ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".webp"}
MAX_PHOTOS_PER_USER = 6


async def save_user_photo(
    db: Session,
    user: User,
    file: UploadFile,
    is_main: bool = False,
) -> Photo:
    """
    Saves an uploaded photo to media/user_{user_id}/ and adds record to database.
    Enforces maximum of 6 photos per user and single main photo rule.
    """
    # 1. Check photos limit (maximum 6)
    existing_count = db.query(Photo).filter(Photo.user_id == user.id).count()
    if existing_count >= MAX_PHOTOS_PER_USER:
        raise AppException(
            status_code=409,
            error_name="PHOTO_LIMIT_REACHED",
            message="Maximum of 6 photos reached",
        )

    # 2. Check file extension
    filename = file.filename or "photo.jpg"
    ext = os.path.splitext(filename)[1].lower()
    if ext not in ALLOWED_EXTENSIONS:
        raise AppException(
            status_code=400,
            error_name="Bad Request",
            message=f"Unsupported file format '{ext}'. Allowed: {', '.join(ALLOWED_EXTENSIONS)}",
        )

    # 3. If this is the user's first photo, automatically make it main
    if existing_count == 0:
        is_main = True

    # 4. If new photo is main, unset previous main photo
    if is_main:
        db.query(Photo).filter(Photo.user_id == user.id, Photo.is_main == True).update(
            {"is_main": False}
        )

    # 5. Create directory media/user_{user_id}
    user_media_folder = os.path.join(settings.MEDIA_DIR, f"user_{user.id}")
    os.makedirs(user_media_folder, exist_ok=True)

    # 6. Generate unique filename and write to disk
    photo_id = uuid.uuid4()
    saved_filename = f"{photo_id}{ext}"
    file_path = os.path.join(user_media_folder, saved_filename)

    content = await file.read()
    async with aiofiles.open(file_path, "wb") as out_file:
        await out_file.write(content)

    # 7. Create DB record with relative URL path
    photo_url = f"/media/user_{user.id}/{saved_filename}"
    new_photo = Photo(
        id=photo_id,
        user_id=user.id,
        url=photo_url,
        is_main=is_main,
    )
    db.add(new_photo)
    db.commit()
    db.refresh(new_photo)
    return new_photo


def set_main_photo(db: Session, user: User, photo_id: UUID) -> Photo:
    """
    Sets the specified photo as main for the user.
    """
    photo = db.query(Photo).filter(Photo.id == photo_id).first()
    if not photo:
        raise AppException(
            status_code=404,
            error_name="Not Found",
            message="Photo not found",
        )

    if photo.user_id != user.id:
        raise AppException(
            status_code=403,
            error_name="Forbidden",
            message="You do not have permission to modify this photo",
        )

    # Unset is_main for all photos of this user
    db.query(Photo).filter(Photo.user_id == user.id).update({"is_main": False})

    # Set selected photo as main
    photo.is_main = True
    db.commit()
    db.refresh(photo)
    return photo


def delete_user_photo(db: Session, user: User, photo_id: UUID) -> None:
    """
    Deletes photo from disk and database.
    If the deleted photo was main, promotes another photo to main if any remain.
    """
    photo = db.query(Photo).filter(Photo.id == photo_id).first()
    if not photo:
        raise AppException(
            status_code=404,
            error_name="Not Found",
            message="Photo not found",
        )

    if photo.user_id != user.id:
        raise AppException(
            status_code=403,
            error_name="Forbidden",
            message="You do not have permission to delete this photo",
        )

    was_main = photo.is_main
    url = photo.url

    # Delete from database
    db.delete(photo)
    db.commit()

    # Try to delete file from disk
    if url.startswith("/media/"):
        relative_disk_path = url[len("/media/"):]
        abs_disk_path = os.path.join(settings.MEDIA_DIR, relative_disk_path)
        if os.path.exists(abs_disk_path):
            try:
                os.remove(abs_disk_path)
            except OSError:
                pass

    # If the deleted photo was main, set another photo as main if available
    if was_main:
        remaining_photo = db.query(Photo).filter(Photo.user_id == user.id).first()
        if remaining_photo:
            remaining_photo.is_main = True
            db.commit()
