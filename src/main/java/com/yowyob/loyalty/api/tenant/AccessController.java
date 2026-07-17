package com.yowyob.loyalty.api.tenant;

import com.yowyob.loyalty.api.tenant.dto.AccessResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Source de vérité unique pour le frontend afin de savoir si l'utilisateur courant est
 * TENANT_ADMIN (voit/gère tout le tenant) ou un simple développeur (scope limité à ses
 * propres clés API) — évite de dupliquer côté client la logique d'autorisation du backend.
 */
@RestController
@RequestMapping("/api/access")
public class AccessController {

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public Mono<AccessResponse> me() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .map(auth -> new AccessResponse(
                        auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TENANT_ADMIN"))));
    }
}
