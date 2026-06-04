package com.yowyob.loyalty.domain.loyalty;

import com.yowyob.loyalty.domain.loyalty.exception.LoyaltyDomainException;
import com.yowyob.loyalty.domain.loyalty.model.points.PointsAccount;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PointsAccountTest {

    @Test
    void earnPoints_increasesAvailableAndLifetime() {
        PointsAccount account = PointsAccount.create(UUID.randomUUID(), new TenantId(java.util.UUID.randomUUID()), new UserId(java.util.UUID.randomUUID()));
        
        PointsAccount updated = account.earn(500);
        
        assertEquals(500, updated.getAvailablePoints());
        assertEquals(500, updated.getLifetimeEarned());
        assertEquals(0, updated.getLifetimeSpent());
        assertEquals(1, updated.getVersion());
    }

    @Test
    void spendPoints_decreasesAvailableAndIncreasesLifetimeSpent() {
        PointsAccount account = PointsAccount.create(UUID.randomUUID(), new TenantId(java.util.UUID.randomUUID()), new UserId(java.util.UUID.randomUUID()));
        account = account.earn(1000);
        
        PointsAccount updated = account.spend(200);
        
        assertEquals(800, updated.getAvailablePoints());
        assertEquals(1000, updated.getLifetimeEarned());
        assertEquals(200, updated.getLifetimeSpent());
    }

    @Test
    void spendPoints_insufficientBalance_throwsException() {
        PointsAccount account = PointsAccount.create(UUID.randomUUID(), new TenantId(java.util.UUID.randomUUID()), new UserId(java.util.UUID.randomUUID()));
        account = account.earn(200);
        
        PointsAccount finalAccount = account;
        assertThrows(LoyaltyDomainException.class, () -> finalAccount.spend(1000));
    }

    @Test
    void expirePoints_decreasesAvailableWithoutAffectingLifetimeSpent() {
        PointsAccount account = PointsAccount.create(UUID.randomUUID(), new TenantId(java.util.UUID.randomUUID()), new UserId(java.util.UUID.randomUUID()));
        account = account.earn(1000);
        
        PointsAccount updated = account.expire(100);
        
        assertEquals(900, updated.getAvailablePoints());
        assertEquals(1000, updated.getLifetimeEarned());
        assertEquals(0, updated.getLifetimeSpent());
    }
}
