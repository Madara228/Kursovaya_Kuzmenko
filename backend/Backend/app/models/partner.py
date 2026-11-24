import uuid
from datetime import date
from sqlalchemy import (
    Column,
    String,
    Date,
    ForeignKey,
    Index,
)
from sqlalchemy.dialects.postgresql import UUID
from sqlalchemy.orm import relationship

from app.database import Base


class Partner(Base):
    __tablename__ = "partners"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    login = Column(String, unique=True, nullable=False, index=True)
    password_hash = Column(String, nullable=False)
    contact_person = Column(String, nullable=False)
    phone = Column(String, nullable=False)
    name = Column(String, nullable=False)
    city = Column(String, nullable=False, index=True)
    address = Column(String, nullable=False)
    contact_info = Column(String, nullable=True)

    # Relationship to offers
    offers = relationship(
        "Offer",
        back_populates="partner",
        cascade="all, delete-orphan",
    )

    def __repr__(self):
        return f"<Partner(id={self.id}, name='{self.name}', city='{self.city}')>"


class Offer(Base):
    __tablename__ = "offers"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    partner_id = Column(
        UUID(as_uuid=True),
        ForeignKey("partners.id", ondelete="CASCADE"),
        nullable=False,
        index=True,
    )
    title = Column(String, nullable=False)
    discount_info = Column(String, nullable=False)
    valid_until = Column(Date, nullable=False, index=True)
    photo_url = Column(String, nullable=False)

    # Relationships
    partner = relationship("Partner", back_populates="offers")
    promocodes = relationship(
        "Promocode",
        back_populates="offer",
        cascade="all, delete-orphan",
    )

    def __repr__(self):
        return f"<Offer(id={self.id}, title='{self.title}', valid_until={self.valid_until})>"
