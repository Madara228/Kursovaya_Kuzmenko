from datetime import datetime
from sqlalchemy import (
    Column,
    Text,
    Boolean,
    DateTime,
    CheckConstraint,
    ForeignKeyConstraint,
    Index,
)
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship, foreign
from sqlalchemy.sql import func

from app.database import Base


class Message(Base):
    __tablename__ = "messages"

    author_id = Column(
        UUID(as_uuid=True),
        primary_key=True,
        nullable=False,
    )
    created_at = Column(
        DateTime(timezone=True),
        primary_key=True,
        nullable=False,
        server_default=func.now(),
    )
    contact_initiator_id = Column(
        UUID(as_uuid=True),
        nullable=False,
    )
    contact_responder_id = Column(
        UUID(as_uuid=True),
        nullable=False,
    )
    text = Column(Text, nullable=False)
    is_read = Column(Boolean, nullable=False, default=False)

    # Relationships
    author = relationship(
        "User",
        primaryjoin="foreign(Message.author_id) == User.id",
    )
    contact = relationship(
        "Contact",
        back_populates="messages",
    )

    __table_args__ = (
        ForeignKeyConstraint(
            ["contact_initiator_id", "contact_responder_id"],
            ["contacts.initiator_id", "contacts.responder_id"],
            name="fk_messages_contact",
            ondelete="CASCADE",
        ),
        CheckConstraint(
            "author_id = contact_initiator_id OR author_id = contact_responder_id",
            name="chk_author_is_participant",
        ),
        Index("idx_messages_contact", "contact_initiator_id", "contact_responder_id"),
    )

    def __repr__(self):
        return f"<Message(author_id={self.author_id}, created_at={self.created_at}, is_read={self.is_read})>"
