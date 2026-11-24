from fastapi import APIRouter, Depends, status
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.user import User
from app.api.deps import get_current_user
from app.schemas.invite import (
    CreateInviteRequest,
    CreateInviteResponse,
    InvitePreviewResponse,
    AcceptInviteResponse,
)
from app.services.invite_service import (
    create_invite as service_create_invite,
    get_invite_preview as service_get_invite_preview,
    accept_invite as service_accept_invite,
)

router = APIRouter(tags=["Invites & Promocodes"])


# 6.1. Create invite (Link)
@router.post(
    "/invites",
    response_model=CreateInviteResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Create date invite link",
    description="Generates JWT invite link (7 days) and posts chat message from initiator.",
)
def create_invite_endpoint(
    data: CreateInviteRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    invite_link, message_id = service_create_invite(
        db=db,
        current_user=current_user,
        offer_id=data.offer_id,
        contact_initiator_id=data.contact_initiator_id,
        contact_responder_id=data.contact_responder_id,
    )
    return CreateInviteResponse(
        invite_link=invite_link,
        message_id=message_id,
    )


# 6.2. Preview invite link (Public)
@router.get(
    "/invites/{token}",
    response_model=InvitePreviewResponse,
    status_code=status.HTTP_200_OK,
    summary="Preview invite link",
    description="Public endpoint to inspect invite details before accepting.",
)
def get_invite_endpoint(
    token: str,
    db: Session = Depends(get_db),
):
    return service_get_invite_preview(db=db, token=token)


# 6.3. Accept invite
@router.post(
    "/invites/{token}/accept",
    response_model=AcceptInviteResponse,
    status_code=status.HTTP_200_OK,
    summary="Accept invite",
    description="Generates a promocode, splits into two halves, and posts 2 messages in chat.",
)
def accept_invite_endpoint(
    token: str,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    promocode = service_accept_invite(
        db=db,
        current_user=current_user,
        token=token,
    )
    return AcceptInviteResponse(
        status="SUCCESS",
        promocode_id=promocode.id,
    )
