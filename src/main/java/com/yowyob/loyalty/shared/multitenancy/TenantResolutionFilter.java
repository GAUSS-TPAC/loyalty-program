package com.yowyob.loyalty.shared.multitenancy;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.port.out.TenantRepository;
import com.yowyob.loyalty.infrastructure.redis.adapter.TenantCacheAdapter;
import com.yowyob.loyalty.shared.security.JwtClaimsExtractor;
import com.yowyob.loyalty.shared.security.JwtTokenValidator;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(-200)
@Profile("!dev")
public class TenantResolutionFilter implements WebFilter {

    private static final String[] PUBLIC_PATHS = {
            "/public/", "/actuator", "/swagger-ui", "/v3/api-docs", "/api-docs", "/webjars/"
    };

    private final TenantCacheAdapter tenantCacheAdapter;
    private final TenantRepository tenantRepository;
    private final JwtTokenValidator jwtTokenValidator;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    public TenantResolutionFilter(TenantCacheAdapter tenantCacheAdapter,
                                  TenantRepository tenantRepository,
                                  JwtTokenValidator jwtTokenValidator,
                                  JwtClaimsExtractor jwtClaimsExtractor) {
        this.tenantCacheAdapter = tenantCacheAdapter;
        this.tenantRepository = tenantRepository;
        this.jwtTokenValidator = jwtTokenValidator;
        this.jwtClaimsExtractor = jwtClaimsExtractor;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        for (String pub : PUBLIC_PATHS) {
            if (path.startsWith(pub)) return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No JWT — let ApiKeyResolutionFilter handle it; Spring Security enforces auth at the end
            return chain.filter(exchange);
        }

        String token = authHeader.substring(7);

        // Validate signature via JWK before trusting any claim
        return jwtTokenValidator.validateToken(token)
                .flatMap(result -> {
                    if (!result.isValid()) {
                        return writeUnauthorized(exchange);
                    }
                    TenantId tenantId;
                    try {
                        tenantId = jwtClaimsExtractor.extractTenantId(result.jwt());
                    } catch (Exception e) {
                        return writeUnauthorized(exchange);
                    }
                    return resolveTenant(tenantId, exchange, chain);
                })
                .onErrorResume(e -> writeUnauthorized(exchange));
    }

    private Mono<Void> resolveTenant(TenantId tenantId, ServerWebExchange exchange, WebFilterChain chain) {
        return tenantCacheAdapter.findById(tenantId)
                .switchIfEmpty(Mono.defer(() -> tenantRepository.findById(tenantId)
                        .flatMap(tenant -> tenantCacheAdapter.cache(tenant).thenReturn(tenant))))
                .flatMap(tenant -> {
                    if (!tenant.isActive()) {
                        return writeForbidden(exchange);
                    }
                    TenantContext ctx = TenantContext.from(tenant);
                    return chain.filter(exchange)
                            .contextWrite(TenantContextHolder.withTenantContext(ctx));
                })
                .switchIfEmpty(Mono.defer(() -> writeUnauthorized(exchange)));
    }

    private Mono<Void> writeUnauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> writeForbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }
}
