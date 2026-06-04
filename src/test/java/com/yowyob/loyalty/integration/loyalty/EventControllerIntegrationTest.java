package com.yowyob.loyalty.integration.loyalty;

import com.yowyob.loyalty.config.TestContainersConfig;
import com.yowyob.loyalty.domain.loyalty.model.rule.*;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.infrastructure.persistence.loyalty.adapter.RuleRepositoryAdapter;
import com.yowyob.loyalty.shared.security.TestJwtFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.testcontainers.DockerClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "stub"})
@Import(TestContainersConfig.class)
@TestPropertySource(properties = {
        "spring.flyway.enabled=true",
        "app.security.jwt.tenant-id-claim=tenant_id"
})
@EnabledIf("com.yowyob.loyalty.integration.loyalty.EventControllerIntegrationTest#isDockerAvailable")
class EventControllerIntegrationTest {

    static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Exception e) {
            return false;
        }
    }

    private static final UUID TENANT_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final UUID MEMBER_ID = UUID.fromString("00000000-0000-0000-0000-000000000123");

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DatabaseClient databaseClient;

    @Autowired
    private RuleRepositoryAdapter ruleRepository;

    private String authHeader;

    @BeforeEach
    void setUp() {
        authHeader = TestJwtFactory.bearerHeaderForTenant(TENANT_ID, "integration-admin");

        databaseClient.sql("DELETE FROM points_transactions").then().block();
        databaseClient.sql("DELETE FROM points_accounts").then().block();
        databaseClient.sql("DELETE FROM loyalty_counters").then().block();
        databaseClient.sql("DELETE FROM rules").then().block();
        databaseClient.sql("DELETE FROM tenants WHERE id = :id").bind("id", TENANT_ID).then().block();

        databaseClient.sql("""
                INSERT INTO tenants (id, name, slug, status, plan, config)
                VALUES (:id, 'Loyalty IT', 'loyalty-it', 'ACTIVE', 'PRO', '{}')
                """)
                .bind("id", TENANT_ID)
                .then()
                .block();

        Rule rule = Rule.create(
                UUID.randomUUID(),
                TenantId.of(TENANT_ID),
                "Purchase bonus",
                "3 purchases = 100 points",
                new TriggerDefinition("purchase.completed", null),
                List.of(new ConditionDefinition(
                        ConditionType.CUMULATIVE_COUNT,
                        ConditionOperator.GREATER_THAN_OR_EQUAL,
                        3,
                        "LIFETIME",
                        "purchase_count")),
                List.of(new EffectDefinition(EffectType.CREDIT_POINTS, Map.of("amount", 100))),
                10,
                null,
                null
        ).activate();
        ruleRepository.save(rule);
    }

    @Test
    void firstEvent_noEffects_counterAtOne() {
        postEvent("evt-001")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.effectsApplied").isEmpty();
    }

    @Test
    void duplicateIdempotencyKey_returnsCachedResponse() {
        firstEvent_noEffects_counterAtOne();
        postEvent("evt-001")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.effectsApplied").isEmpty();
    }

    @Test
    void thirdEvent_creditsPoints() {
        postEvent("evt-001").expectStatus().isOk();
        postEvent("evt-002").expectStatus().isOk();
        postEvent("evt-003")
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.effectsApplied.length()").isEqualTo(1)
                .jsonPath("$.effectsApplied[0].effectType").isEqualTo("CREDIT_POINTS");
    }

    @Test
    void getMemberPoints_returnsBalanceAfterThirdEvent() {
        thirdEvent_creditsPoints();
        webTestClient.get()
                .uri("/api/v1/members/{memberId}/points", MEMBER_ID)
                .header("Authorization", authHeader)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.availablePoints").isEqualTo(100);
    }

    @Test
    void createRule_returnsDraft() {
        String body = """
                {
                  "name": "New rule",
                  "description": "Test",
                  "trigger": { "eventType": "purchase.completed", "filters": {} },
                  "conditions": [{
                    "type": "CUMULATIVE_COUNT",
                    "operator": "GREATER_THAN_OR_EQUAL",
                    "thresholdValue": 5,
                    "windowType": "LIFETIME",
                    "counterKey": "purchases"
                  }],
                  "effects": [{ "type": "CREDIT_POINTS", "params": { "amount": 50 } }],
                  "priority": 5
                }
                """;
        webTestClient.post()
                .uri("/api/v1/admin/rules")
                .header("Authorization", authHeader)
                .header("Idempotency-Key", "rule-create-001")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo("DRAFT")
                .jsonPath("$.name").isEqualTo("New rule");
    }

    @Test
    void activateRule_setsStatusActive() {
        String createBody = """
                {
                  "name": "Activate me",
                  "trigger": { "eventType": "trip.ended" },
                  "conditions": [{
                    "type": "FIRST_EVENT",
                    "operator": "EQUALS",
                    "thresholdValue": 1,
                    "counterKey": "trips"
                  }],
                  "effects": [{ "type": "CREDIT_POINTS", "params": { "amount": 10 } }],
                  "priority": 1
                }
                """;
        AtomicReference<String> ruleId = new AtomicReference<>();
        webTestClient.post()
                .uri("/api/v1/admin/rules")
                .header("Authorization", authHeader)
                .header("Idempotency-Key", "rule-create-002")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createBody)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").value(ruleId::set)
                .jsonPath("$.status").isEqualTo("DRAFT");

        webTestClient.patch()
                .uri("/api/v1/admin/rules/{ruleId}/activate", ruleId.get())
                .header("Authorization", authHeader)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("ACTIVE");
    }

    private WebTestClient.ResponseSpec postEvent(String idempotencyKey) {
        String body = """
                {
                  "eventType": "purchase.completed",
                  "memberId": "%s",
                  "occurredAt": "%s",
                  "payload": { "amount": 5000 }
                }
                """.formatted(MEMBER_ID, Instant.now());
        return webTestClient.post()
                .uri("/api/v1/events")
                .header("Authorization", authHeader)
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .exchange();
    }
}
