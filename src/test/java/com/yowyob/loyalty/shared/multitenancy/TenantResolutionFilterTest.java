package com.yowyob.loyalty.shared.multitenancy;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.model.Tenant;
import com.yowyob.loyalty.domain.tenant.model.TenantConfig;
import com.yowyob.loyalty.domain.tenant.model.enums.TenantPlan;
import com.yowyob.loyalty.domain.tenant.port.out.TenantRepository;
import com.yowyob.loyalty.infrastructure.redis.adapter.TenantCacheAdapter;
import com.yowyob.loyalty.shared.security.JwtClaimsExtractor;
import com.yowyob.loyalty.shared.security.JwtTokenValidator;
import com.yowyob.loyalty.shared.security.JwtValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class TenantResolutionFilterTest {

    private TenantCacheAdapter tenantCacheAdapter;
    private TenantRepository tenantRepository;
    private JwtTokenValidator jwtTokenValidator;
    private JwtClaimsExtractor jwtClaimsExtractor;
    private TenantResolutionFilter filter;

    @BeforeEach
    public void setup() {
        tenantCacheAdapter = Mockito.mock(TenantCacheAdapter.class);
        tenantRepository = Mockito.mock(TenantRepository.class);
        jwtTokenValidator = Mockito.mock(JwtTokenValidator.class);
        jwtClaimsExtractor = Mockito.mock(JwtClaimsExtractor.class);
        filter = new TenantResolutionFilter(tenantCacheAdapter, tenantRepository, jwtTokenValidator, jwtClaimsExtractor);
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
    public void testMissingAuthorizationReturnsUnauthorized() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/secure").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        WebFilterChain chain = filterExchange -> Mono.error(new IllegalStateException("Should not reach here"));

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assertEquals(HttpStatus.UNAUTHORIZED, exchange.getResponse().getStatusCode());
    }

    @Test
    public void testValidTokenResolvesTenantContext() throws Exception {
        TenantId tenantId = TenantId.of(UUID.randomUUID());
        Tenant tenant = Tenant.create(tenantId, "Test", "test", TenantPlan.PRO, TenantConfig.defaults(), "admin").activate();
        Jwt mockJwt = Mockito.mock(Jwt.class);

        when(jwtTokenValidator.validateToken("valid-token")).thenReturn(Mono.just(JwtValidationResult.valid(mockJwt)));
        when(jwtClaimsExtractor.extractTenantId(mockJwt)).thenReturn(tenantId);
        when(tenantCacheAdapter.findById(tenantId)).thenReturn(Mono.just(tenant));

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/secure")
                .header(HttpHeaders.AUTHORIZATION, "Bearer valid-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        WebFilterChain chain = filterExchange -> {
            return TenantContextHolder.getTenantContext()
                    .doOnNext(ctx -> assertEquals(tenantId, ctx.tenantId()))
                    .then();
        };

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
    }
}
