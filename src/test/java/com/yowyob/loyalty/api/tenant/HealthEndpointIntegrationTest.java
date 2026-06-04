package com.yowyob.loyalty.api.tenant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

// @Import(TestContainersConfig.class) - Included when tests run with the right profiles
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@org.springframework.test.context.ActiveProfiles({"test", "stub"})
public class HealthEndpointIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void testPublicHealth() {
        webTestClient.get().uri("/public/health")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("OK");
    }

    @Test
    public void testSecureHealthWithoutAuthReturns401() {
        webTestClient.get().uri("/api/health")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
