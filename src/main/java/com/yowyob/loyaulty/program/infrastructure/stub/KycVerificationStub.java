package com.yowyob.loyaulty.program.infrastructure.stub;

import com.yowyob.loyaulty.program.domain.wallet.model.KycStatus;
import com.yowyob.loyaulty.program.domain.wallet.port.out.KycVerificationPort;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Stub de développement pour {@link KycVerificationPort}.
 *
 * <p>Actif uniquement avec le profil Spring {@code stub}.
 * En mode stub, tous les membres sont considérés comme vérifiés KYC.
 * À remplacer par {@code SmartKycAdapter} quand le Kernel Core sera disponible.</p>
 */
@Slf4j
@Component
@Profile("stub")
public class KycVerificationStub implements KycVerificationPort {

    @PostConstruct
    public void logWarning() {
        log.warn("[STUB] KycVerificationStub actif — tous les membres sont considérés comme vérifiés. " +
                "Remplacer par SmartKycAdapter en production.");
    }

    @Override
    public Mono<Boolean> isMemberVerified(UUID tenantId, UUID memberId) {
        return Mono.just(true);
    }

    @Override
    public Mono<KycStatus> getMemberKycStatus(UUID tenantId, UUID memberId) {
        return Mono.just(KycStatus.VERIFIED);
    }
}
