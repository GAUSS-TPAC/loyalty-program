package com.yowyob.loyalty.infrastructure.kernelcore.adapter;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.model.Tenant;
import com.yowyob.loyalty.domain.tenant.port.out.TenantDirectoryPort;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class TenantDirectoryAdapter implements TenantDirectoryPort {

    private final KernelCoreTenantAdapter kernelCoreTenantAdapter;

    public TenantDirectoryAdapter(KernelCoreTenantAdapter kernelCoreTenantAdapter) {
        this.kernelCoreTenantAdapter = kernelCoreTenantAdapter;
    }

    @Override
    public Mono<Tenant> resolveTenant(TenantId tenantId) {
        return kernelCoreTenantAdapter.fetchAndCache(tenantId);
    }
}
