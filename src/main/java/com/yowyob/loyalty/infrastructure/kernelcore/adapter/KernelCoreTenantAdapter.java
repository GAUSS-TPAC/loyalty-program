package com.yowyob.loyalty.infrastructure.kernelcore.adapter;

import com.yowyob.loyalty.domain.shared.model.AuditInfo;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.model.Tenant;
import com.yowyob.loyalty.domain.tenant.model.TenantConfig;
import com.yowyob.loyalty.domain.tenant.model.enums.TenantPlan;
import com.yowyob.loyalty.domain.tenant.model.enums.TenantStatus;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelApiResponse;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelCreateOrganizationRequestDto;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelOrganizationDto;
import com.yowyob.loyalty.infrastructure.redis.adapter.TenantCacheAdapter;
import com.yowyob.loyalty.shared.exception.KernelCoreUnavailableException;
import com.yowyob.loyalty.shared.exception.RegistrationFailedException;
import com.yowyob.loyalty.shared.exception.TenantNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Résout et valide un tenant via Kernel Core.
 * Endpoint : GET /api/organizations/{organizationId}
 * Résultat mis en cache Redis 5 min.
 */
@Component
public class KernelCoreTenantAdapter {

    private static final Logger log = LoggerFactory.getLogger(KernelCoreTenantAdapter.class);

    private static final ParameterizedTypeReference<KernelApiResponse<KernelOrganizationDto>> ORG_TYPE =
            new ParameterizedTypeReference<>() {};

    private final WebClient kernelCoreWebClient;
    private final KernelCoreTokenService tokenService;
    private final TenantCacheAdapter tenantCache;

    public KernelCoreTenantAdapter(
            @Qualifier("kernelCoreWebClient") WebClient kernelCoreWebClient,
            KernelCoreTokenService tokenService,
            TenantCacheAdapter tenantCache) {
        this.kernelCoreWebClient = kernelCoreWebClient;
        this.tokenService = tokenService;
        this.tenantCache = tenantCache;
    }

    public Mono<Tenant> fetchAndCache(TenantId tenantId) {
        return fetchAndCache(tenantId, null);
    }

    /**
     * @param userBearerToken JWT de l'admin courant, utilisé à défaut de token de service
     *                        configuré (KERNEL_TOKEN_ENDPOINT, souvent inactif). GET
     *                        /api/organizations/{id} exige côté Kernel Core soit un token
     *                        client_credentials, soit un Bearer utilisateur valide — sans l'un
     *                        des deux, Kernel Core répond 500 "Access Denied" (vérifié en
     *                        conditions réelles).
     */
    public Mono<Tenant> fetchAndCache(TenantId tenantId, String userBearerToken) {
        return tokenService.getServiceToken()
                .flatMap(token -> fetchOrganization(tenantId, token))
                .switchIfEmpty(Mono.defer(() -> fetchOrganization(tenantId, userBearerToken)))
                .map(org -> toTenant(tenantId, org))
                .flatMap(tenant -> tenantCache.cache(tenant).thenReturn(tenant))
                .doOnSuccess(t -> log.debug("Tenant résolu depuis Kernel Core: {}", tenantId))
                .doOnError(e -> log.warn("Échec résolution Kernel Core pour {}: {}", tenantId, e.getMessage()));
    }

    /**
     * Crée une organisation KernelCore pour l'acteur porteur du Bearer token, via
     * POST /api/organizations. Utilisée pour auto-provisionner un espace de travail au
     * premier login d'un compte inscrit en self-service (voir AuthService.register/login) :
     * l'inscription publique (sign-up) ne crée qu'un compte, jamais d'organisation.
     */
    public Mono<KernelOrganizationDto> createOrganization(String bearerToken, UUID businessActorId,
                                                           String code, String legalName, String displayName) {
        return kernelCoreWebClient.post()
                .uri("/api/organizations")
                .headers(headers -> headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + bearerToken))
                .bodyValue(new KernelCreateOrganizationRequestDto(businessActorId, code, legalName, displayName, "PRIVATE_COMPANY"))
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError,
                        resp -> resp.bodyToMono(String.class).defaultIfEmpty("")
                                .flatMap(body -> Mono.error(new RegistrationFailedException(
                                        "Création de l'organisation refusée: " + body))))
                .onStatus(HttpStatusCode::is5xxServerError,
                        resp -> Mono.error(new KernelCoreUnavailableException("KernelCore indisponible pour la création de l'organisation")))
                .bodyToMono(ORG_TYPE)
                .flatMap(response -> {
                    if (!response.isSuccess() || response.getData() == null) {
                        return Mono.error(new KernelCoreUnavailableException("Réponse KernelCore invalide pour POST /api/organizations"));
                    }
                    return Mono.just(response.getData());
                })
                .doOnError(e -> log.warn("Échec création d'organisation KernelCore: {}", e.getMessage()));
    }

    private Mono<KernelOrganizationDto> fetchOrganization(TenantId tenantId, String token) {
        return kernelCoreWebClient.get()
                .uri("/api/organizations/{id}", tenantId.value())
                .headers(headers -> {
                    if (token != null && !token.isBlank()) {
                        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                    }
                })
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        resp -> Mono.error(new TenantNotFoundException("Organisation introuvable dans Kernel Core: " + tenantId.value())))
                .onStatus(status -> status.is4xxClientError(),
                        resp -> Mono.error(new TenantNotFoundException("Accès refusé pour l'organisation: " + tenantId.value())))
                .bodyToMono(ORG_TYPE)
                .flatMap(response -> {
                    if (!response.isSuccess() || response.getData() == null) {
                        return Mono.error(new TenantNotFoundException("Réponse Kernel Core invalide pour: " + tenantId.value()));
                    }
                    return Mono.just(response.getData());
                });
    }

    private Tenant toTenant(TenantId tenantId, KernelOrganizationDto org) {
        return new Tenant(
                tenantId,
                org.resolveName(),
                org.resolveSlug(),
                mapStatus(org),
                TenantPlan.FREE,
                TenantConfig.defaults(),
                AuditInfo.now("kernel-core")
        );
    }

    private TenantStatus mapStatus(KernelOrganizationDto org) {
        if (!org.isActive()) return TenantStatus.SUSPENDED;
        if (org.getStatus() == null) return TenantStatus.ACTIVE;
        return switch (org.getStatus().toUpperCase()) {
            case "ACTIVE"                       -> TenantStatus.ACTIVE;
            case "SUSPENDED", "INACTIVE",
                 "CLOSED", "REJECTED"           -> TenantStatus.SUSPENDED;
            case "PENDING", "PENDING_APPROVAL"  -> TenantStatus.PENDING_SETUP;
            default                             -> TenantStatus.ACTIVE;
        };
    }
}
