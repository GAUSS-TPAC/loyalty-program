package com.yowyob.loyalty.domain.reward;

import com.yowyob.loyalty.domain.reward.exception.GrantAlreadyUsedException;
import com.yowyob.loyalty.domain.reward.exception.GrantExpiredException;
import com.yowyob.loyalty.domain.reward.exception.RewardDomainException;
import com.yowyob.loyalty.domain.reward.model.*;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RewardGrantModelTest {

    private static TenantId tenantId() { return new TenantId(UUID.randomUUID()); }
    private static UserId userId() { return new UserId(UUID.randomUUID()); }

    private static Reward rewardWithExpiry(int grantExpiryDays, int maxApplications) {
        return Reward.create(UUID.randomUUID(), tenantId(), "Reward", "d", RewardType.PERCENT_DISCOUNT,
                RewardValue.percent(BigDecimal.TEN, maxApplications), 100, null, null, null, grantExpiryDays, null, null);
    }

    @Test
    void create_expiresAtNullWhenGrantExpiryDaysNotPositive() {
        RewardGrant grant = RewardGrant.create(UUID.randomUUID(), tenantId(), userId(),
                rewardWithExpiry(0, 1), GrantSource.RULE_ENGINE, null, null);
        assertNull(grant.expiresAt());
        assertFalse(grant.isExpired());
    }

    @Test
    void create_startsAsPendingWithRemainingApplicationsFromRewardValue() {
        RewardGrant grant = RewardGrant.create(UUID.randomUUID(), tenantId(), userId(),
                rewardWithExpiry(0, 3), GrantSource.RULE_ENGINE, null, null);
        assertEquals(GrantStatus.PENDING, grant.status());
        assertEquals(3, grant.remainingApplications());
    }

    @Test
    void activate_onlyFromPending() {
        RewardGrant grant = RewardGrant.create(UUID.randomUUID(), tenantId(), userId(),
                rewardWithExpiry(0, 1), GrantSource.RULE_ENGINE, null, null);
        RewardGrant active = grant.activate();
        assertEquals(GrantStatus.ACTIVE, active.status());
        assertThrows(RewardDomainException.class, active::activate);
    }

    @Test
    void consume_notActive_throwsAlreadyUsed() {
        RewardGrant pending = RewardGrant.create(UUID.randomUUID(), tenantId(), userId(),
                rewardWithExpiry(0, 1), GrantSource.RULE_ENGINE, null, null);
        assertThrows(GrantAlreadyUsedException.class, () -> pending.consume("ctx"));
    }

    @Test
    void consume_expired_throwsExpired() {
        RewardGrant expired = RewardGrant.reconstruct(UUID.randomUUID(), tenantId(), userId(), UUID.randomUUID(),
                "Reward", RewardType.PERCENT_DISCOUNT, RewardValue.percent(BigDecimal.TEN, 1),
                GrantSource.RULE_ENGINE, null, null, GrantStatus.ACTIVE, 1,
                Instant.now().minusSeconds(100), Instant.now().minusSeconds(1), null, null, 0);
        assertThrows(GrantExpiredException.class, () -> expired.consume("ctx"));
    }

    @Test
    void consume_singleApplication_transitionsToUsed() {
        RewardGrant active = RewardGrant.create(UUID.randomUUID(), tenantId(), userId(),
                rewardWithExpiry(0, 1), GrantSource.RULE_ENGINE, null, null).activate();
        RewardGrant consumed = active.consume("order-123");
        assertEquals(GrantStatus.USED, consumed.status());
        assertEquals(0, consumed.remainingApplications());
        assertEquals("order-123", consumed.usedInContext());
        assertNotNull(consumed.usedAt());
    }

    @Test
    void consume_multiApplication_staysActiveUntilExhausted() {
        RewardGrant active = RewardGrant.create(UUID.randomUUID(), tenantId(), userId(),
                rewardWithExpiry(0, 3), GrantSource.RULE_ENGINE, null, null).activate();

        RewardGrant afterFirst = active.consume("order-1");
        assertEquals(GrantStatus.ACTIVE, afterFirst.status(), "grant with remaining applications must stay ACTIVE");
        assertEquals(2, afterFirst.remainingApplications());

        RewardGrant afterSecond = afterFirst.consume("order-2");
        assertEquals(GrantStatus.ACTIVE, afterSecond.status());

        RewardGrant afterThird = afterSecond.consume("order-3");
        assertEquals(GrantStatus.USED, afterThird.status(), "last application must flip to USED");
    }

    @Test
    void expire_onlyFromActive() {
        RewardGrant pending = RewardGrant.create(UUID.randomUUID(), tenantId(), userId(),
                rewardWithExpiry(0, 1), GrantSource.RULE_ENGINE, null, null);
        assertThrows(RewardDomainException.class, pending::expire);

        RewardGrant active = pending.activate();
        assertEquals(GrantStatus.EXPIRED, active.expire().status());
    }

    @Test
    void reverseAndCancel_allowedFromPendingOrActiveOnly() {
        // RewardGrant mutators mutate `this` in place and return it (not immutable) -- each
        // scenario needs its own freshly-created grant, not a shared/reused instance.
        RewardGrant pending = RewardGrant.create(UUID.randomUUID(), tenantId(), userId(),
                rewardWithExpiry(0, 1), GrantSource.RULE_ENGINE, null, null);
        assertEquals(GrantStatus.REVERSED, pending.reverse("fraud").status());

        RewardGrant used = RewardGrant.create(UUID.randomUUID(), tenantId(), userId(),
                rewardWithExpiry(0, 1), GrantSource.RULE_ENGINE, null, null).activate().consume("ctx");
        assertThrows(RewardDomainException.class, () -> used.cancel("too late"));
    }

    @Test
    void isExpired_trueOnlyAfterExpiryInstant() {
        RewardGrant grant = RewardGrant.reconstruct(UUID.randomUUID(), tenantId(), userId(), UUID.randomUUID(),
                "R", RewardType.FREE_PRODUCT, RewardValue.product("X"), GrantSource.RULE_ENGINE, null, null,
                GrantStatus.ACTIVE, 1, Instant.now(), Instant.now().plusSeconds(60), null, null, 0);
        assertFalse(grant.isExpired());
    }
}
