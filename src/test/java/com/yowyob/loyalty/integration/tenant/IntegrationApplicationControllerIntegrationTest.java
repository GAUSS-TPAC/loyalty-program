package com.yowyob.loyalty.integration.tenant;

import com.yowyob.loyalty.config.TestContainersConfig;
import com.yowyob.loyalty.domain.shared.model.AuditInfo;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.model.Tenant;
import com.yowyob.loyalty.domain.tenant.model.TenantConfig;
import com.yowyob.loyalty.domain.tenant.model.enums.TenantPlan;
import com.yowyob.loyalty.domain.tenant.model.enums.TenantStatus;
import com.yowyob.loyalty.infrastructure.redis.adapter.TenantCacheAdapter;
import com.yowyob.loyalty.shared.security.TestJwtFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.DockerClientFactory;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Parcours complet du mécanisme d'intégration (modèle My-CoolPay) : création d'une
 * application (secrets révélés une fois), envoi d'un événement authentifié par la clé
 * privée sur la route scopée par clé publique, rotation et suppression.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "stub"})
@Import(TestContainersConfig.class)
@TestPropertySource(properties = {
        "spring.liquibase.enabled=true",
        "app.security.jwt.tenant-id-claim=tenant_id"
})
@EnabledIf("com.yowyob.loyalty.integration.tenant.IntegrationApplicationControllerIntegrationTest#isDockerAvailable")
class IntegrationApplicationControllerIntegrationTest {

