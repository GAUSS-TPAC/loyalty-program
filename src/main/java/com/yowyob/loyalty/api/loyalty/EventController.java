package com.yowyob.loyalty.api.loyalty;

import com.yowyob.loyalty.api.loyalty.dto.request.IncomingEventRequest;
import com.yowyob.loyalty.api.loyalty.dto.response.EventProcessingResponse;
import com.yowyob.loyalty.application.loyalty.handler.ProcessEventHandler;
import com.yowyob.loyalty.application.tenant.ApiKeyService;
import com.yowyob.loyalty.domain.tenant.model.ApiKey;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@Tag(name = "Events", description = "Traitement des événements loyalty")
public class EventController {

    private static final String API_KEY_PRINCIPAL_PREFIX = "api-key:";

    private final ProcessEventHandler processEventHandler;
    private final ApiKeyService apiKeyService;

    public EventController(ProcessEventHandler processEventHandler, ApiKeyService apiKeyService) {
        this.processEventHandler = processEventHandler;
        this.apiKeyService = apiKeyService;
    }

    @PostMapping
    public Mono<EventProcessingResponse> processEvent(
            @Valid @RequestBody IncomingEventRequest request,
            @RequestHeader(value = HttpHeaders.CONTENT_TYPE, required = false) String contentType,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestHeader(value = "X-Api-Key", required = false) String rawApiKey
    ) {
        return Mono.zip(TenantContextHolder.getTenantId(), resolveApiKeyId(rawApiKey))
                .map(tuple -> LoyaltyApiMapper.toIncomingEvent(
                        request, tuple.getT1(), idempotencyKey, tuple.getT2().orElse(null)))
                .flatMap(processEventHandler::handle)
                .map(EventProcessingResponse::from);
    }

    /**
     * Attribue l'événement à la clé API appelante pour le suivi du flux de points
     * par application : via le principal posé par ApiKeyResolutionFilter en prod,
     * ou directement via le header X-Api-Key (seul chemin en profil dev où le
     * filtre est désactivé).
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
