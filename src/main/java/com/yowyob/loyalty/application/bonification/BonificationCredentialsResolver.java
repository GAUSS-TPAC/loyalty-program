package com.yowyob.loyalty.application.bonification;

import com.yowyob.loyalty.domain.bonification.model.BonificationCredentials;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.model.Tenant;
import com.yowyob.loyalty.domain.tenant.model.TenantConfig;
import com.yowyob.loyalty.domain.tenant.port.out.TenantRepository;
import com.yowyob.loyalty.infrastructure.bonification.config.BonificationProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class BonificationCredentialsResolver {

    private final TenantRepository tenantRepository;
    private final BonificationProperties properties;

    public BonificationCredentialsResolver(TenantRepository tenantRepository, BonificationProperties properties) {
        this.tenantRepository = tenantRepository;
        this.properties = properties;
    }

    public Mono<BonificationCredentials> resolve(TenantId tenantId) {
        return tenantRepository.findById(tenantId)
                .map(Tenant::getConfig)
                .defaultIfEmpty(TenantConfig.defaults())
                .map(config -> merge(config, properties));
    }

    private static BonificationCredentials merge(TenantConfig config, BonificationProperties properties) {
        String login = firstNonBlank(config.bonificationApiUsername(), properties.getDefaultLogin());
        String password = firstNonBlank(config.bonificationApiPassword(), properties.getDefaultPassword());
        return new BonificationCredentials(login, password);
    }

    private static String firstNonBlank(String primary, String fallback) {
        if (primary != null && !primary.isBlank()) {
            return primary;
        }
        return fallback != null ? fallback : "";
    }
}
