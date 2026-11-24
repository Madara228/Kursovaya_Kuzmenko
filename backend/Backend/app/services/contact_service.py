import math
from typing import List, Tuple
from uuid import UUID
from sqlalchemy.orm import Session
from sqlalchemy import or_, and_, desc

from app.models.user import User, GenderEnum
from app.models.contact import Contact, ContactStatus
from app.schemas.contact import SwipeAction, SwipeResponse
from app.core.exceptions import AppException


def get_feed(
    db: Session,
    current_user: User,
    page: int = 1,
    limit: int = 20,
) -> Tuple[List[User], int, int]:
    """
    Returns users for feed:
    - Excludes current user.
    - Excludes users that current_user has already initiated a swipe on (like or dislike).
    - Filters by OPPOSITE gender (men see only women, women see only men).
    - Excludes users who are not active.
    Returns (users, total_items, total_pages).
    """
    if page < 1:
        page = 1
    if limit < 1 or limit > 100:
        limit = 20

    # Opposite gender
    target_gender = GenderEnum.F if current_user.gender == GenderEnum.M else GenderEnum.M

    # IDs already swiped by current user (likes or dislikes)
    swiped_user_ids_subquery = (
        db.query(Contact.responder_id)
        .filter(Contact.initiator_id == current_user.id)
        .subquery()
    )

    query = db.query(User).filter(
        User.id != current_user.id,
        User.gender == target_gender,
        User.is_active == True,
        ~User.id.in_(swiped_user_ids_subquery),
    )

    total_items = query.count()
    total_pages = math.ceil(total_items / limit) if total_items > 0 else 0
    offset = (page - 1) * limit

    users = query.order_by(desc(User.created_at)).offset(offset).limit(limit).all()
    return users, total_items, total_pages


def swipe(
    db: Session,
    initiator_id: UUID,
    target_user_id: UUID,
    action: SwipeAction,
) -> SwipeResponse:
    """
    Handles swipe logic according to project rules:
    - No self-swipe allowed.
    - Target user must exist and be active.
    - If contact already exists from initiator to target -> 409 Conflict.
    - If action == 'DISLIKE': creates contact with REJECTED status, is_match = False.
    - If action == 'LIKE':
        - Checks if there is a reverse contact (target -> initiator) with status == PENDING.
        - If yes: both users like each other -> status becomes MATCHED for both, is_match = True.
        - If no reverse contact or reverse is not pending: status is PENDING, is_match = False.
    """
    if initiator_id == target_user_id:
        raise AppException(
            status_code=409,
            error_name="SELF_SWIPE_NOT_ALLOWED",
            message="Self swipe is not allowed",
        )

    # Verify target user exists and active
    target_user = db.query(User).filter(User.id == target_user_id).first()
    if not target_user:
        raise AppException(
            status_code=404,
            error_name="Not Found",
            message="Target user not found",
        )
    if not target_user.is_active:
        raise AppException(
            status_code=403,
            error_name="USER_BLOCKED",
            message="Target user is inactive",
        )

    # Check if contact from current user to target already exists
    existing_contact = (
        db.query(Contact)
        .filter(
            Contact.initiator_id == initiator_id,
            Contact.responder_id == target_user_id,
        )
        .first()
    )
    if existing_contact:
        raise AppException(
            status_code=409,
            error_name="CONTACT_ALREADY_EXISTS",
            message="Contact with this user already exists",
        )

    # Check reverse contact (target -> initiator)
    reverse_contact = (
        db.query(Contact)
        .filter(
            Contact.initiator_id == target_user_id,
            Contact.responder_id == initiator_id,
        )
        .first()
    )

    if action == SwipeAction.DISLIKE:
        # Create contact with REJECTED status
        new_contact = Contact(
            initiator_id=initiator_id,
            responder_id=target_user_id,
            status=ContactStatus.REJECTED,
        )
        db.add(new_contact)
        db.commit()
        return SwipeResponse(status=ContactStatus.REJECTED, is_match=False)

    # Action is LIKE
    if reverse_contact and reverse_contact.status == ContactStatus.PENDING:
        # Mutual match!
        reverse_contact.status = ContactStatus.MATCHED
        new_contact = Contact(
            initiator_id=initiator_id,
            responder_id=target_user_id,
            status=ContactStatus.MATCHED,
        )
        db.add(new_contact)
        db.commit()
        return SwipeResponse(status=ContactStatus.MATCHED, is_match=True)
    else:
        # Pending like
        new_contact = Contact(
            initiator_id=initiator_id,
            responder_id=target_user_id,
            status=ContactStatus.PENDING,
        )
        db.add(new_contact)
        db.commit()
        return SwipeResponse(status=ContactStatus.PENDING, is_match=False)
