Guide complet — Moteur de Règles et Points (Loyalty Core)
Contexte obligatoire à lire avant toute action
Tu travailles sur le backend du Loyalty Programme Royal, un SaaS multi-tenant de fidélisation. Le package racine est com.yowyob.loyalty. Le projet utilise Spring Boot 3.4, Spring WebFlux (réactif — .block() interdit partout), R2DBC pour PostgreSQL, Redis pour le cache et les compteurs, Kafka pour les événements. L'architecture est hexagonale stricte avec DDD.
Les modules suivants sont déjà en place et fonctionnels — ne les modifie pas sauf instruction explicite :

Fondations : TenantContextHolder, filtres JWT, IdempotencyPort, RedisKeyBuilder
Module Wallet complet : Wallet, WalletDomainService, CreditWalletUseCase

Règle absolue : aucun fichier dans domain/ n'importe quoi de Spring, R2DBC, Kafka, Redis, ou toute librairie externe. Seuls java.*, reactor.core.publisher.* et les autres classes du domaine sont autorisés.

Ce que ce module doit accomplir
Le moteur de règles est le cerveau de la plateforme. Une plateforme cliente (RidnGo, KSM, EventaaS) envoie un événement — "Jean vient de faire un achat de 5000 XAF". Le moteur évalue toutes les règles actives du tenant, détecte que Jean en est à son 10ème achat premium, déclenche les effets configurés (crédite 500 points, donne un ticket gratuit, remet le compteur à zéro), et retourne à la plateforme la liste de ce qui s'est passé.
Tout est configurable par le tenant sans toucher au code : les déclencheurs, les conditions, les effets, les seuils. Un admin configure ses règles via le dashboard, et elles s'appliquent immédiatement sur le prochain événement entrant.

