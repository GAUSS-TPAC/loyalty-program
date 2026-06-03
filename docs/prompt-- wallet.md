Guide complet — Module Wallet
Contexte obligatoire à lire avant toute action
Tu travailles sur le backend du Loyalty Programme Royal, un SaaS multi-tenant de fidélisation. Le package racine est com.yowyob.loyalty. Le projet utilise Spring Boot 3.4, Spring WebFlux (réactif — .block() interdit partout), R2DBC pour PostgreSQL, Redis pour le cache et l'idempotence, Kafka pour les événements. L'architecture est hexagonale stricte avec DDD.
Les fondations sont déjà en place : multi-tenancy via JWT, TenantContextHolder, filtres de sécurité, stubs Kernel Core, migration wallet SQL de base. Tu construis par-dessus ces fondations — ne touche à rien dans shared/, infrastructure/kernelcore/, infrastructure/stub/ sauf si une modification est explicitement demandée dans ce guide.
Règle absolue : aucun fichier dans domain/ ne doit importer quoi que ce soit de Spring, R2DBC, Kafka, Redis, ou toute librairie externe. Seuls les imports java.*, reactor.core.publisher.* et les autres classes du domaine sont autorisés.

Ce que ce module doit accomplir
Le wallet est le portefeuille électronique d'un membre au sein d'un tenant. Il maintient un solde en monnaie virtuelle du tenant (ex: RidnCoins, YowCredits). Il peut être crédité automatiquement par le moteur de fidélité (récompenses, cashback), rechargé manuellement par le membre via Mobile Money ou Stripe, et débité lors d'un paiement sur la plateforme cliente. Les retraits vers un compte Mobile Money réel sont optionnels selon la configuration du tenant.
Chaque opération est atomique (le solde et la transaction sont créés en une seule opération PostgreSQL), idempotente (une même clé ne produit jamais deux effets), et traçable (chaque mouvement génère une WalletTransaction immuable).

