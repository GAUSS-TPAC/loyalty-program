package com.yowyob.loyaulty.program.infrastructure.persistence.config;

import com.yowyob.loyaulty.program.shared.multitenancy.TenantContext;
import com.yowyob.loyaulty.program.shared.multitenancy.TenantContextHolder;
import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryMetadata;
import org.reactivestreams.Publisher;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;

public class TenantAwareConnectionFactory implements ConnectionFactory {

    private final ConnectionFactory delegate;

    public TenantAwareConnectionFactory(ConnectionFactory delegate) {
        this.delegate = delegate;
    }

    @Override
    public Publisher<? extends Connection> create() {
        return Mono.deferContextual(ctx -> {
            Mono<? extends Connection> connectionMono = Mono.from(delegate.create());

            if (!ctx.hasKey(TenantContextHolder.CONTEXT_KEY)) {
                return connectionMono;
            }

            TenantContext tenantContext = ctx.get(TenantContextHolder.CONTEXT_KEY);
            String schema = "tenant_" + tenantContext.tenantId().value().toString().replace("-", "_");

            return connectionMono.flatMap(connection ->
                    Mono.from(
                            connection.createStatement(
                                    "SET search_path TO " + schema + ", public"
                            ).execute()
                    )
                    .then(Mono.just(connection))
            );
        });
    }

    @Override
    public ConnectionFactoryMetadata getMetadata() {
        return delegate.getMetadata();
    }
}
