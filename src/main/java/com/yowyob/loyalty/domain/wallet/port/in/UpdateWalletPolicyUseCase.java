package com.yowyob.loyalty.domain.wallet.port.in;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.wallet.model.WalletPolicy;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;

public interface UpdateWalletPolicyUseCase {
    Mono<WalletPolicy> updatePointsConversion(
        TenantId tenantId, String currencyName, String currencySymbol, BigDecimal exchangeRate);
}
