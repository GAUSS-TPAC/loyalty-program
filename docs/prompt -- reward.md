Guide complet — Module Récompenses (Rewards)
Contexte obligatoire à lire avant toute action
Tu travailles sur le backend du Loyalty Programme Royal, un SaaS multi-tenant de fidélisation. Le package racine est com.yowyob.loyalty. Le projet utilise Spring Boot 3.4, Spring WebFlux (réactif — .block() interdit partout), R2DBC pour PostgreSQL, Redis pour le cache, Kafka pour les événements. L'architecture est hexagonale stricte avec DDD.
Les modules suivants sont déjà en place et fonctionnels — ne les modifie pas sauf instruction explicite :

Fondations : TenantContextHolder, filtres JWT, IdempotencyPort, RedisKeyBuilder
Module Wallet complet : Wallet, WalletDomainService, CreditWalletUseCase, DebitWalletUseCase
Moteur de règles : RuleEngine, LoyaltyDomainService, PointsAccount, ProcessEventUseCase

Le module Récompenses est le point de convergence entre le moteur de règles et l'expérience membre. Quand le moteur de règles déclenche un effet GRANT_REWARD, il publie un événement Kafka GrantRewardRequestedEvent. Ce module consomme cet événement, crée le RewardGrant, et gère tout le cycle de vie jusqu'à la consommation au checkout.
Règle absolue : aucun fichier dans domain/ n'importe quoi de Spring, R2DBC, Kafka, Redis, ou toute librairie externe.

Ce que ce module doit accomplir
Un tenant configure son catalogue de récompenses : "Ticket Premium Gratuit", "Réduction 20%", "500 XAF de cashback". Quand un membre remplit les conditions d'une règle, le moteur lui attribue une récompense du catalogue. Le membre peut aussi dépenser ses points pour en obtenir une. Au moment du paiement sur la plateforme cliente, la récompense est consommée de façon atomique et irréversible.

