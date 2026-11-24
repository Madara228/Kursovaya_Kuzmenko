import enum
import uuid
from datetime import datetime, date
from sqlalchemy import (
    Column,
    String,
    Boolean,
    Date,
    DateTime,
    Text,
    ForeignKey,
    CheckConstraint,
    Index,
)
from sqlalchemy.dialects.postgresql import UUID, ENUM
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func

from app.database import Base


class GenderEnum(str, enum.Enum):
    M = "M"
    F = "F"


# Register PostgreSQL ENUM type with existing type in DB
gender_enum = ENUM(
    GenderEnum,
    name="gender_enum",
    create_type=False,
    values_callable=lambda obj: [e.value for e in obj],
)


class EsiaAccount(Base):
    __tablename__ = "esia_accounts"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    login = Column(String, unique=True, nullable=False, index=True)
    password_hash = Column(String, nullable=False)
    first_name = Column(String, nullable=False)
    last_name = Column(String, nullable=False)
    birth_date = Column(Date, nullable=False)
    gender = Column(gender_enum, nullable=False)

    # Relationship to user
    user = relationship("User", back_populates="esia_account", uselist=False)

    def __repr__(self):
        return f"<EsiaAccount(id={self.id}, login='{self.login}')>"


class User(Base):
    __tablename__ = "users"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    esia_id = Column(
        UUID(as_uuid=True),
        ForeignKey("esia_accounts.id", ondelete="RESTRICT"),
        unique=True,
        nullable=False,
        index=True,
    )
    first_name = Column(String, nullable=False)
    last_name = Column(String, nullable=False)
    birth_date = Column(Date, nullable=False)
    gender = Column(gender_enum, nullable=False)
    city = Column(String, nullable=True, index=True)
    bio = Column(Text, nullable=True)
    is_active = Column(Boolean, nullable=False, default=True)
    created_at = Column(
        DateTime(timezone=True),
        nullable=False,
        server_default=func.now(),
    )

    # Relationships
    esia_account = relationship("EsiaAccount", back_populates="user")
    photos = relationship(
        "Photo",
        back_populates="user",
        cascade="all, delete-orphan",
        order_by="desc(Photo.is_main)",
    )

    # Contacts where user is initiator or responder
    initiated_contacts = relationship(
        "Contact",
        foreign_keys="Contact.initiator_id",
        back_populates="initiator",
        cascade="all, delete-orphan",
    )
    received_contacts = relationship(
        "Contact",
        foreign_keys="Contact.responder_id",
        back_populates="responder",
        cascade="all, delete-orphan",
    )

    __table_args__ = (
        CheckConstraint(
            "birth_date <= CURRENT_DATE - INTERVAL '18 years'",
            name="chk_user_18_plus",
        ),
    )

    def __repr__(self):
        return f"<User(id={self.id}, name='{self.first_name} {self.last_name}', city='{self.city}')>"


class Photo(Base):
    __tablename__ = "photos"

    id = Column(UUID(as_uuid=True), primary_key=True, default=uuid.uuid4)
    user_id = Column(
        UUID(as_uuid=True),
        ForeignKey("users.id", ondelete="CASCADE"),
        nullable=False,
        index=True,
    )
    url = Column(String, nullable=False)
    is_main = Column(Boolean, nullable=False, default=False)

    # Relationship to user
    user = relationship("User", back_populates="photos")

    def __repr__(self):
        return f"<Photo(id={self.id}, user_id={self.user_id}, is_main={self.is_main})>"
