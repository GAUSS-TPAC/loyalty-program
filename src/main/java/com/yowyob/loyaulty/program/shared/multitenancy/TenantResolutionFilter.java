package com.yowyob.loyaulty.program.shared.multitenancy;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.tenant.model.Tenant;
import com.yowyob.loyaulty.program.infrastructure.redis.adapter.TenantCacheAdapter;
import com.yowyob.loyaulty.program.shared.exception.TenantNotFoundException;
import com.yowyob.loyaulty.program.shared.exception.TenantSuspendedException;
import com.yowyob.loyaulty.program.shared.security.JwtClaimsExtractor;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
@Order(-100)
public class TenantResolutionFilter implements WebFilter {

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/actuator/health",
            "/api/v1/health",
            "/api-docs",
            "/swagger-ui"
    );

    private final TenantCacheAdapter tenantCache;
    private final JwtClaimsExtractor claimsExtractor;

    public TenantResolutionFilter(TenantCacheAdapter tenantCache,
                                   JwtClaimsExtractor claimsExtractor) {
        this.tenantCache = tenantCache;
        this.claimsExtractor = claimsExtractor;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        return ReactiveSecurityContextHolder.getContext()
                .flatMap(secCtx -> {
                    if (secCtx.getAuthentication() instanceof JwtAuthenticationToken jwtAuth) {
                        Jwt jwt = jwtAuth.getToken();
                        return claimsExtractor.extractTenantId(jwt.getClaims())
                                .map(TenantId::of)
                                .flatMap(tenantId -> resolveTenant(tenantId, exchange, chain))
                                .switchIfEmpty(chain.filter(exchange));
                    }
                    return chain.filter(exchange);
                })
                .switchIfEmpty(chain.filter(exchange));
    }

    private Mono<Void> resolveTenant(TenantId tenantId, ServerWebExchange exchange, WebFilterChain chain) {
        return tenantCache.findById(tenantId)
                .switchIfEmpty(Mono.error(new TenantNotFoundException(tenantId.toString())))
                .flatMap(tenant -> {
                    if (tenant.isSuspended()) {
                        return Mono.error(new TenantSuspendedException(tenantId.toString()));
                    }
                    TenantContext ctx = new TenantContext(
                            tenant.getId(),
                            tenant.getName(),
                            tenant.getStatus(),
                            tenant.getPlan()
                    );
                    return chain.filter(exchange)
                            .contextWrite(TenantContextHolder.withTenantContext(ctx));
                });
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
}
