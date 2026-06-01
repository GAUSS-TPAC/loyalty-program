package com.yowyob.loyaulty.program.domain.shared.port.out;

import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Port de sortie vers le Kernel Core pour les informations de tenant.
 *
 * <p>Notre domaine n'héberge plus les entités tenant. Ces données proviennent
 * du Kernel Core (claims JWT pour les infos de base, appels API pour les
 * informations métier nécessitant une vérification temps réel).</p>
 *
 * <p>Zéro annotation Spring — interface du domaine pur.</p>
 */
public interface TenantQueryPort {

    /**
     * Vérifie si un tenant existe et est actif dans le Kernel Core.
     *
     * @param tenantId identifiant UUID du tenant.
     * @return {@code Mono<true>} si le tenant existe, {@code Mono<false>} sinon.
     */
    Mono<Boolean> tenantExists(UUID tenantId);

    /**
     * Retourne le code devise ISO 4217 configuré pour un tenant.
     *
     * @param tenantId identifiant UUID du tenant.
     * @return code devise, ex. "XAF", "EUR", "USD".
     */
    Mono<String> getTenantCurrencyCode(UUID tenantId);

    /**
     * Retourne le nombre maximum de règles de fidélisation pour un tenant.
     * Déterminé par le plan d'abonnement du tenant.
     *
     * @param tenantId identifiant UUID du tenant.
     * @return nombre maximum de règles autorisées.
     */
    Mono<Integer> getTenantMaxRules(UUID tenantId);
}
