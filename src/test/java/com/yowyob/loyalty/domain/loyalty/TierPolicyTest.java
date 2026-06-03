package com.yowyob.loyalty.domain.loyalty;

import com.yowyob.loyalty.domain.loyalty.model.tier.TierLevel;
import com.yowyob.loyalty.domain.loyalty.model.tier.TierPolicy;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TierPolicyTest {

    @Test
    void calculateTier_returnsCorrectTierBasedOnThresholds() {
        TierPolicy policy = TierPolicy.defaults(new TenantId(java.util.UUID.randomUUID())); // 0->BRONZE, 1000->SILVER, 5000->GOLD, 20000->PLATINUM

        assertEquals(TierLevel.BRONZE, policy.calculateTier(0));
        assertEquals(TierLevel.BRONZE, policy.calculateTier(999));
        
        assertEquals(TierLevel.SILVER, policy.calculateTier(1000));
        assertEquals(TierLevel.SILVER, policy.calculateTier(4999));
        
        assertEquals(TierLevel.GOLD, policy.calculateTier(5000));
        assertEquals(TierLevel.GOLD, policy.calculateTier(19999));
        
        assertEquals(TierLevel.PLATINUM, policy.calculateTier(20000));
        assertEquals(TierLevel.PLATINUM, policy.calculateTier(50000));
    }
}
