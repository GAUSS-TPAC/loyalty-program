package com.yowyob.loyalty.infrastructure.security;

import com.yowyob.loyalty.infrastructure.security.config.JwtProperties;
import com.yowyob.loyalty.shared.security.JwtTokenValidator;
import com.yowyob.loyalty.shared.security.JwtValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

/**
 * Le claim "permissions" de KernelCore auth-core porte des codes de permission scopés
 * (ex: "tenant:admin#TENANT", "ROLE_OWNER"), pas des rôles Keycloak-style — voir
 * SecurityContextRepository.extractAuthorities. Ces tests figent le comportement de mapping
 * vers les autorités Spring ROLE_* réellement consommées par @PreAuthorize("hasRole(...)").
 */
public class SecurityContextRepositoryTest {

    private JwtTokenValidator jwtTokenValidator;
    private JwtProperties jwtProperties;
    private SecurityContextRepository repository;

    @BeforeEach
    void setup() {
        jwtTokenValidator = Mockito.mock(JwtTokenValidator.class);
        jwtProperties = new JwtProperties();
        jwtProperties.setRolesClaim("permissions");
        repository = new SecurityContextRepository(jwtTokenValidator, jwtProperties);
    }

    private MockServerWebExchange exchangeWithBearer(String token) {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/admin/api-keys")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        return MockServerWebExchange.from(request);
    }

    private Jwt jwtWithPermissions(List<String> permissions) {
        return Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("sub", "actor-1")
                .claim("permissions", permissions)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    @Test
    void grantsTenantAdminForRealKernelCoreAdminPermissionCode() {
        // Real permission list observed from a live KernelCore login (see AuthService).
        Jwt jwt = jwtWithPermissions(List.of("tenant:admin#TENANT", "tenant:admin", "ROLE_OWNER#TENANT", "settings:read"));
        when(jwtTokenValidator.validateToken("t")).thenReturn(Mono.just(JwtValidationResult.valid(jwt)));

        var context = repository.load(exchangeWithBearer("t")).block();
        Set<String> names = context.getAuthentication().getAuthorities().stream()
                .map(Object::toString).collect(Collectors.toSet());

        assertEquals(true, names.contains("ROLE_TENANT_ADMIN"));
    }

    @Test
    void grantsTenantAdminForRoleOwnerAlone() {
        Jwt jwt = jwtWithPermissions(List.of("ROLE_OWNER"));
        when(jwtTokenValidator.validateToken("t")).thenReturn(Mono.just(JwtValidationResult.valid(jwt)));

        var context = repository.load(exchangeWithBearer("t")).block();
        Set<String> names = context.getAuthentication().getAuthorities().stream()
                .map(Object::toString).collect(Collectors.toSet());

        assertEquals(true, names.contains("ROLE_TENANT_ADMIN"));
    }

    @Test
    void doesNotGrantTenantAdminForUnrelatedPermissions() {
        Jwt jwt = jwtWithPermissions(List.of("settings:write#TENANT", "products:write"));
        when(jwtTokenValidator.validateToken("t")).thenReturn(Mono.just(JwtValidationResult.valid(jwt)));

        var context = repository.load(exchangeWithBearer("t")).block();
        Set<String> names = context.getAuthentication().getAuthorities().stream()
                .map(Object::toString).collect(Collectors.toSet());

        assertFalse(names.contains("ROLE_TENANT_ADMIN"));
        assertEquals(true, names.contains("ROLE_SETTINGS_WRITE"));
    }

    @Test
    void invalidTokenYieldsEmptySecurityContext() {
        when(jwtTokenValidator.validateToken("bad")).thenReturn(Mono.just(JwtValidationResult.invalid("expired")));

        StepVerifier.create(repository.load(exchangeWithBearer("bad")))
                .verifyComplete();
    }
}
