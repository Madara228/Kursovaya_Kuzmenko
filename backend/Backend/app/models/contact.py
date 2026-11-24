import enum
from datetime import datetime
from sqlalchemy import (
    Column,
    DateTime,
    ForeignKey,
    CheckConstraint,
    ForeignKeyConstraint,
    Index,
)
from sqlalchemy.dialects.postgresql import UUID, ENUM
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func

from app.database import Base


class ContactStatus(str, enum.Enum):
    PENDING = "PENDING"
    MATCHED = "MATCHED"
    REJECTED = "REJECTED"


# PostgreSQL ENUM type for contact_status
contact_status_enum = ENUM(
    ContactStatus,
    name="contact_status",
    create_type=False,
    values_callable=lambda obj: [e.value for e in obj],
)


class Contact(Base):
    __tablename__ = "contacts"

    initiator_id = Column(
        UUID(as_uuid=True),
        ForeignKey("users.id", ondelete="CASCADE"),
        primary_key=True,
        nullable=False,
    )
    responder_id = Column(
        UUID(as_uuid=True),
        ForeignKey("users.id", ondelete="CASCADE"),
        primary_key=True,
        nullable=False,
    )
    status = Column(
        contact_status_enum,
        nullable=False,
        default=ContactStatus.PENDING,
    )
    created_at = Column(
        DateTime(timezone=True),
        nullable=False,
        server_default=func.now(),
    )

    # Relationships
    initiator = relationship(
        "User",
        foreign_keys=[initiator_id],
        back_populates="initiated_contacts",
    )
    responder = relationship(
        "User",
        foreign_keys=[responder_id],
        back_populates="received_contacts",
    )
    messages = relationship(
        "Message",
        back_populates="contact",
        cascade="all, delete-orphan",
    )
    promocodes = relationship(
        "Promocode",
        back_populates="contact",
        cascade="all, delete-orphan",
    )

    __table_args__ = (
        CheckConstraint("initiator_id <> responder_id", name="chk_no_self_like"),
        Index("idx_contacts_initiator", "initiator_id"),
        Index("idx_contacts_responder", "responder_id"),
    )

    def __repr__(self):
        return f"<Contact({self.initiator_id} -> {self.responder_id}: status={self.status})>"
