package com.yowyob.loyaulty.program;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.shared.model.Money;
import com.yowyob.loyaulty.program.domain.tenant.model.Tenant;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke test — vérifie que le domaine fonctionne sans démarrer Spring.
 * Les tests d'intégration complets (avec Testcontainers) sont dans integration/.
 */
class LoyaultyProgramApplicationTests {

    @Test
    void domainModels_areInstantiable() {
        TenantId id = TenantId.of(UUID.randomUUID());
        assertThat(id).isNotNull();

        Money money = Money.of(BigDecimal.valueOf(5000), "XAF");
        assertThat(money.amount()).isEqualByComparingTo("5000.00");
        assertThat(money.currency()).isEqualTo("XAF");

        Tenant tenant = Tenant.create(id, "Test Corp", "test-corp", "system");
        assertThat(tenant.isActive()).isFalse();
        tenant.activate();
        assertThat(tenant.isActive()).isTrue();
    }
}
