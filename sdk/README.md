# SDK officiels Yowyob Loyalty

Bibliothèques clientes pour intégrer la plateforme de fidélité **Yowyob Loyalty** dans un projet tiers, sur le modèle « espace marchand » : une **Application** créée dans le portail fournit une **clé publique** (`pk_…`, identifiant exposable), une **clé privée** (`sk_…`, authentifie les appels) et un **secret webhook** (`whsec_…`, signe les callbacks).

| Langage | Dossier | Installation | Cible |
|---|---|---|---|
| PHP ≥ 8.0 | [`php/`](php/) | Composer | Laravel, Symfony, WordPress |
| JavaScript / TypeScript (Node ≥ 18) | [`javascript/`](javascript/) | npm | Express, Next.js, NestJS |
| Python ≥ 3.9 | [`python/`](python/) | pip | Django, Flask, FastAPI |
| Java ≥ 17 | [`java/`](java/) | Maven | Spring Boot, Jakarta EE |

## Surface commune

Tous les SDK exposent la même API :

| Méthode | Rôle | Endpoint |
|---|---|---|
| `trackEvent(eventType, memberId, occurredAt?, payload?, idempotencyKey?)` | Envoyer un événement métier | `POST /api/v1/apps/{publicKey}/events` |
| `getMemberPoints(memberId)` | Solde de points | `GET /api/v1/members/{id}/points` |
| `getMemberTier(memberId)` | Palier de fidélité | `GET /api/v1/members/{id}/tier` |
| `getWallet(memberId)` | Portefeuille | `GET /api/v1/members/{id}/wallet` |
| `checkCallbackIntegrity(headers, rawBody, tolerance?)` | Vérifier un callback signé | — (local) |

Et les mêmes erreurs typées : `AuthenticationError` (401/403), `ApiError` (autre non-2xx), `SignatureVerificationError` (callback forgé/rejoué).

## Règles de sécurité communes

- La **clé privée** et le **secret webhook** ne vont ni dans Git, ni côté navigateur/mobile — variables d'environnement côté serveur uniquement.
- La **clé publique** peut être exposée sans risque (elle identifie l'application, ne donne aucun accès).
- **Toujours** vérifier la signature d'un callback avant de le traiter (`checkCallbackIntegrity`), avec le corps **brut** de la requête.
- Utiliser des **clés d'idempotence** (`Idempotency-Key`) pour pouvoir rejouer un envoi sans double crédit.
- Clés `TEST` pendant l'intégration, clés `LIVE` en production.

Guide complet : page **Documentation** du portail d'administration.