Phase 1 — Modèles du domaine Rewards
Ces fichiers vont dans src/main/java/com/yowyob/loyalty/domain/reward/. Zéro annotation Spring. Zéro import externe.
Fichier 1 : domain/reward/model/RewardType.java
Enum Java pur représentant les types de récompenses supportés.
javapublic enum RewardType {
    FREE_PRODUCT,        // Produit ou service offert
    PERCENT_DISCOUNT,    // Réduction en pourcentage sur le prochain achat
    FIXED_DISCOUNT,      // Réduction d'un montant fixe
    CASHBACK_WALLET,     // Crédit directement dans le wallet du membre
    EXCLUSIVE_ACCESS,    // Accès à une catégorie ou fonctionnalité réservée
    TIER_UPGRADE;        // Montée de palier immédiate

    public boolean hasMonetaryValue() {
        return this == PERCENT_DISCOUNT
            || this == FIXED_DISCOUNT
            || this == CASHBACK_WALLET;
    }

    public boolean requiresProductId() {
        return this == FREE_PRODUCT;
    }
}
Fichier 2 : domain/reward/model/RewardStatus.java
Enum représentant le cycle de vie d'une récompense dans le catalogue.
javapublic enum RewardStatus {
    DRAFT,      // Créée, non visible aux membres
    ACTIVE,     // Visible et échangeable
    PAUSED,     // Temporairement désactivée
    EXHAUSTED,  // Stock épuisé
    EXPIRED,    // Date de fin dépassée
    ARCHIVED;   // Archivée définitivement

    public boolean isAvailable() {
        return this == ACTIVE;
    }

    public boolean canTransitionTo(RewardStatus next) {
        return switch (this) {
            case DRAFT -> next == ACTIVE || next == ARCHIVED;
            case ACTIVE -> next == PAUSED || next == EXHAUSTED
                        || next == EXPIRED || next == ARCHIVED;
            case PAUSED -> next == ACTIVE || next == ARCHIVED;
            case EXHAUSTED -> next == ACTIVE || next == ARCHIVED;
            case EXPIRED, ARCHIVED -> false;
        };
    }
}
Fichier 3 : domain/reward/model/GrantStatus.java
Enum représentant le cycle de vie d'un RewardGrant — l'attribution d'une récompense à un membre spécifique.
javapublic enum GrantStatus {
    PENDING,    // Créé mais pas encore confirmé (ex: en attente de paiement)
    ACTIVE,     // Disponible et utilisable par le membre
    USED,       // Consommé au checkout — état final
    EXPIRED,    // Date d'expiration dépassée — état final
    REVERSED,   // Annulé suite à un remboursement — état final
    CANCELLED;  // Annulé manuellement par l'admin — état final

    public boolean isFinal() {
        return this == USED || this == EXPIRED
            || this == REVERSED || this == CANCELLED;
    }

    public boolean isUsable() {
        return this == ACTIVE;
    }

    public boolean canTransitionTo(GrantStatus next) {
        return switch (this) {
            case PENDING -> next == ACTIVE || next == CANCELLED || next == REVERSED;
            case ACTIVE  -> next == USED || next == EXPIRED || next == CANCELLED || next == REVERSED;
            default      -> false;
        };
    }
}
Fichier 4 : domain/reward/model/RewardValue.java
Value Object immuable représentant la valeur concrète d'une récompense. Ce record centralise le calcul de la réduction applicable.
javapublic record RewardValue(
    BigDecimal numericValue,  // 20.0 pour 20%, 5000 pour 5000 XAF
    String unit,              // "PERCENT", "XAF", "EUR", "PRODUCT_ID"
    int maxApplicationCount   // 1 = une seule utilisation, 2 = sur 2 prochains achats
) {
    // Validation dans le constructeur compact
    public RewardValue {
        if (numericValue == null || numericValue.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("numericValue doit être positif");
        if (unit == null || unit.isBlank())
            throw new IllegalArgumentException("unit ne peut pas être vide");
        if (maxApplicationCount < 1)
            throw new IllegalArgumentException("maxApplicationCount doit être >= 1");
    }

    // Calcule le montant de réduction à appliquer sur un montant donné
    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        if ("PERCENT".equals(unit)) {
            return orderAmount.multiply(numericValue)
                              .divide(new BigDecimal("100"), 2,
                                      java.math.RoundingMode.HALF_UP);
        }
        // Pour les montants fixes, retourne la valeur directement
        // (le contrôleur vérifie que ça ne dépasse pas le montant de commande)
        return numericValue;
    }

    public static RewardValue percent(BigDecimal pct, int applications) {
        return new RewardValue(pct, "PERCENT", applications);
    }

    public static RewardValue fixed(BigDecimal amount, String currency) {
        return new RewardValue(amount, currency, 1);
    }

    public static RewardValue product(String productId) {
        return new RewardValue(BigDecimal.ONE, productId, 1);
    }
}
Fichier 5 : domain/reward/model/Reward.java
Agrégat racine du catalogue de récompenses. Classe Java standard, constructeur privé, factory method statique.
javapublic class Reward {
    private final UUID id;
    private final TenantId tenantId;
    private String name;
    private String description;
    private RewardType type;
    private RewardValue value;
    private long costInPoints;      // 0 = gratuit (attribué par règle uniquement)
    private Integer stockTotal;     // null = stock illimité
    private Integer stockRemaining; // null = stock illimité
    private Instant validFrom;      // null = disponible immédiatement
    private Instant validUntil;     // null = pas de date de fin
    private int grantExpiryDays;    // Durée de validité du grant après attribution (0 = pas d'expiry)
    private String imageUrl;        // URL de l'image (optionnel)
    private Map<String, Object> metadata; // Données libres du tenant
    private RewardStatus status;
    private int version;
    private Instant createdAt;
    private Instant updatedAt;
Méthode statique create(UUID id, TenantId tenantId, String name, RewardType type, RewardValue value, long costInPoints, Integer stockTotal, Instant validFrom, Instant validUntil, int grantExpiryDays) — valide que name n'est pas vide, que costInPoints >= 0, que stockTotal est null ou > 0. Crée la récompense en statut DRAFT. Retourne le Reward.
Méthode activate() — vérifie la transition DRAFT → ACTIVE, retourne un nouveau Reward avec status ACTIVE.
Méthode pause() — transition ACTIVE → PAUSED.
Méthode resume() — transition PAUSED → ACTIVE.
Méthode archive() — transite vers ARCHIVED.
Méthode decrementStock() — si stockRemaining est null (illimité), ne fait rien. Sinon vérifie que stockRemaining > 0 — sinon lance RewardDomainException("Stock épuisé pour la récompense " + name). Décrémente stockRemaining. Si stockRemaining == 0, passe le status à EXHAUSTED. Retourne le Reward mis à jour.
Méthode restoreStock(int quantity) — incrémente stockRemaining. Si le status était EXHAUSTED, repasse à ACTIVE. Retourne le Reward.
Méthode isAvailableAt(Instant moment) — retourne true si status.isAvailable() ET (validFrom == null OU moment >= validFrom) ET (validUntil == null OU moment <= validUntil).
Méthode isRedeemableWithPoints() — retourne costInPoints > 0.
Méthode update(String name, String description, String imageUrl, Map<String, Object> metadata) — met à jour les champs modifiables. Ne permet pas de modifier type, value, costInPoints d'une récompense ACTIVE — lance exception si tentative.
Getters pour tous les champs.
Fichier 6 : domain/reward/model/RewardGrant.java
Agrégat représentant l'attribution concrète d'une récompense à un membre spécifique. Une fois créé, très peu de champs sont modifiables.
javapublic class RewardGrant {
    private final UUID id;
    private final TenantId tenantId;
    private final UserId memberId;
    private final UUID rewardId;
    private final String rewardName;      // Snapshot du nom au moment de l'attribution
    private final RewardType rewardType;  // Snapshot du type
    private final RewardValue rewardValue; // Snapshot de la valeur
    private final GrantSource source;     // Comment ce grant a été créé
    private final UUID sourceRuleId;      // Règle qui a déclenché (nullable)
    private final UUID sourceEventId;     // Event idempotency key (nullable)
    private GrantStatus status;
    private int remainingApplications;    // Nombre d'utilisations restantes
    private Instant grantedAt;
    private Instant expiresAt;            // null = jamais
    private Instant usedAt;               // null si pas encore utilisé
    private String usedInContext;         // JSON : {order_id, platform_ref} au moment de l'utilisation
    private int version;
Méthode statique create(UUID id, TenantId tenantId, UserId memberId, Reward reward, GrantSource source, UUID sourceRuleId, UUID sourceEventId) — crée le grant en statut PENDING. Copie les snapshots depuis reward (name, type, value). Calcule expiresAt = now + reward.grantExpiryDays() jours si grantExpiryDays > 0. Initialise remainingApplications = reward.value().maxApplicationCount(). Retourne le RewardGrant.
Méthode activate() — transition PENDING → ACTIVE. Retourne le grant mis à jour.
Méthode consume(String context) — vérifie que status.isUsable(), que expiresAt == null ou expiresAt.isAfter(now). Si ok, décrémente remainingApplications. Si remainingApplications == 0, passe status à USED. Sinon reste ACTIVE (pour les grants multi-utilisation). Met à jour usedAt = now et usedInContext = context. Retourne le grant.
Méthode expire() — vérifie que canTransitionTo(EXPIRED), passe status à EXPIRED.
Méthode reverse(String reason) — passe status à REVERSED.
Méthode cancel(String reason) — passe status à CANCELLED.
Méthode isExpired() — retourne expiresAt != null && Instant.now().isAfter(expiresAt).
Getters pour tous les champs.
Fichier 7 : domain/reward/model/GrantSource.java
Enum indiquant l'origine d'un RewardGrant.
javapublic enum GrantSource {
    RULE_ENGINE,     // Déclenché automatiquement par le moteur de règles
    POINTS_REDEMPTION, // Échangé contre des points par le membre
    REFERRAL_BONUS,  // Récompense de parrainage
    MANUAL_ADMIN,    // Attribué manuellement par un admin
    CAMPAIGN_BONUS;  // Campagne promotionnelle
}
Fichier 8 : domain/reward/model/RedemptionRequest.java
Value Object représentant une demande d'échange de points contre une récompense. Immuable.
javapublic record RedemptionRequest(
    TenantId tenantId,
    UserId memberId,
    UUID rewardId,
    String idempotencyKey
) {
    public RedemptionRequest {
        Objects.requireNonNull(tenantId, "tenantId obligatoire");
        Objects.requireNonNull(memberId, "memberId obligatoire");
        Objects.requireNonNull(rewardId, "rewardId obligatoire");
        Objects.requireNonNull(idempotencyKey, "idempotencyKey obligatoire");
    }
}
Fichier 9 : domain/reward/model/RedemptionResult.java
Value Object représentant le résultat d'un échange de points.
javapublic record RedemptionResult(
    RewardGrant grant,
    long pointsSpent,
    long remainingPointsBalance
) {}
Fichier 10 : domain/reward/model/ConsumeGrantRequest.java
Value Object représentant une demande de consommation d'un grant au checkout.
javapublic record ConsumeGrantRequest(
    TenantId tenantId,
    UserId memberId,
    UUID grantId,
    String orderReference,       // Référence de la commande côté plateforme cliente
    BigDecimal orderAmount,      // Montant de la commande (pour valider la réduction)
    String idempotencyKey
) {}
Fichier 11 : domain/reward/model/ConsumeGrantResult.java
Value Object représentant le résultat de la consommation.
javapublic record ConsumeGrantResult(
    RewardGrant updatedGrant,
    BigDecimal discountApplied,  // Montant de réduction calculé
    BigDecimal finalOrderAmount, // Montant après réduction
    boolean fullyConsumed        // true si le grant est maintenant USED
) {}
Fichier 12 : domain/reward/exception/RewardDomainException.java
javapublic class RewardDomainException extends DomainException {
    public RewardDomainException(String message) { super(message); }
    public RewardDomainException(String message, Map<String, Object> details) {
        super(message, details);
    }
}
Fichier 13 : domain/reward/exception/RewardNotFoundException.java
javapublic class RewardNotFoundException extends RewardDomainException {
    public RewardNotFoundException(UUID rewardId) {
        super("Récompense introuvable : " + rewardId,
              Map.of("rewardId", rewardId.toString()));
    }
}
Fichier 14 : domain/reward/exception/GrantNotFoundException.java
javapublic class GrantNotFoundException extends RewardDomainException {
    public GrantNotFoundException(UUID grantId) {
        super("Grant introuvable : " + grantId,
              Map.of("grantId", grantId.toString()));
    }
}
Fichier 15 : domain/reward/exception/InsufficientPointsException.java
javapublic class InsufficientPointsException extends RewardDomainException {
    public InsufficientPointsException(long required, long available) {
        super("Points insuffisants pour l'échange",
              Map.of("required", required, "available", available));
    }
}
Fichier 16 : domain/reward/exception/GrantAlreadyUsedException.java
javapublic class GrantAlreadyUsedException extends RewardDomainException {
    public GrantAlreadyUsedException(UUID grantId) {
        super("Ce grant a déjà été utilisé",
              Map.of("grantId", grantId.toString()));
    }
}
Fichier 17 : domain/reward/exception/GrantExpiredException.java
javapublic class GrantExpiredException extends RewardDomainException {
    public GrantExpiredException(UUID grantId, Instant expiredAt) {
        super("Ce grant est expiré",
              Map.of("grantId", grantId.toString(),
                     "expiredAt", expiredAt.toString()));
    }
}

Phase 2 — Événements du domaine Rewards
Ces fichiers vont dans domain/reward/event/.
Fichier 18 : domain/reward/event/RewardDomainEvent.java
javapublic interface RewardDomainEvent extends DomainEvent {}
Fichier 19 : domain/reward/event/RewardGrantedEvent.java
javapublic record RewardGrantedEvent(
    UUID eventId,
    Instant occurredAt,
    TenantId tenantId,
    UserId memberId,
    UUID grantId,
    UUID rewardId,
    String rewardName,
    RewardType rewardType,
    GrantSource source
) implements RewardDomainEvent {
    @Override public String eventType() { return "reward.granted"; }
}
Fichier 20 : domain/reward/event/RewardConsumedEvent.java
javapublic record RewardConsumedEvent(
    UUID eventId,
    Instant occurredAt,
    TenantId tenantId,
    UserId memberId,
    UUID grantId,
    UUID rewardId,
    String orderReference,
    BigDecimal discountApplied
) implements RewardDomainEvent {
    @Override public String eventType() { return "reward.consumed"; }
}
Fichier 21 : domain/reward/event/RewardGrantExpiredEvent.java
javapublic record RewardGrantExpiredEvent(
    UUID eventId,
    Instant occurredAt,
    TenantId tenantId,
    UserId memberId,
    UUID grantId,
    UUID rewardId
) implements RewardDomainEvent {
    @Override public String eventType() { return "reward.grant.expired"; }
}
Fichier 22 : domain/reward/event/GrantRewardRequestedEvent.java
Événement entrant publié par le moteur de règles sur Kafka. Ce record est consommé par ce module.
javapublic record GrantRewardRequestedEvent(
    UUID eventId,
    Instant occurredAt,
    TenantId tenantId,
    UserId memberId,
    String rewardId,
    UUID sourceRuleId,
    String sourceEventIdempotencyKey
) implements RewardDomainEvent {
    @Override public String eventType() { return "reward.grant.requested"; }
}

Phase 3 — Ports du domaine Rewards
Fichier 23 : domain/reward/port/in/CreateRewardUseCase.java
javapublic interface CreateRewardUseCase {
    Mono<Reward> createReward(
        TenantId tenantId,
        String name,
        String description,
        RewardType type,
        RewardValue value,
        long costInPoints,
        Integer stockTotal,
        Instant validFrom,
        Instant validUntil,
        int grantExpiryDays,
        String imageUrl,
        Map<String, Object> metadata,
        String idempotencyKey
    );
}
Fichier 24 : domain/reward/port/in/UpdateRewardUseCase.java
javapublic interface UpdateRewardUseCase {
    Mono<Reward> updateReward(
        TenantId tenantId,
        UUID rewardId,
        String name,
        String description,
        String imageUrl,
        Map<String, Object> metadata
    );
    Mono<Reward> activateReward(TenantId tenantId, UUID rewardId);
    Mono<Reward> pauseReward(TenantId tenantId, UUID rewardId);
    Mono<Reward> archiveReward(TenantId tenantId, UUID rewardId);
}
Fichier 25 : domain/reward/port/in/GetRewardCatalogUseCase.java
javapublic interface GetRewardCatalogUseCase {
    Flux<Reward> getCatalog(TenantId tenantId, boolean activeOnly, int page, int size);
    Mono<Reward> getReward(TenantId tenantId, UUID rewardId);
}
Fichier 26 : domain/reward/port/in/RedeemRewardUseCase.java
javapublic interface RedeemRewardUseCase {
    Mono<RedemptionResult> redeem(RedemptionRequest request);
}
Fichier 27 : domain/reward/port/in/GrantRewardUseCase.java
javapublic interface GrantRewardUseCase {
    Mono<RewardGrant> grantReward(
        TenantId tenantId,
        UserId memberId,
        UUID rewardId,
        GrantSource source,
        UUID sourceRuleId,
        String sourceEventKey,
        String idempotencyKey
    );
}
Fichier 28 : domain/reward/port/in/ConsumeGrantUseCase.java
javapublic interface ConsumeGrantUseCase {
    Mono<ConsumeGrantResult> consumeGrant(ConsumeGrantRequest request);
}
Fichier 29 : domain/reward/port/in/GetMemberGrantsUseCase.java
javapublic interface GetMemberGrantsUseCase {
    Flux<RewardGrant> getActiveGrants(TenantId tenantId, UserId memberId);
    Flux<RewardGrant> getAllGrants(TenantId tenantId, UserId memberId, int page, int size);
    Mono<RewardGrant> getGrant(TenantId tenantId, UUID grantId);
}
Fichier 30 : domain/reward/port/out/RewardRepository.java
javapublic interface RewardRepository {
    Mono<Reward> save(Reward reward);
    Mono<Reward> findById(UUID id);
    Mono<Reward> findByIdAndTenant(UUID id, TenantId tenantId);
    Flux<Reward> findByTenant(TenantId tenantId, boolean activeOnly, int page, int size);
    Mono<Boolean> existsByIdAndTenant(UUID id, TenantId tenantId);
}
Fichier 31 : domain/reward/port/out/RewardGrantRepository.java
javapublic interface RewardGrantRepository {
    Mono<RewardGrant> save(RewardGrant grant);
    Mono<RewardGrant> findById(UUID id);
    Mono<RewardGrant> findByIdAndTenant(UUID id, TenantId tenantId);
    Mono<RewardGrant> findByIdempotencyKey(String key);
    Flux<RewardGrant> findActiveByMember(UserId memberId, TenantId tenantId);
    Flux<RewardGrant> findAllByMember(UserId memberId, TenantId tenantId, int page, int size);
    Flux<RewardGrant> findExpiredActive(Instant before);  // Pour le job d'expiration
}
Fichier 32 : domain/reward/port/out/RewardEventPublisherPort.java
javapublic interface RewardEventPublisherPort {
    Mono<Void> publish(RewardDomainEvent event);
}

Phase 4 — Services du domaine
Fichier 33 : domain/reward/service/RewardCatalogService.java
Classe Java pure, zéro annotation Spring. Implémente CreateRewardUseCase, UpdateRewardUseCase, GetRewardCatalogUseCase.
Constructeur : RewardCatalogService(RewardRepository rewardRepo, RewardEventPublisherPort eventPublisher).
Implémentation de createReward : génère un UUID, appelle Reward.create(...), sauvegarde via rewardRepo, publie un événement RewardCreatedEvent (à créer dans domain/reward/event/), retourne le reward.
Implémentation de updateReward : charge le reward via findByIdAndTenant (vérifie l'appartenance au tenant), appelle reward.update(...), sauvegarde, retourne.
Implémentation de activateReward : charge, appelle reward.activate(), sauvegarde. Invalide le cache catalogue du tenant (via un RewardCachePort — voir plus bas).
Implémentation de pauseReward et archiveReward : même pattern.
Implémentation de getCatalog et getReward** : délèguent au repository.
Fichier 34 : domain/reward/service/GrantRewardService.java
Classe Java pure. Implémente GrantRewardUseCase.
Constructeur : GrantRewardService(RewardRepository rewardRepo, RewardGrantRepository grantRepo, RewardEventPublisherPort eventPublisher).
Implémentation de grantReward : charge le Reward via findByIdAndTenant. Vérifie qu'il est isAvailableAt(Instant.now()) — sinon lance RewardDomainException("La récompense n'est pas disponible"). Appelle reward.decrementStock() pour décrémenter le stock atomiquement. Crée le RewardGrant via RewardGrant.create(...) en statut PENDING. Sauvegarde le reward mis à jour (stock décrémenté). Sauvegarde le grant. Appelle grant.activate() pour passer le grant en ACTIVE. Sauvegarde le grant activé. Publie un RewardGrantedEvent. Retourne le grant.
Gestion de la concurrence : la décrémentation du stock est une opération critique. Dans l'adapter de persistance, decrementStock() utilise un UPDATE rewards SET stock_remaining = stock_remaining - 1, version = version + 1 WHERE id = :id AND version = :version AND stock_remaining > 0 RETURNING *. Si aucune ligne n'est affectée, le stock est épuisé ou modifié concurrentiellement — lance RewardDomainException("Stock épuisé").
Fichier 35 : domain/reward/service/RedemptionService.java
Classe Java pure. Implémente RedeemRewardUseCase. C'est le service le plus critique — il débite les points et crée le grant de façon atomique.
Constructeur : RedemptionService(RewardRepository rewardRepo, RewardGrantRepository grantRepo, PointsAccountRepository pointsRepo, PointsTransactionRepository pointsTxRepo, RewardEventPublisherPort eventPublisher).
Implémentation de redeem(RedemptionRequest request) :
Étape 1 — Charge le Reward via findByIdAndTenant. Vérifie isAvailableAt(now). Vérifie isRedeemableWithPoints() — sinon RewardDomainException("Cette récompense n'est pas échangeable contre des points").
Étape 2 — Charge le PointsAccount du membre. S'il n'existe pas, lance InsufficientPointsException(reward.costInPoints(), 0).
Étape 3 — Vérifie pointsAccount.hasEnoughPoints(reward.costInPoints()) — sinon InsufficientPointsException(reward.costInPoints(), pointsAccount.availablePoints()).
Étape 4 — Appelle reward.decrementStock().
Étape 5 — Appelle pointsAccount.spend(reward.costInPoints()) — retourne le compte mis à jour.
Étape 6 — Crée une PointsTransaction de type DEBIT avec source REDEMPTION.
Étape 7 — Crée un RewardGrant via RewardGrant.create(...) avec source POINTS_REDEMPTION.
Étape 8 — Sauvegarde dans l'ordre : reward mis à jour, pointsAccount mis à jour, pointsTransaction, grant.
Étape 9 — Appelle grant.activate(), sauvegarde le grant activé.
Étape 10 — Publie RewardGrantedEvent.
Étape 11 — Retourne RedemptionResult(grant, costInPoints, newBalance).
Note sur l'atomicité : tout le bloc de sauvegarde (étapes 8-9) doit être dans une transaction PostgreSQL unique. Dans Spring WebFlux R2DBC, cela se fait avec @Transactional sur le handler applicatif, pas sur le service domaine. Le service domaine prépare tous les objets à sauvegarder, le handler les sauvegarde dans une transaction.
Fichier 36 : domain/reward/service/ConsumeGrantService.java
Classe Java pure. Implémente ConsumeGrantUseCase.
Constructeur : ConsumeGrantService(RewardGrantRepository grantRepo, RewardEventPublisherPort eventPublisher, CreditWalletUseCase creditWalletUseCase).
Implémentation de consumeGrant(ConsumeGrantRequest request) :
Étape 1 — Charge le RewardGrant via findByIdAndTenant. Si absent, lance GrantNotFoundException.
Étape 2 — Vérifie que grant.memberId() correspond au request.memberId() — protection cross-member.
Étape 3 — Si grant.isExpired(), appelle grant.expire(), sauvegarde, lance GrantExpiredException.
Étape 4 — Vérifie grant.status().isUsable() — sinon lance GrantAlreadyUsedException si USED, ou l'exception appropriée selon le statut.
Étape 5 — Construit le contexte de consommation : {order_reference: ..., order_amount: ..., consumed_at: now}.
Étape 6 — Appelle grant.consume(context) — décrémente remainingApplications, met à jour le statut.
Étape 7 — Calcule la réduction : appelle grant.rewardValue().calculateDiscount(request.orderAmount()). Vérifie que la réduction ne dépasse pas le montant de commande.
Étape 8 — Si le type est CASHBACK_WALLET, appelle creditWalletUseCase.credit(tenantId, memberId, rewardValue.numericValue(), LOYALTY_REWARD, grantId, idempotencyKey).
Étape 9 — Sauvegarde le grant mis à jour.
Étape 10 — Publie RewardConsumedEvent.
Étape 11 — Retourne ConsumeGrantResult(updatedGrant, discountApplied, finalAmount, grant.status() == USED).
Fichier 37 : domain/reward/service/GrantExpiryService.java
Classe Java pure. Gère l'expiration automatique des grants.
Constructeur : GrantExpiryService(RewardGrantRepository grantRepo, RewardEventPublisherPort eventPublisher).
Méthode expireOutdatedGrants(Instant now) retourne Mono<Integer> (nombre de grants expirés). Charge via grantRepo.findExpiredActive(now). Pour chaque grant, appelle grant.expire(), sauvegarde, publie RewardGrantExpiredEvent. Retourne le count.

Phase 5 — Handlers applicatifs
Fichier 38 : application/reward/handler/CreateRewardHandler.java
java@Service
public class CreateRewardHandler implements CreateRewardUseCase {

    private final RewardCatalogService catalogService;
    private final IdempotencyPort idempotency;
    private final RewardCachePort rewardCache;

    // Constructeur par injection

    @Override
    public Mono<Reward> createReward(TenantId tenantId, String name, ...,
                                     String idempotencyKey) {
        return idempotency.checkAndMark(idempotencyKey)
            .flatMap(existing -> existing
                ? idempotency.getResponse(idempotencyKey, Reward.class)
                : catalogService.createReward(tenantId, name, ...)
                    .flatMap(reward ->
                        idempotency.storeResponse(idempotencyKey, reward)
                            .thenReturn(reward))
            );
    }
}
Fichier 39 : application/reward/handler/GrantRewardHandler.java
java@Service
public class GrantRewardHandler implements GrantRewardUseCase {

    private final GrantRewardService grantService;
    private final IdempotencyPort idempotency;

    @Override
    @Transactional
    public Mono<RewardGrant> grantReward(TenantId tenantId, UserId memberId,
                                          UUID rewardId, GrantSource source,
                                          UUID sourceRuleId, String sourceEventKey,
                                          String idempotencyKey) {
        return idempotency.checkAndMark(idempotencyKey)
            .flatMap(existing -> existing
                ? idempotency.getResponse(idempotencyKey, RewardGrant.class)
                : grantService.grantReward(tenantId, memberId, rewardId,
                                           source, sourceRuleId, sourceEventKey)
                    .flatMap(grant ->
                        idempotency.storeResponse(idempotencyKey, grant)
                            .thenReturn(grant))
            );
    }
}
Fichier 40 : application/reward/handler/RedeemRewardHandler.java
java@Service
public class RedeemRewardHandler implements RedeemRewardUseCase {

    private final RedemptionService redemptionService;
    private final IdempotencyPort idempotency;

    @Override
    @Transactional  // CRITIQUE : points débit + grant création = une seule transaction
    public Mono<RedemptionResult> redeem(RedemptionRequest request) {
        return idempotency.checkAndMark(request.idempotencyKey())
            .flatMap(existing -> existing
                ? idempotency.getResponse(request.idempotencyKey(),
                                          RedemptionResult.class)
                : redemptionService.redeem(request)
                    .flatMap(result ->
                        idempotency.storeResponse(request.idempotencyKey(), result)
                            .thenReturn(result))
            );
    }
}
Fichier 41 : application/reward/handler/ConsumeGrantHandler.java
java@Service
public class ConsumeGrantHandler implements ConsumeGrantUseCase {

    private final ConsumeGrantService consumeService;
    private final IdempotencyPort idempotency;

    @Override
    @Transactional
    public Mono<ConsumeGrantResult> consumeGrant(ConsumeGrantRequest request) {
        return idempotency.checkAndMark(request.idempotencyKey())
            .flatMap(existing -> existing
                ? idempotency.getResponse(request.idempotencyKey(),
                                          ConsumeGrantResult.class)
                : consumeService.consumeGrant(request)
                    .flatMap(result ->
                        idempotency.storeResponse(request.idempotencyKey(), result)
                            .thenReturn(result))
            );
    }
}
Fichier 42 : application/reward/handler/GrantExpirySchedulerHandler.java
java@Component
public class GrantExpirySchedulerHandler {

    private final GrantExpiryService expiryService;

    // Tourne chaque nuit à 01h00
    @Scheduled(cron = "0 0 1 * * *")
    public void runExpiry() {
        expiryService.expireOutdatedGrants(Instant.now())
            .doOnNext(count -> log.info("[EXPIRY] {} grants expirés", count))
            .doOnError(e -> log.error("[EXPIRY] Erreur lors de l'expiration", e))
            .subscribe();
    }
}

Phase 6 — Consommateur Kafka
Fichier 43 : infrastructure/kafka/consumer/GrantRewardRequestConsumer.java
java@Component
public class GrantRewardRequestConsumer {

    private final GrantRewardUseCase grantRewardUseCase;
    private final ObjectMapper objectMapper;

    // Écoute le topic loyalty.rewards.grant-requests
    // Ce topic est publié par LoyaltyDomainService quand un effet GRANT_REWARD se déclenche

    @Bean
    public Consumer<Flux<ConsumerRecord<String, String>>> grantRewardRequestConsumer() {
        return flux -> flux
            .flatMap(record -> processRecord(record))
            .doOnError(e -> log.error("Erreur consommation grant-request", e))
            .subscribe();
    }

    private Mono<Void> processRecord(ConsumerRecord<String, String> record) {
        return Mono.fromCallable(() ->
                objectMapper.readValue(record.value(), GrantRewardRequestedEvent.class))
            .flatMap(event -> grantRewardUseCase.grantReward(
                event.tenantId(),
                event.memberId(),
                UUID.fromString(event.rewardId()),
                GrantSource.RULE_ENGINE,
                event.sourceRuleId(),
                event.sourceEventIdempotencyKey(),
                "grant-" + event.eventId()  // Idempotency key construite depuis l'eventId
            ))
            .doOnSuccess(grant ->
                log.info("Grant créé : {} pour membre {}",
                         grant.id(), grant.memberId()))
            .onErrorResume(e -> {
                log.error("Échec création grant pour event {} : {}",
                          record.offset(), e.getMessage());
                // Ne pas propager l'erreur pour éviter le rebalancing Kafka
                // L'événement sera dans le DLQ après maxRetries
                return Mono.empty();
            })
            .then();
    }
}

Phase 7 — Persistance R2DBC
Fichier 44 : infrastructure/persistence/reward/entity/RewardEntity.java
java@Table("rewards")
public class RewardEntity {
    @Id UUID id;
    @Column("tenant_id") UUID tenantId;
    String name;
    String description;
    String type;              // RewardType.name()
    String valueJson;         // RewardValue sérialisé en JSON
    @Column("cost_in_points") long costInPoints;
    @Column("stock_total") Integer stockTotal;
    @Column("stock_remaining") Integer stockRemaining;
    @Column("valid_from") Instant validFrom;
    @Column("valid_until") Instant validUntil;
    @Column("grant_expiry_days") int grantExpiryDays;
    @Column("image_url") String imageUrl;
    @Column("metadata") String metadataJson;
    String status;
    int version;
    @Column("created_at") Instant createdAt;
    @Column("updated_at") Instant updatedAt;
}
Fichier 45 : infrastructure/persistence/reward/entity/RewardGrantEntity.java
java@Table("reward_grants")
public class RewardGrantEntity {
    @Id UUID id;
    @Column("tenant_id") UUID tenantId;
    @Column("member_id") UUID memberId;
    @Column("reward_id") UUID rewardId;
    @Column("reward_name") String rewardName;
    @Column("reward_type") String rewardType;
    @Column("reward_value_json") String rewardValueJson;
    String source;
    @Column("source_rule_id") UUID sourceRuleId;
    @Column("source_event_id") String sourceEventId;
    String status;
    @Column("remaining_applications") int remainingApplications;
    @Column("granted_at") Instant grantedAt;
    @Column("expires_at") Instant expiresAt;
    @Column("used_at") Instant usedAt;
    @Column("used_in_context") String usedInContext;
    @Column("idempotency_key") String idempotencyKey;
    int version;
}
Fichier 46 : infrastructure/persistence/reward/repository/RewardR2dbcRepository.java
javapublic interface RewardR2dbcRepository
    extends ReactiveCrudRepository<RewardEntity, UUID> {

    Flux<RewardEntity> findByTenantIdAndStatus(UUID tenantId, String status,
                                               Pageable pageable);
    Flux<RewardEntity> findByTenantId(UUID tenantId, Pageable pageable);
    Mono<RewardEntity> findByIdAndTenantId(UUID id, UUID tenantId);

    // Décrémentation atomique du stock avec optimistic locking
    @Modifying
    @Query("""
        UPDATE rewards
        SET stock_remaining = stock_remaining - 1,
            status = CASE WHEN stock_remaining - 1 = 0 THEN 'EXHAUSTED' ELSE status END,
            version = version + 1,
            updated_at = NOW()
        WHERE id = :id
          AND tenant_id = :tenantId
          AND version = :version
          AND (stock_remaining IS NULL OR stock_remaining > 0)
        """)
    Mono<Integer> decrementStockAtomically(UUID id, UUID tenantId, int version);
}
Fichier 47 : infrastructure/persistence/reward/repository/RewardGrantR2dbcRepository.java
javapublic interface RewardGrantR2dbcRepository
    extends ReactiveCrudRepository<RewardGrantEntity, UUID> {

    Mono<RewardGrantEntity> findByIdAndTenantId(UUID id, UUID tenantId);
    Mono<RewardGrantEntity> findByIdempotencyKey(String key);

    Flux<RewardGrantEntity> findByMemberIdAndTenantIdAndStatus(
        UUID memberId, UUID tenantId, String status);

    Flux<RewardGrantEntity> findByMemberIdAndTenantId(
        UUID memberId, UUID tenantId, Pageable pageable);

    // Grants expirés qui sont encore ACTIVE
    @Query("""
        SELECT * FROM reward_grants
        WHERE status = 'ACTIVE'
          AND expires_at IS NOT NULL
          AND expires_at < :now
        LIMIT 500
        """)
    Flux<RewardGrantEntity> findExpiredActiveGrants(Instant now);
}
Fichier 48 : infrastructure/persistence/reward/mapper/RewardMapper.java
Interface MapStruct @Mapper(componentModel = "spring", uses = {JacksonMapperHelper.class}).
Méthode Reward toDomain(RewardEntity entity). La conversion du valueJson String en RewardValue utilise une méthode default avec ObjectMapper. La conversion du metadataJson en Map<String, Object> idem.
Méthode RewardEntity toEntity(Reward domain).
Fichier 49 : infrastructure/persistence/reward/mapper/RewardGrantMapper.java
Interface MapStruct similaire. La conversion rewardValueJson ↔ RewardValue utilise Jackson.
Fichier 50 : infrastructure/persistence/reward/adapter/RewardRepositoryAdapter.java
java@Component
public class RewardRepositoryAdapter implements RewardRepository {

    private final RewardR2dbcRepository r2dbcRepo;
    private final RewardMapper mapper;

    @Override
    public Mono<Reward> save(Reward reward) {
        return r2dbcRepo.save(mapper.toEntity(reward)).map(mapper::toDomain);
    }

    @Override
    public Mono<Reward> findByIdAndTenant(UUID id, TenantId tenantId) {
        return r2dbcRepo.findByIdAndTenantId(id, tenantId.value())
            .map(mapper::toDomain)
            .switchIfEmpty(Mono.error(new RewardNotFoundException(id)));
    }

    // Méthode spéciale pour la décrémentation atomique
    public Mono<Void> decrementStockAtomically(UUID id, TenantId tenantId, int version) {
        return r2dbcRepo.decrementStockAtomically(id, tenantId.value(), version)
            .flatMap(rowsAffected -> rowsAffected == 0
                ? Mono.error(new RewardDomainException("Stock épuisé ou version conflict"))
                : Mono.empty());
    }

    // Autres méthodes déléguant au r2dbcRepo...
}
Fichier 51 : infrastructure/persistence/reward/adapter/RewardGrantRepositoryAdapter.java
Même pattern que RewardRepositoryAdapter. Implémente RewardGrantRepository. Méthode findByIdAndTenant lance GrantNotFoundException si absent.

Phase 8 — Cache Redis
Fichier 52 : domain/reward/port/out/RewardCachePort.java
javapublic interface RewardCachePort {
    Mono<List<Reward>> getCachedCatalog(TenantId tenantId);
    Mono<Void> cacheCatalog(TenantId tenantId, List<Reward> rewards, Duration ttl);
    Mono<Void> evictCatalog(TenantId tenantId);
    Mono<Optional<Reward>> getCachedReward(TenantId tenantId, UUID rewardId);
    Mono<Void> cacheReward(TenantId tenantId, Reward reward, Duration ttl);
    Mono<Void> evictReward(TenantId tenantId, UUID rewardId);
}
Fichier 53 : infrastructure/redis/adapter/RewardCacheAdapter.java
java@Component
public class RewardCacheAdapter implements RewardCachePort {

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    // Clés Redis :
    // loyalty:{tenantId}:rewards:catalog         → liste JSON du catalogue actif
    // loyalty:{tenantId}:reward:{rewardId}        → JSON d'une récompense individuelle
    // TTL catalogue : 10 minutes
    // TTL récompense individuelle : 30 minutes
    // Invalidation : sur activate, pause, archive, decrementStock

    @Override
    public Mono<List<Reward>> getCachedCatalog(TenantId tenantId) {
        String key = "loyalty:" + tenantId.value() + ":rewards:catalog";
        return redisTemplate.opsForValue().get(key)
            .flatMap(json -> Mono.fromCallable(() -> {
                TypeReference<List<Reward>> type = new TypeReference<>() {};
                return objectMapper.readValue(json, type);
            }))
            .onErrorResume(e -> Mono.empty()); // Cache miss silencieux
    }

    // Autres méthodes...
}
Ajoute dans RedisKeyBuilder.java :

rewardCatalogKey(TenantId tenantId) → "loyalty:" + tenantId.value() + ":rewards:catalog"
rewardKey(TenantId tenantId, UUID rewardId) → "loyalty:" + tenantId.value() + ":reward:" + rewardId


Phase 9 — Kafka Producer
Fichier 54 : infrastructure/kafka/producer/RewardEventProducer.java
java@Component
public class RewardEventProducer implements RewardEventPublisherPort {

    private final ReactiveKafkaProducerTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> publish(RewardDomainEvent event) {
        String topic = "loyalty.reward.events." + event.tenantId().value();
        String key = event.tenantId().value() + ":" +
                     (event instanceof RewardGrantedEvent g ? g.memberId().value() : "");

        return kafkaTemplate.send(topic, key, event)
            .doOnSuccess(result ->
                log.debug("Événement {} publié sur {}", event.eventType(), topic))
            .doOnError(e ->
                log.error("Échec publication {} : {}", event.eventType(), e.getMessage()))
            .onErrorResume(e -> Mono.empty()) // Ne pas faire échouer l'opération principale
            .then();
    }
}

Phase 10 — Contrôleurs REST WebFlux
Fichier 55 : api/reward/dto/request/CreateRewardRequest.java
javapublic record CreateRewardRequest(
    @NotBlank String name,
    String description,
    @NotNull String type,             // RewardType enum name
    @NotNull BigDecimal numericValue, // Valeur de la récompense
    @NotBlank String valueUnit,       // "PERCENT", "XAF", code produit...
    int maxApplicationCount,
    @Min(0) long costInPoints,        // 0 = attribué par règle uniquement
    Integer stockTotal,               // null = illimité
    Instant validFrom,
    Instant validUntil,
    int grantExpiryDays,
    String imageUrl,
    Map<String, Object> metadata
) {}
Fichier 56 : api/reward/dto/request/RedeemRewardRequest.java
javapublic record RedeemRewardRequest(
    @NotNull UUID rewardId
) {}
Fichier 57 : api/reward/dto/request/ConsumeGrantRequest.java
javapublic record ConsumeGrantRequestDto(
    @NotNull UUID grantId,
    @NotBlank String orderReference,
    @NotNull @Positive BigDecimal orderAmount
) {}
Fichier 58 : api/reward/dto/response/RewardResponse.java
javapublic record RewardResponse(
    UUID id,
    String name,
    String description,
    String type,
    BigDecimal numericValue,
    String valueUnit,
    int maxApplicationCount,
    long costInPoints,
    Integer stockRemaining,
    Instant validFrom,
    Instant validUntil,
    int grantExpiryDays,
    String imageUrl,
    String status,
    Instant createdAt
) {
    public static RewardResponse from(Reward reward) {
        return new RewardResponse(
            reward.id(),
            reward.name(),
            reward.description(),
            reward.type().name(),
            reward.value().numericValue(),
            reward.value().unit(),
            reward.value().maxApplicationCount(),
            reward.costInPoints(),
            reward.stockRemaining(),
            reward.validFrom(),
            reward.validUntil(),
            reward.grantExpiryDays(),
            reward.imageUrl(),
            reward.status().name(),
            reward.createdAt()
        );
    }
}
Fichier 59 : api/reward/dto/response/RewardGrantResponse.java
javapublic record RewardGrantResponse(
    UUID id,
    String rewardName,
    String rewardType,
    BigDecimal numericValue,
    String valueUnit,
    String status,
    int remainingApplications,
    Instant grantedAt,
    Instant expiresAt
) {
    public static RewardGrantResponse from(RewardGrant grant) {
        return new RewardGrantResponse(
            grant.id(),
            grant.rewardName(),
            grant.rewardType().name(),
            grant.rewardValue().numericValue(),
            grant.rewardValue().unit(),
            grant.status().name(),
            grant.remainingApplications(),
            grant.grantedAt(),
            grant.expiresAt()
        );
    }
}
Fichier 60 : api/reward/dto/response/RedemptionResponse.java
javapublic record RedemptionResponse(
    RewardGrantResponse grant,
    long pointsSpent,
    long remainingPointsBalance
) {
    public static RedemptionResponse from(RedemptionResult result) {
        return new RedemptionResponse(
            RewardGrantResponse.from(result.grant()),
            result.pointsSpent(),
            result.remainingPointsBalance()
        );
    }
}
Fichier 61 : api/reward/dto/response/ConsumeGrantResponse.java
javapublic record ConsumeGrantResponse(
    UUID grantId,
    String rewardType,
    BigDecimal discountApplied,
    BigDecimal finalOrderAmount,
    boolean fullyConsumed,
    String grantStatus
) {
    public static ConsumeGrantResponse from(ConsumeGrantResult result) {
        return new ConsumeGrantResponse(
            result.updatedGrant().id(),
            result.updatedGrant().rewardType().name(),
            result.discountApplied(),
            result.finalOrderAmount(),
            result.fullyConsumed(),
            result.updatedGrant().status().name()
        );
    }
}
Fichier 62 : api/reward/RewardCatalogController.java
RouterFunction WebFlux. Routes d'administration du catalogue.
POST   /api/v1/admin/rewards
       → Requiert ROLE_TENANT_ADMIN + Idempotency-Key
       → Valide CreateRewardRequest
       → Construit RewardValue depuis numericValue + valueUnit + maxApplicationCount
       → Appelle CreateRewardUseCase
       → Retourne RewardResponse avec 201

GET    /api/v1/admin/rewards
       → Query params : activeOnly (défaut true), page, size
       → Retourne Flux<RewardResponse>

GET    /api/v1/admin/rewards/{rewardId}
       → Retourne Mono<RewardResponse>
       → 404 si absent

PATCH  /api/v1/admin/rewards/{rewardId}/activate
       → Appelle UpdateRewardUseCase.activateReward()
       → Invalide le cache catalogue
       → Retourne RewardResponse

PATCH  /api/v1/admin/rewards/{rewardId}/pause
       → Appelle UpdateRewardUseCase.pauseReward()

PATCH  /api/v1/admin/rewards/{rewardId}/archive
       → Appelle UpdateRewardUseCase.archiveReward()

PUT    /api/v1/admin/rewards/{rewardId}
       → Modifie name, description, imageUrl, metadata uniquement
       → Retourne RewardResponse

GET    /api/v1/rewards
       → Route publique membre (JWT requis, ROLE_MEMBER)
       → Retourne le catalogue actif visible pour les membres
       → activeOnly=true, paginé
Fichier 63 : api/reward/RedemptionController.java
POST   /api/v1/members/me/redeem
       → Requiert JWT membre + Idempotency-Key
       → Lit memberId depuis TenantContextHolder
       → Valide RedeemRewardRequest
       → Construit RedemptionRequest
       → Appelle RedeemRewardUseCase
       → Retourne RedemptionResponse avec 201
       → 422 si points insuffisants (InsufficientPointsException)
       → 409 si stock épuisé

GET    /api/v1/members/me/grants
       → Retourne les grants ACTIVE du membre courant
       → Flux<RewardGrantResponse>

GET    /api/v1/members/me/grants/history
       → Retourne tous les grants (tous statuts), paginé
       → Query params : page, size

GET    /api/v1/members/me/grants/{grantId}
       → Retourne un grant spécifique
       → 404 si absent ou n'appartient pas au membre
Fichier 64 : api/reward/ConsumeGrantController.java
POST   /api/v1/reward-grants/{grantId}/consume
       → Route appelée par la PLATEFORME CLIENTE au checkout
       → Requiert JWT plateforme (ROLE_TENANT_ADMIN ou ROLE_PLATFORM)
       → Requiert Idempotency-Key
       → Valide ConsumeGrantRequestDto
       → Extrait memberId du body (la plateforme connaît son membre)
       → Construit ConsumeGrantRequest
       → Appelle ConsumeGrantUseCase
       → Retourne ConsumeGrantResponse avec 200
       → 404 si grant absent
       → 422 si grant déjà utilisé (GrantAlreadyUsedException)
       → 410 si grant expiré (GrantExpiredException) — HTTP 410 Gone
       → 409 si conflit de version (optimistic lock)

GET    /api/v1/reward-grants/{grantId}/validate
       → Route de validation AVANT consommation
       → Retourne le grant avec son statut actuel
       → Utile pour la plateforme cliente avant de finaliser le checkout
       → Ne modifie rien

Phase 11 — Migrations Liquibase
Fichier 65 : db/changelog/migrations/V004__create_reward_tables.sql
sql-- ============================================================
-- Rewards catalogue
-- ============================================================
CREATE TABLE IF NOT EXISTS rewards (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL,
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    type                VARCHAR(100) NOT NULL,
    value_json          JSONB NOT NULL,
    cost_in_points      BIGINT NOT NULL DEFAULT 0,
    stock_total         INT,
    stock_remaining     INT,
    valid_from          TIMESTAMPTZ,
    valid_until         TIMESTAMPTZ,
    grant_expiry_days   INT NOT NULL DEFAULT 0,
    image_url           VARCHAR(500),
    metadata            JSONB NOT NULL DEFAULT '{}',
    status              VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    version             INT NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_stock CHECK (
        (stock_total IS NULL AND stock_remaining IS NULL)
        OR (stock_total IS NOT NULL AND stock_remaining IS NOT NULL
            AND stock_remaining >= 0 AND stock_remaining <= stock_total)
    ),
    CONSTRAINT chk_cost CHECK (cost_in_points >= 0)
);

CREATE INDEX IF NOT EXISTS idx_rewards_tenant_status
    ON rewards(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_rewards_tenant_active
    ON rewards(tenant_id)
    WHERE status = 'ACTIVE';

-- ============================================================
-- Reward grants (attributions individuelles)
-- ============================================================
CREATE TABLE IF NOT EXISTS reward_grants (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id               UUID NOT NULL,
    member_id               UUID NOT NULL,
    reward_id               UUID NOT NULL REFERENCES rewards(id),
    reward_name             VARCHAR(255) NOT NULL,
    reward_type             VARCHAR(100) NOT NULL,
    reward_value_json       JSONB NOT NULL,
    source                  VARCHAR(100) NOT NULL,
    source_rule_id          UUID,
    source_event_id         VARCHAR(255),
    status                  VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    remaining_applications  INT NOT NULL DEFAULT 1,
    granted_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    expires_at              TIMESTAMPTZ,
    used_at                 TIMESTAMPTZ,
    used_in_context         JSONB,
    idempotency_key         VARCHAR(255) UNIQUE,
    version                 INT NOT NULL DEFAULT 0,
    CONSTRAINT chk_remaining CHECK (remaining_applications >= 0)
);

CREATE INDEX IF NOT EXISTS idx_grants_member_tenant
    ON reward_grants(member_id, tenant_id);
CREATE INDEX IF NOT EXISTS idx_grants_member_status
    ON reward_grants(member_id, tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_grants_expires
    ON reward_grants(expires_at)
    WHERE status = 'ACTIVE' AND expires_at IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_grants_idempotency
    ON reward_grants(idempotency_key)
    WHERE idempotency_key IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_grants_reward
    ON reward_grants(reward_id);
Mettre à jour db.changelog-master.xml
xml<include file="migrations/V004__create_reward_tables.sql"
         relativeToChangelogFile="true"/>

Phase 12 — Ajout des ErrorCodes
Dans shared/exception/ErrorCode.java, ajoute à la fin sans toucher aux existants :
java// Module Rewards
REWARD_NOT_FOUND(404),
REWARD_NOT_AVAILABLE(422),
REWARD_STOCK_EXHAUSTED(409),
REWARD_NOT_REDEEMABLE(422),
GRANT_NOT_FOUND(404),
GRANT_ALREADY_USED(409),
GRANT_EXPIRED(410),
GRANT_CANCELLED(422),
GRANT_NOT_OWNED_BY_MEMBER(403),
INSUFFICIENT_POINTS_FOR_REDEMPTION(422),
REWARD_CATALOG_EMPTY(404);
Crée les exceptions concrètes correspondantes dans shared/exception/ pour REWARD_NOT_FOUND, GRANT_NOT_FOUND, GRANT_ALREADY_USED, GRANT_EXPIRED — chacune héritant de AppException et passant le bon ErrorCode.

Phase 13 — Configuration Spring
Fichier 66 : infrastructure/config/RewardConfig.java
java@Configuration
public class RewardConfig {

    @Bean
    public RewardCatalogService rewardCatalogService(
            RewardRepository rewardRepo,
            RewardEventPublisherPort eventPublisher,
            RewardCachePort rewardCache) {
        return new RewardCatalogService(rewardRepo, eventPublisher, rewardCache);
    }

    @Bean
    public GrantRewardService grantRewardService(
            RewardRepository rewardRepo,
            RewardGrantRepository grantRepo,
            RewardEventPublisherPort eventPublisher) {
        return new GrantRewardService(rewardRepo, grantRepo, eventPublisher);
    }

    @Bean
    public RedemptionService redemptionService(
            RewardRepository rewardRepo,
            RewardGrantRepository grantRepo,
            PointsAccountRepository pointsRepo,
            PointsTransactionRepository pointsTxRepo,
            RewardEventPublisherPort eventPublisher) {
        return new RedemptionService(rewardRepo, grantRepo, pointsRepo,
                                     pointsTxRepo, eventPublisher);
    }

    @Bean
    public ConsumeGrantService consumeGrantService(
            RewardGrantRepository grantRepo,
            RewardEventPublisherPort eventPublisher,
            CreditWalletUseCase creditWalletUseCase) {
        return new ConsumeGrantService(grantRepo, eventPublisher, creditWalletUseCase);
    }

    @Bean
    public GrantExpiryService grantExpiryService(
            RewardGrantRepository grantRepo,
            RewardEventPublisherPort eventPublisher) {
        return new GrantExpiryService(grantRepo, eventPublisher);
    }
}

Phase 14 — Tests
Fichier 67 : test/domain/reward/RewardTest.java
Test JUnit 5 sans Spring, zéro Testcontainers.
Test 1 : create() avec stockTotal=10 crée reward DRAFT avec stockRemaining=10
Test 2 : activate() depuis DRAFT → ACTIVE
Test 3 : activate() depuis ARCHIVED → lance RewardDomainException
Test 4 : decrementStock() depuis stockRemaining=1 → stockRemaining=0, status=EXHAUSTED
Test 5 : decrementStock() depuis stockRemaining=0 → lance RewardDomainException
Test 6 : decrementStock() avec stock null (illimité) → ne change rien
Test 7 : isAvailableAt(now) avec reward ACTIVE sans dates → true
Test 8 : isAvailableAt(now) avec validUntil passé → false
Test 9 : restoreStock(5) depuis EXHAUSTED → stockRemaining=5, status=ACTIVE
Fichier 68 : test/domain/reward/RewardGrantTest.java
Test 1 : create() depuis reward ACTIVE → grant PENDING avec snapshot correct
Test 2 : activate() → statut ACTIVE
Test 3 : consume() depuis ACTIVE avec remainingApplications=1
         → statut USED, remainingApplications=0
Test 4 : consume() depuis USED → lance RewardDomainException
Test 5 : consume() avec grant expiré → lance GrantExpiredException
Test 6 : create() avec grantExpiryDays=7 → expiresAt = now + 7 jours
Test 7 : consume() grant multi-utilisation (maxApplicationCount=2)
         → après 1ère utilisation: ACTIVE, remainingApplications=1
         → après 2ème utilisation: USED, remainingApplications=0
Test 8 : reverse() depuis ACTIVE → REVERSED
Test 9 : cancel() depuis PENDING → CANCELLED
Fichier 69 : test/domain/reward/RewardValueTest.java
Test 1 : calculateDiscount(5000, PERCENT 20%) → 1000
Test 2 : calculateDiscount(5000, FIXED 2000 XAF) → 2000
Test 3 : create() avec numericValue négative → IllegalArgumentException
Test 4 : create() avec unit vide → IllegalArgumentException
Fichier 70 : test/domain/reward/RedemptionServiceTest.java
Test JUnit 5 sans Spring. Utilise des fakes en mémoire.
Setup : reward ACTIVE avec costInPoints=500, stockRemaining=10
        pointsAccount avec availablePoints=1000

Test 1 : redeem() réussit
         → grant créé ACTIVE, points débités, stockRemaining=9,
           pointsBalance=500

Test 2 : redeem() avec points insuffisants
         → lance InsufficientPointsException

Test 3 : redeem() avec reward PAUSED
         → lance RewardDomainException("La récompense n'est pas disponible")

Test 4 : redeem() avec costInPoints=0 (attribué par règle uniquement)
         → lance RewardDomainException("non échangeable contre des points")

Test 5 : redeem() avec stock épuisé (stockRemaining=0)
         → lance RewardDomainException("Stock épuisé")
Fichier 71 : test/domain/reward/ConsumeGrantServiceTest.java
Setup : grant ACTIVE pour memberId="member-1", rewardType=PERCENT_DISCOUNT 20%

Test 1 : consumeGrant() réussit
         → grant USED, discountApplied = 20% de orderAmount,
           fullyConsumed = true

Test 2 : consumeGrant() par memberId="member-2" (mauvais membre)
         → lance RewardDomainException("Grant n'appartient pas à ce membre")

Test 3 : consumeGrant() grant USED
         → lance GrantAlreadyUsedException

Test 4 : consumeGrant() grant expiré (expiresAt dans le passé)
         → lance GrantExpiredException

Test 5 : consumeGrant() avec CASHBACK_WALLET
         → vérifie que creditWalletUseCase.credit() est appelé
Fichier 72 : test/integration/reward/RewardControllerIntegrationTest.java
Test @SpringBootTest(webEnvironment = RANDOM_PORT), @ActiveProfiles("stub"), Testcontainers PostgreSQL + Redis.
@BeforeEach : rien à insérer, on part d'une base vide

Test 1 — Création et activation d'une récompense
  POST /api/v1/admin/rewards
    body: {name: "Ticket Gratuit", type: "FREE_PRODUCT",
           numericValue: 1, valueUnit: "TICKET_PREMIUM",
           costInPoints: 500, stockTotal: 100, grantExpiryDays: 30}
  → 201, status: "DRAFT"

  PATCH /api/v1/admin/rewards/{id}/activate
  → 200, status: "ACTIVE"

Test 2 — Échange de points contre récompense
  (Prérequis: insérer en base un PointsAccount avec 1000 points)

  POST /api/v1/members/me/redeem
    header: Idempotency-Key: redeem-001
    body: {rewardId: "{id du test 1}"}
  → 201, grant.status: "ACTIVE", pointsSpent: 500, remainingBalance: 500

  Même requête avec Idempotency-Key: redeem-001
  → 201, réponse identique (idempotence vérifiée)

Test 3 — Points insuffisants
  (Prérequis: membre avec 100 points seulement)
  POST /api/v1/members/me/redeem
  → 422, errorCode: INSUFFICIENT_POINTS_FOR_REDEMPTION

Test 4 — Consommation au checkout
  POST /api/v1/reward-grants/{grantId}/consume
    header: Idempotency-Key: consume-001
    body: {grantId: "{grantId}", orderReference: "ORD-001",
           orderAmount: 10000, memberId: "member-1"}
  → 200, discountApplied: 2000 (20% de 10000), fullyConsumed: true

  Même requête avec Idempotency-Key: consume-001
  → 200, réponse identique

Test 5 — Réutilisation d'un grant USED
  POST /api/v1/reward-grants/{grantId}/consume (nouveau Idempotency-Key)
  → 409, errorCode: GRANT_ALREADY_USED

Test 6 — Validation avant consommation
  GET /api/v1/reward-grants/{grantId}/validate
  → 200, status: "USED"

Test 7 — Attribution par le moteur de règles via Kafka
  Publier manuellement un GrantRewardRequestedEvent sur le topic
  Attendre 2 secondes
  GET /api/v1/members/me/grants
  → Le grant apparaît avec status ACTIVE

Ordre d'implémentation recommandé
Jour 1 — Phase 1 complète (modèles du domaine). mvn compile doit passer. Exécute RewardTest, RewardGrantTest, RewardValueTest — doivent passer sans Spring.
Jour 2 — Phase 2 (événements) + Phase 3 (ports) + Phase 4 (services domaine). Exécute RedemptionServiceTest et ConsumeGrantServiceTest avec fakes — doivent passer sans Spring.
Jour 3 — Phase 11 (migration SQL) + Phase 7 (entités R2DBC et repositories). L'application démarre avec les nouvelles tables.
Jour 4 — Phase 8 (cache Redis) + Phase 9 (Kafka producer) + Phase 6 (Kafka consumer) + Phase 12 (ErrorCodes). Infrastructure complète.
Jour 5 — Phase 5 (handlers applicatifs) + Phase 13 (configuration Spring). Tous les beans connectés.
Jour 6 — Phase 10 (contrôleurs REST). Endpoints exposés.
Jour 7 — Phase 14 (tests d'intégration). Les 7 scénarios passent.

Vérifications finales
Vérification 1 — mvn test -Dtest=HexagonalArchitectureTest : aucune classe du domaine reward n'importe Spring, R2DBC, Kafka ou Redis.
Vérification 2 — mvn test -Dtest="RewardTest,RewardGrantTest,RewardValueTest,RedemptionServiceTest,ConsumeGrantServiceTest" : tous passent sans Spring, sans base.
Vérification 3 — mvn test -Dtest=RewardControllerIntegrationTest : les 7 scénarios passent.
Vérification 4 — mvn test : zéro régression sur Wallet et Loyalty.
Vérification 5 — Séquence manuelle Postman : créer une récompense → activer → envoyer 3 events → vérifier que le moteur de règles a déclenché un grant → consommer le grant au checkout → vérifier le solde de points et le statut du grant.
