package com.yowyob.loyalty.api.tenant;

import com.yowyob.loyalty.api.tenant.dto.TenantHealthResponse;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class SecureHealthController {

    @GetMapping("/health")
    @PreAuthorize("isAuthenticated()")
    public Mono<TenantHealthResponse> getSecureHealth() {
        return TenantContextHolder.getTenantContext()
                .map(ctx -> new TenantHealthResponse(
                        ctx.tenantId().value(),
                        ctx.tenantName(),
                        ctx.tenantStatus().name()
                ));
    }
}
