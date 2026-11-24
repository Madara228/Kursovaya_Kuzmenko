import enum
import uuid
from datetime import datetime
from sqlalchemy import (
    Column,
    String,
    DateTime,
    ForeignKey,
    ForeignKeyConstraint,
)
from sqlalchemy.dialects.postgresql import UUID, ENUM
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func

from app.database import Base


class PromocodeStatus(str, enum.Enum):
    PENDING = "PENDING"
    ISSUED = "ISSUED"
    ACTIVATED = "ACTIVATED"
    EXPIRED = "EXPIRED"


# PostgreSQL ENUM type for promocode_status
promocode_status_enum = ENUM(
    PromocodeStatus,
    name="promocode_status",
    create_type=False,
    values_callable=lambda obj: [e.value for e in obj],
)


class Promocode(Base):
    __tablename__ = "promocodes"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    offer_id = Column(
        UUID(as_uuid=True),
        ForeignKey("offers.id", ondelete="CASCADE"),
        nullable=False,
    )
    contact_initiator_id = Column(
        UUID(as_uuid=True),
        nullable=False,
    )
    contact_responder_id = Column(
        UUID(as_uuid=True),
        nullable=False,
    )
    code = Column(String, unique=True, nullable=False)
    status = Column(
        promocode_status_enum,
        nullable=False,
        default=PromocodeStatus.PENDING,
    )
    activated_by_id = Column(
        UUID(as_uuid=True),
        ForeignKey("users.id", ondelete="SET NULL"),
        nullable=True,
    )
    created_at = Column(
        DateTime(timezone=True),
        nullable=False,
        server_default=func.now(),
    )

    # Relationships
    offer = relationship("Offer", back_populates="promocodes")
    contact = relationship("Contact", back_populates="promocodes")
    activated_by = relationship("User", foreign_keys=[activated_by_id])

    __table_args__ = (
        ForeignKeyConstraint(
            ["contact_initiator_id", "contact_responder_id"],
            ["contacts.initiator_id", "contacts.responder_id"],
            name="fk_promo_contact",
            ondelete="CASCADE",
        ),
    )

    def __repr__(self):
        return f"<Promocode(id={self.id}, code='{self.code}', status={self.status})>"
