import secrets
import uuid
from datetime import date, datetime, timedelta, timezone
from typing import Tuple
from uuid import UUID

from jose import jwt, JWTError
from sqlalchemy.orm import Session

from app.config import settings
from app.core.exceptions import AppException
from app.models.contact import Contact, ContactStatus
from app.models.message import Message
from app.models.partner import Offer
from app.models.promocode import Promocode, PromocodeStatus
from app.models.user import User
from app.schemas.invite import InvitePreviewResponse


def create_invite(
    db: Session,
    current_user: User,
    offer_id: UUID,
    contact_initiator_id: UUID,
    contact_responder_id: UUID,
) -> Tuple[str, UUID]:
    # 1. Validate that current user is a participant of the contact
    if current_user.id not in (contact_initiator_id, contact_responder_id):
        raise AppException(
            status_code=403,
            error_name="Forbidden",
            message="You are not a participant in this contact",
        )

    # 2. Check contact exists and status is MATCHED
    contact = (
        db.query(Contact)
        .filter(
            Contact.initiator_id == contact_initiator_id,
            Contact.responder_id == contact_responder_id,
        )
        .first()
    )
    if not contact:
        raise AppException(
            status_code=404,
            error_name="Not Found",
            message="Contact not found",
        )

    if contact.status != ContactStatus.MATCHED:
        raise AppException(
            status_code=400,
            error_name="Bad Request",
            message="Contact is not in MATCHED status",
        )

    # 3. Check offer exists and is active
    offer = db.query(Offer).filter(Offer.id == offer_id).first()
    if not offer:
        raise AppException(
            status_code=404,
            error_name="Not Found",
            message="Offer not found",
        )

    if offer.valid_until < date.today():
        raise AppException(
            status_code=400,
            error_name="INVITE_EXPIRED",
            message="Offer has expired",
        )

    # Determine other user id
    other_user_id = (
        contact_responder_id
        if current_user.id == contact_initiator_id
        else contact_initiator_id
    )

    # 4. Generate JWT invite token (expires in 7 days)
    now = datetime.now(timezone.utc)
    expire = now + timedelta(days=7)
    payload = {
        "sub": "invite",
        "type": "invite",
        "offer_id": str(offer.id),
        "contact_initiator_id": str(contact_initiator_id),
        "contact_responder_id": str(contact_responder_id),
        "sender_id": str(current_user.id),
        "receiver_id": str(other_user_id),
        "iat": int(now.timestamp()),
        "exp": int(expire.timestamp()),
    }
    invite_token = jwt.encode(payload, settings.SECRET_KEY, algorithm=settings.ALGORITHM)
    invite_link = f"/api/invites/{invite_token}"

    # 5. Create chat message from current_user
    message_text = f'{current_user.first_name} предлагает пойти в "{offer.title}". Если согласен — перейди по ссылке: {invite_link}'
    msg = Message(
        author_id=current_user.id,
        contact_initiator_id=contact_initiator_id,
        contact_responder_id=contact_responder_id,
        text=message_text,
        is_read=False,
        created_at=now,
    )
    db.add(msg)
    db.commit()

    message_id = uuid.uuid4()
    return invite_link, message_id


def get_invite_preview(
    db: Session,
    token: str,
) -> InvitePreviewResponse:
    try:
        payload = jwt.decode(
            token,
            settings.SECRET_KEY,
            algorithms=[settings.ALGORITHM],
        )
    except JWTError:
        return InvitePreviewResponse(
            offer_title="",
            partner_name="",
            initiator_name="",
            is_valid=False,
        )

    if payload.get("type") != "invite":
        return InvitePreviewResponse(
            offer_title="",
            partner_name="",
            initiator_name="",
            is_valid=False,
        )

    offer_id_str = payload.get("offer_id")
    sender_id_str = payload.get("sender_id")
    initiator_id_str = payload.get("contact_initiator_id")
    responder_id_str = payload.get("contact_responder_id")

    if not all([offer_id_str, sender_id_str, initiator_id_str, responder_id_str]):
        return InvitePreviewResponse(
            offer_title="",
            partner_name="",
            initiator_name="",
            is_valid=False,
        )

    offer = db.query(Offer).filter(Offer.id == UUID(offer_id_str)).first()
    if not offer:
        return InvitePreviewResponse(
            offer_title="",
            partner_name="",
            initiator_name="",
            is_valid=False,
        )

    sender = db.query(User).filter(User.id == UUID(sender_id_str)).first()
    initiator_name = sender.first_name if sender else ""

    is_valid = True
    if offer.valid_until < date.today():
        is_valid = False

    # Check if promocode has already been issued
    existing_promo = (
        db.query(Promocode)
        .filter(
            Promocode.offer_id == offer.id,
            Promocode.contact_initiator_id == UUID(initiator_id_str),
            Promocode.contact_responder_id == UUID(responder_id_str),
        )
        .first()
    )
    if existing_promo:
        is_valid = False

    return InvitePreviewResponse(
        offer_title=offer.title,
        partner_name=offer.partner.name if offer.partner else "",
        initiator_name=initiator_name,
        is_valid=is_valid,
    )


