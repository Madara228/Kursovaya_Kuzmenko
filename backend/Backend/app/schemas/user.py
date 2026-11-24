from datetime import date, datetime
from typing import List, Optional
from uuid import UUID
from pydantic import BaseModel, Field

from app.models.user import GenderEnum


# Photo schemas
class PhotoResponse(BaseModel):
    id: UUID
    url: str
    is_main: bool

    class Config:
        from_attributes = True


class MessageResponse(BaseModel):
    message: str


# User schemas
class UserUpdateRequest(BaseModel):
    city: Optional[str] = Field(None, description="User's city")
    bio: Optional[str] = Field(None, description="User's bio / description")


class UserProfileResponse(BaseModel):
    id: UUID
    esia_id: UUID
    first_name: str
    last_name: str
    birth_date: date
    gender: GenderEnum
    city: Optional[str] = None
    bio: Optional[str] = None
    photos: List[PhotoResponse] = []

    class Config:
        from_attributes = True


class PublicUserProfileResponse(BaseModel):
    id: UUID
    first_name: str
    last_name: str
    birth_date: date
    gender: GenderEnum
    city: Optional[str] = None
    bio: Optional[str] = None
    photos: List[PhotoResponse] = []

    class Config:
        from_attributes = True
