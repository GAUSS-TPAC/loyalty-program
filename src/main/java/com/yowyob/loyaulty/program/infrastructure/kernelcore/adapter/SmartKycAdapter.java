package com.yowyob.loyaulty.program.infrastructure.kernelcore.adapter;

import com.yowyob.loyaulty.program.domain.wallet.model.KycStatus;
import com.yowyob.loyaulty.program.domain.wallet.port.out.KycVerificationPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Placeholder pour l'adapter Smart KYC — fourni par le Kernel Core.
 *
 * <p>ACTIF uniquement quand le profil {@code stub} N'EST PAS actif.
 * Ce fichier est un squelette indiquant le travail à faire quand
 * le Kernel Core sera disponible.</p>
 *
 * <h2>Pour implémenter ce adapter :</h2>
 * <ol>
 *   <li>Injecter {@code KernelCoreProperties} et un {@code WebClient}.</li>
 *   <li>Implémenter l'authentification OAuth2 client_credentials.</li>
 *   <li>Remplacer chaque {@code throw} par un appel WebClient vers Smart KYC.</li>
 * </ol>
 */
@Component
@Profile("!stub")
public class SmartKycAdapter implements KycVerificationPort {

    @Override
    public Mono<Boolean> isMemberVerified(UUID tenantId, UUID memberId) {
        // TODO: GET {KERNEL_CORE_URL}/api/v1/kyc/members/{memberId}/verified
        //       Header: X-Tenant-ID: {tenantId}
        //       Retourne true si le champ "kycStatus" == "VERIFIED"
        throw new UnsupportedOperationException(
                "TODO: implémenter quand le Kernel Core sera disponible. " +
                "Endpoint: GET /api/v1/kyc/members/{memberId}/verified");
    }

    @Override
    public Mono<KycStatus> getMemberKycStatus(UUID tenantId, UUID memberId) {
        // TODO: GET {KERNEL_CORE_URL}/api/v1/kyc/members/{memberId}/status
        //       Header: X-Tenant-ID: {tenantId}
        //       Mapper la réponse vers KycStatus
        throw new UnsupportedOperationException(
                "TODO: implémenter quand le Kernel Core sera disponible. " +
                "Endpoint: GET /api/v1/kyc/members/{memberId}/status");
    }
}
