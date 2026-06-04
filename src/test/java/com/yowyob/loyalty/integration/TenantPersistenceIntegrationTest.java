package com.yowyob.loyalty.integration;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.model.enums.TenantStatus;
import com.yowyob.loyalty.config.TestContainersConfig;
import com.yowyob.loyalty.infrastructure.persistence.tenant.adapter.TenantRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.testcontainers.DockerClientFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.util.UUID;

@SpringBootTest
@Import(TestContainersConfig.class)
@org.springframework.test.context.ActiveProfiles({"test", "stub"})
@TestPropertySource(properties = "spring.flyway.enabled=true")
@EnabledIf("com.yowyob.loyalty.integration.TenantPersistenceIntegrationTest#isDockerAvailable")
public class TenantPersistenceIntegrationTest {

    static boolean isDockerAvailable() {
        try {
            return DockerClientFactory.instance().isDockerAvailable();
        } catch (Exception e) {
            return false;
        }
    }

    @Autowired
    private TenantRepositoryAdapter tenantRepositoryAdapter;

    @Autowired
    private DatabaseClient databaseClient;

    private UUID testId;

    @BeforeEach
    public void setup() {
        testId = UUID.randomUUID();
        databaseClient.sql("INSERT INTO tenants (id, name, slug, status, plan, config) VALUES (:id, 'Test Name', 'test-slug', 'ACTIVE', 'PRO', '{}')")
                .bind("id", testId)
                .then()
                .block();
    }

    @Test
    public void testFindById() {
        tenantRepositoryAdapter.findById(TenantId.of(testId))
                .as(StepVerifier::create)
                .expectNextMatches(tenant -> tenant.getName().equals("Test Name") && tenant.getSlug().equals("test-slug") && tenant.getStatus() == TenantStatus.ACTIVE)
                .verifyComplete();
    }

    @Test
    public void testFindBySlug() {
        tenantRepositoryAdapter.findBySlug("test-slug")
                .as(StepVerifier::create)
                .expectNextMatches(tenant -> tenant.getId().value().equals(testId))
                .verifyComplete();
    }

    @Test
    public void testFindByIdNotFound() {
        tenantRepositoryAdapter.findById(TenantId.of(UUID.randomUUID()))
                .as(StepVerifier::create)
                .verifyComplete();
    }
}
