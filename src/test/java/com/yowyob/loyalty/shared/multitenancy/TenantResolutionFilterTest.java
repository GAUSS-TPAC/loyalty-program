package com.yowyob.loyalty.shared.multitenancy;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.model.Tenant;
import com.yowyob.loyalty.domain.tenant.model.TenantConfig;
import com.yowyob.loyalty.domain.tenant.model.enums.TenantPlan;
import com.yowyob.loyalty.infrastructure.kernelcore.adapter.KernelCoreTenantAdapter;
import com.yowyob.loyalty.infrastructure.redis.adapter.TenantCacheAdapter;
import com.yowyob.loyalty.shared.security.JwtClaimsExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class TenantResolutionFilterTest {

    private TenantCacheAdapter tenantCacheAdapter;
    private KernelCoreTenantAdapter kernelCoreTenantAdapter;
    private JwtClaimsExtractor jwtClaimsExtractor;
    private TenantResolutionFilter filter;

    @BeforeEach
    public void setup() {
        tenantCacheAdapter = Mockito.mock(TenantCacheAdapter.class);
        kernelCoreTenantAdapter = Mockito.mock(KernelCoreTenantAdapter.class);
        jwtClaimsExtractor = Mockito.mock(JwtClaimsExtractor.class);
        filter = new TenantResolutionFilter(tenantCacheAdapter, kernelCoreTenantAdapter, jwtClaimsExtractor);
    }

    @Test
    public void testPublicPathBypassesFilter() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/public/health").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = filterExchange -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    public void testMissingAuthorizationPassesToChain() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/secure").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = filterExchange -> Mono.empty();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    public void testValidTokenResolvesTenantContext() throws Exception {
        TenantId tenantId = TenantId.of(UUID.randomUUID());
        Tenant tenant = Tenant.create(tenantId, "Test", "test", TenantPlan.PRO, TenantConfig.defaults(), "admin").activate();

        when(jwtClaimsExtractor.extractTenantIdFromRawToken("valid-token")).thenReturn(tenantId);
        when(tenantCacheAdapter.findById(tenantId)).thenReturn(Mono.just(tenant));

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/secure")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        WebFilterChain chain = filterExchange -> TenantContextHolder.getTenantContext()
                .doOnNext(ctx -> assertEquals(tenantId, ctx.tenantId()))
                .then();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    public void testInvalidTokenReturnsUnauthorized() throws Exception {
        when(jwtClaimsExtractor.extractTenantIdFromRawToken("bad-token"))
                .thenThrow(new IllegalArgumentException("Tenant claim missing"));

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/secure")
                .header(HttpHeaders.AUTHORIZATION, "Bearer bad-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = filterExchange -> Mono.error(new IllegalStateException("Should not reach here"));

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    /**
     * KernelCore auth-core ne met pas l'ID d'organisation dans le JWT d'un login tenant-scopé
     * "nu" : le client doit propager X-Organization-Id, et ce header doit primer sur tout claim
     * JWT (qui n'est utilisé qu'en repli — voir TenantResolutionFilter.resolveTenantIdFromRequest).
     */
    @Test
    public void testOrganizationHeaderTakesPrecedenceOverJwtClaim() {
        TenantId headerTenantId = TenantId.of(UUID.randomUUID());
        Tenant tenant = Tenant.create(headerTenantId, "Test", "test", TenantPlan.PRO, TenantConfig.defaults(), "admin").activate();

        when(tenantCacheAdapter.findById(headerTenantId)).thenReturn(Mono.just(tenant));

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/secure")
                .header(HttpHeaders.AUTHORIZATION, "Bearer some-token")
                .header("X-Organization-Id", headerTenantId.value().toString())
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        WebFilterChain chain = filterExchange -> TenantContextHolder.getTenantContext()
                .doOnNext(ctx -> assertEquals(headerTenantId, ctx.tenantId()))
                .then();

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        // The header resolved tenant without ever touching the JWT claim extractor.
        Mockito.verifyNoInteractions(jwtClaimsExtractor);
    }

    @Test
    public void testMalformedOrganizationHeaderReturnsUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/secure")
                .header(HttpHeaders.AUTHORIZATION, "Bearer some-token")
                .header("X-Organization-Id", "not-a-uuid")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = filterExchange -> Mono.error(new IllegalStateException("Should not reach here"));

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }
}
