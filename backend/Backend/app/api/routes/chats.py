from uuid import UUID
from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.user import User
from app.api.deps import get_current_user
from app.schemas.chat import (
    MatchesResponse,
    ChatsListResponse,
    MessagesListResponse,
    SendMessageRequest,
    SendMessageResponse,
    MarkReadResponse,
    PaginationMeta,
)
from app.services.chat_service import (
    get_user_matches,
    get_chats_list,
    get_chat_messages,
    send_chat_message,
    mark_chat_as_read,
)

router = APIRouter(tags=["Matches & Chats"])


@router.get(
    "/matches",
    response_model=MatchesResponse,
    status_code=status.HTTP_200_OK,
    summary="Get user's matches",
    description="Returns list of users with mutual sympathy (status = MATCHED).",
)
def read_matches(
    page: int = Query(1, ge=1, description="Page number"),
    limit: int = Query(20, ge=1, le=100, description="Items per page"),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    items, total_items, total_pages = get_user_matches(db, current_user.id, page, limit)
    return MatchesResponse(
        data=items,
        meta=PaginationMeta(
            total_items=total_items,
            total_pages=total_pages,
            current_page=page,
            per_page=limit,
        ),
    )


@router.get(
    "/chats",
    response_model=ChatsListResponse,
    status_code=status.HTTP_200_OK,
    summary="Get chats list",
    description="Returns list of chats with last message and unread count.",
)
def read_chats(
    page: int = Query(1, ge=1, description="Page number"),
    limit: int = Query(20, ge=1, le=100, description="Items per page"),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    chats, total_items, total_pages = get_chats_list(db, current_user.id, page, limit)
    return ChatsListResponse(
        data=chats,
        meta=PaginationMeta(
            total_items=total_items,
            total_pages=total_pages,
            current_page=page,
            per_page=limit,
        ),
    )


@router.get(
    "/chats/{contact_initiator_id}/{contact_responder_id}/messages",
    response_model=MessagesListResponse,
    status_code=status.HTTP_200_OK,
    summary="Get messages in chat",
    description="Returns paginated list of messages in the specified chat.",
)
def read_messages(
    contact_initiator_id: UUID,
    contact_responder_id: UUID,
    page: int = Query(1, ge=1, description="Page number"),
    limit: int = Query(20, ge=1, le=100, description="Items per page"),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
): 
    msgs, total_items, total_pages = get_chat_messages(
        db,
        current_user.id,
        contact_initiator_id,
        contact_responder_id,
        page,
        limit,
    )
    return MessagesListResponse(
        data=msgs,
        meta=PaginationMeta(
            total_items=total_items,
            total_pages=total_pages,
            current_page=page,
            per_page=limit,
        ),
    )


@router.post(
    "/chats/{contact_initiator_id}/{contact_responder_id}/messages",
    response_model=SendMessageResponse,
    status_code=status.HTTP_201_CREATED,
    summary="Send message",
    description="Sends a new message to a matched chat.",
)
def post_message(
    contact_initiator_id: UUID,
    contact_responder_id: UUID,
    data: SendMessageRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
): 
    new_msg = send_chat_message(
        db,
        current_user.id,
        contact_initiator_id,
        contact_responder_id,
        data.text,
    )
    return new_msg


@router.patch(
    "/chats/{contact_initiator_id}/{contact_responder_id}/read",
    response_model=MarkReadResponse,
    status_code=status.HTTP_200_OK,
    summary="Mark messages as readable",
    description="Marks all unread messages from companion as read.",
)
def mark_read(
    contact_initiator_id: UUID,
    contact_responder_id: UUID,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
): 
    updated_count = mark_chat_as_read(
        db,
        current_user.id,
        contact_initiator_id,
        contact_responder_id,
    )
    return MarkReadResponse(status="success", updated_count=updated_count)
