from datetime import date
from typing import List, Optional
from uuid import UUID
from fastapi import APIRouter, Depends, Query, UploadFile, File, Form, status
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.user import User
from app.models.partner import Partner
from app.api.deps import get_current_user, get_current_partner
from app.schemas.contact import PaginationMeta
from app.schemas.partner import (
    PartnerProfileResponse,
    PartnerOfferResponse,
    UserOfferItem,
    UserOffersResponse,
    OfferDeactivateResponse,
)
from app.services.partner_service import (
    get_active_offers_for_users,
    get_partner_offers,
    create_partner_offer,
    update_partner_offer,
    deactivate_partner_offer,
)

router = APIRouter(tags=["Partners & Offers"])

# 7.1 User gets active dating offers
@router.get(
    "/offers",
    response_model=UserOffersResponse,
    status_code=status.HTTP_200_OK,
    summary="Get dating offers",
    description="Returns paginated list of active partner offers for dating.",
)
def read_offers(
    page: int = Query(1, ge=1, description="Page number"),
    limit: int = Query(20, ge=1, le=100, description="Items per page"),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    offers, total_items, total_pages = get_active_offers_for_users(db, page, limit)
    return UserOffersResponse(
        data=[UserOfferItem.model_validate(o) for o in offers],
        meta=PaginationMeta(
            total_items=total_items,
            total_pages=total_pages,
            current_page=page,
            per_page=limit,
        ),
    )


# 7.2 Partner gets own profile
@router.get(
    "/partners/me",
    response_model=PartnerProfileResponse,
    status_code=status.HTTP_200_OK,
    summary="Get partner profile",
    description="Returns full profile of authenticated partner.",
)
def get_my_partner_profile(
    current_partner: Partner = Depends(get_current_partner),
):
    return current_partner


# 7.4 Partner gets own offers
@router.get(
    "/partners/me/offers",
    response_model=List[PartnerOfferResponse],
    status_code=status.HTTP_200_OK,
    summary="Get partner's offers",
    description="Returns all offers created by authenticated partner.",
)
def read_partner_offers(
    current_partner: Partner = Depends(get_current_partner),
    db: Session = Depends(get_db),
):
    return get_partner_offers(db, current_partner.id)


# 7.3 Partner creates new offer
@router.post(
    "/partners/me/offers",
    response_model=PartnerOfferResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Create partner offer",
    description="Creates a new partner offer with an uploaded photo.",
)
async def create_offer(
    title: str = Form(...),
    discount_info: str = Form(...),
    valid_until: date = Form(...),
    photo: UploadFile = File(...),
    current_partner: Partner = Depends(get_current_partner),
    db: Session = Depends(get_db),
):
    new_offer = await create_partner_offer(
        db,
        current_partner,
        title,
        discount_info,
        valid_until,
        photo,
    )
    return new_offer


# 7.5 Partner updates offer
@router.patch(
    "/partners/me/offers/{offer_id}",
    response_model=PartnerOfferResponse,
    status_code=status.HTTP_200_OK,
    summary="Update partner offer",
    description="Updates offer fields or photo.",
)
async def patch_offer(
    offer_id: UUID,
    title: Optional[str] = Form(None),
    discount_info: Optional[str] = Form(None),
    valid_until: Optional[date] = Form(None),
    photo: Optional[UploadFile] = File(None),
    current_partner: Partner = Depends(get_current_partner),
    db: Session = Depends(get_db),
):
    updated = await update_partner_offer(
        db,
        current_partner,
        offer_id,
        title,
        discount_info,
        valid_until,
        photo,
    )
    return updated


# 7.6 Partner deactivates offer
@router.delete(
    "/partners/me/offers/{offer_id}",
    response_model=OfferDeactivateResponse,
    status_code=status.HTTP_200_OK,
    summary="Deactivate partner offer",
    description="Sets valid_until = CURRENT_DATE for the offer.",
)
def delete_offer(
    offer_id: UUID,
    current_partner: Partner = Depends(get_current_partner),
    db: Session = Depends(get_db),
):
    deactivate_partner_offer(db, current_partner, offer_id)
    return OfferDeactivateResponse()