Phase 1 — Modèles du domaine Wallet
Ces fichiers vont dans src/main/java/com/yowyob/loyalty/domain/wallet/. Zéro annotation Spring. Zéro import externe.
Fichier 1 : domain/wallet/model/WalletStatus.java
Enum Java pur représentant le cycle de vie d'un wallet.
Constantes : PENDING_KYC, ACTIVE, FROZEN, CLOSED.
Méthode canDebit() retourne this == ACTIVE. Méthode canCredit() retourne this == ACTIVE || this == PENDING_KYC. Méthode isFinal() retourne this == CLOSED. Méthode canTransitionTo(WalletStatus next) implémente les transitions autorisées : PENDING_KYC peut aller vers ACTIVE ou CLOSED. ACTIVE peut aller vers FROZEN ou CLOSED. FROZEN peut aller vers ACTIVE ou CLOSED. CLOSED ne peut aller nulle part — retourne toujours false.
Fichier 2 : domain/wallet/model/TransactionType.java
Enum : CREDIT, DEBIT, REVERSAL, RESERVE, RELEASE.
Méthode isCredit() retourne this == CREDIT || this == RELEASE. Méthode isDebit() retourne this == DEBIT || this == RESERVE.
Fichier 3 : domain/wallet/model/TransactionSource.java
Enum représentant l'origine d'une transaction. Constantes : TOPUP_MTN, TOPUP_ORANGE, TOPUP_STRIPE, LOYALTY_REWARD, REFERRAL_BONUS, CASHBACK, CAMPAIGN_BONUS, PURCHASE, WITHDRAWAL, MANUAL_ADJUSTMENT, REVERSAL.
Fichier 4 : domain/wallet/model/TransactionStatus.java
Enum : PENDING, COMPLETED, FAILED, REVERSED.
Méthode isFinal() retourne this == COMPLETED || this == FAILED || this == REVERSED.
Fichier 5 : domain/wallet/model/WalletPolicy.java
Record Java 21 immuable représentant les limites configurées par le tenant pour les wallets de ses membres. Ce record vit dans le domaine — il est passé en paramètre aux méthodes métier, jamais chargé directement depuis la base dans le domaine.
Champs : String currencyName, String currencySymbol, BigDecimal exchangeRate (taux de conversion depuis la devise réelle, ex: 1.0 pour un taux 1:1), BigDecimal dailySpendCap pouvant être null (null = pas de limite), BigDecimal maxBalance pouvant être null, BigDecimal maxTopupPerTransaction pouvant être null, BigDecimal minWithdrawal pouvant être null, int withdrawalDelayHours (défaut 24), BigDecimal otpThreshold pouvant être null (montant au-delà duquel un OTP est requis), boolean kycRequiredForWithdrawal (défaut true), Integer expiryDays pouvant être null.
Méthode statique defaults() retourne une WalletPolicy avec currencyName="Credits", currencySymbol="CR", exchangeRate=1.0, tous les caps null, withdrawalDelayHours=24, kycRequiredForWithdrawal=true, expiryDays=null.
Méthode validateCredit(BigDecimal amount, BigDecimal currentBalance) retourne Optional<String> avec le message d'erreur si la validation échoue, vide si OK. Vérifie que amount > 0, et si maxBalance non null que currentBalance + amount <= maxBalance, et si maxTopupPerTransaction non null que amount <= maxTopupPerTransaction.
Méthode validateDebit(BigDecimal amount, BigDecimal currentBalance, BigDecimal todaySpent) retourne Optional<String>. Vérifie amount > 0, currentBalance >= amount, et si dailySpendCap non null que todaySpent + amount <= dailySpendCap.
Méthode requiresOtp(BigDecimal amount) retourne boolean — true si otpThreshold non null et amount >= otpThreshold.
Fichier 6 : domain/wallet/model/Wallet.java
C'est l'agrégat racine du module wallet. Classe Java standard (pas un record car elle a des méthodes qui modifient l'état). Constructeur privé. Tous les champs sont privés et finals sauf balance, status, version, updatedAt qui peuvent être mis à jour.
Champs : UUID id, TenantId tenantId, UserId memberId, BigDecimal balance, String currencyCode, WalletStatus status, long version (optimistic locking), Instant frozenAt, String frozenReason, Instant closedAt, Instant createdAt, Instant updatedAt.
Méthode statique create(UUID id, TenantId tenantId, UserId memberId, String currencyCode, boolean autoActivate) crée un nouveau wallet. Si autoActivate est true, le status est ACTIVE, sinon PENDING_KYC. Retourne le Wallet créé.
Méthode credit(BigDecimal amount, WalletPolicy policy) retourne WalletCreditResult. Avant de modifier quoi que ce soit, appelle policy.validateCredit(amount, this.balance) — si un message d'erreur est présent, lance une WalletDomainException avec ce message. Vérifie que status.canCredit() — sinon lance WalletDomainException("Wallet ne peut pas recevoir de crédit dans l'état " + status). Calcule newBalance = balance + amount. Met à jour balance = newBalance, incrémente version, met à jour updatedAt. Retourne un WalletCreditResult avec le wallet mis à jour, le montant crédité, et le nouveau solde.
Méthode debit(BigDecimal amount, WalletPolicy policy, BigDecimal todaySpent) retourne WalletDebitResult. Vérifie policy.validateDebit(amount, balance, todaySpent) puis status.canDebit(). Met à jour le solde. Retourne WalletDebitResult.
Méthode freeze(String reason) retourne Wallet. Vérifie que la transition status → FROZEN est autorisée. Met à jour status, frozenAt, frozenReason, version, updatedAt.
Méthode unfreeze() retourne Wallet. Vérifie transition vers ACTIVE.
Méthode close() retourne Wallet. Vérifie transition vers CLOSED. Met à jour closedAt.
Méthode activate() retourne Wallet. Transition PENDING_KYC → ACTIVE.
Getters pour tous les champs.
Fichier 7 : domain/wallet/model/WalletCreditResult.java
Record : Wallet updatedWallet, BigDecimal amountCredited, BigDecimal newBalance.
Fichier 8 : domain/wallet/model/WalletDebitResult.java
Record : Wallet updatedWallet, BigDecimal amountDebited, BigDecimal newBalance, boolean otpRequired.
Fichier 9 : domain/wallet/model/WalletTransaction.java
Record immuable représentant une transaction. Une fois créée, elle ne peut jamais être modifiée.
Champs : UUID id, UUID walletId, TenantId tenantId, TransactionType type, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter, TransactionStatus status, TransactionSource source, String idempotencyKey, UUID referenceId (pouvant être null — lien vers une commande, un RewardGrant, etc.), UUID reversalOf (pouvant être null — lien vers la transaction annulée), Map<String, Object> metadata, Instant createdAt.
Méthode statique create(UUID walletId, TenantId tenantId, TransactionType type, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter, TransactionSource source, String idempotencyKey) — génère un UUID aléatoire pour id, status = COMPLETED, createdAt = now, metadata vide.
Méthode statique createPending(...) — identique mais status = PENDING. Utilisée pour les transactions en attente de confirmation Mobile Money.
Fichier 10 : domain/wallet/exception/WalletDomainException.java
Étend DomainException (la classe abstraite existante dans domain/shared/exception/). Constructeur WalletDomainException(String message). Constructeur WalletDomainException(String message, Map<String, Object> details).

Phase 2 — Ports du domaine Wallet
Ces interfaces vont dans domain/wallet/port/. Zéro annotation Spring.
Fichier 11 : domain/wallet/port/in/CreateWalletUseCase.java
Interface déclarant Mono<Wallet> createWallet(TenantId tenantId, UserId memberId, String currencyCode, boolean autoActivate, String idempotencyKey).
Fichier 12 : domain/wallet/port/in/CreditWalletUseCase.java
Interface déclarant Mono<WalletCreditResult> credit(TenantId tenantId, UserId memberId, BigDecimal amount, TransactionSource source, String referenceId, String idempotencyKey).
Fichier 13 : domain/wallet/port/in/DebitWalletUseCase.java
Interface déclarant Mono<WalletDebitResult> debit(TenantId tenantId, UserId memberId, BigDecimal amount, String description, String orderReference, String idempotencyKey).
Fichier 14 : domain/wallet/port/in/InitiateTopUpUseCase.java
Interface déclarant Mono<PaymentInitiationResult> initiateTopUp(TenantId tenantId, UserId memberId, BigDecimal amount, String provider, String idempotencyKey).
Fichier 15 : domain/wallet/port/in/InitiateWithdrawalUseCase.java
Interface déclarant Mono<PaymentInitiationResult> initiateWithdrawal(TenantId tenantId, UserId memberId, BigDecimal amount, String targetAccount, String provider, String idempotencyKey).
Fichier 16 : domain/wallet/port/in/ConfirmOtpUseCase.java
Interface déclarant Mono<WalletDebitResult> confirmOtp(TenantId tenantId, UserId memberId, String challengeId, String otpCode, String idempotencyKey).
Fichier 17 : domain/wallet/port/in/FreezeWalletUseCase.java
Interface déclarant Mono<Wallet> freeze(TenantId tenantId, UserId memberId, String reason, String actorId).
Fichier 18 : domain/wallet/port/in/UnfreezeWalletUseCase.java
Interface déclarant Mono<Wallet> unfreeze(TenantId tenantId, UserId memberId, String actorId).
Fichier 19 : domain/wallet/port/in/GetWalletUseCase.java
Interface déclarant Mono<Wallet> getWallet(TenantId tenantId, UserId memberId) et Mono<BigDecimal> getBalance(TenantId tenantId, UserId memberId).
Fichier 20 : domain/wallet/port/in/GetTransactionHistoryUseCase.java
Interface déclarant Flux<WalletTransaction> getHistory(TenantId tenantId, UserId memberId, TransactionType typeFilter, TransactionSource sourceFilter, Instant from, Instant to, int page, int size).
Fichier 21 : domain/wallet/port/in/ReverseTransactionUseCase.java
Interface déclarant Mono<WalletTransaction> reverse(TenantId tenantId, UUID transactionId, String reason, String actorId, String idempotencyKey).
Fichier 22 : domain/wallet/port/out/WalletRepository.java
Interface de persistance. Méthodes : Mono<Wallet> findByMemberAndTenant(UserId memberId, TenantId tenantId). Mono<Wallet> findById(UUID id). Mono<Wallet> save(Wallet wallet). Mono<Boolean> existsByMemberAndTenant(UserId memberId, TenantId tenantId).
Fichier 23 : domain/wallet/port/out/WalletTransactionRepository.java
Interface. Méthodes : Mono<WalletTransaction> save(WalletTransaction transaction). Mono<WalletTransaction> findByIdempotencyKey(String key). Flux<WalletTransaction> findByWalletId(UUID walletId, int page, int size). Flux<WalletTransaction> findByWalletIdAndFilters(UUID walletId, TransactionType type, TransactionSource source, Instant from, Instant to, int page, int size). Mono<BigDecimal> sumDebitsTodayForWallet(UUID walletId).
Fichier 24 : domain/wallet/port/out/PaymentRequestRepository.java
Interface. Méthodes : Mono<PaymentRequest> save(PaymentRequest request). Mono<PaymentRequest> findByExternalRef(String externalRef). Mono<PaymentRequest> findById(UUID id). Mono<PaymentRequest> update(PaymentRequest request).
On crée aussi domain/wallet/model/PaymentRequest.java — record immuable avec UUID id, UUID walletTransactionId nullable, String externalRef, String provider, String direction (INBOUND/OUTBOUND), BigDecimal realAmount, String realCurrency, BigDecimal virtualAmount, BigDecimal exchangeRate, PaymentStatus status, Instant initiatedAt, Instant confirmedAt nullable, Instant expiresAt nullable.
Fichier 25 : domain/wallet/port/out/WalletAuditLogRepository.java
Interface. Méthode Mono<Void> log(UUID walletId, String action, String actor, String reason, Map<String, Object> metadata).
Fichier 26 : domain/wallet/port/out/WalletPolicyRepository.java
Interface. Méthode Mono<WalletPolicy> findByTenant(TenantId tenantId). Retourne Mono.just(WalletPolicy.defaults()) si aucune politique n'est configurée — les adapters doivent respecter ce comportement.
Fichier 27 : domain/wallet/port/out/WalletEventPublisherPort.java
Interface. Méthode Mono<Void> publish(WalletDomainEvent event). L'adaptateur Kafka l'implémentera.
On crée aussi domain/wallet/event/WalletDomainEvent.java — interface étendant DomainEvent existant dans domain/shared/port/. Puis les records d'événements concrets : WalletCreditedEvent, WalletDebitedEvent, WalletFrozenEvent, WalletUnfrozenEvent, WalletCreatedEvent, tous dans domain/wallet/event/. Chaque record implémente WalletDomainEvent et les méthodes de DomainEvent.

Phase 3 — Service du domaine
Fichier 28 : domain/wallet/service/WalletDomainService.java
Classe Java pure, pas d'annotation Spring. Reçoit tous ses ports par constructeur. Implémente CreditWalletUseCase, DebitWalletUseCase, GetWalletUseCase, GetTransactionHistoryUseCase, FreezeWalletUseCase, UnfreezeWalletUseCase, CreateWalletUseCase, ReverseTransactionUseCase.
Constructeur : WalletDomainService(WalletRepository walletRepo, WalletTransactionRepository txRepo, WalletPolicyRepository policyRepo, WalletAuditLogRepository auditRepo, WalletEventPublisherPort eventPublisher).
Implémentation de credit : charge la WalletPolicy via policyRepo. Charge le wallet via walletRepo. Appelle wallet.credit(amount, policy) qui retourne WalletCreditResult. Crée une WalletTransaction via WalletTransaction.create(...). Sauvegarde le wallet mis à jour via walletRepo. Sauvegarde la transaction via txRepo. Publie un WalletCreditedEvent. Retourne le résultat. Tout en chaîne réactive Reactor.
Implémentation de debit : charge la policy. Charge le wallet. Calcule todaySpent via txRepo.sumDebitsTodayForWallet(walletId). Si wallet.debit(amount, policy, todaySpent) retourne otpRequired = true, ne pas débiter — retourner un WalletDebitResult avec otpRequired = true et le wallet inchangé. Sinon, effectue le débit, crée la transaction, sauvegarde, publie WalletDebitedEvent.
Implémentation de freeze : appelle wallet.freeze(reason), sauvegarde, logue via auditRepo, publie WalletFrozenEvent.
Implémentation de reverse : charge la transaction originale. Vérifie qu'elle n'est pas déjà reversée. Crée une transaction REVERSAL qui annule l'effet. Si c'était un débit, crédit le wallet du montant inverse. Si c'était un crédit, débite. Sauvegarde tout.

Phase 4 — Handlers applicatifs
Ces fichiers vont dans application/wallet/handler/. Annotés @Service. Ils orchestrent le cas d'utilisation complet avec idempotence.
Fichier 29 : application/wallet/handler/CreditWalletHandler.java
Annoté @Service. Implémente CreditWalletUseCase. Injecte WalletDomainService domainService et IdempotencyPort idempotency.
La méthode credit(...) : vérifie l'idempotence via idempotency.checkAndMark(idempotencyKey). Si la clé existe déjà, retourne la réponse en cache sans retraitement. Sinon, appelle domainService.credit(...), met la réponse en cache, retourne le résultat.
Fichier 30 : application/wallet/handler/DebitWalletHandler.java
Même structure que CreditWalletHandler mais pour le débit. Si le domaine retourne otpRequired = true, persiste un OTP challenge en Redis avec TTL 5 minutes et retourne un résultat partiel indiquant qu'un OTP est attendu.
Fichier 31 : application/wallet/handler/InitiateTopUpHandler.java
Annoté @Service. Implémente InitiateTopUpUseCase. Injecte WalletDomainService domainService, PaymentGatewayPort paymentGateway, PaymentRequestRepository paymentRequestRepo, IdempotencyPort idempotency.
Séquence : vérifier idempotence. Vérifier que le wallet existe et est ACTIVE. Charger la WalletPolicy et valider le montant. Créer une PaymentRequest en statut INITIATED. Appeler paymentGateway.initiateTopUp(...). Mettre à jour la PaymentRequest en statut PENDING. Retourner le PaymentInitiationResult.
Fichier 32 : application/wallet/handler/PaymentWebhookHandler.java
Annoté @Service. Gère les callbacks des providers de paiement. Méthode handleConfirmation(String externalRef, PaymentStatus status, Map<String, Object> rawPayload).
Séquence : charge la PaymentRequest via externalRef. Si status == COMPLETED et c'était un INBOUND (recharge), crédite le wallet du virtualAmount avec source TOPUP_*. Si c'était un OUTBOUND (retrait), libère la réservation. Si status == FAILED, annule la réservation et restitue le solde si nécessaire. Met à jour la PaymentRequest. Publie l'événement.
Fichier 33 : application/wallet/handler/FreezeWalletHandler.java
Annoté @Service. Implémente FreezeWalletUseCase. Appelle domainService.freeze(...) avec vérification d'idempotence.
Fichier 34 : application/wallet/handler/UnfreezeWalletHandler.java
Même structure pour le dégel.
Fichier 35 : application/wallet/handler/ReverseTransactionHandler.java
Implémente ReverseTransactionUseCase. Appelle domainService.reverse(...).

Phase 5 — Persistance R2DBC
Ces fichiers vont dans infrastructure/persistence/wallet/.
Fichier 36 : infrastructure/persistence/wallet/entity/WalletEntity.java
Classe annotée @Table("wallets"). Champs avec @Id sur UUID id et @Column pour chaque champ. Les noms snake_case de PostgreSQL sont mappés via @Column("member_id") etc. Champs : UUID id, UUID memberId, UUID tenantId, BigDecimal balance, String currencyCode, String status, Long version, Instant frozenAt, String frozenReason, Instant closedAt, Instant createdAt, Instant updatedAt, String createdBy, String updatedBy.
Fichier 37 : infrastructure/persistence/wallet/entity/WalletTransactionEntity.java
Annotée @Table("wallet_transactions"). Champs correspondant à la table SQL. Note : metadata est stocké comme String (JSON sérialisé) en base.
Fichier 38 : infrastructure/persistence/wallet/entity/PaymentRequestEntity.java
Annotée @Table("payment_requests").
Fichier 39 : infrastructure/persistence/wallet/entity/WalletAuditLogEntity.java
Annotée @Table("wallet_audit_logs").
Fichier 40 : infrastructure/persistence/wallet/entity/WalletPolicyEntity.java
Annotée @Table("wallet_policies"). Correspond à la nouvelle table qui sera créée dans la migration.
Fichier 41 : infrastructure/persistence/wallet/repository/WalletR2dbcRepository.java
Interface étendant ReactiveCrudRepository<WalletEntity, UUID>. Déclare Mono<WalletEntity> findByMemberIdAndTenantId(UUID memberId, UUID tenantId) et Mono<Boolean> existsByMemberIdAndTenantId(UUID memberId, UUID tenantId).
Fichier 42 : infrastructure/persistence/wallet/repository/WalletTransactionR2dbcRepository.java
Interface étendant ReactiveCrudRepository<WalletTransactionEntity, UUID>. Déclare Mono<WalletTransactionEntity> findByIdempotencyKey(String key). Déclare Flux<WalletTransactionEntity> findByWalletIdOrderByCreatedAtDesc(UUID walletId, Pageable pageable). Déclare avec @Query la requête SQL pour sumDebitsTodayForWallet : SELECT COALESCE(SUM(amount), 0) FROM wallet_transactions WHERE wallet_id = :walletId AND type IN ('DEBIT', 'RESERVE') AND status = 'COMPLETED' AND created_at >= :startOfDay.
Fichier 43 : infrastructure/persistence/wallet/repository/PaymentRequestR2dbcRepository.java
Interface. Déclare Mono<PaymentRequestEntity> findByExternalRef(String externalRef).
Fichier 44 : infrastructure/persistence/wallet/repository/WalletPolicyR2dbcRepository.java
Interface. Déclare Mono<WalletPolicyEntity> findByTenantId(UUID tenantId).
Fichier 45 : infrastructure/persistence/wallet/mapper/WalletMapper.java
Interface MapStruct @Mapper(componentModel = "spring"). Méthodes Wallet toDomain(WalletEntity entity) et WalletEntity toEntity(Wallet domain). Pour la conversion du status String en WalletStatus enum et vice-versa, utilise @ValueMapping. Pour tenantId de type TenantId et UUID, définit des méthodes default qui font la conversion TenantId.of(uuid) et tenantId.value().
Fichier 46 : infrastructure/persistence/wallet/mapper/WalletTransactionMapper.java
Interface MapStruct. La conversion du champ metadata de Map<String,Object> vers String JSON utilise une méthode default avec Jackson ObjectMapper. Attention : ObjectMapper ne peut pas être injecté directement dans MapStruct — utilise @Mapping(target = "metadata", expression = "java(serializeMetadata(domain.metadata()))") avec une méthode default dans l'interface.
Fichier 47 : infrastructure/persistence/wallet/mapper/WalletPolicyMapper.java
Interface MapStruct pour la conversion entre WalletPolicyEntity et WalletPolicy.
Fichier 48 : infrastructure/persistence/wallet/adapter/WalletRepositoryAdapter.java
Annoté @Component. Implémente WalletRepository du domaine. Injecte WalletR2dbcRepository r2dbcRepo et WalletMapper mapper. Chaque méthode délègue au repo R2DBC et mappe. Pour save(Wallet wallet), utilise r2dbcRepo.save(mapper.toEntity(wallet)).map(mapper::toDomain).
Fichier 49 : infrastructure/persistence/wallet/adapter/WalletTransactionRepositoryAdapter.java
Annoté @Component. Implémente WalletTransactionRepository. Implémentation de findByWalletIdAndFilters : si tous les filtres sont null, utilise findByWalletIdOrderByCreatedAtDesc. Sinon construit une requête @Query avec les filtres dynamiques — pour éviter une requête trop complexe, utilise DatabaseClient injecté pour construire la requête dynamiquement.
Fichier 50 : infrastructure/persistence/wallet/adapter/PaymentRequestRepositoryAdapter.java
Annoté @Component. Implémente PaymentRequestRepository.
Fichier 51 : infrastructure/persistence/wallet/adapter/WalletAuditLogRepositoryAdapter.java
Annoté @Component. Implémente WalletAuditLogRepository. La méthode log(...) crée un WalletAuditLogEntity et le sauvegarde. Si la sauvegarde échoue, logue l'erreur en WARN mais ne propage pas l'exception — l'audit ne doit jamais faire échouer l'opération principale.
Fichier 52 : infrastructure/persistence/wallet/adapter/WalletPolicyRepositoryAdapter.java
Annoté @Component. Implémente WalletPolicyRepository. La méthode findByTenant(TenantId id) : appelle r2dbcRepo.findByTenantId(id.value()). Si vide, retourne Mono.just(WalletPolicy.defaults()).

Phase 6 — Cache Redis
Fichier 53 : infrastructure/redis/adapter/WalletCacheAdapter.java
Annoté @Component. Injecte ReactiveRedisTemplate<String, String> redisTemplate et ObjectMapper objectMapper. Méthode cacheBalance(TenantId tenantId, UserId memberId, BigDecimal balance) stocke dans Redis avec TTL 30 secondes. Clé : RedisKeyBuilder.walletBalanceKey(tenantId, memberId). Méthode getBalance(TenantId tenantId, UserId memberId) retourne Mono<Optional<BigDecimal>>. Méthode evictBalance(TenantId tenantId, UserId memberId) supprime la clé. Ce cache est invalidé automatiquement après chaque mutation du wallet par les handlers.
Ajoute dans RedisKeyBuilder.java existant la méthode statique walletBalanceKey(TenantId tenantId, UserId memberId) retournant "loyalty:" + tenantId.value() + ":wallet:" + memberId.value() + ":balance".

Phase 7 — Kafka
Fichier 54 : infrastructure/kafka/producer/WalletEventProducer.java
Annoté @Component. Implémente WalletEventPublisherPort. Injecte ReactiveKafkaProducerTemplate<String, Object> kafkaTemplate.
La méthode publish(WalletDomainEvent event) sérialise l'événement en JSON, publie sur le topic loyalty.wallet.events.{tenantId} avec comme clé le memberId. Headers Kafka inclus : eventType, tenantId, eventId, occurredAt. En cas d'échec Kafka, logue ERROR mais retourne Mono.empty() — la publication d'événements ne doit jamais faire échouer l'opération principale (elle sera réessayée via l'outbox si nécessaire).

Phase 8 — Contrôleurs REST WebFlux
Ces fichiers vont dans api/wallet/.
Fichier 55 : api/wallet/dto/request/TopUpRequest.java
Record avec @NotNull @Positive BigDecimal amount et @NotBlank String provider.
Fichier 56 : api/wallet/dto/request/DebitRequest.java
Record avec @NotNull @Positive BigDecimal amount, @NotBlank String description, @NotBlank String orderReference.
Fichier 57 : api/wallet/dto/request/WithdrawRequest.java
Record avec @NotNull @Positive BigDecimal amount, @NotBlank String targetAccount, @NotBlank String provider.
Fichier 58 : api/wallet/dto/request/OtpConfirmRequest.java
Record avec @NotBlank String challengeId, @NotBlank String otpCode.
Fichier 59 : api/wallet/dto/request/FreezeRequest.java
Record avec @NotBlank String reason.
Fichier 60 : api/wallet/dto/response/WalletResponse.java
Record avec UUID id, String memberId, BigDecimal balance, String currencyCode, String currencyName, String status, Instant createdAt. Méthode statique from(Wallet wallet, WalletPolicy policy).
Fichier 61 : api/wallet/dto/response/TransactionResponse.java
Record avec UUID id, String type, String source, BigDecimal amount, BigDecimal balanceBefore, BigDecimal balanceAfter, String status, Instant createdAt, Map<String, Object> metadata. Méthode statique from(WalletTransaction tx).
Fichier 62 : api/wallet/dto/response/PaymentInitiationResponse.java
Record avec String externalRef, String status, String ussdCode, String redirectUrl, Instant expiresAt, boolean requiresUserAction.
Fichier 63 : api/wallet/WalletController.java
RouterFunction WebFlux. Routes pour les membres :
GET /api/v1/wallet — retourne WalletResponse. Lit le TenantId et UserId depuis le TenantContextHolder. Appelle GetWalletUseCase. Charge aussi la WalletPolicy pour construire la réponse complète.
GET /api/v1/wallet/transactions — retourne Flux<TransactionResponse> paginé. Accepte query params : type, source, from, to, page, size.
POST /api/v1/wallet/topup — requiert header Idempotency-Key. Valide le body TopUpRequest. Appelle InitiateTopUpUseCase. Retourne PaymentInitiationResponse avec status 202 Accepted.
POST /api/v1/wallet/debit — requiert Idempotency-Key. Appelle DebitWalletUseCase. Retourne 200 si débit direct ou 202 si OTP requis.
POST /api/v1/wallet/withdraw — requiert Idempotency-Key. Appelle InitiateWithdrawalUseCase. Retourne 202.
POST /api/v1/wallet/otp/confirm — requiert Idempotency-Key. Appelle ConfirmOtpUseCase.
Fichier 64 : api/wallet/WalletAdminController.java
Routes d'administration (requièrent rôle ROLE_TENANT_ADMIN) :
POST /api/v1/admin/wallet/{memberId}/freeze — appelle FreezeWalletUseCase.
POST /api/v1/admin/wallet/{memberId}/unfreeze — appelle UnfreezeWalletUseCase.
POST /api/v1/admin/wallet/{memberId}/adjust — crédit ou débit manuel avec motif obligatoire. Appelle CreditWalletUseCase ou DebitWalletUseCase selon le signe du montant.
GET /api/v1/admin/wallet/config — retourne la WalletPolicy du tenant.
PUT /api/v1/admin/wallet/config — met à jour la WalletPolicy. Body : tous les champs de WalletPolicy.
Fichier 65 : api/wallet/WebhookController.java
Routes publiques (sans JWT) pour les callbacks de paiement. Ces routes sont dans la liste blanche du SecurityConfig et du TenantResolutionFilter.
POST /api/v1/webhooks/payment/mtn — valide la signature MTN (en-tête X-MTN-Signature), appelle PaymentWebhookHandler.handleConfirmation(...).
POST /api/v1/webhooks/payment/orange — idem pour Orange.
POST /api/v1/webhooks/payment/stripe — valide la signature Stripe via Stripe-Signature, appelle le handler.
Pour l'instant en mode stub, la validation de signature logue un WARNING et continue — elle sera implémentée quand les vrais providers seront disponibles.

Phase 9 — Migrations Liquibase
Fichier 66 : Mettre à jour db/changelog/migrations/V001__create_wallet_tables.sql
La migration existante crée les tables wallets, wallet_transactions, payment_requests, wallet_audit_logs. Il faut ajouter à la fin de ce fichier la table manquante :
sql-- ============================================================
-- Wallet policies (configuration par tenant)
-- ============================================================
CREATE TABLE IF NOT EXISTS wallet_policies (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id               UUID NOT NULL UNIQUE,
    currency_name           VARCHAR(100) NOT NULL DEFAULT 'Credits',
    currency_symbol         VARCHAR(20) NOT NULL DEFAULT 'CR',
    exchange_rate           DECIMAL(10,6) NOT NULL DEFAULT 1.0,
    daily_spend_cap         DECIMAL(18,4),
    max_balance             DECIMAL(18,4),
    max_topup_per_txn       DECIMAL(18,4),
    min_withdrawal          DECIMAL(18,4),
    withdrawal_delay_hours  INT NOT NULL DEFAULT 24,
    otp_threshold           DECIMAL(18,4),
    kyc_required            BOOLEAN NOT NULL DEFAULT true,
    expiry_days             INT,
    expiry_notif_days       INT NOT NULL DEFAULT 30,
    allowed_operations      TEXT[] NOT NULL DEFAULT '{TOPUP,PURCHASE}',
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_wallet_policies_tenant
    ON wallet_policies(tenant_id);

-- ============================================================
-- OTP challenges (stockés en Redis, table pour l'audit)
-- ============================================================
CREATE TABLE IF NOT EXISTS otp_challenges (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    member_id       UUID NOT NULL,
    challenge_type  VARCHAR(50) NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    context         JSONB NOT NULL DEFAULT '{}',
    expires_at      TIMESTAMPTZ NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resolved_at     TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_otp_challenges_member
    ON otp_challenges(tenant_id, member_id, status);
Fichier 67 : Mettre à jour db/changelog/db.changelog-master.xml
Ajoute la nouvelle migration :
xml<include file="migrations/V002__add_wallet_policy_tables.sql"
         relativeToChangelogFile="true"/>
Crée le fichier V002__add_wallet_policy_tables.sql avec le contenu ci-dessus.

Phase 10 — Tests
Fichier 68 : test/domain/wallet/WalletTest.java
Test JUnit 5 sans Spring. Pas de Testcontainers. Teste uniquement la logique du domaine.
Teste que Wallet.create(...) avec autoActivate=true crée un wallet ACTIVE. Teste autoActivate=false crée PENDING_KYC. Teste que credit(amount, policy) sur un wallet ACTIVE met à jour le solde correctement. Teste que credit(amount, policy) sur un wallet FROZEN lance WalletDomainException. Teste que debit(amount, policy, todaySpent) avec solde insuffisant lance WalletDomainException. Teste que debit(...) avec daily cap dépassé lance WalletDomainException. Teste les transitions d'état : freeze(), unfreeze(), close(). Teste que close() depuis PENDING_KYC est valide. Teste que close() depuis CLOSED lance une exception.
Fichier 69 : test/domain/wallet/WalletPolicyTest.java
Teste validateCredit() : montant négatif → erreur. Montant dépassant maxBalance → erreur. Montant valide → vide. Teste validateDebit() : solde insuffisant → erreur. Daily cap dépassé → erreur. Cas valide → vide. Teste requiresOtp() : en dessous du seuil → false. Au-dessus → true. Seuil null → false.
Fichier 70 : test/domain/wallet/WalletDomainServiceTest.java
Test JUnit 5 sans Spring. Crée des fakes en mémoire pour tous les ports (inner classes dans le test). Teste credit(...) : vérifie que le wallet est sauvegardé avec le nouveau solde, que la transaction est persistée, que l'événement est publié. Teste debit(...) normal : même vérifications. Teste debit(...) avec OTP requis : vérifie que le wallet n'est pas modifié et qu'aucune transaction n'est créée. Teste freeze(...) : vérifie le changement d'état et l'audit log. Teste reverse(...) : vérifie que le solde est corrigé et qu'une transaction REVERSAL est créée.
Fichier 71 : test/application/wallet/CreditWalletHandlerTest.java
Test Mockito. Vérifie que le handler vérifie l'idempotence avant d'appeler le service. Vérifie que la même idempotencyKey soumise deux fois n'appelle le service qu'une seule fois.
Fichier 72 : test/application/wallet/InitiateTopUpHandlerTest.java
Test Mockito. Vérifie la séquence : chargement wallet, chargement policy, création PaymentRequest, appel gateway, mise à jour PaymentRequest.
Fichier 73 : test/integration/wallet/WalletControllerIntegrationTest.java
Test avec @SpringBootTest(webEnvironment = RANDOM_PORT), @ActiveProfiles("stub") et @Import(TestContainersConfig.class). Démarre PostgreSQL et Redis via Testcontainers.
@BeforeEach : insère en base un wallet actif pour le membre de test avec un solde de 10000.
Test 1 : GET /api/v1/wallet avec JWT valide → 200 avec balance 10000. Test 2 : POST /api/v1/wallet/topup avec body {amount: 5000, provider: "MTN"} et Idempotency-Key header → 202 avec ussdCode non null (grâce au stub). Test 3 : POST /api/v1/wallet/debit avec body {amount: 3000, description: "Test", orderReference: "ORD-001"} → 200 avec nouveau solde 7000. Test 4 : POST /api/v1/wallet/debit avec la même Idempotency-Key → 200 avec exactement la même réponse, solde toujours 7000 (idempotence vérifiée). Test 5 : POST /api/v1/wallet/debit avec montant supérieur au solde → 422 avec errorCode INSUFFICIENT_BALANCE. Test 6 : Admin POST /api/v1/admin/wallet/{memberId}/freeze → 200. Puis POST /api/v1/wallet/debit → 422 avec WALLET_FROZEN.

Phase 11 — Configuration Spring
Fichier 74 : infrastructure/config/WalletConfig.java
Annoté @Configuration. Déclare le bean WalletDomainService en injectant tous ses ports : WalletRepositoryAdapter, WalletTransactionRepositoryAdapter, WalletPolicyRepositoryAdapter, WalletAuditLogRepositoryAdapter, WalletEventProducer (ou le stub Kafka si profil stub). Cela garantit que WalletDomainService reste une classe Java pure sans annotation Spring tout en étant disponible comme bean.
java@Bean
public WalletDomainService walletDomainService(
    WalletRepository walletRepo,
    WalletTransactionRepository txRepo,
    WalletPolicyRepository policyRepo,
    WalletAuditLogRepository auditRepo,
    WalletEventPublisherPort eventPublisher
) {
    return new WalletDomainService(walletRepo, txRepo, policyRepo, auditRepo, eventPublisher);
}

Ordre d'implémentation recommandé
Implémente dans cet ordre exact pour éviter les blocages de compilation.
Jour 1 : Phase 1 complète (tous les modèles du domaine) + Phase 2 (tous les ports). Rien ne dépend encore de Spring. Exécute mvn compile — doit passer sans erreur. Exécute les tests du domaine — doivent passer sans Spring.
Jour 2 : Phase 3 (WalletDomainService) + Fichier 70 (test du service domaine avec fakes). Le service compile et ses tests passent prouvant l'isolation du domaine.
Jour 3 : Phase 5 (entités R2DBC et repositories) + Phase 9 (migrations SQL). L'application démarre avec les nouvelles tables créées.
Jour 4 : Phase 4 (handlers applicatifs) + Phase 6 (cache Redis) + Phase 11 (configuration Spring). Les beans sont connectés.
Jour 5 : Phase 7 (Kafka producer) + Phase 8 (contrôleurs REST). Les endpoints sont exposés.
Jour 6 : Phase 10 complète (tous les tests). Les tests d'intégration passent avec Testcontainers.

Vérifications finales
Vérification 1 — mvn test -Dtest=HexagonalArchitectureTest : toutes les règles ArchUnit passent.
Vérification 2 — mvn test -Dtest="WalletTest,WalletPolicyTest,WalletDomainServiceTest" : tests domaine sans Spring.
Vérification 3 — mvn test -Dtest=WalletControllerIntegrationTest : les 6 scénarios d'intégration passent.
Vérification 4 — mvn test : zéro régression.
Vérification 5 — Démarrage manuel avec le profil stub, puis séquence d'appels Postman/curl : créer un wallet via POST events, consulter le solde via GET wallet, initier un topup via POST topup, simuler un webhook de confirmation MTN sur POST webhooks/payment/mtn avec le externalRef reçu, vérifier que le solde a augmenté.