    static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Exception e) {
            return false;
        }
    }

    private static final UUID TENANT_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final UUID OTHER_TENANT_ID = UUID.fromString("bbbbbbbb-cccc-dddd-eeee-ffffffffffff");
    private static final UUID MEMBER_ID = UUID.fromString("00000000-0000-0000-0000-000000000456");

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    private TenantCacheAdapter tenantCacheAdapter;

    private String authHeader;

    @BeforeEach
    void setUp() {
        authHeader = TestJwtFactory.bearerHeaderForTenant(TENANT_ID, "integration-admin");

        databaseClient.sql("DELETE FROM integration_applications").then().block();
        databaseClient.sql("DELETE FROM webhook_deliveries").then().block();
        databaseClient.sql("DELETE FROM webhook_endpoints").then().block();
        databaseClient.sql("DELETE FROM api_keys").then().block();
        databaseClient.sql("DELETE FROM points_transactions").then().block();
        databaseClient.sql("DELETE FROM points_accounts").then().block();
        databaseClient.sql("DELETE FROM loyalty_counters").then().block();
        databaseClient.sql("DELETE FROM rules").then().block();
        databaseClient.sql("DELETE FROM tenants WHERE id IN (:a, :b)")
                .bind("a", TENANT_ID).bind("b", OTHER_TENANT_ID).then().block();

        databaseClient.sql("""
                INSERT INTO tenants (id, name, slug, status, plan, config)
                VALUES (:id, 'Loyalty IT', 'loyalty-app-it', 'ACTIVE', 'PRO', '{}')
                """)
                .bind("id", TENANT_ID)
                .then()
                .block();

        // La résolution par clé API passe par le cache Redis des tenants (sinon repli
        // Kernel Core, indisponible en test) : on alimente le cache directement.
        tenantCacheAdapter.cache(activeTenant(TENANT_ID)).block();
        tenantCacheAdapter.cache(activeTenant(OTHER_TENANT_ID)).block();
    }

    private static Tenant activeTenant(UUID id) {
        return new Tenant(TenantId.of(id), "Tenant " + id, "slug-" + id,
                TenantStatus.ACTIVE, TenantPlan.FREE, TenantConfig.defaults(), AuditInfo.now("test"));
    }

    @Test
    void fullApplicationLifecycle() {
        AtomicReference<String> appId = new AtomicReference<>();
        AtomicReference<String> publicKey = new AtomicReference<>();
        AtomicReference<String> privateKey = new AtomicReference<>();
        AtomicReference<String> webhookSecret = new AtomicReference<>();

        // 1. Création : clé publique + clé privée + secret webhook révélés une seule fois
        webTestClient.post()
                .uri("/api/v1/admin/applications")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "name": "Boutique e-commerce",
                          "description": "Site de vente en ligne",
                          "websiteUrl": "https://shop.example.com",
                          "mode": "TEST",
                          "callbackUrl": "https://shop.example.com/loyalty/callback",
                          "eventTypes": ["points.earned", "tier.changed"]
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(appId::set)
                .jsonPath("$.publicKey").value(publicKey::set)
                .jsonPath("$.privateKey").value(privateKey::set)
                .jsonPath("$.webhookSecret").value(webhookSecret::set)
                .jsonPath("$.name").isEqualTo("Boutique e-commerce");

        org.junit.jupiter.api.Assertions.assertTrue(publicKey.get().startsWith("pk_test_"));
        org.junit.jupiter.api.Assertions.assertTrue(privateKey.get().startsWith("sk_test_"));
        org.junit.jupiter.api.Assertions.assertTrue(webhookSecret.get().startsWith("whsec_"));

        // 2. La liste masque les secrets
        webTestClient.get()
                .uri("/api/v1/admin/applications")
                .header("Authorization", authHeader)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].publicKey").isEqualTo(publicKey.get())
                .jsonPath("$[0].privateKey").doesNotExist()
                .jsonPath("$[0].webhookSecret").doesNotExist()
                .jsonPath("$[0].callbackUrl").isEqualTo("https://shop.example.com/loyalty/callback");

        // 3. Envoi d'événement authentifié par la clé privée sur la route scopée
        postEvent(publicKey.get(), privateKey.get(), "app-evt-001")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.eventId").exists();

        // 4. Une clé publique inconnue du tenant → 404
        postEvent("pk_test_inconnu", privateKey.get(), "app-evt-002")
                .expectStatus().isNotFound();

        // 5. Rotation de la clé privée : nouvelle clé révélée, l'ancienne est révoquée
        AtomicReference<String> newPrivateKey = new AtomicReference<>();
        webTestClient.post()
                .uri("/api/v1/admin/applications/{id}/rotate-private-key", appId.get())
                .header("Authorization", authHeader)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.privateKey").value(newPrivateKey::set);
        org.junit.jupiter.api.Assertions.assertNotEquals(privateKey.get(), newPrivateKey.get());

        postEvent(publicKey.get(), privateKey.get(), "app-evt-003")
                .expectStatus().isUnauthorized();
        postEvent(publicKey.get(), newPrivateKey.get(), "app-evt-004")
                .expectStatus().isOk();

        // 6. Suppression : la route scopée ne connaît plus l'application
        webTestClient.delete()
                .uri("/api/v1/admin/applications/{id}", appId.get())
                .header("Authorization", authHeader)
                .exchange()
                .expectStatus().isNoContent();

        postEvent(publicKey.get(), newPrivateKey.get(), "app-evt-005")
                .expectStatus().isUnauthorized();
    }

    @Test
    void publicKeyOfAnotherTenantIsRejected() {
        // Application du tenant principal
        AtomicReference<String> publicKey = new AtomicReference<>();
        webTestClient.post()
                .uri("/api/v1/admin/applications")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\": \"App A\", \"mode\": \"TEST\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.publicKey").value(publicKey::set);

        // Clé privée d'un AUTRE tenant
        String otherAuthHeader = TestJwtFactory.bearerHeaderForTenant(OTHER_TENANT_ID, "other-admin");
        AtomicReference<String> otherPrivateKey = new AtomicReference<>();
        webTestClient.post()
                .uri("/api/v1/admin/applications")
                .header("Authorization", otherAuthHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\": \"App B\", \"mode\": \"TEST\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.privateKey").value(otherPrivateKey::set);

        // La clé publique du tenant A avec la clé privée du tenant B → 404
        postEvent(publicKey.get(), otherPrivateKey.get(), "cross-evt-001")
                .expectStatus().isNotFound();
    }

    private WebTestClient.ResponseSpec postEvent(String publicKey, String privateKey, String idempotencyKey) {
        String body = """
                {
                  "eventType": "purchase.completed",
                  "memberId": "%s",
                  "occurredAt": "%s",
                  "payload": { "amount": 2500 }
                }
                """.formatted(MEMBER_ID, Instant.now());
        return webTestClient.post()
                .uri("/api/v1/apps/{publicKey}/events", publicKey)
                .header("X-Api-Key", privateKey)
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange();
    }
}
