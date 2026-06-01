package com.yowyob.loyaulty.program.shared.security;

import com.yowyob.loyaulty.program.shared.exception.CrossTenantAccessException;
import com.yowyob.loyaulty.program.shared.multitenancy.TenantContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Vérifie que le header X-Tenant-ID, s'il est fourni, correspond au tenant du JWT.
 * Protège contre les tentatives d'accès cross-tenant explicites.
 * S'exécute après TenantResolutionFilter (@Order -100).
 */
@Component
@Order(-50)
public class TenantSecurityFilter implements WebFilter {

    private static final String TENANT_HEADER = "X-Tenant-ID";

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/actuator/health",
            "/api/v1/health",
            "/api-docs",
            "/swagger-ui",
            "/api/v1/webhooks"
    );

    @Override
    @NonNull
    public Mono<Void> filter(@NonNull ServerWebExchange exchange, @NonNull WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String requestedTenantId = exchange.getRequest().getHeaders().getFirst(TENANT_HEADER);
        if (requestedTenantId == null || requestedTenantId.isBlank()) {
            return chain.filter(exchange);
        }

        return TenantContextHolder.getTenantId()
                .flatMap(resolvedTenantId -> {
                    if (!resolvedTenantId.value().toString().equals(requestedTenantId.trim())) {
                        return Mono.error(new CrossTenantAccessException());
                    }
                    return chain.filter(exchange);
                })
                .onErrorResume(TenantContextHolder.TenantContextMissingException.class,
                        e -> chain.filter(exchange));
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }
}
