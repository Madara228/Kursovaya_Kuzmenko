import uuid
from typing import Optional
from uuid import UUID
from pydantic import BaseModel, Field


# 6.1. Create invite request and response
class CreateInviteRequest(BaseModel):
    offer_id: UUID = Field(..., description="UUID of the partner offer")
    contact_initiator_id: UUID = Field(..., description="UUID of contact initiator")
    contact_responder_id: UUID = Field(..., description="UUID of contact responder")


class CreateInviteResponse(BaseModel):
    invite_link: str = Field(..., description="Full invite link with JWT token")
    message_id: UUID = Field(default_factory=uuid.uuid4, description="UUID of the chat message")


# 6.2. Preview invite response
class InvitePreviewResponse(BaseModel):
    offer_title: str
    partner_name: str
    initiator_name: str
    is_valid: bool


# 6.3. Accept invite response
class AcceptInviteResponse(BaseModel):
    status: str = "SUCCESS"
    promocode_id: UUID
