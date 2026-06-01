package com.yowyob.loyalty.domain.tenant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.yowyob.loyalty.domain.shared.exception.DomainValidationException;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.model.Tenant;
import com.yowyob.loyalty.domain.tenant.model.TenantConfig;
import com.yowyob.loyalty.domain.tenant.model.enums.TenantPlan;
import com.yowyob.loyalty.domain.tenant.model.enums.TenantStatus;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class TenantTest {

    @Test
    public void testCreateTenantWithEmptyNameThrowsException() {
        TenantId id = TenantId.of(UUID.randomUUID());
        TenantConfig config = TenantConfig.defaults();

        assertThrows(DomainValidationException.class, () -> {
            Tenant.create(id, "", "slug-test", TenantPlan.FREE, config, "admin");
        });
        
        assertThrows(DomainValidationException.class, () -> {
            Tenant.create(id, "Valid Name", "", TenantPlan.FREE, config, "admin");
        });
    }

    @Test
    public void testTenantActivate() {
        TenantId id = TenantId.of(UUID.randomUUID());
        TenantConfig config = TenantConfig.defaults();
        Tenant tenant = Tenant.create(id, "Test Tenant", "test-tenant", TenantPlan.PRO, config, "admin");

        assertEquals(TenantStatus.PENDING_SETUP, tenant.getStatus());
        assertFalse(tenant.isActive());

        Tenant activatedTenant = tenant.activate();

        assertEquals(TenantStatus.ACTIVE, activatedTenant.getStatus());
        assertTrue(activatedTenant.isActive());
        
        Tenant suspendedTenant = activatedTenant.suspend();
        assertEquals(TenantStatus.SUSPENDED, suspendedTenant.getStatus());
        assertTrue(suspendedTenant.isSuspended());
    }

    @Test
    public void testTenantPlanLimits() {
        TenantId id = TenantId.of(UUID.randomUUID());
        TenantConfig config = TenantConfig.defaults();
        
        Tenant freeTenant = Tenant.create(id, "Free", "free", TenantPlan.FREE, config, "admin");
        assertTrue(freeTenant.canCreateMoreRules(4));
        assertFalse(freeTenant.canCreateMoreRules(5)); // FREE maxRules = 5
        
        Tenant proTenant = Tenant.create(id, "Pro", "pro", TenantPlan.PRO, config, "admin");
        assertTrue(proTenant.canCreateMoreRules(49));
        assertFalse(proTenant.canCreateMoreRules(50)); // PRO maxRules = 50
    }
}
