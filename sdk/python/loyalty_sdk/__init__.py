"""SDK Python officiel de la plateforme de fidélité Yowyob Loyalty."""

from .client import LoyaltyClient
from .errors import (
    ApiError,
    AuthenticationError,
    LoyaltyError,
    SignatureVerificationError,
)

__all__ = [
    "LoyaltyClient",
    "LoyaltyError",
    "ApiError",
    "AuthenticationError",
    "SignatureVerificationError",
]
