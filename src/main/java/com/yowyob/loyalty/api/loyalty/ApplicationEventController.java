package com.yowyob.loyalty.api.loyalty;

import com.yowyob.loyalty.api.loyalty.dto.request.IncomingEventRequest;
import com.yowyob.loyalty.api.loyalty.dto.response.EventProcessingResponse;
import com.yowyob.loyalty.application.loyalty.handler.ProcessEventHandler;
import com.yowyob.loyalty.application.tenant.ApiKeyService;
import com.yowyob.loyalty.application.tenant.IntegrationApplicationService;
import com.yowyob.loyalty.domain.tenant.exception.ApplicationNotFoundException;
import com.yowyob.loyalty.domain.tenant.model.ApiKey;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

/**
 * Ingestion d'événements scopée par application, à la My-CoolPay : la clé publique
 * dans l'URL identifie l'application appelante, la clé privée (X-Api-Key) authentifie.
 */
@RestController
@RequestMapping("/api/v1/apps/{publicKey}/events")
@Tag(name = "Application Events", description = "Traitement des événements loyalty au nom d'une application d'intégration")
public class ApplicationEventController {

    private static final String API_KEY_PRINCIPAL_PREFIX = "api-key:";

    private final ProcessEventHandler processEventHandler;
    private final ApiKeyService apiKeyService;
    private final IntegrationApplicationService applicationService;

    public ApplicationEventController(ProcessEventHandler processEventHandler,
                                      ApiKeyService apiKeyService,
                                      IntegrationApplicationService applicationService) {
        this.processEventHandler = processEventHandler;
        this.apiKeyService = apiKeyService;
        this.applicationService = applicationService;
    }

    @PostMapping
    @Operation(summary = "Envoyer un événement au nom d'une application",
            description = "La clé publique de l'URL doit appartenir au tenant authentifié par X-Api-Key.")
    public Mono<EventProcessingResponse> processEvent(
            @PathVariable String publicKey,
            @Valid @RequestBody IncomingEventRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(value = "X-Api-Key", required = false) String rawApiKey
    ) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> applicationService.findByPublicKeyForTenant(tenantId, publicKey)
                        .switchIfEmpty(Mono.error(new ApplicationNotFoundException(publicKey)))
                        .flatMap(app -> resolveApiKeyId(rawApiKey)
                                .map(apiKeyId -> LoyaltyApiMapper.toIncomingEvent(
                                        request, tenantId, idempotencyKey, apiKeyId.orElse(null)))))
                .flatMap(processEventHandler::handle)
                .map(EventProcessingResponse::from);
    }

    /**
     * Attribue l'événement à la clé API appelante (même logique que EventController) :
     * principal posé par ApiKeyResolutionFilter en prod, header X-Api-Key en dev.
     */
    private Mono<Optional<UUID>> resolveApiKeyId(String rawApiKey) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication() != null ? ctx.getAuthentication().getName() : "")
                .filter(name -> name.startsWith(API_KEY_PRINCIPAL_PREFIX))
                .map(name -> Optional.of(UUID.fromString(name.substring(API_KEY_PRINCIPAL_PREFIX.length()))))
                .switchIfEmpty(Mono.defer(() -> rawApiKey == null || rawApiKey.isBlank()
                        ? Mono.just(Optional.empty())
                        : apiKeyService.validate(rawApiKey)
                                .map(ApiKey::id)
                                .map(Optional::of)
                                .defaultIfEmpty(Optional.empty())))
                .onErrorReturn(Optional.empty());
    }
}
