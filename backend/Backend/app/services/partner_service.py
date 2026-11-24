import math
import os
import uuid
import aiofiles
from datetime import date
from typing import List, Tuple, Optional
from uuid import UUID
from fastapi import UploadFile
from sqlalchemy.orm import Session
from sqlalchemy import desc

from app.config import settings
from app.models.partner import Partner, Offer
from app.core.exceptions import AppException

ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".webp"}


async def save_offer_photo(offer_id: UUID, file: UploadFile) -> str:
    filename = file.filename or "main.jpg"
    ext = os.path.splitext(filename)[1].lower()
    if ext not in ALLOWED_EXTENSIONS:
        raise AppException(
            status_code=400,
            error_name="Bad Request",
            message=f"Unsupported file format '{ext}'. Allowed: {', '.join(ALLOWED_EXTENSIONS)}",
        )

    offer_media_folder = os.path.join(settings.MEDIA_DIR, "offers", str(offer_id))
    os.makedirs(offer_media_folder, exist_ok=True)

    saved_filename = f"main{ext}"
    file_path = os.path.join(offer_media_folder, saved_filename)

    content = await file.read()
    async with aiofiles.open(file_path, "wb") as out_file:
        await out_file.write(content)

    return f"/media/offers/{offer_id}/{saved_filename}"


def get_active_offers_for_users(
    db: Session,
    page: int = 1,
    limit: int = 20,
) -> Tuple[List[Offer], int, int]:
    if page < 1:
        page = 1
    if limit < 1 or limit > 100:
        limit = 20

    today = date.today()
    query = (
        db.query(Offer)
        .filter(Offer.valid_until >= today)
        .order_by(desc(Offer.valid_until))
    )

    total_items = query.count()
    total_pages = math.ceil(total_items / limit) if total_items > 0 else 0
    offset = (page - 1) * limit
    offers = query.offset(offset).limit(limit).all()

    return offers, total_items, total_pages



def get_partner_offers(
    db: Session,
    partner_id: UUID,
) -> List[Offer]:
    return (db.query(Offer)
        .filter(Offer.partner_id == partner_id)
        .order_by(desc(Offer.valid_until))
        .all())



async def create_partner_offer(
    db: Session,
    partner: Partner,
    title: str,
    discount_info: str,
    valid_until: date,
    photo: UploadFile,
) -> Offer:
    offer_id = uuid.uuid4()
    photo_url = await save_offer_photo(offer_id, photo)

    new_offer = Offer(
        id=offer_id,
        partner_id=partner.id,
        title=title,
        discount_info=discount_info,
        valid_until=valid_until,
        photo_url=photo_url,
    )
    db.add(new_offer)
    db.commit()
    db.refresh(new_offer)
    return new_offer



async def update_partner_offer(
    db: Session,
    partner: Partner,
    offer_id: UUID,
    title: Optional[str] = None,
    discount_info: Optional[str] = None,
    valid_until: Optional[date] = None,
    photo: Optional[UploadFile] = None,
) -> Offer:
    offer = db.query(Offer).filter(Offer.id == offer_id).first()
    if not offer:
        raise AppException(status_code=404, error_name="Not Found", message="Offer not found")

    if offer.partner_id != partner.id:
        raise AppException(status_code=403, error_name="Forbidden", message="You do not own this offer")

    if title is not None:
        offer.title = title
    if discount_info is not None:
        offer.discount_info = discount_info
    if valid_until is not None:
        offer.valid_until = valid_until
    if photo is not None:
        photo_url = await save_offer_photo(offer.id, photo)
        offer.photo_url = photo_url


    db.commit()
    db.refresh(offer)
    return offer



def deactivate_partner_offer(
    db: Session,
    partner: Partner,
    offer_id: UUID,
) -> None:
    offer = db.query(Offer).filter(Offer.id == offer_id).first()
    if not offer:
        raise AppException(status_code=404, error_name="Not Found", message="Offer not found")

    if offer.partner_id != partner.id:
        raise AppException(status_code=403, error_name="Forbidden", message="You do not own this offer")

    offer.valid_until = date.today()
    db.commit()
