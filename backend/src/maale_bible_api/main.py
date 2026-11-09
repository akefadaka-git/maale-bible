from fastapi import FastAPI
from fastapi.responses import JSONResponse

app = FastAPI(
    title="Maale Bible API",
    version="0.1.0",
    description=(
        "Backend foundation. Authentication routes are intentionally not "
        "enabled until persistence, OTP delivery and session controls pass review."
    ),
    docs_url=None,
    redoc_url=None,
    openapi_url="/openapi.json",
)


@app.get("/health/live", include_in_schema=False)
async def liveness() -> JSONResponse:
    """Process liveness only; this endpoint does not assert database readiness."""
    return JSONResponse(
        status_code=200,
        content={"status": "alive", "service": "maale-bible-api"},
        headers={"Cache-Control": "no-store"},
    )
