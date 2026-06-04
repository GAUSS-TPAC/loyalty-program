package com.yowyob.loyalty.infrastructure.bonification.adapter;

import com.yowyob.loyalty.domain.bonification.exception.BonificationException;
import com.yowyob.loyalty.domain.bonification.model.BonificationCredentials;
import com.yowyob.loyalty.domain.bonification.model.BonificationTransactionRequest;
import com.yowyob.loyalty.domain.bonification.model.BonificationTransactionResult;
import com.yowyob.loyalty.domain.bonification.port.out.BonificationPort;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;

public class BonificationDisabledAdapter implements BonificationPort {

    @Override
    public Mono<Boolean> checkConnectivity() {
        return Mono.just(false);
    }

    @Override
    public Mono<Boolean> verifyCredentials(BonificationCredentials credentials) {
        return Mono.just(false);
    }

    @Override
    public Mono<BonificationTransactionResult> submitTransaction(
            TenantId tenantId,
            BonificationCredentials credentials,
            BonificationTransactionRequest request
    ) {
        return Mono.error(new BonificationException("Intégration API Bonification désactivée (app.bonification.enabled=false)"));
    }
}
