package com.yowyob.loyalty.application.wallet.handler;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.model.OtpChallenge;
import com.yowyob.loyalty.domain.wallet.model.WalletDebitResult;
import com.yowyob.loyalty.domain.wallet.port.in.DebitWalletUseCase;
import com.yowyob.loyalty.domain.wallet.port.out.IdempotencyPort;
import com.yowyob.loyalty.domain.wallet.port.out.OtpChallengePort;
import com.yowyob.loyalty.domain.wallet.service.WalletDomainService;
import com.yowyob.loyalty.shared.exception.IdempotencyConflictException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class DebitWalletHandler implements DebitWalletUseCase {

    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final WalletDomainService domainService;
    private final IdempotencyPort idempotency;
    private final OtpChallengePort otpChallengePort;

    public DebitWalletHandler(WalletDomainService domainService, IdempotencyPort idempotency,
                               OtpChallengePort otpChallengePort) {
        this.domainService = domainService;
        this.idempotency = idempotency;
        this.otpChallengePort = otpChallengePort;
    }

    @Override
    public Mono<WalletDebitResult> debit(TenantId tenantId, UserId memberId, BigDecimal amount,
                                          String description, String orderReference, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            return executeDebit(tenantId, memberId, amount, description, orderReference, null);
        }

        String tenantStr = tenantId.value().toString();
        return idempotency.registerIfAbsent(idempotencyKey, tenantStr, IDEMPOTENCY_TTL, "PROCESSING")
                .flatMap(registered -> {
                    if (!registered) {
                        return Mono.error(new IdempotencyConflictException(idempotencyKey));
                    }
                    return executeDebit(tenantId, memberId, amount, description, orderReference, idempotencyKey);
                });
    }

    private Mono<WalletDebitResult> executeDebit(TenantId tenantId, UserId memberId, BigDecimal amount,
                                                   String description, String orderReference, String idempotencyKey) {
        return domainService.debit(tenantId, memberId, amount, description, orderReference, idempotencyKey)
                .flatMap(result -> {
                    if (result.otpRequired()) {
                        String challengeId = UUID.randomUUID().toString();
                        String otpCode = String.format("%06d", RANDOM.nextInt(1_000_000));
                        OtpChallenge challenge = new OtpChallenge(
                                challengeId,
                                result.updatedWallet().getId(),
                                tenantId,
                                memberId,
                                amount,
                                description,
                                orderReference,
                                idempotencyKey,
                                otpCode,
                                Instant.now().plus(OTP_TTL)
                        );
                        return otpChallengePort.store(challenge, OTP_TTL)
                                .thenReturn(new WalletDebitResult(
                                        result.updatedWallet(),
                                        result.amountDebited(),
                                        result.newBalance(),
                                        true,
                                        challengeId
                                ));
                    }
                    return Mono.just(result);
                });
    }
}