Phase 1 — Modèles du domaine Loyalty
Ces fichiers vont dans src/main/java/com/yowyob/loyalty/domain/loyalty/. Zéro annotation Spring. Zéro import externe.
Fichier 1 : domain/loyalty/model/event/IncomingEvent.java
Record Java 21 immuable représentant un événement envoyé par la plateforme cliente via POST /api/v1/events. C'est l'entrée du moteur de règles.
Champs : String eventType (ex: purchase.completed, member.enrolled, trip.ended), TenantId tenantId, UserId memberId, String idempotencyKey, Instant occurredAt, Map<String, Object> payload (données libres : montant, catégorie produit, référence commande, etc.).
Méthode getPayloadValue(String key) retourne Optional<Object> en lisant payload.get(key). Méthode getPayloadString(String key) retourne Optional<String>. Méthode getPayloadDecimal(String key) retourne Optional<BigDecimal> en tentant la conversion depuis String ou Number. Ces méthodes seront utilisées par les conditions lors de l'évaluation.
Fichier 2 : domain/loyalty/model/event/EventProcessingResult.java
Record représentant le résultat complet du traitement d'un événement par le moteur.
Champs : String eventId, TenantId tenantId, UserId memberId, List<AppliedEffect> effectsApplied, List<String> notifications, Instant processedAt.
Méthode hasEffects() retourne !effectsApplied.isEmpty().
Fichier 3 : domain/loyalty/model/event/AppliedEffect.java
Record représentant un effet qui a été appliqué suite à une règle.
Champs : String effectType, String ruleId, String ruleName, Map<String, Object> details (ex: {points_credited: 500, new_balance: 4500} ou {reward_granted: "ticket-premium-gratuit"}).
Fichier 4 : domain/loyalty/model/rule/RuleStatus.java
Enum : DRAFT, ACTIVE, SUSPENDED, ARCHIVED.
Méthode isEvaluable() retourne this == ACTIVE.
Fichier 5 : domain/loyalty/model/rule/TriggerDefinition.java
Record représentant le déclencheur d'une règle. Un trigger définit quel type d'événement active l'évaluation de la règle.
Champs : String eventType (ex: purchase.completed), Map<String, Object> filters (ex: {"category": "premium_ticket", "min_amount": 5000}).
Méthode matches(IncomingEvent event) retourne boolean. Elle compare event.eventType() avec this.eventType. Si les types correspondent, elle évalue chaque entrée de filters : pour chaque filtre, elle lit la valeur correspondante dans event.payload() et vérifie l'égalité. Si tous les filtres passent, retourne true. Si filters est vide, retourne true dès que le type correspond.
Fichier 6 : domain/loyalty/model/rule/ConditionType.java
Enum listant tous les types de conditions supportés.
Constantes : CUMULATIVE_COUNT (nombre d'événements cumulés depuis le début ou sur une fenêtre), CUMULATIVE_AMOUNT (montant total cumulé), POINTS_BALANCE (solde de points actuel du membre), TIER_IS (palier actuel du membre), TIME_WINDOW (heure ou jour de la semaine), MEMBER_ATTRIBUTE (attribut libre du membre dans le payload), FIRST_EVENT (premier événement de ce type pour ce membre).
Fichier 7 : domain/loyalty/model/rule/ConditionOperator.java
Enum : EQUALS, NOT_EQUALS, GREATER_THAN, GREATER_THAN_OR_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL, IN, NOT_IN.
Méthode evaluate(Comparable actual, Comparable threshold) retourne boolean selon l'opérateur. Pour IN et NOT_IN, le threshold est une liste.
Fichier 8 : domain/loyalty/model/rule/ConditionDefinition.java
Record représentant une condition d'une règle. Toutes les conditions d'une règle doivent être vraies (AND logique) pour que les effets soient déclenchés.
Champs : ConditionType type, ConditionOperator operator, Object thresholdValue (peut être Long, BigDecimal, String, ou List selon le type), String windowType nullable (LIFETIME, MONTHLY, WEEKLY, DAILY — pour les types cumulatifs), String counterKey nullable (identifiant du compteur à utiliser — généré automatiquement si null).
Méthode getThresholdAsLong() retourne Long. Méthode getThresholdAsBigDecimal() retourne BigDecimal. Ces conversions sont nécessaires car le threshold est stocké en JSON et désérialisé comme Object.
Fichier 9 : domain/loyalty/model/rule/EffectType.java
Enum listant tous les types d'effets supportés.
Constantes : CREDIT_POINTS (ajouter des points), DEBIT_POINTS (consommer des points), CREDIT_WALLET (ajouter du crédit monétaire au wallet), GRANT_REWARD (attribuer une récompense du catalogue), UPDATE_TIER (changer le palier du membre), RESET_COUNTER (remettre un compteur à zéro), SEND_NOTIFICATION (déclencher une notification), MULTIPLY_POINTS (multiplicateur sur les points d'une autre règle dans le même traitement).
Fichier 10 : domain/loyalty/model/rule/EffectDefinition.java
Record représentant un effet à appliquer quand toutes les conditions d'une règle sont satisfaites.
Champs : EffectType type, Map<String, Object> params. Les params varient selon le type : pour CREDIT_POINTS → {amount: 500}, pour GRANT_REWARD → {reward_id: "uuid-xxx"}, pour RESET_COUNTER → {counter_key: "premium_ticket_count"}, pour SEND_NOTIFICATION → {template: "reward_earned", channel: "PUSH"}.
Méthode getParamAsLong(String key) retourne Optional<Long>. Méthode getParamAsString(String key) retourne Optional<String>. Méthode getParamAsBigDecimal(String key) retourne Optional<BigDecimal>.
Fichier 11 : domain/loyalty/model/rule/Rule.java
C'est l'agrégat central du moteur. Classe Java standard, constructeur privé, factory method statique.
Champs : UUID id, TenantId tenantId, String name, String description, int priority (plus élevé = évalué en premier), RuleStatus status, TriggerDefinition trigger, List<ConditionDefinition> conditions, List<EffectDefinition> effects, Instant validFrom nullable, Instant validUntil nullable, int version, Instant createdAt, Instant updatedAt.
Méthode statique create(UUID id, TenantId tenantId, String name, TriggerDefinition trigger, List<ConditionDefinition> conditions, List<EffectDefinition> effects, int priority) valide que les listes ne sont pas vides et retourne une règle en statut DRAFT.
Méthode activate() retourne un nouveau Rule avec status ACTIVE. Méthode suspend() retourne un Rule avec status SUSPENDED. Méthode archive() retourne un Rule avec status ARCHIVED.
Méthode isActiveAt(Instant moment) retourne true si status est ACTIVE, et si validFrom/validUntil sont non null, que le moment est dans la fenêtre.
Méthode triggerMatches(IncomingEvent event) délègue à trigger.matches(event).
Getters pour tous les champs.
Fichier 12 : domain/loyalty/model/points/PointsAccount.java
Agrégat représentant le compte de points d'un membre pour un tenant donné.
Champs : UUID id, TenantId tenantId, UserId memberId, long availablePoints, long lifetimeEarned, long lifetimeSpent, long version, Instant lastActivityAt, Instant updatedAt.
Méthode statique create(UUID id, TenantId tenantId, UserId memberId) crée un compte à zéro.
Méthode earn(long points) retourne un nouveau PointsAccount avec availablePoints += points, lifetimeEarned += points, lastActivityAt = now, version++. Valide que points > 0.
Méthode spend(long points) retourne un nouveau PointsAccount. Vérifie que availablePoints >= points — sinon lance LoyaltyDomainException("Solde de points insuffisant"). Met à jour availablePoints et lifetimeSpent.
Méthode expire(long points) retourne un nouveau PointsAccount en soustrayant les points expirés de availablePoints sans affecter lifetimeSpent.
Méthode hasEnoughPoints(long required) retourne availablePoints >= required.
Fichier 13 : domain/loyalty/model/points/PointsTransaction.java
Record immuable. Une fois créée, ne peut jamais être modifiée.
Champs : UUID id, UUID pointsAccountId, TenantId tenantId, String type (CREDIT, DEBIT, EXPIRY, REVERSAL), long amount, long balanceAfter, String source (RULE_ENGINE, REDEMPTION, EXPIRY_JOB, REFERRAL, ADMIN_ADJUSTMENT), UUID ruleId nullable, String eventIdempotencyKey nullable, Map<String, Object> metadata, Instant createdAt.
Méthode statique forCredit(UUID accountId, TenantId tenantId, long amount, long balanceAfter, UUID ruleId, String eventKey) construit la transaction avec type CREDIT et source RULE_ENGINE.
Méthode statique forDebit(UUID accountId, TenantId tenantId, long amount, long balanceAfter) avec source REDEMPTION.
Fichier 14 : domain/loyalty/model/counter/Counter.java
Record représentant un compteur cumulatif par membre et par règle. Les compteurs permettent d'implémenter les tampons (10 achats = 1 récompense).
Champs : UUID id, TenantId tenantId, UserId memberId, String counterKey (identifiant unique du compteur, ex: rule_{ruleId}_count), long value, String windowType (LIFETIME, MONTHLY, WEEKLY, DAILY — null = LIFETIME), Instant windowStart nullable, Instant updatedAt.
Méthode increment(long delta) retourne un nouveau Counter avec value += delta et updatedAt = now.
Méthode reset() retourne un nouveau Counter avec value = 0 et updatedAt = now.
Méthode isExpiredWindow(Instant now) retourne true si la fenêtre temporelle est dépassée. Pour MONTHLY, vérifie si windowStart est dans un mois différent du mois courant. Pour WEEKLY, même logique. Pour DAILY, si c'est un jour différent. Pour LIFETIME ou null, retourne toujours false.
Fichier 15 : domain/loyalty/model/tier/TierLevel.java
Enum : BRONZE, SILVER, GOLD, PLATINUM.
Méthode isHigherThan(TierLevel other) utilise ordinal() pour comparer. Méthode next() retourne Optional<TierLevel> avec le palier suivant ou vide si PLATINUM.
Fichier 16 : domain/loyalty/model/tier/MemberTier.java
Record représentant le palier actuel d'un membre.
Champs : UUID id, TenantId tenantId, UserId memberId, TierLevel level, BigDecimal pointsMultiplier (ex: 1.5 pour Silver = 50% de points en plus), Instant reachedAt, Instant validUntil nullable.
Méthode statique defaultTier(UUID id, TenantId tenantId, UserId memberId) crée un palier BRONZE avec multiplicateur 1.0.
Méthode withLevel(TierLevel newLevel, BigDecimal multiplier) retourne un nouveau MemberTier avec le nouveau niveau.
Fichier 17 : domain/loyalty/model/tier/TierPolicy.java
Record représentant la configuration des paliers d'un tenant.
Champs : TenantId tenantId, String criterion (LIFETIME_POINTS, TOTAL_SPENT, PURCHASE_COUNT), List<TierThreshold> thresholds, String maintainPeriod (QUARTERLY, MONTHLY), long maintainThresholdPoints, int downgradeGraceDays.
Record interne TierThreshold avec TierLevel level, long threshold, BigDecimal multiplier.
Méthode calculateTier(long criterionValue) retourne TierLevel en itérant les thresholds du plus élevé au plus bas.
Méthode statique defaults(TenantId tenantId) retourne une politique Bronze/Silver/Gold/Platinum avec des seuils typiques.
Fichier 18 : domain/loyalty/exception/LoyaltyDomainException.java
Étend DomainException existante. Constructeur avec String message. Constructeur avec String message, Map<String, Object> details.

Phase 2 — Contexte d'évaluation du moteur
Ces fichiers sont au cœur de la logique d'évaluation. Ils vont dans domain/loyalty/model/engine/.
Fichier 19 : domain/loyalty/model/engine/EvaluationContext.java
Record immuable qui encapsule toutes les données disponibles au moment de l'évaluation d'une règle. Le moteur charge ces données une fois avant d'évaluer toutes les règles, ce qui évite N+1 requêtes.
Champs : IncomingEvent event, PointsAccount pointsAccount nullable (null si le membre n'a pas encore de compte), MemberTier memberTier nullable, Map<String, Counter> counters (map de counterKey → Counter, pré-chargés depuis Redis/base), TierPolicy tierPolicy nullable.
Méthode getCounter(String key) retourne Optional<Counter>. Méthode getCounterValue(String key) retourne long (0 si le compteur n'existe pas encore).
Fichier 20 : domain/loyalty/model/engine/ConditionEvaluationResult.java
Record : boolean passed, String conditionType, String reason.
Méthodes statiques passed(String conditionType) et failed(String conditionType, String reason).
Fichier 21 : domain/loyalty/model/engine/RuleEvaluationResult.java
Record : Rule rule, boolean triggered, List<ConditionEvaluationResult> conditionResults, List<AppliedEffect> appliedEffects, String skipReason.
Méthodes statiques triggered(Rule rule, List<ConditionEvaluationResult> conditions, List<AppliedEffect> effects) et skipped(Rule rule, String reason) et conditionsFailed(Rule rule, List<ConditionEvaluationResult> conditions).

Phase 3 — Évaluateurs de conditions
Ces fichiers vont dans domain/loyalty/service/evaluator/. Ils sont des classes Java pures, zéro annotation Spring. Le pattern Strategy permet d'ajouter de nouveaux types de conditions sans modifier le code existant.
Fichier 22 : domain/loyalty/service/evaluator/ConditionEvaluator.java
Interface Java pure déclarant deux méthodes.
boolean supports(ConditionType type) indique si cet évaluateur gère ce type de condition.
ConditionEvaluationResult evaluate(ConditionDefinition condition, EvaluationContext context) évalue la condition et retourne le résultat.
Fichier 23 : domain/loyalty/service/evaluator/CumulativeCountEvaluator.java
Implémente ConditionEvaluator. supports() retourne true pour CUMULATIVE_COUNT.
evaluate(...) : lit la clé de compteur depuis condition.counterKey(). Appelle context.getCounterValue(key). Compare avec condition.thresholdValue() en utilisant condition.operator(). Retourne ConditionEvaluationResult.passed() ou .failed() selon le résultat.
Fichier 24 : domain/loyalty/service/evaluator/CumulativeAmountEvaluator.java
Implémente ConditionEvaluator. supports() retourne true pour CUMULATIVE_AMOUNT.
evaluate(...) : lit le montant cumulé depuis le counter. Compare comme BigDecimal.
Fichier 25 : domain/loyalty/service/evaluator/PointsBalanceEvaluator.java
Implémente ConditionEvaluator. supports() retourne true pour POINTS_BALANCE.
evaluate(...) : si context.pointsAccount() est null, retourne failed avec raison "Aucun compte de points". Sinon compare pointsAccount.availablePoints() avec le threshold.
Fichier 26 : domain/loyalty/service/evaluator/TierEvaluator.java
Implémente ConditionEvaluator. supports() retourne true pour TIER_IS.
evaluate(...) : si context.memberTier() est null, retourne failed. Compare le niveau du palier avec la valeur threshold (qui est le nom du TierLevel comme String). Pour l'opérateur EQUALS, vérifie memberTier.level().name().equals(threshold). Pour GREATER_THAN, utilise isHigherThan().
Fichier 27 : domain/loyalty/service/evaluator/TimeWindowEvaluator.java
Implémente ConditionEvaluator. supports() retourne true pour TIME_WINDOW.
evaluate(...) : lit l'heure et le jour de la semaine depuis context.event().occurredAt(). Le threshold est un objet JSON avec des champs optionnels days_of_week (liste de MONDAY, TUESDAY...) et hours (liste de plages comme "18:00-20:00"). Évalue si l'événement est dans la fenêtre configurée.
Fichier 28 : domain/loyalty/service/evaluator/FirstEventEvaluator.java
Implémente ConditionEvaluator. supports() retourne true pour FIRST_EVENT.
evaluate(...) : lit le compteur du membre pour ce type d'événement. Si la valeur est 0 (aucun événement précédent de ce type), retourne passed. Sinon, failed.

Phase 4 — Exécuteurs d'effets
Ces fichiers vont dans domain/loyalty/service/executor/. Classes Java pures, zéro annotation Spring.
Fichier 29 : domain/loyalty/service/executor/EffectExecutionContext.java
Record mutable (classe Java standard, pas record) qui accumule les effets à appliquer pendant l'évaluation d'un lot de règles. Il est passé à chaque exécuteur d'effets pour qu'ils enregistrent ce qui doit être fait.
Champs : List<PointsOperation> pendingPointsOperations, List<WalletOperation> pendingWalletOperations, List<RewardOperation> pendingRewardOperations, List<CounterOperation> pendingCounterOperations, List<NotificationOperation> pendingNotifications, List<TierOperation> pendingTierOperations.
Records internes :

PointsOperation(UserId memberId, long amount, String type, UUID ruleId)
WalletOperation(UserId memberId, BigDecimal amount, String source)
RewardOperation(UserId memberId, String rewardId, UUID ruleId)
CounterOperation(String counterKey, String operationType, long delta) — operationType: INCREMENT ou RESET
NotificationOperation(UserId memberId, String template, Map<String, Object> params)
TierOperation(UserId memberId, TierLevel newLevel)

Méthodes d'ajout : addPointsCredit(...), addWalletCredit(...), addRewardGrant(...), addCounterIncrement(...), addCounterReset(...), addNotification(...), addTierUpdate(...).
Fichier 30 : domain/loyalty/service/executor/EffectExecutor.java
Interface déclarant :
boolean supports(EffectType type) — indique si cet exécuteur gère ce type d'effet.
AppliedEffect execute(EffectDefinition effect, EvaluationContext context, EffectExecutionContext executionContext) — enregistre l'opération dans executionContext et retourne un AppliedEffect décrivant ce qui a été planifié.
Fichier 31 : domain/loyalty/service/executor/CreditPointsExecutor.java
Implémente EffectExecutor. supports() retourne true pour CREDIT_POINTS et MULTIPLY_POINTS.
Pour CREDIT_POINTS : lit amount depuis effect.params(). Si context.memberTier() non null, multiplie par memberTier.pointsMultiplier() (ex: 500 pts × 1.5 = 750 pts pour un membre Silver). Appelle executionContext.addPointsCredit(memberId, finalAmount, "CREDIT", ruleId). Retourne un AppliedEffect avec {points_credited: finalAmount, multiplier_applied: 1.5}.
Pour MULTIPLY_POINTS : lit multiplier depuis params. Modifie toutes les opérations CREDIT déjà dans executionContext.pendingPointsOperations en multipliant leur montant. Retourne un AppliedEffect avec {multiplier: multiplier}.
Fichier 32 : domain/loyalty/service/executor/CreditWalletExecutor.java
Implémente EffectExecutor. supports() retourne true pour CREDIT_WALLET.
Lit amount depuis params. Appelle executionContext.addWalletCredit(memberId, amount, "CASHBACK"). Retourne AppliedEffect avec {wallet_credited: amount}.
Fichier 33 : domain/loyalty/service/executor/GrantRewardExecutor.java
Implémente EffectExecutor. supports() retourne true pour GRANT_REWARD.
Lit reward_id depuis params. Appelle executionContext.addRewardGrant(memberId, rewardId, ruleId). Retourne AppliedEffect avec {reward_id: rewardId}.
Fichier 34 : domain/loyalty/service/executor/ResetCounterExecutor.java
Implémente EffectExecutor. supports() retourne true pour RESET_COUNTER.
Lit counter_key depuis params. Appelle executionContext.addCounterReset(counterKey). Retourne AppliedEffect avec {counter_reset: counterKey}.
Fichier 35 : domain/loyalty/service/executor/UpdateTierExecutor.java
Implémente EffectExecutor. supports() retourne true pour UPDATE_TIER.
Lit tier_level depuis params. Appelle executionContext.addTierUpdate(memberId, TierLevel.valueOf(tierLevel)). Retourne AppliedEffect avec {new_tier: tierLevel}.
Fichier 36 : domain/loyalty/service/executor/SendNotificationExecutor.java
Implémente EffectExecutor. supports() retourne true pour SEND_NOTIFICATION.
Lit template et channel depuis params. Appelle executionContext.addNotification(memberId, template, event.payload()). Retourne AppliedEffect avec {notification_queued: template}.

Phase 5 — Le moteur de règles (cœur du système)
Fichier 37 : domain/loyalty/service/RuleEngine.java
Classe Java pure, zéro annotation Spring. C'est le composant central qui orchestre l'évaluation.
Constructeur : RuleEngine(List<ConditionEvaluator> conditionEvaluators, List<EffectExecutor> effectExecutors). Ces listes sont injectées par le conteneur Spring via WalletConfig — le domaine ne connaît pas Spring mais reçoit les implémentations.
Méthode principale : EventProcessingResult process(IncomingEvent event, EvaluationContext context, List<Rule> activeRules).
Algorithme détaillé :

Trie les activeRules par priorité décroissante.
Crée un EffectExecutionContext vide.
Pour chaque règle, appelle evaluateRule(rule, event, context, executionContext).
Après toutes les règles, retourne un EventProcessingResult avec les effets planifiés dans executionContext.

Méthode evaluateRule : RuleEvaluationResult evaluateRule(Rule rule, IncomingEvent event, EvaluationContext context, EffectExecutionContext executionContext).

Vérifie rule.triggerMatches(event) — si false, retourne RuleEvaluationResult.skipped(rule, "trigger_mismatch").
Évalue chaque condition via evaluateCondition(condition, context).
Si toutes les conditions passent, exécute chaque effet via executeEffect(effect, context, executionContext).
Si au moins une condition échoue, retourne RuleEvaluationResult.conditionsFailed(rule, conditionResults).

Méthode evaluateCondition : trouve l'évaluateur qui supports(condition.type()), appelle evaluate(condition, context). Si aucun évaluateur trouvé, lance LoyaltyDomainException("Aucun évaluateur pour le type: " + condition.type()).
Méthode executeEffect : trouve l'exécuteur qui supports(effect.type()), appelle execute(effect, context, executionContext). Retourne l'AppliedEffect.
Note importante : le RuleEngine ne persiste rien, ne publie rien, ne connaît pas Redis ni Kafka. Il produit uniquement un EffectExecutionContext rempli et des résultats d'évaluation. La persistance des effets est déléguée au LoyaltyApplicationService.
Fichier 38 : domain/loyalty/service/CounterService.java
Classe Java pure. Gère la logique des compteurs — incrémentation et réinitialisation.
Constructeur : CounterService(CounterRepository counterRepo).
Méthode incrementCounters(TenantId tenantId, UserId memberId, IncomingEvent event, List<Rule> rules) retourne Mono<Map<String, Counter>>. Pour chaque règle active dont le trigger correspond à l'événement, incrémente le compteur associé. Vérifie si la fenêtre temporelle du compteur est expirée — si oui, remet à zéro avant d'incrémenter. La clé du compteur est construite comme rule_{ruleId}_count par défaut, ou la valeur de counterKey de la condition si elle est spécifiée.
Méthode resetCounter(TenantId tenantId, UserId memberId, String counterKey) retourne Mono<Void>.
Méthode loadCounters(TenantId tenantId, UserId memberId, List<String> counterKeys) retourne Mono<Map<String, Counter>>.
Fichier 39 : domain/loyalty/service/TierCalculationService.java
Classe Java pure.
Constructeur : TierCalculationService(MemberTierRepository tierRepo, TierPolicyRepository policyRepo, PointsAccountRepository pointsRepo).
Méthode recalculateTier(TenantId tenantId, UserId memberId) retourne Mono<Optional<TierOperation>>. Charge la TierPolicy du tenant, charge le PointsAccount du membre, calcule le palier attendu avec policy.calculateTier(lifetimeEarned). Si le palier calculé est différent du palier actuel, retourne Optional.of(new TierOperation(memberId, newLevel)). Sinon retourne Optional.empty().

Phase 6 — Ports du domaine Loyalty
Fichier 40 : domain/loyalty/port/in/ProcessEventUseCase.java
Interface déclarant Mono<EventProcessingResult> processEvent(IncomingEvent event).
Fichier 41 : domain/loyalty/port/in/CreateRuleUseCase.java
Interface déclarant Mono<Rule> createRule(TenantId tenantId, String name, String description, TriggerDefinition trigger, List<ConditionDefinition> conditions, List<EffectDefinition> effects, int priority, String idempotencyKey).
Fichier 42 : domain/loyalty/port/in/ActivateRuleUseCase.java
Interface déclarant Mono<Rule> activateRule(TenantId tenantId, UUID ruleId).
Fichier 43 : domain/loyalty/port/in/GetMemberPointsUseCase.java
Interface déclarant Mono<PointsAccount> getPoints(TenantId tenantId, UserId memberId) et Flux<PointsTransaction> getPointsHistory(TenantId tenantId, UserId memberId, int page, int size).
Fichier 44 : domain/loyalty/port/in/GetMemberTierUseCase.java
Interface déclarant Mono<MemberTier> getTier(TenantId tenantId, UserId memberId).
Fichier 45 : domain/loyalty/port/out/RuleRepository.java
Interface déclarant Flux<Rule> findActiveByTenant(TenantId tenantId), Mono<Rule> findById(UUID ruleId), Mono<Rule> save(Rule rule), Flux<Rule> findByTenant(TenantId tenantId, int page, int size).
Fichier 46 : domain/loyalty/port/out/PointsAccountRepository.java
Interface déclarant Mono<PointsAccount> findByMemberAndTenant(UserId memberId, TenantId tenantId), Mono<PointsAccount> save(PointsAccount account), Mono<Boolean> existsByMemberAndTenant(UserId memberId, TenantId tenantId).
Fichier 47 : domain/loyalty/port/out/PointsTransactionRepository.java
Interface déclarant Mono<PointsTransaction> save(PointsTransaction tx), Flux<PointsTransaction> findByAccountId(UUID accountId, int page, int size).
Fichier 48 : domain/loyalty/port/out/CounterRepository.java
Interface déclarant Mono<Counter> findByMemberAndKey(UserId memberId, TenantId tenantId, String counterKey), Mono<Counter> save(Counter counter), Flux<Counter> findAllByMemberAndKeys(UserId memberId, TenantId tenantId, List<String> keys).
Fichier 49 : domain/loyalty/port/out/MemberTierRepository.java
Interface déclarant Mono<MemberTier> findByMemberAndTenant(UserId memberId, TenantId tenantId), Mono<MemberTier> save(MemberTier tier).
Fichier 50 : domain/loyalty/port/out/TierPolicyRepository.java
Interface déclarant Mono<TierPolicy> findByTenant(TenantId tenantId), Mono<TierPolicy> save(TierPolicy policy). Si aucune politique n'est configurée, retourne Mono.just(TierPolicy.defaults(tenantId)).
Fichier 51 : domain/loyalty/port/out/RuleCachePort.java
Interface pour le cache des règles actives. Déclarant Mono<List<Rule>> getCachedRules(TenantId tenantId), Mono<Void> cacheRules(TenantId tenantId, List<Rule> rules, Duration ttl), Mono<Void> evictRules(TenantId tenantId).
Fichier 52 : domain/loyalty/port/out/LoyaltyEventPublisherPort.java
Interface déclarant Mono<Void> publish(LoyaltyDomainEvent event).
On crée aussi domain/loyalty/event/LoyaltyDomainEvent.java interface étendant DomainEvent. Puis les records : PointsEarnedEvent, PointsSpentEvent, TierChangedEvent, RuleTriggeredEvent, EventProcessedEvent — tous dans domain/loyalty/event/.

Phase 7 — Service applicatif principal
Fichier 53 : domain/loyalty/service/LoyaltyDomainService.java
Classe Java pure. Implémente ProcessEventUseCase, CreateRuleUseCase, ActivateRuleUseCase, GetMemberPointsUseCase, GetMemberTierUseCase.
Constructeur : LoyaltyDomainService(RuleEngine ruleEngine, CounterService counterService, TierCalculationService tierCalcService, RuleRepository ruleRepo, PointsAccountRepository pointsRepo, PointsTransactionRepository pointsTxRepo, CounterRepository counterRepo, MemberTierRepository tierRepo, TierPolicyRepository tierPolicyRepo, RuleCachePort ruleCache, LoyaltyEventPublisherPort eventPublisher).
Implémentation de processEvent : c'est la méthode la plus importante du système. Voici l'algorithme exact en chaîne réactive :
Étape 1 — Charger les règles actives depuis le cache Redis via ruleCache.getCachedRules(tenantId). Si le cache est vide, charger depuis ruleRepo.findActiveByTenant(tenantId) et mettre en cache avec TTL 5 minutes.
Étape 2 — Filtrer les règles valides au moment de l'événement via rule.isActiveAt(event.occurredAt()).
Étape 3 — Charger les clés de compteurs nécessaires : itérer les règles, extraire tous les counterKey des conditions, créer une liste unique de clés.
Étape 4 — Charger en parallèle (Mono.zip) : PointsAccount du membre (ou null si absent), MemberTier du membre (ou null), les Counter correspondants aux clés, la TierPolicy du tenant.
Étape 5 — Construire l'EvaluationContext avec toutes les données chargées.
Étape 6 — Appeler ruleEngine.process(event, context, activeRules) — opération synchrone, aucun I/O.
Étape 7 — Appliquer les effets en séquence depuis executionContext :
Pour chaque PointsOperation de type CREDIT : si le PointsAccount n'existe pas, le créer d'abord. Appeler pointsAccount.earn(amount). Sauvegarder le compte mis à jour. Créer et sauvegarder une PointsTransaction. Recalculer le palier via tierCalcService.recalculateTier(...).
Pour chaque WalletOperation : appeler le CreditWalletUseCase (injecté depuis le module wallet).
Pour chaque RewardOperation : sera traité par le module Rewards — pour l'instant, publier un event GrantRewardRequestedEvent sur Kafka que le module Rewards consommera.
Pour chaque CounterOperation de type INCREMENT : counterService.incrementCounters(...). De type RESET : counterService.resetCounter(...).
Pour chaque TierOperation : charger le tier, appeler tier.withLevel(newLevel, multiplier), sauvegarder, publier TierChangedEvent.
Pour chaque NotificationOperation : publier sur Kafka pour que le NotificationService consomme.
Étape 8 — Incrémenter automatiquement les compteurs de toutes les règles dont le trigger a matché, indépendamment du fait que les conditions aient passé ou non. Cela permet aux tampons de compter même si le seuil n'est pas encore atteint.
Étape 9 — Publier un EventProcessedEvent sur Kafka avec le résultat complet.
Étape 10 — Retourner l'EventProcessingResult.

Phase 8 — Handlers applicatifs
Fichier 54 : application/loyalty/handler/ProcessEventHandler.java
Annoté @Service. Implémente ProcessEventUseCase. Injecte LoyaltyDomainService domainService et IdempotencyPort idempotency.
La méthode processEvent(IncomingEvent event) : vérifie l'idempotence via event.idempotencyKey(). Si la clé existe, retourne la réponse en cache. Sinon appelle domainService.processEvent(event), met en cache avec TTL 24h, retourne le résultat.
Fichier 55 : application/loyalty/handler/CreateRuleHandler.java
Annoté @Service. Implémente CreateRuleUseCase. Injecte LoyaltyDomainService domainService, RuleCachePort ruleCache, IdempotencyPort idempotency.
La méthode createRule(...) : vérifie idempotence. Appelle domainService.createRule(...). Invalide le cache des règles du tenant via ruleCache.evictRules(tenantId). Retourne la règle créée.
Fichier 56 : application/loyalty/handler/ActivateRuleHandler.java
Annoté @Service. Implémente ActivateRuleUseCase. Invalide le cache après activation.

Phase 9 — Persistance R2DBC
Fichier 57 : infrastructure/persistence/loyalty/entity/RuleEntity.java
Annotée @Table("rules"). Champs : UUID id, UUID tenantId, String name, String description, int priority, String status, String triggerDefinition (JSON sérialisé), String conditions (JSON sérialisé — liste), String effects (JSON sérialisé — liste), Instant validFrom, Instant validUntil, int version, Instant createdAt, Instant updatedAt.
Les champs triggerDefinition, conditions, effects sont stockés comme JSON string en base. La désérialisation est gérée par le mapper avec Jackson.
Fichier 58 : infrastructure/persistence/loyalty/entity/PointsAccountEntity.java
Annotée @Table("points_accounts"). Champs correspondant à PointsAccount.
Fichier 59 : infrastructure/persistence/loyalty/entity/PointsTransactionEntity.java
Annotée @Table("points_transactions").
Fichier 60 : infrastructure/persistence/loyalty/entity/CounterEntity.java
Annotée @Table("loyalty_counters").
Fichier 61 : infrastructure/persistence/loyalty/entity/MemberTierEntity.java
Annotée @Table("member_tiers").
Fichier 62 : infrastructure/persistence/loyalty/entity/TierPolicyEntity.java
Annotée @Table("tier_policies"). Le champ thresholds est un JSON sérialisé.
Fichier 63 : infrastructure/persistence/loyalty/repository/RuleR2dbcRepository.java
Interface étendant ReactiveCrudRepository<RuleEntity, UUID>. Déclare Flux<RuleEntity> findByTenantIdAndStatus(UUID tenantId, String status) et Flux<RuleEntity> findByTenantId(UUID tenantId, Pageable pageable).
Fichier 64 : infrastructure/persistence/loyalty/repository/PointsAccountR2dbcRepository.java
Déclare Mono<PointsAccountEntity> findByMemberIdAndTenantId(UUID memberId, UUID tenantId).
Fichier 65 : infrastructure/persistence/loyalty/repository/CounterR2dbcRepository.java
Déclare Mono<CounterEntity> findByMemberIdAndTenantIdAndCounterKey(UUID memberId, UUID tenantId, String counterKey) et Flux<CounterEntity> findByMemberIdAndTenantIdAndCounterKeyIn(UUID memberId, UUID tenantId, List<String> keys).
Fichier 66 : infrastructure/persistence/loyalty/repository/MemberTierR2dbcRepository.java
Déclare Mono<MemberTierEntity> findByMemberIdAndTenantId(UUID memberId, UUID tenantId).
Fichier 67 : infrastructure/persistence/loyalty/mapper/RuleMapper.java
Interface MapStruct. La conversion de TriggerDefinition, List<ConditionDefinition>, List<EffectDefinition> vers String JSON utilise des méthodes default avec ObjectMapper. Injection de ObjectMapper via @Mapper(uses = {JacksonMapperHelper.class}, componentModel = "spring").
Crée infrastructure/persistence/loyalty/mapper/JacksonMapperHelper.java annoté @Component avec ObjectMapper objectMapper injecté, qui expose des méthodes de sérialisation/désérialisation utilisables par MapStruct.
Fichier 68 à 71 : Adapters
RuleRepositoryAdapter.java, PointsAccountRepositoryAdapter.java, CounterRepositoryAdapter.java, MemberTierRepositoryAdapter.java — même pattern que les adapters wallet. Chaque adapter annoté @Component, implémente l'interface du domaine, délègue au repository R2DBC, utilise le mapper.

Phase 10 — Cache Redis pour les règles et compteurs
Fichier 72 : infrastructure/redis/adapter/RuleCacheAdapter.java
Annoté @Component. Implémente RuleCachePort. Injecte ReactiveRedisTemplate<String, String> redisTemplate et ObjectMapper objectMapper.
getCachedRules(TenantId tenantId) : lit depuis Redis la clé RedisKeyBuilder.rulesKey(tenantId). Si présente, désérialise la liste JSON en List<Rule>. Si absente, retourne Mono.empty().
cacheRules(TenantId tenantId, List<Rule> rules, Duration ttl) : sérialise la liste en JSON, stocke dans Redis avec la TTL fournie.
evictRules(TenantId tenantId) : supprime la clé Redis.
Ajoute dans RedisKeyBuilder.java : rulesKey(TenantId tenantId) retourne "loyalty:" + tenantId.value() + ":rules:active". counterKey(TenantId tenantId, UserId memberId, String counterKey) retourne "loyalty:" + tenantId.value() + ":counter:" + memberId.value() + ":" + counterKey".

Phase 11 — Kafka
Fichier 73 : infrastructure/kafka/producer/LoyaltyEventProducer.java
Annoté @Component. Implémente LoyaltyEventPublisherPort. Publie sur le topic loyalty.events.{tenantId}. Même pattern que WalletEventProducer — en cas d'échec, logue ERROR et retourne Mono.empty() sans propager l'exception.
Fichier 74 : infrastructure/kafka/config/LoyaltyKafkaTopicConfig.java
Annoté @Configuration. Déclare les beans NewTopic pour loyalty.events et loyalty.rewards.grant-requests (topic consommé par le futur module Rewards).

Phase 12 — Contrôleurs REST WebFlux
Fichier 75 : api/loyalty/dto/request/IncomingEventRequest.java
Record avec @NotBlank String eventType, @NotBlank String memberId, @NotNull Instant occurredAt, Map<String, Object> payload.
Fichier 76 : api/loyalty/dto/response/EventProcessingResponse.java
Record avec String eventId, List<AppliedEffectResponse> effectsApplied, List<String> notifications, Instant processedAt.
Record interne AppliedEffectResponse avec String effectType, String ruleName, Map<String, Object> details.
Méthode statique from(EventProcessingResult result).
Fichier 77 : api/loyalty/dto/request/CreateRuleRequest.java
Record avec @NotBlank String name, String description, @NotNull TriggerRequest trigger, @NotEmpty List<ConditionRequest> conditions, @NotEmpty List<EffectRequest> effects, int priority, Instant validFrom, Instant validUntil.
Records internes : TriggerRequest(String eventType, Map<String, Object> filters), ConditionRequest(String type, String operator, Object thresholdValue, String windowType, String counterKey), EffectRequest(String type, Map<String, Object> params).
Fichier 78 : api/loyalty/dto/response/RuleResponse.java
Record avec tous les champs d'une Rule. Méthode statique from(Rule rule).
Fichier 79 : api/loyalty/dto/response/PointsAccountResponse.java
Record avec long availablePoints, long lifetimeEarned, long lifetimeSpent, String tierLevel, BigDecimal tierMultiplier. Méthode statique from(PointsAccount account, MemberTier tier).
Fichier 80 : api/loyalty/EventController.java
RouterFunction WebFlux.
POST /api/v1/events — requiert header Idempotency-Key et JWT valide. Lit le TenantId depuis le TenantContextHolder. Construit un IncomingEvent depuis le body IncomingEventRequest et le TenantId. Appelle ProcessEventUseCase. Retourne EventProcessingResponse avec status 200.
Fichier 81 : api/loyalty/RuleController.java
POST /api/v1/admin/rules — requiert ROLE_TENANT_ADMIN et Idempotency-Key. Valide le body CreateRuleRequest. Convertit en objets domaine TriggerDefinition, ConditionDefinition, EffectDefinition. Appelle CreateRuleUseCase. Retourne RuleResponse avec status 201.
GET /api/v1/admin/rules — retourne Flux<RuleResponse> paginé avec query params page et size.
GET /api/v1/admin/rules/{ruleId} — retourne Mono<RuleResponse>.
PATCH /api/v1/admin/rules/{ruleId}/activate — appelle ActivateRuleUseCase. Retourne RuleResponse.
PATCH /api/v1/admin/rules/{ruleId}/suspend — suspend la règle et invalide le cache.
Fichier 82 : api/loyalty/MemberLoyaltyController.java
GET /api/v1/members/{memberId}/points — retourne PointsAccountResponse.
GET /api/v1/members/{memberId}/points/history — retourne Flux<PointsTransactionResponse> paginé.
GET /api/v1/members/{memberId}/tier — retourne MemberTierResponse.

Phase 13 — Migrations Liquibase
Fichier 83 : db/changelog/migrations/V003__create_loyalty_tables.sql
sql-- ============================================================
-- Rules table
-- ============================================================
CREATE TABLE IF NOT EXISTS rules (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL,
    name                VARCHAR(255) NOT NULL,
    description         TEXT,
    priority            INT NOT NULL DEFAULT 0,
    status              VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    trigger_definition  JSONB NOT NULL,
    conditions          JSONB NOT NULL DEFAULT '[]',
    effects             JSONB NOT NULL DEFAULT '[]',
    valid_from          TIMESTAMPTZ,
    valid_until         TIMESTAMPTZ,
    version             INT NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rules_tenant_status
    ON rules(tenant_id, status);
CREATE INDEX IF NOT EXISTS idx_rules_tenant_priority
    ON rules(tenant_id, priority DESC)
    WHERE status = 'ACTIVE';

-- ============================================================
-- Points accounts
-- ============================================================
CREATE TABLE IF NOT EXISTS points_accounts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id           UUID NOT NULL,
    member_id           UUID NOT NULL,
    available_points    BIGINT NOT NULL DEFAULT 0,
    lifetime_earned     BIGINT NOT NULL DEFAULT 0,
    lifetime_spent      BIGINT NOT NULL DEFAULT 0,
    version             BIGINT NOT NULL DEFAULT 0,
    last_activity_at    TIMESTAMPTZ,
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_points_member_tenant UNIQUE (member_id, tenant_id)
);

CREATE INDEX IF NOT EXISTS idx_points_member_tenant
    ON points_accounts(member_id, tenant_id);

-- ============================================================
-- Points transactions (immuables)
-- ============================================================
CREATE TABLE IF NOT EXISTS points_transactions (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    points_account_id       UUID NOT NULL REFERENCES points_accounts(id),
    tenant_id               UUID NOT NULL,
    type                    VARCHAR(50) NOT NULL,
    amount                  BIGINT NOT NULL,
    balance_after           BIGINT NOT NULL,
    source                  VARCHAR(100) NOT NULL,
    rule_id                 UUID,
    event_idempotency_key   VARCHAR(255),
    metadata                JSONB NOT NULL DEFAULT '{}',
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_points_txn_account
    ON points_transactions(points_account_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_points_txn_event_key
    ON points_transactions(event_idempotency_key)
    WHERE event_idempotency_key IS NOT NULL;

-- ============================================================
-- Counters
-- ============================================================
CREATE TABLE IF NOT EXISTS loyalty_counters (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    member_id       UUID NOT NULL,
    counter_key     VARCHAR(255) NOT NULL,
    value           BIGINT NOT NULL DEFAULT 0,
    window_type     VARCHAR(50),
    window_start    TIMESTAMPTZ,
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_counter_member_key UNIQUE (tenant_id, member_id, counter_key)
);

CREATE INDEX IF NOT EXISTS idx_counter_member_tenant
    ON loyalty_counters(tenant_id, member_id);

-- ============================================================
-- Member tiers
-- ============================================================
CREATE TABLE IF NOT EXISTS member_tiers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    member_id       UUID NOT NULL,
    tier_level      VARCHAR(50) NOT NULL DEFAULT 'BRONZE',
    multiplier      DECIMAL(4,2) NOT NULL DEFAULT 1.0,
    reached_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    valid_until     TIMESTAMPTZ,
    CONSTRAINT uq_tier_member_tenant UNIQUE (member_id, tenant_id)
);

CREATE INDEX IF NOT EXISTS idx_tier_member_tenant
    ON member_tiers(member_id, tenant_id);

-- ============================================================
-- Tier policies
-- ============================================================
CREATE TABLE IF NOT EXISTS tier_policies (
    id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id                   UUID NOT NULL UNIQUE,
    criterion                   VARCHAR(100) NOT NULL DEFAULT 'LIFETIME_POINTS',
    thresholds                  JSONB NOT NULL DEFAULT '[]',
    maintain_period             VARCHAR(50) NOT NULL DEFAULT 'QUARTERLY',
    maintain_threshold_points   BIGINT NOT NULL DEFAULT 0,
    downgrade_grace_days        INT NOT NULL DEFAULT 30,
    created_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
Mettre à jour db.changelog-master.xml
Ajoute <include file="migrations/V003__create_loyalty_tables.sql" relativeToChangelogFile="true"/>.

Phase 14 — Configuration Spring
Fichier 84 : infrastructure/config/LoyaltyConfig.java
Annoté @Configuration. Déclare les beans Java purs du domaine.
java@Bean
public List<ConditionEvaluator> conditionEvaluators() {
    return List.of(
        new CumulativeCountEvaluator(),
        new CumulativeAmountEvaluator(),
        new PointsBalanceEvaluator(),
        new TierEvaluator(),
        new TimeWindowEvaluator(),
        new FirstEventEvaluator()
    );
}

@Bean
public List<EffectExecutor> effectExecutors(
        CreditWalletUseCase creditWalletUseCase) {
    return List.of(
        new CreditPointsExecutor(),
        new CreditWalletExecutor(creditWalletUseCase),
        new GrantRewardExecutor(),
        new ResetCounterExecutor(),
        new UpdateTierExecutor(),
        new SendNotificationExecutor()
    );
}

@Bean
public RuleEngine ruleEngine(
        List<ConditionEvaluator> conditionEvaluators,
        List<EffectExecutor> effectExecutors) {
    return new RuleEngine(conditionEvaluators, effectExecutors);
}

@Bean
public CounterService counterService(CounterRepository counterRepo) {
    return new CounterService(counterRepo);
}

@Bean
public TierCalculationService tierCalculationService(...) {
    return new TierCalculationService(...);
}

@Bean
public LoyaltyDomainService loyaltyDomainService(
        RuleEngine ruleEngine,
        CounterService counterService,
        TierCalculationService tierCalcService,
        RuleRepository ruleRepo,
        PointsAccountRepository pointsRepo,
        PointsTransactionRepository pointsTxRepo,
        CounterRepository counterRepo,
        MemberTierRepository tierRepo,
        TierPolicyRepository tierPolicyRepo,
        RuleCachePort ruleCache,
        LoyaltyEventPublisherPort eventPublisher,
        CreditWalletUseCase creditWalletUseCase) {
    return new LoyaltyDomainService(...);
}

Phase 15 — Tests
Fichier 85 : test/domain/loyalty/RuleEngineTest.java
Test JUnit 5 sans Spring. Crée les instances directement.
Setup : crée une liste d'évaluateurs et d'exécuteurs réels. Crée une règle active avec un trigger purchase.completed, une condition CUMULATIVE_COUNT >= 10, et un effet CREDIT_POINTS {amount: 500}.
Test 1 — Trigger non matché : envoie un event trip.ended. Vérifie que la règle est skipped et qu'aucun effet n'est planifié.
Test 2 — Conditions non remplies : envoie un event purchase.completed avec un compteur à 5. Vérifie que les conditions échouent et qu'aucun effet n'est planifié.
Test 3 — Règle déclenchée : envoie un event purchase.completed avec un compteur à 10. Vérifie que effectsApplied contient un effet CREDIT_POINTS avec amount 500.
Test 4 — Multiplicateur de palier : même scénario mais avec un MemberTier SILVER (multiplicateur 1.5). Vérifie que les points crédités sont 750 (500 × 1.5).
Test 5 — Effet MULTIPLY_POINTS : crée une règle prioritaire haute avec effet MULTIPLY_POINTS {multiplier: 2} et une règle normale avec CREDIT_POINTS {amount: 500}. Vérifie que les points finaux sont 1000.
Test 6 — TimeWindowEvaluator : crée une règle avec condition TIME_WINDOW active uniquement le vendredi. Envoie un event un lundi. Vérifie que la règle est skipped.
Test 7 — Priorité d'évaluation : crée trois règles avec priorités 10, 5, 1. Vérifie que process() les évalue dans l'ordre 10, 5, 1.
Fichier 86 : test/domain/loyalty/PointsAccountTest.java
Test JUnit 5 sans Spring.
Test 1 : earn(500) met à jour availablePoints et lifetimeEarned. Test 2 : spend(200) avec solde suffisant met à jour correctement. Test 3 : spend(1000) avec solde 200 lance LoyaltyDomainException. Test 4 : expire(100) soustrait de availablePoints sans affecter lifetimeSpent.
Fichier 87 : test/domain/loyalty/TierPolicyTest.java
Test JUnit 5 sans Spring. Crée une politique avec BRONZE=0, SILVER=1000, GOLD=5000, PLATINUM=20000.
Test 1 : calculateTier(0) retourne BRONZE. Test 2 : calculateTier(999) retourne BRONZE. Test 3 : calculateTier(1000) retourne SILVER. Test 4 : calculateTier(20000) retourne PLATINUM.
Fichier 88 : test/domain/loyalty/LoyaltyDomainServiceTest.java
Test JUnit 5 sans Spring. Utilise des fakes en mémoire pour tous les ports (inner classes dans le test file, pas de Mockito).
Fake InMemoryRuleRepository : liste en mémoire, méthode add() pour setup.
Fake InMemoryPointsAccountRepository : map en mémoire.
Fake InMemoryCounterRepository : map en mémoire.
Fake InMemoryCapturedEvents implémentant LoyaltyEventPublisherPort : accumule les événements publiés pour assertions.
Test 1 — Traitement complet : configure une règle purchase.completed avec condition CUMULATIVE_COUNT >= 3 et effet CREDIT_POINTS {amount: 100}. Envoie 3 événements pour le même membre. Vérifie que le 3ème événement crédite 100 points et que le PointsAccount a 100 points.
Test 2 — Idempotence du domaine : envoie le même événement deux fois (même idempotencyKey). Vérifie que les points ne sont crédités qu'une seule fois.
Test 3 — Compteur incrémenté même si condition non remplie : envoie 2 événements pour une règle à seuil 10. Vérifie que le compteur est à 2 mais qu'aucun point n'est crédité.
Test 4 — Reset de compteur : configure une règle avec CREDIT_POINTS et RESET_COUNTER. Envoie 10 événements. Vérifie que le compteur est remis à 0 après le 10ème.
Test 5 — Multiplicateur de palier : configure un MemberTier GOLD (multiplicateur 2.0). Envoie un event qui déclenche CREDIT_POINTS {amount: 500}. Vérifie que les points crédités sont 1000.
Fichier 89 : test/integration/loyalty/EventControllerIntegrationTest.java
Test @SpringBootTest(webEnvironment = RANDOM_PORT), @ActiveProfiles("stub"), @Import(TestContainersConfig.class). Démarre PostgreSQL et Redis.
@BeforeEach : insère en base une règle active avec trigger purchase.completed, condition CUMULATIVE_COUNT >= 3, effet CREDIT_POINTS {amount: 100}.
Test 1 : POST /api/v1/events avec body {eventType: "purchase.completed", memberId: "member-123", occurredAt: now, payload: {amount: 5000}} et Idempotency-Key: evt-001 → 200. Vérifie que effectsApplied est vide (compteur à 1, seuil = 3).
Test 2 : Même appel avec Idempotency-Key: evt-001 → 200, réponse identique, compteur toujours à 1 (idempotence).
Test 3 : Deux autres events avec clés evt-002 et evt-003. Vérifie que le 3ème retourne effectsApplied avec un effet CREDIT_POINTS.
Test 4 : GET /api/v1/members/member-123/points → 200 avec availablePoints: 100.
Test 5 : POST /api/v1/admin/rules avec une nouvelle règle valide → 201 avec le JSON de la règle créée en DRAFT. Vérifie que le cache des règles est invalidé.
Test 6 : PATCH /api/v1/admin/rules/{ruleId}/activate → 200 avec status ACTIVE.

Ordre d'implémentation recommandé
Jour 1 : Phase 1 complète (tous les modèles) + Phase 2 (contexte d'évaluation). Exécute mvn compile. Doit passer sans erreur.
Jour 2 : Phase 3 (évaluateurs de conditions) + Phase 4 (exécuteurs d'effets). Ces classes sont toutes des Java purs testables immédiatement. Exécute le test RuleEngineTest — doit passer entièrement.
Jour 3 : Phase 5 (RuleEngine, CounterService, TierCalculationService) + Phase 6 (ports) + test LoyaltyDomainServiceTest avec fakes. Le cœur métier est complet et testé.
Jour 4 : Phase 13 (migration SQL) + Phase 9 (entités R2DBC et repositories). L'application démarre avec les nouvelles tables.
Jour 5 : Phase 10 (cache Redis) + Phase 11 (Kafka) + Phase 7 (LoyaltyDomainService complet) + Phase 14 (configuration Spring). Tous les beans sont connectés.
Jour 6 : Phase 8 (handlers applicatifs) + Phase 12 (contrôleurs REST). Les endpoints sont exposés.
Jour 7 : Phase 15 complète (tests d'intégration). Les 6 scénarios passent avec Testcontainers.

Vérifications finales
Vérification 1 — mvn test -Dtest=HexagonalArchitectureTest : toutes les règles ArchUnit passent. En particulier aucun fichier du domaine loyalty n'importe Spring, et RuleEngine n'a aucune dépendance vers infrastructure.
Vérification 2 — mvn test -Dtest="RuleEngineTest,PointsAccountTest,TierPolicyTest" : tests domaine purs, sans Spring, sans base.
Vérification 3 — mvn test -Dtest=LoyaltyDomainServiceTest : test du service domaine avec fakes, sans Spring.
Vérification 4 — mvn test -Dtest=EventControllerIntegrationTest : les 6 scénarios d'intégration passent.
Vérification 5 — mvn test : zéro régression sur les tests wallet existants.
Vérification 6 — Démarrage manuel et séquence Postman : créer une règle via POST /api/v1/admin/rules, l'activer via PATCH .../activate, envoyer 3 events via POST /api/v1/events, vérifier que le 3ème retourne des points crédités, vérifier le solde via GET /api/v1/members/{id}/points.
