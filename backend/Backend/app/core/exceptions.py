from typing import Any, Optional
from fastapi import HTTPException, Request, status
from fastapi.responses import JSONResponse


class AppException(HTTPException):
    def __init__(
        self,
        status_code: int,
        error_name: str,
        message: str,
        headers: Optional[dict[str, Any]] = None,
    ):
        super().__init__(status_code=status_code, detail=message, headers=headers)
        self.error_name = error_name
        self.message = message
        self.code = status_code


async def app_exception_handler(request: Request, exc: AppException):
    return JSONResponse(
        status_code=exc.status_code,
        content={
            "error": exc.error_name,
            "message": exc.message,
            "code": exc.status_code,
        },
    )
