package com.yowyob.loyalty.domain.reward;

import com.yowyob.loyalty.domain.reward.exception.RewardDomainException;
import com.yowyob.loyalty.domain.reward.model.Reward;
import com.yowyob.loyalty.domain.reward.model.RewardStatus;
import com.yowyob.loyalty.domain.reward.model.RewardType;
import com.yowyob.loyalty.domain.reward.model.RewardValue;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RewardModelTest {

    private static TenantId tenantId() { return new TenantId(UUID.randomUUID()); }

    private static Reward unlimitedReward() {
        return Reward.create(UUID.randomUUID(), tenantId(), "Free coffee", "desc",
                RewardType.FREE_PRODUCT, RewardValue.product("SKU-1"), 500, null, null, null, 0, null, null);
    }

    private static Reward limitedStockReward(int stock) {
        return Reward.create(UUID.randomUUID(), tenantId(), "Limited item", "desc",
                RewardType.FREE_PRODUCT, RewardValue.product("SKU-2"), 500, stock, null, null, 0, null, null);
    }

    // ── RewardValue invariants ──

    @Test
    void rewardValue_nonPositiveNumericValue_throws() {
        assertThrows(IllegalArgumentException.class, () -> new RewardValue(BigDecimal.ZERO, "PERCENT", 1));
        assertThrows(IllegalArgumentException.class, () -> new RewardValue(null, "PERCENT", 1));
    }

    @Test
    void rewardValue_blankUnit_throws() {
        assertThrows(IllegalArgumentException.class, () -> new RewardValue(BigDecimal.TEN, " ", 1));
    }

    @Test
    void rewardValue_maxApplicationCountBelowOne_throws() {
        assertThrows(IllegalArgumentException.class, () -> new RewardValue(BigDecimal.TEN, "PERCENT", 0));
    }

    @Test
    void rewardValue_calculateDiscount_percentAppliesToOrderAmount() {
        RewardValue value = RewardValue.percent(new BigDecimal("20"), 1);
        assertEquals(0, new BigDecimal("20.00").compareTo(value.calculateDiscount(new BigDecimal("100"))));
    }

    @Test
    void rewardValue_calculateDiscount_fixedIgnoresOrderAmountAndDoesNotCap() {
        // Documented quirk: capping to the order amount happens in ConsumeGrantService, not here.
        RewardValue value = RewardValue.fixed(new BigDecimal("500"), "XAF");
        assertEquals(0, new BigDecimal("500").compareTo(value.calculateDiscount(new BigDecimal("10"))));
    }

    // ── Reward.create invariants ──

    @Test
    void create_blankName_throws() {
        assertThrows(RewardDomainException.class, () -> Reward.create(UUID.randomUUID(), tenantId(), "  ", "d",
                RewardType.FREE_PRODUCT, RewardValue.product("X"), 100, null, null, null, 0, null, null));
    }

    @Test
    void create_negativeCostInPoints_throws() {
        assertThrows(RewardDomainException.class, () -> Reward.create(UUID.randomUUID(), tenantId(), "N", "d",
                RewardType.FREE_PRODUCT, RewardValue.product("X"), -1, null, null, null, 0, null, null));
    }

    @Test
    void create_zeroCostInPoints_allowed() {
        Reward reward = Reward.create(UUID.randomUUID(), tenantId(), "N", "d",
                RewardType.FREE_PRODUCT, RewardValue.product("X"), 0, null, null, null, 0, null, null);
        assertFalse(reward.isRedeemableWithPoints());
    }

    @Test
    void create_nonPositiveStockTotal_throws() {
        assertThrows(RewardDomainException.class, () -> Reward.create(UUID.randomUUID(), tenantId(), "N", "d",
                RewardType.FREE_PRODUCT, RewardValue.product("X"), 100, 0, null, null, 0, null, null));
        assertThrows(RewardDomainException.class, () -> Reward.create(UUID.randomUUID(), tenantId(), "N", "d",
                RewardType.FREE_PRODUCT, RewardValue.product("X"), 100, -5, null, null, 0, null, null));
    }

    @Test
    void create_startsInDraftWithStockRemainingMirroringStockTotal() {
        Reward reward = limitedStockReward(10);
        assertEquals(RewardStatus.DRAFT, reward.status());
        assertEquals(10, reward.stockRemaining());
    }

    // ── State transitions ──

    @Test
    void activate_fromDraftPausedOrExhausted_allowed_fromArchivedOrExpired_notAllowed() {
        assertEquals(RewardStatus.ACTIVE, unlimitedReward().activate().status());

        Reward archived = unlimitedReward().activate().archive();
        assertThrows(RewardDomainException.class, archived::activate);
    }

    @Test
    void pause_onlyFromActive() {
        Reward draft = unlimitedReward();
        assertThrows(RewardDomainException.class, draft::pause);

        Reward active = draft.activate();
        assertEquals(RewardStatus.PAUSED, active.pause().status());
    }

    @Test
    void archive_isTerminal() {
        Reward archived = unlimitedReward().archive();
        assertThrows(RewardDomainException.class, archived::activate);
        assertThrows(RewardDomainException.class, archived::pause);
        assertThrows(RewardDomainException.class, archived::archive);
    }

    @Test
    void decrementStock_unlimitedReward_isNoOp() {
        Reward reward = unlimitedReward();
        Reward after = reward.decrementStock();
        assertNull(after.stockRemaining());
        assertEquals(reward.version(), after.version(), "no-op must not bump the version");
    }

    @Test
    void decrementStock_reachingZero_marksExhausted() {
        Reward reward = limitedStockReward(1).activate();
        Reward exhausted = reward.decrementStock();
        assertEquals(0, exhausted.stockRemaining());
        assertEquals(RewardStatus.EXHAUSTED, exhausted.status());
    }

    @Test
    void decrementStock_belowZero_throws() {
        Reward exhausted = limitedStockReward(1).activate().decrementStock();
        assertThrows(RewardDomainException.class, exhausted::decrementStock);
    }

    @Test
    void restoreStock_reactivatesFromExhausted() {
        Reward exhausted = limitedStockReward(1).activate().decrementStock();
        assertEquals(RewardStatus.EXHAUSTED, exhausted.status());
        Reward restored = exhausted.restoreStock(3);
        assertEquals(3, restored.stockRemaining());
        assertEquals(RewardStatus.ACTIVE, restored.status());
    }

    @Test
    void restoreStock_unlimitedReward_isNoOp() {
        Reward reward = unlimitedReward();
        assertNull(reward.restoreStock(5).stockRemaining());
    }

    @Test
    void isAvailableAt_requiresActiveAndWithinValidityWindow() {
        Instant now = Instant.now();
        Reward outsideWindow = Reward.create(UUID.randomUUID(), tenantId(), "N", "d",
                RewardType.FREE_PRODUCT, RewardValue.product("X"), 100, null,
                now.plus(1, ChronoUnit.DAYS), null, 0, null, null).activate();
        assertFalse(outsideWindow.isAvailableAt(now), "not yet valid");

        Reward draftInWindow = Reward.create(UUID.randomUUID(), tenantId(), "N", "d",
                RewardType.FREE_PRODUCT, RewardValue.product("X"), 100, null, null, null, 0, null, null);
        assertFalse(draftInWindow.isAvailableAt(now), "DRAFT is never available even inside the window");

        Reward available = unlimitedReward().activate();
        assertTrue(available.isAvailableAt(now));
    }

    @Test
    void update_partialUpdate_onlyOverwritesNonNullNonBlankFields() {
        // Reward.update() mutates `this` in place and returns it (not immutable) -- capture the
        // original name up front, otherwise comparing reward.name() to updated.name() after the
        // call is a no-op self-comparison (same object) that can't catch a real overwrite bug.
        Reward reward = unlimitedReward();
        String originalName = reward.name();
        Reward updated = reward.update(null, "new description", null, Map.of("k", "v"));
        assertEquals(originalName, updated.name(), "null name must not overwrite");
        assertEquals("new description", updated.description());
        assertEquals(Map.of("k", "v"), updated.metadata());
    }

    @Test
    void metadata_getterReturnsDefensiveImmutableCopy() {
        Reward reward = Reward.create(UUID.randomUUID(), tenantId(), "N", "d",
                RewardType.FREE_PRODUCT, RewardValue.product("X"), 100, null, null, null, 0, null,
                new java.util.HashMap<>(Map.of("k", "v")));
        assertThrows(UnsupportedOperationException.class, () -> reward.metadata().put("k2", "v2"));
    }
}
