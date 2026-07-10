package com.yowyob.loyalty.shared.security;

import com.yowyob.loyalty.infrastructure.security.config.JwtProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * "stub" retiré des exclusions : ce profil active des adaptateurs de persistance de repli
 * (PaymentRequestRepositoryStub, WalletAuditLogRepositoryStub) pour des ports non encore
 * implémentés, indépendant de la validation JWT — voir TestJwtSecurityConfig. Un déploiement
 * "stub" (ex. Render, no-kafka+stub) doit continuer à valider les JWT KernelCore réels via
 * RS256/JWKS, pas via le décodeur de test qui ne vérifie aucune signature.
 */
@Configuration
@Profile("!test & !dev")
public class JwtProductionSecurityConfig {

    @Bean
    JwtTokenValidator jwtTokenValidator(JwtProperties jwtProperties) {
        return new JwtTokenValidator(jwtProperties);
    }
}
