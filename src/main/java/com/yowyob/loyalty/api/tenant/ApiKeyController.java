package com.yowyob.loyalty.api.tenant;

import com.yowyob.loyalty.api.tenant.dto.ApiKeyResponse;
import com.yowyob.loyalty.api.tenant.dto.CreateApiKeyRequest;
import com.yowyob.loyalty.application.tenant.ApiKeyService;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Un TENANT_ADMIN voit/gère toutes les clés du tenant ; tout autre utilisateur authentifié
 * (développeur) ne voit/gère que les clés dont il est le propriétaire (voir Caller.isAdmin()).
 */
@RestController
@RequestMapping("/api/v1/admin/api-keys")
@PreAuthorize("isAuthenticated()")
@Tag(name = "API Keys", description = "Gestion des clés API pour l'authentification des organisations")
public class ApiKeyController {

    private final ApiKeyService service;

    public ApiKeyController(ApiKeyService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une clé API", description = "Génère une nouvelle clé API pour le tenant courant, attribuée à l'utilisateur courant. La clé brute n'est affichée qu'une seule fois.")
    public Mono<ApiKeyResponse> create(@Valid @RequestBody CreateApiKeyRequest request) {
        return Mono.zip(TenantContextHolder.getTenantId(), currentCaller())
                .flatMap(t -> service.create(t.getT1(), request.name(), request.mode(), t.getT2().id()))
                .map(created -> ApiKeyResponse.fromCreated(created.record(), created.rawKey()));
    }

    @GetMapping
    @Operation(summary = "Lister les clés API accessibles à l'utilisateur courant (toutes pour un admin, les siennes pour un développeur)")
    public Flux<ApiKeyResponse> list() {
        return Mono.zip(TenantContextHolder.getTenantId(), currentCaller())
                .flatMapMany(t -> t.getT2().isAdmin()
                        ? service.listForTenant(t.getT1())
                        : service.listForOwner(t.getT1(), t.getT2().id()))
                .map(ApiKeyResponse::from);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Révoquer une clé API (la sienne pour un développeur, n'importe laquelle pour un admin)")
    public Mono<Void> revoke(@PathVariable UUID id) {
        return Mono.zip(TenantContextHolder.getTenantId(), currentCaller())
                .flatMap(t -> service.revoke(t.getT1(), id, t.getT2().id(), t.getT2().isAdmin()));
    }

    private record Caller(UUID id, boolean isAdmin) {}

    /**
     * L'authentification par clé API (portail développeur, X-Api-Key) produit un
     * UsernamePasswordAuthenticationToken avec ROLE_TENANT_ADMIN (voir ApiKeyResolutionFilter),
     * pas un JwtAuthenticationToken : aucune identité humaine n'est associée, donc id=null
     * (sans conséquence puisque isAdmin est alors toujours true).
     */
    private Mono<Caller> currentCaller() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .map(auth -> new Caller(
                        auth instanceof JwtAuthenticationToken jwt ? UUID.fromString(jwt.getToken().getSubject()) : null,
                        auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TENANT_ADMIN"))));
    }
}
