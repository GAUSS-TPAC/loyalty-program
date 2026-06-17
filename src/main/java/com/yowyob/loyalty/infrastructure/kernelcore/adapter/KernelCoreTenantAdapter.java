package com.yowyob.loyalty.infrastructure.kernelcore.adapter;

import com.yowyob.loyalty.domain.shared.model.AuditInfo;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.model.Tenant;
import com.yowyob.loyalty.domain.tenant.model.TenantConfig;
import com.yowyob.loyalty.domain.tenant.model.enums.TenantPlan;
import com.yowyob.loyalty.domain.tenant.model.enums.TenantStatus;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelCoreOrganizationDto;
import com.yowyob.loyalty.infrastructure.kernelcore.service.KernelCoreTokenService;
import com.yowyob.loyalty.infrastructure.redis.adapter.TenantCacheAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Récupère les données d'une organisation depuis le Kernel Core
 * et les met en cache Redis sous forme de Tenant.
 *
 * Endpoint : GET <kernel-core-url>/api/organizations/{id}
 * Auth      : Bearer token OAuth2 client_credentials
 */
public class KernelCoreTenantAdapter {

    private static final Logger log = LoggerFactory.getLogger(KernelCoreTenantAdapter.class);

    private final WebClient webClient;
    private final KernelCoreTokenService tokenService;
    private final TenantCacheAdapter tenantCache;

    public KernelCoreTenantAdapter(
            WebClient webClient,
            KernelCoreTokenService tokenService,
            TenantCacheAdapter tenantCache) {
        this.webClient = webClient;
        this.tokenService = tokenService;
        this.tenantCache = tenantCache;
    }

    /**
     * Récupère l'organisation depuis Kernel Core, la convertit en Tenant
     * et la met en cache Redis avant de la retourner.
     * Retourne {@code Mono.empty()} si l'organisation est introuvable ou en cas d'erreur.
     */
    public Mono<Tenant> fetchAndCache(TenantId tenantId) {
        return tokenService.getServiceToken()
                .flatMap(token -> webClient.get()
                        .uri("/api/organizations/{id}", tenantId.value())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .retrieve()
                        .onStatus(status -> status == HttpStatus.NOT_FOUND,
                                response -> Mono.error(new RuntimeException("Organization not found: " + tenantId.value())))
                        .onStatus(status -> status == HttpStatus.UNAUTHORIZED || status == HttpStatus.FORBIDDEN,
                                response -> {
                                    log.warn("Token Kernel Core invalide pour l'organisation {}", tenantId.value());
                                    return Mono.error(new RuntimeException("Kernel Core token rejected"));
                                })
                        .bodyToMono(KernelCoreOrganizationDto.class))
                .flatMap(dto -> {
                    Tenant tenant = toDomain(dto);
                    return tenantCache.cache(tenant).thenReturn(tenant);
                })
                .doOnError(e -> log.warn("Impossible de récupérer l'organisation {} depuis Kernel Core: {}",
                        tenantId.value(), e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    private Tenant toDomain(KernelCoreOrganizationDto dto) {
        TenantId tenantId = TenantId.of(dto.id());
        return new Tenant(
                tenantId,
                dto.name() != null ? dto.name() : dto.id().toString(),
                dto.slug() != null ? dto.slug() : dto.id().toString(),
                parseStatus(dto.status()),
                parsePlan(dto.plan()),
                TenantConfig.defaults(),
                AuditInfo.now("kernel-core")
        );
    }

    private TenantStatus parseStatus(String raw) {
        if (raw == null) return TenantStatus.PENDING_SETUP;
        try {
            return TenantStatus.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return switch (raw.toUpperCase()) {
                case "ENABLED", "VALIDATED" -> TenantStatus.ACTIVE;
                case "DISABLED", "BLOCKED", "BANNED" -> TenantStatus.SUSPENDED;
                case "TRIAL" -> TenantStatus.TRIAL;
                default -> TenantStatus.PENDING_SETUP;
            };
        }
    }

    private TenantPlan parsePlan(String raw) {
        if (raw == null) return TenantPlan.FREE;
        try {
            return TenantPlan.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return switch (raw.toUpperCase()) {
                case "PREMIUM", "BUSINESS" -> TenantPlan.PRO;
                case "UNLIMITED", "CORPORATE" -> TenantPlan.ENTERPRISE;
                default -> TenantPlan.FREE;
            };
        }
    }
}
