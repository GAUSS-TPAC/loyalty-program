Instructions complètes — Préparation à l'intégration du Kernel Core

Contexte obligatoire à lire avant toute action
Tu travailles sur le backend du Loyalty Programme, un SaaS multi-tenant de fidélisation. Ce projet fait partie de l'écosystème Yowyob et sera connecté au Kernel Core — le noyau partagé de l'écosystème. Le Kernel Core n'est pas encore disponible. Ton travail consiste à préparer l'architecture pour l'accueillir proprement quand il arrivera, sans bloquer le développement actuel.
Le projet est construit avec Spring Boot 3.4, Spring WebFlux (tout est réactif — .block() est interdit), R2DBC pour PostgreSQL, Redis pour le cache, Kafka pour les événements. L'architecture est hexagonale stricte avec DDD. Le package racine est com.yowyob.loyalty.
Ce que le Kernel Core fournira quand il sera disponible :
La gestion des tenants et organisations, l'authentification via YowAuth0, la vérification d'identité via Smart KYC, les paiements via Payment API partagé.
Ce que tu dois faire maintenant :
Restructurer le code pour que tout ce qui vient du Kernel Core soit derrière des ports et des stubs. Quand le Kernel Core arrivera, on remplacera uniquement les stubs par de vrais adapters — sans toucher au domaine ni aux handlers.

Règle absolue inchangée
Aucun fichier dans domain/ ne doit importer quoi que ce soit de Spring, R2DBC, Kafka, Redis ou toute librairie externe. Cette règle s'applique à tous les nouveaux fichiers créés dans cette session.

Phase 1 — Suppressions
Exécute ces suppressions dans l'ordre. Avant de supprimer chaque fichier, vérifie dans ton IDE ou via grep -r "NomDuFichier" src/ qu'il n'est pas importé ailleurs. Si des imports existent, note-les pour les corriger dans les phases suivantes.

Supprimer le dossier complet :
src/main/java/com/yowyob/loyalty/domain/tenant/
Ce dossier contient Tenant.java, TenantConfig.java, TenantStatus.java, TenantPlan.java et TenantRepository.java. Le Kernel Core sera la seule source de vérité pour les tenants. Notre domaine ne modélise plus cette entité — il reçoit les informations du tenant via les claims JWT et des ports dédiés.

Supprimer le dossier complet :
src/main/java/com/yowyob/loyalty/infrastructure/persistence/tenant/
Ce dossier contient TenantEntity.java, TenantR2dbcRepository.java, TenantRepositoryAdapter.java et TenantMapper.java. La persistance des tenants dans notre base n'a plus lieu d'être.

Supprimer le dossier complet :
src/main/java/com/yowyob/loyalty/infrastructure/payment/
Ce dossier contient mtn/, orange/ et stripe/ avec leurs adapters, clients et DTOs. Les paiements seront délégués au Payment API du Kernel Core. Pour l'instant on les remplace par des stubs.

Supprimer ces fichiers de migration :
src/main/resources/db/changelog/migrations/V001__create_public_tenants_table.sql
src/main/resources/db/changelog/migrations/V002__create_api_keys_table.sql
Ces tables vivaient dans notre base parce qu'on gérait les tenants nous-mêmes. Ce n'est plus le cas.

Supprimer ces deux fichiers de sécurité :
src/main/java/com/yowyob/loyalty/shared/security/JwtTokenValidator.java
src/main/java/com/yowyob/loyalty/shared/security/JwtValidationResult.java
Ces fichiers implémentaient une validation JWT maison. YowAuth0 sera le seul émetteur de tokens. Spring Security Resource Server gérera la validation nativement contre YowAuth0 via la configuration issuer-uri. Notre implémentation maison sera remplacée par la configuration Spring Security standard.

Phase 2 — Créations
Crée tous ces fichiers dans l'ordre exact. Chaque groupe est indépendant mais respecte l'ordre général domaine → infrastructure → shared → config → tests.

Groupe A — Modèles du domaine manquants
Ces fichiers complètent le domaine wallet avec les concepts liés aux paiements et au KYC. Zéro annotation Spring. Zéro import externe.

Fichier A1 : src/main/java/com/yowyob/loyalty/domain/wallet/model/PaymentStatus.java
Enum Java pur. Constantes : PENDING, COMPLETED, FAILED, CANCELLED, EXPIRED. Ajoute une méthode isFinal() qui retourne this == COMPLETED || this == FAILED || this == CANCELLED || this == EXPIRED. Ajoute une méthode isSuccessful() qui retourne this == COMPLETED.

