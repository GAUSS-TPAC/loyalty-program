package com.yowyob.loyalty.infrastructure.persistence.config;

import com.yowyob.loyalty.domain.tenant.port.out.TenantRepository;
import io.r2dbc.spi.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class DatabaseInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final ConnectionFactory connectionFactory;
    private final TenantRepository tenantRepository;

    public DatabaseInitializer(ConnectionFactory connectionFactory, TenantRepository tenantRepository) {
        this.connectionFactory = connectionFactory;
        this.tenantRepository = tenantRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Checking database and initializing tenant schemas...");
        
        Mono.from(connectionFactory.create())
            .flatMap(connection -> Mono.from(connection.createStatement(
                    "SELECT 1 FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'tenants'"
                ).execute())
                .flatMap(result -> Mono.from(result.map((row, rowMetadata) -> row.get(0, Integer.class))))
                .hasElement()
                .flatMap(tenantsTableExists -> {
                    if (!tenantsTableExists) {
                        log.warn("Public 'tenants' table not found — skipping tenant schema initialization.");
                        return Mono.empty();
                    }
                    log.info("Public 'tenants' table verified.");
                    return Mono.from(connection.createStatement("SELECT id FROM public.tenants WHERE status = 'ACTIVE'").execute())
                            .flatMapMany(result -> result.map((row, rowMetadata) -> row.get("id", java.util.UUID.class)))
                            .flatMap(tenantId -> {
                                String schemaName = "tenant_" + tenantId.toString().substring(0, 8);
                                return Mono.from(connection.createStatement("CREATE SCHEMA IF NOT EXISTS " + schemaName).execute())
                                        .doOnSuccess(v -> log.info("Schema verified/created: {}", schemaName));
                            })
                            .then();
                })
                .doFinally(signalType -> Mono.from(connection.close()).subscribe())
            )
            .subscribe(
                null,
                error -> log.error("Failed to initialize database schemas", error),
                () -> log.info("Database initialization check complete.")
            );
    }
}
