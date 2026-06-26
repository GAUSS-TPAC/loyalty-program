package com.yowyob.loyalty.domain.subscription.port.in;

import reactor.core.publisher.Mono;

public interface ProcessSubscriptionRenewalUseCase {
    Mono<Integer> processExpiredTrials();
    Mono<Integer> processExpiredSubscriptions();
    Mono<Integer> processOverdueInvoices();
}
