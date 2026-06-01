package com.yowyob.loyalty.shared.multitenancy;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.model.enums.TenantPlan;
import com.yowyob.loyalty.domain.tenant.model.enums.TenantStatus;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

public class TenantContextHolderTest {

    @Test
    public void testGetTenantContextWithValidContext() {
        TenantId tenantId = TenantId.of(UUID.randomUUID());
        TenantContext ctx = new TenantContext(tenantId, "Test Tenant", TenantStatus.ACTIVE, TenantPlan.PRO);

        Mono<TenantContext> contextMono = TenantContextHolder.getTenantContext()
                .contextWrite(TenantContextHolder.withTenantContext(ctx));

        StepVerifier.create(contextMono)
                .expectNext(ctx)
                .verifyComplete();
    }

    @Test
    public void testGetTenantContextMissingThrowsException() {
        Mono<TenantContext> contextMono = TenantContextHolder.getTenantContext();

        StepVerifier.create(contextMono)
                .expectError(TenantContextMissingException.class)
                .verify();
    }
}
