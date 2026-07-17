"""Client officiel de l'API Yowyob Loyalty (stdlib uniquement)."""

import hashlib
import hmac
import json
import time
import urllib.error
import urllib.request
from datetime import datetime, timezone
from typing import Any, Dict, Mapping, Optional, Union

from .errors import ApiError, AuthenticationError, LoyaltyError, SignatureVerificationError

DEFAULT_TIMEOUT_SECONDS = 10
DEFAULT_SIGNATURE_TOLERANCE_SECONDS = 300


class LoyaltyClient:
    """Client de la plateforme de fidélité Yowyob Loyalty.

    Exemple::

        loyalty = LoyaltyClient(
            public_key=os.environ["LOYALTY_PUBLIC_KEY"],    # pk_live_… (exposable)
            private_key=os.environ["LOYALTY_PRIVATE_KEY"],  # sk_live_… (SECRÈTE)
            base_url="https://loyalty.yowyob.com",
            webhook_secret=os.environ.get("LOYALTY_WEBHOOK_SECRET"),  # whsec_…
        )
        result = loyalty.track_event("purchase.completed", member_id, payload={"amount": 4990})
    """

    def __init__(
        self,
        public_key: str,
        private_key: str,
        base_url: str = "https://loyalty.yowyob.com",
        webhook_secret: Optional[str] = None,
        timeout: int = DEFAULT_TIMEOUT_SECONDS,
    ):
        self.public_key = public_key
        self._private_key = private_key
        self.base_url = base_url.rstrip("/")
        self._webhook_secret = webhook_secret
        self.timeout = timeout

    # ------------------------------------------------------------------ API

    def track_event(
        self,
        event_type: str,
        member_id: str,
        occurred_at: Optional[str] = None,
        payload: Optional[Dict[str, Any]] = None,
        idempotency_key: Optional[str] = None,
    ) -> Dict[str, Any]:
        """Envoie un événement métier (achat, trajet, inscription…) au moteur de fidélité.

        :param event_type: type d'événement, ex. ``"purchase.completed"``
        :param member_id: identifiant du membre dans votre système (UUID)
        :param occurred_at: date ISO-8601 (défaut : maintenant, UTC)
        :param payload: données métier lues par les règles (ex. ``{"amount": 4990}``)
        :param idempotency_key: le même événement renvoyé deux fois avec la même clé
            n'est traité qu'une seule fois
        :returns: dict avec ``eventId``, ``effectsApplied``, ``notifications``, ``processedAt``
        """
        if occurred_at is None:
            occurred_at = datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
        headers = {}
        if idempotency_key:
            headers["Idempotency-Key"] = idempotency_key
        return self._request(
            "POST",
            f"/api/v1/apps/{self.public_key}/events",
            body={
                "eventType": event_type,
                "memberId": member_id,
                "occurredAt": occurred_at,
                "payload": payload or {},
            },
            headers=headers,
        )

    def get_member_points(self, member_id: str) -> Dict[str, Any]:
        """Solde de points et palier courant d'un membre."""
        return self._request("GET", f"/api/v1/members/{member_id}/points")

    def get_member_tier(self, member_id: str) -> Dict[str, Any]:
        """Palier de fidélité courant d'un membre."""
        return self._request("GET", f"/api/v1/members/{member_id}/tier")

    def get_wallet(self, member_id: str) -> Dict[str, Any]:
        """Portefeuille (wallet) d'un membre : solde monétaire et politique associée."""
        return self._request("GET", f"/api/v1/members/{member_id}/wallet")

    # ------------------------------------------------------------- Webhooks

    def check_callback_integrity(
        self,
        headers: Mapping[str, str],
        raw_body: Union[str, bytes],
        tolerance_seconds: int = DEFAULT_SIGNATURE_TOLERANCE_SECONDS,
    ) -> Dict[str, Any]:
        """Vérifie l'authenticité d'un callback webhook reçu de la plateforme Loyalty.

        La plateforme signe chaque callback avec le SECRET WEBHOOK (``whsec_…``, différent
        de la clé privée) : ``signature = "sha256=" + hex(HMAC-SHA256(secret, timestamp + "." + corps brut))``.

        :param headers: en-têtes HTTP reçus (insensible à la casse) — doit contenir
            ``X-Webhook-Signature`` et ``X-Webhook-Timestamp``
        :param raw_body: corps BRUT de la requête (avant tout ``json.loads``)
        :param tolerance_seconds: tolérance d'horodatage anti-rejeu (défaut 300 s)
        :returns: le payload décodé, une fois la signature validée
        :raises SignatureVerificationError: si la requête est forgée, altérée ou rejouée
        """
        if self._webhook_secret is None:
            raise LoyaltyError(
                "webhook_secret non configuré : passez-le au constructeur de LoyaltyClient."
            )

        normalized = {name.lower(): value for name, value in headers.items()}
        signature = normalized.get("x-webhook-signature")
        timestamp = normalized.get("x-webhook-timestamp")
        if not signature or not timestamp:
            raise SignatureVerificationError(
                "En-têtes X-Webhook-Signature ou X-Webhook-Timestamp absents."
            )
        try:
            timestamp_value = int(timestamp)
        except ValueError:
            raise SignatureVerificationError("X-Webhook-Timestamp illisible.")
        if abs(time.time() - timestamp_value) > tolerance_seconds:
            raise SignatureVerificationError("Horodatage du callback hors tolérance (rejeu possible).")

        body = raw_body.decode("utf-8") if isinstance(raw_body, bytes) else raw_body
        expected = "sha256=" + hmac.new(
            self._webhook_secret.encode("utf-8"),
            f"{timestamp}.{body}".encode("utf-8"),
            hashlib.sha256,
        ).hexdigest()
        if not hmac.compare_digest(expected, signature):
            raise SignatureVerificationError("Signature du callback invalide.")

        try:
            return json.loads(body)
        except json.JSONDecodeError:
            raise SignatureVerificationError("Corps du callback illisible (JSON attendu).")

    # -------------------------------------------------------------- interne

    def _request(
        self,
        method: str,
        path: str,
        body: Optional[Dict[str, Any]] = None,
        headers: Optional[Dict[str, str]] = None,
    ) -> Dict[str, Any]:
        all_headers = {
            "Accept": "application/json",
            "X-Api-Key": self._private_key,
        }
        if headers:
            all_headers.update(headers)

        data = None
        if body is not None:
            data = json.dumps(body).encode("utf-8")
            all_headers["Content-Type"] = "application/json"

        request = urllib.request.Request(
            self.base_url + path, data=data, headers=all_headers, method=method
        )
        try:
            with urllib.request.urlopen(request, timeout=self.timeout) as response:
                return self._decode(response.read()) or {}
        except urllib.error.HTTPError as error:
            decoded = self._decode(error.read())
            if error.code in (401, 403):
                raise AuthenticationError(
                    f"Clé API invalide ou révoquée (HTTP {error.code}).", error.code, decoded
                ) from None
            message = None
            if isinstance(decoded, dict):
                message = decoded.get("detail") or decoded.get("title")
            raise ApiError(
                message or f"Erreur API Loyalty (HTTP {error.code}).", error.code, decoded
            ) from None
        except urllib.error.URLError as error:
            raise ApiError(f"Erreur réseau vers l'API Loyalty : {error.reason}", 0) from None

    @staticmethod
    def _decode(raw: bytes) -> Optional[Dict[str, Any]]:
        try:
            decoded = json.loads(raw.decode("utf-8"))
            return decoded if isinstance(decoded, dict) else None
        except (json.JSONDecodeError, UnicodeDecodeError):
            return None
