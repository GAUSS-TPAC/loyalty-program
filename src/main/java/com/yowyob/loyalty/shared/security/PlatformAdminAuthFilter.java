package com.yowyob.loyalty.shared.security;

import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Garde d'accès pour la console plateforme (/api/v1/admin/platform/**), qui
 * n'est pas rattachée à un tenant : ni TenantResolutionFilter (Bearer JWT) ni
 * ApiKeyResolutionFilter (X-Api-Key) ne s'appliquent à cette route, donc c'est
 * ce filtre — seul — qui protège l'accès, via un secret statique dédié
 * (distinct des credentials tenant).
 */
@Component
@Order(-198)
public class PlatformAdminAuthFilter implements WebFilter {

    private static final String PLATFORM_PATH_PREFIX = "/api/v1/admin/platform";
    private static final String SECRET_HEADER = "X-Platform-Admin-Key";

    private final String configuredSecret;

    public PlatformAdminAuthFilter(
            @org.springframework.beans.factory.annotation.Value("${app.platform-admin.secret:}") String configuredSecret) {
        this.configuredSecret = configuredSecret;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (!path.startsWith(PLATFORM_PATH_PREFIX)) {
            return chain.filter(exchange);
        }

        String provided = exchange.getRequest().getHeaders().getFirst(SECRET_HEADER);
        if (configuredSecret.isBlank() || provided == null || !provided.equals(configuredSecret)) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }
}
