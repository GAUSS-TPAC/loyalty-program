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

    private final JwtProperties jwtProperties;

    public TenantSecurityFilter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
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
