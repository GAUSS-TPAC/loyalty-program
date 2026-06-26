package com.yowyob.loyalty.application.wallet.handler;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.exception.WalletDomainException;
import com.yowyob.loyalty.domain.wallet.model.WalletDebitResult;
import com.yowyob.loyalty.domain.wallet.port.in.ConfirmOtpUseCase;
import com.yowyob.loyalty.domain.wallet.port.out.OtpChallengePort;
import com.yowyob.loyalty.domain.wallet.service.WalletDomainService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
public class ConfirmOtpHandler implements ConfirmOtpUseCase {

    private final WalletDomainService domainService;
    private final OtpChallengePort otpChallengePort;

    public ConfirmOtpHandler(WalletDomainService domainService, OtpChallengePort otpChallengePort) {
        this.domainService = domainService;
        this.otpChallengePort = otpChallengePort;
    }

    @Override
    public Mono<WalletDebitResult> confirmOtp(TenantId tenantId, UserId memberId, String challengeId, String otpCode, String idempotencyKey) {
        return otpChallengePort.findById(challengeId)
            .switchIfEmpty(Mono.error(new WalletDomainException("Challenge OTP introuvable ou expiré")))
            .flatMap(challenge -> {
                if (!challenge.tenantId().equals(tenantId) || !challenge.memberId().equals(memberId)) {
                    return Mono.error(new WalletDomainException("Challenge OTP invalide pour ce membre"));
                }
                if (challenge.expiresAt().isBefore(Instant.now())) {
                    return otpChallengePort.delete(challengeId)
                        .then(Mono.error(new WalletDomainException("Challenge OTP expiré")));
                }
                if (!challenge.otpCode().equals(otpCode)) {
                    return Mono.error(new WalletDomainException("Code OTP incorrect"));
                }
                return domainService.debit(
                        tenantId,
                        memberId,
                        challenge.amount(),
                        challenge.description(),
                        challenge.orderReference(),
                        idempotencyKey != null ? idempotencyKey : challenge.idempotencyKey()
                    )
                    .flatMap(result -> otpChallengePort.delete(challengeId).thenReturn(result));
            });
    }
}
