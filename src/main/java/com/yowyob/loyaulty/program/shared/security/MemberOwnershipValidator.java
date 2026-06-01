package com.yowyob.loyaulty.program.shared.security;

import com.yowyob.loyaulty.program.shared.exception.ForbiddenException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Vérifie qu'un utilisateur authentifié ne peut accéder qu'à ses propres ressources.
 * Les TENANT_ADMIN et SUPER_ADMIN sont exemptés.
 * Appelé manuellement dans les handlers, pas en tant que filtre global.
 */
@Component
public class MemberOwnershipValidator {

    private static final String ROLE_TENANT_ADMIN = "ROLE_TENANT_ADMIN";
    private static final String ROLE_SUPER_ADMIN  = "ROLE_SUPER_ADMIN";

    /**
     * Valide que l'utilisateur courant peut accéder aux données du membre {@code resourceMemberId}.
     * Retourne Mono.empty() si autorisé, Mono.error(ForbiddenException) sinon.
     */
    public Mono<Void> validate(String resourceMemberId) {
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(ctx -> {
                    var auth = ctx.getAuthentication();
                    if (auth == null || !auth.isAuthenticated()) {
                        return Mono.error(new ForbiddenException("Not authenticated"));
                    }

                    boolean isAdmin = auth.getAuthorities().stream()
                            .map(GrantedAuthority::getAuthority)
                            .anyMatch(r -> r.equals(ROLE_TENANT_ADMIN) || r.equals(ROLE_SUPER_ADMIN));

                    if (isAdmin) {
                        return Mono.empty();
                    }

                    String tokenUserId = auth.getName();
                    if (!tokenUserId.equals(resourceMemberId)) {
                        return Mono.error(new ForbiddenException(
                                "You can only access your own resources"));
                    }
                    return Mono.empty();
                });
    }
}
