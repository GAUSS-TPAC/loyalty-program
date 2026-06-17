package com.yowyob.loyalty.shared.multitenancy;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.infrastructure.kernelcore.adapter.KernelCoreTenantAdapter;
import com.yowyob.loyalty.infrastructure.redis.adapter.TenantCacheAdapter;
import com.yowyob.loyalty.shared.exception.TenantNotFoundException;
import com.yowyob.loyalty.shared.security.JwtClaimsExtractor;
import org.springframework.context.annotation.Profile;
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
@Profile("!dev")
public class TenantResolutionFilter implements WebFilter {

    private static final String[] PUBLIC_PATHS = {
            "/public/", "/actuator", "/swagger-ui", "/v3/api-docs", "/api-docs", "/webjars/"
    };

    private final TenantCacheAdapter tenantCacheAdapter;
    private final KernelCoreTenantAdapter kernelCoreTenantAdapter;
    private final JwtClaimsExtractor jwtClaimsExtractor;

    public TenantResolutionFilter(
            TenantCacheAdapter tenantCacheAdapter,
            KernelCoreTenantAdapter kernelCoreTenantAdapter,
            JwtClaimsExtractor jwtClaimsExtractor) {
        this.tenantCacheAdapter = tenantCacheAdapter;
        this.kernelCoreTenantAdapter = kernelCoreTenantAdapter;
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
            return writeUnauthorized(exchange);
        }

        TenantId tenantId;
        try {
            tenantId = jwtClaimsExtractor.extractTenantIdFromRawToken(authHeader.substring(7));
        } catch (Exception e) {
            return writeUnauthorized(exchange);
        }

        return tenantCacheAdapter.findById(tenantId)
                .switchIfEmpty(Mono.defer(() -> kernelCoreTenantAdapter.fetchAndCache(tenantId)))
                .flatMap(tenant -> {
                    if (!tenant.isActive()) return writeUnauthorized(exchange);
                    TenantContext ctx = TenantContext.from(tenant);
                    return chain.filter(exchange).contextWrite(TenantContextHolder.withTenantContext(ctx));
                })
                .onErrorResume(TenantNotFoundException.class, e -> writeUnauthorized(exchange))
                .switchIfEmpty(Mono.defer(() -> writeUnauthorized(exchange)));
    }

    private Mono<Void> writeUnauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
