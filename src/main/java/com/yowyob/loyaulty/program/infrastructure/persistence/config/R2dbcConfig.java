package com.yowyob.loyaulty.program.infrastructure.persistence.config;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import java.time.Duration;

@Configuration
@EnableR2dbcRepositories(basePackages = "com.yowyob.loyaulty.program.infrastructure.persistence")
public class R2dbcConfig extends AbstractR2dbcConfiguration {

    @Value("${spring.r2dbc.url}")
    private String r2dbcUrl;

    @Value("${spring.r2dbc.username}")
    private String username;

    @Value("${spring.r2dbc.password}")
    private String password;

    @Value("${spring.r2dbc.pool.initial-size:5}")
    private int initialSize;

    @Value("${spring.r2dbc.pool.max-size:20}")
    private int maxSize;

    @Override
    @Bean
    @Primary
    public ConnectionFactory connectionFactory() {
        ConnectionFactory base = ConnectionFactories.get(
                ConnectionFactoryOptions.parse(r2dbcUrl)
                        .mutate()
                        .option(ConnectionFactoryOptions.USER, username)
                        .option(ConnectionFactoryOptions.PASSWORD, password)
                        .build()
        );

        ConnectionPool pool = new ConnectionPool(
                ConnectionPoolConfiguration.builder(base)
                        .initialSize(initialSize)
                        .maxSize(maxSize)
                        .maxIdleTime(Duration.ofMinutes(30))
                        .build()
        );

        return new TenantAwareConnectionFactory(pool);
    }
}