Fichier A2 : src/main/java/com/yowyob/loyalty/domain/wallet/model/PaymentInitiationResult.java
Record Java 21 immuable. Champs : String externalRef représentant la référence de la transaction chez le provider, PaymentStatus status, String redirectUrl pouvant être null utilisé pour Stripe, String ussdCode pouvant être null utilisé pour MTN et Orange, Instant expiresAt représentant la date d'expiration de la demande de paiement. Méthode requiresUserAction() retourne ussdCode != null || redirectUrl != null.

Fichier A3 : src/main/java/com/yowyob/loyalty/domain/wallet/model/KycStatus.java
Enum Java pur. Constantes : NOT_STARTED, PENDING_REVIEW, VERIFIED, REJECTED. Méthode allowsWithdrawal() retourne this == VERIFIED.

Groupe B — Ports de sortie du domaine vers le futur Kernel Core
Ces interfaces déclarent ce que notre domaine a besoin du monde extérieur. Elles vivent dans domain/ donc zéro annotation Spring.

Fichier B1 : src/main/java/com/yowyob/loyalty/domain/shared/port/out/TenantQueryPort.java
Interface dans le domaine partagé. Déclare les trois informations dont notre logique métier a besoin sur un tenant — et uniquement ces trois. Méthode Mono<Boolean> tenantExists(TenantId tenantId). Méthode Mono<String> getTenantCurrencyCode(TenantId tenantId). Méthode Mono<Integer> getTenantMaxRules(TenantId tenantId). Rien d'autre — le reste des données tenant vient du JWT.

Fichier B2 : src/main/java/com/yowyob/loyalty/domain/wallet/port/out/PaymentGatewayPort.java
Interface dans le domaine wallet. Méthodes : Mono<PaymentInitiationResult> initiateTopUp(TenantId tenantId, UserId memberId, BigDecimal amount, String currency, String provider, String idempotencyKey). Mono<PaymentStatus> getPaymentStatus(TenantId tenantId, String externalRef). Mono<PaymentInitiationResult> initiateWithdrawal(TenantId tenantId, UserId memberId, BigDecimal amount, String currency, String targetAccount, String provider, String idempotencyKey). Mono<Void> cancelPayment(TenantId tenantId, String externalRef).

Fichier B3 : src/main/java/com/yowyob/loyalty/domain/wallet/port/out/KycVerificationPort.java
Interface dans le domaine wallet. Méthodes : Mono<Boolean> isMemberVerified(TenantId tenantId, UserId memberId). Mono<KycStatus> getMemberKycStatus(TenantId tenantId, UserId memberId).

Groupe C — TenantContext reécrit
Le TenantContext existait mais dépendait de TenantStatus et TenantPlan du domaine tenant qu'on vient de supprimer. Il doit être réécrit pour être autonome.

Fichier C1 : Réécrire src/main/java/com/yowyob/loyalty/shared/multitenancy/TenantContext.java
Supprime l'ancien contenu entièrement. Recrée comme record Java 21 immuable avec ces champs. TenantId tenantId. String tenantName. String tenantStatus sous forme de String brute venant des claims JWT — on ne possède plus l'enum puisqu'on ne gère plus le cycle de vie du tenant. String tenantPlan sous forme de String brute. String organizationId représentant l'identifiant de l'organisation dans le Kernel Core pouvant être identique au tenantId. Méthode isActive() retourne "ACTIVE".equalsIgnoreCase(tenantStatus). Méthode isSuspended() retourne "SUSPENDED".equalsIgnoreCase(tenantStatus). Méthode statique fromClaims(Map<String, Object> claims, String tenantIdClaim, String orgNameClaim, String statusClaim, String planClaim, String orgIdClaim) qui construit le record depuis les claims JWT en gérant les valeurs nulles avec des defaults raisonnables.

Groupe D — Stubs pour développement sans Kernel Core
Ces stubs permettent au projet de fonctionner et d'être testé entièrement sans que le Kernel Core soit disponible. Ils sont activés uniquement avec le profil Spring stub.

Fichier D1 : src/main/java/com/yowyob/loyalty/infrastructure/stub/TenantQueryStub.java
Annotée @Component et @Profile("stub"). Implémente TenantQueryPort. La méthode tenantExists() retourne toujours Mono.just(true). La méthode getTenantCurrencyCode() retourne Mono.just("XAF"). La méthode getTenantMaxRules() retourne Mono.just(50). Ajoute un logger et logue au niveau WARN au démarrage du composant via @PostConstruct : "[STUB] TenantQueryStub actif — données factices utilisées. Remplacer par KernelCoreTenantAdapter en production.".

