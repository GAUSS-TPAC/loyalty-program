package com.yowyob.loyalty.infrastructure.security;

import com.yowyob.loyalty.infrastructure.security.config.JwtProperties;
import com.yowyob.loyalty.shared.security.JwtTokenValidator;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Résout le SecurityContext à partir du Bearer JWT.
 * Convertit le claim de rôles/permissions (configurable via app.security.jwt.roles-claim) en
 * autorités Spring ("ROLE_xxx") afin que les vérifications @PreAuthorize("hasRole(...)")
 * fonctionnent réellement. Sans cette conversion, JwtAuthenticationToken n'accorde par défaut
 * que des autorités SCOPE_* dérivées du claim "scope", ce qui fait échouer silencieusement tout
 * hasRole(...).
 *
 * KernelCore auth-core n'émet pas de rôles Keycloak-style : le claim "permissions" porte une
 * liste de codes scopés (ex: "tenant:admin", "tenant:admin#TENANT", "ROLE_OWNER#TENANT",
 * "settings:write"). On normalise chaque code (suffixe #SCOPE retiré, non-alphanumériques ->
 * "_", majuscules) en une autorité ROLE_<CODE>, et on reconnaît en plus les codes équivalents à
 * un accès admin de tenant pour accorder ROLE_TENANT_ADMIN (requis par ApiKeyController,
 * AdminLogsController, etc.).
 */
@Component
public class SecurityContextRepository implements ServerSecurityContextRepository {

    private final JwtTokenValidator jwtTokenValidator;
    private final JwtProperties jwtProperties;

    public SecurityContextRepository(JwtTokenValidator jwtTokenValidator, JwtProperties jwtProperties) {
        this.jwtTokenValidator = jwtTokenValidator;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.error(new UnsupportedOperationException("Stateless API does not support saving context."));
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtTokenValidator.validateToken(token)
                    .filter(result -> result.isValid())
                    .map(result -> new JwtAuthenticationToken(result.jwt(), extractAuthorities(result.jwt())))
                    .map(SecurityContextImpl::new);
        } else {
            return Mono.empty();
        }
    }

    /** Codes de permission KernelCore (scope #TENANT/#ORGANIZATION retiré) équivalents à un accès admin de tenant. */
    private static final Set<String> TENANT_ADMIN_PERMISSION_CODES = Set.of(
            "tenant:admin", "role_owner", "owner", "tenant_admin"
    );

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Object claim = jwt.getClaim(jwtProperties.getRolesClaim());
        List<String> roles;
        if (claim instanceof Collection<?>) {
            roles = ((Collection<Object>) claim).stream().map(String::valueOf).collect(Collectors.toList());
        } else if (claim instanceof String str && !str.isBlank()) {
            roles = List.of(str.split("[,\\s]+"));
        } else {
            roles = List.of();
        }

        boolean isTenantAdmin = false;
        Set<GrantedAuthority> authorities = new java.util.LinkedHashSet<>();
        for (String role : roles) {
            String withoutScope = role.contains("#") ? role.substring(0, role.indexOf('#')) : role;
            String normalizedKey = withoutScope.toLowerCase(Locale.ROOT);
            if (TENANT_ADMIN_PERMISSION_CODES.contains(normalizedKey)) {
                isTenantAdmin = true;
            }
            String authorityName = withoutScope.replaceAll("[^A-Za-z0-9]+", "_").toUpperCase(Locale.ROOT);
            if (!authorityName.isBlank()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + authorityName));
            }
        }
        if (isTenantAdmin) {
            authorities.add(new SimpleGrantedAuthority("ROLE_TENANT_ADMIN"));
        }
        return authorities;
    }
}
