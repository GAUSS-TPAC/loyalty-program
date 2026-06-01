package com.yowyob.loyaulty.program.domain.wallet.port.out;

import com.yowyob.loyaulty.program.domain.wallet.model.KycStatus;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Port de sortie vers le Smart KYC du Kernel Core.
 *
 * <p>Notre domaine délègue la vérification d'identité au Kernel Core.
 * Ce port est la seule façon pour la logique métier d'interroger le
 * statut KYC d'un membre.</p>
 *
 * <p>Zéro annotation Spring — interface du domaine pur.</p>
 */
public interface KycVerificationPort {

    /**
     * Vérifie si un membre a complété et validé son KYC.
     *
     * @param tenantId identifiant UUID du tenant.
     * @param memberId identifiant UUID du membre.
     * @return {@code Mono<true>} si le membre est vérifié, {@code Mono<false>} sinon.
     */
    Mono<Boolean> isMemberVerified(UUID tenantId, UUID memberId);

    /**
     * Retourne le statut KYC détaillé d'un membre.
     *
     * @param tenantId identifiant UUID du tenant.
     * @param memberId identifiant UUID du membre.
     * @return statut KYC courant du membre.
     */
    Mono<KycStatus> getMemberKycStatus(UUID tenantId, UUID memberId);
}
