package com.yowyob.loyalty.api.tenant;

import com.yowyob.loyalty.api.tenant.dto.ApplicationResponse;
import com.yowyob.loyalty.api.tenant.dto.CreateApplicationRequest;
import com.yowyob.loyalty.api.tenant.dto.UpdateApplicationRequest;
import com.yowyob.loyalty.application.tenant.IntegrationApplicationService;
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
@RequestMapping("/api/v1/admin/applications")
@PreAuthorize("hasRole('TENANT_ADMIN')")
@Tag(name = "Applications", description = "Gestion des applications d'intégration (clé publique + clé privée + callback)")
public class IntegrationApplicationController {

    private final IntegrationApplicationService service;

    public IntegrationApplicationController(IntegrationApplicationService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une application",
            description = "Génère la clé publique, la clé privée et, si une URL de callback est fournie, le secret webhook. Les secrets ne sont affichés qu'une seule fois.")
    public Mono<ApplicationResponse> create(@Valid @RequestBody CreateApplicationRequest request) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> service.create(tenantId, request.name(), request.description(),
                        request.websiteUrl(), request.logoUrl(), request.mode(),
                        request.callbackUrl(), request.eventTypes()))
                .map(created -> ApplicationResponse.fromCreated(created,
                        created.webhookSecret() != null ? requestCallbackUrl(request) : null));
    }

    @GetMapping
    @Operation(summary = "Lister les applications du tenant")
    public Flux<ApplicationResponse> list() {
        return TenantContextHolder.getTenantId()
                .flatMapMany(service::list)
                .map(ApplicationResponse::from);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'une application")
    public Mono<ApplicationResponse> get(@PathVariable UUID id) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> service.get(tenantId, id))
                .map(ApplicationResponse::from);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Mettre à jour une application",
            description = "callbackUrl vide supprime le callback ; une nouvelle URL sur une application sans callback crée un endpoint dont le secret n'est affiché qu'une seule fois.")
    public Mono<ApplicationResponse> update(@PathVariable UUID id, @RequestBody UpdateApplicationRequest request) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> service.update(tenantId, id, request.name(), request.description(),
                        request.websiteUrl(), request.logoUrl(), request.callbackUrl(), request.eventTypes()))
                .map(created -> ApplicationResponse.fromCreated(created, request.callbackUrl()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer une application",
            description = "Révoque la clé privée et supprime l'endpoint webhook associé.")
    public Mono<Void> delete(@PathVariable UUID id) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> service.delete(tenantId, id));
    }

    @PostMapping("/{id}/rotate-private-key")
    @Operation(summary = "Régénérer la clé privée", description = "La nouvelle clé n'est affichée qu'une seule fois ; l'ancienne est révoquée.")
    public Mono<ApplicationResponse> rotatePrivateKey(@PathVariable UUID id) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> service.rotatePrivateKey(tenantId, id))
                .map(created -> ApplicationResponse.fromCreated(created, null));
    }

    @PostMapping("/{id}/rotate-webhook-secret")
    @Operation(summary = "Régénérer le secret webhook", description = "Le nouveau secret n'est affiché qu'une seule fois.")
    public Mono<ApplicationResponse> rotateWebhookSecret(@PathVariable UUID id) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> service.rotateWebhookSecret(tenantId, id))
                .map(created -> ApplicationResponse.fromCreated(created, null));
    }

    private static String requestCallbackUrl(CreateApplicationRequest request) {
        return request.callbackUrl() != null && !request.callbackUrl().isBlank() ? request.callbackUrl() : null;
    }
}
