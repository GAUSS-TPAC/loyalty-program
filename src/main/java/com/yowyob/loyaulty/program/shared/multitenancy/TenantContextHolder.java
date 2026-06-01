package com.yowyob.loyaulty.program.shared.multitenancy;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.shared.exception.ErrorCode;
import com.yowyob.loyaulty.program.shared.exception.AppException;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.function.Function;

public final class TenantContextHolder {

    public static final Class<TenantContext> CONTEXT_KEY = TenantContext.class;

    private TenantContextHolder() {}

    public static Mono<TenantContext> getTenantContext() {
        return Mono.deferContextual(ctx -> {
            if (ctx.hasKey(CONTEXT_KEY)) {
                return Mono.just(ctx.get(CONTEXT_KEY));
            }
            return Mono.error(new TenantContextMissingException());
        });
    }

    public static Mono<TenantId> getTenantId() {
        return getTenantContext().map(TenantContext::tenantId);
    }

    public static Function<Context, Context> withTenantContext(TenantContext tenantContext) {
        return ctx -> ctx.put(CONTEXT_KEY, tenantContext);
    }

    public static class TenantContextMissingException extends AppException {
        public TenantContextMissingException() {
            super(ErrorCode.TENANT_CONTEXT_MISSING, "TenantContext is missing from the reactive context");
        }
    }
}
