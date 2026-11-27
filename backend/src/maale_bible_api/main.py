from __future__ import annotations
from fastapi import Depends,FastAPI,HTTPException
from fastapi.responses import JSONResponse
from sqlalchemy import text
from sqlalchemy.orm import Session
from maale_bible_api.api.auth import router as auth_router
from maale_bible_api.dependencies import get_auth_runtime,get_db_session
from maale_bible_api.providers.http_sms import sms_provider_from_environment

def create_app(*,sms_provider=...)->FastAPI:
    app=FastAPI(title="Maale Bible API",version="0.2.0",
        description="Offline-first Maale Bible API. Registration requires an explicitly configured HTTPS SMS gateway.",
        docs_url=None,redoc_url=None,openapi_url="/openapi.json")
    app.state.sms_provider=sms_provider_from_environment() if sms_provider is ... else sms_provider
    app.include_router(auth_router)
    @app.get("/health/live",include_in_schema=False)
    async def liveness()->JSONResponse:
        return JSONResponse(status_code=200,content={"status":"alive","service":"maale-bible-api"},
            headers={"Cache-Control":"no-store"})
    @app.get("/health/ready",include_in_schema=False)
    def readiness(session:Session=Depends(get_db_session),_runtime=Depends(get_auth_runtime))->JSONResponse:
        if app.state.sms_provider is None:
            raise HTTPException(503,detail="Registration delivery is not configured.",headers={"Cache-Control":"no-store"})
        try: session.execute(text("SELECT 1"))
        except Exception as exc:
            raise HTTPException(503,detail="Database readiness check failed.",headers={"Cache-Control":"no-store"}) from exc
        return JSONResponse(status_code=200,content={"status":"ready","service":"maale-bible-api"},
            headers={"Cache-Control":"no-store"})
    return app

app=create_app()
