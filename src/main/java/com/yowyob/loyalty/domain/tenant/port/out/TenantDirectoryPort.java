package com.yowyob.loyalty.domain.tenant.port.out;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.model.Tenant;
import reactor.core.publisher.Mono;

/**
 * Résolution d'un tenant (nom, statut) depuis le référentiel d'organisations
 * externe (Kernel Core), pour les vues plateforme qui doivent afficher des
 * informations lisibles au-delà du seul tenantId.
 */
public interface TenantDirectoryPort {
    Mono<Tenant> resolveTenant(TenantId tenantId);
}
