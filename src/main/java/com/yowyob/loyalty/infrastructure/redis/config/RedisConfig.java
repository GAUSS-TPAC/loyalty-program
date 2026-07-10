package com.yowyob.loyalty.infrastructure.redis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.yowyob.loyalty.domain.tenant.model.Tenant;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        return new ReactiveRedisTemplate<>(factory, RedisSerializationContext.string());
    }

    /**
     * Un {@code Jackson2JsonRedisSerializer<Object>} construit avec la classe cible {@code Object}
     * sérialise correctement mais désérialise en {@code LinkedHashMap} (Jackson n'a aucune info de
     * type polymorphe embarquée dans le JSON) — {@code .cast(Tenant.class)} côté TenantCacheAdapter
     * échouait donc systématiquement avec un ClassCastException sur tout cache hit réel (masqué en
     * dev par DevTenantResolutionFilter, qui court-circuite ce cache). D'où un template dédié et
     * correctement typé plutôt qu'un template générique partagé.
     */
    @Bean(name = "tenantRedisTemplate")
    public ReactiveRedisTemplate<String, Tenant> tenantRedisTemplate(ReactiveRedisConnectionFactory factory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonRedisSerializer<Tenant> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, Tenant.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, Tenant> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, Tenant> context = builder.value(serializer).build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}
