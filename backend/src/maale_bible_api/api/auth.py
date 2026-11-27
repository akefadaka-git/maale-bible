from __future__ import annotations
import secrets
from functools import lru_cache
from typing import Annotated
from uuid import UUID
from fastapi import APIRouter, Depends, Header, HTTPException, Request, Response
from pydantic import BaseModel, Field
from sqlalchemy import delete
from sqlalchemy.orm import Session
from maale_bible_api.auth_runtime import AuthRuntime
from maale_bible_api.dependencies import get_auth_runtime, get_db_session
from maale_bible_api.db.models import RegistrationChallenge
from maale_bible_api.providers.sms import RegistrationSmsProvider
from maale_bible_api.services.login import login_with_password
from maale_bible_api.services.registration import RegistrationNotAvailable, create_registration_challenge, normalize_registration_mobile
from maale_bible_api.services.registration_abuse import RateLimitExceeded, consume_registration_rate_limit
from maale_bible_api.services.registration_activation import verify_and_activate_registration
from maale_bible_api.services.sessions import IssuedSession, account_id_for_access_token, revoke_session_for_access_token, rotate_refresh_token

router=APIRouter(prefix="/auth",tags=["authentication"])
class RegistrationRequest(BaseModel):
    username:str=Field(min_length=3,max_length=30)
    mobile_number:str=Field(min_length=8,max_length=32)
    password:str=Field(min_length=12,max_length=1024)
class RegistrationVerificationRequest(BaseModel):
    challenge_id:UUID
    otp_code:str=Field(pattern=r"^[0-9]{6}$")
class LoginRequest(BaseModel):
    identifier:str=Field(min_length=3,max_length=64)
    password:str=Field(min_length=1,max_length=1024)
class RefreshRequest(BaseModel):
    refresh_token:str=Field(min_length=32,max_length=256)

def _no_store(response:Response)->None:
    response.headers["Cache-Control"]="no-store"; response.headers["Pragma"]="no-cache"
def _bearer_token(authorization:str|None)->str:
    if not authorization: raise HTTPException(401,detail="Authentication required.")
    scheme,separator,token=authorization.partition(" ")
    if not separator or scheme.casefold()!="bearer" or not token.strip():
        raise HTTPException(401,detail="Authentication required.")
    return token.strip()
@lru_cache(maxsize=1)
def _dummy_password_hash()->str:
    from maale_bible_api.security.passwords import hash_password
    return hash_password(secrets.token_urlsafe(48))
def _session_response(issued:IssuedSession,runtime:AuthRuntime)->dict[str,object]:
    return {"access_token":issued.access_token,"refresh_token":issued.refresh_token,"token_type":"Bearer",
        "expires_in":runtime.session_policy.access_ttl_seconds,"access_expires_at":issued.access_expires_at.isoformat(),
        "refresh_expires_at":issued.refresh_expires_at.isoformat()}
def _sms_provider(request:Request)->RegistrationSmsProvider|None:
    return getattr(request.app.state,"sms_provider",None)

@router.post("/register",status_code=202)
def register(payload:RegistrationRequest,request:Request,response:Response,
    session:Annotated[Session,Depends(get_db_session)],runtime:Annotated[AuthRuntime,Depends(get_auth_runtime)])->dict[str,str]:
    _no_store(response)
    provider=_sms_provider(request)
    if provider is None: raise HTTPException(503,detail="Registration delivery is not configured.")
    try:
        mobile=normalize_registration_mobile(payload.mobile_number)
        client_ip=request.client.host if request.client else "unknown"
        consume_registration_rate_limit(session,scope="registration-mobile",subject=mobile,
            secret=runtime.rate_limit_hmac_key,policy=runtime.registration_rate_limit_policy)
        consume_registration_rate_limit(session,scope="registration-client",subject=client_ip,
            secret=runtime.rate_limit_hmac_key,policy=runtime.registration_rate_limit_policy)
        challenge=create_registration_challenge(session,username=payload.username,mobile_number=mobile,
            password=payload.password,otp_hmac_key=runtime.otp_hmac_key)
    except RateLimitExceeded as exc:
        raise HTTPException(429,detail="Registration temporarily unavailable. Try again later.",
            headers={"Retry-After":str(exc.retry_after_seconds),"Cache-Control":"no-store"}) from exc
    except RegistrationNotAvailable as exc:
        raise HTTPException(409,detail="Registration cannot be started.") from exc
    except ValueError as exc:
        raise HTTPException(422,detail="Registration details are invalid.") from exc
    try: provider.send_registration_code(mobile,challenge.otp_code)
    except Exception as exc:
        with session.begin():
            session.execute(delete(RegistrationChallenge).where(RegistrationChallenge.id==challenge.challenge_id))
        raise HTTPException(503,detail="Registration message could not be delivered. Try again later.") from exc
    return {"challenge_id":str(challenge.challenge_id),"expires_at":challenge.expires_at.isoformat()}

@router.post("/register/verify")
def verify_registration(payload:RegistrationVerificationRequest,response:Response,
    session:Annotated[Session,Depends(get_db_session)],runtime:Annotated[AuthRuntime,Depends(get_auth_runtime)])->dict[str,object]:
    _no_store(response)
    activated=verify_and_activate_registration(session,challenge_id=payload.challenge_id,otp_code=payload.otp_code,
        otp_hmac_key=runtime.otp_hmac_key,session_policy=runtime.session_policy)
    if activated is None: raise HTTPException(400,detail="Registration verification failed.")
    return _session_response(activated.session,runtime)

@router.post("/login")
def login(payload:LoginRequest,response:Response,session:Annotated[Session,Depends(get_db_session)],
    runtime:Annotated[AuthRuntime,Depends(get_auth_runtime)])->dict[str,object]:
    _no_store(response)
    issued=login_with_password(session,identifier=payload.identifier,password=payload.password,
        dummy_password_hash=_dummy_password_hash(),policy=runtime.session_policy)
    if issued is None: raise HTTPException(401,detail="Invalid username/mobile or password.")
    return _session_response(issued,runtime)

@router.post("/refresh")
def refresh(payload:RefreshRequest,response:Response,session:Annotated[Session,Depends(get_db_session)],
    runtime:Annotated[AuthRuntime,Depends(get_auth_runtime)])->dict[str,object]:
    _no_store(response)
    issued=rotate_refresh_token(session,refresh_token=payload.refresh_token,policy=runtime.session_policy)
    if issued is None: raise HTTPException(401,detail="Session refresh failed.")
    return _session_response(issued,runtime)

@router.post("/logout",status_code=204)
def logout(response:Response,authorization:Annotated[str|None,Header()]=None,
    session:Session=Depends(get_db_session))->Response:
    _no_store(response)
    revoke_session_for_access_token(session,access_token=_bearer_token(authorization))
    return Response(status_code=204,headers={"Cache-Control":"no-store","Pragma":"no-cache"})

@router.get("/me")
def current_account(response:Response,authorization:Annotated[str|None,Header()]=None,
    session:Session=Depends(get_db_session))->dict[str,str]:
    _no_store(response)
    account_id=account_id_for_access_token(session,access_token=_bearer_token(authorization))
    if account_id is None: raise HTTPException(401,detail="Authentication required.")
    return {"account_id":str(account_id)}
