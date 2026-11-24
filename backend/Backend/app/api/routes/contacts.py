from fastapi import APIRouter, Depends, Query, status
from sqlalchemy.orm import Session

from app.database import get_db
from app.models.user import User
from app.api.deps import get_current_user
from app.schemas.contact import (
    FeedResponse,
    FeedUserItem,
    PaginationMeta,
    SwipeRequest,
    SwipeResponse,
)
from app.services.contact_service import get_feed, swipe

router = APIRouter(tags=["Feed & Contacts"])


@router.get(
    "/feed",
    response_model=FeedResponse,
    status_code=status.HTTP_200_OK,
    summary="Get user feed",
    description="Returns profiles of users that current user has not swiped on yet, with pagination.",
)
def read_feed(
    page: int = Query(1, ge=1, description="Page number"),
    limit: int = Query(20, ge=1, le=100, description="Items per page"),
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    users, total_items, total_pages = get_feed(db, current_user, page, limit)
    return FeedResponse(
        data=[FeedUserItem.model_validate(u) for u in users],
        meta=PaginationMeta(
            total_items=total_items,
            total_pages=total_pages,
            current_page=page,
            per_page=limit,
        ),
    )


@router.post(
    "/contacts/swipe",
    response_model=SwipeResponse,
    status_code=status.HTTP_200_OK,
    summary="Swipe on a user (Like / Dislike)",
    description="Processes LIKE or DISLIKE action. If mutual like occurs, returns status MATCHED and is_match: true.",
)
def swipe_user(
    data: SwipeRequest,
    current_user: User = Depends(get_current_user),
    db: Session = Depends(get_db),
):
    result = swipe(db, current_user.id, data.target_user_id, data.action)
    return result
