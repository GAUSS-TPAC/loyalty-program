package com.yowyob.loyalty.domain.subscription.port.in;

import com.yowyob.loyalty.domain.subscription.model.PlatformTenantSummary;
import reactor.core.publisher.Flux;

public interface ListPlatformTenantsUseCase {
    Flux<PlatformTenantSummary> listSubscribedTenants();
}
