import os
from contextlib import asynccontextmanager
from fastapi import FastAPI, Depends, status
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from sqlalchemy.orm import Session

from app.config import settings
from app.database import Base, engine, get_db, check_db_connection
from app.core.exceptions import AppException, app_exception_handler
import app.models  # Ensures all models are registered with Base.metadata


@asynccontextmanager
async def lifespan(app: FastAPI):
    # Startup: ensure media directory exists
    os.makedirs(settings.MEDIA_DIR, exist_ok=True)
    # Ensure all tables exist in database
    Base.metadata.create_all(bind=engine)
    yield
    # Shutdown actions (if any)


app = FastAPI(
    title=settings.PROJECT_NAME,
    description="Backend API for RealMatch dating platform with ESIA authentication and partner offers.",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan,
)

# CORS Middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.CORS_ORIGINS,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.add_exception_handler(AppException, app_exception_handler)

# Include API routes
from app.api.routes import api_router
app.include_router(api_router)

# Mount media directory for static files
os.makedirs(settings.MEDIA_DIR, exist_ok=True)
app.mount("/media", StaticFiles(directory=settings.MEDIA_DIR), name="media")


@app.get("/", tags=["Root"])
def root():
    """
    Root endpoint indicating API health and status.
    """
    return {
        "status": "online",
        "project": settings.PROJECT_NAME,
        "environment": settings.ENVIRONMENT,
        "docs_url": "/docs",
        "redoc_url": "/redoc",
        "health_check": "/health",
    }


@app.get("/health", tags=["Health"])
def health_check(db: Session = Depends(get_db)):
    """
    Health check endpoint verifying database connectivity.
    """
    db_ok = check_db_connection()
    if db_ok:
        return {
            "status": "healthy",
            "database": "connected",
        }
    return {
        "status": "unhealthy",
        "database": "disconnected",
    }
