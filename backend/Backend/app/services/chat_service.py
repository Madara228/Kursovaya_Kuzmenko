import math
from datetime import datetime, timezone
from typing import List, Tuple, Optional
from uuid import UUID
from sqlalchemy.orm import Session
from sqlalchemy import or_, and_, desc, func

from app.models.user import User
from app.models.contact import Contact, ContactStatus
from app.models.message import Message
from app.schemas.chat import (
    MatchItem,
    MatchUser,
    ChatOtherUser,
    ChatListItem,
    MessageItem,
)
from app.schemas.user import PhotoResponse
from app.core.exceptions import AppException



def get_user_matches(
    db: Session,
    user_id: UUID,
    page: int = 1,
    limit: int = 20,
) -> Tuple[List[MatchItem], int, int]:
    if page < 1:
        page = 1
    if limit < 1 or limit > 100:
        limit = 20

    query = (
        db.query(Contact)
        .filter(
            Contact.initiator_id == user_id,
            Contact.status == ContactStatus.MATCHED,
        )
        .order_by(desc(Contact.created_at))
    )

    total_items = query.count()
    total_pages = math.ceil(total_items / limit) if total_items > 0 else 0
    offset = (page - 1) * limit
    contacts = query.offset(offset).limit(limit).all()

    items = []
    for c in contacts:
        companion = c.responder
        if companion and companion.is_active:
            photos = [PhotoResponse.model_validate(p) for p in companion.photos]
            match_user = MatchUser(
                id=companion.id,
                first_name=companion.first_name,
                photos=photos,
            )
            items.append(MatchItem(id=companion.id, user=match_user))

    return items, total_items, total_pages




def get_chats_list(
    db: Session,
    user_id: UUID,
    page: int = 1,
    limit: int = 20,
) -> Tuple[List[ChatListItem], int, int]:
    if page < 1:
        page = 1
    if limit < 1 or limit > 100:
        limit = 20

    query = (
        db.query(Contact)
        .filter(
            Contact.initiator_id == user_id,
            Contact.status == ContactStatus.MATCHED,
        )
    )

    total_items = query.count()
    total_pages = math.ceil(total_items / limit) if total_items > 0 else 0
    offset = (page - 1) * limit
    contacts = query.offset(offset).limit(limit).all()

    chat_items = []
    for c in contacts:
        init_id = c.initiator_id
        resp_id = c.responder_id
        companion = c.responder

        if not companion or not companion.is_active:
            continue

        main_photo_url = None
        for p in companion.photos:
            if p.is_main:
                main_photo_url = p.url
                break
        if not main_photo_url and companion.photos:
            main_photo_url = companion.photos[0].url

        other_user = ChatOtherUser(
            id=companion.id,
            first_name=companion.first_name,
            main_photo=main_photo_url,
        )

        last_msg = (
            db.query(Message)
            .filter(
                or_(
                    and_(
                        Message.contact_initiator_id == init_id,
                        Message.contact_responder_id == resp_id,
                    ),
                    and_(
                        Message.contact_initiator_id == resp_id,
                        Message.contact_responder_id == init_id,
                    ),
                )
            )
            .order_by(desc(Message.created_at))
            .first()
        )

        unread_count = (
            db.query(Message)
            .filter(
                or_(
                    and_(
                        Message.contact_initiator_id == init_id,
                        Message.contact_responder_id == resp_id,
                    ),
                    and_(
                        Message.contact_initiator_id == resp_id,
                        Message.contact_responder_id == init_id,
                    ),
                ),
                Message.author_id != user_id,
                Message.is_read == False,
            )
            .count()
        )


        last_msg_text = last_msg.text if last_msg else None
        last_msg_time = last_msg.created_at if last_msg else None

        chat_items.append(
            ChatListItem(
                contact_initiator_id=init_id,
                contact_responder_id=resp_id,
                other_user=other_user,
                last_message=last_msg_text,
                last_message_time=last_msg_time,
                unread_count=unread_count,
            )
        )

    chat_items.sort(
        key=lambda item: item.last_message_time if item.last_message_time else datetime.min.replace(tzinfo=timezone.utc),
        reverse=True,
    )

    return chat_items, total_items, total_pages




def get_chat_messages(
    db: Session,
    user_id: UUID,
    initiator_id: UUID,
    responder_id: UUID,
    page: int = 1,
    limit: int = 20,
) -> Tuple[List[MessageItem], int, int]:
    if user_id not in (initiator_id, responder_id):
        raise AppException(
            status_code=403,
            error_name="Forbidden",
            message="You are not a participant of this chat",
        )

    if page < 1:
        page = 1
    if limit < 1 or limit > 100:
        limit = 20

    query = (
        db.query(Message)
        .filter(
            or_(
                and_(
                    Message.contact_initiator_id == initiator_id,
                    Message.contact_responder_id == responder_id,
                ),
                and_(
                    Message.contact_initiator_id == responder_id,
                    Message.contact_responder_id == initiator_id,
                ),
            )
        )
        .order_by(desc(Message.created_at))
    )

    total_items = query.count()
    total_pages = math.ceil(total_items / limit) if total_items > 0 else 0
    offset = (page - 1) * limit
    messages = query.offset(offset).limit(limit).all()

    items = [MessageItem.model_validate(m) for m in messages]
    return items, total_items, total_pages



def send_chat_message(
    db: Session,
    author_id: UUID,
    initiator_id: UUID,
    responder_id: UUID,
    text: str,
) -> Message:
    if author_id not in (initiator_id, responder_id):
        raise AppException(
            status_code=403,
            error_name="Forbidden",
            message="You are not a participant of this contact",
        )

    contact = (
        db.query(Contact)
        .filter(
            Contact.initiator_id == initiator_id,
            Contact.responder_id == responder_id,
        )
        .first()
    )
    if not contact:
        contact = (
            db.query(Contact)
            .filter(
                Contact.initiator_id == responder_id,
                Contact.responder_id == initiator_id,
            )
            .first()
        )
        if contact:
            initiator_id, responder_id = responder_id, initiator_id

    if not contact:
        raise AppException(
            status_code=404,
            error_name="Not Found",
            message="Contact does not exist",
        )

    if contact.status != ContactStatus.MATCHED:
        raise AppException(
            status_code=403,
            error_name="Forbidden",
            message="Cannot send messages to non-matched contacts",
        )

    now = datetime.now(timezone.utc)
    new_message = Message(
        author_id=author_id,
        created_at=now,
        contact_initiator_id=initiator_id,
        contact_responder_id=responder_id,
        text=text,
        is_read=False,
    )
    db.add(new_message)
    db.commit()
    db.refresh(new_message)
    return new_message




def mark_chat_as_read(
    db: Session,
    user_id: UUID,
    initiator_id: UUID,
    responder_id: UUID,
) -> int:
    if user_id not in (initiator_id, responder_id):
        raise AppException(
            status_code=403,
            error_name="Forbidden",
            message="You are not a participant of this chat",
        )

    updated_count = (
        db.query(Message)
        .filter(
            or_(
                and_(
                    Message.contact_initiator_id == initiator_id,
                    Message.contact_responder_id == responder_id,
                ),
                and_(
                    Message.contact_initiator_id == responder_id,
                    Message.contact_responder_id == initiator_id,
                ),
            ),
            Message.author_id != user_id,
            Message.is_read == False,
        )
        .update({"is_read": True}, synchronize_session=False)
    )
    db.commit()
    return updated_count
