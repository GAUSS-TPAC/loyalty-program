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
 * Réservé au profil "test" (JUnit) uniquement : {@code stub} a été retiré de ce @Profile.
 * "stub" active des adaptateurs de persistance de repli (PaymentRequestRepositoryStub,
 * WalletAuditLogRepositoryStub) pour des ports non encore implémentés — cela ne doit pas
 * impliquer, comme c'était le cas avant, de désactiver aussi la vérification de signature JWT.
 * Un déploiement réel (ex. Render, profil "no-kafka,stub") doit continuer à valider les JWT
 * KernelCore via JwtProductionSecurityConfig — vérifié en conditions réelles : avec "stub" dans
 * ce @Profile, un JWT KernelCore réel et valide était rejeté (voir bug ci-dessous) et, plus grave,
 * un JWT non signé aurait été accepté silencieusement dans un déploiement quasi-production.
 */
@Configuration
@Profile({"test"})
public class TestJwtSecurityConfig {

    @Bean
    @Primary
    ReactiveJwtDecoder testJwtDecoder() {
        return token -> {
            try {
                var claims = JWTParser.parse(token).getJWTClaimsSet();
                Jwt.Builder builder = Jwt.withTokenValue(token)
                        .header("alg", "none")
                        .issuedAt(claims.getIssueTime() != null ? claims.getIssueTime().toInstant() : Instant.now())
                        .expiresAt(claims.getExpirationTime() != null ? claims.getExpirationTime().toInstant() : Instant.now().plusSeconds(3600));
                for (Map.Entry<String, Object> entry : claims.getClaims().entrySet()) {
                    // "iat"/"exp"/"nbf" sont déjà portés en Instant via .issuedAt()/.expiresAt() ci-dessus ;
                    // claims.getClaims() les renvoie en java.util.Date (API Nimbus), et Jwt.Builder exige
                    // strictement Instant pour ces trois clés -- les recopier ici écrase la valeur Instant
                    // par un Date et fait échouer .build() avec IllegalArgumentException (vérifié en
                    // conditions réelles : provoquait un rejet 401 systématique de tout JWT KernelCore réel).
                    if (entry.getKey().equals("iat") || entry.getKey().equals("exp") || entry.getKey().equals("nbf")) {
                        continue;
                    }
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
    JwtTokenValidator testJwtTokenValidator(JwtProperties jwtProperties, ReactiveJwtDecoder testJwtDecoder) {
        return new JwtTokenValidator(jwtProperties, testJwtDecoder);
    }
}
