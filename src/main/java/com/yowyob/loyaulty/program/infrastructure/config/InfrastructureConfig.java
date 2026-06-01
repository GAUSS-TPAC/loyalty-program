package com.yowyob.loyaulty.program.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuration des beans Spring pour l'infrastructure.
 *
 * <p>Regroupe :</p>
 * <ul>
 *   <li>Redis : {@link ReactiveStringRedisTemplate}</li>
 * </ul>
 *
 * <p>Les WebClients pour MTN, Orange et Stripe ont été supprimés.
 * Les paiements sont désormais délégués au Kernel Core Payment API
 * via {@code KernelCorePaymentAdapter}.</p>
 */
@Configuration
public class InfrastructureConfig {

    // ── Redis ─────────────────────────────────────────────────────────────────

    /**
     * Template Redis réactif avec sérialisation String/String.
     * Utilisé par {@code IdempotencyAdapter} et {@code WalletCacheAdapter}.
     */
    @Bean
    public ReactiveStringRedisTemplate reactiveStringRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        RedisSerializationContext<String, String> serializationContext =
                RedisSerializationContext.<String, String>newSerializationContext(new StringRedisSerializer())
                        .value(new StringRedisSerializer())
                        .build();
        return new ReactiveStringRedisTemplate(connectionFactory, serializationContext);
    }
}
