package com.yowyob.loyalty.domain.wallet.port.in;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.model.WalletDebitResult;
import reactor.core.publisher.Mono;

public interface ConfirmOtpUseCase {
    Mono<WalletDebitResult> confirmOtp(TenantId tenantId, UserId memberId, String challengeId, String otpCode, String idempotencyKey);
}
