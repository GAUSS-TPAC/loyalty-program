package com.yowyob.loyalty.infrastructure.kernelcore.adapter;

import com.yowyob.loyalty.domain.shared.model.AuditInfo;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.model.Tenant;
import com.yowyob.loyalty.domain.tenant.model.TenantConfig;
import com.yowyob.loyalty.domain.tenant.model.enums.TenantPlan;
import com.yowyob.loyalty.domain.tenant.model.enums.TenantStatus;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelApiResponse;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelOrganizationDto;
import com.yowyob.loyalty.infrastructure.redis.adapter.TenantCacheAdapter;
import com.yowyob.loyalty.shared.exception.TenantNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

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
        return tokenService.getServiceToken()
                .flatMap(token -> fetchOrganization(tenantId, token))
                .switchIfEmpty(Mono.defer(() -> fetchOrganization(tenantId, null)))
                .map(org -> toTenant(tenantId, org))
                .flatMap(tenant -> tenantCache.cache(tenant).thenReturn(tenant))
                .doOnSuccess(t -> log.debug("Tenant résolu depuis Kernel Core: {}", tenantId))
                .doOnError(e -> log.warn("Échec résolution Kernel Core pour {}: {}", tenantId, e.getMessage()));
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