Fichier D2 : src/main/java/com/yowyob/loyalty/infrastructure/stub/PaymentGatewayStub.java
Annotée @Component et @Profile("stub"). Implémente PaymentGatewayPort. La méthode initiateTopUp() génère un externalRef aléatoire via UUID.randomUUID().toString(), retourne un PaymentInitiationResult avec status PENDING, ussdCode = "*126*1*" + montant + "#" (simule un code USSD MTN), et expiresAt = Instant.now().plusSeconds(300). La méthode getPaymentStatus() simule une confirmation automatique — retourne Mono.just(PaymentStatus.COMPLETED) après un délai de 1 seconde via Mono.delay(Duration.ofSeconds(1)).thenReturn(PaymentStatus.COMPLETED). La méthode initiateWithdrawal() retourne un résultat PENDING similaire. La méthode cancelPayment() retourne Mono.empty(). Logue un WARN au démarrage.

Fichier D3 : src/main/java/com/yowyob/loyalty/infrastructure/stub/KycVerificationStub.java
Annotée @Component et @Profile("stub"). Implémente KycVerificationPort. La méthode isMemberVerified() retourne toujours Mono.just(true) — en mode stub, tous les membres sont vérifiés. La méthode getMemberKycStatus() retourne Mono.just(KycStatus.VERIFIED). Logue un WARN au démarrage.

Groupe E — Nouvelles migrations Liquibase
Notre base de données contient uniquement les données de fidélisation. Les UUIDs des tenants et membres sont des références externes provenant du Kernel Core — on les référence sans les posséder.

Fichier E1 : src/main/resources/db/changelog/migrations/V001__create_wallet_tables.sql
Contenu exact :
sql-- ============================================================
-- Wallet tables
-- Créées dans le schéma courant (per-tenant via search_path)
-- Les tenant_id et member_id sont des références externes
-- provenant du Kernel Core — pas de FK vers une table locale
-- ============================================================

CREATE TABLE IF NOT EXISTS wallets (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    member_id       UUID        NOT NULL,
    tenant_id       UUID        NOT NULL,
    balance         DECIMAL(18,4) NOT NULL DEFAULT 0,
    currency_code   VARCHAR(10) NOT NULL DEFAULT 'XAF',
    status          VARCHAR(50) NOT NULL DEFAULT 'PENDING_KYC',
    version         BIGINT      NOT NULL DEFAULT 0,
    frozen_at       TIMESTAMPTZ,
    frozen_reason   TEXT,
    closed_at       TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_by      VARCHAR(255),
    updated_by      VARCHAR(255),
    CONSTRAINT uq_wallet_member_tenant UNIQUE (member_id, tenant_id)
);

CREATE INDEX IF NOT EXISTS idx_wallets_member_tenant
    ON wallets(member_id, tenant_id);
CREATE INDEX IF NOT EXISTS idx_wallets_status
    ON wallets(status);
CREATE INDEX IF NOT EXISTS idx_wallets_tenant
    ON wallets(tenant_id);

-- ============================================================

