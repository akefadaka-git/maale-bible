from __future__ import annotations
import os
from functools import lru_cache
from typing import Iterator
from fastapi import HTTPException
from sqlalchemy import create_engine
from sqlalchemy.engine import Engine
from sqlalchemy.orm import Session
from maale_bible_api.auth_runtime import AuthRuntime, load_auth_runtime

def _normalized_database_url(raw: str) -> str:
    if raw.startswith("postgres://"): return "postgresql+psycopg://" + raw[len("postgres://"):]
    if raw.startswith("postgresql://"): return "postgresql+psycopg://" + raw[len("postgresql://"):]
    return raw

@lru_cache(maxsize=4)
def _engine_for_url(database_url: str) -> Engine:
    return create_engine(_normalized_database_url(database_url), pool_pre_ping=True, pool_recycle=300)

def get_db_session() -> Iterator[Session]:
    raw_url = os.environ.get("DATABASE_URL")
    if not raw_url:
        raise HTTPException(503, detail="Database service is not configured.", headers={"Cache-Control":"no-store"})
    try: engine = _engine_for_url(raw_url)
    except Exception as exc:
        raise HTTPException(503, detail="Database service is unavailable.", headers={"Cache-Control":"no-store"}) from exc
    with Session(engine, expire_on_commit=False) as session:
        yield session

def get_auth_runtime() -> AuthRuntime:
    return load_auth_runtime()
