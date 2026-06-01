package com.yowyob.loyalty.shared.multitenancy;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.port.out.TenantRepository;
import com.yowyob.loyalty.infrastructure.redis.adapter.TenantCacheAdapter;
import com.yowyob.loyalty.shared.security.JwtClaimsExtractor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(-200)
public class TenantResolutionFilter implements WebFilter {

    private final TenantCacheAdapter tenantCacheAdapter;
    private final TenantRepository tenantRepository;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    public TenantResolutionFilter(TenantCacheAdapter tenantCacheAdapter, TenantRepository tenantRepository, JwtClaimsExtractor jwtClaimsExtractor) {
        this.tenantCacheAdapter = tenantCacheAdapter;
        this.tenantRepository = tenantRepository;
        this.jwtClaimsExtractor = jwtClaimsExtractor;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (path.startsWith("/public/") || path.startsWith("/actuator") || path.startsWith("/swagger-ui") || path.startsWith("/api-docs")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return writeUnauthorized(exchange);
        }

        String token = authHeader.substring(7);
        TenantId tenantId;
        try {
            tenantId = jwtClaimsExtractor.extractTenantIdFromRawToken(token);
        } catch (Exception e) {
            return writeUnauthorized(exchange);
        }

        return tenantCacheAdapter.findById(tenantId)
                .switchIfEmpty(Mono.defer(() -> tenantRepository.findById(tenantId)
                        .flatMap(tenant -> tenantCacheAdapter.cache(tenant).thenReturn(tenant))))
                .flatMap(tenant -> {
                    if (!tenant.isActive()) {
                        return writeUnauthorized(exchange);
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
}
