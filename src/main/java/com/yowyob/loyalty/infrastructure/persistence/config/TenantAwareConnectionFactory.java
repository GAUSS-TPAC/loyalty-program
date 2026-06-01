package com.yowyob.loyalty.infrastructure.persistence.config;

import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryMetadata;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public class TenantAwareConnectionFactory implements ConnectionFactory {

    private final ConnectionFactory delegate;

    public TenantAwareConnectionFactory(ConnectionFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public Publisher<? extends Connection> create() {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> {
                    String schemaName = "tenant_" + tenantId.value().toString().substring(0, 8);
                    return Mono.from(delegate.create())
                            .flatMap(connection -> Mono.from(connection.createStatement("SET search_path TO " + schemaName + ", public").execute())
                                    .then(Mono.just(connection)));
                })
                .onErrorResume(e -> Mono.from(delegate.create()));
    }

    @Override
    public ConnectionFactoryMetadata getMetadata() {
        return delegate.getMetadata();
    }
}
