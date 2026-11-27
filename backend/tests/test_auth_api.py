from __future__ import annotations
import base64
import pytest
from fastapi.testclient import TestClient
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool
from maale_bible_api.db.base import Base
from maale_bible_api.dependencies import get_db_session
from maale_bible_api.main import create_app

OTP_KEY=b"test-only-registration-key-material-32-bytes"
RATE_KEY=b"test-only-rate-limit-key-material-32-bytes"
class FakeSmsProvider:
    def __init__(self): self.sent=[]
    def send_registration_code(self,destination_e164:str,code:str)->None: self.sent.append((destination_e164,code))

@pytest.fixture
def client(monkeypatch):
    monkeypatch.setenv("OTP_HMAC_KEY_B64",base64.urlsafe_b64encode(OTP_KEY).decode("ascii"))
    monkeypatch.setenv("RATE_LIMIT_HMAC_KEY_B64",base64.urlsafe_b64encode(RATE_KEY).decode("ascii"))
    monkeypatch.setenv("ACCESS_TOKEN_TTL_SECONDS","900")
    monkeypatch.setenv("REFRESH_TOKEN_TTL_SECONDS","86400")
    monkeypatch.setenv("REGISTRATION_RATE_LIMIT_MAX_REQUESTS","20")
    monkeypatch.setenv("REGISTRATION_RATE_LIMIT_WINDOW_SECONDS","3600")
    monkeypatch.setenv("REGISTRATION_RATE_LIMIT_BLOCK_SECONDS","900")
    engine=create_engine("sqlite+pysqlite://",connect_args={"check_same_thread":False},poolclass=StaticPool)
    Base.metadata.create_all(engine)
    factory=sessionmaker(engine,expire_on_commit=False)
    provider=FakeSmsProvider()
    app=create_app(sms_provider=provider)
    def override_db():
        with factory() as session: yield session
    app.dependency_overrides[get_db_session]=override_db
    with TestClient(app) as test_client: yield test_client,provider
    engine.dispose()

def register_and_verify(test_client,provider):
    response=test_client.post("/auth/register",json={"username":"reader.one","mobile_number":"+251 911-223-344",
        "password":"synthetic-password-2026"})
    assert response.status_code==202
    assert "otp_code" not in response.json()
    assert len(provider.sent)==1
    destination,code=provider.sent[0]
    assert destination=="+251911223344"
    verified=test_client.post("/auth/register/verify",json={"challenge_id":response.json()["challenge_id"],"otp_code":code})
    assert verified.status_code==200
    return verified.json()

def test_registration_delivers_otp_without_exposing_it_and_issues_session(client):
    test_client,provider=client
    tokens=register_and_verify(test_client,provider)
    assert tokens["token_type"]=="Bearer" and tokens["expires_in"]==900
    me=test_client.get("/auth/me",headers={"Authorization":f"Bearer {tokens['access_token']}"})
    assert me.status_code==200 and me.json()["account_id"]

def test_login_accepts_username_or_mobile_and_refresh_replay_revokes_family(client):
    test_client,provider=client
    register_and_verify(test_client,provider)
    login=test_client.post("/auth/login",json={"identifier":"READER.ONE","password":"synthetic-password-2026"})
    assert login.status_code==200
    first=login.json()
    refreshed=test_client.post("/auth/refresh",json={"refresh_token":first["refresh_token"]})
    assert refreshed.status_code==200
    second=refreshed.json()
    assert second["access_token"]!=first["access_token"]
    replay=test_client.post("/auth/refresh",json={"refresh_token":first["refresh_token"]})
    assert replay.status_code==401
    assert test_client.get("/auth/me",headers={"Authorization":f"Bearer {second['access_token']}"}).status_code==401

def test_mobile_login_and_logout_revoke_device_session(client):
    test_client,provider=client
    register_and_verify(test_client,provider)
    login=test_client.post("/auth/login",json={"identifier":"+251 911 223 344","password":"synthetic-password-2026"})
    assert login.status_code==200
    token=login.json()["access_token"]
    assert test_client.post("/auth/logout",headers={"Authorization":f"Bearer {token}"}).status_code==204
    assert test_client.get("/auth/me",headers={"Authorization":f"Bearer {token}"}).status_code==401

def test_unknown_identity_has_generic_failure(client):
    test_client,_=client
    response=test_client.post("/auth/login",json={"identifier":"missing.reader","password":"synthetic-password-2026"})
    assert response.status_code==401
    assert response.json()["detail"]=="Invalid username/mobile or password."

def test_registration_fails_closed_without_sms_provider(client):
    test_client,_=client
    # Replace the configured provider without changing database or runtime dependencies.
    test_client.app.state.sms_provider=None
    response=test_client.post("/auth/register",json={"username":"reader.one","mobile_number":"+251911223344",
        "password":"synthetic-password-2026"})
    assert response.status_code==503
