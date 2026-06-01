package com.yowyob.loyaulty.program.infrastructure.stub;

import com.yowyob.loyaulty.program.domain.shared.port.out.TenantQueryPort;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Stub de développement pour {@link TenantQueryPort}.
 *
 * <p>Actif uniquement avec le profil Spring {@code stub}.
 * Retourne des données factices sans appel réseau.
 * À remplacer par {@code KernelCoreTenantAdapter} quand le Kernel Core sera disponible.</p>
 */
@Slf4j
@Component
@Profile("stub")
public class TenantQueryStub implements TenantQueryPort {

    @PostConstruct
    public void logWarning() {
        log.warn("[STUB] TenantQueryStub actif — données factices utilisées. " +
                "Remplacer par KernelCoreTenantAdapter en production.");
    }

    @Override
    public Mono<Boolean> tenantExists(UUID tenantId) {
        return Mono.just(true);
    }

    @Override
    public Mono<String> getTenantCurrencyCode(UUID tenantId) {
        return Mono.just("XAF");
    }

    @Override
    public Mono<Integer> getTenantMaxRules(UUID tenantId) {
        return Mono.just(50);
    }
}
