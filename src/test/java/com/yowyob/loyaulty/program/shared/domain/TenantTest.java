package com.yowyob.loyaulty.program.shared.domain;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.tenant.model.Tenant;
import com.yowyob.loyaulty.program.domain.tenant.model.enums.TenantStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TenantTest {

    @Test
    void newTenant_shouldBeInPendingSetupStatus() {
        Tenant tenant = Tenant.create(TenantId.of(UUID.randomUUID()), "RidnGo", "ridngo", "system");
        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.PENDING_SETUP);
        assertThat(tenant.isActive()).isFalse();
    }

    @Test
    void activate_shouldMakeTenantActive() {
        Tenant tenant = Tenant.create(TenantId.of(UUID.randomUUID()), "RidnGo", "ridngo", "system");
        tenant.activate();
        assertThat(tenant.isActive()).isTrue();
        assertThat(tenant.isSuspended()).isFalse();
    }

    @Test
    void suspend_shouldMakeTenantSuspended() {
        Tenant tenant = Tenant.create(TenantId.of(UUID.randomUUID()), "RidnGo", "ridngo", "system");
        tenant.activate();
        tenant.suspend();
        assertThat(tenant.isSuspended()).isTrue();
        assertThat(tenant.isActive()).isFalse();
    }

    @Test
    void tenantId_invalidUuid_shouldThrow() {
        assertThatThrownBy(() -> TenantId.of("not-a-uuid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid TenantId format");
    }

    @Test
    void tenantId_equality_shouldWorkByValue() {
        UUID uuid = UUID.randomUUID();
        TenantId id1 = TenantId.of(uuid);
        TenantId id2 = TenantId.of(uuid.toString());
        assertThat(id1).isEqualTo(id2);
        assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
    }
}
