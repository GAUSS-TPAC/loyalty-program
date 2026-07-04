package com.yowyob.loyalty.api.tenant;

import com.yowyob.loyalty.infrastructure.kernelcore.adapter.KernelCoreActorAdapter;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelBusinessActorDto;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelBusinessActorUpdateRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Passe-plat vers le profil "acteur métier" de l'utilisateur courant sur Kernel Core (/api/actors/me).
 * Le Bearer JWT de la requête entrante est transmis tel quel à Kernel Core.
 */
@RestController
@RequestMapping("/api/kernel-core/actors")
public class KernelCoreProfileController {

    private final KernelCoreActorAdapter kernelCoreActorAdapter;

    public KernelCoreProfileController(KernelCoreActorAdapter kernelCoreActorAdapter) {
        this.kernelCoreActorAdapter = kernelCoreActorAdapter;
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public Mono<KernelBusinessActorDto> getMyProfile() {
        return currentBearerToken().flatMap(kernelCoreActorAdapter::getMyProfile);
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public Mono<KernelBusinessActorDto> updateMyProfile(@RequestBody KernelBusinessActorUpdateRequest request) {
        return currentBearerToken().flatMap(token -> kernelCoreActorAdapter.updateMyProfile(token, request));
    }

    private Mono<String> currentBearerToken() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .ofType(JwtAuthenticationToken.class)
                .map(auth -> auth.getToken().getTokenValue());
    }
}
