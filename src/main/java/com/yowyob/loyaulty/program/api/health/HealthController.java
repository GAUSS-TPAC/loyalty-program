package com.yowyob.loyaulty.program.api.health;

import com.yowyob.loyaulty.program.api.health.dto.TenantHealthResponse;
import com.yowyob.loyaulty.program.api.shared.dto.ProblemDetails;
import com.yowyob.loyaulty.program.shared.multitenancy.TenantContextHolder;
import com.yowyob.loyaulty.program.shared.security.JwtClaimsExtractor;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
public class HealthController {

    private final JwtClaimsExtractor claimsExtractor;

    public HealthController(JwtClaimsExtractor claimsExtractor) {
        this.claimsExtractor = claimsExtractor;
    }

    @GetMapping
    public Mono<Map<String, Object>> health() {
        return Mono.just(Map.of(
                "status", "UP",
                "version", "1.0.0",
                "timestamp", Instant.now().toString()
        ));
    }

    @GetMapping("/tenant")
    public Mono<TenantHealthResponse> tenantHealth() {
        return Mono.zip(
                TenantContextHolder.getTenantContext(),
                ReactiveSecurityContextHolder.getContext()
        ).map(tuple -> {
            var tenantCtx = tuple.getT1();
            var secCtx = tuple.getT2();

            String userId = "unknown";
            var roles = java.util.Set.<String>of();

            if (secCtx.getAuthentication() instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                userId = claimsExtractor.extractUserId(jwt.getClaims()).orElse("unknown");
                roles = claimsExtractor.extractRoles(jwt.getClaims());
            }

            return TenantHealthResponse.up(
                    tenantCtx.tenantId().toString(),
                    tenantCtx.tenantName(),
                    tenantCtx.tenantStatus().name(),
                    tenantCtx.tenantPlan().name(),
                    userId,
                    roles
            );
        });
    }
}
