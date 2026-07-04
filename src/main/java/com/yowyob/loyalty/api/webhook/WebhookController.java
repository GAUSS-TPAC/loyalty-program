package com.yowyob.loyalty.api.webhook;

import com.yowyob.loyalty.api.webhook.dto.*;
import com.yowyob.loyalty.application.webhook.WebhookDispatchService;
import com.yowyob.loyalty.application.webhook.WebhookEndpointService;
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
@RequestMapping("/api/v1/admin/webhooks")
@PreAuthorize("hasRole('TENANT_ADMIN')")
@Tag(name = "Webhooks", description = "Gestion des points de terminaison webhook pour l'intégration des systèmes tiers")
public class WebhookController {

    private final WebhookEndpointService endpointService;
    private final WebhookDispatchService dispatchService;

    public WebhookController(WebhookEndpointService endpointService, WebhookDispatchService dispatchService) {
        this.endpointService = endpointService;
        this.dispatchService = dispatchService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer un webhook", description = "Enregistre une URL de callback pour un ensemble d'événements. Le secret n'est affiché qu'une seule fois.")
    public Mono<WebhookEndpointResponse> create(@Valid @RequestBody CreateWebhookEndpointRequest request) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> endpointService.create(tenantId, request.url(), request.description(), request.eventTypes()))
                .map(created -> WebhookEndpointResponse.withSecret(created.endpoint(), created.plainSecret()));
    }

    @GetMapping
    @Operation(summary = "Lister les webhooks du tenant")
    public Flux<WebhookEndpointResponse> list() {
        return TenantContextHolder.getTenantId()
                .flatMapMany(endpointService::listForTenant)
                .map(WebhookEndpointResponse::from);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail d'un webhook")
    public Mono<WebhookEndpointResponse> get(@PathVariable UUID id) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> endpointService.get(tenantId, id))
                .map(WebhookEndpointResponse::from);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Mettre à jour un webhook")
    public Mono<WebhookEndpointResponse> update(@PathVariable UUID id, @RequestBody UpdateWebhookEndpointRequest request) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> endpointService.update(tenantId, id, request.url(), request.description(),
                        request.eventTypes(), request.active()))
                .map(WebhookEndpointResponse::from);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Supprimer un webhook")
    public Mono<Void> delete(@PathVariable UUID id) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> endpointService.delete(tenantId, id));
    }

    @PostMapping("/{id}/rotate-secret")
    @Operation(summary = "Régénérer le secret de signature", description = "Le nouveau secret n'est affiché qu'une seule fois.")
    public Mono<WebhookEndpointResponse> rotateSecret(@PathVariable UUID id) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> endpointService.rotateSecret(tenantId, id))
                .map(created -> WebhookEndpointResponse.withSecret(created.endpoint(), created.plainSecret()));
    }

    @PostMapping("/{id}/test")
    @Operation(summary = "Envoyer un ping de test", description = "Envoie une charge utile de test signée à l'URL enregistrée.")
    public Mono<TestPingResponse> sendTestPing(@PathVariable UUID id) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> dispatchService.sendTestPing(tenantId, id))
                .map(TestPingResponse::from);
    }

    @GetMapping("/deliveries")
    @Operation(summary = "Journal des livraisons webhook", description = "Historique des tentatives de livraison pour tous les webhooks du tenant.")
    public Flux<WebhookDeliveryResponse> listDeliveries(@RequestParam(defaultValue = "0") int page,
                                                          @RequestParam(defaultValue = "20") int size) {
        return TenantContextHolder.getTenantId()
                .flatMapMany(tenantId -> dispatchService.listDeliveries(tenantId, page, size))
                .map(WebhookDeliveryResponse::from);
    }
}
