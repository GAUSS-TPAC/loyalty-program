package com.yowyob.loyaulty.program.shared.security;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JwtClaimsExtractor {

    private final JwtProperties properties;

    public JwtClaimsExtractor(JwtProperties properties) {
        this.properties = properties;
    }

    public Mono<String> extractTenantId(Map<String, Object> claims) {
        Object value = claims.get(properties.tenantIdClaim());
        if (value == null) return Mono.empty();
        return Mono.just(value.toString());
    }

    public Optional<String> extractUserId(Map<String, Object> claims) {
        Object value = claims.get(properties.userIdClaim());
        return Optional.ofNullable(value).map(Object::toString);
    }

    public Set<String> extractRoles(Map<String, Object> claims) {
        Object value = claims.get(properties.rolesClaim());
        if (value instanceof List<?> list) {
            return list.stream().map(Object::toString).collect(Collectors.toSet());
        }
        return Set.of();
    }
}
