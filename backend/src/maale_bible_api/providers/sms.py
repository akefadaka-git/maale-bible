from typing import Protocol


class RegistrationSmsProvider(Protocol):
    """Server-side port for delivering a registration OTP."""

    def send_registration_code(self, destination_e164: str, code: str) -> None:
        """Deliver a code without persisting or logging its plaintext value."""
        ...