CREATE TABLE IF NOT EXISTS wallet_transactions (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id       UUID        NOT NULL REFERENCES wallets(id),
    type            VARCHAR(50) NOT NULL,
    amount          DECIMAL(18,4) NOT NULL,
    balance_before  DECIMAL(18,4) NOT NULL,
    balance_after   DECIMAL(18,4) NOT NULL,
    status          VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    source          VARCHAR(100) NOT NULL,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    reference_id    UUID,
    reversal_of     UUID        REFERENCES wallet_transactions(id),
    metadata        JSONB       NOT NULL DEFAULT '{}',
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_wallet_txn_wallet_created
    ON wallet_transactions(wallet_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_wallet_txn_idempotency
    ON wallet_transactions(idempotency_key);
CREATE INDEX IF NOT EXISTS idx_wallet_txn_status
    ON wallet_transactions(status);
CREATE INDEX IF NOT EXISTS idx_wallet_txn_type
    ON wallet_transactions(type);

-- ============================================================

CREATE TABLE IF NOT EXISTS payment_requests (
    id                      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_transaction_id   UUID        REFERENCES wallet_transactions(id),
    external_ref            VARCHAR(255) NOT NULL UNIQUE,
    provider                VARCHAR(50) NOT NULL,
    direction               VARCHAR(20) NOT NULL,
    real_amount             DECIMAL(18,4) NOT NULL,
    real_currency           VARCHAR(10) NOT NULL,
    virtual_amount          DECIMAL(18,4) NOT NULL,
    exchange_rate           DECIMAL(10,6) NOT NULL DEFAULT 1.0,
    status                  VARCHAR(50) NOT NULL DEFAULT 'INITIATED',
    initiated_at            TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    confirmed_at            TIMESTAMPTZ,
    expires_at              TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_payment_req_external_ref
    ON payment_requests(external_ref);
CREATE INDEX IF NOT EXISTS idx_payment_req_status
    ON payment_requests(status);

-- ============================================================

CREATE TABLE IF NOT EXISTS wallet_audit_logs (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id   UUID        NOT NULL REFERENCES wallets(id),
    action      VARCHAR(100) NOT NULL,
    actor       VARCHAR(255) NOT NULL,
    reason      TEXT,
    metadata    JSONB       NOT NULL DEFAULT '{}',
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_wallet_audit_wallet_created
    ON wallet_audit_logs(wallet_id, created_at DESC);

Fichier E2 : Mettre à jour src/main/resources/db/changelog/db.changelog-master.xml
Remplace le contenu entier par ceci :
xml<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
    http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <include file="migrations/V001__create_wallet_tables.sql"
             relativeToChangelogFile="true"/>

</databaseChangeLog>

Groupe F — Mise à jour de la sécurité
La validation JWT maison est supprimée. Spring Security Resource Server gère ça nativement via issuer-uri. Il faut adapter les composants qui utilisaient JwtTokenValidator.

Fichier F1 : src/main/java/com/yowyob/loyalty/shared/security/YowAuth0ClaimsExtractor.java
Ce fichier remplace et étend JwtClaimsExtractor. Annotée @Component. Injecte JwtProperties. Déclare toutes les méthodes suivantes.
Méthode extractAllClaims(String rawToken) retourne Map<String, Object>. Décode le token sans vérifier la signature uniquement pour lire les claims rapidement. Utilise com.nimbusds.jwt.SignedJWT.parse(rawToken).getJWTClaimsSet().toJSONObject(). Encapsule dans un try-catch et retourne une Map vide en cas d'erreur de parsing.
Méthode extractTenantId(Map<String, Object> claims) retourne Optional<TenantId>. Lit claims.get(properties.getTenantIdClaim()). Si null, essaie aussi claims.get("organization_id") comme fallback car YowAuth0 peut utiliser ce nom. Retourne Optional.empty() si toujours null.
Méthode extractUserId(Map<String, Object> claims) retourne Optional<UserId>. Lit le claim sub.
Méthode extractRoles(Map<String, Object> claims) retourne Set<String>. YowAuth0 Keycloak structure les rôles dans realm_access.roles. Gère les deux formats : format Keycloak imbriqué {"realm_access": {"roles": [...]}} et format plat {"roles": [...]}. Si le claim est une String séparée par espaces, découpe et retourne un Set.
Méthode extractTenantName(Map<String, Object> claims) retourne String pouvant être null. Lit claims.get("org_name").
Méthode extractTenantStatus(Map<String, Object> claims) retourne String avec valeur par défaut "ACTIVE". Lit claims.get("org_status").
Méthode extractTenantPlan(Map<String, Object> claims) retourne String avec valeur par défaut "FREE". Lit claims.get("org_plan").
Méthode extractOrganizationId(Map<String, Object> claims) retourne String pouvant être null. Lit claims.get("organization_id").

Groupe G — ErrorCodes supplémentaires
Ouvre src/main/java/com/yowyob/loyalty/shared/exception/ErrorCode.java. Ajoute ces constantes à la fin de l'enum sans toucher aux existantes :
javaKERNEL_CORE_UNAVAILABLE(503),
KERNEL_CORE_TENANT_NOT_FOUND(404),
KERNEL_CORE_PAYMENT_FAILED(502),
KERNEL_CORE_KYC_UNAVAILABLE(503),
KERNEL_CORE_AUTH_FAILED(502),
KERNEL_CORE_CIRCUIT_OPEN(503),
PAYMENT_PROVIDER_UNAVAILABLE(503),
PAYMENT_INITIATION_FAILED(422),
KYC_VERIFICATION_REQUIRED(403),
KYC_VERIFICATION_FAILED(422)

Fichier G1 : src/main/java/com/yowyob/loyalty/shared/exception/KernelCoreException.java
Hérite de AppException. Constructeur public KernelCoreException(ErrorCode errorCode, String detail) appelle super(errorCode, detail). Constructeur supplémentaire public KernelCoreException(ErrorCode errorCode, String detail, Throwable cause) pour encapsuler les exceptions WebClient.

Fichier G2 : src/main/java/com/yowyob/loyalty/shared/exception/PaymentException.java
Hérite de AppException. Constructeur avec ErrorCode et String detail.

Fichier G3 : src/main/java/com/yowyob/loyalty/shared/exception/KycException.java
Hérite de AppException. Constructeur avec ErrorCode et String detail.

Groupe H — Placeholder pour le futur Kernel Core Adapter
Ces fichiers sont des squelettes vides qui montrent exactement où les vrais adapters seront créés quand le Kernel Core sera disponible. Ils contiennent uniquement des TODOs et des commentaires — pas d'implémentation réelle.

Fichier H1 : src/main/java/com/yowyob/loyalty/infrastructure/kernelcore/adapter/KernelCoreTenantAdapter.java
Annotée @Component et @Profile("!stub") — actif uniquement quand le profil stub n'est PAS actif. Implémente TenantQueryPort. Chaque méthode contient uniquement throw new UnsupportedOperationException("TODO: implémenter quand le Kernel Core sera disponible") suivi d'un commentaire détaillé expliquant quel endpoint du Kernel Core appeler. Par exemple pour tenantExists() : commentaire // TODO: GET {KERNEL_CORE_URL}/api/v1/organizations/{tenantId} — retourne true si status == ACTIVE. Cela fait compiler le projet et indique clairement le travail à faire.

Fichier H2 : src/main/java/com/yowyob/loyalty/infrastructure/kernelcore/adapter/KernelCorePaymentAdapter.java
Même principe. Annotée @Component et @Profile("!stub"). Implémente PaymentGatewayPort. Chaque méthode throw UnsupportedOperationException avec un commentaire expliquant l'endpoint Kernel Core à appeler.

Fichier H3 : src/main/java/com/yowyob/loyalty/infrastructure/kernelcore/adapter/SmartKycAdapter.java
Même principe. Annotée @Component et @Profile("!stub"). Implémente KycVerificationPort.

Fichier H4 : src/main/java/com/yowyob/loyalty/infrastructure/kernelcore/KernelCoreProperties.java
Annotée @ConfigurationProperties(prefix = "app.kernel-core") et @Component. Champs avec getters/setters : String baseUrl défaut "http://localhost:8090", String serviceClientId défaut "loyalty-service", String serviceClientSecret défaut "changeme", String tokenEndpoint défaut vide, int connectTimeoutMs défaut 3000, int readTimeoutMs défaut 5000. Ce fichier sera utilisé quand on implémentera les vrais adapters.

Phase 3 — Modifications de fichiers existants
Modifie ces fichiers dans l'ordre indiqué. Pour chaque fichier, remplace uniquement ce qui est décrit — ne touche pas au reste.

Modification 1 : TenantResolutionFilter.java
Ce fichier change significativement. La logique de chargement depuis la base de données est remplacée par une lecture depuis les claims JWT.
Retire les injections de TenantCacheAdapter et TenantRepositoryAdapter du constructeur.
Ajoute l'injection de YowAuth0ClaimsExtractor claimsExtractor via le constructeur.
Remplace la méthode filter() entièrement par cette logique :
Vérifier si le path est dans la liste blanche publique. Si oui, appeler directement chain.filter(exchange) sans aucun traitement.
Extraire le header Authorization. Si absent, retourner 401 avec le body Problem Details contenant errorCode = JWT_MISSING et detail = "Token d'authentification requis".
Extraire le Bearer token en retirant le préfixe "Bearer ". Si le token ne commence pas par "Bearer ", retourner 401 avec JWT_INVALID.
Appeler claimsExtractor.extractAllClaims(rawToken) pour décoder le token sans vérifier la signature. Cette vérification est déjà faite par Spring Security Resource Server — ce filtre extrait uniquement les claims pour construire le TenantContext.
Appeler claimsExtractor.extractTenantId(claims). Si Optional vide, retourner 401 avec MISSING_TENANT_CLAIM.
Extraire tenantStatus via claimsExtractor.extractTenantStatus(claims). Si la valeur est "SUSPENDED", retourner 403 avec TENANT_SUSPENDED. Si la valeur est "PENDING_SETUP", retourner 403 avec TENANT_NOT_READY.
Construire le TenantContext via TenantContext.fromClaims(claims, ...) en utilisant les méthodes de YowAuth0ClaimsExtractor.
Appeler chain.filter(exchange).contextWrite(TenantContextHolder.withTenantContext(tenantContext)).

Modification 2 : JwtProperties.java
Ajoute ces champs avec getters et setters sans modifier les existants :
javaprivate String orgNameClaim = "org_name";
private String orgStatusClaim = "org_status";
private String orgPlanClaim = "org_plan";
private String organizationIdClaim = "organization_id";

Modification 3 : SecurityConfig.java
Retire la référence à JwtTokenValidator si elle existe. Spring Security Resource Server gère maintenant la validation JWT nativement via la configuration issuer-uri dans application.yml. Le bean SecurityWebFilterChain reste identique pour tout le reste.

Modification 4 : application.yml
Ajoute ces sections sans modifier l'existant. Place-les sous la section app: existante :
yamlapp:
  kernel-core:
    base-url: ${KERNEL_CORE_URL:http://localhost:8090}
    service-client-id: ${KERNEL_SERVICE_CLIENT_ID:loyalty-service}
    service-client-secret: ${KERNEL_SERVICE_CLIENT_SECRET:changeme}
    token-endpoint: ${KERNEL_TOKEN_ENDPOINT:}
    connect-timeout-ms: 3000
    read-timeout-ms: 5000

  security:
    jwt:
      # Noms des claims YowAuth0
      tenant-id-claim: organization_id
      org-name-claim: org_name
      org-status-claim: org_status
      org-plan-claim: org_plan
      roles-claim: realm_access.roles
      organization-id-claim: organization_id

Modification 5 : application-dev.yml
Ajoute l'activation du profil stub pour le développement local sans Kernel Core :
yamlspring:
  profiles:
    active: dev, stub

Modification 6 : application-test.yml
Ajoute l'activation du profil stub pour les tests :
yamlspring:
  profiles:
    active: test, stub

Modification 7 : HexagonalArchitectureTest.java
Ajoute ces trois nouvelles règles ArchUnit à la suite des règles existantes. Ne modifie aucune règle existante.
java@ArchTest
static final ArchRule stubs_only_in_infrastructure =
    classes()
        .that().haveSimpleNameEndingWith("Stub")
        .should().resideInAPackage("..infrastructure.stub..")
        .because("Les stubs doivent être dans infrastructure/stub " +
                 "pour être facilement identifiables et remplaçables");

@ArchTest
static final ArchRule kernel_core_placeholders_implement_ports =
    classes()
        .that().resideInAPackage("..infrastructure.kernelcore.adapter..")
        .and().haveSimpleNameEndingWith("Adapter")
        .should().implement(
            resideInAPackage("..domain..port.out..")
        )
        .because("Les adapters Kernel Core doivent implémenter " +
                 "les ports du domaine");

@ArchTest
static final ArchRule no_direct_payment_provider_calls_from_domain =
    noClasses()
        .that().resideInAPackage("..domain..")
        .should().dependOnClassesThat()
        .haveSimpleNameContaining("Mtn")
        .orShould().dependOnClassesThat()
        .haveSimpleNameContaining("Orange")
        .orShould().dependOnClassesThat()
        .haveSimpleNameContaining("Stripe")
        .because("Le domaine ne connaît pas les providers de paiement");

Phase 4 — Tests à créer

Fichier T1 : src/test/java/com/yowyob/loyalty/shared/security/YowAuth0ClaimsExtractorTest.java
Test JUnit 5 sans Spring. Instancie YowAuth0ClaimsExtractor directement avec un JwtProperties configuré manuellement. Crée des Maps de claims simulant les formats YowAuth0. Teste extractTenantId() avec claim organization_id présent. Teste extractTenantId() avec claim absent — doit retourner Optional vide sans exception. Teste extractRoles() avec format Keycloak imbriqué {"realm_access": {"roles": ["ROLE_TENANT_ADMIN"]}}. Teste extractRoles() avec format plat. Teste extractTenantStatus() avec valeur présente et avec valeur absente — doit retourner "ACTIVE" par défaut.

Fichier T2 : src/test/java/com/yowyob/loyalty/shared/multitenancy/TenantContextTest.java
Test JUnit 5 sans Spring. Teste TenantContext.fromClaims() avec une Map complète — vérifie tous les champs. Teste fromClaims() avec une Map vide — vérifie que les defaults sont raisonnables et qu'aucune NullPointerException n'est lancée. Teste isActive() avec statut "ACTIVE" — doit retourner true. Teste isActive() avec statut "SUSPENDED" — doit retourner false. Teste isSuspended().

Fichier T3 : src/test/java/com/yowyob/loyalty/infrastructure/stub/StubsTest.java
Test JUnit 5 avec @SpringBootTest et @ActiveProfiles("stub"). Vérifie que les trois stubs sont bien chargés par Spring dans le profil stub. Vérifie que TenantQueryStub.tenantExists() retourne true. Vérifie que PaymentGatewayStub.initiateTopUp() retourne un résultat avec status PENDING. Vérifie que KycVerificationStub.isMemberVerified() retourne true. Utilise StepVerifier de Reactor Test pour les assertions sur les Mono.

Fichier T4 : Mettre à jour src/test/java/com/yowyob/loyalty/shared/multitenancy/TenantResolutionFilterTest.java
Retire tous les mocks de TenantCacheAdapter et TenantRepositoryAdapter. Ajoute un mock de YowAuth0ClaimsExtractor. Crée des tokens JWT de test via TestJwtFactory enrichis avec les claims YowAuth0 — organization_id, org_name, org_status, org_plan. Teste que le TenantContext est construit depuis les claims JWT sans aucun appel base de données. Teste que tenant avec org_status = "SUSPENDED" dans les claims retourne 403. Teste que token sans organization_id retourne 401 avec MISSING_TENANT_CLAIM.

Fichier T5 : Mettre à jour src/test/java/com/yowyob/loyalty/integration/config/TestJwtFactory.java
Ajoute des méthodes pour générer des tokens avec les claims YowAuth0. Méthode forTenantAdmin(String tenantId) existante — enrichis le token avec organization_id = tenantId, org_name = "Test Organization", org_status = "ACTIVE", org_plan = "PRO", realm_access = {"roles": ["ROLE_TENANT_ADMIN"]}. Méthode forMember(String tenantId, String userId) existante — enrichis avec les mêmes claims org et realm_access = {"roles": ["ROLE_MEMBER"]}. Méthode withSuspendedTenant(String tenantId) — génère un token avec org_status = "SUSPENDED". Méthode withPendingSetupTenant(String tenantId) — génère un token avec org_status = "PENDING_SETUP".

Phase 5 — Vérifications finales
Exécute dans l'ordre. Ne passe à l'étape suivante que si la précédente est entièrement verte.

Vérification 1 — Compilation
Exécute mvn compile. La compilation doit réussir sans erreur. Si des classes supprimées sont encore référencées quelque part, le compilateur te le dira exactement. Corrige chaque erreur avant de continuer.

Vérification 2 — Tests d'architecture
Exécute mvn test -Dtest=HexagonalArchitectureTest. Toutes les règles ArchUnit doivent passer en vert. Si une violation est signalée, le message indique exactement quel fichier viole quelle règle. Corrige avant de continuer.

Vérification 3 — Tests du domaine
Exécute mvn test -Dtest="SharedDomainModelsTest,TenantTest,TenantContextTest". Ces tests doivent passer sans démarrer Spring, sans connexion réseau, sans base de données. S'ils démarrent un contexte Spring, quelque chose est mal placé dans le domaine.

Vérification 4 — Tests des stubs
Exécute mvn test -Dtest=StubsTest. Les trois stubs doivent être chargés correctement dans le profil stub et retourner les valeurs attendues.

Vérification 5 — Tests de sécurité
Exécute mvn test -Dtest="YowAuth0ClaimsExtractorTest,TenantResolutionFilterTest". Tous les scénarios JWT et tenant doivent passer.

Vérification 6 — Tous les tests
Exécute mvn test. Tous les tests doivent passer. Aucune régression par rapport à ce qui fonctionnait avant ces modifications.

Vérification 7 — Démarrage en mode stub
Lance mvn spring-boot:run -Dspring-boot.run.profiles=dev,stub avec PostgreSQL et Redis démarrés. L'application doit démarrer sans erreur. Les logs doivent afficher les warnings des stubs au démarrage. Appelle GET http://localhost:8080/api/v1/health — doit retourner 200. Appelle GET http://localhost:8080/api/v1/health/tenant avec un JWT valide généré par TestJwtFactory.forTenantAdmin("test-tenant-id") — doit retourner 200 avec le tenantId correct.

Vérification 8 — Vérification que le profil non-stub refuse de démarrer sans Kernel Core
Lance mvn spring-boot:run -Dspring-boot.run.profiles=dev sans le profil stub. L'application doit démarrer mais les appels aux méthodes des adapters Kernel Core doivent lancer UnsupportedOperationException avec un message clair. Cela confirme que les placeholders sont bien en place et qu'il est impossible d'utiliser le système en production sans implémenter les vrais adapters.

Résumé complet de toutes les modifications
SUPPRESSIONS
──────────────────────────────────────────────────────────────
domain/tenant/                                   → supprimé
infrastructure/persistence/tenant/               → supprimé
infrastructure/payment/                          → supprimé
db/migrations/V001__create_public_tenants.sql    → supprimé
db/migrations/V002__create_api_keys.sql          → supprimé
shared/security/JwtTokenValidator.java           → supprimé
shared/security/JwtValidationResult.java         → supprimé

CRÉATIONS — Domaine
──────────────────────────────────────────────────────────────
domain/wallet/model/PaymentStatus.java
domain/wallet/model/PaymentInitiationResult.java
domain/wallet/model/KycStatus.java
domain/shared/port/out/TenantQueryPort.java
domain/wallet/port/out/PaymentGatewayPort.java
domain/wallet/port/out/KycVerificationPort.java

CRÉATIONS — Stubs
──────────────────────────────────────────────────────────────
infrastructure/stub/TenantQueryStub.java
infrastructure/stub/PaymentGatewayStub.java
infrastructure/stub/KycVerificationStub.java

CRÉATIONS — Placeholders Kernel Core
──────────────────────────────────────────────────────────────
infrastructure/kernelcore/KernelCoreProperties.java
infrastructure/kernelcore/adapter/KernelCoreTenantAdapter.java
infrastructure/kernelcore/adapter/KernelCorePaymentAdapter.java
infrastructure/kernelcore/adapter/SmartKycAdapter.java

CRÉATIONS — Migrations
──────────────────────────────────────────────────────────────
db/migrations/V001__create_wallet_tables.sql

CRÉATIONS — Exceptions
──────────────────────────────────────────────────────────────
shared/exception/KernelCoreException.java
shared/exception/PaymentException.java
shared/exception/KycException.java

CRÉATIONS — Tests
──────────────────────────────────────────────────────────────
test/shared/security/YowAuth0ClaimsExtractorTest.java
test/shared/multitenancy/TenantContextTest.java
test/infrastructure/stub/StubsTest.java

MODIFICATIONS
──────────────────────────────────────────────────────────────
shared/multitenancy/TenantContext.java           → réécrit
shared/multitenancy/TenantResolutionFilter.java  → logique JWT
shared/security/JwtProperties.java              → claims YowAuth0
shared/security/SecurityConfig.java             → retirer JwtTokenValidator
shared/exception/ErrorCode.java                 → ajout codes Kernel Core
application.yml                                 → ajout kernel-core
application-dev.yml                             → profil stub activé
application-test.yml                            → profil stub activé
db/changelog/db.changelog-master.xml            → références mises à jour
HexagonalArchitectureTest.java                  → 3 nouvelles règles
TestJwtFactory.java                             → claims YowAuth0 enrichis
TenantResolutionFilterTest.java                 → mocks mis à jour

FICHIER CRÉÉ ET REMPLACÉ
──────────────────────────────────────────────────────────────
shared/security/YowAuth0ClaimsExtractor.java    → remplace JwtClaimsExtractor
                                                   (garde JwtClaimsExtractor
                                                    si d'autres classes l'utilisent
                                                    en le faisant étendre
                                                    YowAuth0ClaimsExtractor)

Ce que cette préparation permet
Quand le Kernel Core sera disponible, le travail se limitera exactement à ces actions sans toucher à quoi que ce soit d'autre.
Ouvrir KernelCoreTenantAdapter.java et remplacer chaque throw new UnsupportedOperationException(...) par un vrai appel WebClient vers le Kernel Core.
Faire de même pour KernelCorePaymentAdapter.java et SmartKycAdapter.java.
Désactiver le profil stub en production.
Le domaine, les handlers, les contrôleurs, les tests de domaine — tout reste intact. C'est exactement ce que l'architecture hexagonale garantit.
