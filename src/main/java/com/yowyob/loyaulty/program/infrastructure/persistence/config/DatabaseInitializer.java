package com.yowyob.loyaulty.program.infrastructure.persistence.config;

import com.yowyob.loyaulty.program.infrastructure.persistence.tenant.adapter.TenantRepositoryAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final JdbcTemplate jdbcTemplate;
    private final TenantRepositoryAdapter tenantRepository;

    public DatabaseInitializer(JdbcTemplate jdbcTemplate,
                                TenantRepositoryAdapter tenantRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.tenantRepository = tenantRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Initializing tenant schemas...");
        tenantRepository.findAllActive()
                .doOnNext(tenant -> {
                    String schema = "tenant_" + tenant.getId().value().toString().replace("-", "_");
                    createSchemaIfNotExists(schema);
                })
                .doOnComplete(() -> log.info("Tenant schema initialization complete"))
                .doOnError(e -> log.error("Error during tenant schema initialization", e))
                .subscribe();
    }

    private void createSchemaIfNotExists(String schema) {
        try {
            jdbcTemplate.execute("CREATE SCHEMA IF NOT EXISTS " + schema);
            log.debug("Schema ensured: {}", schema);
        } catch (Exception e) {
            log.warn("Could not create schema {}: {}", schema, e.getMessage());
        }
    }
}
