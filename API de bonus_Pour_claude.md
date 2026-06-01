je veux faire la conception d'un programme de fidelisation sous forme de saas confiurable . sceanario possible: une entreprise souhaite recomprenser ses clients les plus regulier, alors avec notre saas, il doit pouvoir configurer ses mecanismes de recompenses ("apres 10 achat de ticket premium , faire gagner un ticket premium" , ou bien "chaque achat dans la plateforme donne 50 points, à 5000 points faire un reduction de X% sur les 2 prochains produits" ) en gros notre solutin doit etre plug and pay telque. chaque plateforme doit pouvoir venir se connecter et paramettrer ses politiques/mecanismes independament des autres.
parrainage, wallet, bonus, et autre ... tous doit etre pris en compte.
je veux qu'on brainstorm. dit moi explique moi les conception qui se font sur des application existante. ensuite reflechissons sur la conception de notre solution
27 avr.
Brainstorm : SaaS de Fidélisation Configurable
Commençons par analyser ce qui existe, puis on construit ta vision.

1. Ce que font les solutions existantes
Stamp Me / Loyverse / Square Loyalty (PME, simples)
Carte de fidélité digitale : X achats = 1 récompense
Logique purement linéaire, pas de règles complexes
Pas d'API pour s'intégrer dans une autre plateforme
❌ Zéro flexibilité de configuration
Yotpo / Smile.io (e-commerce, mid-market)
Système de points + paliers (Bronze, Silver, Gold)
Règles basiques : "1€ dépensé = 10 points"
Parrainage intégré
Limité à l'écosystème Shopify/WooCommerce
❌ Pas multi-tenant, pas configurable par règles métier
Antavo / Loyalty Lion (Enterprise)
Moteur de règles plus avancé
Segmentation clients, gamification
Multi-canaux (online + offline)
✅ Plus proche de ce que tu veux, MAIS très cher, très lourd, pas plug-and-play
❌ Pas pensé pour être un middleware léger
Salesforce Loyalty Management
Extrêmement puissant, moteur de règles configurable
Intégré dans tout l'écosystème SF
❌ Coût prohibitif, lock-in total, sur-ingéniéré
Ce que tous ratent :
Une API-first pensée pour être branchée sur N'IMPORTE quelle plateforme
Un moteur de règles vraiment expressif et configurable par le client lui-même
Une logique multi-tenant propre où chaque tenant a ses propres règles isolées
La combinaison wallet + points + tampons + parrainage dans un seul moteur

2. Les mécanismes fondamentaux à modéliser
Avant l'architecture, listons tous les primitives que ton moteur doit comprendre :
🎯 Mécanismes de gain
Mécanisme
Exemple
Points par achat
50 pts par achat
Points par montant
1pt / 100 FCFA dépensé
Tampon (stamp)
10 achats = 1 récompense
Bonus événementiel
2x points le weekend
Bonus catégorie
+100pts sur produits premium
Parrainage
500pts si filleul fait 1er achat
Bonus inscription
200pts à la création de compte
Streak
3 achats consécutifs = bonus

🎁 Mécanismes de récompense
Récompense
Exemple
Produit offert
Ticket premium gratuit
Réduction %
-20% sur les 2 prochains
Réduction fixe
-5000 FCFA
Cashback wallet
Créditer X sur le wallet interne
Accès exclusif
Débloque une catégorie VIP
Upgrade statut
Passe en niveau Gold

🏆 Mécanismes de statut / paliers
Niveaux (Bronze → Silver → Gold → Platinum)
Critères : montant cumulé, nombre d'achats, points
Avantages par niveau : multiplicateur de points, accès prioritaire
👥 Parrainage
Lien/code unique par client
Récompense pour le parrain ET le filleul
Conditions : premier achat, montant minimum, délai
💰 Wallet
Crédit en monnaie virtuelle ou réelle
Utilisable comme moyen de paiement partiel ou total
Expiration configurable

3. La conception de NOTRE solution
Vision : un Rule Engine as a Service
Le cœur de tout est un moteur de règles événementiel. Chaque action sur la plateforme cliente déclenche un événement, le moteur évalue les règles, et applique les effets.
Plateforme Cliente ──► POST /events ──► Moteur de Règles ──► Effets appliqués
                       (achat, inscription,              (points crédités,
                        parrainage, etc.)                 récompense déclenchée)

Architecture en couches
┌─────────────────────────────────────────────────────┐
│                   DASHBOARD TENANT                   │  ← Interface config
│         (chaque client configure ses règles)         │
└─────────────────────┬───────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────┐
│                    API GATEWAY                       │
│              Auth / Tenant Resolution                │
└──────┬───────────────┬──────────────────┬───────────┘
       │               │                  │
┌──────▼──────┐ ┌──────▼──────┐ ┌────────▼────────┐
│  Event API  │ │  Admin API  │ │   Webhook API   │
│  /events    │ │  /rules     │ │   Notif sortantes│
└──────┬──────┘ └──────┬──────┘ └─────────────────┘
       │               │
┌──────▼───────────────▼──────────────────────────────┐
│                  RULE ENGINE                         │
│   Évalue les règles du tenant en temps réel         │
│   (conditions + triggers + effects)                 │
└──────┬──────────────────────────────────────────────┘
       │
┌──────▼──────────────────────────────────────────────┐
│              CORE SERVICES                           │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌────────┐ │
│  │  Points  │ │  Wallet  │ │ Parrain- │ │ Statut │ │
│  │  Engine  │ │ Service  │ │   age    │ │/Paliers│ │
│  └──────────┘ └──────────┘ └──────────┘ └────────┘ │
└──────┬──────────────────────────────────────────────┘
       │
┌──────▼──────────────────────────────────────────────┐
│              DATA LAYER (multi-tenant isolé)         │
│   PostgreSQL (règles, membres) + Redis (sessions)   │
└─────────────────────────────────────────────────────┘

Le modèle de données central : la Règle
Une règle = Trigger + Conditions + Effets
json
{
  "tenant_id": "airtel-cm",
  "rule": {
    "name": "10 tickets premium → 1 gratuit",
    "trigger": {
      "event": "purchase.completed",
      "filters": { "product_category": "premium_ticket" }
    },
    "conditions": [
      {
        "type": "cumulative_count",
        "event": "purchase.completed",
        "filters": { "product_category": "premium_ticket" },
        "operator": ">=",
        "value": 10,
        "window": "rolling_lifetime"
      }
    ],
    "effects": [
      {
        "type": "grant_reward",
        "reward": { "type": "free_product", "product_id": "premium_ticket" }
      },
      {
        "type": "reset_counter",
        "counter": "premium_ticket_purchases"
      }
    ]
  }
}

Les events entrants (ce que la plateforme cliente envoie)
http
POST /v1/events
X-Tenant-ID: airtel-cm
X-API-Key: sk_live_xxx

{
  "event": "purchase.completed",
  "member_id": "user_456",
  "timestamp": "2026-04-27T10:30:00Z",
  "data": {
    "order_id": "ORD-789",
    "amount": 15000,
    "currency": "XAF",
    "products": [
      { "id": "premium_ticket", "category": "premium_ticket", "qty": 1, "price": 15000 }
    ]
  }
}
Le moteur répond immédiatement avec les effets appliqués :
json
{
  "member_id": "user_456",
  "effects_applied": [
    { "type": "points_credited", "amount": 750, "balance": 4250 },
    { "type": "counter_updated", "counter": "premium_ticket_purchases", "value": 7 }
  ],
  "notifications": [
    { "message": "Plus que 3 tickets pour gagner 1 ticket gratuit !" }
  ]
}

4. Les grandes questions de conception à trancher
Voilà les points sur lesquels on doit aller plus loin ensemble :
A. Isolation des tenants — Schema PostgreSQL séparé par tenant, ou tenant_id partout ? (trade-off coût vs isolation)
B. Expressivité des règles — DSL visuel (drag & drop dans le dashboard) ou règles JSON écrites à la main ? Ou les deux ?
C. Idempotence des events — Si la plateforme cliente envoie le même event deux fois (réseau), comment éviter de doubler les points ?
D. Temps réel vs asynchrone — Les effets sont-ils appliqués de façon synchrone (réponse API immédiate) ou via une queue (Kafka/Redis Streams) ?
E. Wallet — Monnaie virtuelle seulement, ou intégration paiement réel (Orange Money, MTN Mobile Money) ?

Par où veux-tu qu'on aille plus loin ? Le moteur de règles, le modèle de données complet, l'architecture technique, ou le dashboard de configuration ?
toujours dans l'analyse et la conception, je veux qu'on identifie les entités / classe qui interviennent et comment ils interagissent
27 avr.
Parfait. Faisons une analyse orientée domaine propre, en pensant DDD (Domain-Driven Design).
Voilà le diagramme des entités principales, puis on détaille leurs interactions.
V
Connexion à visualize...
Maintenant détaillons chaque entité et ses responsabilités précises.

Les 6 domaines et leurs entités
1. Domaine — Plateforme (violet)
Tenant — l'entité racine de tout. Chaque entreprise cliente est un tenant isolé. Il possède son propre namespace de données, ses règles, ses membres. C'est l'unité d'isolation du système.
Attributs clés : id, name, slug, plan (free/pro/enterprise), status, created_at
ApiKey — clé d'authentification émise pour un tenant. Une plateforme cliente s'authentifie via cette clé pour envoyer des events. Peut avoir des scopes (read-only, write, admin).
TenantConfig — configuration globale du tenant : devise des points, durée d'expiration, timezone, langue des notifications, limites de fraude (ex: max 1 event identique par minute par membre).

2. Domaine — Identité membre (vert)
Member — le client final de la plateforme cliente. Il est identifié par un external_id (l'ID que la plateforme cliente lui attribue). Un même membre physique peut exister dans plusieurs tenants sans confusion.
Attributs clés : id, tenant_id, external_id, email?, phone?, metadata (JSON libre), enrolled_at
MemberTier — niveau actuel du membre dans un programme par paliers (Bronze / Silver / Gold / Platinum). Calculé automatiquement en fonction des règles de progression définies par le tenant.
ReferralLink — lien/code unique généré par membre pour recruter des filleuls. Appartient à un membre, tracé par le ReferralProgram du tenant.
MemberSegment — tag de segmentation (ex: "VIP", "inactif_90j", "nouveau"). Permet de cibler des règles sur des sous-groupes.

3. Domaine — Moteur de règles (ambre)
C'est le cœur du système. Tout tourne autour de Rule.
Rule — une règle de fidélité. Structure : trigger + liste de conditions + liste d'effects. Appartient à un tenant. Peut avoir une priorité, un statut actif/inactif, une période de validité.
Trigger — l'événement qui active l'évaluation de la règle. Exemples : purchase.completed, account.created, referral.converted. Peut avoir des filtres (ex: seulement si product_category = "premium").
Condition — prédicat évalué sur le contexte au moment du trigger. Types possibles :
cumulative_count (ex : ≥ 10 achats)
cumulative_amount (ex : ≥ 50 000 XAF dépensés)
points_balance (ex : ≥ 5000 pts)
tier_is (ex : membre Gold)
time_window (ex : entre 18h et 20h)
Effect — action appliquée si toutes les conditions sont vraies. Types :
credit_points (ajouter N points)
debit_points (consommer N points)
credit_wallet (ajouter du crédit monétaire)
grant_reward (attribuer une récompense du catalogue)
update_tier (changer le niveau)
reset_counter (remettre à zéro un compteur)
send_notification (déclencher une notif)
Event — l'event entrant envoyé par la plateforme cliente via l'API. Contient : type, member_id, timestamp, data (payload JSON), idempotency_key (pour éviter les doublons).
Counter — compteur cumulatif par membre et par règle. Exemple : "nombre de tickets premium achetés par ce membre". Incrémenté à chaque event pertinent, réinitialisé par un effet reset_counter.

4. Domaine — Récompenses & Wallet (bleu)
PointsAccount — compte de points d'un membre pour un tenant donné. Un membre a exactement un PointsAccount par tenant. Contient le solde courant, les points en attente d'expiration.
Wallet — crédit monétaire (FCFA, EUR, etc.) utilisable comme moyen de paiement. Distinct des points — c'est de la valeur réelle ou semi-réelle. Configurable : expiration, plafond, utilisable en paiement partiel ou total.
Reward — le catalogue des récompenses définies par le tenant. Exemples : "Ticket premium gratuit", "Réduction 20%", "Accès VIP". Chaque récompense a un type, un coût en points, une quantité disponible, une date d'expiration.
RewardGrant — l'attribution concrète d'une récompense à un membre. C'est l'instance : statut (pending / active / used / expired), date d'utilisation, lien vers l'event déclencheur.
Transaction — l'écriture comptable de chaque mouvement. Chaque crédit ou débit de points ou de wallet crée une Transaction immuable. Permet l'audit complet et le recalcul du solde depuis zéro.

5. Domaine — Parrainage (rose)
ReferralProgram — la configuration du programme de parrainage du tenant. Définit : récompense pour le parrain, récompense pour le filleul, condition de déclenchement (ex: "premier achat du filleul ≥ 5000 XAF"), délai d'attribution.
ReferralEvent — l'enregistrement d'un parrainage concret : qui a parrainé qui, quand, quel event l'a déclenché, statut (pending / rewarded / expired).

6. Domaine — Transversal (corail)
Notification — message envoyé à un membre (push, email, SMS) ou à la plateforme cliente. Déclenché par un Effect. Le contenu est un template configurable par le tenant.
Webhook — callback HTTP sortant vers la plateforme cliente. Déclenché quand un effet est appliqué. Permet à la plateforme de savoir en temps réel qu'un membre a gagné une récompense.
AuditLog — trace immuable de chaque action : quel event a déclenché quelle règle, quels effets ont été appliqués, à quel timestamp. Indispensable pour le support et la conformité.
Campaign — règle temporisée ou ponctuelle. Ex : "double points ce weekend". Une Campaign crée ou modifie des Rule sur une période définie, puis les désactive automatiquement.

Les interactions clés
Event (entrant)
  → résolution du Tenant (via ApiKey)
  → identification du Member (via external_id)
  → évaluation de toutes les Rules actives du tenant
      → pour chaque Rule : évaluation du Trigger → filtres OK ?
          → évaluation des Conditions (Counter, PointsAccount, MemberTier…)
              → si tout OK : application des Effects
                  → Transaction créée (points / wallet)
                  → RewardGrant créée (récompense)
                  → Counter mis à jour / réinitialisé
                  → MemberTier recalculé
                  → Notification envoyée
                  → Webhook déclenché
                  → AuditLog écrit

Ce qu'on doit trancher ensuite
La question de conception la plus importante qui découle de ce modèle : comment représenter Trigger, Condition et Effect en base de données ? Trois options :
Table unique avec type + JSON — simple, flexible, mais requêtes lourdes
Tables séparées par type — propre, typé, mais migrations fréquentes à chaque nouveau type
EAV (Entity-Attribute-Value) — ultra flexible mais illisible
Et aussi : quel est le cycle de vie exact d'un Event entrant — synchrone ou queue asynchrone ?
Tu veux qu'on attaque le schéma de la base de données, ou plutôt la logique d'évaluation du moteur de règles ?
CITE donc :
les cas d'utilisations
les classes metier
27 avr.
On va faire deux diagrammes séparés : d'abord les cas d'utilisation par acteur, ensuite les classes métier avec leurs relations.
Cas d'utilisation
V
Connexion à visualize...
Maintenant les classes métier avec leurs attributs et relations de cardinalité.
Classes métier
V
Connexion à visualize...

Voici la synthèse structurée des deux vues.
Cas d'utilisation — récapitulatif
3 acteurs avec des responsabilités bien séparées :
L'Admin Tenant agit uniquement via le dashboard : il configure l'identité de son tenant, génère ses clés API, compose ses règles de fidélité (triggers, conditions, effets), construit son catalogue de récompenses, définit ses paliers et lance des campagnes temporisées.
La Plateforme Cliente est un acteur purement programmatique : elle ne touche jamais au dashboard. Elle s'authentifie avec une ApiKey, envoie des events (achats, inscriptions, actions), enregistre ses membres, consulte leurs soldes, valide la consommation de récompenses, reçoit les webhooks sortants et gère les codes de parrainage.
Le Membre Final vit l'expérience via l'interface de la plateforme cliente (pas directement notre SaaS). Il consulte ses points et récompenses, échange ses points, utilise son wallet, parraine ses amis, suit sa progression de palier et reçoit des notifications.

Classes métier — récapitulatif
14 classes réparties en 4 groupes fonctionnels :
Groupe Plateforme : Tenant (racine, tout lui appartient) et ApiKey (moyen d'authentification, N par tenant).
Groupe Identité : Member (client final, identifié par external_id pour rester découplé), MemberTier (palier courant, 1-pour-1 avec Member), et en extension ReferralProgram (programme de parrainage configuré par tenant).
Groupe Moteur : Rule (l'entité centrale), composée d'un Trigger (1-pour-1), de plusieurs Condition (1 à N, toutes évaluées en AND par défaut) et de plusieurs Effect (1 à N, appliqués séquentiellement si toutes les conditions passent). Le Counter est un compteur par membre/règle, maintenu en vie entre les events.
Groupe Récompenses : PointsAccount (solde de points, 1-pour-1 avec Member par tenant), Wallet (crédit monétaire, idem), Reward (catalogue du tenant), RewardGrant (attribution concrète à un membre), Transaction (écriture comptable immuable de chaque mouvement de points ou wallet), Notification (message envoyé au membre) et AuditLog (trace immuable par event traité).

On peut maintenant passer au schéma de base de données complet, ou à la modélisation du moteur d'évaluation des règles. Par où tu veux aller ?
c'est quoi en pointillé
27 avr.
Les pointillés sur le diagramme représentent des relations de dépendance ou d'association indirecte — c'est-à-dire que deux classes se connaissent via une clé étrangère (member_id, tenant_id…) mais ne sont pas liées par une composition ou agrégation forte.
Plus concrètement dans notre diagramme :
Ligne pleine → relation structurelle forte. Exemple : Rule → Trigger (le trigger fait partie intégrante de la règle, il n'existe pas sans elle).
Ligne pointillée → association par référence. Exemple : Member → PointsAccount — le compte de points appartient au membre, mais c'est une entité autonome avec son propre cycle de vie. On matérialise le lien par member_id en base, pas par une imbrication.
En notation UML stricte, j'aurais dû utiliser des symboles précis :
Losange plein (composition) → l'enfant ne vit pas sans le parent. Ex : Rule ◆→ Condition
Losange vide (agrégation) → l'enfant peut exister indépendamment. Ex : Tenant ◇→ Member
Flèche simple (association) → simple référence entre deux entités
Flèche pointillée (dépendance) → une classe utilise une autre sans la posséder
Le diagramme que j'ai produit est une version simplifiée — les pointillés signifient globalement "ces deux classes se référencent". Si tu veux, on peut refaire un diagramme UML rigoureux avec les bons symboles de cardinalité et de composition.
sur le diagramme de use case, dit ce qui extend de qui, include de qui, et est un cas particulier de qui. (pas besoin de retracer le diagramme)
sur le diagramme de classe, dit moi les relations qui interviennement entre les classes (pas besoin de retracer le diagramme)
27 avr.
Diagramme de cas d'utilisation
Relations <<include>> — cas obligatoirement inclus
Certains cas d'utilisation en appellent systématiquement un autre pour fonctionner.
Envoyer un événement <<include>> S'authentifier via ApiKey — impossible d'envoyer un event sans que la plateforme cliente présente une clé valide. C'est un prérequis non négociable.
Envoyer un événement <<include>> Identifier le membre — le moteur doit résoudre le external_id vers un Member interne avant toute évaluation.
Créer une règle <<include>> Définir un Trigger — une règle sans trigger est invalide, la création du trigger fait partie intégrante du processus.
Créer une règle <<include>> Définir au moins une Condition — même logique, une règle sans condition ne peut pas être sauvegardée.
Créer une règle <<include>> Définir au moins un Effect — une règle sans effet n'a aucun sens métier.
Échanger des points <<include>> Vérifier le solde — avant toute rédemption, le système vérifie que le PointsAccount a suffisamment de points.
Consommer une récompense <<include>> Valider le RewardGrant — la plateforme cliente doit vérifier que le grant existe, appartient au bon membre, et n'est pas déjà utilisé ou expiré.
Utiliser le wallet <<include>> Vérifier le solde du Wallet — même mécanique que pour les points.

Relations <<extend>> — cas optionnels qui enrichissent un cas de base
Ces cas ne se déclenchent que sous certaines conditions.
Envoyer un événement <<extend>> par Incrémenter un Counter — seulement si la règle matchée utilise un mécanisme cumulatif (tampons, seuils).
Envoyer un événement <<extend>> par Déclencher une Notification — seulement si un Effect de type send_notification est configuré.
Envoyer un événement <<extend>> par Envoyer un Webhook — seulement si le tenant a configuré une URL de callback.
Envoyer un événement <<extend>> par Upgrader le palier — seulement si les critères de MemberTier sont atteints suite à l'event.
Créer une règle <<extend>> par Associer à une Campaign — optionnel, uniquement quand la règle est temporisée (double points le weekend, etc.).
Parrainer un ami <<extend>> par Récompenser le parrain — se déclenche seulement quand le filleul remplit la condition définie dans le ReferralProgram (ex: premier achat).
Parrainer un ami <<extend>> par Récompenser le filleul — idem, conditionnel à la conversion du filleul.

Relations de généralisation — cas particuliers (héritage)
Ces cas sont des spécialisations d'un cas plus général.
Créditer des points et Débiter des points sont des cas particuliers de Créer une Transaction — les deux créent une Transaction immuable, mais avec un type credit ou debit et des règles de validation différentes.
Cashback wallet est un cas particulier de Créditer un Wallet — même mécanique, mais déclenché automatiquement par une règle plutôt que manuellement.
Réduction en % et Réduction fixe et Produit offert sont des cas particuliers de Accorder une récompense — ils partagent la création d'un RewardGrant, mais leur Effect.params et leur logique de validation à la consommation diffèrent.
Enregistrer un filleul est un cas particulier de Enregistrer un membre — même flux de base, avec en plus la résolution du code de parrainage et la création d'un ReferralEvent.

Diagramme de classes — relations
Composition ◆ — l'enfant ne vit pas sans le parent
Rule ◆→ Trigger (1 à 1) — un trigger n'a aucune existence hors d'une règle. Si la règle est supprimée, le trigger disparaît.
Rule ◆→ Condition (1 à N) — même logique. Les conditions sont des composants internes de la règle.
Rule ◆→ Effect (1 à N) — les effets sont définis dans le contexte de la règle, ils n'ont pas de vie propre.

Agrégation ◇ — l'enfant peut exister indépendamment
Tenant ◇→ Member (1 à N) — les membres appartiennent au tenant, mais leur cycle de vie peut être géré séparément (archivage, export).
Tenant ◇→ Rule (1 à N) — les règles sont configurées par le tenant mais peuvent être désactivées, exportées, clonées indépendamment.
Tenant ◇→ Reward (1 à N) — le catalogue de récompenses appartient au tenant, chaque récompense a son propre cycle de vie (stock, expiration).

Association → — référence directe entre entités autonomes
Member → PointsAccount (1 à 1 par tenant) — un membre possède exactement un compte de points par tenant. Le PointsAccount référence le membre mais peut être recalculé indépendamment depuis les Transaction.
Member → Wallet (1 à 1 par tenant) — même structure que PointsAccount, mais pour la valeur monétaire.
Member → MemberTier (1 à 1) — le palier courant est une projection calculée à partir de l'historique du membre.
Member → RewardGrant (1 à N) — un membre peut recevoir plusieurs récompenses au fil du temps.
Reward → RewardGrant (1 à N) — une récompense du catalogue peut être attribuée à plusieurs membres.
PointsAccount → Transaction (1 à N) — chaque mouvement de points génère une Transaction immuable. Le solde est la somme de toutes les transactions.
Wallet → Transaction (1 à N) — même mécanique pour les mouvements monétaires.
Event → AuditLog (1 à 1) — chaque event traité produit une entrée d'audit qui trace quelles règles ont été évaluées et quels effets appliqués.
Event → Transaction (1 à N) — un event peut produire plusieurs transactions (ex: points + wallet simultanément).
Rule → Counter (1 à N) — une règle cumulative maintient un compteur par membre.
Member → Counter (1 à N) — un membre peut avoir plusieurs compteurs actifs en parallèle (un par règle cumulative).

Dépendance ⇢ — une classe utilise une autre sans la posséder
Effect ⇢ PointsAccount — l'effet de type credit_points manipule le compte de points du membre, mais ne le possède pas.
Effect ⇢ Wallet — idem pour credit_wallet.
Effect ⇢ RewardGrant — l'effet de type grant_reward crée une instance de RewardGrant, il en dépend sans l'agréger.
Effect ⇢ Notification — l'effet de type send_notification crée une Notification, sans en être le propriétaire permanent.
Effect ⇢ MemberTier — l'effet update_tier modifie le palier du membre.
Effect ⇢ Counter — l'effet reset_counter remet à zéro le compteur lié à la règle.
ReferralProgram ⇢ Reward (2 fois) — le programme référence deux récompenses du catalogue : une pour le parrain (referrer_reward_id) et une pour le filleul (referee_reward_id), sans les posséder.
pour les use case pertinents, decris le process de maniere à ce que l'on trace les diagrammes de sequences technique facilement
27 avr.
Descriptions des séquences techniques
Je couvre les cas d'utilisation les plus structurants — ceux qui font le cœur du moteur.

UC1 — Envoyer un événement (cas central)
Acteurs : Plateforme Cliente, API Gateway, RuleEngine, EventStore, CounterStore, PointsAccount, WalletService, RewardService, TierService, NotificationService, WebhookService, AuditLog
Précondition : la plateforme cliente possède une ApiKey valide.
Séquence :
La Plateforme Cliente envoie POST /v1/events avec le header X-API-Key et un body contenant event_type, external_id, idempotency_key, payload.
L'API Gateway reçoit la requête. Il extrait l'X-API-Key et interroge le TenantStore pour résoudre le tenant_id correspondant. Si la clé est invalide ou révoquée, il retourne 401. Si le tenant est suspendu, il retourne 403.
L'API Gateway vérifie l'idempotency_key dans le IdempotencyStore. Si la clé existe déjà, il retourne directement la réponse mise en cache sans retraitement (200 avec le résultat original).
L'API Gateway résout le Member depuis le MemberStore via (tenant_id, external_id). Si le membre n'existe pas, il le crée automatiquement (enrolment implicite) puis continue.
L'API Gateway persiste l'Event dans l'EventStore avec le statut received.
L'API Gateway transmet l'event au RuleEngine avec le contexte enrichi {tenant_id, member_id, event}.
Le RuleEngine charge toutes les Rule actives du tenant depuis le RuleStore, triées par priorité décroissante.
Pour chaque Rule, le RuleEngine évalue le Trigger : il compare event_type et applique les filtres du trigger sur le payload. Si le trigger ne matche pas, la règle est ignorée.
Pour chaque règle dont le trigger matche, le RuleEngine évalue les Condition une par une. Pour les conditions de type cumulative_count ou cumulative_amount, il interroge le CounterStore pour obtenir la valeur courante du compteur (member_id, rule_id). Pour les conditions de type points_balance, il interroge le PointsAccount. Pour les conditions de type tier_is, il interroge le TierStore. Si toutes les conditions sont vraies, la règle est déclenchée. Si au moins une est fausse, la règle est ignorée.
Pour chaque règle déclenchée, le RuleEngine exécute les Effect séquentiellement :
credit_points → appel au PointsAccount pour créditer N points, qui crée une Transaction immuable.
debit_points → appel au PointsAccount pour débiter N points, qui crée une Transaction immuable.
credit_wallet → appel au WalletService pour créditer le montant, qui crée une Transaction immuable.
grant_reward → appel au RewardService pour créer un RewardGrant avec statut active.
reset_counter → appel au CounterStore pour remettre à zéro le compteur (member_id, rule_id).
update_tier → appel au TierService pour recalculer et mettre à jour le MemberTier.
send_notification → appel au NotificationService pour envoyer le message au membre.
Après tous les effets, le CounterStore incrémente les compteurs de toutes les règles cumulatives matchées (qu'elles aient déclenché ou non leurs conditions).
Le RuleEngine retourne au WebhookService la liste des effets appliqués. Le WebhookService interroge le TenantStore pour récupérer l'URL de callback configurée, puis envoie un POST asynchrone vers la plateforme cliente avec le résumé des effets.
L'AuditLog persiste une entrée avec : event_id, liste des règles évaluées, liste des règles déclenchées, liste des effets appliqués, timestamp.
L'API Gateway met à jour l'EventStore avec le statut processed et persiste la réponse dans l'IdempotencyStore.
L'API Gateway retourne 200 à la Plateforme Cliente avec le résumé : points crédités, récompenses gagnées, nouveau palier, notifications envoyées.
Postcondition : l'event est persisté, les effets sont appliqués, le webhook est parti, l'audit est écrit.

UC2 — Créer une règle de fidélité
Acteurs : Admin Tenant, Dashboard API, RuleValidator, RuleStore
Précondition : l'admin est authentifié sur le dashboard, son tenant est actif.
Séquence :
L'Admin soumet le formulaire de création de règle avec : name, priority, trigger {event_type, filters}, conditions[], effects[], valid_from?, valid_until?.
Le Dashboard API reçoit la requête et extrait le tenant_id depuis le token de session.
Le Dashboard API transmet la règle au RuleValidator. Celui-ci vérifie que le event_type du trigger est un type d'event connu et supporté. Il vérifie que chaque Condition a un type valide, un operator compatible avec ce type, et une value cohérente. Il vérifie que chaque Effect a un type valide et que ses params sont complets (ex: un effet grant_reward doit référencer un reward_id existant dans le catalogue du tenant). Il vérifie qu'il n'existe pas de règle active avec le même trigger et une priorité identique (risque de conflit).
Si la validation échoue, le Dashboard API retourne 422 avec le détail des erreurs par champ.
Si la validation réussit, le RuleStore persiste la règle avec le statut draft.
L'Admin choisit d'activer immédiatement ou de laisser en draft. Si activation immédiate, le RuleStore passe le statut à active et invalide le cache des règles du tenant dans le RuleEngine.
Le Dashboard API retourne 201 avec la règle créée et son id.
Postcondition : la règle est persistée. Si activée, elle sera évaluée dès le prochain event entrant du tenant.

UC3 — Échanger des points contre une récompense
Acteurs : Membre Final (via interface Plateforme), Plateforme Cliente, API Gateway, PointsAccount, RewardService, TransactionStore, NotificationService, WebhookService
Précondition : le membre a un PointsAccount avec un solde suffisant. La récompense cible existe dans le catalogue du tenant avec un stock disponible.
Séquence :
Le Membre choisit une récompense dans l'interface de la Plateforme Cliente.
La Plateforme Cliente envoie POST /v1/members/{external_id}/redeem avec reward_id et idempotency_key.
L'API Gateway authentifie la requête via X-API-Key, résout le tenant_id et le member_id.
L'API Gateway vérifie l'idempotency_key pour éviter une double rédemption.
Le RewardService charge la Reward depuis le RewardStore. Il vérifie que la récompense appartient bien au tenant. Il vérifie que le stock est supérieur à 0 (si stock défini). Il vérifie que la date d'expiration n'est pas dépassée.
Le PointsAccount est interrogé pour vérifier que balance >= reward.cost_points. Si le solde est insuffisant, l'API retourne 422 avec le motif insufficient_points.
Le PointsAccount décrédite reward.cost_points en créant une Transaction immuable de type debit avec source = "redemption" et reward_id en metadata.
Le RewardService crée un RewardGrant avec statut active, member_id, reward_id, granted_at = now.
Si la récompense a un stock défini, le RewardStore décrémente stock de 1 de manière atomique (avec optimistic locking pour éviter la survente).
Le NotificationService envoie une notification au membre pour confirmer l'obtention de la récompense.
Le WebhookService notifie la Plateforme Cliente de manière asynchrone avec les détails du RewardGrant créé.
L'API Gateway retourne 201 avec le RewardGrant : id, status, reward, expires_at?.
Postcondition : les points sont débités, le RewardGrant est actif, la plateforme cliente est notifiée et peut afficher la récompense à l'utilisateur.

UC4 — Consommer une récompense (validation par la plateforme)
Acteurs : Plateforme Cliente, API Gateway, RewardService, AuditLog, WebhookService
Précondition : un RewardGrant avec statut active existe pour le membre.
Séquence :
Au moment de l'utilisation concrète de la récompense (passage en caisse, checkout…), la Plateforme Cliente envoie POST /v1/reward-grants/{grant_id}/consume avec member_external_id et context (metadata libre : order_id, etc.).
L'API Gateway authentifie la requête et résout le tenant_id.
Le RewardService charge le RewardGrant via grant_id. Il vérifie que le grant appartient bien au tenant (protection cross-tenant). Il vérifie que le member_id du grant correspond au external_id fourni. Il vérifie que le statut est active. Il vérifie que la date d'expiration n'est pas dépassée. Si une vérification échoue, retour 422 avec le motif précis.
Le RewardService passe le statut du RewardGrant à used et enregistre used_at = now et context fourni.
L'AuditLog persiste la consommation avec : grant_id, member_id, tenant_id, context, timestamp.
L'API Gateway retourne 200 avec le grant mis à jour et les détails de la récompense (type, valeur, contexte).
Postcondition : le RewardGrant est used, non réutilisable. La plateforme cliente applique l'avantage concret dans son propre système.

UC5 — Parrainer un ami
Acteurs : Membre Parrain, Plateforme Cliente, API Gateway, ReferralService, MemberStore, RuleEngine, NotificationService
Précondition : un ReferralProgram actif existe pour le tenant.
Séquence — Phase 1 : génération du lien
Le Membre demande son lien de parrainage dans l'interface.
La Plateforme Cliente envoie GET /v1/members/{external_id}/referral-link.
L'API Gateway résout le tenant_id et le member_id.
Le ReferralService vérifie si un ReferralLink actif existe déjà pour ce membre. Si oui, il le retourne directement. Si non, il en génère un nouveau avec un code unique, le persiste et le retourne.
La Plateforme Cliente affiche le lien/code au membre pour qu'il le partage.
Séquence — Phase 2 : enrôlement du filleul
Le filleul s'inscrit sur la Plateforme Cliente via le lien de parrainage.
La Plateforme Cliente envoie POST /v1/members avec external_id du filleul et referral_code dans le payload.
L'API Gateway crée le Member filleul dans le MemberStore.
Le ReferralService résout le referral_code vers le Member parrain. Il crée un ReferralEvent avec referrer_id, referee_id, statut pending.
Séquence — Phase 3 : déclenchement de la récompense
Plus tard, quand le filleul déclenche l'event de conversion défini dans le ReferralProgram (ex: purchase.completed avec amount >= min_order), la Plateforme Cliente envoie l'event normalement via UC1.
Le RuleEngine évalue les règles normalement. Une règle spéciale liée au ReferralProgram détecte que l'émetteur de l'event est un filleul avec un ReferralEvent en statut pending.
Le RuleEngine applique deux effets distincts : grant_reward avec referrer_reward_id pour le parrain, et grant_reward avec referee_reward_id pour le filleul.
Le ReferralService passe le ReferralEvent au statut rewarded.
Le NotificationService envoie une notification au parrain ("Votre ami a rejoint, vous avez gagné X") et au filleul ("Bienvenue, vous avez gagné Y").
Postcondition : les deux membres ont reçu leur RewardGrant. Le ReferralEvent est clôturé. La mécanique ne peut pas se déclencher une seconde fois pour le même couple parrain/filleul.

UC6 — Lancer une campagne
Acteurs : Admin Tenant, Dashboard API, CampaignService, RuleStore, Scheduler
Précondition : le tenant a déjà des règles existantes ou crée de nouvelles règles pour la campagne.
Séquence :
L'Admin crée une campagne avec : name, valid_from, valid_until, et une liste de modifications de règles (ex: multiplier les points par 2 sur toutes les règles credit_points).
Le Dashboard API transmet au CampaignService qui persiste la Campaign avec statut scheduled.
Le Scheduler surveille les campagnes dont valid_from <= now. À l'heure de démarrage, il appelle le CampaignService pour activer la campagne.
Le CampaignService applique les modifications de règles définies : il crée des versions temporaires des règles affectées avec les nouveaux paramètres, en conservant les originales en statut suspended_by_campaign.
Le RuleStore invalide le cache des règles du tenant. À partir de maintenant, le RuleEngine évalue les règles modifiées.
À valid_until, le Scheduler déclenche la désactivation. Le CampaignService restaure les règles originales, supprime les versions temporaires, et passe la Campaign au statut completed.
Le RuleStore invalide à nouveau le cache.
Postcondition : les règles originales sont restaurées. L'AuditLog conserve la trace de toutes les transactions effectuées pendant la campagne avec le flag campaign_id.

Ces six séquences couvrent le cœur du système. Les suivantes — Configurer les paliers, Consulter le solde, Recevoir un webhook — sont plus simples et se déduisent facilement depuis ces bases. Tu veux qu'on attaque les diagrammes de séquence UML formels maintenant ?
recommence avec moins de terme technique. juste des phrase simple et des fleches
27 avr.
UC1 — Envoyer un événement
Plateforme Cliente
      |
      |  "J'ai une action à signaler (un achat, une inscription…)"
      ▼
API Gateway
      |
      |  "C'est quelle entreprise qui parle ?"
      ▼
TenantStore  ──►  "Clé valide, c'est Airtel-CM"
      |
      |  "Est-ce que ce message a déjà été traité ?"
      ▼
IdempotencyStore  ──►  "Non, c'est nouveau"
      |
      |  "C'est quel client ?"
      ▼
MemberStore  ──►  "C'est Jean Dupont, member_id = 456"
      |
      |  "Je sauvegarde l'action"
      ▼
EventStore  ──►  "Action enregistrée"
      |
      |  "Quelles règles s'appliquent pour Airtel-CM ?"
      ▼
RuleEngine
      |
      |  "Je charge les règles actives"
      ▼
RuleStore  ──►  "Voici 5 règles actives"
      |
      |  "Règle 1 : est-ce que l'action correspond au déclencheur ?"
      |  "→ Oui, c'est bien un achat de ticket premium"
      |
      |  "Est-ce que les conditions sont remplies ?"
      ▼
CounterStore  ──►  "Jean en est à son 10ème achat premium"
      |
      |  "→ Oui ! Toutes les conditions sont remplies"
      |
      |  "J'applique les effets"
      |
      |──────────────────────────────────────────────────┐
      |                                                  ▼
      |                                          PointsAccount
      |                                    "Je crédite 500 points à Jean"
      |
      |──────────────────────────────────────────────────┐
      |                                                  ▼
      |                                          RewardService
      |                                    "Je crée un ticket gratuit pour Jean"
      |
      |──────────────────────────────────────────────────┐
      |                                                  ▼
      |                                          CounterStore
      |                                    "Je remets le compteur de Jean à 0"
      |
      |  "Je notifie Jean et la plateforme"
      |
      |──────────────────────────────────────────────────┐
      |                                                  ▼
      |                                       NotificationService
      |                                    "Message envoyé à Jean :
      |                                     vous avez gagné un ticket !"
      |
      |──────────────────────────────────────────────────┐
      |                                                  ▼
      |                                          WebhookService
      |                               "POST vers Airtel-CM :
      |                                Jean a gagné une récompense"
      |
      |  "Je trace tout"
      ▼
AuditLog  ──►  "Enregistré : quelle règle, quel effet, pour qui, quand"
      |
      ▼
Plateforme Cliente
"Réponse : Jean a gagné 500 points et un ticket gratuit"

UC2 — Créer une règle de fidélité
Admin Tenant
      |
      |  "Je veux créer une règle :
      |   après 10 achats premium → donner 1 ticket gratuit"
      ▼
Dashboard
      |
      |  "Est-ce que cette règle est valide ?"
      ▼
RuleValidator
      |
      |  "Le déclencheur existe-t-il ?"  ──►  "Oui, purchase.completed"
      |  "Les conditions sont-elles cohérentes ?"  ──►  "Oui"
      |  "La récompense existe-t-elle dans le catalogue ?"  ──►  "Oui"
      |  "Y a-t-il un conflit de priorité ?"  ──►  "Non"
      |
      |  "→ Règle valide"
      ▼
RuleStore
      |  "Règle sauvegardée en brouillon"
      |
      |  "L'admin veut l'activer maintenant"
      ▼
RuleStore  ──►  "Règle passée en active"
      |
      ▼
RuleEngine  ──►  "Cache vidé, la règle sera évaluée dès le prochain event"
      |
      ▼
Admin Tenant
"Votre règle est active"

UC3 — Échanger des points contre une récompense
Membre Final
      |
      |  "Je veux échanger mes points contre une réduction 20%"
      ▼
Plateforme Cliente
      |
      |  "Ce membre peut-il faire cet échange ?"
      ▼
API Gateway  ──►  "Clé valide, tenant identifié, membre identifié"
      |
      |  "Cette récompense est-elle disponible ?"
      ▼
RewardService
      |  "Elle existe ? ──► Oui"
      |  "Stock disponible ? ──► Oui"
      |  "Pas expirée ? ──► Oui"
      |
      |  "Jean a assez de points ?"
      ▼
PointsAccount  ──►  "Solde : 5200 pts, coût : 5000 pts  →  OK"
      |
      |  "Je débite les points"
      ▼
PointsAccount  ──►  "Transaction créée : -5000 pts pour Jean"
      |
      |  "Je crée la récompense pour Jean"
      ▼
RewardService  ──►  "Récompense créée, statut : active"
      |
      |  "Je préviens Jean et la plateforme"
      |
      |──────────────────────────────────┐
      |                                  ▼
      |                         NotificationService
      |                      "Message à Jean :
      |                       votre réduction est disponible !"
      |
      |──────────────────────────────────┐
      |                                  ▼
      |                          WebhookService
      |                    "POST vers la plateforme :
      |                     Jean a une réduction active"
      |
      ▼
Plateforme Cliente
"Récompense prête, Jean peut l'utiliser"

UC4 — Utiliser une récompense (au moment du paiement)
Membre Final
      |
      |  "J'utilise ma réduction au moment de payer"
      ▼
Plateforme Cliente
      |
      |  "Je confirme que cette récompense est bien valide"
      ▼
API Gateway  ──►  "Tenant et membre identifiés"
      |
      ▼
RewardService
      |  "Cette récompense appartient bien à ce tenant ?  ──►  Oui"
      |  "Elle appartient bien à Jean ?  ──►  Oui"
      |  "Son statut est bien 'active' ?  ──►  Oui"
      |  "Elle n'est pas expirée ?  ──►  Oui"
      |
      |  "→ Je la consomme"
      ▼
RewardService  ──►  "Statut passé à 'utilisée', heure notée"
      |
      ▼
AuditLog  ──►  "Enregistré : qui, quand, dans quel contexte"
      |
      ▼
Plateforme Cliente
"Réduction appliquée, Jean a économisé 20%"

UC5 — Parrainer un ami
── PHASE 1 : Jean veut parrainer ──

Membre Jean
      |
      |  "Je veux mon lien de parrainage"
      ▼
Plateforme Cliente  ──►  API Gateway  ──►  ReferralService
      |
      |  "Jean a déjà un lien ?"  ──►  "Non"
      |  "→ Je génère un code unique pour Jean"
      ▼
Plateforme Cliente
"Voici votre lien : ref.app/JEAN42"


── PHASE 2 : Marie s'inscrit via le lien ──

Membre Marie
      |
      |  "Je m'inscris avec le code JEAN42"
      ▼
Plateforme Cliente  ──►  API Gateway
      |
      |  "Je crée le compte de Marie"
      ▼
MemberStore  ──►  "Marie créée"
      |
      |  "Je note que Marie vient de Jean"
      ▼
ReferralService  ──►  "Parrainage enregistré, statut : en attente"


── PHASE 3 : Marie fait son premier achat ──

Plateforme Cliente
      |
      |  "Marie vient de faire un achat de 6000 FCFA"
      ▼
API Gateway  ──►  RuleEngine
      |
      |  "Marie est une filleule avec un parrainage en attente ?"  ──►  "Oui"
      |  "L'achat remplit la condition du programme ?"  ──►  "Oui, ≥ 5000 FCFA"
      |
      |  "→ Je récompense les deux"
      |
      |──────────────────────────┐
      |                          ▼
      |                   RewardService
      |              "Ticket gratuit créé pour Jean (parrain)"
      |
      |──────────────────────────┐
      |                          ▼
      |                   RewardService
      |              "Réduction 10% créée pour Marie (filleule)"
      |
      ▼
ReferralService  ──►  "Parrainage clôturé, statut : récompensé"
      |
      |──────────────────────────┐
      |                          ▼
      |                 NotificationService
      |          "Message à Jean : votre ami a rejoint !"
      |          "Message à Marie : bienvenue, voici votre cadeau !"

UC6 — Lancer une campagne (double points ce weekend)
Admin Tenant
      |
      |  "Je veux doubler les points
      |   du samedi 9h au dimanche 23h"
      ▼
Dashboard  ──►  CampaignService
      |
      |  "Campagne sauvegardée, statut : programmée"
      ▼
Scheduler
      |
      |  ... (le temps passe) ...
      |
      |  "Samedi 9h00 — c'est l'heure !"
      ▼
CampaignService
      |
      |  "Je modifie temporairement les règles de points"
      ▼
RuleStore  ──►  "Règles originales mises en veille,
                 versions ×2 activées"
      |
      ▼
RuleEngine  ──►  "Cache vidé, nouvelles règles en place"


      ... (weekend en cours, tous les points sont doublés) ...


      |
      |  "Dimanche 23h00 — c'est terminé"
      ▼
CampaignService
      |
      |  "Je restaure les règles originales"
      ▼
RuleStore  ──►  "Règles ×2 supprimées,
                 règles originales réactivées"
      |
      ▼
RuleEngine  ──►  "Cache vidé, règles normales en place"
      |
      ▼
Admin Tenant
"Campagne terminée — rapport disponible dans le dashboard"
Forme des phrases
27 avr.
UC1 — Envoyer un événement
La plateforme cliente envoie une action à signaler (un achat, une inscription…) à l'API Gateway.
L'API Gateway demande au TenantStore à quelle entreprise appartient cette clé API. Le TenantStore répond que c'est Airtel-CM.
L'API Gateway demande ensuite à l'IdempotencyStore si ce message a déjà été traité. L'IdempotencyStore répond que non, c'est nouveau.
L'API Gateway demande au MemberStore qui est ce client. Le MemberStore répond que c'est Jean Dupont.
L'API Gateway demande à l'EventStore de sauvegarder l'action. L'EventStore confirme que c'est enregistré.
L'API Gateway transmet l'action au RuleEngine en lui demandant d'évaluer les règles d'Airtel-CM. Le RuleEngine demande au RuleStore les règles actives du tenant. Le RuleStore lui retourne les règles.
Le RuleEngine examine la première règle et demande : est-ce que cette action correspond au déclencheur de la règle ? Il conclut que oui, c'est bien un achat de ticket premium.
Le RuleEngine vérifie ensuite les conditions. Il demande au CounterStore combien d'achats premium Jean a effectués. Le CounterStore répond que Jean en est à son 10ème achat. Le RuleEngine conclut que toutes les conditions sont remplies.
Le RuleEngine applique alors les effets un par un. Il demande au PointsAccount de créditer 500 points à Jean. Le PointsAccount crée une transaction et confirme. Il demande ensuite au RewardService de créer un ticket gratuit pour Jean. Le RewardService crée la récompense et confirme. Il demande enfin au CounterStore de remettre le compteur de Jean à zéro. Le CounterStore confirme.
Le RuleEngine demande au NotificationService d'envoyer un message à Jean pour lui dire qu'il a gagné un ticket gratuit. Le NotificationService confirme l'envoi.
Le RuleEngine demande au WebhookService de notifier Airtel-CM que Jean a gagné une récompense. Le WebhookService envoie le callback à la plateforme.
Le RuleEngine demande à l'AuditLog de tout tracer : quelle règle a été déclenchée, quel effet a été appliqué, pour qui et à quelle heure. L'AuditLog confirme.
L'API Gateway retourne la réponse à la plateforme cliente en lui indiquant que Jean a gagné 500 points et un ticket gratuit.

UC2 — Créer une règle de fidélité
L'admin soumet sa règle au Dashboard en indiquant qu'après 10 achats premium, le client doit recevoir un ticket gratuit.
Le Dashboard envoie la règle au RuleValidator pour vérification. Le RuleValidator vérifie que le déclencheur existe et est connu du système, que les conditions sont cohérentes, que la récompense existe bien dans le catalogue du tenant, et qu'il n'y a pas de conflit de priorité avec une règle existante. Le RuleValidator répond que tout est valide.
Le Dashboard demande au RuleStore de sauvegarder la règle. Le RuleStore la persiste en statut brouillon et confirme.
L'admin décide d'activer la règle immédiatement. Le Dashboard demande au RuleStore de passer la règle en statut actif. Le RuleStore confirme et signale au RuleEngine que son cache doit être vidé.
Le Dashboard confirme à l'admin que sa règle est désormais active.

UC3 — Échanger des points contre une récompense
Le membre choisit une récompense dans l'interface et demande à l'échanger contre ses points.
La plateforme cliente envoie la demande à l'API Gateway. L'API Gateway vérifie la clé, identifie le tenant et retrouve le membre.
L'API Gateway demande au RewardService si la récompense est disponible. Le RewardService vérifie qu'elle existe, que le stock n'est pas épuisé et qu'elle n'est pas expirée. Il répond que tout est bon.
L'API Gateway demande au PointsAccount si Jean a suffisamment de points. Le PointsAccount répond que Jean a 5200 points et que la récompense en coûte 5000, donc c'est bon.
Le PointsAccount débite 5000 points à Jean en créant une transaction immuable.
Le RewardService crée la récompense pour Jean avec le statut actif et confirme.
Le NotificationService envoie un message à Jean pour lui confirmer que sa récompense est disponible.
Le WebhookService notifie la plateforme cliente que Jean dispose d'une nouvelle récompense active.
La plateforme cliente reçoit la confirmation et affiche la récompense à Jean.

UC4 — Utiliser une récompense au moment du paiement
Jean présente sa récompense au moment de payer. La plateforme cliente envoie une demande de validation à l'API Gateway. L'API Gateway identifie le tenant et le membre.
Le RewardService reçoit la demande et vérifie que la récompense appartient bien à ce tenant, qu'elle appartient bien à Jean, que son statut est actif et qu'elle n'est pas expirée. Tout est en ordre.
Le RewardService passe le statut de la récompense à utilisée et note l'heure d'utilisation.
L'AuditLog enregistre qui a utilisé quoi, quand et dans quel contexte.
La plateforme cliente reçoit la confirmation et applique la réduction de 20% sur la commande de Jean.

UC5 — Parrainer un ami
Jean veut obtenir son lien de parrainage.
Jean demande son lien de parrainage depuis l'interface. La plateforme cliente transmet la demande à l'API Gateway qui l'achemine vers le ReferralService. Le ReferralService vérifie si Jean a déjà un lien actif. Il n'en a pas, donc il génère un code unique pour Jean, le sauvegarde et le retourne. La plateforme cliente affiche le lien à Jean.
Marie s'inscrit via le lien de Jean.
Marie clique sur le lien et s'inscrit sur la plateforme. La plateforme cliente envoie la demande d'inscription à l'API Gateway avec le code de parrainage de Jean. L'API Gateway crée le compte de Marie dans le MemberStore. Le ReferralService prend note que Marie vient de Jean et crée un parrainage en statut en attente.
Marie fait son premier achat.
La plateforme cliente signale l'achat de Marie via l'API Gateway comme pour n'importe quel événement. Le RuleEngine évalue les règles et détecte que Marie est une filleule avec un parrainage en attente. Il vérifie que l'achat remplit la condition du programme, soit un montant supérieur ou égal à 5000 FCFA. La condition est remplie.
Le RuleEngine demande au RewardService de créer un ticket gratuit pour Jean. Le RewardService confirme. Le RuleEngine demande au RewardService de créer une réduction de 10% pour Marie. Le RewardService confirme.
Le ReferralService passe le parrainage au statut récompensé pour éviter tout doublon.
Le NotificationService envoie un message à Jean pour lui dire que son ami a rejoint la plateforme et qu'il a gagné une récompense. Il envoie également un message à Marie pour lui souhaiter la bienvenue et l'informer de son cadeau.

UC6 — Lancer une campagne double points
L'admin crée une campagne depuis le Dashboard en indiquant qu'il veut doubler les points du samedi 9h au dimanche 23h. Le Dashboard transmet la demande au CampaignService qui sauvegarde la campagne en statut programmé.
Le Scheduler surveille en continu les campagnes programmées. Quand il détecte que l'heure de démarrage est atteinte, il demande au CampaignService d'activer la campagne.
Le CampaignService demande au RuleStore de mettre en veille les règles de points existantes et d'activer des versions temporaires qui multiplient les points par deux. Le RuleStore confirme et signale au RuleEngine de vider son cache. À partir de ce moment, tous les événements déclenchent des règles doublées.
Quand le Scheduler détecte que l'heure de fin est atteinte, il demande au CampaignService de terminer la campagne. Le CampaignService demande au RuleStore de supprimer les versions temporaires et de réactiver les règles originales. Le RuleStore confirme et signale au RuleEngine de vider son cache à nouveau.
Le Dashboard informe l'admin que la campagne est terminée et qu'un rapport est disponible.
je veux faire une document de conception et analyse pour ce projet
12 mai
Je vois que tu as déjà un document de conception assez avancé et tout le travail qu'on a fait ensemble. Avant de rédiger, laisse-moi cadrer le document correctement.
Dis-moi comment orienter le document pour que je le rédige correctement.
Public cible — jury académique, équipe technique, ou les deux ?
Sections à inclure — contexte et objectifs, cas d'utilisation, diagrammes de classes, diagrammes de séquence, architecture technique, modèle de données, sécurité et déploiement ?
Langue — français, anglais, ou français avec résumé en anglais ?
Q : Quel est le public cible de ce document ? R : Jury académique (soutenance)
Q : Qu'est-ce que tu veux inclure ? (Sélectionnez toutes les réponses applicables) R : Contexte et objectifs, Cas d'utilisation, Diagrammes de classes, Architecture technique
Q : Dans quelle langue ? R : Français
12 mai
Parfait. Je lis d'abord le skill pour produire un document Word propre.
Consulté un fichier, a exécuté une commande, fichier créé
une conception du wallet? comment tu visualise les choses
12 mai
Bonne question. Avant de concevoir, laisse-moi te donner ma vision complète du wallet, puis on aligne.

Ma vision du Wallet
Le wallet c'est un compte bancaire simplifié, embarqué dans l'application. Mais dans notre contexte multi-tenant, c'est plus subtil que ça.

Ce que je vois comme les 3 grandes questions de conception
Question 1 — Qu'est-ce qu'un wallet exactement ?
Je vois deux natures possibles qui coexistent :
Le wallet monétaire — il contient de la vraie valeur (FCFA, EUR…). On peut le recharger avec MTN Mobile Money, Orange Money ou Stripe. On peut l'utiliser pour payer un trajet, un ticket, une location. On peut en retirer l'argent vers son compte mobile money. C'est de l'argent réel.
Le wallet points — c'est la même structure mais avec une monnaie virtuelle non convertible. Les points s'accumulent, s'échangent contre des récompenses, mais ne sortent jamais en cash. C'est ce qu'on a déjà appelé PointsAccount.
La question est : est-ce qu'on unifie les deux dans une seule entité Wallet avec un flag type, ou est-ce qu'on garde deux classes séparées ?
Ma position : une seule classe Wallet avec type = MONETARY | POINTS. Ça simplifie la comptabilité et les transactions.

Question 2 — Comment fonctionne une transaction ?
Je vois 5 étapes obligatoires pour toute mutation du wallet :
La plateforme cliente décide de débiter Jean pour un achat. Elle envoie la demande au wallet service. Le wallet vérifie d'abord l'idempotency key — si cette demande a déjà été traitée, il retourne le résultat en cache sans retraiter. Ensuite il vérifie les règles de la WalletPolicy du tenant : est-ce que le solde est suffisant, est-ce que le plafond journalier n'est pas dépassé, est-ce que le wallet n'est pas gelé. Si tout est bon, il émet un événement Kafka. Le consommateur Kafka met à jour le solde en base de données et dans le read model Elasticsearch. Le NotificationService envoie une confirmation à Jean.
Ce qui est critique ici : le solde n'est jamais modifié directement. On écrit d'abord l'événement, puis le solde est une projection calculée depuis les événements. C'est l'event sourcing.

Question 3 — Comment gérer la recharge et le retrait avec Mobile Money ?
Recharger c'est un flux en deux temps. La plateforme initie une demande de paiement vers MTN ou Orange Money. MTN envoie un push USSD au téléphone de Jean. Jean confirme. MTN rappelle notre système via un webhook. C'est seulement à la réception du webhook de confirmation que le wallet est crédité. Avant ça, la transaction est en statut PENDING.
Le retrait c'est l'inverse. Jean demande à vider son wallet vers son compte mobile money. On initie le virement sortant. On marque le wallet en attente. On attend la confirmation de MTN ou Orange. Si ça échoue, on annule et on rembourse.

Les entités que je vois
Wallet
  ├── id, tenant_id, member_id
  ├── type : MONETARY | POINTS
  ├── currency : XAF | EUR | POINTS
  ├── balance : decimal
  ├── status : ACTIVE | FROZEN | CLOSED
  └── WalletPolicy (embedded)
        ├── daily_spend_cap
        ├── min_balance
        ├── max_topup_per_txn
        └── allowed_operations[]

WalletTransaction (immuable, jamais modifiée)
  ├── id, wallet_id, tenant_id
  ├── type : CREDIT | DEBIT | REVERSAL | TRANSFER
  ├── amount, currency
  ├── status : PENDING | COMPLETED | FAILED | REVERSED
  ├── idempotency_key
  ├── source : TOPUP | PURCHASE | REWARD | REFERRAL | WITHDRAWAL | MANUAL
  └── metadata (JSON libre : order_id, payment_ref…)

PaymentRequest (lien avec Mobile Money)
  ├── id, wallet_transaction_id
  ├── provider : MTN | ORANGE | STRIPE
  ├── external_ref (référence chez le provider)
  ├── status : INITIATED | PENDING | CONFIRMED | FAILED
  ├── initiated_at, confirmed_at
  └── raw_response (JSON)

WalletAuditLog (immuable)
  ├── wallet_id, action, actor (system | admin | member)
  ├── before_balance, after_balance
  └── reason, created_at

Les flux que je vois
Flux 1 : Recharge via Mobile Money
Membre demande recharge
  → PaymentRequest créé (INITIATED)
  → Appel MTN API → push USSD sur le téléphone
  → PaymentRequest passe à PENDING
  → Jean confirme sur son téléphone
  → MTN envoie webhook de confirmation
  → WalletTransaction créée (CREDIT, COMPLETED)
  → Solde mis à jour
  → Notification envoyée à Jean
Flux 2 : Paiement sur la plateforme
Jean achète un ticket sur EventaaS
  → EventaaS envoie POST /wallet/debit
  → Idempotency check
  → WalletPolicy vérifie : solde OK, plafond OK, wallet actif
  → WalletTransaction créée (DEBIT, COMPLETED)
  → Solde mis à jour
  → Kafka event → Fidelisation Module (peut déclencher des points)
  → Notification à Jean : "Paiement de 5000 XAF effectué"
Flux 3 : Transfert inter-wallets (entre tenants)
Jean a du crédit chez RidnGo,
il veut l'utiliser chez KSM
  → Transfer request : wallet_source → wallet_dest
  → Deux transactions atomiques : DEBIT sur source, CREDIT sur dest
  → Si l'une échoue : les deux sont annulées (saga)
Flux 4 : Gel pour fraude
Système détecte 10 débits en 30 secondes
  → WalletFraudSuspectedEvent émis
  → Wallet status → FROZEN
  → Admin notifié
  → Toutes les transactions refusées jusqu'à décision admin

Ce que je veux qu'on tranche ensemble
Point A — Le wallet monétaire et les points sont-ils la même entité ou deux entités séparées ?
Point B — Les transferts inter-tenants sont-ils autorisés ? Jean peut-il dépenser son crédit RidnGo chez KSM ?
Point C — Le retrait vers Mobile Money est-il dans le périmètre du projet ou on le laisse pour plus tard ?
Point D — Est-ce qu'un wallet peut avoir plusieurs devises (XAF + EUR) ou un wallet = une devise ?
Dis-moi comment tu vois ces quatre points et on passe à la conception formelle.
pour la conception generale, genere le diagramme de classe en png, ainsi que le diagramme d'etat transition, et d'activité pour ceux necessaires.
donc cette question demande une connaisance de tout le systeme reflechi
12 mai
Laisse-moi d'abord réfléchir à ce qui est nécessaire avant de générer quoi que ce soit.

Ce que je vais produire et pourquoi
Diagramme de classes — 1 seul, vue complète du système avec toutes les entités et leurs relations.
Diagrammes d'état-transition — uniquement pour les entités qui ont un cycle de vie non trivial. Je vois 5 candidates :
Wallet (ACTIVE → FROZEN → CLOSED)
WalletTransaction (PENDING → COMPLETED → REVERSED)
RewardGrant (PENDING → ACTIVE → USED / EXPIRED)
Rule (DRAFT → ACTIVE → SUSPENDED → ARCHIVED)
ReferralEvent (PENDING → CONVERTED → REWARDED / EXPIRED)
Diagrammes d'activité — pour les flux métier complexes avec des branchements. Je vois 3 candidats :
Traitement d'un Event entrant (le flux central du moteur)
Recharge du Wallet via Mobile Money
Échange de points contre une récompense
Ça fait 9 diagrammes. Je les génère tous en PNG maintenant.
Exécuté 6 commandes, 3 fichiers créés
Les 3 fichiers sont générés. Voici ce que tu as.
Presented 3 files

Ce qui a été produit
01 — Diagramme de classes : les 14 classes réparties en 4 domaines (Plateforme, Identité, Moteur, Récompenses/Wallet), avec tous les attributs typés et les relations (composition, agrégation, association, dépendance).
02 — Diagrammes d'état-transition : 5 entités dont le cycle de vie est non trivial — Wallet, WalletTransaction, RewardGrant, Rule, et ReferralEvent. Chaque état a ses transitions avec les conditions de déclenchement.
03 — Diagrammes d'activité : 3 flux métier centraux — le traitement d'un event entrant (le flux principal du moteur), la recharge wallet via Mobile Money (flux en deux temps avec webhook), et l'échange de points contre une récompense (flux de rédemption avec débit atomique).
Tu veux qu'on intègre ces images dans le document Word de conception ?
01 class diagram
Image · PNG 
02 state diagrams
Image · PNG 
03 activity diagrams
Image · PNG 
je veux qu'on brainstorm sur la conception du wallet.
21 mai
Parfait. Reprenons les 4 questions que j'avais posées et allons plus loin.

Les 4 questions fondamentales
Question 1 — Wallet monétaire et wallet points : une seule entité ou deux ?
Ma position initiale était "une seule entité avec un flag type". Mais en réfléchissant plus profondément, je change d'avis.
Ce sont deux bêtes fondamentalement différentes. Le wallet monétaire touche à de l'argent réel — il faut des logs de conformité, des règles KYC, des intégrations avec des providers de paiement externes, une gestion des devises, des taux de change. Le wallet points est une monnaie virtuelle propriétaire — pas de provider externe, pas de KYC, pas de conversion réelle. Mélanger les deux dans une même table crée de la confusion et des risques de sécurité.
Ma position finale : deux entités séparées. Wallet pour le monétaire. PointsAccount pour les points. Elles partagent le concept de "solde" mais pas grand chose d'autre.

Question 2 — Transfert inter-tenants : oui ou non ?
Imaginons Jean a 10 000 XAF dans son wallet RidnGo. Il veut payer un ticket sur EventaaS. Est-ce qu'on autorise ça ?
Techniquement c'est faisable. Mais ça crée un problème métier énorme : RidnGo n'a aucune envie que son argent "fuie" vers EventaaS. Chaque tenant a financé ses propres récompenses et cashbacks. Laisser les fonds circuler librement entre tenants détruit l'incitation de chaque tenant à investir dans son programme de fidélité.
Ma position : pas de transfert inter-tenants par défaut. Mais on peut prévoir un mécanisme opt-in où deux tenants signent un accord de portabilité et notre système gère la compensation entre leurs wallets.

Question 3 — Le retrait vers Mobile Money : dans le périmètre ou pas ?
C'est la question la plus délicate. Autoriser le retrait transforme notre wallet en quasi-compte bancaire. En Afrique centrale, ça tombe sous la réglementation COBAC et potentiellement les licences d'établissement de monnaie électronique.
Ma position : retrait dans le périmètre, mais conditionnel. Le tenant doit activer la fonctionnalité explicitement dans sa config. Le membre doit avoir passé le KYC. On impose un délai de 24h entre le crédit et le retrait pour limiter la fraude. Et on passe par les APIs MTN/Orange plutôt que de toucher à l'argent directement — ce sont eux qui portent la licence.

Question 4 — Un wallet = une devise ou multi-devises ?
Un wallet multi-devises c'est séduisant mais c'est un enfer opérationnel. Qui gère le taux de change XAF → EUR ? Au moment du débit ou du crédit ? Que se passe-t-il si le taux fluctue entre le crédit et l'utilisation ?
Ma position : un wallet = une devise. Si un tenant opère en plusieurs devises, il configure plusieurs wallets pour ses membres. La conversion éventuelle est gérée en dehors de notre système.

Ce que je vois comme les vrais sujets de conception
Maintenant que les questions de base sont tranchées, voilà ce qui mérite vraiment un brainstorm approfondi.

Sujet A — La structure du solde
Le solde d'un wallet, c'est quoi exactement ?
Option 1 — Solde calculé : le solde n'est jamais stocké directement. Il est toujours recalculé comme la somme de toutes les transactions. C'est l'event sourcing pur. Avantage : impossible d'avoir un solde incohérent. Inconvénient : recalculer depuis zéro à chaque lecture devient lent quand un membre a des milliers de transactions.
Option 2 — Solde matérialisé : on stocke le solde courant directement sur l'entité Wallet, et on le met à jour à chaque transaction. Avantage : lecture instantanée. Inconvénient : si une mise à jour échoue à mi-chemin, le solde est incohérent avec les transactions.
Option 3 — Hybride avec snapshot : on stocke le solde courant pour les lectures rapides, mais on garde toutes les transactions immuables pour l'audit et la vérification. Périodiquement (ou à la demande), on recalcule le solde depuis les transactions et on vérifie qu'il correspond au solde matérialisé. C'est ce que font les vraies banques.
Je penche fortement pour l'option 3.

Sujet B — La gestion de la concurrence
C'est le problème le plus difficile du wallet. Que se passe-t-il quand Jean déclenche deux achats en même temps depuis deux appareils différents ? Les deux requêtes lisent un solde de 5000 XAF. Les deux valident que c'est suffisant pour un achat de 3000 XAF. Les deux débitent 3000 XAF. Jean se retrouve avec -1000 XAF.
Trois approches possibles :
Optimistic locking : chaque transaction porte un numéro de version. Au moment du débit, on vérifie que la version n'a pas changé depuis la lecture. Si elle a changé, on recommence. Simple à implémenter, mais peut créer des boucles de retry en cas de forte contention.
Pessimistic locking : on pose un verrou exclusif sur le wallet avant de lire. Une seule transaction à la fois. Garantit la cohérence mais crée des goulots d'étranglement.
Queue per wallet : toutes les opérations d'un wallet sont sérialisées dans une queue dédiée. Un seul worker traite les opérations d'un wallet à la fois. C'est l'approche la plus scalable pour les systèmes haute fréquence.
Je vois la queue par wallet comme la meilleure option pour notre contexte Kafka.

Sujet C — Les limites et la WalletPolicy
Chaque tenant doit pouvoir configurer ses propres règles sur les wallets de ses membres. Voilà ce que je vois comme limites nécessaires :
Le plafond de solde : un wallet ne peut pas dépasser X XAF. Utile pour les plateformes qui ne veulent pas que leurs clients accumulent trop de crédit non utilisé.
Le plafond de recharge par transaction : on ne peut pas recharger plus de Y XAF en une seule opération. Limite la fraude et le blanchiment.
Le plafond de dépense journalier : total des débits limité à Z XAF par jour. Protection anti-fraude.
Le solde minimum : le wallet ne peut pas descendre en dessous de W XAF. Utile pour les plateformes qui facturent des frais récurrents.
Le délai de retrait : on ne peut retirer que des fonds crédités depuis plus de N heures. Evite la fraude dépôt-retrait immédiat.
La liste des opérations autorisées : certains tenants veulent un wallet uniquement en dépense (pas de retrait). D'autres veulent uniquement de la recharge interne (pas de top-up Mobile Money).

Sujet D — Le cycle de vie et les états
Je vois plus d'états que ce que j'avais mis dans le diagramme initial.
PENDING_KYC
    ↓ (KYC validé)
ACTIVE
    ↓ (retrait > seuil)        ↓ (fraude détectée)
PENDING_VERIFICATION         FROZEN
    ↓ (vérif OK)                ↓ (admin)
ACTIVE ←─────────────────── ACTIVE ou CLOSED
    ↓ (clôture volontaire ou inactivité longue)
CLOSED
L'état PENDING_KYC est important — un nouveau membre peut avoir un wallet avec des opérations limitées (recevoir des credits, pas de retrait) jusqu'à validation de son identité.

Sujet E — La comptabilité en partie double
C'est un sujet que la plupart des systèmes de wallet ignorent et qui leur coûte cher plus tard. En comptabilité réelle, chaque mouvement d'argent a deux faces : un débit quelque part et un crédit ailleurs. Si Jean recharge son wallet de 5000 XAF depuis MTN, il faut enregistrer :
Débit du compte "Fonds clients RidnGo" : +5000 XAF
Crédit du wallet de Jean : +5000 XAF
Ca permet à tout moment de vérifier que la somme de tous les wallets actifs est égale aux fonds réellement détenus. Si ce n'est pas le cas, il y a un bug ou une fraude quelque part.
Est-ce qu'on va aussi loin dans notre conception ? C'est la question.

Sujet F — Les reversals et remboursements
Que se passe-t-il quand un achat est annulé après paiement ? Trois options :
Créer une nouvelle transaction de type REVERSAL qui crédite le montant. La transaction originale reste intacte. C'est l'approche la plus propre comptablement.
Modifier le statut de la transaction originale à REVERSED et créer une transaction miroir. Plus traçable mais plus complexe.
Supprimer la transaction et recalculer. Jamais. Une transaction immuable ne se supprime pas.
Je penche pour la première option : une transaction REVERSAL indépendante, avec une référence vers la transaction originale.

Les questions ouvertes que je te pose
Avant qu'on décide de la conception finale, j'ai besoin de tes réponses sur ces points :
A — Est-ce que les membres peuvent se transférer de l'argent entre eux au sein d'un même tenant ? (ex: Jean envoie 2000 XAF à Marie sur RidnGo)
B — Est-ce qu'un wallet peut être partagé entre plusieurs membres ? (ex: wallet famille, wallet entreprise avec plusieurs utilisateurs)
C — Est-ce que notre système doit gérer les frais de transaction ? (ex: 1% de frais sur chaque recharge Mobile Money)
D — Est-ce qu'on doit supporter le wallet offline ? (transactions sans connexion, synchronisées plus tard)
E — Est-ce que la comptabilité en partie double est dans le périmètre du projet ou on laisse ça aux ERP des tenants ?
Ces cinq réponses vont déterminer toute l'architecture interne du wallet.
pour la question 1, pourquoi ne pas slm convertir l'argent en monaie virtuelle, qui pourra ensuite etre utiliser dans l'appli
21 mai
Bonne intuition. C'est exactement ce que font Lyft Credits, Uber Cash ou les étoiles Starbucks.

Ce que tu proposes concrètement
L'utilisateur recharge avec de l'argent réel (MTN, Orange, Stripe). Dès que l'argent entre, il est immédiatement converti en monnaie virtuelle. À partir de là, le système ne manipule plus jamais d'argent réel en interne — uniquement des unités virtuelles.
Jean recharge 5000 XAF via MTN
        ↓
Système reçoit confirmation MTN
        ↓
5000 XAF → convertis en 5000 "YowCoins" (taux 1:1)
        ↓
Le wallet de Jean affiche 5000 YowCoins
        ↓
Jean paie un trajet RidnGo : 1500 YowCoins débités
Jean achète un ticket EventaaS : 2000 YowCoins débités
L'argent réel ne circule plus dans notre système après la conversion. Il est "dehors", chez MTN ou Orange. Nous on gère uniquement des unités virtuelles.

Ce que ça change fondamentalement
Ce que ça simplifie énormément :
Le système n'a plus besoin de gérer plusieurs devises en interne. Tout est en YowCoins. Pas de taux de change, pas de conversion XAF → EUR à gérer. Un seul type de solde pour tous les membres de tous les tenants.
La réglementation devient beaucoup plus simple. Techniquement on ne "détient" pas d'argent — on vend des unités virtuelles contre de l'argent, comme un ticket de manège. C'est MTN et Orange qui détiennent les fonds réels. Nous on gère juste les crédits.
Les points de fidélité et le wallet deviennent la même chose. Les points gagnés par fidélité sont des YowCoins. L'argent rechargé devient des YowCoins. Jean peut utiliser indifféremment ses YowCoins qu'il les ait gagnés par fidélité ou rechargés en cash. C'est l'unification totale.
Jean a 5000 YowCoins
  → dont 3000 viennent d'une recharge MTN
  → dont 2000 viennent de points de fidélité
  → il les dépense sans distinction
Ce que ça complique :
Le retrait devient une vraie question. Si Jean veut retirer ses YowCoins en cash, il faut reconvertir — et là on retouche à de l'argent réel. Est-ce qu'on autorise la reconversion ? À quel taux ? Avec quels frais ?
Si le taux de conversion n'est pas 1:1, ça devient sensible. "J'ai rechargé 5000 XAF et j'ai seulement 4750 YowCoins" — le client va se sentir lésé si ce n'est pas clairement expliqué.
La valeur perçue dépend du taux. Si 1 YowCoin = 1 XAF, c'est transparent. Si le taux est différent, il faut communiquer dessus.

Le modèle qui en découle
Du coup, on n'a plus qu'une seule entité wallet avec une seule monnaie virtuelle. Le PointsAccount séparé disparaît. Tout est unifié.
Wallet
  ├── id, member_id, tenant_id
  ├── balance : Int  (en YowCoins, jamais en XAF)
  ├── status : ACTIVE | FROZEN | CLOSED
  └── WalletPolicy

WalletTransaction  (immuable)
  ├── type : CREDIT | DEBIT | REVERSAL
  ├── amount : Int  (YowCoins)
  ├── source : TOPUP_MTN | TOPUP_ORANGE | LOYALTY_REWARD
  │             | REFERRAL | PURCHASE | CAMPAIGN_BONUS
  └── exchange_rate : Decimal?  (taux utilisé à la conversion si TOPUP)

PaymentRequest  (lien avec le monde réel)
  ├── real_amount : Decimal  (5000 XAF)
  ├── real_currency : String  (XAF)
  ├── virtual_amount : Int  (5000 YowCoins)
  ├── exchange_rate : Decimal  (1.0)
  └── provider : MTN | ORANGE | STRIPE

La question qui reste ouverte
Est-ce que le taux de conversion est fixé par nous (1:1 pour tout le monde) ou est-ce que chaque tenant peut le configurer ?
Si chaque tenant peut le configurer, RidnGo peut décider que 1000 XAF = 1200 RidnCoins (pour donner l'impression de plus de valeur), et KSM peut décider que 1000 XAF = 800 KsmCoins (pour réduire les dépenses de fidélité).
Ça veut dire que les YowCoins ne sont plus universels — chaque tenant a sa propre monnaie virtuelle. On revient à l'isolation par tenant, mais de manière plus propre.
C'est probablement la bonne architecture : chaque tenant a sa propre monnaie virtuelle avec son propre taux de conversion. L'universalité n'est pas nécessaire — ce qui compte c'est que chaque membre ait une expérience cohérente au sein d'une même plateforme.
Qu'est-ce que tu en penses ? On fixe le taux à 1:1 pour simplifier, ou on laisse chaque tenant configurer le sien ?
c'est quoi le KYC? l'idempotence? pour la multidevise? je ne vois pas ou est le probleme, c'est pas juste comme configurer anglais-francais sur la langue?
21 mai
KYC — Know Your Customer
C'est le processus de vérification d'identité d'un utilisateur avant de lui permettre certaines opérations financières.
Concrètement, quand Jean veut retirer de l'argent de son wallet vers MTN Mobile Money, la loi exige qu'on sache qui est Jean. Alors on lui demande une photo de sa CNI, un selfie, parfois une preuve de domicile. On vérifie que ce sont de vrais documents. Seulement après, on autorise le retrait.
C'est obligatoire partout dans le monde dès qu'on touche à de l'argent réel. Les banques le font, MTN le fait, Orange le fait. Si on permet les retraits, on doit le faire aussi.
Dans notre système ça se traduit par l'état PENDING_KYC du wallet — le membre peut recevoir des crédits mais ne peut pas retirer tant qu'il n'a pas prouvé son identité.

Idempotence
C'est le fait qu'exécuter la même opération plusieurs fois donne le même résultat qu'une seule fois.
Le problème concret : Jean appuie sur "Payer 5000 XAF". Le réseau coupe. L'application ne sait pas si le paiement est passé ou non. Elle renvoie la même requête. Sans idempotence, Jean est débité deux fois.
La solution : chaque requête de paiement porte un identifiant unique (idempotency_key). Si le système reçoit deux fois la même clé, il retourne le résultat du premier traitement sans retraiter. Jean n'est débité qu'une seule fois, peu importe combien de fois la requête est envoyée.
C'est critique dans tout système de paiement. MTN et Stripe l'implémentent tous les deux.

La multidevise — pourquoi c'est différent d'une langue
Ta comparaison est intuitive mais il y a une différence fondamentale.
Changer la langue c'est changer l'affichage. Le mot "Purchase" devient "Achat". La donnée en base ne change pas du tout. C'est purement cosmétique.
Changer de devise c'est changer la valeur réelle stockée. Et là les problèmes commencent.

Problème 1 — Quel montant stocker en base ?
Jean recharge 5000 XAF. Marie recharge 10 EUR. Est-ce qu'on stocke 5000 XAF et 10 EUR séparément ? Ou est-ce qu'on convertit tout dans une devise pivot et on stocke 5000 et 6550 (si 1 EUR = 655 XAF) ?
Si on stocke dans chaque devise, deux wallets de devises différentes ne sont pas comparables. Si on convertit, quel taux on utilise ? Celui du jour de la recharge ? Du jour de la dépense ?

Problème 2 — Le taux change tous les jours
Jean recharge 10 EUR quand 1 EUR = 655 XAF. Il a 6550 XAF en équivalent. Trois mois plus tard il veut dépenser, et 1 EUR = 680 XAF. Son solde "vaut" maintenant 6800 XAF. On lui crédite la différence ? On l'ignore ? Qui absorbe la perte ou le gain ?
Avec une langue, ce problème n'existe pas. "Purchase" reste "Purchase" peu importe le contexte.

Problème 3 — La dépense dans une devise différente
Jean a rechargé en EUR. Il veut payer un service facturé en XAF. On convertit au moment de la dépense ? Avec quel taux ? Qui fournit le taux en temps réel ? Il faut intégrer une API de taux de change (Fixer.io, Open Exchange Rates…) et gérer les cas où l'API est indisponible.

Ce que ça donne concrètement dans le code
Avec une seule devise ou une monnaie virtuelle, une transaction c'est :
amount : 5000
currency : YOWCOIN
Avec multidevise, une transaction c'est :
amount_original : 10
currency_original : EUR
amount_stored : 6550
currency_stored : XAF
exchange_rate : 655.0
rate_source : ECB
rate_timestamp : 2026-03-15T09:00:00Z
Et chaque opération doit gérer la conversion, les arrondis, les écarts de taux, et les cas d'échec de l'API de taux.

Donc la vraie réponse à ta question
La multidevise c'est faisable. Mais c'est 5 à 10 fois plus de complexité qu'une monnaie unique. Pour un projet académique en semestre 2, la question c'est : est-ce que cette complexité apporte de la valeur métier réelle, ou est-ce qu'on fait payer XAF partout et on passe à autre chose ?
Mon conseil : monnaie virtuelle unique par tenant, taux de conversion configurable à la recharge, on n'expose jamais plusieurs devises en interne. Si RidnGo opère au Cameroun en XAF, tout est en RidnCoins avec taux 1:1. Si demain ils s'étendent au Nigeria, ils créent un tenant séparé avec NairaCoins. Simple, propre, scalable.
quel sont d'autre probleme du wallet?
21 mai
Bonne question. Voilà tous les problèmes réels d'un wallet que la plupart des équipes découvrent trop tard.

1. Le problème de la cohérence du solde
C'est le problème le plus silencieux et le plus dangereux.
Imagine que le système crédite le wallet de Jean et en même temps une panne réseau coupe la connexion avant que la transaction soit confirmée en base. Est-ce que Jean a été crédité ou pas ? La base dit non, MTN dit oui. Le solde réel et le solde affiché divergent.
Ce problème s'appelle le split-brain. Dans un système distribué avec Kafka, PostgreSQL et Redis qui tournent en parallèle, les trois peuvent avoir des visions différentes du solde de Jean à un instant T.
La solution c'est de définir une seule source de vérité et de tout reconstruire depuis elle. Soit les transactions en base sont la vérité absolue et le solde affiché en est une projection. Soit le solde est la vérité et les transactions sont des logs. Les deux approches ont des implications très différentes.

2. Le problème des fonds bloqués
Jean a 5000 YowCoins. Il lance deux achats simultanément de 3000 YowCoins chacun depuis deux onglets. Les deux lectures voient 5000. Les deux validations passent. Les deux débits s'exécutent. Jean se retrouve à -1000.
On en a parlé avec le locking. Mais le vrai problème c'est ce qui se passe entre la lecture du solde et l'écriture du débit. C'est cette fenêtre de quelques millisecondes qui crée tous les problèmes. Plus le système est distribué et réactif (WebFlux, R2DBC, Kafka), plus cette fenêtre est dangereuse parce que tout est non-bloquant et concurrent.

3. Le problème des fonds fantômes
Un tenant configure une campagne double points. Le système crédite 10 000 YowCoins à 50 000 membres en même temps. En quelques secondes, il y a 500 millions de YowCoins créés de nulle part.
Si ces YowCoins sont convertibles en argent réel, le tenant doit avoir les fonds pour couvrir. Est-ce qu'on vérifie que le tenant a les fonds avant de lancer la campagne ? Est-ce qu'on lui envoie une alerte quand la masse de YowCoins en circulation dépasse un seuil ? Est-ce qu'on plafonne les campagnes ?
Sans ce contrôle, un tenant peut créer une dette virtuelle infinie et ne jamais pouvoir honorer les retraits.

4. Le problème de l'expiration
Les points expirent. Mais comment exactement ?
Expiration totale : tous les points expirent au bout de 12 mois sans activité. Simple mais brutal — Jean se connecte le lendemain de l'expiration et perd tout. Mauvaise expérience.
Expiration par batch : les points gagnés en janvier expirent en janvier de l'année suivante, ceux de février en février... Il faut alors tracker chaque "lot" de points avec sa propre date d'expiration. Le solde affiché est en réalité une somme de plusieurs sous-soldes avec des dates différentes.
Expiration glissante : toute activité repousse l'expiration. Tant que Jean fait un achat par mois, ses points ne expirent jamais.
Chaque modèle a des implications comptables différentes. Et il faut envoyer des notifications avant expiration — 30 jours avant, 7 jours avant, le jour J. Ça crée une mécanique de scheduler non triviale.

5. Le problème du remboursement partiel
Jean achète un trajet à 3000 YowCoins. Il paie 2000 depuis son wallet et 1000 depuis une réduction. Le trajet est annulé. On lui rembourse combien ? Les 3000 ? Seulement les 2000 du wallet ? Et la réduction, elle revient sous quelle forme ?
Ou autre cas : Jean achète 5 articles pour 10 000 YowCoins. Il en retourne 2. On lui rembourse 40% ? Mais comment calculer précisément quand il y avait peut-être un code promo appliqué sur le total ?
Le remboursement partiel est un cauchemar comptable que beaucoup de systèmes gèrent mal ou pas du tout.

6. Le problème de la fraude
Voilà les patterns de fraude les plus courants sur un wallet :
Le dépôt-retrait rapide : quelqu'un recharge 100 000 XAF, retire immédiatement, recommence. Si le système de paiement a un délai de confirmation et que notre wallet crédite en avance, il peut retirer de l'argent qui n'est pas encore confirmé.
Le multi-compte : une personne crée 50 comptes, collecte les bonus d'inscription sur chacun, puis transfère tout vers un compte principal.
Le referral abuse : une personne parraine elle-même avec des numéros de téléphone différents pour collecter les bonus de parrainage en boucle.
Le chargeback : Jean paie avec une carte Stripe, reçoit ses YowCoins, dépense tout, puis conteste le paiement auprès de sa banque. Stripe nous retire l'argent. Nous on a déjà crédité et les YowCoins sont dépensés.
Chaque pattern demande une réponse technique différente — délais de retrait, détection d'anomalies, vérification de l'unicité des comptes via KYC, gel préventif.

7. Le problème de l'audit et de la réconciliation
À la fin du mois, le tenant veut savoir exactement combien de YowCoins ont été distribués, dépensés, expirés, remboursés. Le total des wallets actifs correspond-il aux fonds réels détenus ?
Si la réponse n'est pas immédiate et exacte, c'est qu'il y a un problème de conception. Beaucoup de systèmes ne peuvent pas répondre à cette question sans lancer une requête qui tourne pendant des heures sur des millions de transactions.
La réconciliation doit être un processus quotidien automatique, pas une opération manuelle de crise.

8. Le problème de la scalabilité des lectures
Un wallet très actif peut avoir des dizaines de milliers de transactions. Afficher l'historique devient lent. Calculer le solde depuis les transactions devient lent. Filtrer par date, par type, par montant devient lent.
Il faut penser dès la conception à la pagination, aux index, aux projections dénormalisées pour les lectures fréquentes — sans toucher à l'immuabilité des transactions.

9. Le problème de la notification en temps réel
Jean fait un achat. Il s'attend à voir son solde se mettre à jour instantanément dans l'application. Mais dans une architecture event-driven avec Kafka, la mise à jour du solde est asynchrone — elle peut prendre quelques secondes.
Pendant ces quelques secondes, Jean voit encore l'ancien solde. S'il rafraîchit, il voit le nouveau. S'il ne rafraîchit pas, il croit que l'opération a échoué et recommence.
Comment garantir que l'interface affiche toujours la bonne valeur sans sacrifier les performances du système asynchrone ? C'est le problème de la cohérence éventuelle visible par l'utilisateur.

10. Le problème des limites et des cas limites
Quelques scénarios que la plupart des équipes n'anticipent pas :
Un membre essaie de dépenser exactement son solde restant — 0 après débit, pas négatif. Ça doit marcher. Mais avec les arrondis sur les conversions de taux, est-ce qu'on arrive vraiment à 0 exact ?
Un wallet est gelé pendant qu'une transaction est en cours. Est-ce qu'on complète la transaction en cours ou on l'annule ?
Un tenant est suspendu (impayé). Que se passe-t-il avec les wallets de ses membres ? On gèle tout ? On laisse les membres dépenser mais plus recharger ?
Un membre supprime son compte. Ses YowCoins disparaissent ? On les rembourse ? Pendant combien de temps garde-t-on les données pour la conformité réglementaire ?

Résumé — les 10 problèmes
#
Problème
Niveau de difficulté
1
Cohérence du solde
★★★★★
2
Concurrence et race conditions
★★★★★
3
Fonds fantômes (campagnes)
★★★★☆
4
Expiration des points
★★★☆☆
5
Remboursement partiel
★★★★☆
6
Fraude
★★★★★
7
Audit et réconciliation
★★★☆☆
8
Scalabilité des lectures
★★★☆☆
9
Cohérence éventuelle (UX)
★★★☆☆
10
Cas limites
★★☆☆☆

Lesquels tu veux qu'on creuse pour décider comment les adresser dans la conception ?
brainstormmons maintenant sur la conception de la promo
21 mai
Bonne transition. La promo c'est le module le plus visible pour l'utilisateur final mais le plus complexe à concevoir correctement.

D'abord, c'est quoi une "promo" exactement ?
Avant de concevoir, il faut clarifier le périmètre. Le mot "promo" recouvre des réalités très différentes.
Ce que les gens appellent promo :
Un code promo — Jean entre "NOEL25" au checkout et obtient 25% de réduction. Usage unique ou multiple, avec ou sans conditions.
Un bonus de bienvenue — tout nouveau membre reçoit 500 YowCoins à l'inscription. Automatique, pas de code à entrer.
Un bonus de fidélité — après 10 achats, le 11ème est gratuit. C'est une règle du moteur qu'on a déjà conçue.
Une campagne temporisée — double points tous les vendredis. Activée et désactivée automatiquement.
Une offre flash — 50% sur les tickets EventaaS pendant 2 heures. Limitée dans le temps et en stock.
Un bundle — achète 3 trajets, le 4ème offert. Logique de panier.
Une promotion ciblée — seulement pour les membres Gold, seulement pour les membres inactifs depuis 30 jours, seulement pour les membres d'une certaine ville.
La vraie question de conception : est-ce que toutes ces choses sont des instances d'un même concept générique, ou est-ce que ce sont des entités différentes ?

Ce que font les systèmes existants
Shopify Discounts — distingue 4 types fixes : pourcentage, montant fixe, livraison gratuite, achetez-X-obtenez-Y. Simple mais peu flexible.
Stripe Coupons — un coupon a un type (percent_off ou amount_off), une durée, un nombre d'utilisations max. Très générique mais pas de logique de ciblage.
Voucherify — le plus avancé. Tout est une "campagne" avec des règles de validation composables. Très puissant mais très complexe.
Ce qu'on doit retenir : les systèmes simples sont faciles à implémenter mais bloquants dès qu'un tenant veut quelque chose d'un peu différent. Les systèmes génériques sont flexibles mais difficiles à configurer pour un admin non-technique.

Ma vision : trois niveaux de promo
Je vois trois niveaux distincts qui coexistent dans notre système.

Niveau 1 — Le code promo (PromoCampaign)
C'est la promo classique. Un code que l'utilisateur entre manuellement. La complexité vient des règles qui gouvernent son utilisation.
Ce qu'un code promo doit pouvoir exprimer :
"NOEL25"
  → réduction de 25%
  → sur tout achat
  → valable du 24 au 26 décembre
  → maximum 1000 utilisations au total
  → maximum 1 utilisation par membre
  → seulement pour les membres inscrits avant le 1er décembre
Chaque ligne de cet exemple est une dimension de configuration indépendante.
Les types de réduction possibles :
Réduction en pourcentage — 25% sur le montant total. Simple mais il faut définir sur quoi s'applique le pourcentage — le sous-total, le total avec frais, certaines catégories seulement.
Réduction en montant fixe — 2000 XAF de réduction. Problème : que se passe-t-il si la commande est inférieure à 2000 XAF ? On autorise le solde négatif ? On plafonne à 0 ?
Cashback en YowCoins — pas de réduction sur le prix, mais X YowCoins crédités après l'achat. Plus engageant pour la fidélité car l'utilisateur revient dépenser.
Produit ou service gratuit — le prochain trajet est offert, le prochain ticket est gratuit. C'est un RewardGrant déguisé en promo.
Livraison gratuite ou frais offerts — pertinent pour KSM eShop, moins pour RidnGo.
Les conditions d'éligibilité :
Conditions possibles sur le membre :
  - tier_is : Gold, Silver…
  - enrolled_before : date
  - enrolled_after : date
  - segment_in : [VIP, nouveau, inactif]
  - has_not_used_promo : true
  - min_total_spent : 50000 XAF

Conditions possibles sur la commande :
  - min_order_amount : 5000 XAF
  - product_category_in : [premium, food]
  - first_order_only : true
  - nth_order : 5

Conditions possibles sur le temps :
  - valid_from / valid_until
  - day_of_week_in : [FRIDAY, SATURDAY]
  - hour_between : 18h - 20h
Les limites d'utilisation :
Limite globale — le code peut être utilisé X fois au total. Quand le stock est épuisé, le code est désactivé automatiquement.
Limite par membre — chaque membre peut l'utiliser Y fois maximum.
Limite par période — chaque membre peut l'utiliser Z fois par semaine.

Niveau 2 — La campagne automatique (AutoCampaign)
Pas de code à entrer. La promotion s'applique automatiquement quand les conditions sont remplies, de manière transparente pour l'utilisateur.
Exemples :
Tous les vendredis, les points gagnés sont doublés. Le système l'applique sans que l'utilisateur fasse quoi que ce soit.
Tout nouveau membre reçoit 500 YowCoins à l'inscription. Déclenché par l'event member.enrolled.
Tout membre inactif depuis 30 jours reçoit un code promo de relance. Déclenché par un scheduler qui scanne les membres inactifs.
La différence avec le moteur de règles qu'on a déjà :
La campagne automatique c'est en réalité une Rule avec une période de validité. On les a déjà dans notre moteur. Donc techniquement ce niveau n'est pas une nouvelle entité — c'est une Rule avec valid_from et valid_until et un flag is_campaign.
Ce qui est nouveau c'est l'interface de création dans le dashboard — plus simple, plus visuelle, pensée pour un admin non-technique qui ne veut pas configurer des triggers et des conditions manuellement.

Niveau 3 — L'offre personnalisée (TargetedOffer)
C'est le niveau le plus avancé. Une offre générée spécifiquement pour un membre en particulier, en fonction de son comportement.
Jean n'a pas fait de trajet depuis 3 semaines. Le système lui génère automatiquement une offre personnalisée : "Revenez cette semaine et bénéficiez de 30% sur votre prochain trajet."
Marie est cliente Gold depuis 6 mois. Le système lui envoie une offre exclusive : "En tant que membre Gold, bénéficiez de 500 YowCoins offerts sur votre prochain événement EventaaS."
Ce niveau implique de la segmentation, de la personnalisation et potentiellement du ML pour prédire quelle offre va déclencher le retour d'un membre inactif. C'est ambitieux pour le périmètre actuel mais il faut que la conception le permette pour plus tard.

Les problèmes spécifiques à la promo
Problème 1 — La combinaison de promos
Jean a un code promo "NOEL25" et il est membre Gold ce qui lui donne un multiplicateur de points x2. Est-ce qu'on applique les deux ? Dans quel ordre ? Le pourcentage s'applique avant ou après le multiplicateur ?
Il faut définir une politique de combinaison explicite pour chaque tenant :
stacking_policy :
  NONE        → un seul avantage à la fois, le plus avantageux gagne
  ADDITIVE    → on additionne tous les avantages
  SEQUENTIAL  → on applique dans un ordre de priorité défini
Sans cette politique, les tenants se retrouvent avec des situations imprévues où des utilisateurs malins combinent des promos pour obtenir des réductions de 100%.

Problème 2 — La race condition sur le stock
Un code promo est limité à 100 utilisations. 500 membres essaient de l'utiliser en même temps au moment du lancement. Comment garantir qu'on n'en valide pas 150 ?
C'est le même problème que le stock des récompenses mais avec encore plus de pression concurrentielle. Il faut un compteur atomique — idéalement dans Redis, pas dans PostgreSQL, pour la performance.
Redis :
  SET promo:NOEL25:usage_count 0
  INCR promo:NOEL25:usage_count  → retourne la nouvelle valeur
  Si valeur > usage_limit → rejeter

Problème 3 — Le promo washing
Un utilisateur crée 10 comptes différents pour utiliser 10 fois un code limité à 1 utilisation par membre.
Solutions possibles : lier l'éligibilité au numéro de téléphone vérifié (pas à l'email), imposer un KYC léger pour les promos au-delà d'un certain seuil, détecter les patterns de création de compte en série.

Problème 4 — La rétroactivité
Un admin crée une promo "valable sur tous les achats du mois de décembre". Le 15 décembre il modifie la promo et change le taux de 25% à 15%. Est-ce qu'on recalcule les réductions déjà accordées depuis le 1er décembre ?
Réponse évidente : non. Mais il faut le concevoir explicitement. Une PromoCampaign modifiée crée une nouvelle version. Les utilisations passées sont liées à l'ancienne version. C'est le versioning des promos.

Problème 5 — L'affichage du prix barré
L'interface doit afficher "Prix original : 5000 XAF → Prix avec promo : 3750 XAF". Pour ça il faut que le système retourne non seulement le montant final mais aussi le détail de chaque réduction appliquée.
PriceBreakdown {
  original_amount : 5000
  discounts : [
    { source: "PROMO_CODE", code: "NOEL25", amount: 1250, type: "PERCENT_25" }
  ]
  final_amount : 3750
}

Problème 6 — Le remboursement d'une commande avec promo
Jean achète pour 3750 XAF après une réduction de 25%. Il annule. On lui rembourse 3750 (ce qu'il a payé) ou 5000 (le prix original) ? La réduction est-elle perdue ou récupérée ?
Et si le code promo était à usage unique et qu'on annule, est-ce que l'usage est restitué ? Est-ce que Jean peut réutiliser le code ?

Le modèle de données que je vois
PromoCampaign
  ├── id, tenant_id, name, description
  ├── code : String?  (null = automatique sans code)
  ├── type : CODE | AUTO | TARGETED
  ├── discount_type : PERCENT | FIXED | CASHBACK | FREE_PRODUCT
  ├── discount_value : Decimal
  ├── stacking_policy : NONE | ADDITIVE | SEQUENTIAL
  ├── valid_from, valid_until
  ├── status : DRAFT | ACTIVE | PAUSED | EXPIRED
  ├── version : Int  (pour le versioning)
  └── parent_campaign_id : UUID?  (lien vers version précédente)

PromoEligibility  (règles d'éligibilité, 0 à N par campagne)
  ├── campaign_id
  ├── dimension : MEMBER_TIER | MEMBER_SEGMENT | ORDER_AMOUNT | FIRST_ORDER…
  ├── operator : EQ | GTE | LTE | IN
  └── value : JSONB

PromoLimit  (limites d'utilisation, 0 à N par campagne)
  ├── campaign_id
  ├── scope : GLOBAL | PER_MEMBER | PER_MEMBER_PER_PERIOD
  ├── max_uses : Int
  ├── period : DAY | WEEK | MONTH?  (pour PER_MEMBER_PER_PERIOD)
  └── current_uses : Int  (compteur atomique Redis)

PromoUsage  (historique immuable des utilisations)
  ├── id, campaign_id, member_id, tenant_id
  ├── order_reference : String
  ├── discount_applied : Decimal
  ├── campaign_version : Int
  └── used_at : Timestamp

Les questions ouvertes
A — Est-ce qu'un admin peut créer un code promo en masse ? Genre générer 10 000 codes uniques à distribuer dans des emails (PROMO-X7K2A, PROMO-B9QR3…) ou c'est toujours un seul code partagé ?
B — Est-ce qu'une promo peut s'appliquer à une catégorie de produits spécifique ou toujours sur le total de la commande ?
C — Est-ce qu'on veut un simulateur dans le dashboard — l'admin entre un contexte fictif et voit comment sa promo serait appliquée — pour tester avant de lancer ?
D — Est-ce que les promos générées automatiquement pour les membres inactifs (niveau 3) sont dans le périmètre de ce semestre ?
Ces quatre réponses vont déterminer si on conçoit un moteur simple ou un moteur expressif complet.
dans promo j'entends, les reduction, les bonus, tout ce qui est fait pour fideliser le client.
21 mai
OK. Donc la promo c'est pas juste les codes — c'est tout le système d'incentives. Ça recouvre ce qu'on a déjà conçu partiellement et ce qu'on n'a pas encore touché.
Laisse-moi cartographier tout ça proprement.

Ce que "fidéliser" veut dire concrètement
Fidéliser c'est répondre à une question simple : pourquoi Jean reviendrait sur RidnGo plutôt que d'aller chez un concurrent ?
Les réponses possibles forment exactement le périmètre de notre module promo.

Les 6 familles d'incentives
Famille 1 — Récompenser l'achat (Points & Cashback)
C'est le mécanisme le plus basique. Chaque achat rapporte quelque chose.
Points sur achat — Jean fait un trajet à 5000 XAF, il gagne 500 points. Le tenant configure le ratio.
Cashback — Au lieu de points, Jean reçoit directement des YowCoins dans son wallet. Plus concret psychologiquement — les gens comprennent mieux "tu récupères 200 XAF" que "tu gagnes 200 points".
Points bonus sur catégorie — Les trajets aéroport rapportent 2x plus de points que les trajets normaux. Le tenant veut pousser certains services.
Points sur montant dépensé — Pas par achat mais proportionnellement. Plus tu dépenses, plus tu gagnes. Favorise les gros acheteurs.
La question de conception ici c'est : points et cashback sont-ils deux mécanismes distincts ou deux faces du même mécanisme ?
Ma vision : c'est le même mécanisme. Un Effect de type CREDIT qui peut cibler soit le PointsAccount soit le Wallet. Le tenant choisit.

Famille 2 — Récompenser la régularité (Streaks & Tampons)
C'est la carte de fidélité digitale. On récompense la constance, pas juste le volume.
Tampon (Stamp) — 10 achats = 1 récompense. C'est un Counter avec un seuil. Quand le compteur atteint 10, l'effet se déclenche et le compteur repart à zéro.
Streak — Jean a commandé 7 jours de suite. Le 7ème jour il reçoit un bonus. Ici c'est un compteur de jours consécutifs — plus complexe parce qu'il faut détecter la rupture de séquence. Si Jean saute un jour, le streak repart à zéro.
Objectif périodique — "Fais 5 trajets ce mois-ci et gagne 1000 points bonus." C'est un Counter avec fenêtre temporelle (window: MONTHLY).
Le problème spécifique ici c'est la détection de rupture de streak. Il faut un job qui tourne chaque nuit, regarde les membres en streak, et remet à zéro ceux qui n'ont pas eu d'activité aujourd'hui. C'est un Scheduler qui émet des events streak.broken.

Famille 3 — Récompenser la progression (Paliers & Statuts)
C'est le système Bronze / Silver / Gold / Platinum. On donne un statut aux meilleurs clients et ce statut apporte des avantages permanents.
Avantages du palier :
Multiplicateur de points — Gold gagne 2x les points de tous ses achats. Ça s'applique automatiquement sur chaque calcul de points.
Accès prioritaire — Gold peut accéder à des récompenses exclusives du catalogue que Bronze ne voit pas.
Service premium — Gold a un support client prioritaire. C'est en dehors de notre système mais notre API expose le palier pour que la plateforme cliente s'adapte.
Réduction permanente — Gold bénéficie de 10% sur tous ses achats tant qu'il reste Gold.
Le vrai problème des paliers c'est la dégradation. Jean est Gold depuis 6 mois. Il n'a pas été actif ce trimestre. Est-ce qu'il redescend Silver ? Quand ? Avec quelle notification préalable ?
Il faut concevoir explicitement la politique de maintien du palier — pas juste la montée.
TierPolicy
  ├── upgrade_threshold : 10000 points à vie
  ├── maintain_threshold : 2000 points par trimestre
  ├── downgrade_grace_period : 30 jours d'avertissement
  └── downgrade_notification : 30j avant, 7j avant, jour J

Famille 4 — Récompenser le recrutement (Parrainage)
On l'a déjà bien conçu. Mais il y a des nuances qu'on n'a pas encore vues.
Parrainage simple — Jean recrute Marie, tous les deux reçoivent un bonus quand Marie fait son premier achat.
Parrainage multi-niveaux — Jean recrute Marie qui recrute Paul. Jean reçoit un bonus quand Paul achète aussi. C'est du MLM léger. Très efficace pour la croissance mais potentiellement problématique légalement selon les marchés.
Parrainage conditionnel — Le bonus n'est versé qu'après que Marie ait fait 3 achats, pas juste le premier. Réduit la fraude.
Leaderboard de parrainage — Les 10 meilleurs parraineurs du mois reçoivent un bonus supplémentaire. Ça crée de la compétition et de la visibilité sociale.
La question de conception ici : est-ce qu'on supporte le multi-niveaux ? C'est une décision structurante parce que ça change complètement le modèle de données du ReferralEvent.

Famille 5 — Créer l'urgence (Codes Promo & Offres Flash)
C'est la famille qu'on venait de brainstormer. Mais vue de l'angle "fidélisation", son rôle est précis : réactiver les membres inactifs et créer des pics d'engagement.
Code promo — déjà détaillé.
Offre flash — valable 2 heures seulement. La contrainte temporelle crée l'urgence. Jean ne procrastine pas — il achète maintenant ou il rate l'offre.
Offre de réactivation — Jean n'a pas commandé depuis 30 jours. Le système lui envoie automatiquement une offre personnalisée. C'est de la promo ciblée déclenchée par l'inactivité.
Happy hour — Chaque vendredi entre 17h et 19h, double points. Régulier, prévisible, crée une habitude.
La question de conception ici : qui décide quand envoyer l'offre de réactivation ? Soit l'admin configure une règle ("si inactif depuis 30 jours, envoyer cette offre"), soit le système détecte automatiquement l'inactivité. La première approche est dans notre moteur de règles. La deuxième demande un Scheduler intelligent.

Famille 6 — Créer l'engagement (Gamification)
C'est le niveau le plus avancé. On transforme l'expérience d'achat en jeu.
Badges — Jean a fait 100 trajets. Il reçoit le badge "Voyageur assidu". Les badges sont visibles sur son profil. Pas de valeur monétaire mais valeur sociale forte.
Missions — "Cette semaine : fais 3 trajets et laisse 2 avis. Récompense : 500 YowCoins." Une mission est un groupe de conditions à remplir dans un délai, avec une récompense globale à la clé.
Niveaux d'expérience — Distinct des paliers monétaires. Jean a un niveau XP qui monte avec chaque interaction (achat, avis, parrainage…). Le niveau XP débloque des cosmétiques ou des fonctionnalités, pas des réductions.
Classement — Les 10 membres les plus actifs de la semaine sont affichés. Crée de la compétition sociale.
La question de conception : est-ce que la gamification est dans notre périmètre ou on s'arrête à la famille 5 ?

Comment tout ça s'articule
Voilà comment je vois les relations entre ces 6 familles et ce qu'on a déjà conçu.
Tout part d'un Event
        ↓
    Rule Engine
        ↓
  évalue les Rules
        ↓
┌───────────────────────────────────────┐
│         Types d'Effects possibles     │
├───────────────────────────────────────┤
│ CREDIT_POINTS  → PointsAccount        │ Famille 1
│ CREDIT_WALLET  → Wallet               │ Famille 1
│ INCREMENT_COUNTER → Counter           │ Famille 2
│ GRANT_REWARD   → RewardGrant          │ Famille 3, 5
│ UPDATE_TIER    → MemberTier           │ Famille 3
│ SEND_PROMO     → PromoCampaign        │ Famille 5
│ GRANT_BADGE    → Badge                │ Famille 6
│ UPDATE_MISSION → MissionProgress      │ Famille 6
└───────────────────────────────────────┘
Ce qui est élégant dans cette vision : on n'a pas 6 systèmes séparés. On a un seul moteur de règles qui peut produire différents types d'effets. Ajouter la gamification c'est juste ajouter deux nouveaux types d'effets — GRANT_BADGE et UPDATE_MISSION — sans toucher au reste du moteur.

Ce qu'on a déjà et ce qui manque
Ce qu'on a déjà conçu :
La famille 1 est couverte par credit_points et credit_wallet dans les Effects.
La famille 2 est couverte par Counter avec seuil et fenêtre temporelle.
La famille 3 est couverte par MemberTier et update_tier — mais il manque la politique de dégradation.
La famille 4 est couverte par ReferralProgram et ReferralEvent — mais il manque le multi-niveaux.
Ce qui manque encore :
La famille 5 demande PromoCampaign, PromoEligibility, PromoLimit, PromoUsage qu'on a esquissés mais pas finalisés.
La famille 6 demande Badge, Mission, MissionProgress — pas encore touchés.

Les 3 questions structurantes
Question 1 — Le périmètre
Est-ce qu'on va jusqu'à la famille 6 (gamification) ou on s'arrête à la famille 5 ? La gamification ajoute de la complexité mais différencie vraiment le produit.
Question 2 — Le Scheduler
Plusieurs mécanismes demandent un job qui tourne en tâche de fond — détection des streaks cassés, envoi des offres de réactivation, dégradation des paliers, expiration des points. Est-ce qu'on conçoit un SchedulerService dédié ou on laisse Kafka gérer ça avec des topics à délai ?
Question 3 — La visibilité pour le membre
Comment le membre voit-il tout ça ? Un seul écran "Ma fidélité" qui regroupe points, palier, missions, badges, offres disponibles ? Ou des sections séparées ? Cette question semble UX mais elle impacte les APIs qu'on doit exposer.
Qu'est-ce que tu en penses sur ces trois points ?
ce qui se passe c'est que tout ca doit etre configurer par le tenant, donc comment est ce qu'on le permet de configurer tout ceci; wallet et promo
21 mai
Exactement. C'est la vraie question de conception. Tout ce qu'on a brainstormé jusqu'ici n'a de sens que si le tenant peut le configurer lui-même, sans toucher au code.
Réfléchissons à ça proprement.

Le problème fondamental
Le tenant est un admin non-technique. Il ouvre son dashboard et il veut dire :
"Après 10 achats de ticket premium, donne un ticket gratuit"
Mais notre système parle ce langage :
json
{
  "trigger": { "event_type": "purchase.completed", "filters": { "category": "premium" }},
  "conditions": [{ "type": "cumulative_count", "operator": "gte", "value": 10 }],
  "effects": [{ "type": "grant_reward", "params": { "reward_id": "xxx" }}]
}
Le gap entre ce que l'admin veut exprimer et ce que le système comprend, c'est exactement ce qu'on doit combler.

Comment les grands systèmes résolvent ça
Shopify — propose des templates fixes. Tu choisis parmi 5 types de remises prédéfinis. Simple mais très limité. Dès que tu veux quelque chose d'original, tu es bloqué.
Zapier — interface "Si ceci, alors cela". Très intuitif. Mais limité à des règles simples sans conditions composées.
Salesforce — éditeur de règles visuel drag-and-drop. Très puissant mais interface complexe, réservé aux experts.
Voucherify — le plus proche de notre besoin. Ils ont un "Rule Builder" avec des blocs configurables. C'est notre inspiration principale.

Ma vision : une interface en 3 couches
Plutôt qu'une seule interface qui essaie de tout faire, je vois 3 couches de configuration selon le niveau de complexité.

Couche 1 — Les templates prêts à l'emploi
Pour 80% des cas d'usage, le tenant n'a pas besoin de configurer une règle from scratch. Il choisit un template, il remplit quelques paramètres, c'est parti.
Templates disponibles :

🎯 Programme de points
   "Chaque achat rapporte X points"
   → Paramètres : X (nombre de points par achat)

🎟️ Carte de fidélité
   "Après N achats, offrir une récompense"
   → Paramètres : N (nombre d'achats), récompense choisie

💰 Cashback
   "X% du montant de chaque achat crédité en wallet"
   → Paramètres : X (pourcentage)

🎂 Bonus anniversaire
   "Offrir X points le jour de l'anniversaire du membre"
   → Paramètres : X (points offerts)

👋 Bonus de bienvenue
   "Offrir X points à l'inscription"
   → Paramètres : X (points offerts)

🤝 Parrainage simple
   "Parrain reçoit X, filleul reçoit Y à la première commande"
   → Paramètres : X, Y, montant minimum de la commande

⭐ Paliers
   "Bronze → Silver → Gold → Platinum selon les points cumulés"
   → Paramètres : seuils de chaque palier, avantages par palier

📅 Double points weekend
   "Points doublés le samedi et le dimanche"
   → Paramètres : jours concernés, multiplicateur

🔥 Offre flash
   "X% de réduction pendant N heures"
   → Paramètres : X, N, date de démarrage
Chaque template génère automatiquement la Rule, le Trigger, les Conditions et les Effects correspondants en arrière-plan. L'admin ne voit jamais ces détails techniques.

Couche 2 — Le Rule Builder visuel
Pour les cas plus complexes que les templates ne couvrent pas. L'admin construit sa règle avec des blocs visuels.
L'interface ressemble à ça :
┌─────────────────────────────────────────────────┐
│  QUAND...                                        │
│  [ Un achat est complété ▼ ]                     │
│  [ sur la catégorie : [ premium ▼ ]              │
├─────────────────────────────────────────────────┤
│  SI...                          + Ajouter condition│
│  [ Nombre d'achats ▼ ] [ ≥ ▼ ] [ 10 ]           │
│  ET                                              │
│  [ Palier du membre ▼ ] [ est ▼ ] [ Gold ▼ ]    │
├─────────────────────────────────────────────────┤
│  ALORS...                       + Ajouter effet  │
│  [ Donner une récompense ▼ ] [ Ticket premium ▼ ]│
│  ET                                              │
│  [ Envoyer une notification ▼ ] [ "Bravo !" ]   │
├─────────────────────────────────────────────────┤
│  LIMITES                                         │
│  Valable du [ 01/12/2026 ] au [ 31/12/2026 ]    │
│  Maximum [ 1 ] fois par membre                  │
└─────────────────────────────────────────────────┘
Chaque bloc "QUAND", "SI", "ALORS" est une liste déroulante avec les options disponibles. L'admin ne tape pas de JSON — il clique.

Couche 3 — L'API directe (pour les tenants techniques)
Pour les rares tenants qui ont une équipe technique et veulent automatiser la création de règles depuis leur propre système.
http
POST /api/v1/admin/rules
{
  "name": "10 achats premium → ticket gratuit",
  "trigger": { "event_type": "purchase.completed",
               "filters": { "category": "premium" }},
  "conditions": [...],
  "effects": [...]
}
C'est l'API qu'on a déjà conçue. Elle est réservée aux intégrateurs, pas à l'admin moyen.

La configuration du Wallet
La configuration du wallet est plus simple parce qu'elle n'implique pas de logique conditionnelle. C'est une liste de paramètres on/off ou numériques.
┌─────────────────────────────────────────────────┐
│  WALLET — Configuration                          │
├─────────────────────────────────────────────────┤
│  Monnaie virtuelle                               │
│  Nom de la monnaie : [ RidnCoins        ]        │
│  Symbole           : [ RC               ]        │
│  Taux de conversion : 1 XAF = [ 1 ] RC           │
│                                                  │
│  Limites                                         │
│  Solde maximum        : [ 500 000 ] RC           │
│  Recharge max/opérat. : [ 100 000 ] RC           │
│  Dépense max/jour     : [ 200 000 ] RC           │
│  Solde minimum        : [ 0       ] RC           │
│                                                  │
│  Opérations autorisées                           │
│  ✅ Recharge via Mobile Money                    │
│  ✅ Paiement sur la plateforme                   │
│  ☐  Retrait vers Mobile Money                   │
│  ☐  Transfert entre membres                     │
│                                                  │
│  Expiration                                      │
│  Les credits expirent : ✅ Oui  ☐ Non           │
│  Délai d'expiration   : [ 12 ] mois              │
│  Notification avant   : [ 30 ] jours             │
│                                                  │
│  Sécurité                                        │
│  OTP requis si retrait > [ 50 000 ] RC           │
│  Délai retrait après recharge : [ 24 ] heures    │
└─────────────────────────────────────────────────┘

La configuration des Promos
┌─────────────────────────────────────────────────┐
│  PROMO — Nouveau code promo                      │
├─────────────────────────────────────────────────┤
│  Informations de base                            │
│  Nom          : [ Promo Noël 2026        ]       │
│  Code         : [ NOEL25 ] ou [ Générer  ]       │
│  Description  : [ 25% pour les fêtes... ]        │
│                                                  │
│  Type de réduction                               │
│  ◉ Pourcentage    : [ 25 ] %                     │
│  ○ Montant fixe   : [    ] XAF                   │
│  ○ Cashback wallet: [    ] RC offerts            │
│  ○ Produit offert : [ choisir... ▼ ]             │
│                                                  │
│  Conditions d'utilisation                        │
│  Montant minimum de commande : [ 5 000 ] XAF    │
│  Réservé aux membres         : [ Tous ▼ ]        │
│    options: Tous | Gold | Silver | Nouveau       │
│             | Inactif 30j | Segment personnalisé │
│                                                  │
│  Limites                                         │
│  Utilisations totales  : [ 1000 ] (0 = illimité) │
│  Par membre            : [ 1    ]                │
│  Par membre par semaine: [ -    ]                │
│                                                  │
│  Période                                         │
│  Du [ 24/12/2026 ] au [ 26/12/2026 ]            │
│                                                  │
│  Combinaison avec d'autres promos                │
│  ○ Non combinable                                │
│  ◉ Combinable avec les points de fidélité        │
│  ○ Combinable avec tout                          │
└─────────────────────────────────────────────────┘

La configuration des Paliers
┌─────────────────────────────────────────────────┐
│  PALIERS — Configuration                         │
├─────────────────────────────────────────────────┤
│  Critère de progression                          │
│  ◉ Points cumulés à vie                          │
│  ○ Montant dépensé à vie                         │
│  ○ Nombre d'achats                               │
│                                                  │
│  ┌──────────┬──────────┬────────────────────┐   │
│  │ Palier   │ Seuil    │ Avantages          │   │
│  ├──────────┼──────────┼────────────────────┤   │
│  │ Bronze   │ 0 pts    │ x1 points          │   │
│  │ Silver   │ 1000 pts │ x1.5 points        │   │
│  │ Gold     │ 5000 pts │ x2 points + accès  │   │
│  │ Platinum │ 20000pts │ x3 points + VIP    │   │
│  └──────────┴──────────┴────────────────────┘   │
│  + Ajouter un palier                             │
│                                                  │
│  Politique de maintien                           │
│  Pour maintenir son palier, le membre doit       │
│  gagner [ 500 ] points par [ trimestre ▼ ]       │
│                                                  │
│  En cas de dégradation                           │
│  Notifier [ 30 ] jours avant                     │
│  Délai de grâce : [ 30 ] jours après la date     │
└─────────────────────────────────────────────────┘

Ce que ça implique techniquement
Toute cette interface de configuration se traduit en arrière-plan par des entités qu'on a déjà conçues. Voilà la correspondance.
Template "Carte de fidélité"
        ↓  génère automatiquement
Rule {
  trigger: { event_type: "purchase.completed" },
  conditions: [{ type: "cumulative_count", operator: "gte", value: N }],
  effects: [
    { type: "grant_reward", params: { reward_id: "..." }},
    { type: "reset_counter" }
  ]
}

Configuration Wallet
        ↓  génère automatiquement
TenantWalletConfig {
  currency_name: "RidnCoins",
  exchange_rate: 1.0,
  daily_spend_cap: 200000,
  allowed_operations: ["TOPUP", "PURCHASE"],
  expiry_days: 365,
  ...
}

Configuration Paliers
        ↓  génère automatiquement
TierPolicy {
  criterion: "lifetime_points",
  tiers: [
    { level: "BRONZE", threshold: 0, multiplier: 1.0 },
    { level: "SILVER", threshold: 1000, multiplier: 1.5 },
    ...
  ],
  maintain_points_per_quarter: 500,
  downgrade_grace_days: 30
}

Le principe clé
L'interface cache la complexité, elle ne la supprime pas.
En dessous, le système reste le même moteur de règles expressif qu'on a conçu. Ce que le dashboard fait c'est traduire des intentions humaines simples en configurations techniques complexes. Un admin qui clique sur le template "Carte de fidélité" crée exactement la même Rule qu'un développeur qui écrirait le JSON à la main.
C'est ce qui rend le système à la fois accessible pour un admin non-technique et puissant pour un intégrateur technique.

Ce qu'on doit encore décider
A — Est-ce qu'un tenant peut créer ses propres templates et les réutiliser ? Ou les templates sont-ils fixes et définis par nous ?
B — Est-ce qu'on propose un simulateur dans le dashboard ? L'admin configure une règle, entre un scénario fictif (membre Gold, 10ème achat, 5000 XAF), et voit ce qui se passerait sans déclencher de vraie transaction.
C — Est-ce qu'on veut un système de preview en temps réel — pendant que l'admin configure sa règle, il voit une phrase en langage naturel qui résume ce qu'il est en train de créer : "Quand un membre Gold complète son 10ème achat premium, il reçoit un ticket gratuit."
Ces trois fonctionnalités transforment l'expérience admin de "je configure un système technique" à "je parle à mon programme de fidélité". C'est la différence entre un outil et un produit.
\section{Conception détaillée des modules} \subsection{Module Wallet} \textbf{Principes de conception} : \begin{itemize} \item \textbf{Idempotence} : chaque opération de crédit ou débit est liée à une clé d’idempotence unique, garantissant qu’une même demande n’altère le solde qu’une seule fois. \item \textbf{Atomicité} : la modification du solde et l’écriture de la transaction sont réalisées de manière indivisible (transaction de base de données). \item \textbf{Multi‑devises} : chaque wallet est rattaché à une devise (XAF, NGN, KES, EUR) ; le tenant choisit les devises autorisées. \item \textbf{Limites configurables} : plafond de dépense journalier, solde minimum, montant maximum par recharge sont définis au niveau du tenant et éventuellement ajustables par membre. \item \textbf{Sécurité} : les transactions sensibles (montant élevé) exigent une validation par OTP ; un mécanisme de gel automatique (statut FROZEN) est déclenché en cas de détection de motif frauduleux (nombreux débits successifs, montants inhabituels). \end{itemize} \textbf{États du wallet} : ACTIVE, FROZEN, CLOSED. Le diagramme d’états correspondant pourra être ajouté ; la transition d’ACTIVE vers FROZEN peut être manuelle (action admin) ou automatique (fraude). Le retour à ACTIVE est possible après vérification. La fermeture (CLOSED) est irréversible. \textbf{Opérations principales} : \begin{itemize} \item \texttt{credit(amount, idempotencyKey)} : ajoute le montant après validation des limites de recharge. \item \texttt{debit(amount, idempotencyKey)} : vérifie le solde, le statut ACTIVE et les plafonds journaliers avant d’effectuer le débit. \item \texttt{freeze()} / \texttt{unfreeze()} : changement d’état. \item \texttt{withdraw(amount, targetAccount)} : retrait d’argent vers un compte mobile money ou bancaire, soumis à la validation KYC. \end{itemize} La conception s’appuie sur le diagramme de séquence « Utiliser son wallet » déjà présenté, qui illustre le débit lors d’un paiement. \subsection{Module Fidélisation – Moteur promotionnel} \textbf{Conception du moteur de règles} : \begin{itemize} \item \textbf{Règles paramétrables} : chaque règle (Rule) se compose d’un Trigger (type d’événement, filtres), d’une ou plusieurs Conditions (compteurs cumulés, solde, palier) et d’une série d’Effets (credit\_points, grant\_reward, update\_tier, reset\_counter). La configuration est stockée au format JSON, accessible et modifiable depuis le dashboard administrateur. \item \textbf{Gestion des points et paliers} : le PointsAccount accumule les points au fil des actions. Les paliers (MemberTier) sont calculés automatiquement selon des seuils définis par le tenant ; ils influencent les multiplicateurs de points. \item \textbf{Catalogue de récompenses} : le tenant crée des Reward avec un coût en points, une quantité disponible et une période de validité. L’échange est atomique : les points sont débités et un RewardGrant est généré. \item \textbf{Campagnes promotionnelles} : une Campaign peut modifier temporairement les règles (ex. multiplication par 2 des points). Un planificateur active/désactive automatiquement les règles modifiées selon les dates définies. \item \textbf{Parrainage} : le module génère un lien unique par membre. Lorsqu’un filleul s’inscrit via ce lien et réalise l’action qualifiante, des récompenses sont attribuées au parrain et au filleul. \end{itemize} Les interactions liées à la fidélisation sont détaillées dans les diagrammes de séquence « Gérer les paliers », « Configurer le catalogue » et « Échanger des points ».
JE VEUX DONC REDIGER CETTE PARTIE DE MON CAHIER D'ANALYSE ET CONCEPTION DE LA MANIERE LA PLUS EXPLICITE POSSIBLE
21 mai
Laisse-moi d'abord lire le skill docx avant de rédiger.
Lire le skill docx
Bien. Je génère maintenant le document Word complet et détaillé.
2 fichiers modifiés, fichier créé, a exécuté une commande
Loyalty Programme ○ Promo_Core API (Promo code/Bonus/: ○ Implementation as a rewards program to incentivize repeat usage.) (From previous project 5GI): Promo code, Bonus, ○ Referral Program + reward: Offer incentives for referring new users. ○ This must include cost Services / Financial service. Examples are but not limited to Subscription + Pricing for example
je veux initialiser le backend en springboot, je dois mettre quoi dans mon spring initializ
22 mai
Voilà exactement ce qu'il faut configurer sur start.spring.io.

Configuration de base
Champ
Valeur
Project
Maven
Language
Java
Spring Boot
3.4.x (la 4.0 n'est pas encore sur initializr — on migrera manuellement)
Group
com.yowyob
Artifact
loyalty-service
Name
loyalty-service
Package name
com.yowyob.loyalty
Packaging
Jar
Java
21

Dépendances à cocher
Web & API
Spring Reactive Web — WebFlux, le serveur non-bloquant (Netty, pas Tomcat)
Base de données
Spring Data R2DBC — accès réactif à PostgreSQL
PostgreSQL Driver — driver JDBC/R2DBC PostgreSQL
Flyway Migration — migrations de schéma (Liquibase marche aussi, le prof dit Liquibase donc prends Liquibase Migration à la place)
Cache & Sessions
Spring Data Redis (Reactive) — cache Redis réactif pour idempotence, sessions, rate limiting
Messaging
Spring for Apache Kafka — intégration Kafka pour les events inter-modules
Sécurité
Spring Security — sécurité de base
OAuth2 Resource Server — validation des JWT émis par YowAuth0
Observabilité
Spring Boot Actuator — endpoints /health, /metrics, /info
Prometheus — export des métriques au format Prometheus (scraped par Grafana)
Documentation API
Validation — Bean Validation (javax/jakarta) pour valider les requêtes entrantes
Dev
Lombok — réduction du boilerplate (getters, builders, constructeurs)
Spring Boot DevTools — rechargement à chaud en développement

Ce qu'on ajoute manuellement dans le pom.xml après génération
Ces dépendances ne sont pas sur l'initializr mais sont nécessaires.
xml
<!-- OpenAPI / Swagger pour WebFlux -->
<dependency>
  <groupId>org.springdoc</groupId>
  <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
  <version>2.5.0</version>
</dependency>

<!-- MapStruct — mapping DTO <-> Entity -->
<dependency>
  <groupId>org.mapstruct</groupId>
  <artifactId>mapstruct</artifactId>
  <version>1.5.5.Final</version>
</dependency>
<dependency>
  <groupId>org.mapstruct</groupId>
  <artifactId>mapstruct-processor</artifactId>
  <version>1.5.5.Final</version>
  <scope>provided</scope>
</dependency>

<!-- Elasticsearch client réactif -->
<dependency>
  <groupId>org.springframework.data</groupId>
  <artifactId>spring-data-elasticsearch</artifactId>
</dependency>

<!-- Tests réactifs -->
<dependency>
  <groupId>io.projectreactor</groupId>
  <artifactId>reactor-test</artifactId>
  <scope>test</scope>
</dependency>

<!-- Testcontainers pour tests d'intégration -->
<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>postgresql</artifactId>
  <scope>test</scope>
</dependency>
<dependency>
  <groupId>org.testcontainers</groupId>
  <artifactId>kafka</artifactId>
  <scope>test</scope>
</dependency>

Structure de packages à créer après génération
C'est l'architecture hexagonale DDD qu'on a conçue. Tu crées ces packages manuellement.
com.yowyob.loyalty
│
├── domain                          ← Logique métier pure, zéro dépendance Spring
│   ├── wallet
│   │   ├── model                   ← Wallet, WalletTransaction, WalletPolicy
│   │   ├── port                    ← Interfaces (WalletRepository, PaymentGatewayPort)
│   │   └── service                 ← WalletDomainService
│   ├── loyalty
│   │   ├── model                   ← Rule, Trigger, Condition, Effect, Counter
│   │   ├── port
│   │   └── service                 ← RuleEngine, PointsService
│   ├── referral
│   ├── promo
│   └── member
│
├── application                     ← Orchestration des cas d'utilisation (CQRS)
│   ├── wallet
│   │   ├── command                 ← CreditWalletCommand, DebitWalletCommand
│   │   ├── query                   ← GetWalletBalanceQuery
│   │   └── handler                 ← WalletCommandHandler, WalletQueryHandler
│   ├── loyalty
│   └── promo
│
├── infrastructure                  ← Adaptateurs techniques
│   ├── persistence
│   │   ├── wallet                  ← WalletRepositoryImpl (R2DBC)
│   │   └── loyalty
│   ├── kafka
│   │   ├── producer                ← EventProducer
│   │   └── consumer                ← LoyaltyEventConsumer
│   ├── redis                       ← IdempotencyStore, CacheAdapter
│   ├── payment
│   │   ├── mtn                     ← MtnMobileMoneyAdapter
│   │   ├── orange                  ← OrangeMoneyAdapter
│   │   └── stripe                  ← StripeAdapter
│   └── notification                ← NotificationAdapter (FCM, Twilio, SMTP)
│
├── api                             ← Couche REST (contrôleurs WebFlux)
│   ├── wallet                      ← WalletController
│   ├── loyalty                     ← LoyaltyController
│   ├── promo                       ← PromoController
│   ├── admin                       ← AdminConfigController
│   └── webhook                     ← WebhookController (callbacks MTN/Orange/Stripe)
│
└── shared                          ← Utilitaires partagés
    ├── multitenancy                ← TenantContextHolder, TenantFilter
    ├── security                    ← JwtFilter, TenantSecurityFilter
    └── exception                   ← GlobalExceptionHandler, AppException

application.yml minimal pour démarrer
yaml
spring:
  application:
    name: loyalty-service

  # PostgreSQL R2DBC
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/loyalty_db
    username: loyalty_user
    password: loyalty_pass
    pool:
      initial-size: 5
      max-size: 20

  # Liquibase (utilise JDBC pour les migrations, pas R2DBC)
  liquibase:
    url: jdbc:postgresql://localhost:5432/loyalty_db
    user: loyalty_user
    password: loyalty_pass
    change-log: classpath:db/changelog/db.changelog-master.xml

  # Redis
  data:
    redis:
      host: localhost
      port: 6379

  # Kafka
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: loyalty-service
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer

  # Security OAuth2 JWT
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8180/realms/yowyob

# Actuator
management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  metrics:
    export:
      prometheus:
        enabled: true

# Swagger
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

server:
  port: 8080

docker-compose.yml minimal pour l'environnement local
yaml
version: '3.8'
services:

  postgres:
    image: postgres:18
    environment:
      POSTGRES_DB: loyalty_db
      POSTGRES_USER: loyalty_user
      POSTGRES_PASSWORD: loyalty_pass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  kafka:
    image: confluentinc/cp-kafka:7.6.0
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    ports:
      - "9092:9092"
    depends_on:
      - zookeeper

  zookeeper:
    image: confluentinc/cp-zookeeper:7.6.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

volumes:
  postgres_data:

Ordre de développement recommandé
Une fois le projet initialisé, voilà l'ordre logique pour ne pas bloquer.
Semaine 1 — Poser les fondations : multitenancy filter, JWT security filter, structure de packages, premier endpoint health qui retourne le tenant résolu depuis le token.
Semaine 2 — Module Wallet : entités R2DBC, repository, service de crédit et débit avec idempotence Redis, premier test d'intégration avec Testcontainers.
Semaine 3 — Moteur de règles : Rule, Trigger, Condition, Effect en base, RuleEngine qui évalue une règle simple sur un Event entrant.
Semaine 4 — Kafka : producer d'events depuis le WalletService, consumer dans le LoyaltyModule, chaîne complète event → règle → effet.
Semaine 5+ — Parrainage, codes promo, campagnes, dashboard admin.
Module Wallet : entités R2DBC, repository, service de crédit et débit avec idempotence Redis, premier test d'intégration avec Testcontainers.
je veux travailler sur le wallet, fait une liste exhaustive des fonctionnalité et et fichier à creer.
23 mai
Fonctionnalités exhaustives du Module Wallet

F1 — Création du Wallet
Créer automatiquement un wallet à l'enrôlement d'un membre
Associer la WalletPolicy du tenant au wallet créé
Initialiser le solde à zéro
Statut initial : PENDING_KYC ou ACTIVE selon la config du tenant
F2 — Crédit du Wallet
Crédit via MTN Mobile Money
Crédit via Orange Money
Crédit via Stripe
Crédit interne automatique (loyalty reward, cashback, bonus)
Vérification idempotence avant tout traitement
Vérification WalletPolicy (recharge_max_par_operation, solde_maximum)
Création de la WalletTransaction immuable
Émission de l'event Kafka wallet.credited
F3 — Débit du Wallet
Débit à la demande de la plateforme cliente (paiement service)
Vérification idempotence
Vérification statut ACTIVE
Vérification solde suffisant
Vérification plafond journalier (depense_max_journaliere)
Challenge OTP si montant > seuil configuré
Création de la WalletTransaction immuable
Émission de l'event Kafka wallet.debited
F4 — Retrait vers Mobile Money
Initier un retrait vers MTN ou Orange Money
Vérification KYC validé
Vérification délai post-crédit
Réservation du montant pendant le traitement
Confirmation via webhook provider
Annulation et restitution si échec
F5 — Consultation du solde
Lire le solde courant d'un membre
Lire le solde avec détail (disponible, réservé, expirant bientôt)
F6 — Historique des transactions
Lister les transactions paginées
Filtrer par type (CREDIT/DEBIT/REVERSAL)
Filtrer par source (TOPUP_MTN, PURCHASE, LOYALTY…)
Filtrer par période (date_from, date_to)
Filtrer par statut
F7 — Annulation / Reversal
Annuler une transaction COMPLETED
Créer une WalletTransaction de type REVERSAL
Restituer le solde
Lier le REVERSAL à la transaction originale
F8 — Gel et dégel
Geler un wallet (action admin ou détection fraude)
Dégeler un wallet (action admin uniquement)
Tracer le motif et l'acteur dans AuditLog
F9 — Clôture
Clore définitivement un wallet
Vérifier solde soldé avant clôture
Transition irréversible vers CLOSED
F10 — Détection de fraude
Détecter les débits successifs rapides
Détecter les montants inhabituels
Détecter le dépôt-retrait immédiat
Geler automatiquement + émettre wallet.fraud_suspected
F11 — Gestion des PaymentRequests
Créer une PaymentRequest à l'initiation
Recevoir et traiter les webhooks providers (MTN, Orange, Stripe)
Gérer les timeouts et retry
Lier PaymentRequest ↔ WalletTransaction
F12 — Configuration tenant (WalletPolicy)
Lire la WalletPolicy du tenant
Appliquer les limites à chaque opération
Permettre la surcharge par membre (membres VIP)
F13 — Réconciliation
Vérifier que balance = somme des transactions
Détecter les incohérences
Exposer un endpoint de réconciliation pour l'admin
F14 — Notifications
Notifier le membre à chaque crédit
Notifier le membre à chaque débit
Notifier l'admin en cas de fraude détectée
Notifier avant expiration de crédits

Fichiers à créer — liste exhaustive
com/yowyob/loyalty/
│
├── domain/wallet/
│   ├── model/
│   │   ├── Wallet.java
│   │   ├── WalletTransaction.java
│   │   ├── WalletPolicy.java
│   │   ├── PaymentRequest.java
│   │   ├── WalletAuditLog.java
│   │   └── enums/
│   │       ├── WalletStatus.java
│   │       ├── TransactionType.java
│   │       ├── TransactionSource.java
│   │       ├── TransactionStatus.java
│   │       ├── PaymentProvider.java
│   │       ├── PaymentDirection.java
│   │       └── PaymentRequestStatus.java
│   │
│   ├── port/
│   │   ├── in/
│   │   │   ├── CreditWalletUseCase.java
│   │   │   ├── DebitWalletUseCase.java
│   │   │   ├── WithdrawWalletUseCase.java
│   │   │   ├── FreezeWalletUseCase.java
│   │   │   ├── UnfreezeWalletUseCase.java
│   │   │   ├── CloseWalletUseCase.java
│   │   │   ├── GetWalletBalanceUseCase.java
│   │   │   ├── GetTransactionHistoryUseCase.java
│   │   │   └── ReverseTransactionUseCase.java
│   │   └── out/
│   │       ├── WalletRepository.java
│   │       ├── WalletTransactionRepository.java
│   │       ├── PaymentRequestRepository.java
│   │       ├── WalletAuditLogRepository.java
│   │       ├── IdempotencyPort.java
│   │       ├── PaymentGatewayPort.java
│   │       └── WalletEventPublisherPort.java
│   │
│   ├── service/
│   │   ├── WalletDomainService.java
│   │   ├── WalletPolicyValidator.java
│   │   └── FraudDetectionService.java
│   │
│   └── event/
│       ├── WalletCreditedEvent.java
│       ├── WalletDebitedEvent.java
│       ├── WalletFrozenEvent.java
│       ├── WalletUnfrozenEvent.java
│       ├── WalletClosedEvent.java
│       ├── WalletFraudSuspectedEvent.java
│       └── PaymentConfirmedEvent.java
│
├── application/wallet/
│   ├── command/
│   │   ├── CreditWalletCommand.java
│   │   ├── DebitWalletCommand.java
│   │   ├── WithdrawCommand.java
│   │   ├── FreezeWalletCommand.java
│   │   ├── UnfreezeWalletCommand.java
│   │   ├── CloseWalletCommand.java
│   │   └── ReverseTransactionCommand.java
│   │
│   ├── query/
│   │   ├── GetWalletBalanceQuery.java
│   │   └── GetTransactionHistoryQuery.java
│   │
│   └── handler/
│       ├── CreditWalletHandler.java
│       ├── DebitWalletHandler.java
│       ├── WithdrawHandler.java
│       ├── FreezeWalletHandler.java
│       ├── UnfreezeWalletHandler.java
│       ├── CloseWalletHandler.java
│       ├── ReverseTransactionHandler.java
│       ├── GetWalletBalanceHandler.java
│       └── GetTransactionHistoryHandler.java
│
├── infrastructure/
│   ├── persistence/wallet/
│   │   ├── entity/
│   │   │   ├── WalletEntity.java
│   │   │   ├── WalletTransactionEntity.java
│   │   │   ├── PaymentRequestEntity.java
│   │   │   └── WalletAuditLogEntity.java
│   │   ├── repository/
│   │   │   ├── WalletR2dbcRepository.java
│   │   │   ├── WalletTransactionR2dbcRepository.java
│   │   │   ├── PaymentRequestR2dbcRepository.java
│   │   │   └── WalletAuditLogR2dbcRepository.java
│   │   ├── adapter/
│   │   │   ├── WalletRepositoryAdapter.java
│   │   │   ├── WalletTransactionRepositoryAdapter.java
│   │   │   ├── PaymentRequestRepositoryAdapter.java
│   │   │   └── WalletAuditLogRepositoryAdapter.java
│   │   └── mapper/
│   │       ├── WalletMapper.java
│   │       ├── WalletTransactionMapper.java
│   │       └── PaymentRequestMapper.java
│   │
│   ├── redis/
│   │   ├── IdempotencyAdapter.java
│   │   └── WalletCacheAdapter.java
│   │
│   ├── kafka/
│   │   ├── producer/
│   │   │   └── WalletEventProducer.java
│   │   └── consumer/
│   │       └── PaymentWebhookConsumer.java
│   │
│   └── payment/
│       ├── mtn/
│       │   ├── MtnMobileMoneyAdapter.java
│       │   ├── MtnApiClient.java
│       │   └── dto/
│       │       ├── MtnPaymentRequest.java
│       │       └── MtnPaymentResponse.java
│       ├── orange/
│       │   ├── OrangeMoneyAdapter.java
│       │   ├── OrangeApiClient.java
│       │   └── dto/
│       │       ├── OrangePaymentRequest.java
│       │       └── OrangePaymentResponse.java
│       └── stripe/
│           ├── StripeAdapter.java
│           └── dto/
│               ├── StripePaymentRequest.java
│               └── StripePaymentResponse.java
│
├── api/wallet/
│   ├── WalletController.java
│   ├── WalletAdminController.java
│   ├── WebhookController.java
│   ├── dto/
│   │   ├── request/
│   │   │   ├── TopUpRequest.java
│   │   │   ├── DebitRequest.java
│   │   │   ├── WithdrawRequest.java
│   │   │   ├── FreezeRequest.java
│   │   │   └── ReverseTransactionRequest.java
│   │   └── response/
│   │       ├── WalletResponse.java
│   │       ├── WalletBalanceResponse.java
│   │       ├── TransactionResponse.java
│   │       ├── TransactionPageResponse.java
│   │       └── PaymentRequestResponse.java
│   └── validation/
│       └── WalletRequestValidator.java
│
├── shared/
│   ├── multitenancy/
│   │   ├── TenantContext.java
│   │   ├── TenantContextHolder.java
│   │   └── TenantWebFilter.java
│   ├── security/
│   │   ├── JwtTenantFilter.java
│   │   └── SecurityConfig.java
│   └── exception/
│       ├── WalletNotFoundException.java
│       ├── InsufficientBalanceException.java
│       ├── WalletFrozenException.java
│       ├── DailyLimitExceededException.java
│       ├── IdempotencyConflictException.java
│       └── GlobalExceptionHandler.java
│
└── resources/
    ├── application.yml
    ├── application-dev.yml
    └── db/changelog/
        ├── db.changelog-master.xml
        └── migrations/
            ├── V001__create_wallets_table.sql
            ├── V002__create_wallet_transactions_table.sql
            ├── V003__create_payment_requests_table.sql
            ├── V004__create_wallet_audit_logs_table.sql
            └── V005__create_wallet_indexes.sql

test/
└── com/yowyob/loyalty/
    ├── domain/wallet/
    │   ├── WalletDomainServiceTest.java
    │   ├── WalletPolicyValidatorTest.java
    │   └── FraudDetectionServiceTest.java
    ├── application/wallet/
    │   ├── CreditWalletHandlerTest.java
    │   ├── DebitWalletHandlerTest.java
    │   └── ReverseTransactionHandlerTest.java
    └── integration/
        ├── WalletControllerIntegrationTest.java
        ├── WalletCreditIntegrationTest.java
        ├── WalletDebitIntegrationTest.java
        └── config/
            └── TestContainersConfig.java

Par où commencer
L'ordre logique pour ne pas se bloquer :
Étape 1 — Les enums et les modèles du domaine (aucune dépendance externe).
Étape 2 — Les interfaces des ports (in et out) — définit le contrat sans implémentation.
Étape 3 — Les migrations SQL Liquibase — crée les tables en base.
Étape 4 — Les entités R2DBC et les repositories Spring Data.
Étape 5 — Les adapters Redis (idempotence) et les adapters de persistence.
Étape 6 — Le WalletDomainService et WalletPolicyValidator.
Étape 7 — Les Command Handlers (credit, debit, freeze…).
Étape 8 — Les contrôleurs REST WebFlux.
Étape 9 — Les tests unitaires domaine, puis les tests d'intégration Testcontainers.
Tu veux qu'on commence à coder étape par étape ?
il y'aura un frontend du projet entier, n'estce pas? si oui,donne un descriptif detaillé de ce à quoi doit il doit ressembler - on travaille en nextJS - je veux une description detaillé de ce qui est attendu pour chaque page. donne moi du texte
23 mai
Frontend — Loyalty Programme SaaS
Vision générale
Le frontend du Loyalty Programme est une application web construite en Next.js avec Server-Side Rendering. Elle couvre deux univers distincts qui ne se mélangent jamais : le Dashboard d'administration destiné aux tenants qui configurent leur programme de fidélité, et le Portail membre destiné aux clients finaux qui consultent leurs points, leurs récompenses et leur wallet. Ces deux univers partagent la même base de code mais ont des layouts, des routes et des logiques d'authentification complètement séparés.
L'application est pensée PWA-first — elle doit fonctionner comme une application installable sur mobile, avec un Service Worker qui gère le cache et les notifications push. Le design est sobre, professionnel et modulaire. Chaque tenant peut personnaliser les couleurs primaires, le logo et le nom de sa monnaie virtuelle — ces préférences sont chargées depuis la TenantConfig et appliquées dynamiquement via des variables CSS au niveau du layout racine.

Univers 1 — Dashboard d'administration tenant
C'est l'interface que l'équipe marketing ou le responsable fidélité d'une entreprise cliente utilise au quotidien pour piloter son programme. Il n'y a pas de code à écrire — tout se configure via des formulaires, des sélecteurs et des tableaux de bord visuels.

Page 1 — Authentification admin
Cette page est la porte d'entrée unique pour tous les administrateurs de tous les tenants. Elle affiche le logo de la plateforme Yowyob Loyalty au centre, un formulaire minimal avec un champ email et un champ mot de passe, un bouton de connexion principal, et un lien "Mot de passe oublié". En dessous du formulaire, une section "Se connecter avec" propose l'authentification sociale via Google et Microsoft pour les tenants qui ont activé cette option dans YowAuth0.
Après soumission, le système identifie automatiquement le tenant auquel appartient cet administrateur depuis les claims du JWT retourné par YowAuth0. Si l'administrateur gère plusieurs tenants (cas d'un consultant externe), une étape intermédiaire affiche la liste de ses tenants sous forme de cartes avec le logo et le nom de chaque plateforme, et il choisit sur lequel il souhaite travailler. Ce choix est mémorisé pour la session.
La page gère proprement les états d'erreur — identifiants incorrects, compte suspendu, tenant inactif — avec des messages explicites et non génériques. Elle n'affiche aucune barre de navigation ni header tant que l'utilisateur n'est pas connecté.

Page 2 — Vue d'ensemble (Home Dashboard)
C'est la première page que voit l'administrateur après connexion. Elle donne une photographie instantanée de la santé de son programme de fidélité à travers des indicateurs clés organisés en sections visuelles claires.
En haut de page, une barre de bienvenue affiche le nom du tenant, le logo configuré, le plan actif (Free / Pro / Enterprise) et la date de renouvellement. Un bandeau d'alerte orange s'affiche si des problèmes nécessitent l'attention de l'admin — wallet d'un membre gelé en attente de décision, campagne qui expire dans 48 heures, seuil d'utilisation d'un code promo presque atteint.
La section centrale présente six métriques en cartes visuelles. La première carte affiche le nombre total de membres actifs dans le programme avec un graphique sparkline montrant l'évolution sur les 30 derniers jours et la variation en pourcentage par rapport au mois précédent. La deuxième carte montre le total de points distribués ce mois, avec la même logique de comparaison. La troisième affiche le taux de rédemption — le pourcentage des points distribués qui ont été échangés contre des récompenses — avec une jauge colorée (rouge si < 10%, orange si 10-30%, vert si > 30%). La quatrième carte présente le volume total des transactions wallet du mois en devise virtuelle et en équivalent XAF. La cinquième montre le nombre de nouveaux membres parrainés ce mois. La sixième affiche le nombre de codes promo actifs avec leur taux d'utilisation moyen.
En dessous des métriques, deux panneaux côte à côte. Le panneau gauche est une liste des dix derniers événements importants du programme — membre passé Gold, code promo épuisé, fraude détectée, campagne activée — avec un icône coloré selon le type, un timestamp et un lien vers le détail. Le panneau droit est un graphique en courbes superposées montrant l'évolution jour par jour sur les 30 derniers jours des points distribués, des points dépensés et du nombre de transactions wallet. L'administrateur peut basculer entre une vue journalière, hebdomadaire et mensuelle.
Tout en bas, un bloc "Actions rapides" propose quatre boutons larges : Créer une règle, Lancer une campagne, Ajouter une récompense, Générer un code promo. Ces boutons ouvrent directement les formulaires correspondants sans navigation supplémentaire.

Page 3 — Gestion des membres
Cette page est le registre complet de tous les membres du tenant. Elle présente un tableau paginé avec recherche et filtres avancés.
La barre de recherche en haut permet de trouver un membre par son nom, email, numéro de téléphone ou external_id. À droite de la recherche, un bouton "Filtres" ouvre un panneau latéral avec des filtres multiples combinables : filtrer par palier (Bronze / Silver / Gold / Platinum), par statut du wallet (ACTIVE / FROZEN / CLOSED), par segment, par période d'inscription, par tranche de solde de points, par activité récente (actif dans les 7j / 30j / inactif).
Le tableau affiche pour chaque membre : son avatar ou initiales, son nom et email, son palier avec le badge coloré correspondant, son solde de points formaté, son solde wallet en devise virtuelle, son statut wallet avec un indicateur coloré, sa date d'inscription et la date de sa dernière activité. Chaque ligne est cliquable et mène au profil détaillé du membre.
En haut à droite du tableau, un bouton "Exporter CSV" génère un export des membres filtrés actuels.
Le profil détaillé d'un membre s'ouvre dans une page dédiée — pas une modale, une vraie page avec son propre URL pour faciliter le bookmarking et le partage en équipe. Cette page est organisée en onglets. L'onglet Vue d'ensemble présente toutes les informations du membre, son wallet avec le solde et les dernières transactions, sa progression vers le palier suivant avec une barre de progression visuelle, et ses récompenses actives. L'onglet Historique des transactions liste toutes les WalletTransactions et PointsTransactions avec les mêmes capacités de filtre que la page principale. L'onglet Récompenses liste tous les RewardGrants avec leur statut. L'onglet Parrainage affiche le lien de parrainage du membre, le nombre de filleuls qu'il a recrutés, leur statut de conversion et les récompenses générées. L'onglet Actions admin permet de geler ou dégeler le wallet avec saisie obligatoire du motif, d'ajouter manuellement des points ou du crédit wallet avec justification, et de clore le compte.

Page 4 — Règles de fidélité
C'est la page centrale du dashboard — celle où l'administrateur configure toute la logique de son programme. Elle est organisée en deux vues basculables : la vue "Templates" et la vue "Règles personnalisées".
La vue Templates affiche une galerie de cartes, chaque carte représentant un template prêt à l'emploi. Chaque carte montre une icône représentative, le nom du template en gras, une description d'une phrase expliquant ce que fait ce template, et un bouton "Configurer". Quand l'administrateur clique sur Configurer, la carte se retourne ou s'agrandit en place pour afficher uniquement les paramètres spécifiques à ce template — sans jargon technique, sans JSON visible. Un template "Carte de fidélité" demande juste combien d'achats avant la récompense et quelle récompense donner. Un template "Programme de points" demande combien de points par achat. Un template "Bonus de bienvenue" demande combien de points offrir à l'inscription. Une fois les paramètres saisis, un bouton "Créer la règle" génère tout en arrière-plan et affiche une confirmation.
La vue Règles personnalisées liste toutes les règles existantes du tenant dans un tableau. Pour chaque règle : son nom, son statut avec un toggle on/off permettant d'activer ou désactiver directement depuis la liste, son type de trigger résumé en une phrase courte, le nombre de fois qu'elle s'est déclenchée depuis sa création, sa date de création et ses boutons d'action (modifier, dupliquer, archiver). Un bouton "Nouvelle règle" en haut ouvre le Rule Builder.
Le Rule Builder est la pièce maîtresse de cette page. Il s'ouvre dans une page dédiée, pas une modale, pour avoir l'espace nécessaire. Il est structuré en trois sections verticales clairement labellisées. La section QUAND présente un sélecteur d'événement déclencheur avec un menu déroulant listant tous les types d'events supportés en langage naturel — "Un achat est complété", "Un membre s'inscrit", "Un trajet est terminé". Sous ce sélecteur, des filtres contextuels apparaissent selon le type d'event choisi — si "achat", un filtre "catégorie de produit" et un filtre "montant minimum" apparaissent. La section SI présente les conditions. Un bouton "Ajouter une condition" permet d'en ajouter plusieurs. Chaque condition est une ligne avec trois sélecteurs : la métrique (nombre d'achats cumulés, solde de points, palier du membre, heure de la journée…), l'opérateur (est supérieur ou égal à, est égal à, est…), et la valeur. Les conditions sont reliées par un opérateur ET visible entre chaque ligne. La section ALORS présente les effets. Même logique — un bouton "Ajouter un effet" permet d'en ajouter plusieurs. Chaque effet est une ligne avec un sélecteur de type d'effet et les paramètres correspondants qui apparaissent dynamiquement.
En bas du Rule Builder, deux éléments importants. D'abord, une phrase de résumé générée en temps réel qui se met à jour à chaque modification et décrit en français courant ce que la règle va faire — "Quand un membre Gold complète son 10ème achat de ticket premium, il reçoit un ticket gratuit et son compteur repart à zéro." Ensuite, un panneau de configuration avancée repliable par défaut qui expose la priorité de la règle, sa période de validité et sa politique de combinaison avec d'autres règles.

Page 5 — Catalogue de récompenses
Cette page permet à l'administrateur de créer et gérer les récompenses que les membres peuvent obtenir. Elle affiche une galerie de cartes, une par récompense, avec le nom, le type (icône distinctif), le coût en points, le stock disponible ou "illimité", et le statut actif/inactif.
Un formulaire de création de récompense s'ouvre dans un panneau latéral glissant depuis la droite. Il demande le nom de la récompense, sa description affichée aux membres, son type parmi quatre options présentées comme des boutons radio illustrés (Produit ou service offert, Réduction en pourcentage, Réduction montant fixe, Crédit wallet), le coût en points, le stock disponible avec une case "Illimité", la période de validité du RewardGrant après attribution, et une image optionnelle uploadée vers MinIO.
Un filtre en haut de la galerie permet de filtrer par type, par statut et de trier par coût ou par popularité (nombre de rédemptions).

Page 6 — Campagnes et codes promo
Cette page regroupe deux onglets : Campagnes automatiques et Codes promo.
L'onglet Campagnes automatiques liste les campagnes existantes dans un tableau avec leur nom, leur type, leur période de validité avec une barre de progression temporelle visuelle (la barre se remplit au fur et à mesure que la campagne avance dans le temps), leur statut (Programmée / Active / Terminée) et leurs actions. Un bouton "Nouvelle campagne" ouvre un formulaire en deux étapes. La première étape demande le nom, le type de campagne parmi les options disponibles présentées comme des cartes illustrées, et la fenêtre temporelle avec un date-picker double. La seconde étape affiche les paramètres spécifiques au type choisi — pour un multiplicateur de points, un slider permet de choisir le multiplicateur de 1x à 5x avec une visualisation en temps réel de ce que ça donnerait ("Un membre qui gagnait 100 points par achat en gagnera 200").
L'onglet Codes promo liste les codes existants avec leur nom, le code lui-même affiché dans une police monospace sur fond gris pour être facilement lisible et copiable, le type et la valeur de la réduction, le compteur d'utilisations sur le total autorisé sous forme de barre de progression colorée (verte jusqu'à 75%, orange jusqu'à 90%, rouge au-delà), la période de validité et les actions. Un bouton "Nouveau code" ouvre le formulaire de création de code promo décrit dans la section conception.

Page 7 — Configuration des paliers
Cette page présente les quatre paliers dans un affichage horizontal de cartes, du plus bas au plus haut, avec un dégradé de couleur (gris pour Bronze, argent pour Silver, doré pour Gold, platine pour Platinum). Chaque carte affiche le nom du palier, son icône, le seuil de points pour l'atteindre, le multiplicateur de points, et la liste des avantages configurés. Un bouton "Modifier" sur chaque carte ouvre un formulaire d'édition.
En dessous des cartes de paliers, une section "Politique de maintien" avec des champs pour configurer les points requis par période, la période de référence et le délai de grâce. Une section "Prévisualisation" montre en temps réel comment les membres actuels seraient distribués entre les paliers avec la configuration en cours — un camembert avec la répartition en pourcentage et en nombre absolu.

Page 8 — Programme de parrainage
Cette page est divisée en deux colonnes. La colonne gauche présente la configuration du programme — un formulaire pour définir la récompense du parrain, la récompense du filleul, la condition de conversion (quel event, quel montant minimum), le délai de conversion maximum et la politique anti-fraude. La colonne droite affiche les statistiques du programme en temps réel — nombre de parrainages actifs, taux de conversion, récompenses attribuées ce mois, et un tableau des meilleurs parraineurs.

Page 9 — Wallet et transactions (vue admin)
Cette page donne une vue macro sur tous les wallets du tenant. En haut, quatre métriques : volume total en circulation (somme de tous les soldes actifs), volume de recharges ce mois, volume de dépenses ce mois, nombre de wallets gelés en attente de décision.
Le tableau principal liste tous les wallets avec les mêmes capacités de recherche et filtre que la page membres. Pour chaque wallet : l'identifiant du membre, son nom, le solde en devise virtuelle et son équivalent en XAF, le statut avec indicateur coloré, le nombre de transactions ce mois et les actions rapides (voir détail, geler, ajustement manuel).
Un onglet "Transactions suspectes" affiche les transactions signalées par le système de détection de fraude avec leur motif, le membre concerné et les boutons Valider ou Bloquer.
Un onglet "Réconciliation" lance un calcul de vérification à la demande et affiche si le solde de chaque wallet correspond bien à la somme de ses transactions. Les incohérences éventuelles sont surlignées en rouge avec le détail.

Page 10 — Analytics
Cette page est le tableau de bord analytique complet. Elle est organisée en sections distinctes séparées par des titres clairs.
La section Fidélisation montre l'évolution du nombre de membres par palier au fil du temps sous forme de graphique en aires empilées, le taux de rétention mensuel, la durée de vie moyenne d'un membre avant churn, et les règles les plus déclenchées sous forme de classement.
La section Récompenses montre les récompenses les plus échangées en classement horizontal, le coût total en points des rédemptions par mois, le délai moyen entre attribution et utilisation d'une récompense, et le taux d'expiration des récompenses non utilisées.
La section Wallet montre la courbe des soldes cumulés en circulation, la ventilation des crédits par source (MTN / Orange / Stripe / Loyalty / Cashback), le ticket moyen des transactions, et la fréquence d'utilisation du wallet par membre actif.
La section Parrainage montre l'entonnoir de parrainage — de la génération du lien à la conversion et à la récompense — avec les taux de conversion à chaque étape.
Un sélecteur de période en haut à droite (7 jours / 30 jours / 90 jours / 12 mois / personnalisé) s'applique à l'ensemble des graphiques de la page. Un bouton "Exporter rapport PDF" génère un rapport mis en page avec tous les graphiques visibles.

Page 11 — Configuration du tenant
Cette page est l'interface de paramétrage global du tenant. Elle est organisée en sections accessibles via une navigation secondaire verticale à gauche.
La section Identité permet de modifier le nom du tenant, uploader le logo, choisir les couleurs primaires du dashboard via un color picker, et saisir les informations légales et de contact.
La section Wallet configure la monnaie virtuelle (nom, symbole, taux de conversion), active ou désactive les opérations autorisées via des toggles, et configure toutes les limites de WalletPolicy via des champs numériques avec les unités clairement indiquées.
La section Notifications configure les templates de messages pour chaque type d'événement (crédit, débit, récompense, dégradation de palier…) avec des variables dynamiques insérables via des chips cliquables ({member_name}, {amount}, {reward_name}…). Un bouton "Envoyer un test" permet de prévisualiser la notification sur son propre compte.
La section Intégrations affiche les clés API du tenant avec la possibilité d'en générer de nouvelles et de révoquer les existantes, la liste des webhooks configurés avec leur URL et les événements écoutés, et un journal des derniers appels API avec leur statut pour le débogage.
La section Sécurité configure le seuil OTP pour les transactions wallet, les politiques de session et les adresses IP autorisées pour les appels API.

Univers 2 — Portail Membre
C'est l'interface que le client final d'une plateforme cliente utilise pour suivre sa fidélité. Elle peut être intégrée comme une page dans la plateforme cliente via un widget embarqué, ou accessible directement comme une PWA standalone. Elle hérite de la personnalisation visuelle du tenant — couleurs, logo, nom de la monnaie virtuelle.

Page 12 — Tableau de bord membre
C'est la page d'accueil du membre. En haut, une carte hero visuelle affiche son prénom, son avatar, son palier actuel avec l'icône correspondant, et une barre de progression vers le palier suivant avec le nombre de points manquants clairement indiqué. La carte change de couleur selon le palier — grise pour Bronze, argentée pour Silver, dorée pour Gold, platine pour Platinum.
En dessous, deux grandes cartes côte à côte affichent le solde de points avec le nombre exact formaté et l'équivalent en récompenses disponibles, et le solde wallet avec la devise virtuelle et un bouton "Recharger" direct.
Une section "Mes récompenses disponibles" affiche les trois premières récompenses actives non utilisées avec une présentation visuelle attractive — image, nom, date d'expiration surlignée en orange si elle approche, et un bouton "Utiliser". Un lien "Voir toutes mes récompenses" mène à la page dédiée.
Une section "Dernières activités" liste les dix derniers mouvements de points et de wallet de manière chronologique avec une icône colorée selon le type (flèche verte pour crédit, rouge pour débit), la description de l'action, le montant et la date.

Page 13 — Mon Wallet
Cette page est dédiée à la gestion du wallet du membre. En haut, une grande carte affiche le solde actuel en gros, le nom de la devise virtuelle, le statut du wallet et une indication "Équivalent à X XAF" si le taux de conversion est différent de 1.
Trois boutons d'action principaux : Recharger, Payer et Retirer (si activé par le tenant). Le bouton Recharger ouvre un panneau avec les options de recharge disponibles — chaque provider est présenté avec son logo (MTN, Orange, Stripe), un champ montant, et les limites rappelées en sous-texte. Le bouton Retirer ouvre un formulaire avec le compte de destination, le montant et un récapitulatif avant confirmation.
En dessous des boutons, l'historique complet des transactions du wallet dans un tableau filtrable par type et par période. Chaque transaction peut être développée pour afficher tous ses détails — référence, horodatage précis, statut, et lien vers la commande associée si disponible.

Page 14 — Mes Points et Récompenses
Cette page est en deux onglets. L'onglet Mes Points présente le solde disponible, le total gagné depuis l'inscription, le total dépensé, et une timeline visuelle des dernières attributions de points — quand, pour quelle action, combien. Si des points expirent prochainement, une alerte orange les signale avec le délai restant.
L'onglet Catalogue présente toutes les récompenses disponibles dans le catalogue du tenant, filtrable par type et triable par coût croissant ou décroissant. Chaque récompense est une carte avec une image, le nom, la description courte, le coût en points mis en valeur visuellement, et le stock restant si défini. Le bouton "Échanger" déclenche une confirmation modale qui rappelle le coût et le solde restant après échange, et demande confirmation avant de soumettre.

Page 15 — Mon Palier
Cette page détaille la progression du membre dans le programme de paliers. En haut, sa situation actuelle — palier, date d'atteinte, multiplicateur actif. En dessous, une visualisation horizontale de tous les paliers avec les avantages de chacun présentés sous forme de liste à cocher — les avantages déjà acquis sont cochés en vert, les avantages du palier suivant sont en grisé avec un cadenas.
Une section "Comment progresser" explique en termes simples combien de points il faut gagner encore pour le palier suivant et suggère des actions concrètes pour y parvenir rapidement.
Si le tenant a configuré une politique de maintien, une section "Maintien de mon palier" indique les points accumulés dans la période en cours, le seuil à atteindre et le temps restant avant la fin de la période — avec une barre de progression colorée.

Page 16 — Parrainage
Cette page présente le programme de parrainage du tenant de manière engageante. En haut, un bloc Hero explique les avantages — ce que le membre gagne et ce que son filleul gagne — avec des montants mis en valeur.
En dessous, le lien de parrainage du membre affiché dans un encadré avec un bouton "Copier" et des boutons de partage direct vers WhatsApp, SMS et les autres canaux disponibles. Un QR Code téléchargeable est également proposé.
La section "Mes filleuls" liste tous les contacts parrainés avec leur nom ou email partiel (pour la confidentialité), leur statut (Inscrit / En attente de conversion / Converti), et la récompense déjà reçue ou à venir. Un compteur en haut de cette section affiche le total de filleuls convertis avec fierté.

Notes techniques transversales
Chaque page utilise le système de layout Next.js avec des layouts imbriqués — un layout racine qui charge la TenantConfig et applique les variables CSS, un layout admin avec la sidebar de navigation, un layout membre avec la barre de navigation mobile en bas d'écran. Les données sont chargées côté serveur via les Server Components de Next.js pour les données statiques ou peu fréquemment mises à jour (configuration, catalogue), et via des Client Components avec SWR pour les données en temps réel (solde, notifications). Les WebSockets ou Server-Sent Events gèrent les mises à jour en temps réel du solde et des notifications sans que le membre ait à rafraîchir la page.
donne un descriptif detaillé de faire le systeme concernant le wallet , la promo et tout - on travaille en springboot - je veux une description detaillé de ce qui est attendu pour chaque module. donne moi du texte
25 mai
Backend Spring Boot — Description détaillée des modules

Vision générale de l'architecture backend
Le backend du Loyalty Programme est une application Spring Boot 3.4 construite sur le paradigme réactif avec Spring WebFlux et R2DBC. Tout est non-bloquant — aucun thread n'attend jamais une réponse d'une base de données ou d'un service externe. L'application est un monolithe modulaire organisé selon les principes du Domain-Driven Design, avec des frontières de domaine strictes entre les modules. Chaque module est autonome, possède sa propre couche de persistance et communique avec les autres exclusivement via des événements Kafka ou des interfaces de port bien définies. Cette architecture garantit qu'on peut extraire n'importe quel module en microservice indépendant le jour où la charge le justifie, sans réécriture.
L'ensemble de l'application est multi-tenant par conception. Chaque requête entrante porte un tenantId dans son token JWT. Ce tenantId est résolu dès l'entrée dans le système par un filtre WebFlux et propagé dans toute la chaîne réactive via le Context API de Project Reactor. Aucune couche de l'application n'a besoin de demander explicitement le tenant courant — il est toujours disponible dans le contexte réactif.

Module 1 — Infrastructure transversale
Ce module n'est pas un domaine métier mais le socle sur lequel tous les autres s'appuient. Il doit être le premier développé car tout en dépend.
1.1 Gestion du contexte multi-tenant
Le TenantWebFilter est un filtre WebFlux qui s'exécute avant tout traitement métier. Il intercepte chaque requête entrante, extrait le token JWT de l'en-tête Authorization, valide sa signature auprès de YowAuth0, et extrait le tenantId depuis les claims. Si le token est absent ou invalide, il retourne immédiatement un 401. Si le tenant est suspendu ou introuvable, il retourne un 403. Si tout est valide, il stocke le tenantId dans le Context Reactor de la requête et laisse passer vers le contrôleur.
Le TenantContextHolder expose une méthode statique getCurrentTenant() qui retourne un Mono contenant le tenantId depuis le Context Reactor courant. Cette méthode est appelée partout dans l'application — services, repositories, handlers — pour obtenir le tenant sans le passer explicitement en paramètre à chaque méthode. C'est la clé de la propreté du code multi-tenant.
Le TenantAwareConnectionFactory est un adaptateur R2DBC qui surcharge la sélection de la connexion à la base de données. À chaque demande de connexion, il lit le tenantId depuis le contexte Reactor et configure la connexion pour utiliser le schéma PostgreSQL correspondant à ce tenant. De l'extérieur, le code n'a pas conscience de travailler dans un schéma particulier — il fait ses requêtes normalement, et la connexion pointe automatiquement vers le bon schéma.
1.2 Sécurité
La SecurityConfig configure Spring Security pour une application WebFlux réactive. Elle désactive CSRF (inutile pour une API REST stateless), configure les routes publiques (webhooks de callback de paiement, health check, swagger) et exige un JWT valide pour toutes les autres routes. Elle configure également le Resource Server OAuth2 pour valider les tokens contre l'issuer-uri de YowAuth0.
Le JwtClaimsExtractor est un composant utilitaire qui parse les claims d'un JWT déjà validé pour en extraire les informations métier — tenantId, userId, roles, scopes. Il gère les différences de format entre les tokens émis pour les admins et ceux émis pour les membres finaux.
Le RoleBasedAccessFilter vérifie que l'utilisateur authentifié possède les droits suffisants pour l'opération demandée. Un membre final ne peut accéder qu'à ses propres données. Un admin peut accéder aux données de tous les membres de son tenant mais pas à ceux d'un autre tenant. Un super-admin Yowyob peut accéder à tous les tenants — uniquement pour le support.
1.3 Gestion des erreurs
Le GlobalExceptionHandler est un ControllerAdvice WebFlux qui intercepte toutes les exceptions non gérées et les traduit en réponses HTTP conformes au format RFC 7807 Problem Details. Chaque réponse d'erreur contient un type (URL identifiant le type d'erreur), un title (libellé court), un status (code HTTP), un detail (message explicatif pour le développeur), et un instance (identifiant unique de cette occurrence d'erreur pour le debugging). Il gère les exceptions métier — InsufficientBalanceException, WalletFrozenException, RuleValidationException — ainsi que les erreurs techniques — DataAccessException, TimeoutException, WebClientException.
1.4 Idempotence
L'IdempotencyFilter est un filtre WebFlux appliqué uniquement sur les méthodes POST et PATCH. Il lit l'en-tête Idempotency-Key obligatoire sur toutes les requêtes de mutation. Si la clé est absente, il retourne 400 avec un message explicite. Si la clé existe déjà dans Redis, il retourne immédiatement la réponse mise en cache avec le même code HTTP qu'à la première exécution. Si la clé est nouvelle, il laisse passer la requête et, après traitement, stocke la réponse dans Redis avec une TTL de 24 heures.
L'implémentation Redis utilise une structure SET NX (Set if Not eXists) qui garantit l'atomicité de la vérification et de l'insertion — deux requêtes concurrentes avec la même clé ne peuvent pas passer toutes les deux en même temps. C'est la protection contre les doubles soumissions en cas de problème réseau.
1.5 Observabilité
Le MicrometerConfig configure les métriques Micrometer exportées au format Prometheus. Les métriques personnalisées incluent le nombre de transactions par tenant et par type, le taux de succès et d'échec des appels aux providers de paiement, le temps de traitement des règles de fidélité, la taille des queues Kafka, et le nombre de wallets gelés par tenant. Ces métriques sont exposées sur l'endpoint /actuator/prometheus et scrapées par Prometheus toutes les 15 secondes.
Le StructuredLoggingConfig configure Logback pour produire des logs JSON structurés avec le tenantId, userId, requestId et traceId inclus automatiquement dans chaque ligne de log. Ces logs sont collectés par Logstash et indexés dans Elasticsearch pour être consultables via Kibana.

Module 2 — Module Wallet
Le Module Wallet est le composant financier du système. Il gère tout ce qui touche à la valeur monétaire ou quasi-monétaire — les soldes, les mouvements, les intégrations avec les providers de paiement externes et la sécurité des transactions.
2.1 Domaine Wallet
Le domaine est la couche la plus importante et la plus soignée de ce module. Elle ne contient aucune annotation Spring, aucune dépendance vers une librairie externe — uniquement du Java 21 pur. C'est ce qui la rend testable à 100% sans démarrer de contexte Spring.
La classe Wallet est l'agrégat racine. Elle encapsule toutes les règles métier qui gouvernent un portefeuille. Sa méthode credit() valide que le montant est positif, que le solde résultant ne dépasse pas le plafond configuré dans la WalletPolicy, et que le wallet est dans un état ACTIVE ou PENDING_KYC selon le type de crédit. Sa méthode debit() valide le solde suffisant, le statut ACTIVE, et délègue à la WalletPolicy la vérification du plafond journalier. Sa méthode freeze() transite l'état vers FROZEN et enregistre le motif. Sa méthode unfreeze() transite vers ACTIVE. Sa méthode close() transite vers CLOSED de manière irréversible. Ces méthodes ne font aucun accès base de données — elles manipulent uniquement l'état de l'objet en mémoire et retournent des événements de domaine décrivant ce qui s'est passé. La persistance est déléguée aux adapters.
La classe WalletPolicy est un Value Object immuable qui encapsule toutes les limites configurées par le tenant pour ce wallet. Sa méthode validateCredit() retourne soit une validation réussie, soit une liste d'erreurs explicites décrivant quelle limite est violée et de combien. Sa méthode validateDebit() fait de même pour les débits avec en plus la vérification du plafond journalier qui nécessite de connaître le total des débits du jour courant — ce total lui est passé en paramètre par le service.
La classe WalletTransaction est une entité immuable. Elle ne peut être créée qu'à travers une factory method statique qui valide que tous les champs obligatoires sont présents et cohérents. Une fois créée, aucun de ses champs ne peut être modifié. Elle porte les snapshots du solde avant et après l'opération, ce qui permet de reconstruire l'historique complet du solde à n'importe quel point dans le temps sans avoir à relire toutes les transactions précédentes.
Le FraudDetectionService analyse les patterns des transactions récentes d'un wallet pour détecter des comportements anormaux. Il reçoit en paramètre la liste des transactions des dernières 24 heures et la transaction sur le point d'être validée. Il applique plusieurs règles en séquence — fréquence des débits, montant inhabituel par rapport à la moyenne historique, délai entre crédit et retrait. Il retourne soit un FraudAssessment.clean() soit un FraudAssessment.suspicious() avec le motif détecté. Ce service est appelé par le DebitWalletHandler avant d'exécuter le débit.
2.2 Handlers applicatifs
Le CreditWalletHandler orchestre le cas d'utilisation de crédit. Il commence par vérifier l'idempotence via l'IdempotencyPort. Il charge le Wallet depuis le repository. Il charge la WalletPolicy du tenant. Il appelle Wallet.credit() pour valider et produire les événements de domaine. Il persiste la WalletTransaction créée. Il met à jour le solde du Wallet. Il publie un WalletCreditedEvent sur Kafka. Il stocke la réponse dans le cache d'idempotence. Il retourne la réponse au contrôleur. Tout cela est enchaîné dans une chaîne réactive Reactor — Mono et Flux — sans aucun bloc bloquant.
Le DebitWalletHandler orchestre le débit avec un niveau de complexité supérieur car il doit gérer l'OTP. Si le montant dépasse le seuil OTP configuré, le handler interrompt le flux normal et publie un OTPChallengeRequiredEvent. La plateforme cliente reçoit une réponse 202 Accepted avec un challenge_id. Elle demande à l'utilisateur de saisir l'OTP reçu par SMS. Elle renvoie l'OTP avec le challenge_id sur un endpoint de confirmation. Le OTPConfirmationHandler valide l'OTP auprès de YowAuth0 et, si valide, reprend l'exécution du débit depuis le point d'interruption avec la même idempotency_key originale.
Le WithdrawHandler gère le retrait vers Mobile Money. Il est plus complexe que le crédit car il implique un état intermédiaire de réservation. Il commence par vérifier que le wallet a passé le KYC, que le délai post-crédit est respecté, que le solde est suffisant. Il crée une PaymentRequest en statut INITIATED. Il réserve le montant sur le wallet en créant une WalletTransaction de type RESERVE — le solde disponible diminue mais le montant est marqué comme réservé, pas encore sorti. Il appelle le PaymentGatewayPort avec les détails du retrait. Il met à jour la PaymentRequest en PENDING. À ce stade il retourne 202 Accepted à la plateforme cliente — le traitement continue de manière asynchrone.
Le WebhookHandler traite les callbacks des providers de paiement. Quand MTN confirme un paiement, ce handler est appelé. Il retrouve la PaymentRequest concernée via l'external_ref du provider. Si c'était un crédit, il confirme la WalletTransaction en COMPLETED et met à jour le solde. Si c'était un retrait, il confirme la libération de la réservation. Si c'était un échec, il annule la réservation et restitue le solde. Dans tous les cas il met à jour la PaymentRequest et publie l'événement Kafka correspondant.
2.3 Persistance R2DBC
Les entités R2DBC sont des classes Java annotées avec @Table, @Id et @Column de Spring Data R2DBC. Elles sont distinctes des classes du domaine — un WalletEntity n'est pas un Wallet. Les mappers convertissent dans les deux sens. Cette séparation préserve la pureté du domaine et permet de faire évoluer le schéma de base de données sans toucher aux règles métier.
Les WalletR2dbcRepository et WalletTransactionR2dbcRepository sont des interfaces qui étendent ReactiveCrudRepository. Elles déclarent des méthodes de requête nommées pour les cas simples — findByMemberIdAndTenantId(), findByWalletIdAndTypeAndCreatedAtBetween(). Pour les requêtes plus complexes comme le calcul du total des débits du jour ou la détection de patterns de fraude, elles utilisent l'annotation @Query avec du SQL natif. Ces requêtes exploitent les capacités analytiques de PostgreSQL 18 — window functions, CTEs — pour calculer en base ce qui serait inefficace à calculer en mémoire Java.
Les adapters d'implémentation des ports de sortie font le lien entre les interfaces du domaine et les repositories Spring Data. WalletRepositoryAdapter implémente WalletRepository du domaine en déléguant à WalletR2dbcRepository et en utilisant WalletMapper pour les conversions.
2.4 Intégration Redis
L'IdempotencyAdapter implémente IdempotencyPort en utilisant ReactiveRedisTemplate. La méthode checkAndMark() utilise une commande Redis SET NX EX atomique — elle tente d'insérer la clé avec une valeur de placeholder et une TTL de 24 heures. Si l'insertion réussit, la clé est nouvelle et le traitement peut continuer. Si elle échoue, la clé existe déjà et le handler récupère la réponse mise en cache. La mise en cache de la réponse se fait après le traitement avec une deuxième commande SET qui remplace le placeholder par la réponse sérialisée en JSON.
Le WalletCacheAdapter gère le cache des soldes fréquemment lus. Il cache le solde courant de chaque wallet avec une TTL courte de 30 secondes — suffisant pour absorber les pics de lecture sans risquer d'afficher un solde trop obsolète. Le cache est invalidé immédiatement après chaque mutation du wallet. Les clés Redis sont toutes préfixées par le tenantId : loyalty:{tenantId}:wallet:{walletId}:balance.
2.5 Intégration Kafka
Le WalletEventProducer est un composant qui sérialise les événements de domaine en JSON et les publie sur les topics Kafka correspondants. Chaque événement est publié sur le topic wallet.events.{tenantId}. Le message Kafka contient l'événement sérialisé dans le body et des headers avec le tenantId, le type d'événement, la version du schéma et un correlationId pour le tracing distribué.
Le PaymentWebhookConsumer est un consommateur Kafka qui traite les événements de confirmation de paiement émis par les adaptateurs de payment gateway. Il est configuré avec un consumer group dédié wallet-service-payment-confirmations pour garantir que chaque événement est traité exactement une fois même en cas de redémarrage du service.
2.6 Adaptateurs de paiement
MtnMobileMoneyAdapter implémente PaymentGatewayPort pour les transactions MTN. Il encapsule le WebClient réactif configuré pour appeler l'API MTN MOMO. Sa méthode initiatePayment() construit la requête selon le format attendu par MTN, gère l'authentification OAuth2 propre à MTN en maintenant un token en cache Redis, appelle l'endpoint d'initiation, et mappe la réponse vers notre modèle interne PaymentResult. Elle gère les cas d'erreur spécifiques à MTN — numéro non enregistré, solde insuffisant côté utilisateur, timeout — avec des messages d'erreur traduits en termes compréhensibles par notre système.
OrangeMoneyAdapter fait de même pour Orange Money avec les spécificités de leur API — Orange a une structure d'authentification différente et des codes d'erreur différents.
StripeAdapter gère les paiements par carte internationale. Il utilise le SDK officiel Stripe pour Java en mode réactif. Il gère les webhooks Stripe qui arrivent signés avec le secret de webhook — la validation de la signature est faite avant tout traitement pour prévenir les faux callbacks.
2.7 Contrôleurs REST
Le WalletController est un RouterFunction WebFlux qui définit les routes du module wallet pour les membres finaux. POST /api/v1/wallet/topup initie une recharge. POST /api/v1/wallet/debit débite pour un paiement. GET /api/v1/wallet/balance retourne le solde. GET /api/v1/wallet/transactions retourne l'historique paginé avec les paramètres de filtre en query string. POST /api/v1/wallet/withdraw initie un retrait. POST /api/v1/wallet/otp/confirm confirme un débit nécessitant un OTP.
Le WalletAdminController expose les routes d'administration. POST /api/v1/admin/wallet/{walletId}/freeze gèle un wallet. POST /api/v1/admin/wallet/{walletId}/unfreeze dégèle. POST /api/v1/admin/wallet/{walletId}/adjust fait un ajustement manuel avec motif obligatoire. GET /api/v1/admin/wallet/reconcile lance la réconciliation. GET /api/v1/admin/wallet/suspicious liste les transactions suspectes.
Le WebhookController expose les endpoints de callback pour les providers de paiement. Ces endpoints sont publics — pas de JWT requis — mais chaque provider a son propre mécanisme de validation de signature que le contrôleur vérifie avant tout traitement.

Module 3 — Module Fidélisation et Moteur de Règles
Ce module est le cerveau de la plateforme. Il écoute tous les événements métier émis par les plateformes clientes, évalue les règles configurées par chaque tenant, et déclenche les effets correspondants.
3.1 Domaine du moteur de règles
La classe Rule est l'agrégat central. Elle est composée d'un Trigger, d'une liste de Conditions et d'une liste d'Effects. Sa méthode evaluate() reçoit un EvaluationContext — l'objet qui encapsule toutes les données disponibles au moment de l'évaluation : l'Event entrant, le Member, son PointsAccount, son Wallet, son MemberTier, ses Counters actifs — et retourne un EvaluationResult qui indique si la règle s'est déclenchée et quels effets ont été appliqués.
La classe Trigger implémente une méthode matches() qui prend un Event en paramètre. Elle compare le type de l'Event avec le type configuré sur le Trigger. Si les types correspondent, elle évalue les filtres — des prédicats JSON configurables. Un filtre peut spécifier que le champ payload.category doit être égal à "premium", ou que le champ payload.amount doit être supérieur à 5000. L'évaluation des filtres utilise une bibliothèque d'évaluation d'expressions JSONPath. Si tous les filtres passent, matches() retourne true.
Chaque sous-classe de Condition implémente une méthode evaluate() qui prend l'EvaluationContext et retourne true ou false. CumulativeCountCondition interroge le Counter correspondant dans le contexte et compare sa valeur à la valeur seuil configurée. PointsBalanceCondition lit le solde du PointsAccount dans le contexte. TierCondition compare le niveau du MemberTier. TimeWindowCondition évalue l'heure et le jour de la semaine courants. Chaque sous-classe ne contient que sa propre logique — pas d'if-else sur le type dans une classe parente.
De même, chaque sous-classe d'Effect implémente une méthode apply() qui prend l'EvaluationContext et retourne un Flux de DomainEvent décrivant ce qui a été fait. CreditPointsEffect calcule le nombre de points à attribuer en tenant compte du multiplicateur du palier courant du membre, crée un PointsTransaction et retourne un PointsEarnedEvent. GrantRewardEffect charge la Reward depuis le contexte, vérifie le stock, crée un RewardGrant et retourne un RewardGrantedEvent. ResetCounterEffect remet à zéro le Counter spécifié et retourne un CounterResetEvent. Ce pattern Command permet d'ajouter un nouveau type d'Effect sans toucher au code existant — il suffit de créer une nouvelle sous-classe.
Le RuleEngine est le chef d'orchestre. Sa méthode process() reçoit un Event et un EvaluationContext préchargé. Il charge toutes les Rules actives du tenant depuis le RuleCache — un cache Redis qui stocke les règles sérialisées pour éviter de les relire en base à chaque event. Il trie les règles par priorité décroissante. Il itère sur chaque règle, évalue le Trigger, évalue les Conditions si le Trigger matche, et applique les Effects si toutes les Conditions sont vraies. Il collecte tous les DomainEvents produits par les Effects. Après avoir traité toutes les règles, il incrémente les Counters de toutes les règles cumulatives dont le Trigger a matché — cette incrémentation se fait indépendamment du déclenchement ou non des conditions. Il retourne un ProcessingResult contenant la liste de tous les events produits.
3.2 Gestion des Points
Le PointsAccount est l'agrégat qui maintient le solde de points d'un membre. Il expose une méthode earn() qui crée une PointsTransaction de type CREDIT et met à jour available_points et lifetime_earned. Il expose une méthode spend() qui vérifie que le solde disponible est suffisant, crée une PointsTransaction de type DEBIT et met à jour available_points. Il expose une méthode expire() qui soustrait les points expirés du solde disponible sans affecter lifetime_earned.
Le PointsExpiryScheduler est un job Spring Scheduler qui s'exécute chaque nuit à minuit. Il interroge la base de données pour identifier les lots de points qui expirent dans les 30 prochains jours — pour les notifications — et ceux qui ont déjà expiré. Pour les prochains, il publie des ExpiryWarningEvents qui déclenchent des notifications aux membres. Pour les expirés, il appelle PointsAccount.expire() et crée les PointsTransactions correspondantes.
3.3 Gestion des Paliers
Le TierCalculationService est appelé après chaque attribution de points. Il charge la TierPolicy du tenant, calcule le nouveau palier du membre en fonction du critère configuré — points cumulés, montant dépensé ou nombre d'achats — et compare avec le palier actuel. Si le nouveau palier est différent, il met à jour le MemberTier et publie un TierChangedEvent. Si c'est une montée, il publie un TierUpgradedEvent qui peut déclencher des Effects supplémentaires.
Le TierMaintainanceScheduler vérifie quotidiennement les membres qui approchent de la fin de leur période de maintien. Pour les membres en risque de dégradation, il publie des TierDegradationWarningEvents et, quand la période expire réellement et que le seuil n'est pas atteint, il exécute la dégradation et publie un TierDowngradedEvent.
3.4 Gestion du Catalogue et des Récompenses
Le RewardCatalogService gère le cycle de vie des Reward du tenant. Il expose des méthodes réactives pour créer, modifier, activer, désactiver et archiver les récompenses. La création valide que le reward_id est unique dans le tenant, que le coût en points est positif, et que si un stock est défini il est supérieur à zéro.
Le RedemptionService orchestre l'échange de points contre une récompense. Il charge la Reward et vérifie sa disponibilité. Il charge le PointsAccount et vérifie le solde. Il exécute le débit des points et la création du RewardGrant dans une transaction PostgreSQL unique pour garantir l'atomicité. Si le débit réussit mais la création du grant échoue, le rollback PostgreSQL restitue les points. Si la récompense a un stock défini, il utilise un SELECT FOR UPDATE pour verrouiller la ligne Reward pendant la décrémentation du stock — c'est la protection contre la survente concurrente.
3.5 Traitement des Events entrants
Le LoyaltyEventConsumer est un consommateur Kafka qui écoute le topic business.events.{tenantId} pour tous les tenants. Il est configuré avec des consumer groups dynamiques basés sur le tenantId pour garantir la scalabilité horizontale — on peut ajouter des instances du service et Kafka distribuera automatiquement les partitions entre elles.
Pour chaque Event reçu, le consumer construit l'EvaluationContext en chargeant en parallèle les données nécessaires — le Member, son PointsAccount, son Wallet, son MemberTier et ses Counters actifs — en utilisant Mono.zip() de Reactor pour les charger simultanément sans attendre séquentiellement chaque chargement. Il passe ensuite le contexte au RuleEngine. Il persiste les résultats. Il publie les DomainEvents produits sur le topic loyalty.events.{tenantId}.
3.6 Gestion des Counters
Le CounterService maintient les compteurs cumulatifs par membre et par règle. Il utilise Redis comme stockage principal pour les compteurs actifs — la performance est critique car chaque event entrant peut incrémenter plusieurs compteurs. L'incrémentation est atomique via la commande Redis INCR. La valeur en Redis est la source de vérité pour les évaluations en temps réel. Une synchronisation périodique vers PostgreSQL garantit la durabilité — si Redis est redémarré, les compteurs peuvent être reconstruits depuis PostgreSQL.

Module 4 — Module Promotions et Codes Promo
Ce module gère tout ce qui relève de la promotion ponctuelle et configurable — codes promo, campagnes temporisées et offres ciblées.
4.1 Domaine Promo
La classe PromoCampaign est l'agrégat du domaine promo. Sa méthode validate() reçoit un PromoValidationContext contenant le membre, le montant de la commande et l'horodatage courant. Elle évalue les règles dans un ordre précis et déterministe. Elle vérifie d'abord la période de validité temporelle — si la date courante est hors fenêtre, elle retourne une ValidationFailure avec le motif EXPIRED. Elle vérifie ensuite les limites d'utilisation globales via le compteur Redis associé à cette campagne. Elle vérifie la limite par membre via une requête sur PromoUsage. Elle vérifie le montant minimum de commande. Elle vérifie l'éligibilité du palier du membre. Elle vérifie l'éligibilité du segment. Si tous les contrôles passent, elle calcule le PriceBreakdown et retourne un ValidationSuccess.
La classe PriceBreakdown est un Value Object qui encapsule le détail complet du calcul de réduction. Elle contient le montant original, la liste des réductions appliquées avec pour chacune sa source, son type et son montant, et le montant final. Elle expose une méthode stackWith() qui applique une deuxième réduction sur le résultat courant selon la stacking_policy configurée.
4.2 Service de validation et d'application
Le PromoValidationService est le point d'entrée pour la validation d'un code promo. Il est appelé par la plateforme cliente avant de finaliser une commande. Il charge la PromoCampaign, construit le PromoValidationContext, appelle PromoCampaign.validate() et retourne le résultat. Si la validation est réussie, il ne crée pas encore le PromoUsage — la validation est une lecture, pas une écriture. Cela permet à la plateforme cliente d'afficher le prix réduit à l'utilisateur avant confirmation sans créer d'utilisations fantômes si l'utilisateur abandonne.
Le PromoApplicationService est appelé quand la plateforme cliente confirme que la commande a été passée avec le code promo. Il revalide le code — les conditions peuvent avoir changé entre la validation et l'application, notamment le stock. Il incrémente le compteur Redis de manière atomique. Il crée le PromoUsage en base. Il publie un PromoAppliedEvent sur Kafka.
4.3 Gestion des Campagnes
Le CampaignService gère le cycle de vie des Campaign. Sa méthode createCampaign() valide les paramètres, persiste la Campaign en statut SCHEDULED et programme son activation dans le CampaignScheduler. Sa méthode activateCampaign() charge les règles affectées par la campagne, crée des versions temporaires modifiées, suspend les originales et invalide le cache des règles du tenant. Sa méthode deactivateCampaign() supprime les versions temporaires et restaure les originales.
Le CampaignScheduler utilise Spring Scheduling avec des tâches planifiées qui vérifient toutes les minutes les campagnes dont l'heure d'activation ou de désactivation est imminente. Il utilise Redis comme verrou distribué — si plusieurs instances de l'application tournent en parallèle, une seule doit activer chaque campagne. Le verrou est acquis via Redis SET NX avec une TTL courte avant chaque activation.
4.4 Codes promo en masse
Le BulkPromoGenerationService génère des lots de codes promo uniques pour les campagnes de distribution par email ou SMS. Il génère les codes sous la forme d'une combinaison alphanumérique en évitant les caractères ambigus (0/O, 1/I/l). Il vérifie l'unicité de chaque code dans le tenant avant insertion. La génération est asynchrone — pour un lot de 10 000 codes, elle est soumise comme une tâche de fond et le résultat est disponible via un endpoint de polling.

Module 5 — Module Parrainage
5.1 Domaine Parrainage
Le ReferralProgram est le Value Object de configuration défini par le tenant. Il encapsule les règles du programme — conditions de conversion, récompenses, délais.
Le ReferralEvent est l'agrégat qui suit un parrainage individuel du début à la fin. Sa méthode attemptConversion() vérifie que le filleul est légitime en contrôlant les critères anti-fraude — même numéro de téléphone que le parrain, même IP d'inscription — et que l'event de conversion remplit les conditions du ReferralProgram. Si tout est valide, il transite vers CONVERTED et retourne un ReferralConvertedEvent. Sa méthode reward() est appelée après que les récompenses ont été attribuées avec succès — elle transite vers REWARDED.
5.2 Service de parrainage
Le ReferralService orchestre tout le cycle de vie. Sa méthode generateLink() vérifie si un ReferralLink actif existe déjà pour ce membre dans ce tenant. Si oui, il le retourne. Si non, il génère un code en s'assurant de son unicité via une requête en base, persiste le ReferralLink et le retourne.
Sa méthode processEnrollment() est appelée quand un nouveau membre s'inscrit avec un code de parrainage. Il résout le code vers le parrain, vérifie que le parrain est bien membre du même tenant, crée le ReferralEvent en PENDING et le persiste.
Sa méthode processConversion() est appelée par le LoyaltyEventConsumer quand un event de conversion qualifiant est reçu pour un membre qui a un ReferralEvent en PENDING. Il charge le ReferralEvent, appelle attemptConversion(), et si réussi publie un ReferralConvertedEvent. Ce dernier est consommé par le module de fidélisation qui applique les récompenses configurées dans le ReferralProgram pour le parrain et le filleul via le moteur de règles.

Module 6 — Module Notification
Ce module est un consommateur pur — il ne produit aucun événement métier, il ne fait que réagir aux événements des autres modules pour envoyer des messages aux membres.
6.1 Architecture du module
Le NotificationEventConsumer écoute le topic notification.commands.{tenantId}. Pour chaque commande reçue, il charge le template de notification configuré par le tenant pour ce type d'événement depuis le NotificationTemplateRepository. Il résout les variables dynamiques du template — {member_name}, {amount}, {balance}, {reward_name} — en les remplaçant par les valeurs concrètes présentes dans le payload de l'événement. Il détermine les canaux à utiliser selon la configuration du tenant et les préférences du membre. Il appelle les adapters correspondants.
6.2 Adapters de livraison
Le FcmAdapter envoie des notifications push via Firebase Cloud Messaging pour les membres qui ont installé l'application mobile et accordé la permission de notification. Il gère les tokens FCM — qui peuvent expirer ou devenir invalides — en les nettoyant automatiquement quand Firebase signale qu'un token n'est plus valide.
Le TwilioAdapter envoie des SMS via l'API Twilio. Il formate le message pour respecter la limite de 160 caractères, avec une troncature intelligente qui préserve les informations essentielles (montant, solde) en priorité.
Le SmtpAdapter envoie des emails via SMTP. Il utilise des templates HTML Thymeleaf hébergés dans MinIO, un par type de notification et par tenant. Les templates sont téléchargeables et personnalisables par les tenants Pro et Enterprise.
6.3 Gestion des échecs
Chaque tentative d'envoi crée un NotificationLog. Si l'envoi échoue, le NotificationRetryService programme des retry avec backoff exponentiel — retry après 1 minute, puis 5 minutes, puis 30 minutes, puis 2 heures. Après 4 échecs, la notification est marquée FAILED et une alerte est envoyée à l'administrateur du tenant si le taux d'échec dépasse 5%.

Module 7 — Module Configuration Admin
Ce module expose les APIs que le dashboard d'administration utilise pour configurer le tenant. Il est distinct des autres modules car son audience est différente — des administrateurs humains, pas des systèmes automatisés.
7.1 Configuration du Wallet
Le TenantWalletConfigController expose des routes CRUD pour la WalletPolicy. La modification d'une limite prend effet immédiatement pour les nouvelles transactions. Le controller valide que les limites sont cohérentes entre elles — le plafond de dépense journalier ne peut pas être supérieur au plafond de solde, le montant minimum de retrait ne peut pas être supérieur au solde maximum.
7.2 Gestion des règles
Le RuleConfigController expose les routes pour créer, modifier, activer, désactiver et archiver les règles. Le RuleValidator est appelé sur chaque soumission — il vérifie la cohérence de la structure de la règle, l'existence des reward_id référencés, l'absence de conflits de priorité, et la faisabilité des conditions (une condition de type cumulative_count ne peut pas avoir une fenêtre temporelle incompatible avec son type). Une réponse de validation positive retourne également la phrase de résumé en langage naturel générée automatiquement depuis la structure de la règle — c'est cette phrase qui s'affiche dans le dashboard.
7.3 Analytics et rapports
Le AnalyticsController expose des endpoints de lecture qui agrègent des données depuis Elasticsearch. Ces endpoints sont séparés des endpoints de lecture standard car ils peuvent être lents — ils effectuent des agrégations sur des millions de documents. Ils sont cachés agressivement dans Redis avec des TTL de 5 à 15 minutes selon la nature de la métrique. Un paramètre force_refresh permet à l'admin de forcer le recalcul si nécessaire.

Organisation des tests
Les tests sont organisés en trois niveaux. Les tests unitaires du domaine testent chaque classe du domaine isolément avec des données en mémoire — aucun mock de base de données, aucun contexte Spring. Ils s'exécutent en millisecondes et couvrent tous les cas limites métier. Les tests des handlers testent l'orchestration des cas d'utilisation en mockant les ports de sortie — ils vérifient que les bons appels sont faits dans le bon ordre. Les tests d'intégration utilisent Testcontainers pour démarrer de vrais conteneurs PostgreSQL, Redis et Kafka, et testent les flux complets depuis le contrôleur REST jusqu'à la base de données et retour.
DANS CETTE CONCEPTION QU'ON FAIT DEPUIS , est ce qu'on prend en copte le fait que l'architecture doit ertre hexagoanl? si oui, comment savoir et tester tout au long de notre travail et à la fin
26 mai
Oui, on la respecte. Mais laisse-moi d'abord te montrer comment, puis comment la vérifier.

Est-ce qu'on respecte l'architecture hexagonale ?
Oui, et voilà les preuves concrètes dans ce qu'on a déjà conçu.
L'architecture hexagonale repose sur un seul principe fondamental : le domaine métier ne dépend de rien. Ni de Spring, ni de PostgreSQL, ni de Redis, ni de Kafka. C'est lui qui dicte les interfaces — appelées ports — et le monde extérieur s'adapte à lui via des adapters.
Regarde ce qu'on a fait.
Dans le Module Wallet, on a dit que Wallet.java, WalletTransaction.java, WalletPolicy.java ne contiennent aucune annotation Spring, aucune dépendance externe. Ce sont des classes Java pures. C'est exactement l'hexagone — le noyau isolé.
On a dit que WalletRepository.java dans domain/wallet/port/out/ est une interface. Pas une implémentation. Le domaine déclare ce dont il a besoin en termes abstraits — "je veux pouvoir sauvegarder un wallet" — sans savoir si c'est PostgreSQL, MongoDB ou une liste en mémoire derrière. C'est le port de sortie.
On a dit que CreditWalletUseCase.java dans domain/wallet/port/in/ est une interface qui représente ce que le monde extérieur peut demander au domaine. C'est le port d'entrée.
On a dit que WalletRepositoryAdapter.java dans infrastructure/persistence/wallet/adapter/ implémente WalletRepository en utilisant WalletR2dbcRepository. C'est l'adapter de sortie — il branche le monde réel (PostgreSQL via R2DBC) sur le port du domaine.
On a dit que WalletController.java dans api/wallet/ appelle les use cases. C'est l'adapter d'entrée — il branche le monde réel (HTTP) sur le port du domaine.
La structure est donc bien hexagonale. Mais avoir la bonne structure de fichiers ne suffit pas. Il faut des garde-fous pour que ça reste hexagonal tout au long du développement.

Comment vérifier tout au long du développement
Règle 1 — La règle d'or des imports
Le package domain ne doit jamais importer quoi que ce soit hors de domain.
C'est la règle la plus simple et la plus puissante. Si tu ouvres n'importe quel fichier dans com.yowyob.loyalty.domain et que tu vois un import qui commence par org.springframework, io.r2dbc, org.apache.kafka, com.redis ou n'importe quelle librairie externe — l'architecture hexagonale est violée.
Les seuls imports autorisés dans le domaine sont vers d'autres classes du domaine et vers le JDK standard (java.util, java.time, java.math).
Comment vérifier manuellement : dans IntelliJ, fais un clic droit sur le package domain → Analyze → Dependencies. Tu verras immédiatement si des dépendances externes ont été introduites.

Règle 2 — ArchUnit dans les tests
ArchUnit est une librairie Java qui permet d'écrire des tests d'architecture qui s'exécutent comme des tests JUnit normaux. On écrit les règles architecturales en Java et elles échouent si le code les viole. C'est le meilleur outil pour maintenir l'hexagonale sur la durée.
Voilà exactement ce qu'on écrit dans notre projet.
java
// test/architecture/HexagonalArchitectureTest.java

@AnalyzeClasses(packages = "com.yowyob.loyalty")
public class HexagonalArchitectureTest {

    // ── Règle 1 : Le domaine ne dépend de rien d'externe ──────────────────
    @ArchTest
    static final ArchRule domain_should_not_depend_on_spring =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "org.springframework..",
                "io.r2dbc..",
                "org.apache.kafka..",
                "io.lettuce..",
                "org.postgresql.."
            )
            .because("Le domaine doit être pur — zéro dépendance infrastructure");

    // ── Règle 2 : Le domaine ne dépend pas de l'application ───────────────
    @ArchTest
    static final ArchRule domain_should_not_depend_on_application =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("..application..")
            .because("Le domaine ne connaît pas la couche application");

    // ── Règle 3 : Le domaine ne dépend pas de l'infrastructure ────────────
    @ArchTest
    static final ArchRule domain_should_not_depend_on_infrastructure =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("..infrastructure..")
            .because("Le domaine ne connaît pas l'infrastructure");

    // ── Règle 4 : Le domaine ne dépend pas de l'API ───────────────────────
    @ArchTest
    static final ArchRule domain_should_not_depend_on_api =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("..api..")
            .because("Le domaine ne connaît pas la couche API");

    // ── Règle 5 : L'infrastructure dépend du domaine, pas l'inverse ───────
    @ArchTest
    static final ArchRule infrastructure_depends_on_domain_not_reverse =
        noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAPackage("..infrastructure..")
            .because("C'est l'infra qui dépend du domaine, jamais l'inverse");

    // ── Règle 6 : L'API dépend de l'application, pas du domaine direct ────
    @ArchTest
    static final ArchRule api_should_only_call_application_layer =
        noClasses()
            .that().resideInAPackage("..api..")
            .should().dependOnClassesThat()
            .resideInAPackage("..domain.service..")
            .because("L'API passe par les use cases, pas par les services domaine directement");

    // ── Règle 7 : Les ports IN sont des interfaces ─────────────────────────
    @ArchTest
    static final ArchRule ports_in_should_be_interfaces =
        classes()
            .that().resideInAPackage("..domain..port.in..")
            .should().beInterfaces()
            .because("Les ports d'entrée sont des contrats, donc des interfaces");

    // ── Règle 8 : Les ports OUT sont des interfaces ────────────────────────
    @ArchTest
    static final ArchRule ports_out_should_be_interfaces =
        classes()
            .that().resideInAPackage("..domain..port.out..")
            .should().beInterfaces()
            .because("Les ports de sortie sont des contrats, donc des interfaces");

    // ── Règle 9 : Les adapters implémentent les ports ─────────────────────
    @ArchTest
    static final ArchRule adapters_should_implement_ports =
        classes()
            .that().resideInAPackage("..infrastructure..adapter..")
            .should().implement(
                resideInAPackage("..domain..port.out..")
            )
            .because("Un adapter de sortie doit implémenter un port de sortie du domaine");

    // ── Règle 10 : Les entités domaine n'ont pas d'annotations JPA/R2DBC ──
    @ArchTest
    static final ArchRule domain_models_have_no_persistence_annotations =
        noClasses()
            .that().resideInAPackage("..domain.model..")
            .should().beAnnotatedWith("org.springframework.data.relational.core.mapping.Table")
            .orShould().beAnnotatedWith("javax.persistence.Entity")
            .because("Les modèles domaine ne sont pas des entités de persistance");
}
Ces tests s'exécutent avec mvn test comme n'importe quel test JUnit. Si quelqu'un introduit une dépendance Spring dans le domaine, le test échoue immédiatement avec un message explicite.

Règle 3 — La checklist par fichier
À chaque nouveau fichier créé, pose-toi ces questions selon où il se trouve.
Si tu crées un fichier dans domain/ :
Est-ce qu'il contient une annotation @Service, @Component, @Repository ? → Violation
Est-ce qu'il importe quelque chose de Spring ? → Violation
Est-ce qu'il importe une librairie de persistance ? → Violation
Est-ce que ses méthodes retournent Mono ou Flux ? → Violation (le domaine retourne des types Java purs, pas des types Reactor)
Si tu crées un fichier dans infrastructure/ :
Est-ce qu'il implémente une interface du domaine ? → Obligatoire pour les adapters
Est-ce qu'il importe directement des classes du domaine service/ ? → Violation — il ne doit voir que les ports et les modèles
Est-ce qu'il contient de la logique métier ? → Violation — la logique est dans le domaine
Si tu crées un fichier dans api/ :
Est-ce qu'il appelle directement un service du domaine ? → Violation — il doit passer par un use case de la couche application
Est-ce qu'il contient de la logique métier ? → Violation
Si tu crées un fichier dans application/ :
Est-ce qu'il contient de la logique métier complexe ? → Violation — il orchestre, il ne calcule pas

Règle 4 — Le test de la substituabilité
C'est le test mental le plus puissant. À tout moment, pose-toi cette question :
"Si je remplace PostgreSQL par MongoDB demain, combien de fichiers dans domain/ dois-je modifier ?"
La réponse correcte est zéro. Si la réponse est non-zéro, l'hexagonale est violée quelque part.
Même question avec Redis, Kafka, MTN Mobile Money, Spring — tous doivent pouvoir être remplacés sans toucher au domaine.
En pratique : ce test est automatisé par les tests unitaires du domaine. Puisque les tests du domaine n'utilisent que des implémentations en mémoire des ports de sortie (des fakes, pas des mocks), ils prouvent que le domaine fonctionne parfaitement sans aucune infrastructure réelle.

Comment vérifier à la fin du projet
Vérification 1 — Dependency Structure Matrix
IntelliJ Ultimate génère une DSM (Dependency Structure Matrix). Elle montre en un coup d'oeil toutes les dépendances entre packages. Dans une architecture hexagonale correcte, la matrice doit être triangulaire inférieure — les dépendances vont toujours dans un seul sens, jamais dans les deux.
             domain  application  infrastructure  api
domain          ■
application     →       ■
infrastructure  →                   ■
api             →       →                           ■
Si tu vois une flèche dans le sens inverse — domain → infrastructure, ou domain → api — c'est une violation.
Vérification 2 — SonarQube avec plugin ArchUnit
Le plugin ArchUnit pour SonarQube intègre les violations d'architecture dans le Quality Gate de la CI/CD. Un merge qui introduit une violation architecturale est bloqué automatiquement sur la pipeline GitLab.
Dans le .gitlab-ci.yml :
yaml
architecture_check:
  stage: test
  script:
    - mvn test -Dtest=HexagonalArchitectureTest
  rules:
    - if: '$CI_MERGE_REQUEST_ID'
  allow_failure: false  # Bloque le merge si les tests échouent
Vérification 3 — Le test d'isolation du domaine
Ce test prouve que tout le domaine s'exécute sans infrastructure. On crée un test de démarrage qui instancie tous les services du domaine avec des implémentations en mémoire des ports et vérifie qu'ils fonctionnent.
java
// test/domain/DomainIsolationTest.java

public class DomainIsolationTest {

    // Fakes en mémoire — aucune dépendance Spring, Redis ou PostgreSQL
    private WalletRepository walletRepo = new InMemoryWalletRepository();
    private IdempotencyPort idempotency = new InMemoryIdempotencyStore();
    private WalletEventPublisherPort events = new NoOpEventPublisher();

    @Test
    void wallet_domain_works_without_any_infrastructure() {
        // Arrange
        WalletDomainService service = new WalletDomainService(
            walletRepo, idempotency, events
        );
        Wallet wallet = Wallet.create(
            UUID.randomUUID(), UUID.randomUUID(), "RC", WalletPolicy.defaults()
        );
        walletRepo.save(wallet);

        // Act — crédit de 5000 RC
        wallet.credit(new BigDecimal("5000"), "idem-key-001");

        // Assert
        assertThat(wallet.getBalance()).isEqualByComparingTo("5000");

        // Zéro Spring, zéro PostgreSQL, zéro Redis — le domaine tourne seul
    }
}
Si ce test passe, le domaine est bien isolé.
Vérification 4 — Revue de la liste des dépendances Maven
Dans le pom.xml, chaque dépendance non-test doit être justifiée par la couche qui l'utilise. Les dépendances Spring Boot, R2DBC, Redis, Kafka ne doivent apparaître que dans les modules infrastructure et api — si le projet est découpé en modules Maven séparés, c'est automatiquement garanti. Si c'est un seul module Maven, les tests ArchUnit font office de garde-fou.

Résumé — Ce qu'on fait à chaque étape
Pendant le développement, après chaque nouveau fichier : vérifier les imports manuellement et appliquer la checklist par couche.
Pendant les tests : les tests unitaires du domaine utilisent des fakes en mémoire — jamais de @SpringBootTest, jamais de Testcontainers dans les tests domaine. Si un test domaine a besoin de démarrer Spring, c'est une violation.
À chaque pull request : la pipeline GitLab exécute HexagonalArchitectureTest et bloque le merge en cas de violation.
À la fin du projet : générer la DSM dans IntelliJ, vérifier qu'elle est triangulaire, et exécuter le test d'isolation du domaine complet.
Poser les fondations : multitenancy filter, JWT security filter, structure de packages, premier endpoint health qui retourne le tenant résolu depuis le token
je veux travailler dessus, donne une liste exhaustive des fonctionnalité.
26 mai
Liste exhaustive des fonctionnalités — Fondations

F1 — Structure des packages
Créer l'arborescence complète des packages vide avec les fichiers placeholder. Chaque package doit exister physiquement avant qu'on commence à coder pour éviter les imports circulaires et forcer la discipline architecturale dès le départ.
com.yowyob.loyalty/
├── domain/
│   ├── shared/
│   │   ├── model/
│   │   └── port/
│   ├── wallet/
│   │   ├── model/
│   │   ├── port/in/
│   │   ├── port/out/
│   │   ├── service/
│   │   └── event/
│   ├── loyalty/
│   │   ├── model/
│   │   ├── port/in/
│   │   ├── port/out/
│   │   ├── service/
│   │   └── event/
│   ├── promo/
│   │   ├── model/
│   │   ├── port/in/
│   │   ├── port/out/
│   │   └── service/
│   ├── referral/
│   │   ├── model/
│   │   ├── port/in/
│   │   └── port/out/
│   └── member/
│       ├── model/
│       ├── port/in/
│       └── port/out/
│
├── application/
│   ├── wallet/
│   │   ├── command/
│   │   ├── query/
│   │   └── handler/
│   ├── loyalty/
│   ├── promo/
│   └── referral/
│
├── infrastructure/
│   ├── persistence/
│   │   ├── config/
│   │   ├── wallet/
│   │   │   ├── entity/
│   │   │   ├── repository/
│   │   │   ├── adapter/
│   │   │   └── mapper/
│   │   └── shared/
│   ├── redis/
│   │   ├── config/
│   │   └── adapter/
│   ├── kafka/
│   │   ├── config/
│   │   ├── producer/
│   │   └── consumer/
│   ├── payment/
│   │   ├── mtn/
│   │   ├── orange/
│   │   └── stripe/
│   ├── notification/
│   └── security/
│       └── adapter/
│
├── api/
│   ├── wallet/
│   │   └── dto/
│   ├── admin/
│   │   └── dto/
│   ├── health/
│   └── webhook/
│
└── shared/
    ├── multitenancy/
    ├── security/
    ├── exception/
    ├── config/
    └── util/

F2 — Modèles partagés du domaine
Ces classes sont utilisées par tous les modules. Elles doivent être créées en premier car tout en dépend.
TenantId — Value Object immuable qui encapsule un UUID représentant un tenant. Il surcharge equals() et hashCode() pour permettre une comparaison par valeur. Il expose une méthode statique of(String) qui valide que la chaîne est un UUID valide avant de créer l'objet. Il expose une méthode value() qui retourne le UUID sous-jacent.
UserId — même structure que TenantId mais pour les utilisateurs.
Money — Value Object immuable qui encapsule un montant BigDecimal et un code de devise String. Il expose des méthodes add(), subtract(), isGreaterThan(), isLessThan(), isZero() qui retournent des nouveaux objets Money en respectant l'immuabilité. Il valide que le montant n'est jamais négatif à la construction. Il valide que le code devise n'est pas null ou vide.
AuditInfo — Value Object contenant createdAt, updatedAt, createdBy, updatedBy. Embarqué dans tous les agrégats pour la traçabilité.
DomainEvent — interface marqueur que tous les événements du domaine implémentent. Elle déclare eventId(), occurredAt(), tenantId(), eventType().
PageRequest — Value Object pour la pagination : page, size, sortBy, sortDirection. Validations : page >= 0, size entre 1 et 100.
PageResult — Value Object générique contenant la liste des éléments, le total, la page courante et le nombre de pages.

F3 — Multi-tenancy : résolution du tenant
TenantContext — classe thread-local réactive. Elle stocke toutes les informations du tenant courant pour la durée d'une requête. Contient tenantId, tenantName, tenantStatus, tenantPlan. Est immuable une fois construite.
TenantContextHolder — composant qui expose des méthodes statiques réactives pour lire et écrire le TenantContext dans le Context Reactor. Sa méthode getTenantId() retourne un Mono contenant le TenantId extrait du contexte Reactor courant. Si le contexte ne contient pas de tenant, il retourne Mono.error() avec une TenantContextMissingException. Sa méthode withTenant() prend un TenantContext et retourne une fonction qui l'injecte dans le Context Reactor.
TenantRepository — port de sortie du domaine. Interface qui déclare findById(TenantId) retournant Mono, findBySlug(String) retournant Mono, existsById(TenantId) retournant Mono.
Tenant — modèle domaine représentant un tenant. Contient id, name, slug, status (ACTIVE, SUSPENDED, TRIAL), plan (FREE, PRO, ENTERPRISE), createdAt, config.
TenantStatus — enum avec ACTIVE, SUSPENDED, TRIAL, PENDING_SETUP.
TenantPlan — enum avec FREE, PRO, ENTERPRISE.

F4 — Multi-tenancy : filtre WebFlux
TenantResolutionFilter — filtre WebFlux d'ordre très élevé (ORDER = -200) qui s'exécute avant tous les autres filtres. Il extrait le tenantId depuis les claims du JWT déjà validé. Il charge le Tenant correspondant depuis le TenantRepository via le cache Redis. Il vérifie que le tenant est ACTIVE — si SUSPENDED, il retourne 403 avec un corps Problem Details explicite. Il construit un TenantContext et l'injecte dans le Context Reactor de la requête via la méthode chain.filter(exchange).contextWrite(). À partir de ce moment, n'importe quel composant en aval peut appeler TenantContextHolder.getTenantId() sans passer le tenant en paramètre.
TenantCacheAdapter — adapter Redis qui cache les Tenant pour éviter une lecture base de données à chaque requête. Clé Redis : tenant:{tenantId}. TTL : 5 minutes. Invalide automatiquement le cache quand un tenant est modifié via un event Kafka.
TenantRepositoryAdapter — adapter de persistance qui implémente TenantRepository. Lit depuis la table tenants en base via R2DBC. Le schéma de la table tenants est dans le schéma public — partagé entre tous les tenants — contrairement aux autres tables qui sont dans des schémas per-tenant.

F5 — Sécurité : validation JWT
JwtProperties — classe de configuration qui lit les propriétés JWT depuis application.yml. Contient issuerUri, jwkSetUri, audience, et les noms des claims personnalisés (tenantIdClaim, userIdClaim, rolesClaim).
JwtTokenValidator — composant du domaine de sécurité qui valide un token JWT. Il vérifie la signature via le JWK Set de YowAuth0, la date d'expiration, l'issuer, l'audience. Il retourne soit un JwtValidationResult.valid() contenant les claims parsés, soit un JwtValidationResult.invalid() contenant le motif d'invalidité.
JwtClaimsExtractor — composant qui extrait les informations métier depuis les claims d'un JWT validé. Sa méthode extractTenantId() lit le claim configuré dans JwtProperties et le convertit en TenantId. Sa méthode extractUserId() fait de même pour l'userId. Sa méthode extractRoles() lit le claim des rôles et retourne un Set de Role enum. Sa méthode extractScopes() lit les scopes OAuth2.
JwtAuthenticationFilter — filtre WebFlux d'ordre -100 qui s'exécute après le TenantResolutionFilter. Il lit l'en-tête Authorization. Si absent sur une route protégée, il retourne 401. Il extrait le Bearer token. Il appelle JwtTokenValidator. Si invalide, il retourne 401 avec le motif. Si valide, il construit un Authentication Spring Security et le stocke dans le SecurityContext réactif.
SecurityContextRepository — implémentation réactive de ServerSecurityContextRepository pour WebFlux. Elle lit l'Authentication depuis le Context Reactor plutôt que depuis un ThreadLocal — c'est l'adaptation nécessaire pour le monde réactif.

F6 — Sécurité : configuration Spring Security
SecurityConfig — classe annotée @EnableWebFluxSecurity. Elle configure la SecurityWebFilterChain. Elle déclare les routes publiques : /actuator/health, /api-docs/, /swagger-ui/, /api/v1/webhooks/**. Elle déclare que toutes les autres routes nécessitent une authentification. Elle configure le Resource Server OAuth2 pour les JWT. Elle désactive CSRF — inutile pour une API stateless. Elle configure CORS pour autoriser les origines du dashboard Next.js et des apps React Native. Elle configure les headers de sécurité HTTP — X-Content-Type-Options, X-Frame-Options, Strict-Transport-Security.
CorsConfig — configuration CORS séparée pour la lisibilité. Elle lit les origines autorisées depuis application.yml pour permettre des configurations différentes entre dev, staging et production. Elle autorise les méthodes GET, POST, PUT, PATCH, DELETE, OPTIONS. Elle autorise les headers Authorization, Content-Type, Idempotency-Key, X-Tenant-ID.
RoleHierarchy — définit la hiérarchie des rôles. ROLE_SUPER_ADMIN inclut tous les droits de ROLE_TENANT_ADMIN qui inclut tous les droits de ROLE_MEMBER. Utilisé par Spring Security pour les vérifications d'autorisation.
MethodSecurityConfig — active @PreAuthorize et @PostAuthorize sur les méthodes des handlers. Permet d'écrire @PreAuthorize("hasRole('TENANT_ADMIN')") sur les handlers admin.

F7 — Sécurité : autorisation par tenant
TenantSecurityFilter — filtre qui vérifie la cohérence entre le tenantId dans le JWT et les ressources accédées. Il s'exécute après JwtAuthenticationFilter. Il compare le tenantId extrait du token avec le tenantId de la ressource dans le path de la requête si présent (/api/v1/admin/{tenantId}/...). En cas de discordance, il retourne 403. Ce filtre empêche un admin d'un tenant d'accéder aux données d'un autre tenant même avec un JWT valide.
MemberOwnershipValidator — composant appelé dans les handlers pour vérifier qu'un membre ne peut accéder qu'à ses propres ressources. Sa méthode validate() prend le userId du token et le memberId de la ressource et retourne Mono.error(ForbiddenException) si ils ne correspondent pas — sauf si l'utilisateur est TENANT_ADMIN.

F8 — Configuration R2DBC multi-tenant
R2dbcConfig — configuration qui définit le ConnectionFactory principal. Il utilise un pool de connexions R2DBC avec les paramètres configurés dans application.yml.
TenantAwareConnectionFactory — wrapper autour du ConnectionFactory principal. Chaque fois qu'une connexion est demandée, il lit le tenantId depuis le Context Reactor, construit le nom du schéma PostgreSQL correspondant (tenant_{tenantId}), et exécute SET search_path TO tenant_{tenantId}, public sur la connexion avant de la retourner. Toutes les requêtes suivantes sur cette connexion s'exécutent automatiquement dans le bon schéma.
DatabaseInitializer — composant qui s'exécute au démarrage de l'application. Il vérifie que le schéma public contient la table tenants. Il charge la liste de tous les tenants actifs. Pour chaque tenant, il vérifie que son schéma PostgreSQL existe — si non, il le crée. Il déclenche ensuite Liquibase pour appliquer les migrations manquantes sur chaque schéma.

F9 — Migrations Liquibase
db.changelog-master.xml — fichier master qui inclut tous les changelogs dans l'ordre. Il référence d'abord le changelog du schéma public (table tenants, table api_keys), puis les changelogs des schémas per-tenant.
V001__create_public_schema.sql — crée la table tenants avec toutes ses colonnes, la table api_keys avec la contrainte de clé étrangère vers tenants, et les index correspondants.
V002__create_tenant_schema_template.sql — définit le template du schéma per-tenant. Contient les CREATE TABLE pour toutes les tables qui seront créées dans chaque schéma tenant : wallets, wallet_transactions, payment_requests, wallet_audit_logs, loyalty_accounts, points_transactions, rules, triggers, conditions, effects, counters, rewards, reward_grants, referral_programs, referral_links, referral_events, promo_campaigns, promo_usages, members, member_tiers, notifications, audit_logs. Ce fichier est appliqué une fois par tenant à son onboarding.
V003__create_indexes.sql — crée tous les index nécessaires pour les requêtes fréquentes : index sur wallet.member_id, index sur wallet_transactions.wallet_id et created_at, index sur rules.tenant_id et status, index composites pour les requêtes de filtre combinées.

F10 — Configuration Redis
RedisConfig — configuration du ReactiveRedisConnectionFactory et du ReactiveRedisTemplate typé en String, Object. Configure la sérialisation Jackson pour les valeurs et la sérialisation String pour les clés. Configure les timeouts de connexion et de commande.
RedisKeyBuilder — utilitaire qui construit les clés Redis de manière cohérente dans tout le système. Sa méthode tenantKey(tenantId, domain, id) retourne loyalty:{tenantId}:{domain}:{id}. Sa méthode idempotencyKey(idempotencyKey) retourne idempotency:{idempotencyKey}. Centraliser la construction des clés évite les incohérences et les collisions.

F11 — Configuration Kafka
KafkaConfig — configuration des producers et consumers Kafka. Définit la sérialisation JSON des messages via Jackson. Configure le producer avec idempotence activée (enable.idempotence=true), acks=all et retries=3 pour garantir qu'aucun message n'est perdu. Configure les consumer groups et les stratégies de gestion des erreurs.
KafkaTopicConfig — définit programmatiquement les topics Kafka que l'application crée au démarrage si ils n'existent pas. Les topics système — loyalty.dlq (Dead Letter Queue) pour les messages non traçables — sont créés avec une politique de rétention longue.
DeadLetterQueueHandler — consommateur du topic loyalty.dlq. Quand un message ne peut pas être traité après plusieurs retries, il est envoyé ici. Ce handler le persiste dans une table dead_letter_messages en base et alerte l'équipe via notification.

F12 — Gestion globale des erreurs
AppException — classe abstraite racine de toutes les exceptions métier. Elle contient un ErrorCode enum, un message, et des details optionnels. Elle est immuable.
ErrorCode — enum exhaustif de tous les codes d'erreur de l'application. Chaque code est associé à un code HTTP par défaut. Exemples : TENANT_NOT_FOUND (404), TENANT_SUSPENDED (403), JWT_INVALID (401), JWT_EXPIRED (401), INSUFFICIENT_BALANCE (422), WALLET_FROZEN (422), IDEMPOTENCY_CONFLICT (409), RULE_VALIDATION_FAILED (400), REWARD_OUT_OF_STOCK (422).
GlobalExceptionHandler — ControllerAdvice WebFlux annoté @RestControllerAdvice. Il intercepte AppException et retourne un Problem Details RFC 7807 avec le bon code HTTP. Il intercepte WebExchangeBindException (erreurs de validation Bean Validation) et retourne 400 avec la liste des erreurs de champ. Il intercepte DataAccessException (erreurs R2DBC) et retourne 500 en loguant l'exception complète. Il intercepte toutes les autres exceptions non gérées et retourne 500 avec un message générique sans exposer les détails techniques.
ProblemDetails — DTO de réponse d'erreur conforme RFC 7807. Contient type, title, status, detail, instance, timestamp, et un champ errors optionnel pour les erreurs de validation multiples.

F13 — Endpoint Health enrichi
HealthController — RouterFunction WebFlux exposant GET /api/v1/health. Cet endpoint est public — pas d'authentification requise. Il retourne un JSON avec status: UP, version de l'application, timestamp.
SecureHealthController — RouterFunction WebFlux exposant GET /api/v1/health/tenant. Cet endpoint est protégé — JWT requis. Il retourne un JSON enrichi contenant le status général, le tenantId résolu depuis le token, le tenantName, le tenantStatus, le tenantPlan, le userId de l'appelant, ses rôles, le timestamp de la requête et la version de l'API. C'est l'endpoint de vérification de bout en bout — si il retourne le bon tenant, toute la chaîne fonctionne : JWT validé, tenant résolu, contexte propagé.
ApplicationInfoContributor — InfoContributor Spring Actuator qui ajoute les informations de build — version, git commit, timestamp de build — à l'endpoint /actuator/info.
TenantHealthIndicator — HealthIndicator Spring Actuator qui vérifie que la connexion base de données fonctionne pour le tenant courant. Exposé sur /actuator/health/tenant-db.

F14 — Logging et tracing
RequestLoggingFilter — filtre WebFlux qui loge chaque requête entrante et sa réponse. Il génère un requestId unique (UUID) pour chaque requête. Il loge la méthode HTTP, le path, le tenantId, le userId, le code de réponse et la durée en millisecondes. Il injecte le requestId dans les MDC de Logback pour qu'il apparaisse dans toutes les lignes de log de cette requête.
MdcContextLifter — configuration Reactor qui propage automatiquement le MDC (Mapped Diagnostic Context) de Logback à travers les opérateurs réactifs. Sans ça, les logs dans une chaîne Reactor perdent le requestId et le tenantId entre les opérateurs.
LoggingConfig — configuration Logback qui formate les logs en JSON structuré avec les champs : timestamp, level, logger, message, tenantId, userId, requestId, traceId, spanId. Ce format est directement ingérable par Logstash sans configuration supplémentaire.

F15 — Tests d'architecture et d'intégration des fondations
HexagonalArchitectureTest — tests ArchUnit complets décrits précédemment. S'exécute à chaque build.
TenantResolutionFilterTest — test unitaire du filtre de résolution tenant. Vérifie qu'un JWT valide avec un tenant actif retourne 200. Vérifie qu'un JWT avec un tenant suspendu retourne 403. Vérifie qu'un JWT sans claim tenantId retourne 401. Vérifie que le TenantContext est correctement injecté dans le Context Reactor.
JwtAuthenticationFilterTest — test unitaire du filtre JWT. Vérifie qu'une requête sans token retourne 401 sur une route protégée. Vérifie qu'une requête sans token retourne 200 sur /api/v1/health. Vérifie qu'un token expiré retourne 401. Vérifie qu'un token avec mauvaise signature retourne 401.
SecurityIntegrationTest — test d'intégration avec @WebFluxTest. Démarre le contexte Spring WebFlux complet avec les filtres de sécurité. Vérifie le flux complet depuis une requête HTTP jusqu'à la réponse en passant par tous les filtres.
HealthEndpointIntegrationTest — test d'intégration avec Testcontainers. Démarre PostgreSQL et Redis. Vérifie que GET /api/v1/health retourne 200 sans token. Vérifie que GET /api/v1/health/tenant avec un JWT valide retourne le tenantId correct dans le corps de réponse. C'est le test de bout en bout des fondations.
TestContainersConfig — configuration partagée des Testcontainers. Définit les conteneurs PostgreSQL 18 et Redis 7 réutilisés par tous les tests d'intégration. Utilise @Container avec des conteneurs statiques pour éviter de redémarrer un conteneur à chaque test.
TestJwtFactory — utilitaire de test qui génère des JWT signés avec une clé de test pour les tests d'intégration. Expose des méthodes statiques : forTenantAdmin(tenantId), forMember(tenantId, memberId), expired(), withoutTenantClaim(), withSuspendedTenant().

Ordre d'implémentation recommandé
Jour 1 — F1 (structure packages) + F2 (modèles partagés) + F12 (exceptions). Aucune dépendance externe, tout en Java pur, testable immédiatement.
Jour 2 — F3 (TenantContext, TenantContextHolder, interfaces) + F9 (migrations Liquibase V001) + F8 (R2dbcConfig basique sans multi-tenant encore).
Jour 3 — F10 (Redis config) + F4 (TenantResolutionFilter + TenantCacheAdapter + TenantRepositoryAdapter). Premier test : le filtre résout le tenant depuis Redis.
Jour 4 — F5 (JWT validator, extractor) + F6 (SecurityConfig) + F7 (TenantSecurityFilter). Premier test : requête avec JWT valide passe, sans JWT retourne 401.
Jour 5 — F8 complet (TenantAwareConnectionFactory) + F9 complet (toutes les migrations) + F13 (Health endpoints). Premier test de bout en bout : GET /api/v1/health/tenant retourne le bon tenant.
Jour 6 — F11 (Kafka config) + F14 (logging, tracing) + F15 (tous les tests). La suite de tests passe entièrement.
À ce stade les fondations sont complètes et on peut commencer le Module Wallet sur une base solide et vérifiée.
Poser les fondations : multitenancy filter, JWT security filter, structure de packages, premier endpoint health qui retourne le tenant résolu depuis le token
je veux travailler dessus, donne une liste exhaustive des fonctionnalité.
fait comme on avait fait ici, je veux le meme format avec les etapes:
26 mai
Liste exhaustive des fonctionnalités — Fondations

F1 — Structure des packages
Créer l'arborescence complète vide avec les fichiers placeholder. Chaque package existe physiquement avant qu'on commence à coder.
com/yowyob/loyalty/
│
├── domain/
│   ├── shared/
│   │   ├── model/
│   │   │   ├── TenantId.java
│   │   │   ├── UserId.java
│   │   │   ├── Money.java
│   │   │   ├── AuditInfo.java
│   │   │   ├── PageRequest.java
│   │   │   └── PageResult.java
│   │   ├── port/
│   │   │   └── DomainEvent.java
│   │   └── exception/
│   │       └── DomainException.java
│   │
│   ├── tenant/
│   │   ├── model/
│   │   │   ├── Tenant.java
│   │   │   ├── TenantConfig.java
│   │   │   └── enums/
│   │   │       ├── TenantStatus.java
│   │   │       └── TenantPlan.java
│   │   └── port/
│   │       └── out/
│   │           └── TenantRepository.java
│   │
│   ├── wallet/
│   ├── loyalty/
│   ├── promo/
│   ├── referral/
│   └── member/
│
├── application/
│   ├── wallet/
│   ├── loyalty/
│   ├── promo/
│   └── referral/
│
├── infrastructure/
│   ├── persistence/
│   │   ├── config/
│   │   │   ├── R2dbcConfig.java
│   │   │   ├── TenantAwareConnectionFactory.java
│   │   │   └── DatabaseInitializer.java
│   │   ├── tenant/
│   │   │   ├── entity/
│   │   │   │   └── TenantEntity.java
│   │   │   ├── repository/
│   │   │   │   └── TenantR2dbcRepository.java
│   │   │   ├── adapter/
│   │   │   │   └── TenantRepositoryAdapter.java
│   │   │   └── mapper/
│   │   │       └── TenantMapper.java
│   │   └── shared/
│   │
│   ├── redis/
│   │   ├── config/
│   │   │   └── RedisConfig.java
│   │   └── adapter/
│   │       └── TenantCacheAdapter.java
│   │
│   ├── kafka/
│   │   └── config/
│   │       ├── KafkaConfig.java
│   │       └── KafkaTopicConfig.java
│   │
│   └── security/
│       └── adapter/
│           └── JwtValidatorAdapter.java
│
├── api/
│   ├── health/
│   │   ├── HealthController.java
│   │   └── dto/
│   │       └── TenantHealthResponse.java
│   └── shared/
│       └── dto/
│           └── ProblemDetails.java
│
└── shared/
    ├── multitenancy/
    │   ├── TenantContext.java
    │   ├── TenantContextHolder.java
    │   └── TenantResolutionFilter.java
    ├── security/
    │   ├── JwtProperties.java
    │   ├── JwtTokenValidator.java
    │   ├── JwtClaimsExtractor.java
    │   ├── JwtAuthenticationFilter.java
    │   ├── TenantSecurityFilter.java
    │   ├── MemberOwnershipValidator.java
    │   ├── SecurityContextRepository.java
    │   ├── CorsConfig.java
    │   ├── RoleHierarchyConfig.java
    │   └── SecurityConfig.java
    ├── exception/
    │   ├── AppException.java
    │   ├── ErrorCode.java
    │   └── GlobalExceptionHandler.java
    ├── config/
    │   ├── JacksonConfig.java
    │   └── WebFluxConfig.java
    ├── logging/
    │   ├── RequestLoggingFilter.java
    │   ├── MdcContextLifter.java
    │   └── LoggingConfig.java
    └── util/
        └── RedisKeyBuilder.java

resources/
├── application.yml
├── application-dev.yml
├── application-test.yml
├── logback-spring.xml
└── db/changelog/
    ├── db.changelog-master.xml
    └── migrations/
        ├── V001__create_public_tenants_table.sql
        └── V002__create_api_keys_table.sql

test/
└── com/yowyob/loyalty/
    ├── architecture/
    │   └── HexagonalArchitectureTest.java
    ├── shared/
    │   ├── multitenancy/
    │   │   └── TenantResolutionFilterTest.java
    │   └── security/
    │       ├── JwtTokenValidatorTest.java
    │       ├── JwtAuthenticationFilterTest.java
    │       └── TenantSecurityFilterTest.java
    ├── api/health/
    │   └── HealthControllerTest.java
    ├── integration/
    │   ├── HealthEndpointIntegrationTest.java
    │   └── config/
    │       ├── TestContainersConfig.java
    │       └── TestJwtFactory.java
    └── architecture/
        └── HexagonalArchitectureTest.java

F2 — Modèles partagés du domaine
TenantId.java — Value Object immuable wrappant un UUID. Méthode statique of(String) valide que la chaîne est un UUID avant construction. Surcharge equals(), hashCode(), toString(). Zéro annotation Spring, zéro dépendance externe.
UserId.java — même structure que TenantId pour les utilisateurs.
Money.java — Value Object immuable avec amount BigDecimal et currency String. Méthodes add(), subtract(), isGreaterThan(), isLessThan(), isZero() retournent de nouveaux objets Money. Valide que le montant n'est jamais négatif à la construction. Valide que currency n'est pas null.
AuditInfo.java — Value Object avec createdAt, updatedAt, createdBy, updatedBy. Embarqué dans tous les agrégats.
DomainEvent.java — interface marqueur déclarant eventId(), occurredAt(), tenantId(), eventType(). Tous les events du domaine l'implémentent.
PageRequest.java — Value Object pour la pagination : page, size, sortBy, sortDirection. Validations : page >= 0, size entre 1 et 100.
PageResult.java — Value Object générique <T> avec la liste des éléments, total, page, totalPages.
DomainException.java — classe abstraite racine de toutes les exceptions du domaine pur. Contient un message et des details optionnels. Pas d'ErrorCode ici — le domaine ne connaît pas les codes HTTP.

F3 — Modèle Tenant du domaine
TenantStatus.java — enum : ACTIVE, SUSPENDED, TRIAL, PENDING_SETUP.
TenantPlan.java — enum : FREE, PRO, ENTERPRISE.
TenantConfig.java — Value Object immuable contenant toutes les configurations globales du tenant : defaultCurrencyCode, walletAutoActivate (boolean — si true, le wallet créé est directement ACTIVE, pas PENDING_KYC), pointExpiryDays, notificationChannels.
Tenant.java — agrégat du domaine tenant. Contient id (TenantId), name, slug, status (TenantStatus), plan (TenantPlan), config (TenantConfig), auditInfo (AuditInfo). Méthode isActive() retourne status == ACTIVE. Méthode isSuspended() retourne status == SUSPENDED. Zéro annotation Spring, zéro annotation JPA/R2DBC.
TenantRepository.java — port de sortie dans domain/tenant/port/out/. Interface déclarant findById(TenantId) retournant Mono<Tenant>, findBySlug(String) retournant Mono<Tenant>, existsById(TenantId) retournant Mono<Boolean>. Le domaine déclare ce dont il a besoin sans savoir ce qui est derrière.

F4 — Multi-tenancy : TenantContext et TenantContextHolder
TenantContext.java — classe immuable (record Java 21) qui encapsule tenantId (TenantId), tenantName (String), tenantStatus (TenantStatus), tenantPlan (TenantPlan). Construite une seule fois par requête, jamais modifiée. Méthode isActive() délègue à tenantStatus.
TenantContextHolder.java — composant utilitaire avec méthodes statiques réactives. Constante TENANT_CONTEXT_KEY de type Class<TenantContext> utilisée comme clé dans le Context Reactor. Méthode statique getTenantContext() retourne Mono<TenantContext> en lisant depuis Mono.deferContextual(). Méthode statique getTenantId() retourne Mono<TenantId> extrait du TenantContext. Méthode statique withTenantContext(TenantContext) retourne un Function<Context, Context> qui injecte le TenantContext dans le Context Reactor. Retourne Mono.error(TenantContextMissingException) si le contexte est absent.
TenantContextMissingException.java — exception runtime lancée quand on essaie de lire le tenant depuis un thread qui ne vient pas d'une requête HTTP (ex : job batch mal configuré).

F5 — Multi-tenancy : TenantResolutionFilter
TenantResolutionFilter.java — filtre WebFlux implémentant WebFilter. Ordre -200 — s'exécute avant tous les autres filtres. S'applique à toutes les routes sauf les routes publiques définies dans une liste blanche (/actuator/health, /api/v1/health).
Séquence d'exécution dans la méthode filter() :
Extraire le token JWT du header Authorization. Si absent sur une route protégée — retourner directement 401 sans aller plus loin.
Extraire le tenantId depuis les claims JWT via JwtClaimsExtractor. Si claim absent — retourner 401 avec motif MISSING_TENANT_CLAIM.
Appeler TenantCacheAdapter.findById(tenantId). Si absent du cache, appeler TenantRepositoryAdapter.findById(tenantId). Si introuvable en base — retourner 401 avec motif TENANT_NOT_FOUND. Mettre en cache le tenant trouvé pour les prochaines requêtes.
Vérifier tenant.isActive(). Si SUSPENDED — retourner 403 avec motif TENANT_SUSPENDED et un body Problem Details. Si PENDING_SETUP — retourner 403 avec motif TENANT_NOT_READY.
Construire le TenantContext depuis le Tenant chargé.
Appeler chain.filter(exchange).contextWrite(TenantContextHolder.withTenantContext(tenantContext)) pour propager le contexte dans toute la chaîne réactive en aval.

F6 — Multi-tenancy : TenantCacheAdapter
TenantCacheAdapter.java — composant Spring dans infrastructure/redis/adapter/. Utilise ReactiveRedisTemplate<String, String> pour persister le tenant sérialisé en JSON. Méthode findById(TenantId) construit la clé via RedisKeyBuilder.tenantKey(tenantId) — format loyalty:tenant:{tenantId}, TTL 5 minutes. Désérialise le JSON en objet Tenant via Jackson. Méthode evict(TenantId) invalide le cache quand un tenant est modifié.
TenantRepositoryAdapter.java — implémente TenantRepository du domaine. Délègue à TenantR2dbcRepository. Utilise TenantMapper pour convertir TenantEntity en Tenant domaine. Annotée @Component.
TenantEntity.java — entité R2DBC annotée @Table("tenants"). Contient tous les champs de la table avec les annotations @Id, @Column. Distinct du modèle domaine Tenant.
TenantR2dbcRepository.java — interface étendant ReactiveCrudRepository<TenantEntity, UUID>. Déclare findBySlug(String slug) retournant Mono<TenantEntity>.
TenantMapper.java — mapper MapStruct entre TenantEntity et Tenant domaine et vice-versa. Annoté @Mapper(componentModel = "spring").

F7 — Configuration R2DBC multi-tenant
R2dbcConfig.java — annoté @Configuration. Définit le ConnectionPool principal avec les propriétés lues depuis application.yml : url, username, password, pool initial-size, max-size, max-idle-time. Déclare le bean TenantAwareConnectionFactory en wrappant le ConnectionPool.
TenantAwareConnectionFactory.java — implémente ConnectionFactory. Override la méthode create() qui retourne un Publisher<? extends Connection>. À chaque demande de connexion : lire le TenantId depuis le Context Reactor via TenantContextHolder.getTenantId(). Si tenant trouvé — construire le nom de schéma tenant_ + tenantId. Exécuter SET search_path TO tenant_{tenantId}, public sur la connexion. Retourner la connexion configurée. Si pas de tenant dans le contexte (appel système, migration) — utiliser le schéma public par défaut.
DatabaseInitializer.java — implémente ApplicationRunner. S'exécute au démarrage de l'application. Vérifie que le schéma public et la table tenants existent. Charge tous les tenants actifs depuis la base. Pour chaque tenant, vérifie que le schéma tenant_{tenantId} existe en PostgreSQL — si non, le crée avec CREATE SCHEMA IF NOT EXISTS. Déclenche Liquibase pour appliquer les migrations manquantes sur chaque schéma.

F8 — Configuration Redis
RedisConfig.java — annoté @Configuration. Déclare le bean ReactiveRedisConnectionFactory depuis les propriétés spring.data.redis. Déclare le bean ReactiveRedisTemplate<String, String> avec sérialisation StringRedisSerializer pour les clés et les valeurs. Déclare un second bean ReactiveRedisTemplate<String, Object> avec Jackson2JsonRedisSerializer pour les valeurs complexes.
RedisKeyBuilder.java — classe utilitaire statique dans shared/util/. Méthode tenantKey(TenantId tenantId) retourne "loyalty:tenant:" + tenantId.value(). Méthode idempotencyKey(String key) retourne "idempotency:" + key. Méthode walletKey(TenantId tenantId, UUID walletId) retourne "loyalty:" + tenantId + ":wallet:" + walletId. Méthode rateLimitKey(TenantId tenantId, String operation) retourne "loyalty:" + tenantId + ":ratelimit:" + operation. Centraliser ici évite les fautes de frappe et les collisions entre modules.

F9 — Sécurité : JWT Validator
JwtProperties.java — annoté @ConfigurationProperties(prefix = "app.security.jwt"). Contient issuerUri (String), jwkSetUri (String), audience (String), tenantIdClaim (String — défaut "tenant_id"), userIdClaim (String — défaut "sub"), rolesClaim (String — défaut "roles"), scopesClaim (String — défaut "scope").
JwtTokenValidator.java — composant dans shared/security/. Injecte JwtProperties. Au démarrage, charge le JWKSet depuis jwkSetUri via une requête HTTP et le met en cache — recharge toutes les heures. Méthode validate(String token) retourne Mono<JwtValidationResult>. Vérifie la signature via le JWK Set, la date d'expiration, l'issuer, l'audience. En cas de succès retourne JwtValidationResult.valid(claims). En cas d'échec retourne JwtValidationResult.invalid(motif).
JwtValidationResult.java — sealed interface avec deux records : Valid(Map<String, Object> claims) et Invalid(String reason).
JwtClaimsExtractor.java — composant dans shared/security/. Injecte JwtProperties. Méthode extractTenantId(Map<String, Object> claims) lit le claim configuré et retourne Optional<TenantId>. Méthode extractUserId(Map<String, Object> claims) retourne Optional<UserId>. Méthode extractRoles(Map<String, Object> claims) retourne Set<String>. Méthode extractScopes(Map<String, Object> claims) retourne Set<String>. Gère les cas où les claims sont absents sans lancer d'exception.

F10 — Sécurité : filtres WebFlux
JwtAuthenticationFilter.java — filtre WebFlux ordre -100. S'exécute après TenantResolutionFilter. Lit le header Authorization. Si absent sur route protégée — retourne 401. Extrait le Bearer token. Appelle JwtTokenValidator.validate(token). Si Invalid — retourne 401 avec le motif en Problem Details. Si Valid — construit un objet Authentication Spring Security depuis les claims. Stocke l'Authentication dans le SecurityContext réactif via SecurityContextRepository. Appelle chain.filter(exchange) pour continuer.
SecurityContextRepository.java — implémente ServerSecurityContextRepository. Méthode save() stocke le SecurityContext dans le Context Reactor. Méthode load() lit le SecurityContext depuis le Context Reactor. Cette implémentation est nécessaire car WebFlux ne peut pas utiliser le ThreadLocal de Spring Security standard.
TenantSecurityFilter.java — filtre WebFlux ordre -50. S'exécute après JwtAuthenticationFilter. Pour les routes qui contiennent un {tenantId} dans le path — extrait le tenantId du path variable et compare avec le tenantId du TenantContext. Si discordance — retourne 403 avec motif CROSS_TENANT_ACCESS_DENIED. Les super-admins Yowyob (rôle ROLE_SUPER_ADMIN) sont exemptés de cette vérification.
MemberOwnershipValidator.java — composant appelé manuellement dans les handlers. Méthode validate(UserId tokenUserId, UUID resourceMemberId) retourne Mono<Void> — soit vide si autorisé, soit Mono.error(ForbiddenException) si l'utilisateur essaie d'accéder à la ressource d'un autre membre. Les admins sont exemptés.

F11 — Sécurité : SecurityConfig
SecurityConfig.java — annoté @Configuration @EnableWebFluxSecurity. Déclare le bean SecurityWebFilterChain. Configure les routes publiques : /actuator/health, /api/v1/health, /api-docs/**, /swagger-ui/**, /api/v1/webhooks/**. Toutes les autres routes nécessitent authentication. Désactive CSRF. Configure le Resource Server OAuth2 pour déléguer la validation JWT initiale à Spring Security (en complément de notre JwtTokenValidator personnalisé). Injecte les filtres dans la chaîne dans le bon ordre.
CorsConfig.java — annoté @Configuration. Déclare un bean CorsWebFilter. Lit les origines autorisées depuis application.yml (app.cors.allowed-origins). Autorise les méthodes GET, POST, PUT, PATCH, DELETE, OPTIONS. Autorise les headers Authorization, Content-Type, Idempotency-Key. Expose le header X-Request-Id dans les réponses pour que le frontend puisse logger le requestId.
RoleHierarchyConfig.java — déclare le bean RoleHierarchy. Configure la hiérarchie : ROLE_SUPER_ADMIN > ROLE_TENANT_ADMIN > ROLE_MEMBER. Cela signifie qu'un ROLE_TENANT_ADMIN a automatiquement tous les droits d'un ROLE_MEMBER sans annotation supplémentaire.

F12 — Gestion des erreurs
ErrorCode.java — enum exhaustif. Chaque constante porte le code HTTP associé via un constructeur. Exemples : TENANT_NOT_FOUND(404), TENANT_SUSPENDED(403), TENANT_NOT_READY(403), JWT_INVALID(401), JWT_EXPIRED(401), JWT_MISSING(401), MISSING_TENANT_CLAIM(401), CROSS_TENANT_ACCESS_DENIED(403), FORBIDDEN(403), TENANT_CONTEXT_MISSING(500), INTERNAL_ERROR(500).
AppException.java — classe abstraite. Contient ErrorCode errorCode, String detail, Map<String, Object> properties optionnel. Méthode getHttpStatus() délègue à errorCode.getHttpStatus(). Toutes les exceptions métier héritent de cette classe.
TenantNotFoundException.java, TenantSuspendedException.java, JwtInvalidException.java, JwtExpiredException.java, CrossTenantAccessException.java, ForbiddenException.java — exceptions concrètes héritant de AppException. Chacune avec un constructeur qui passe le bon ErrorCode.
ProblemDetails.java — record Java 21 dans api/shared/dto/. Contient type (String — URL identifiant l'erreur), title (String), status (int), detail (String), instance (String — requestId), timestamp (Instant), errors (Map optionnel pour les erreurs de validation). Sérialisé automatiquement en JSON par Jackson.
GlobalExceptionHandler.java — annoté @RestControllerAdvice. Méthode handleAppException(AppException) construit un ProblemDetails depuis l'exception et retourne avec le bon status HTTP. Méthode handleWebExchangeBindException(WebExchangeBindException) retourne 400 avec la liste des erreurs de champ dans errors. Méthode handleDataAccessException(DataAccessException) logue l'exception complète et retourne 500 avec un message générique sans détail technique. Méthode handleGenericException(Exception) capture tout le reste et retourne 500.

F13 — Logging et tracing
RequestLoggingFilter.java — filtre WebFlux ordre 0 (s'exécute après les filtres de sécurité). Génère un requestId UUID unique. Logue la requête entrante avec méthode, path, tenantId, userId. À la fin de la requête, logue le code de réponse et la durée en millisecondes. Injecte requestId et tenantId dans le MDC Logback.
MdcContextLifter.java — configuration Reactor dans shared/logging/. Utilise Hooks.onEachOperator() pour propager le MDC à travers tous les opérateurs réactifs. Sans ça, le tenantId et le requestId disparaissent des logs entre les opérateurs flatMap, switchMap etc. S'initialise via un bean @PostConstruct.
LoggingConfig.java — configuration programmatique Logback. Désactive le format texte par défaut. Active le format JSON structuré avec les champs : timestamp, level, logger, message, tenantId, userId, requestId, traceId, spanId, thread. Compatible direct avec Logstash.
logback-spring.xml — configure l'appender JSON dans resources/. En profil dev, utilise aussi un appender console human-readable en parallèle. En profil prod, uniquement JSON vers stdout pour être capturé par Docker/Kubernetes.

F14 — Configuration Kafka
KafkaConfig.java — annoté @Configuration. Déclare le ReactiveKafkaProducerTemplate avec sérialisation JSON Jackson. Configure le producer avec enable.idempotence=true, acks=all, retries=3, max.in.flight.requests.per.connection=1 — garantit l'ordre et l'exactly-once delivery. Déclare le ReactiveKafkaConsumerTemplate avec désérialisation JSON.
KafkaTopicConfig.java — déclare les beans NewTopic pour les topics créés automatiquement au démarrage. Crée loyalty.dlq (Dead Letter Queue) avec rétention de 7 jours et 3 partitions. Crée tenant.events pour les changements de configuration tenant.

F15 — Migrations Liquibase — schéma public
db.changelog-master.xml — inclut les changelogs dans l'ordre. Référence d'abord migrations/V001__create_public_tenants_table.sql puis migrations/V002__create_api_keys_table.sql.
V001__create_public_tenants_table.sql — crée la table tenants dans le schéma public :
sql
CREATE TABLE IF NOT EXISTS public.tenants (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name            VARCHAR(255) NOT NULL,
    slug            VARCHAR(100) NOT NULL UNIQUE,
    status          VARCHAR(50)  NOT NULL DEFAULT 'PENDING_SETUP',
    plan            VARCHAR(50)  NOT NULL DEFAULT 'FREE',
    config          JSONB        NOT NULL DEFAULT '{}',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255)
);

CREATE INDEX idx_tenants_slug   ON public.tenants(slug);
CREATE INDEX idx_tenants_status ON public.tenants(status);
V002__create_api_keys_table.sql — crée la table api_keys dans le schéma public :
sql
CREATE TABLE IF NOT EXISTS public.api_keys (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID         NOT NULL REFERENCES public.tenants(id),
    key_hash        VARCHAR(255) NOT NULL UNIQUE,
    scopes          TEXT[]       NOT NULL DEFAULT '{}',
    revoked_at      TIMESTAMPTZ,
    last_used_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(255)
);

CREATE INDEX idx_api_keys_tenant_id ON public.api_keys(tenant_id);
CREATE INDEX idx_api_keys_key_hash  ON public.api_keys(key_hash);

F16 — Endpoint Health
HealthController.java — RouterFunction WebFlux dans api/health/. Route publique GET /api/v1/health retourne 200 avec { "status": "UP", "version": "1.0.0", "timestamp": "..." }. Aucun JWT requis. Utilisé par les load balancers et les health checks Docker.
TenantHealthResponse.java — record Java 21 dans api/health/dto/. Contient status, tenantId, tenantName, tenantStatus, tenantPlan, userId, roles, apiVersion, timestamp. Sérialisé en JSON.
SecureHealthController.java — RouterFunction WebFlux. Route protégée GET /api/v1/health/tenant nécessite un JWT valide. Lit le TenantContext via TenantContextHolder.getTenantContext(). Lit l'Authentication depuis le SecurityContext réactif. Construit et retourne un TenantHealthResponse. C'est l'endpoint de vérification bout en bout — s'il répond avec les bonnes données, toute la chaîne fonctionne : JWT validé, tenant résolu, contexte propagé, base de données accessible.

F17 — Configuration applicative
application.yml — configuration principale :
yaml
spring:
  application:
    name: loyalty-service
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/loyalty_db
    username: ${DB_USER:loyalty_user}
    password: ${DB_PASS:loyalty_pass}
    pool:
      initial-size: 5
      max-size: 20
      max-idle-time: 30m
  liquibase:
    url: jdbc:postgresql://localhost:5432/loyalty_db
    user: ${DB_USER:loyalty_user}
    password: ${DB_PASS:loyalty_pass}
    change-log: classpath:db/changelog/db.changelog-master.xml
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
  kafka:
    bootstrap-servers: ${KAFKA_SERVERS:localhost:9092}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: loyalty-service
      auto-offset-reset: earliest
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: ${JWT_ISSUER_URI:http://localhost:8180/realms/yowyob}

app:
  security:
    jwt:
      issuer-uri: ${JWT_ISSUER_URI:http://localhost:8180/realms/yowyob}
      jwk-set-uri: ${JWK_SET_URI:http://localhost:8180/realms/yowyob/protocol/openid-connect/certs}
      audience: loyalty-service
      tenant-id-claim: tenant_id
      user-id-claim: sub
      roles-claim: roles
  cors:
    allowed-origins:
      - http://localhost:3000
      - https://dashboard.yowyob.com

management:
  endpoints:
    web:
      exposure:
        include: health,info,prometheus,metrics
  metrics:
    export:
      prometheus:
        enabled: true

springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html

server:
  port: 8080
application-dev.yml — surcharge pour le développement local. Active les logs SQL R2DBC. Désactive la vérification SSL. Réduit les TTL Redis pour faciliter les tests.
application-test.yml — surcharge pour les tests. Désactive Kafka auto-start. Configure Testcontainers comme source de données.

F18 — Tests
HexagonalArchitectureTest.java — tests ArchUnit complets. Vérifie que domain n'importe rien de Spring, R2DBC, Kafka, Redis. Vérifie que les ports sont des interfaces. Vérifie que les adapters implémentent les ports. Vérifie l'absence d'annotations JPA/R2DBC dans les modèles domaine. S'exécute à chaque build, bloque le merge si violation.
TenantResolutionFilterTest.java — tests unitaires du filtre. Mock de TenantCacheAdapter et JwtClaimsExtractor. Vérifie JWT valide + tenant actif → 200 + TenantContext injecté. Vérifie tenant suspendu → 403. Vérifie claim tenantId absent → 401. Vérifie tenant introuvable → 401.
JwtTokenValidatorTest.java — tests unitaires du validateur. Token valide → Valid. Token expiré → Invalid(JWT_EXPIRED). Mauvaise signature → Invalid(JWT_INVALID). Mauvais issuer → Invalid. Mauvaise audience → Invalid.
JwtAuthenticationFilterTest.java — tests unitaires du filtre. Requête sans header Authorization sur route protégée → 401. Requête sans token sur route publique → 200. Token valide → Authentication stockée dans SecurityContext. Token invalide → 401.
TenantSecurityFilterTest.java — vérifie qu'un admin du tenant A ne peut pas accéder aux ressources du tenant B. Vérifie qu'un super-admin peut accéder à n'importe quel tenant.
HealthControllerTest.java — test unitaire du contrôleur. GET /api/v1/health sans token → 200 avec status UP. GET /api/v1/health/tenant sans token → 401. GET /api/v1/health/tenant avec token valide → 200 avec tenantId correct dans le corps.
TestContainersConfig.java — configuration partagée de tous les tests d'intégration. Déclare les conteneurs statiques PostgreSQL 18 et Redis 7 réutilisés par tous les tests. Surcharge les propriétés spring.r2dbc.url et spring.data.redis.host avec les ports dynamiques alloués par Testcontainers.
TestJwtFactory.java — utilitaire qui génère des JWT signés avec une clé RSA de test. Méthodes : forTenantAdmin(String tenantId), forMember(String tenantId, String userId), expired(), withoutTenantClaim(), withSuspendedTenant(). Utilisé par tous les tests d'intégration qui ont besoin d'un JWT.
HealthEndpointIntegrationTest.java — test de bout en bout. Démarre PostgreSQL et Redis via Testcontainers. Insère un tenant de test en base. Génère un JWT de test avec TestJwtFactory. Appelle GET /api/v1/health/tenant. Vérifie que la réponse contient le tenantId correct. Ce test valide que toute la chaîne fonctionne : filtre JWT → filtre tenant → base de données → réponse.

Ordre d'implémentation
Étape 1 — Modèles partagés du domaine (F2)
Aucune dépendance. Java pur. TenantId, UserId, Money, AuditInfo, PageRequest, PageResult, DomainEvent, DomainException. Testables immédiatement sans Spring.
Étape 2 — Modèle Tenant du domaine (F3)
TenantStatus, TenantPlan, TenantConfig, Tenant, TenantRepository (interface). Toujours zéro Spring. Le domaine définit son contrat.
Étape 3 — Gestion des erreurs (F12)
ErrorCode, AppException, toutes les exceptions concrètes, ProblemDetails, GlobalExceptionHandler. Fondation nécessaire avant les filtres qui retournent des erreurs.
Étape 4 — Structure packages + application.yml (F1 + F17)
Créer tous les packages vides. Configurer application.yml, application-dev.yml, application-test.yml. Vérifier que l'application démarre sans erreur même avec des beans manquants.
Étape 5 — Configuration R2DBC et migrations (F7 + F15)
R2dbcConfig basique sans multi-tenant. Migrations Liquibase V001 et V002. Vérifier que les tables tenants et api_keys sont créées au démarrage.
Étape 6 — Redis et utilitaires (F8)
RedisConfig, RedisKeyBuilder. Vérifier la connexion Redis au démarrage via Actuator.
Étape 7 — Persistance Tenant (F6)
TenantEntity, TenantR2dbcRepository, TenantMapper, TenantRepositoryAdapter. Premier test d'intégration — insérer un tenant en base et le relire.
Étape 8 — TenantContext et TenantContextHolder (F4)
TenantContext, TenantContextHolder, TenantContextMissingException. Tests unitaires — vérifier que le contexte est bien injecté et lu depuis le Context Reactor.
Étape 9 — Cache tenant + filtre résolution (F5 + F6)
TenantCacheAdapter, TenantResolutionFilter. Tests unitaires du filtre avec mocks. Vérifier les scénarios tenant actif, suspendu, introuvable.
Étape 10 — JWT (F9)
JwtProperties, JwtTokenValidator, JwtClaimsExtractor, JwtValidationResult. Tests unitaires avec tokens valides et invalides générés par TestJwtFactory.
Étape 11 — Filtres sécurité (F10 + F11)
JwtAuthenticationFilter, SecurityContextRepository, TenantSecurityFilter, MemberOwnershipValidator. Tests unitaires de chaque filtre isolément.
Étape 12 — SecurityConfig + CORS (F11)
SecurityConfig, CorsConfig, RoleHierarchyConfig. Tests d'intégration — routes publiques accessibles sans token, routes protégées retournent 401 sans token.
Étape 13 — TenantAwareConnectionFactory (F7)
Activer le routing multi-schéma. DatabaseInitializer. Tests d'intégration — vérifier que les requêtes s'exécutent dans le bon schéma selon le tenantId du contexte.
Étape 14 — Kafka (F14)
KafkaConfig, KafkaTopicConfig. Vérifier que les topics sont créés au démarrage.
Étape 15 — Logging et tracing (F13)
RequestLoggingFilter, MdcContextLifter, LoggingConfig, logback-spring.xml. Vérifier que les logs JSON contiennent tenantId et requestId.
Étape 16 — Endpoints Health (F16)
HealthController, SecureHealthController, TenantHealthResponse. Tests unitaires et test d'intégration bout en bout.
Étape 17 — Tests d'architecture (F18)
HexagonalArchitectureTest en dernier — valide que toutes les règles architecturales sont respectées sur le code qu'on vient d'écrire. Si des violations sont détectées, les corriger avant de passer au Module Wallet.
J'ai ete informer que je dois utiliser une API pour gerer les bonus. voici les info dans la documentations de l'API.
dit moi ce que cela implique pour notre backend? ce que l'on ne va plus developper et tout. analyse et donne moi tes impressions et les directives conseillé pour la suite
26 mai
Analyse de l'API de Bonification externe

Ce que fait cette API
En lisant la documentation, cette API est un système de bonification développé par une équipe du semestre précédent (5GI selon le document du projet). Elle gère :
l'enregistrement des utilisateurs et l'authentification JWT, la gestion des clients (bénéficiaires), la définition de règles de bonification par intervalles de montant, l'enregistrement des transactions et le calcul automatique des points, la gestion des récompenses et leur attribution automatique, l'historique des transactions et des récompenses.
Sa structure de dossiers révèle une architecture Spring Boot classique en couches — Controller, Service, Repository, Entity — avec MongoDB comme base de données et une authentification JWT maison.

Mes impressions honnêtes
Ce que cette API couvre bien :
Elle couvre exactement ce qu'on appelle dans notre conception le moteur de règles basique — attribution de points selon des intervalles de montant, gestion des récompenses simples, historique des transactions. Pour un cas d'usage simple, elle est suffisante.
Ce qu'elle ne couvre pas par rapport à notre conception :
Elle n'est pas multi-tenant. Il n'y a aucune notion de tenantId dans les endpoints ni dans les entités. Un seul utilisateur admin configure tout le système — ce n'est pas un SaaS configurable par plusieurs entreprises indépendantes.
Elle n'a pas de Wallet. Il n'y a aucune gestion de solde monétaire, de recharge Mobile Money, de débit ou de retrait. Le wallet.events Kafka qu'on a conçu n'existe pas ici.
Son moteur de règles est basique — uniquement des intervalles de montant. Il n'y a pas de Trigger configurable, pas de Conditions composées, pas de fenêtres temporelles, pas de streaks, pas de paliers avec multiplicateurs.
Elle n't a pas de parrainage, pas de codes promo, pas de campagnes temporisées.
Elle utilise MongoDB, pas PostgreSQL avec le schéma-per-tenant qu'on a conçu. La stack est incompatible avec notre R2DBC réactif.
Elle n'est pas réactive — pas de WebFlux, pas de R2DBC, architecture bloquante classique.

Ce que ça implique concrètement pour notre backend
Ce qu'on ne développe plus nous-mêmes :
Le calcul basique de points par transaction — on délègue à cette API. La gestion des récompenses simples (types, attribution automatique) — on délègue. L'historique des transactions de bonification — on délègue.
Ce qu'on développe quand même :
Le Wallet complet — cette API n'en parle pas. Le multi-tenancy — cette API ne le supporte pas. Les paliers et multiplicateurs — absents de cette API. Les codes promo et campagnes — absents. Le parrainage — absent. Toutes les fondations qu'on a conçues — filtres JWT, TenantContext, R2DBC, Kafka.

Comment intégrer cette API dans notre architecture
L'approche la plus propre est de traiter cette API comme un adapter de sortie dans notre architecture hexagonale. On crée un port de sortie dans notre domaine, et l'implémentation de ce port appelle l'API externe.
Notre domaine
    │
    │  BonificationPort (interface)
    │  + calculatePoints(transaction) : Mono<PointsResult>
    │  + getRewards(memberId) : Flux<Reward>
    │  + recordTransaction(transaction) : Mono<TransactionResult>
    │
    ▼
BonificationApiAdapter (infrastructure)
    │
    │  WebClient réactif
    │
    ▼
API Bonification externe
POST /api/transactions
GET  /api/transactions/{userId}
Concrètement dans le code :
BonificationPort.java — interface dans domain/loyalty/port/out/ :
java
public interface BonificationPort {
    Mono<PointsResult> recordTransaction(
        String externalUserId,
        BigDecimal amount,
        String description
    );
    Flux<TransactionHistory> getTransactionHistory(String externalUserId);
    Mono<Void> assignReward(String externalUserId, String rewardId);
}
BonificationApiAdapter.java — implémentation dans infrastructure/bonification/ :
java
@Component
public class BonificationApiAdapter implements BonificationPort {

    private final WebClient bonificationClient;
    private final BonificationAuthService authService;

    // recordTransaction() appelle POST /api/transactions
    // getTransactionHistory() appelle GET /api/transactions/{userId}
    // Gère l'authentification JWT de l'API externe
    // Gère les erreurs et timeouts
    // Mappe les réponses vers nos modèles domaine
}
BonificationAuthService.java — gère l'authentification auprès de l'API externe. L'API utilise JWT — il faut s'authentifier une première fois, cacher le token, et le renouveler à expiration :
java
@Component
public class BonificationAuthService {
    // Cache le JWT de l'API externe dans Redis
    // Clé : "bonification:auth:token:{tenantId}"
    // Se ré-authentifie automatiquement si le token expire
    // Chaque tenant a ses propres credentials pour l'API externe
}

Les problèmes qu'il faut anticiper
Problème 1 — Le multi-tenant
L'API externe n'est pas multi-tenant. Si RidnGo et KSM utilisent tous les deux cette API, leurs données sont mélangées. La solution est de créer un compte séparé sur l'API externe pour chaque tenant de notre système, et de stocker les credentials de chaque tenant dans notre TenantConfig. Notre adapter utilise les credentials du tenant courant pour chaque appel.
Problème 2 — La cohérence des données
Notre système et l'API externe ont chacun leur propre base de données. Si notre système crédite le wallet d'un membre mais que l'appel à l'API externe échoue, les deux systèmes sont incohérents. Il faut gérer ça avec le pattern Saga — soit les deux réussissent, soit on compense.
Problème 3 — La latence
Chaque opération de fidélisation fait maintenant un appel réseau supplémentaire vers l'API externe. Il faut configurer des timeouts agressifs (max 3 secondes), un circuit breaker avec Resilience4j, et un fallback en cas d'indisponibilité de l'API externe.
Problème 4 — La stack incompatible
L'API externe est bloquante (pas de WebFlux). Notre système est réactif. Le WebClient de Spring WebFlux peut appeler n'importe quelle API REST — c'est lui qui assure le pont entre le monde réactif (notre système) et le monde bloquant (l'API externe).

Les directives pour la suite
Directive 1 — Créer un module d'intégration dédié
Ne pas disséminer les appels à l'API externe partout dans le code. Tout passe par BonificationPort et BonificationApiAdapter. Si l'API externe change ou devient indisponible, on ne touche qu'à un seul fichier.
infrastructure/
└── bonification/
    ├── BonificationApiAdapter.java
    ├── BonificationAuthService.java
    ├── BonificationWebClientConfig.java
    ├── dto/
    │   ├── BonificationTransactionRequest.java
    │   ├── BonificationTransactionResponse.java
    │   └── BonificationRewardResponse.java
    └── mapper/
        └── BonificationMapper.java
Directive 2 — Resilience4j pour la résilience
Ajouter Resilience4j avec circuit breaker et retry sur tous les appels vers l'API externe. Si l'API externe est indisponible, le circuit s'ouvre et les appels échouent immédiatement sans attendre le timeout — notre système reste réactif.
java
@CircuitBreaker(name = "bonification-api", fallbackMethod = "fallbackRecordTransaction")
@Retry(name = "bonification-api")
@TimeLimiter(name = "bonification-api")
public Mono<PointsResult> recordTransaction(...) { ... }

public Mono<PointsResult> fallbackRecordTransaction(Exception e) {
    // Publier un event sur Kafka pour retry ultérieur
    // Retourner un résultat dégradé mais fonctionnel
}
Directive 3 — Ne pas faire confiance aveuglément
Tout ce que l'API externe retourne doit être validé avant utilisation. Si elle retourne un nombre de points négatif ou un rewardId invalide, notre système doit le détecter et le traiter comme une erreur.
Directive 4 — Journaliser tous les appels
Chaque appel vers l'API externe et sa réponse doivent être loggués avec le tenantId, le memberId, le requestId et la durée. En cas de problème, c'est la seule façon de savoir ce qui s'est passé.
Directive 5 — Garder notre modèle domaine indépendant
Notre domaine ne doit jamais connaître les DTOs de l'API externe. Le mapper dans infrastructure/bonification/mapper/ fait la conversion. Si l'API externe change son format de réponse, seul le mapper change — pas le domaine.

Résumé visuel de ce qui change
AVANT (tout développé par nous)
────────────────────────────────
Notre domaine → Notre RuleEngine → Notre PointsEngine → PostgreSQL

APRÈS (intégration API externe)
────────────────────────────────
Notre domaine
    ├── Wallet Module       → On développe entièrement
    ├── Multi-tenancy       → On développe entièrement
    ├── Paliers             → On développe entièrement
    ├── Parrainage          → On développe entièrement
    ├── Codes promo         → On développe entièrement
    ├── Campagnes           → On développe entièrement
    └── Calcul points
        et récompenses
        basiques            → BonificationPort
                                    ↓
                            BonificationApiAdapter
                                    ↓
                            API Bonification externe
Le travail restant est donc encore très conséquent. L'API externe enlève uniquement la partie calcul de points basique et gestion des récompenses simples — tout ce qui fait la valeur différenciante de notre SaaS reste à développer.
UN CAMARADE utilisera claude code pour integerer l'API, mais j'ai peur qu'il ne soit plus dans le meme contexte, en format texte, je veux que tu genere un ensemble d'instruction pour faire cela en respectant l'architecture hexagoanale et toutes le contrainte
08:45
Instructions d'intégration — API de Bonification externe
Contexte du projet à lire avant de commencer
Ce projet est le backend du Loyalty Programme, un SaaS multi-tenant de fidélisation construit en Spring Boot 3.4 avec Spring WebFlux (réactif non-bloquant), R2DBC pour la persistance PostgreSQL, Redis pour le cache et l'idempotence, et Apache Kafka pour les événements. L'architecture est hexagonale stricte avec DDD — le domaine ne dépend de rien d'externe, tout passe par des ports (interfaces) et des adapters (implémentations).
Le projet est dans le package racine com.yowyob.loyalty.
La règle absolue : aucun fichier dans domain/ ne doit importer quoi que ce soit de Spring, R2DBC, Redis, Kafka, ou de l'API externe. Si tu ajoutes un import Spring dans le domaine, tu casses l'architecture et les tests ArchUnit échoueront.

Ce qu'est l'API de Bonification externe
C'est une API REST développée par l'équipe 5GI. Elle gère le calcul de points par transaction selon des règles d'intervalles, la gestion des récompenses simples et l'historique des transactions de bonification. Elle utilise une authentification JWT propre — elle a son propre système de login indépendant du nôtre. Elle n'est pas multi-tenant — chaque tenant de notre système aura son propre compte sur cette API.
Ce qu'elle expose comme endpoints principaux :
POST /api/transactions — enregistre une transaction et calcule les points automatiquement. GET /api/transactions/{userId} — récupère l'historique des transactions d'un utilisateur.
Les autres endpoints (gestion des règles, des récompenses, des bénéficiaires) sont accessibles selon la documentation fournie.

Ce que tu dois faire et dans quel ordre

Étape 1 — Lire et comprendre la structure existante avant de toucher quoi que ce soit
Ouvre et lis ces fichiers dans l'ordre avant d'écrire une seule ligne de code.
Lis shared/multitenancy/TenantContextHolder.java pour comprendre comment on récupère le tenant courant depuis le contexte Reactor. Tu en auras besoin partout.
Lis domain/shared/model/TenantId.java et domain/shared/model/Money.java pour comprendre les Value Objects du domaine. Tout ce que tu crées dans le domaine doit suivre ce style — immuable, Java pur, zéro annotation.
Lis domain/tenant/model/Tenant.java et domain/tenant/port/out/TenantRepository.java pour voir comment un agrégat domaine et son port de sortie sont structurés. Ton travail doit suivre exactement ce pattern.
Lis infrastructure/persistence/tenant/adapter/TenantRepositoryAdapter.java pour voir comment un adapter implémente un port du domaine. Ton adapter de l'API externe doit suivre ce même pattern.
Lis shared/exception/AppException.java et shared/exception/ErrorCode.java pour comprendre comment les erreurs sont gérées. Tu ajouteras tes propres ErrorCode mais sans toucher aux existants.
Lis test/architecture/HexagonalArchitectureTest.java pour comprendre les règles architecturales automatiquement vérifiées. Ton code doit passer ces tests.

Étape 2 — Ajouter les dépendances Maven nécessaires
Ouvre le fichier pom.xml et ajoute ces dépendances dans la section <dependencies>. Ne modifie rien d'autre dans le pom.xml.
xml
<!-- Resilience4j pour circuit breaker et retry -->
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.2.0</version>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-reactor</artifactId>
    <version>2.2.0</version>
</dependency>

<!-- WebClient pour appels HTTP réactifs -->
<!-- Déjà inclus via spring-boot-starter-webflux — ne pas rajouter -->

<!-- Vérifier que cette dépendance est déjà présente, sinon ajouter -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>

Étape 3 — Créer les modèles du domaine de bonification
Ces fichiers vont dans domain/loyalty/model/. Ils sont en Java pur — zéro annotation Spring, zéro import externe.
Crée domain/loyalty/model/BonificationTransaction.java
C'est un record Java 21 immuable représentant une transaction de bonification du point de vue du domaine. Il contient : transactionId (String), externalUserId (String — l'identifiant du membre dans l'API externe), amount (BigDecimal), description (String), pointsEarned (Integer — les points calculés par l'API externe), status (String), occurredAt (Instant).
Ce record ne sait pas qu'il vient d'une API externe. Il représente le concept métier de transaction de bonification dans le domaine.
Crée domain/loyalty/model/BonificationReward.java
Record immuable avec : rewardId (String), name (String), description (String), type (String), value (BigDecimal).
Crée domain/loyalty/model/PointsResult.java
Record immuable représentant le résultat du calcul de points après une transaction. Contient : transactionId (String), pointsEarned (Integer), totalPoints (Integer — solde total après cette transaction), rewardTriggered (boolean — indique si une récompense a été déclenchée), triggeredReward (BonificationReward optionnel).
Crée domain/loyalty/model/TransactionHistory.java
Record immuable représentant l'historique des transactions. Contient : transactions (List de BonificationTransaction), totalCount (Integer), memberId (String).

Étape 4 — Créer les événements de domaine
Ces fichiers vont dans domain/loyalty/event/. Ils implémentent tous l'interface DomainEvent existante.
Crée domain/loyalty/event/BonificationTransactionRecordedEvent.java
Record implémentant DomainEvent. Contient : eventId (UUID), occurredAt (Instant), tenantId (TenantId), memberId (String), transactionId (String), pointsEarned (Integer), rewardTriggered (boolean). Implémente toutes les méthodes de l'interface DomainEvent.
Crée domain/loyalty/event/BonificationRewardTriggeredEvent.java
Record implémentant DomainEvent. Contient : eventId (UUID), occurredAt (Instant), tenantId (TenantId), memberId (String), rewardId (String), rewardName (String).

Étape 5 — Créer le port de sortie dans le domaine
Ce fichier va dans domain/loyalty/port/out/. C'est une interface Java pure — aucune annotation Spring, aucun import de Spring ou de WebFlux.
Crée domain/loyalty/port/out/BonificationPort.java
C'est le contrat que le domaine déclare pour ses besoins en bonification. Le domaine dit "j'ai besoin de ces capacités" sans savoir comment elles sont implémentées.
L'interface déclare ces méthodes, toutes retournant des types Reactor (Mono, Flux) car le domaine sait qu'il est dans un contexte réactif — mais il ne sait pas que c'est Spring qui fournit Reactor :
Mono<PointsResult> recordTransaction(
    TenantId tenantId,
    String externalUserId,
    BigDecimal amount,
    String description
)

Flux<BonificationTransaction> getTransactionHistory(
    TenantId tenantId,
    String externalUserId
)

Mono<Void> createBeneficiary(
    TenantId tenantId,
    String externalUserId,
    String email,
    String name
)

Mono<List<BonificationReward>> getAvailableRewards(
    TenantId tenantId,
    String externalUserId
)
Note importante : le tenantId est passé en paramètre ici car les ports de sortie du domaine reçoivent explicitement le contexte dont ils ont besoin. C'est l'adapter qui saura comment utiliser ce tenantId pour choisir les bons credentials.

Étape 6 — Créer le port d'entrée dans le domaine
Ce fichier va dans domain/loyalty/port/in/.
Crée domain/loyalty/port/in/ProcessBonificationUseCase.java
Interface déclarant :
Mono<PointsResult> processTransaction(
    TenantId tenantId,
    String memberId,
    BigDecimal amount,
    String description,
    String idempotencyKey
)

Flux<BonificationTransaction> getMemberTransactionHistory(
    TenantId tenantId,
    String memberId
)

Étape 7 — Créer les DTOs de l'API externe
Ces fichiers vont dans infrastructure/bonification/dto/. Ce sont des classes simples annotées avec Jackson pour la désérialisation JSON. Elles représentent exactement ce que l'API externe envoie et reçoit — pas ce que notre domaine veut.
Crée infrastructure/bonification/dto/BonificationTransactionRequestDto.java
Classe avec les champs que l'API externe attend en entrée pour POST /api/transactions. Selon la documentation : userId (String), amount (Double ou BigDecimal selon ce que l'API accepte), description (String). Annote avec @JsonProperty si les noms de champs de l'API externe diffèrent des noms Java.
Crée infrastructure/bonification/dto/BonificationTransactionResponseDto.java
Classe avec les champs retournés par l'API externe. Selon la documentation : transactionId (String), userId (String), amount (Double), status (String). Ajoute tous les autres champs retournés en réalité par l'API quand tu l'appelles — la documentation peut être incomplète.
Crée infrastructure/bonification/dto/BonificationAuthRequestDto.java
Pour l'authentification sur l'API externe. Contient username (String) et password (String).
Crée infrastructure/bonification/dto/BonificationAuthResponseDto.java
Contient le token JWT retourné après authentification. Contient token (String) et expiresIn (Long optionnel).
Crée infrastructure/bonification/dto/BonificationBeneficiaryRequestDto.java
Pour la création d'un bénéficiaire. Contient les champs attendus par l'API externe pour POST sur l'endpoint de création de client.

Étape 8 — Créer le mapper
Ce fichier va dans infrastructure/bonification/mapper/.
Crée infrastructure/bonification/mapper/BonificationMapper.java
C'est une interface MapStruct annotée @Mapper(componentModel = "spring"). Elle déclare les méthodes de conversion entre les DTOs de l'API externe et les modèles du domaine :
BonificationTransaction toTransaction(BonificationTransactionResponseDto dto)
PointsResult toPointsResult(BonificationTransactionResponseDto dto)
BonificationTransactionRequestDto toRequestDto(BigDecimal amount, String userId, String description)
Si MapStruct ne suffit pas pour certaines conversions complexes (calcul de champs, logique conditionnelle), crée les méthodes default dans l'interface avec l'implémentation manuelle. Ne crée jamais de logique métier dans le mapper — uniquement de la conversion de format.

Étape 9 — Créer le service d'authentification vers l'API externe
Ce fichier va dans infrastructure/bonification/.
Crée infrastructure/bonification/BonificationAuthService.java
Ce composant Spring (@Component) gère l'authentification auprès de l'API externe. Il maintient un token JWT valide par tenant dans Redis pour éviter de se ré-authentifier à chaque appel.
Il injecte ReactiveRedisTemplate<String, String> et WebClient (le client HTTP configuré pour l'API externe) et BonificationApiProperties.
Sa méthode principale getValidToken(TenantId tenantId) retourne Mono<String> — le Bearer token à utiliser. Elle fonctionne ainsi : construire la clé Redis bonification:auth:token:{tenantId}. Tenter de lire le token depuis Redis. Si présent et non expiré — le retourner directement. Si absent ou expiré — appeler POST sur l'endpoint de login de l'API externe avec les credentials du tenant, stocker le token obtenu dans Redis avec une TTL de 55 minutes (légèrement inférieure à la durée de vie réelle du token pour éviter les tokens expirés en transit), retourner le token.
Les credentials de chaque tenant (username et password pour l'API externe) sont stockés dans la TenantConfig du tenant dans notre base de données. Le service les récupère via TenantRepository.

Étape 10 — Créer la configuration du WebClient pour l'API externe
Ce fichier va dans infrastructure/bonification/.
Crée infrastructure/bonification/BonificationApiProperties.java
Annoté @ConfigurationProperties(prefix = "app.bonification"). Contient baseUrl (String), connectTimeoutMs (int — défaut 3000), readTimeoutMs (int — défaut 5000), maxRetries (int — défaut 3).
Crée infrastructure/bonification/BonificationWebClientConfig.java
Annoté @Configuration. Déclare un bean WebClient nommé bonificationWebClient. Configure le base URL depuis BonificationApiProperties. Configure les timeouts de connexion et de lecture via HttpClient de Netty. Configure les headers par défaut : Content-Type: application/json, Accept: application/json. Ce WebClient n'inclut PAS le header Authorization — celui-ci sera ajouté dynamiquement par l'adapter à chaque requête après avoir obtenu le token via BonificationAuthService.

Étape 11 — Créer l'adapter principal
Ce fichier va dans infrastructure/bonification/.
Crée infrastructure/bonification/BonificationApiAdapter.java
C'est le fichier le plus important de cette intégration. Il est annoté @Component et implémente BonificationPort du domaine. Il injecte WebClient bonificationWebClient, BonificationAuthService authService, BonificationMapper mapper.
La méthode recordTransaction fonctionne ainsi :
Appeler authService.getValidToken(tenantId) pour obtenir le token. Avec ce token, construire la requête avec le WebClient vers POST /api/transactions. Ajouter le header Authorization: Bearer {token}. Envoyer le corps BonificationTransactionRequestDto. Récupérer la réponse et la mapper via BonificationMapper.toPointsResult(). En cas d'erreur HTTP 401 — invalider le token dans Redis et retenter une seule fois avec un nouveau token. En cas d'erreur HTTP 4xx autre que 401 — lancer une BonificationApiException avec le détail de l'erreur. En cas d'erreur 5xx ou timeout — laisser Resilience4j gérer via le circuit breaker.
La méthode getTransactionHistory fonctionne de manière similaire sur GET /api/transactions/{userId}.
Ajoute l'annotation Resilience4j sur chaque méthode :
java
@CircuitBreaker(name = "bonification-api", fallbackMethod = "fallbackRecordTransaction")
@Retry(name = "bonification-api")
Crée les méthodes de fallback correspondantes. Le fallback pour recordTransaction publie un événement Kafka sur le topic bonification.retry pour un retry ultérieur et retourne un PointsResult dégradé avec pointsEarned = 0 et rewardTriggered = false — l'opération principale (paiement, achat) ne doit pas échouer à cause d'une indisponibilité de l'API de bonification.

Étape 12 — Créer le service domaine
Ce fichier va dans domain/loyalty/service/.
Crée domain/loyalty/service/BonificationDomainService.java
Classe Java pure sans annotation Spring. Elle reçoit BonificationPort via le constructeur — injection par constructeur uniquement, jamais par champ pour préserver la testabilité. Elle implémente ProcessBonificationUseCase.
Sa méthode processTransaction orchestre : appeler bonificationPort.recordTransaction(), si le résultat indique rewardTriggered = true créer un BonificationRewardTriggeredEvent, dans tous les cas créer un BonificationTransactionRecordedEvent, retourner le PointsResult.
Elle ne fait aucun appel à une base de données. Elle ne connaît pas Redis, Kafka ou Spring. Elle contient uniquement la logique métier pure.

Étape 13 — Créer le handler applicatif
Ce fichier va dans application/loyalty/handler/.
Crée application/loyalty/handler/ProcessBonificationHandler.java
Annoté @Service. Implémente ProcessBonificationUseCase. Il orchestre le cas d'utilisation complet. Il injecte BonificationDomainService, IdempotencyPort, WalletEventPublisherPort (pour publier les events Kafka).
Sa méthode processTransaction :
Vérifier l'idempotence via IdempotencyPort — si déjà traité, retourner la réponse mise en cache. Appeler BonificationDomainService.processTransaction(). Publier les événements de domaine sur Kafka via WalletEventPublisherPort. Mettre la réponse en cache idempotence. Retourner le résultat.

Étape 14 — Créer le contrôleur REST
Ce fichier va dans api/loyalty/.
Crée api/loyalty/BonificationController.java
RouterFunction WebFlux. Déclare ces routes :
POST /api/v1/loyalty/transactions — enregistre une transaction de bonification. Requiert JWT valide. Requiert header Idempotency-Key. Lit le TenantContext via TenantContextHolder. Appelle ProcessBonificationHandler.
GET /api/v1/loyalty/transactions — retourne l'historique des transactions du membre courant. Requiert JWT valide.
Crée api/loyalty/dto/request/BonificationTransactionRequest.java
DTO d'entrée de l'API avec amount (BigDecimal), description (String). Annoté avec Bean Validation : @NotNull @Positive sur amount, @NotBlank @Size(max=255) sur description.
Crée api/loyalty/dto/response/PointsResultResponse.java
DTO de réponse avec transactionId, pointsEarned, totalPoints, rewardTriggered, reward (optionnel). Construit depuis PointsResult domaine par le contrôleur.

Étape 15 — Ajouter les ErrorCodes et exceptions
Ouvre shared/exception/ErrorCode.java et ajoute ces nouvelles entrées à l'enum sans toucher aux existantes :
BONIFICATION_API_UNAVAILABLE(503),
BONIFICATION_API_AUTH_FAILED(502),
BONIFICATION_API_TRANSACTION_FAILED(502),
BONIFICATION_BENEFICIARY_NOT_FOUND(404),
BONIFICATION_CIRCUIT_OPEN(503)
Crée shared/exception/BonificationApiException.java
Hérite de AppException. Constructeur prenant un ErrorCode et un detail. Représente toute erreur venant de l'API externe.

Étape 16 — Ajouter la configuration dans application.yml
Ouvre resources/application.yml et ajoute cette section sans toucher à l'existant :
yaml
app:
  bonification:
    base-url: ${BONIFICATION_API_URL:http://localhost:8081}
    connect-timeout-ms: 3000
    read-timeout-ms: 5000
    max-retries: 3

resilience4j:
  circuitbreaker:
    instances:
      bonification-api:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        permitted-number-of-calls-in-half-open-state: 3
  retry:
    instances:
      bonification-api:
        max-attempts: 3
        wait-duration: 1s
        retry-exceptions:
          - java.io.IOException
          - java.util.concurrent.TimeoutException
  timelimiter:
    instances:
      bonification-api:
        timeout-duration: 5s
Ajoute la variable d'environnement BONIFICATION_API_URL dans application-dev.yml avec la valeur de l'URL locale de l'API externe pour le développement.

Étape 17 — Créer les tests
Crée test/domain/loyalty/BonificationDomainServiceTest.java
Test JUnit 5 sans Spring. Crée une implémentation en mémoire de BonificationPort (une inner class dans le test qui implémente l'interface). Teste processTransaction avec succès. Teste processTransaction quand une récompense est déclenchée. Vérifie que les bons événements domaine sont produits. Zéro mock framework si possible — utilise de vrais fakes.
Crée test/infrastructure/bonification/BonificationApiAdapterTest.java
Test avec @SpringBootTest minimal ou @ExtendWith(MockitoExtension.class). Mock le WebClient avec MockWebServer de OkHttp (pas de mock Mockito sur WebClient — ça ne fonctionne pas bien en réactif). Teste le cas nominal — réponse 200 de l'API externe. Teste le cas 401 — vérifier que le token est invalidé et qu'une seconde tentative est faite. Teste le cas timeout — vérifier que le fallback est déclenché. Teste le circuit breaker — après plusieurs échecs, vérifier que le circuit s'ouvre.
Crée test/application/loyalty/ProcessBonificationHandlerTest.java
Teste l'orchestration complète avec des mocks de BonificationDomainService et IdempotencyPort. Vérifie que l'idempotence fonctionne — même clé soumise deux fois ne déclenche qu'un seul appel au service domaine.
Ajoute dans test/architecture/HexagonalArchitectureTest.java
Une nouvelle règle ArchUnit :
java
@ArchTest
static final ArchRule bonification_adapter_implements_port =
    classes()
        .that().resideInAPackage("..infrastructure.bonification..")
        .and().haveSimpleNameEndingWith("Adapter")
        .should().implement(
            com.yowyob.loyalty.domain.loyalty.port.out.BonificationPort.class
        )
        .because("L'adapter de bonification doit implémenter le port du domaine");

Étape 18 — Vérification finale avant de considérer le travail terminé
Exécute dans l'ordre ces vérifications et ne passe à la suivante que si la précédente est verte.
Vérification 1 — mvn test -Dtest=HexagonalArchitectureTest. Tous les tests ArchUnit doivent passer. Si des violations sont détectées, les corriger avant de continuer.
Vérification 2 — Ouvre chaque fichier que tu as créé dans domain/. Vérifie manuellement qu'aucun import ne commence par org.springframework, io.r2dbc, org.apache.kafka, io.lettuce. Si tu en trouves, retire-les et trouve une autre façon.
Vérification 3 — mvn test -Dtest=BonificationDomainServiceTest. Le test du domaine doit passer sans démarrer de contexte Spring.
Vérification 4 — mvn test -Dtest=BonificationApiAdapterTest. Les tests de l'adapter doivent passer.
Vérification 5 — mvn test. Tous les tests du projet doivent passer, y compris les tests existants qui ne doivent pas avoir été cassés.
Vérification 6 — Démarre l'application en local avec mvn spring-boot:run -Dspring-boot.run.profiles=dev. L'application doit démarrer sans erreur. Appelle GET /api/v1/health — doit retourner 200. Appelle GET /api/v1/health/tenant avec un JWT valide — doit retourner le tenant correctement résolu.
Vérification 7 — Appelle POST /api/v1/loyalty/transactions avec un JWT valide, un header Idempotency-Key et un body {"amount": 5000, "description": "Test achat"}. Si l'API externe n'est pas disponible, le fallback doit s'activer et retourner une réponse dégradée avec status 200 — pas une erreur 500. L'opération principale ne doit jamais échouer à cause de l'API externe.

Résumé de tous les fichiers à créer
domain/loyalty/
├── model/
│   ├── BonificationTransaction.java
│   ├── BonificationReward.java
│   ├── PointsResult.java
│   └── TransactionHistory.java
├── event/
│   ├── BonificationTransactionRecordedEvent.java
│   └── BonificationRewardTriggeredEvent.java
├── port/
│   ├── in/
│   │   └── ProcessBonificationUseCase.java
│   └── out/
│       └── BonificationPort.java
└── service/
    └── BonificationDomainService.java

application/loyalty/handler/
└── ProcessBonificationHandler.java

infrastructure/bonification/
├── BonificationApiAdapter.java
├── BonificationAuthService.java
├── BonificationWebClientConfig.java
├── BonificationApiProperties.java
├── dto/
│   ├── BonificationTransactionRequestDto.java
│   ├── BonificationTransactionResponseDto.java
│   ├── BonificationAuthRequestDto.java
│   ├── BonificationAuthResponseDto.java
│   └── BonificationBeneficiaryRequestDto.java
└── mapper/
    └── BonificationMapper.java

api/loyalty/
├── BonificationController.java
└── dto/
    ├── request/
    │   └── BonificationTransactionRequest.java
    └── response/
        └── PointsResultResponse.java

shared/exception/
└── BonificationApiException.java

test/
├── domain/loyalty/
│   └── BonificationDomainServiceTest.java
├── infrastructure/bonification/
│   └── BonificationApiAdapterTest.java
└── application/loyalty/
    └── ProcessBonificationHandlerTest.java
Ce qu'il ne faut surtout pas faire
Ne pas appeler le WebClient directement depuis un service du domaine. Ne pas mettre d'annotations Spring dans les classes de domain/. Ne pas mettre de logique métier dans BonificationApiAdapter — uniquement de la conversion et des appels HTTP. Ne pas modifier les fichiers existants du projet sauf ErrorCode.java (ajout uniquement), application.yml (ajout uniquement) et HexagonalArchitectureTest.java (ajout uniquement). Ne pas retourner les DTOs de l'infrastructure (BonificationTransactionResponseDto) hors du package infrastructure/bonification/ — le mapper les convertit en modèles domaine avant que quoi que ce soit d'autre les voie.
UN CAMARADE utilisera claude code pour integerer l'API, mais j'ai peur qu'il ne soit plus dans le meme contexte, en format texte, je veux que tu genere un ensemble d'instruction pour faire cela en respectant l'architecture hexagoanale et toutes le contrainte
09:45
Instructions complètes — Intégration de l'API de Bonification

À lire absolument avant de toucher au code
Tu travailles sur le backend du Loyalty Programme, un SaaS multi-tenant de fidélisation. Avant d'écrire une seule ligne, tu dois comprendre dans quel projet tu te trouves.
Ce projet est construit avec Spring Boot 3.4, Spring WebFlux (tout est réactif et non-bloquant — pas de code bloquant autorisé), R2DBC pour PostgreSQL, Redis pour le cache, et Apache Kafka pour les événements entre modules. Le langage est Java 21 avec usage des records, sealed classes et virtual threads.
L'architecture est hexagonale stricte avec Domain-Driven Design. Cela signifie une seule chose fondamentale : le domaine ne dépend de rien d'extérieur. Ni Spring, ni PostgreSQL, ni Redis, ni Kafka, ni aucune API externe. C'est le monde extérieur qui s'adapte au domaine via des interfaces appelées ports et des implémentations appelées adapters.
Le package racine de tout le projet est com.yowyob.loyalty.

La règle absolue que tu ne peux pas violer
Ouvre n'importe quel fichier dans le dossier domain/. Si tu vois un import qui commence par l'un de ces préfixes, l'architecture est cassée et les tests automatiques échoueront :
org.springframework
io.r2dbc
org.apache.kafka
io.lettuce
com.fasterxml.jackson (sauf dans les records simples pour @JsonIgnore si absolument nécessaire)
Les seuls imports autorisés dans domain/ sont vers d'autres classes du même package domain/, vers le JDK standard (java.util, java.time, java.math, java.util.concurrent) et vers Project Reactor (reactor.core.publisher.Mono, reactor.core.publisher.Flux) car le domaine sait qu'il vit dans un monde réactif.

Ce qu'est l'API de Bonification externe
C'est une API REST développée par une autre équipe. Elle tourne sur son propre serveur avec sa propre base de données MongoDB. Elle gère le calcul de points par transaction selon des règles d'intervalles configurées, la gestion des récompenses simples et l'historique des transactions de bonification.
Elle a son propre système d'authentification JWT complètement indépendant du nôtre. Pour l'appeler, il faut d'abord se connecter avec un username et un password pour obtenir un token JWT, puis inclure ce token dans chaque requête suivante.
Elle n'est pas multi-tenant. Cela signifie que chaque tenant de notre système (RidnGo, KSM, EventaaS...) aura son propre compte séparé sur cette API externe. Les credentials de chaque tenant pour l'API externe sont stockés dans la configuration du tenant dans notre base de données.
Les endpoints principaux à utiliser sont les suivants. POST /api/transactions pour enregistrer une transaction et déclencher le calcul automatique des points. GET /api/transactions/{userId} pour récupérer l'historique des transactions d'un utilisateur. Il existe aussi des endpoints pour créer des bénéficiaires, gérer les récompenses et consulter les règles — consulte la documentation complète de l'API pour les détails.

Lire ces fichiers existants avant de commencer
Ces fichiers existent déjà dans le projet. Lis-les dans l'ordre indiqué car tout ce que tu vas créer doit suivre exactement les mêmes patterns.
Lis domain/shared/model/TenantId.java pour comprendre ce qu'est un Value Object dans ce projet. C'est un record Java 21 immuable, sans annotation, avec une méthode statique of() qui valide l'entrée avant construction.
Lis domain/shared/model/Money.java pour voir comment les types monétaires sont modélisés dans le domaine.
Lis domain/shared/port/DomainEvent.java pour voir l'interface que tous tes événements domaine doivent implémenter.
Lis domain/tenant/model/Tenant.java pour voir comment un agrégat domaine est structuré — classe Java pure, constructeur privé, factory method statique, méthodes métier, zéro annotation.
Lis domain/tenant/port/out/TenantRepository.java pour voir comment un port de sortie est déclaré — interface Java pure, méthodes retournant Mono ou Flux, zéro annotation Spring.
Lis infrastructure/persistence/tenant/adapter/TenantRepositoryAdapter.java pour voir comment un adapter implémente un port — annoté @Component, implémente l'interface du domaine, délègue au repository Spring Data, utilise un mapper pour convertir les entités.
Lis shared/multitenancy/TenantContextHolder.java pour comprendre comment récupérer le tenant courant depuis n'importe quel composant. Tu appelleras TenantContextHolder.getTenantId() qui retourne un Mono<TenantId>.
Lis shared/exception/AppException.java et shared/exception/ErrorCode.java pour comprendre la gestion des erreurs. Tu ajouteras tes propres codes d'erreur à l'enum mais sans modifier les codes existants.
Lis test/architecture/HexagonalArchitectureTest.java pour voir les règles architecturales vérifiées automatiquement. Ton code doit les passer toutes.

Étape 1 — Ajouter les dépendances Maven
Ouvre pom.xml. Dans la section <dependencies>, ajoute uniquement ces dépendances. Ne modifie rien d'autre dans ce fichier.
xml
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
    <version>2.2.0</version>
</dependency>

<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-reactor</artifactId>
    <version>2.2.0</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
Le WebClient est déjà disponible via spring-boot-starter-webflux qui est déjà dans le projet. Ne le rajoute pas.

Étape 2 — Créer les modèles du domaine
Ces fichiers vont tous dans src/main/java/com/yowyob/loyalty/domain/loyalty/model/. Ce sont des records Java 21 immuables. Zéro annotation. Zéro import extérieur.
Fichier 1 : BonificationTransaction.java
Ce record représente une transaction de bonification du point de vue du domaine. Il ne sait pas qu'il vient d'une API externe. Il contient les champs suivants : transactionId de type String, externalUserId de type String qui est l'identifiant du membre dans l'API externe, amount de type BigDecimal, description de type String, pointsEarned de type Integer représentant les points calculés par l'API externe, status de type String, occurredAt de type Instant.
Fichier 2 : BonificationReward.java
Record représentant une récompense. Contient : rewardId String, name String, description String, type String, value BigDecimal.
Fichier 3 : PointsResult.java
Record représentant le résultat du calcul de points après une transaction. Contient : transactionId String, pointsEarned Integer, totalPoints Integer représentant le solde total après cette transaction, rewardTriggered boolean indiquant si une récompense a été déclenchée, triggeredReward de type BonificationReward pouvant être null.
Ajoute une méthode hasReward() qui retourne rewardTriggered && triggeredReward != null.
Fichier 4 : TransactionHistory.java
Record contenant : transactions de type List<BonificationTransaction>, totalCount Integer, memberId String.

Étape 3 — Créer les événements de domaine
Ces fichiers vont dans src/main/java/com/yowyob/loyalty/domain/loyalty/event/. Chaque événement est un record Java 21 qui implémente l'interface DomainEvent existante.
Fichier 1 : BonificationTransactionRecordedEvent.java
Record implémentant DomainEvent. Déclare les champs : eventId UUID, occurredAt Instant, tenantId TenantId, memberId String, transactionId String, pointsEarned Integer, rewardTriggered boolean. Implémente toutes les méthodes de l'interface DomainEvent en retournant les champs correspondants.
Fichier 2 : BonificationRewardTriggeredEvent.java
Record implémentant DomainEvent. Contient : eventId UUID, occurredAt Instant, tenantId TenantId, memberId String, rewardId String, rewardName String.

Étape 4 — Créer les ports du domaine
Ces fichiers vont dans src/main/java/com/yowyob/loyalty/domain/loyalty/port/. Ce sont des interfaces Java pures. Zéro annotation Spring. Les méthodes retournent Mono ou Flux de Reactor.
Fichier 1 : BonificationPort.java dans le sous-dossier out/
C'est le port de sortie. Le domaine déclare ici ce dont il a besoin sans savoir comment c'est implémenté.
L'interface déclare ces quatre méthodes :
La première méthode s'appelle recordTransaction et prend TenantId tenantId, String externalUserId, BigDecimal amount, String description. Elle retourne Mono<PointsResult>.
La deuxième s'appelle getTransactionHistory et prend TenantId tenantId, String externalUserId. Elle retourne Flux<BonificationTransaction>.
La troisième s'appelle createBeneficiary et prend TenantId tenantId, String externalUserId, String email, String name. Elle retourne Mono<Void>.
La quatrième s'appelle getAvailableRewards et prend TenantId tenantId, String externalUserId. Elle retourne Flux<BonificationReward>.
Fichier 2 : ProcessBonificationUseCase.java dans le sous-dossier in/
C'est le port d'entrée que le contrôleur utilisera.
L'interface déclare deux méthodes :
La première processTransaction prend TenantId tenantId, String memberId, BigDecimal amount, String description, String idempotencyKey et retourne Mono<PointsResult>.
La deuxième getMemberTransactionHistory prend TenantId tenantId, String memberId et retourne Flux<BonificationTransaction>.

Étape 5 — Créer le service du domaine
Ce fichier va dans src/main/java/com/yowyob/loyalty/domain/loyalty/service/BonificationDomainService.java.
C'est une classe Java ordinaire. Pas d'annotation Spring. Elle implémente ProcessBonificationUseCase.
Elle reçoit BonificationPort via son constructeur — injection par constructeur uniquement, jamais par champ avec @Autowired. Cela la rend testable sans Spring.
Sa méthode processTransaction fait ceci dans l'ordre. Elle appelle bonificationPort.recordTransaction() avec les paramètres reçus. Si le résultat a rewardTriggered = true, elle crée un BonificationRewardTriggeredEvent avec les données du résultat. Dans tous les cas elle crée un BonificationTransactionRecordedEvent. Elle retourne le PointsResult.
Pour publier les événements, elle ne peut pas appeler Kafka directement — le domaine ne connaît pas Kafka. À la place, elle retourne les événements via le PointsResult enrichi, ou tu peux ajouter un second port de sortie BonificationEventPort que l'adapter implémentera avec Kafka. Choisis la deuxième option car elle est plus propre.
Sa méthode getMemberTransactionHistory délègue simplement à bonificationPort.getTransactionHistory().

Étape 6 — Créer les DTOs de l'API externe
Ces fichiers vont dans src/main/java/com/yowyob/loyalty/infrastructure/bonification/dto/. Ces classes représentent exactement ce que l'API externe envoie et reçoit. Elles peuvent avoir des annotations Jackson. Elles ne sortent jamais du package infrastructure/bonification/.
Fichier 1 : BonificationAuthRequestDto.java
Classe avec username String et password String. Annotée @JsonProperty si les noms attendus par l'API externe sont différents.
Fichier 2 : BonificationAuthResponseDto.java
Classe avec token String. Ajoute expiresIn Long si l'API le retourne.
Fichier 3 : BonificationTransactionRequestDto.java
Classe avec les champs que POST /api/transactions attend. Selon la documentation : userId String, amount BigDecimal ou Double selon ce que l'API accepte (vérifie en testant), description String.
Fichier 4 : BonificationTransactionResponseDto.java
Classe avec les champs retournés par POST /api/transactions. Selon la documentation : transactionId String, userId String, amount Double, status String. Ajoute tous les autres champs que l'API retourne réellement quand tu la testes — la documentation peut être incomplète concernant les points calculés et les récompenses déclenchées.
Fichier 5 : BonificationHistoryItemDto.java
Pour chaque élément retourné par GET /api/transactions/{userId}. Mêmes champs que la réponse de transaction plus les informations d'historique.
Fichier 6 : BonificationBeneficiaryRequestDto.java
Pour la création d'un bénéficiaire. Contient les champs attendus par l'API selon sa documentation.

Étape 7 — Créer le mapper
Ce fichier va dans src/main/java/com/yowyob/loyalty/infrastructure/bonification/mapper/BonificationMapper.java.
C'est une interface MapStruct. Annotée @Mapper(componentModel = "spring"). Elle convertit entre les DTOs de l'infrastructure et les modèles du domaine.
Déclare ces méthodes :
BonificationTransaction toTransaction(BonificationTransactionResponseDto dto) — convertit la réponse de l'API en modèle domaine.
PointsResult toPointsResult(BonificationTransactionResponseDto dto) — si l'API retourne les points dans sa réponse. Sinon, cette conversion sera faite manuellement dans l'adapter.
BonificationTransactionRequestDto toRequestDto(String userId, BigDecimal amount, String description) — construit le DTO de requête.
Pour les champs qui ne correspondent pas directement entre le DTO et le modèle domaine, utilise @Mapping(source = "...", target = "..."). Pour les conversions complexes avec de la logique, crée une méthode default dans l'interface avec l'implémentation manuelle.

Étape 8 — Créer la configuration de l'API externe
Fichier 1 : BonificationApiProperties.java dans infrastructure/bonification/
Annotée @ConfigurationProperties(prefix = "app.bonification"). Contient : baseUrl String, connectTimeoutMs int avec valeur par défaut 3000, readTimeoutMs int avec valeur par défaut 5000, maxRetries int avec valeur par défaut 3.
Fichier 2 : BonificationWebClientConfig.java dans infrastructure/bonification/
Annotée @Configuration. Déclare un bean WebClient nommé bonificationWebClient avec @Bean("bonificationWebClient"). Configure le base URL depuis BonificationApiProperties. Configure les timeouts de connexion et de lecture via HttpClient de Netty avec reactor.netty.http.client.HttpClient. Configure les headers par défaut Content-Type: application/json et Accept: application/json. Ce WebClient n'inclut PAS le header Authorization — il sera ajouté dynamiquement à chaque requête.
java
@Bean("bonificationWebClient")
public WebClient bonificationWebClient(BonificationApiProperties props) {
    HttpClient httpClient = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.getConnectTimeoutMs())
        .responseTimeout(Duration.ofMillis(props.getReadTimeoutMs()));

    return WebClient.builder()
        .baseUrl(props.getBaseUrl())
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
        .build();
}

Étape 9 — Créer le service d'authentification vers l'API externe
Fichier : BonificationAuthService.java dans infrastructure/bonification/
Annotée @Component. Ce composant maintient un token JWT valide par tenant dans Redis.
Il injecte via le constructeur : WebClient bonificationWebClient, ReactiveRedisTemplate<String, String> redisTemplate, TenantRepository tenantRepository, BonificationApiProperties properties.
Sa méthode principale est getValidToken(TenantId tenantId) qui retourne Mono<String>.
Elle fonctionne ainsi. Construire la clé Redis bonification:auth:token:{tenantId.value()} en utilisant RedisKeyBuilder existant dans le projet. Tenter de lire le token depuis Redis avec redisTemplate.opsForValue().get(key). Si la valeur est présente dans Redis, la retourner directement comme token valide. Si absente, charger le tenant depuis tenantRepository.findById(tenantId) pour obtenir ses credentials pour l'API externe stockés dans tenant.getConfig(). Appeler POST sur l'endpoint de login de l'API externe avec ces credentials. Récupérer le token JWT retourné. Le stocker dans Redis avec une TTL de 55 minutes via redisTemplate.opsForValue().set(key, token, Duration.ofMinutes(55)). Retourner le token.
Ajoute une méthode invalidateToken(TenantId tenantId) qui supprime la clé Redis. Elle sera appelée quand l'API retourne 401.

Étape 10 — Créer l'adapter principal
Fichier : BonificationApiAdapter.java dans infrastructure/bonification/
Annotée @Component. Implémente BonificationPort du domaine. C'est le fichier le plus important de toute cette intégration.
Injecte via le constructeur : @Qualifier("bonificationWebClient") WebClient webClient, BonificationAuthService authService, BonificationMapper mapper.
Implémentation de recordTransaction :
La méthode retourne Mono<PointsResult>. Elle fonctionne en chaîne réactive : appelle authService.getValidToken(tenantId) puis avec ce token construit la requête vers POST /api/transactions en ajoutant le header Authorization: Bearer {token}. Envoie le body BonificationTransactionRequestDto construit via le mapper. Récupère la réponse et la mappe en PointsResult via le mapper.
Pour gérer le 401 : utilise l'opérateur .onErrorResume() pour intercepter les erreurs. Si c'est une WebClientResponseException.Unauthorized, appelle authService.invalidateToken(tenantId) puis relance la requête une seule fois. Pour éviter une boucle infinie, utilise un booléen ou un compteur de retry.
Pour les autres erreurs HTTP 4xx : lancer une BonificationApiException avec le bon ErrorCode.
Ajoute les annotations Resilience4j sur la méthode :
java
@CircuitBreaker(name = "bonification-api", fallbackMethod = "fallbackRecordTransaction")
@Retry(name = "bonification-api")
Crée la méthode de fallback fallbackRecordTransaction avec la même signature mais un paramètre Throwable supplémentaire. Le fallback doit loguer l'erreur avec le tenantId et le memberId pour le debugging, puis retourner un PointsResult dégradé avec pointsEarned = 0, totalPoints = 0, rewardTriggered = false. Cela garantit que le paiement du membre n'est pas bloqué par une indisponibilité de l'API de bonification.
Implémentation de getTransactionHistory :
Suit le même pattern que recordTransaction mais sur GET /api/transactions/{externalUserId}. Retourne Flux<BonificationTransaction> en mappant chaque élément de la liste retournée.
Implémentation de createBeneficiary et getAvailableRewards :
Même pattern. Consulte la documentation de l'API externe pour les endpoints exacts.

Étape 11 — Créer le handler applicatif
Fichier : ProcessBonificationHandler.java dans src/main/java/com/yowyob/loyalty/application/loyalty/handler/
Annotée @Service. Implémente ProcessBonificationUseCase.
Injecte via le constructeur : BonificationDomainService domainService, IdempotencyPort idempotencyPort, port d'événements Kafka existant dans le projet.
Sa méthode processTransaction fait les choses suivantes dans cet ordre. Vérifier l'idempotence via idempotencyPort — si la clé existe déjà dans Redis, retourner la réponse mise en cache sans retraiter. Appeler domainService.processTransaction(). Publier les événements de domaine sur Kafka via le port d'événements. Mettre la réponse en cache idempotence avec TTL 24h. Retourner le résultat.

Étape 12 — Créer le contrôleur REST
Fichier 1 : BonificationController.java dans src/main/java/com/yowyob/loyalty/api/loyalty/
RouterFunction WebFlux. Déclare ces routes.
POST /api/v1/loyalty/transactions — requiert JWT valide, requiert header Idempotency-Key. Lit le TenantContext via TenantContextHolder.getTenantContext(). Lit le UserId depuis le SecurityContext. Valide le body de la requête. Appelle ProcessBonificationHandler. Retourne 201 avec le PointsResultResponse.
GET /api/v1/loyalty/transactions — requiert JWT valide. Retourne l'historique du membre courant.
Fichier 2 : BonificationTransactionRequest.java dans api/loyalty/dto/request/
Record Java 21 ou classe avec amount BigDecimal annoté @NotNull @Positive et description String annoté @NotBlank @Size(max = 255).
Fichier 3 : PointsResultResponse.java dans api/loyalty/dto/response/
Record Java 21 avec transactionId String, pointsEarned Integer, totalPoints Integer, rewardTriggered boolean, reward objet optionnel. Ce DTO est construit par le contrôleur depuis le PointsResult du domaine — le contrôleur fait la conversion, il ne retourne jamais directement un objet du domaine.

Étape 13 — Ajouter les codes d'erreur et l'exception
Ouvre shared/exception/ErrorCode.java. Ajoute ces nouvelles entrées à l'enum sans modifier les entrées existantes :
BONIFICATION_API_UNAVAILABLE(503),
BONIFICATION_API_AUTH_FAILED(502),
BONIFICATION_API_TRANSACTION_FAILED(502),
BONIFICATION_BENEFICIARY_NOT_FOUND(404),
BONIFICATION_CIRCUIT_OPEN(503)
Crée shared/exception/BonificationApiException.java
Hérite de AppException. Constructeur prenant ErrorCode errorCode et String detail. Pas de logique supplémentaire.

Étape 14 — Ajouter la configuration dans application.yml
Ouvre resources/application.yml. Ajoute cette section sous la section app: existante sans modifier ce qui existe déjà :
yaml
app:
  bonification:
    base-url: ${BONIFICATION_API_URL:http://localhost:8081}
    connect-timeout-ms: 3000
    read-timeout-ms: 5000
    max-retries: 3

resilience4j:
  circuitbreaker:
    instances:
      bonification-api:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
        permitted-number-of-calls-in-half-open-state: 3
        register-health-indicator: true
  retry:
    instances:
      bonification-api:
        max-attempts: 3
        wait-duration: 1s
        retry-exceptions:
          - java.io.IOException
          - java.util.concurrent.TimeoutException
  timelimiter:
    instances:
      bonification-api:
        timeout-duration: 5s
Ouvre resources/application-dev.yml. Ajoute :
yaml
app:
  bonification:
    base-url: http://localhost:8081
Remplace http://localhost:8081 par l'URL réelle de l'API externe en développement.

Étape 15 — Ajouter la règle ArchUnit pour valider l'intégration
Ouvre test/architecture/HexagonalArchitectureTest.java. Ajoute ces deux nouvelles règles sans modifier les règles existantes :
java
@ArchTest
static final ArchRule bonification_adapter_implements_port =
    classes()
        .that().resideInAPackage("..infrastructure.bonification..")
        .and().haveSimpleNameEndingWith("Adapter")
        .should().implement(
            com.yowyob.loyalty.domain.loyalty.port.out.BonificationPort.class
        )
        .because("L'adapter de bonification doit implémenter le port du domaine");

@ArchTest
static final ArchRule bonification_dtos_stay_in_infrastructure =
    noClasses()
        .that().resideInAPackage("..infrastructure.bonification.dto..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..domain..", "..api..", "..application..")
        .because("Les DTOs de l'API externe ne doivent pas fuiter hors de l'infrastructure");

Étape 16 — Créer les tests
Fichier 1 : BonificationDomainServiceTest.java dans test/domain/loyalty/
Test JUnit 5 sans Spring. Zéro annotation @SpringBootTest. Pour simuler BonificationPort, crée une inner class dans le fichier de test qui implémente l'interface avec une logique en mémoire — pas de mock Mockito si possible.
Teste les scénarios suivants. Transaction normale sans récompense — vérifie que pointsEarned est correct et rewardTriggered est false. Transaction déclenchant une récompense — vérifie que rewardTriggered est true et que l'événement BonificationRewardTriggeredEvent est produit. Vérifie que le service fonctionne sans aucune infrastructure réelle.
Fichier 2 : BonificationApiAdapterTest.java dans test/infrastructure/bonification/
Utilise MockWebServer de la librairie OkHttp pour simuler l'API externe — ne pas essayer de mocker WebClient directement avec Mockito, ça ne fonctionne pas bien en contexte réactif.
Ajoute cette dépendance de test dans pom.xml si pas déjà présente :
xml
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>mockwebserver</artifactId>
    <scope>test</scope>
</dependency>
Teste ces scénarios. Réponse 200 de l'API — vérifie que le PointsResult est correctement mappé. Réponse 401 de l'API — vérifie que le token est invalidé et qu'une seconde tentative est faite avec un nouveau token. Réponse 500 de l'API — vérifie que le fallback retourne un PointsResult dégradé avec pointsEarned = 0. Timeout — vérifie que le fallback est déclenché avant le timeout configuré.
Fichier 3 : ProcessBonificationHandlerTest.java dans test/application/loyalty/
Teste avec Mockito. Vérifie que la même idempotencyKey soumise deux fois ne déclenche qu'un seul appel au service domaine. Vérifie que les événements Kafka sont bien publiés après chaque transaction réussie.

Étape 17 — Vérifications finales obligatoires
Effectue ces vérifications dans l'ordre et ne passe à la suivante que si la précédente est verte. Si une vérification échoue, corrige le problème avant de continuer.
Vérification 1 — Tests d'architecture
Exécute mvn test -Dtest=HexagonalArchitectureTest. Tous les tests doivent passer en vert. S'il y a des violations, lis le message d'erreur qui indique exactement quel fichier viole quelle règle. Corrige avant de continuer.
Vérification 2 — Inspection manuelle du domaine
Ouvre chaque fichier que tu as créé dans domain/loyalty/. Vérifie manuellement qu'aucun import ne commence par org.springframework, io.r2dbc, org.apache.kafka, io.lettuce. Si tu en trouves, retire-les. Si ta logique en a besoin, c'est qu'elle est au mauvais endroit — déplace-la dans application/ ou infrastructure/.
Vérification 3 — Test du domaine isolé
Exécute mvn test -Dtest=BonificationDomainServiceTest. Ce test doit passer sans démarrer de contexte Spring et sans connexion réseau. S'il démarre Spring ou fait des appels réseau, quelque chose est mal conçu dans le domaine.
Vérification 4 — Tests de l'adapter
Exécute mvn test -Dtest=BonificationApiAdapterTest. Vérifie que les scénarios de retry et de fallback fonctionnent correctement.
Vérification 5 — Tous les tests du projet
Exécute mvn test. Tous les tests existants doivent encore passer — tu ne dois pas avoir cassé quelque chose qui fonctionnait. S'il y a des régressions, identifie ce que tu as modifié qui les cause.
Vérification 6 — Démarrage de l'application
Démarre l'application avec mvn spring-boot:run -Dspring-boot.run.profiles=dev. Elle doit démarrer sans erreur. Appelle GET /api/v1/health — doit retourner 200 avec status UP. Appelle GET /api/v1/health/tenant avec un JWT valide — doit retourner le tenantId correctement résolu dans le corps.
Vérification 7 — Test de bout en bout avec l'API externe
Si l'API externe est disponible, appelle POST /api/v1/loyalty/transactions avec un JWT valide, le header Idempotency-Key: test-key-001 et le body {"amount": 5000, "description": "Test achat"}. La réponse doit contenir les points calculés par l'API externe.
Appelle exactement la même requête une seconde fois avec le même Idempotency-Key: test-key-001. La réponse doit être identique à la première sans que l'API externe soit appelée une seconde fois — c'est la preuve que l'idempotence fonctionne.
Si l'API externe n'est pas disponible, le fallback doit s'activer et retourner une réponse avec pointsEarned: 0 et status 200 — pas une erreur 500. L'opération principale du membre ne doit jamais être bloquée par l'indisponibilité de l'API de bonification.

Résumé de tous les fichiers à créer
domain/loyalty/
├── model/
│   ├── BonificationTransaction.java
│   ├── BonificationReward.java
│   ├── PointsResult.java
│   └── TransactionHistory.java
├── event/
│   ├── BonificationTransactionRecordedEvent.java
│   └── BonificationRewardTriggeredEvent.java
├── port/
│   ├── in/
│   │   └── ProcessBonificationUseCase.java
│   └── out/
│       ├── BonificationPort.java
│       └── BonificationEventPort.java
└── service/
    └── BonificationDomainService.java

application/loyalty/handler/
└── ProcessBonificationHandler.java

infrastructure/bonification/
├── BonificationApiAdapter.java
├── BonificationAuthService.java
├── BonificationWebClientConfig.java
├── BonificationApiProperties.java
├── dto/
│   ├── BonificationAuthRequestDto.java
│   ├── BonificationAuthResponseDto.java
│   ├── BonificationTransactionRequestDto.java
│   ├── BonificationTransactionResponseDto.java
│   ├── BonificationHistoryItemDto.java
│   └── BonificationBeneficiaryRequestDto.java
└── mapper/
    └── BonificationMapper.java

api/loyalty/
├── BonificationController.java
└── dto/
    ├── request/
    │   └── BonificationTransactionRequest.java
    └── response/
        └── PointsResultResponse.java

shared/exception/
└── BonificationApiException.java

Modifications dans les fichiers existants :
- pom.xml                           : ajout de 3 dépendances
- shared/exception/ErrorCode.java   : ajout de 5 codes d'erreur
- resources/application.yml         : ajout section bonification + resilience4j
- resources/application-dev.yml     : ajout URL locale de l'API externe
- test/architecture/HexagonalArchitectureTest.java : ajout de 2 règles ArchUnit

Nouveaux fichiers de test :
- test/domain/loyalty/BonificationDomainServiceTest.java
- test/infrastructure/bonification/BonificationApiAdapterTest.java
- test/application/loyalty/ProcessBonificationHandlerTest.java

Ce qu'il ne faut absolument pas faire
N'appelle jamais le WebClient directement depuis un fichier dans domain/. N'appelle jamais le WebClient directement depuis un fichier dans application/. Ne mets jamais d'annotations Spring dans les classes de domain/. Ne retourne jamais un DTO de infrastructure/bonification/dto/ hors du package infrastructure/bonification/ — le mapper les convertit avant. Ne modifie jamais un fichier existant ailleurs que les quatre fichiers listés dans les modifications autorisées. Ne crée jamais de logique métier dans BonificationApiAdapter ou BonificationMapper — uniquement de la conversion de format et des appels HTTP. Ne bloque jamais le thread avec .block() ou .blockFirst() — tout doit rester réactif avec Mono et Flux. Ne fais jamais un appel synchrone vers l'API externe — tout passe par WebClient réactif.


