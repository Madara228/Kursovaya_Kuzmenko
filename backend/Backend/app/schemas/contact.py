import enum
from datetime import date
from typing import List, Optional
from uuid import UUID
from pydantic import BaseModel, Field

from app.schemas.user import PhotoResponse
from app.models.contact import ContactStatus


class SwipeAction(str, enum.Enum):
    LIKE = "LIKE"
    DISLIKE = "DISLIKE"


class SwipeRequest(BaseModel):
    target_user_id: UUID = Field(..., description="ID of the user being liked or disliked")
    action: SwipeAction = Field(..., description="'LIKE' or 'DISLIKE'")


class SwipeResponse(BaseModel):
    status: ContactStatus
    is_match: bool


# Feed item schema according to API contract 4.1
class FeedUserItem(BaseModel):
    id: UUID
    first_name: str
    birth_date: date
    city: Optional[str] = None
    bio: Optional[str] = None
    photos: List[PhotoResponse] = []

    class Config:
        from_attributes = True


# Standard pagination meta
class PaginationMeta(BaseModel):
    total_items: int
    total_pages: int
    current_page: int
    per_page: int


class FeedResponse(BaseModel):
    data: List[FeedUserItem]
    meta: PaginationMeta
