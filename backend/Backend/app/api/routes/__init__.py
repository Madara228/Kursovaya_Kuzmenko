from fastapi import APIRouter
from app.api.routes.auth import router as auth_router
from app.api.routes.users import router as users_router
from app.api.routes.photos import router as photos_router
from app.api.routes.contacts import router as contacts_router

api_router = APIRouter(prefix="/api")
api_router.include_router(auth_router)
api_router.include_router(users_router)
api_router.include_router(photos_router)
api_router.include_router(contacts_router)

__all__ = ["api_router"]