def accept_invite(
    db: Session,
    current_user: User,
    token: str,
) -> Promocode:
    # 1. Decode token
    try:
        payload = jwt.decode(
            token,
            settings.SECRET_KEY,
            algorithms=[settings.ALGORITHM],
        )
    except JWTError:
        raise AppException(
            status_code=400,
            error_name="INVITE_EXPIRED",
            message="Invite link has expired or is invalid",
        )

    if payload.get("type") != "invite":
        raise AppException(
            status_code=400,
            error_name="INVITE_EXPIRED",
            message="Invalid invite token type",
        )

    receiver_id_str = payload.get("receiver_id")
    sender_id_str = payload.get("sender_id")
    offer_id_str = payload.get("offer_id")
    initiator_id_str = payload.get("contact_initiator_id")
    responder_id_str = payload.get("contact_responder_id")

    # 2. Check that current user is the intended receiver
    if str(current_user.id) != receiver_id_str:
        raise AppException(
            status_code=403,
            error_name="WRONG_RESPONDER",
            message="Only the invited user can accept this invitation",
        )

    sender_id = UUID(sender_id_str)
    offer_id = UUID(offer_id_str)
    contact_initiator_id = UUID(initiator_id_str)
    contact_responder_id = UUID(responder_id_str)

    # 3. Check offer is active
    offer = db.query(Offer).filter(Offer.id == offer_id).first()
    if not offer or offer.valid_until < date.today():
        raise AppException(
            status_code=400,
            error_name="INVITE_EXPIRED",
            message="Offer is no longer active",
        )

    # 4. Check contact is MATCHED
    contact = (
        db.query(Contact)
        .filter(
            Contact.initiator_id == contact_initiator_id,
            Contact.responder_id == contact_responder_id,
        )
        .first()
    )
    if not contact or contact.status != ContactStatus.MATCHED:
        raise AppException(
            status_code=400,
            error_name="Bad Request",
            message="Contact is not active or matched",
        )

    # 5. Check if promocode already issued
    existing_promo = (
        db.query(Promocode)
        .filter(
            Promocode.offer_id == offer.id,
            Promocode.contact_initiator_id == contact_initiator_id,
            Promocode.contact_responder_id == contact_responder_id,
        )
        .first()
    )
    if existing_promo:
        raise AppException(
            status_code=409,
            error_name="INVITE_ALREADY_ACCEPTED",
            message="Promocode has already been issued for this invite",
        )

    # 6. Generate promo code (8 uppercase alphanumeric characters)
    raw_code = secrets.token_hex(4).upper()
    half_len = len(raw_code) // 2
    half1 = raw_code[:half_len]
    half2 = raw_code[half_len:]

    # 7. Create Promocode record
    promocode = Promocode(
        id=uuid.uuid4(),
        offer_id=offer.id,
        contact_initiator_id=contact_initiator_id,
        contact_responder_id=contact_responder_id,
        code=raw_code,
        status=PromocodeStatus.ISSUED,
    )
    db.add(promocode)

    # 8. Send 2 messages in chat
    now_1 = datetime.now(timezone.utc)
    msg1 = Message(
        author_id=sender_id,
        contact_initiator_id=contact_initiator_id,
        contact_responder_id=contact_responder_id,
        text=f"Вот моя половина промокода - {half1}",
        is_read=False,
        created_at=now_1,
    )
    now_2 = now_1 + timedelta(milliseconds=10)
    msg2 = Message(
        author_id=current_user.id,
        contact_initiator_id=contact_initiator_id,
        contact_responder_id=contact_responder_id,
        text=f"Вот моя половина промокода - {half2}",
        is_read=False,
        created_at=now_2,
    )
    db.add(msg1)
    db.add(msg2)

    db.commit()
    db.refresh(promocode)
    return promocode
