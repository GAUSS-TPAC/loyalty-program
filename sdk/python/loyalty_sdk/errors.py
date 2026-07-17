"""Exceptions du SDK Yowyob Loyalty."""

from typing import Any, Optional


class LoyaltyError(Exception):
    """Exception de base du SDK Yowyob Loyalty."""


class ApiError(LoyaltyError):
    """Erreur renvoyée par l'API Loyalty (statut HTTP non 2xx)."""

    def __init__(self, message: str, status_code: int, body: Optional[Any] = None):
        super().__init__(message)
        self.status_code = status_code
        self.body = body


class AuthenticationError(ApiError):
    """Clé API invalide, révoquée ou absente (HTTP 401/403)."""


class SignatureVerificationError(LoyaltyError):
    """Signature de callback webhook absente, invalide ou expirée.

    Ne traitez JAMAIS un callback qui lève cette exception.
    """
