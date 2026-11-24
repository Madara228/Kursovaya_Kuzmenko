from datetime import datetime
from typing import List, Optional
from uuid import UUID
from pydantic import BaseModel, Field

from app.schemas.user import PhotoResponse
from app.schemas.contact import PaginationMeta


# 5.1. Matches
class MatchUser(BaseModel):
    id: UUID
    first_name: str
    photos: List[PhotoResponse] = []

    class Config:
        from_attributes = True


class MatchItem(BaseModel):
    id: UUID
    user: MatchUser


class MatchesResponse(BaseModel):
    data: List[MatchItem]
    meta: PaginationMeta


# 5.2. Chats list with last message
class LastMessageInfo(BaseModel):
    text: str
    created_at: datetime
    is_read: bool


class ChatListItem(BaseModel):
    contact_initiator_id: UUID
    contact_responder_id: UUID
    companion: MatchUser
    last_message: Optional[LastMessageInfo] = None
    unread_count: int = 0


class ChatsListResponse(BaseModel):
    data: List[ChatListItem]
    meta: PaginationMeta


# 5.3. Messages in chat
class MessageItem(BaseModel):
    author_id: UUID
    text: str
    is_read: bool
    created_at: datetime

    class Config:
        from_attributes = True


class MessagesListResponse(BaseModel):
    data: List[MessageItem]
    meta: PaginationMeta


# 5.4. Send message
class SendMessageRequest(BaseModel):
    text: str = Field(..., min_length=1, description="Message text")


class SendMessageResponse(BaseModel):
    author_id: UUID
    text: str
    is_read: bool
    created_at: datetime

    class Config:
        from_attributes = True


# 5.5. Mark read
class MarkReadResponse(BaseModel):
    status: str = "success"
    updated_count: int
