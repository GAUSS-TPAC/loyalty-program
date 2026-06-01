package com.yowyob.loyaulty.program.infrastructure.kernelcore.adapter;

import com.yowyob.loyaulty.program.domain.shared.port.out.TenantQueryPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Placeholder pour l'adapter Kernel Core — Gestion des Tenants.
 *
 * <p>ACTIF uniquement quand le profil {@code stub} N'EST PAS actif.
 * Ce fichier est un squelette indiquant le travail à faire quand
 * le Kernel Core sera disponible.</p>
 *
 * <h2>Pour implémenter ce adapter :</h2>
 * <ol>
 *   <li>Injecter {@code KernelCoreProperties} et un {@code WebClient}.</li>
 *   <li>Remplacer chaque {@code throw} par un appel WebClient vers le Kernel Core.</li>
 *   <li>Supprimer ce commentaire et la classe deviendra un vrai adapter.</li>
 * </ol>
 */
@Component
@Profile("!stub")
public class KernelCoreTenantAdapter implements TenantQueryPort {

    @Override
    public Mono<Boolean> tenantExists(UUID tenantId) {
        // TODO: GET {KERNEL_CORE_URL}/api/v1/organizations/{tenantId}
        //       Retourne true si le champ "status" == "ACTIVE"
        throw new UnsupportedOperationException(
                "TODO: implémenter quand le Kernel Core sera disponible. " +
                "Endpoint: GET /api/v1/organizations/{tenantId}");
    }

    @Override
    public Mono<String> getTenantCurrencyCode(UUID tenantId) {
        // TODO: GET {KERNEL_CORE_URL}/api/v1/organizations/{tenantId}/settings
        //       Extraire le champ "defaultCurrencyCode"
        throw new UnsupportedOperationException(
                "TODO: implémenter quand le Kernel Core sera disponible. " +
                "Endpoint: GET /api/v1/organizations/{tenantId}/settings");
    }

    @Override
    public Mono<Integer> getTenantMaxRules(UUID tenantId) {
        // TODO: GET {KERNEL_CORE_URL}/api/v1/organizations/{tenantId}/plan
        //       Extraire le champ "maxLoyaltyRules" selon le plan d'abonnement
        throw new UnsupportedOperationException(
                "TODO: implémenter quand le Kernel Core sera disponible. " +
                "Endpoint: GET /api/v1/organizations/{tenantId}/plan");
    }
}
