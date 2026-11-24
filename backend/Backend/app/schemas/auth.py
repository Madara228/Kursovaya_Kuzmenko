from typing import Optional
from uuid import UUID
from pydantic import BaseModel, Field


# ESIA Login
class EsiaLoginRequest(BaseModel):
    login: str = Field(..., description="Login in ESIA mock database")
    password: str = Field(..., description="Password in ESIA mock database")


class UserTokenInfo(BaseModel):
    id: UUID
    first_name: str
    last_name: str
    is_new_user: bool


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    expires_in: int
    user: UserTokenInfo


# Partner Login
class PartnerLoginRequest(BaseModel):
    login: str = Field(..., description="Partner login")
    password: str = Field(..., description="Partner password")


class PartnerTokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    expires_in: int
    partner_id: UUID
