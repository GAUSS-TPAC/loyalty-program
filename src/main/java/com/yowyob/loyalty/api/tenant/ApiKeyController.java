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
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/api-keys")
@PreAuthorize("hasRole('TENANT_ADMIN')")
@Tag(name = "API Keys", description = "Gestion des clés API pour l'authentification des organisations")
public class ApiKeyController {

    private final ApiKeyService service;

    public ApiKeyController(ApiKeyService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une clé API", description = "Génère une nouvelle clé API pour le tenant courant. La clé brute n'est affichée qu'une seule fois.")
    public Mono<ApiKeyResponse> create(@Valid @RequestBody CreateApiKeyRequest request) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> service.create(tenantId, request.name()))
                .map(created -> ApiKeyResponse.fromCreated(created.record(), created.rawKey()));
    }

    @GetMapping
    @Operation(summary = "Lister les clés API du tenant")
    public Flux<ApiKeyResponse> list() {
        return TenantContextHolder.getTenantId()
                .flatMapMany(service::listForTenant)
                .map(ApiKeyResponse::from);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Révoquer une clé API")
    public Mono<Void> revoke(@PathVariable UUID id) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> service.revoke(tenantId, id));
    }
}
