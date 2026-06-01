package com.yowyob.loyalty.infrastructure.security;

import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component("memberOwnershipValidator")
public class MemberOwnershipValidator {

    public Mono<Boolean> isOwnerOrAdmin(String memberId) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .ofType(JwtAuthenticationToken.class)
                .map(auth -> {
                    String sub = auth.getToken().getSubject();
                    boolean isAdmin = auth.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_TENANT_ADMIN"));
                    return isAdmin || sub.equals(memberId);
                })
                .defaultIfEmpty(false);
    }
}
