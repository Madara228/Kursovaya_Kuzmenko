"""
SQLAlchemy database models package.
Imports all models to ensure metadata registration.
"""
from app.models.user import GenderEnum, EsiaAccount, User, Photo
from app.models.contact import ContactStatus, Contact
from app.models.message import Message
from app.models.partner import Partner, Offer
from app.models.promocode import PromocodeStatus, Promocode

__all__ = [
    "GenderEnum",
    "EsiaAccount",
    "User",
    "Photo",
    "ContactStatus",
    "Contact",
    "Message",
    "Partner",
    "Offer",
    "PromocodeStatus",
    "Promocode",
]
