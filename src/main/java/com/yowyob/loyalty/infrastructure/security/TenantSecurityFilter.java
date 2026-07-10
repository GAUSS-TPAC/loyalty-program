package com.yowyob.loyalty.infrastructure.security;

import com.yowyob.loyalty.infrastructure.security.config.JwtProperties;
import com.yowyob.loyalty.shared.exception.CrossTenantAccessException;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
@Order(-90)
public class TenantSecurityFilter implements WebFilter {

    private static final String ORGANIZATION_HEADER = "X-Organization-Id";

    private final JwtProperties jwtProperties;

    public TenantSecurityFilter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // Un login KernelCore tenant-scopé "nu" ne porte aucun claim d'organisation dans le JWT
        // (voir TenantResolutionFilter) : l'organisation résolue via X-Organization-Id a déjà été
        // validée là-bas (organisation existante + active), donc rien à recomparer ici. Le repli
        // sur le claim JWT reste utile pour un futur token issu du flux discover/select-context.
        String orgHeader = exchange.getRequest().getHeaders().getFirst(ORGANIZATION_HEADER);
        if (orgHeader != null && !orgHeader.isBlank()) {
            return chain.filter(exchange);
        }

        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .ofType(JwtAuthenticationToken.class)
                .flatMap(auth -> {
                    String tokenTenantId = auth.getToken().getClaimAsString(jwtProperties.getTenantIdClaim());
                    return TenantContextHolder.getTenantId()
                            .flatMap(tenantId -> {
                                if (!tenantId.value().toString().equals(tokenTenantId)) {
                                    return Mono.error(new CrossTenantAccessException(
                                            "Cross-tenant access forbidden: token tenant does not match context."));
                                }
                                return chain.filter(exchange);
                            });
                })
                // API-key auth has no JWT context — tenant was already verified by ApiKeyResolutionFilter
                .switchIfEmpty(chain.filter(exchange));
    }
}
