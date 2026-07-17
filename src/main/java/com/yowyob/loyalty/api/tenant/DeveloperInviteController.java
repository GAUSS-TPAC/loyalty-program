package com.yowyob.loyalty.api.tenant;

import com.yowyob.loyalty.api.tenant.dto.InviteDeveloperRequest;
import com.yowyob.loyalty.application.tenant.DeveloperInviteService;
import com.yowyob.loyalty.shared.exception.DeveloperInviteException;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Réservé au TENANT_ADMIN : crée un compte développeur (acteur + login + rôle "developer") sur
 * le tenant courant via Kernel Core, puis déclenche l'email Kernel Core de définition de mot de
 * passe — aucun mot de passe ne transite par ce backend.
 */
@RestController
@RequestMapping("/api/v1/admin/developers")
@PreAuthorize("hasRole('TENANT_ADMIN')")
@Tag(name = "Développeurs", description = "Invitation de comptes développeur sur le tenant (via Kernel Core)")
public class DeveloperInviteController {

    private final DeveloperInviteService service;

    public DeveloperInviteController(DeveloperInviteService service) {
        this.service = service;
    }

    @PostMapping("/invite")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Inviter un développeur", description = "Crée le compte sur Kernel Core et lui envoie un email de définition de mot de passe.")
    public Mono<Void> invite(@Valid @RequestBody InviteDeveloperRequest request) {
        return Mono.zip(TenantContextHolder.getTenantId(), currentBearerToken())
                .flatMap(t -> service.invite(t.getT2(), t.getT1(), request.firstName(), request.lastName(), request.email()));
    }

    /**
     * Une clé API (portail développeur) n'a pas de JWT Kernel Core à transmettre : l'invitation
     * exige donc une session admin authentifiée par email/mot de passe (JwtAuthenticationToken).
     */
    private Mono<String> currentBearerToken() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .ofType(JwtAuthenticationToken.class)
                .map(auth -> auth.getToken().getTokenValue())
                .switchIfEmpty(Mono.error(new DeveloperInviteException(
                        "L'invitation d'un développeur nécessite une connexion admin par email/mot de passe, pas une clé API.")));
    }
}
