package com.yowyob.loyalty.shared.multitenancy;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.function.Function;

public final class TenantContextHolder {

    static final Class<TenantContext> TENANT_CONTEXT_KEY = TenantContext.class;

    private TenantContextHolder() {
        // Prevent instantiation
    }

    public static Mono<TenantContext> getTenantContext() {
        return Mono.deferContextual(ctx -> 
            ctx.hasKey(TENANT_CONTEXT_KEY) 
                ? Mono.just(ctx.get(TENANT_CONTEXT_KEY)) 
                : Mono.error(new TenantContextMissingException())
        );
    }

    public static Mono<TenantId> getTenantId() {
        return getTenantContext().map(TenantContext::tenantId);
    }

    public static Function<Context, Context> withTenantContext(TenantContext tenantContext) {
        return context -> context.put(TENANT_CONTEXT_KEY, tenantContext);
    }
}
