package com.yowyob.loyalty.infrastructure.security;

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
@Order(-90) // Executes right after Spring Security's AuthenticationWebFilter
public class TenantSecurityFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .ofType(JwtAuthenticationToken.class)
                .flatMap(auth -> {
                    // Extract tenant claim directly from the authenticated JWT
                    String tokenTenantId = auth.getToken().getClaimAsString("tenant_id");
                    
                    return TenantContextHolder.getTenantId()
                            .flatMap(tenantId -> {
                                if (!tenantId.value().toString().equals(tokenTenantId)) {
                                    return Mono.error(new CrossTenantAccessException("Cross-tenant access forbidden: Token tenant does not match active context."));
                                }
                                return chain.filter(exchange);
                            });
                })
                .switchIfEmpty(chain.filter(exchange));
    }
}
