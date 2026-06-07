package com.yowyob.loyalty.infrastructure.persistence.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories(basePackages = "com.yowyob.loyalty.infrastructure.persistence")
public class R2dbcConfig {

    @Bean
    public ConnectionFactory rawConnectionFactory(R2dbcProperties properties) {
        return ConnectionFactoryBuilder.withUrl(properties.getUrl())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .build();
    }

    @Bean
    @Primary
    @Profile("!test")
    public ConnectionFactory connectionFactory(ConnectionFactory rawConnectionFactory) {
        return new TenantAwareConnectionFactory(rawConnectionFactory);
    }

    @Bean
    @Primary
    @Profile("test")
    public ConnectionFactory testConnectionFactory(ConnectionFactory rawConnectionFactory) {
        return rawConnectionFactory;
    }
}
