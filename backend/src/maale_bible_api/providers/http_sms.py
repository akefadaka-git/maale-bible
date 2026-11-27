from __future__ import annotations
import json, os
from urllib.error import HTTPError, URLError
from urllib.parse import urlparse
from urllib.request import Request, urlopen

class HttpJsonRegistrationSmsProvider:
    """HTTPS gateway adapter; contract: POST {to, sender_id, message} with bearer auth."""
    def __init__(self, *, endpoint: str, bearer_token: str, sender_id: str) -> None:
        parsed=urlparse(endpoint)
        if parsed.scheme!="https" or not parsed.hostname or parsed.username or parsed.password:
            raise ValueError("SMS_GATEWAY_URL must be an HTTPS URL without embedded credentials.")
        if not bearer_token or not sender_id: raise ValueError("SMS gateway token and sender ID are required.")
        self._endpoint,self._bearer_token,self._sender_id=endpoint,bearer_token,sender_id
    def send_registration_code(self, destination_e164: str, code: str) -> None:
        if not destination_e164.startswith("+") or len(code)!=6 or not code.isascii() or not code.isdigit():
            raise ValueError("Invalid SMS registration delivery input.")
        body=json.dumps({"to":destination_e164,"sender_id":self._sender_id,
            "message":f"Your Maale Bible registration code is {code}. It expires in 5 minutes. Do not share it."}).encode("utf-8")
        request=Request(self._endpoint,data=body,headers={"Authorization":f"Bearer {self._bearer_token}",
            "Content-Type":"application/json","Accept":"application/json"},method="POST")
        try:
            with urlopen(request,timeout=10) as response:
                if not 200 <= response.status < 300: raise RuntimeError("SMS gateway rejected the delivery request.")
                response.read(4096)
        except (HTTPError,URLError,TimeoutError,OSError) as exc:
            raise RuntimeError("SMS gateway delivery failed.") from exc

def sms_provider_from_environment() -> HttpJsonRegistrationSmsProvider | None:
    endpoint=os.environ.get("SMS_GATEWAY_URL")
    token=os.environ.get("SMS_GATEWAY_BEARER_TOKEN")
    sender=os.environ.get("SMS_GATEWAY_SENDER_ID")
    configured=[bool(endpoint),bool(token),bool(sender)]
    if not any(configured): return None
    if not all(configured): raise RuntimeError("SMS gateway configuration is incomplete.")
    return HttpJsonRegistrationSmsProvider(endpoint=endpoint or "",bearer_token=token or "",sender_id=sender or "")
