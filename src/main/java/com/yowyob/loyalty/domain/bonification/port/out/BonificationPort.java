package com.yowyob.loyalty.domain.bonification.port.out;

import com.yowyob.loyalty.domain.bonification.model.BonificationCredentials;
import com.yowyob.loyalty.domain.bonification.model.BonificationTransactionRequest;
import com.yowyob.loyalty.domain.bonification.model.BonificationTransactionResult;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

/**
 * Port vers l'API de bonification externe (bonusapi).
 * @see <a href="https://bonus-api-presentation1.vercel.app/">Documentation</a>
 */
public interface BonificationPort {

    Mono<Boolean> checkConnectivity();

    Mono<Boolean> verifyCredentials(BonificationCredentials credentials);

    Mono<BonificationTransactionResult> submitTransaction(
            TenantId tenantId,
            BonificationCredentials credentials,
            BonificationTransactionRequest request
    );
}
