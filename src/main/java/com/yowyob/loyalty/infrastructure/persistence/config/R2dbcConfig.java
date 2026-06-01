package com.yowyob.loyalty.infrastructure.persistence.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories(basePackages = "com.yowyob.loyalty.infrastructure.persistence")
public class R2dbcConfig {

    @Bean
    @Primary
    public ConnectionFactory tenantAwareConnectionFactory(@Qualifier("connectionFactory") ConnectionFactory delegate) {
        return new TenantAwareConnectionFactory(delegate);
    }
}
