from datetime import date
from typing import List, Optional
from uuid import UUID
from pydantic import BaseModel, Field

from app.schemas.contact import PaginationMeta


# Partner Profile (API 7.2)
class PartnerProfileResponse(BaseModel):
    id: UUID
    login: str
    contact_person: str
    phone: str
    name: str
    city: str
    address: str
    contact_info: Optional[str] = None

    class Config:
        from_attributes = True


# Offer item for Partner (API 7.3, 7.4, 7.5)
class PartnerOfferResponse(BaseModel):
    id: UUID
    partner_id: UUID
    title: str
    discount_info: str
    valid_until: date
    photo_url: str

    class Config:
        from_attributes = True


# Offer info for User (API 7.1)
class OfferPartnerInfo(BaseModel):
    name: str
    city: str
    address: str

    class Config:
        from_attributes = True


class UserOfferItem(BaseModel):
    id: UUID
    partner_id: UUID
    title: str
    discount_info: str
    valid_until: date
    photo_url: str
    partner: OfferPartnerInfo

    class Config:
        from_attributes = True


class UserOffersResponse(BaseModel):
    data: List[UserOfferItem]
    meta: PaginationMeta


class OfferDeactivateResponse(BaseModel):
    message: str = "Offer deactivated successfully"
