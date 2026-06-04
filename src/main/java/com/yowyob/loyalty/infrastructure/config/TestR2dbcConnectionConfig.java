package com.yowyob.loyalty.infrastructure.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcProperties;
import org.springframework.boot.r2dbc.ConnectionFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestR2dbcConnectionConfig {

    @Bean(name = "connectionFactory")
    @ConditionalOnMissingBean(name = "connectionFactory")
    public ConnectionFactory connectionFactory(R2dbcProperties properties) {
        return ConnectionFactoryBuilder.withUrl(properties.getUrl())
                .username(properties.getUsername())
                .password(properties.getPassword())
                .build();
    }
}
