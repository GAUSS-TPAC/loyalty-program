package com.yowyob.loyalty.shared.security;

import com.nimbusds.jwt.JWTParser;
import com.yowyob.loyalty.infrastructure.security.config.JwtProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

/**
 * Dev-only JWT config — accepts unsigned tokens (alg:none).
 * Separated from TestJwtSecurityConfig to make intent explicit.
 * MUST NOT be active in any shared or production environment.
 */
@Configuration
@Profile("dev")
public class DevJwtSecurityConfig {

    @Bean
    @Primary
    ReactiveJwtDecoder devJwtDecoder() {
        return token -> {
            try {
                var claims = JWTParser.parse(token).getJWTClaimsSet();
                Jwt.Builder builder = Jwt.withTokenValue(token)
                        .header("alg", "none")
                        .issuedAt(claims.getIssueTime() != null ? claims.getIssueTime().toInstant() : Instant.now())
                        .expiresAt(claims.getExpirationTime() != null
                                ? claims.getExpirationTime().toInstant()
                                : Instant.now().plusSeconds(3600));
                for (Map.Entry<String, Object> entry : claims.getClaims().entrySet()) {
                    builder.claim(entry.getKey(), entry.getValue());
                }
                return Mono.just(builder.build());
            } catch (Exception e) {
                return Mono.error(e);
            }
        };
    }

    @Bean
    @Primary
    JwtTokenValidator devJwtTokenValidator(JwtProperties jwtProperties, ReactiveJwtDecoder devJwtDecoder) {
        return new JwtTokenValidator(jwtProperties, devJwtDecoder);
    }
}
