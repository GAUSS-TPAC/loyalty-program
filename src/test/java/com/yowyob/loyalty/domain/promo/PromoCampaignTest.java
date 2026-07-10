package com.yowyob.loyalty.domain.promo;

import com.yowyob.loyalty.domain.promo.exception.PromoDomainException;
import com.yowyob.loyalty.domain.promo.model.PromoCampaign;
import com.yowyob.loyalty.domain.promo.model.PromoDiscountType;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PromoCampaignTest {

    private static TenantId tenantId() {
        return new TenantId(UUID.randomUUID());
    }

    private static PromoCampaign percentCampaign(BigDecimal pct) {
        return PromoCampaign.create(UUID.randomUUID(), tenantId(), "welcome10", "Welcome",
                PromoDiscountType.PERCENTAGE, pct, BigDecimal.ZERO, 0, 0, Instant.now(), null);
    }

    @Test
    void create_blankCode_throws() {
        assertThrows(PromoDomainException.class, () -> PromoCampaign.create(
                UUID.randomUUID(), tenantId(), "  ", "Name", PromoDiscountType.FIXED_AMOUNT,
                BigDecimal.TEN, BigDecimal.ZERO, 0, 0, Instant.now(), null));
    }

    @Test
    void create_blankName_throws() {
        assertThrows(PromoDomainException.class, () -> PromoCampaign.create(
                UUID.randomUUID(), tenantId(), "CODE", "  ", PromoDiscountType.FIXED_AMOUNT,
                BigDecimal.TEN, BigDecimal.ZERO, 0, 0, Instant.now(), null));
    }

    @Test
    void create_nonPositiveDiscountValue_throws() {
        assertThrows(PromoDomainException.class, () -> PromoCampaign.create(
                UUID.randomUUID(), tenantId(), "CODE", "Name", PromoDiscountType.FIXED_AMOUNT,
                BigDecimal.ZERO, BigDecimal.ZERO, 0, 0, Instant.now(), null));
        assertThrows(PromoDomainException.class, () -> PromoCampaign.create(
                UUID.randomUUID(), tenantId(), "CODE", "Name", PromoDiscountType.FIXED_AMOUNT,
                null, BigDecimal.ZERO, 0, 0, Instant.now(), null));
    }

    @Test
    void create_percentageOver100_throws() {
        assertThrows(PromoDomainException.class, () -> percentCampaign(new BigDecimal("100.01")));
    }

    @Test
    void create_percentageExactly100_allowed() {
        PromoCampaign campaign = percentCampaign(new BigDecimal("100"));
        assertEquals(new BigDecimal("100"), campaign.discountValue());
    }

    @Test
    void create_nullStartDate_throws() {
        assertThrows(PromoDomainException.class, () -> PromoCampaign.create(
                UUID.randomUUID(), tenantId(), "CODE", "Name", PromoDiscountType.FIXED_AMOUNT,
                BigDecimal.TEN, BigDecimal.ZERO, 0, 0, null, null));
    }

    @Test
    void create_endBeforeStart_throws() {
        Instant start = Instant.now();
        assertThrows(PromoDomainException.class, () -> PromoCampaign.create(
                UUID.randomUUID(), tenantId(), "CODE", "Name", PromoDiscountType.FIXED_AMOUNT,
                BigDecimal.TEN, BigDecimal.ZERO, 0, 0, start, start.minusSeconds(1)));
    }

    @Test
    void create_normalizesCodeAndDefaultsMinOrderAmountAndStartsInactive() {
        PromoCampaign campaign = PromoCampaign.create(UUID.randomUUID(), tenantId(), "  welcome10  ",
                "Welcome", PromoDiscountType.FIXED_AMOUNT, BigDecimal.TEN, null, 0, 0, Instant.now(), null);
        assertEquals("WELCOME10", campaign.code());
        assertEquals(BigDecimal.ZERO, campaign.minOrderAmount());
        assertFalse(campaign.isActive());
    }

    @Test
    void activateAndDeactivate_haveNoPreconditionAndToggleState() {
        PromoCampaign campaign = percentCampaign(BigDecimal.TEN);
        assertFalse(campaign.isActive());
        PromoCampaign activated = campaign.activate();
        assertTrue(activated.isActive());
        PromoCampaign deactivated = activated.deactivate();
        assertFalse(deactivated.isActive());
    }

    @Test
    void isExpired_trueOnlyWhenEndDateInPast() {
        PromoCampaign noEndDate = percentCampaign(BigDecimal.TEN);
        assertFalse(noEndDate.isExpired(Instant.now().plus(365, ChronoUnit.DAYS)));

        PromoCampaign withEnd = PromoCampaign.create(UUID.randomUUID(), tenantId(), "CODE", "Name",
                PromoDiscountType.FIXED_AMOUNT, BigDecimal.TEN, BigDecimal.ZERO, 0, 0,
                Instant.now().minusSeconds(10), Instant.now().minusSeconds(1));
        assertTrue(withEnd.isExpired(Instant.now()));
    }

    @Test
    void isStarted_falseBeforeStartDate() {
        PromoCampaign future = PromoCampaign.create(UUID.randomUUID(), tenantId(), "CODE", "Name",
                PromoDiscountType.FIXED_AMOUNT, BigDecimal.TEN, BigDecimal.ZERO, 0, 0,
                Instant.now().plus(1, ChronoUnit.DAYS), null);
        assertFalse(future.isStarted(Instant.now()));
        assertTrue(future.isStarted(Instant.now().plus(2, ChronoUnit.DAYS)));
    }

    @Test
    void isUnlimited_trueWhenMaxUsesZero() {
        PromoCampaign unlimited = PromoCampaign.create(UUID.randomUUID(), tenantId(), "CODE", "Name",
                PromoDiscountType.FIXED_AMOUNT, BigDecimal.TEN, BigDecimal.ZERO, 0, 0, Instant.now(), null);
        assertTrue(unlimited.isUnlimited());

        PromoCampaign limited = PromoCampaign.create(UUID.randomUUID(), tenantId(), "CODE", "Name",
                PromoDiscountType.FIXED_AMOUNT, BigDecimal.TEN, BigDecimal.ZERO, 5, 0, Instant.now(), null);
        assertFalse(limited.isUnlimited());
    }

    @Test
    void hasPerMemberLimit_trueOnlyWhenPositive() {
        PromoCampaign noLimit = PromoCampaign.create(UUID.randomUUID(), tenantId(), "CODE", "Name",
                PromoDiscountType.FIXED_AMOUNT, BigDecimal.TEN, BigDecimal.ZERO, 0, 0, Instant.now(), null);
        assertFalse(noLimit.hasPerMemberLimit());

        PromoCampaign withLimit = PromoCampaign.create(UUID.randomUUID(), tenantId(), "CODE", "Name",
                PromoDiscountType.FIXED_AMOUNT, BigDecimal.TEN, BigDecimal.ZERO, 0, 1, Instant.now(), null);
        assertTrue(withLimit.hasPerMemberLimit());
    }

    @Test
    void calculateDiscount_percentage() {
        PromoCampaign campaign = percentCampaign(new BigDecimal("10"));
        assertEquals(0, new BigDecimal("10.00").compareTo(campaign.calculateDiscount(new BigDecimal("100"))));
    }

    @Test
    void calculateDiscount_fixedAmount_cappedAtOrderAmount() {
        PromoCampaign campaign = PromoCampaign.create(UUID.randomUUID(), tenantId(), "CODE", "Name",
                PromoDiscountType.FIXED_AMOUNT, new BigDecimal("50"), BigDecimal.ZERO, 0, 0, Instant.now(), null);
        assertEquals(0, new BigDecimal("50").compareTo(campaign.calculateDiscount(new BigDecimal("100"))));
        assertEquals(0, new BigDecimal("20").compareTo(campaign.calculateDiscount(new BigDecimal("20"))),
                "fixed discount cannot exceed the order amount");
    }

    @Test
    void calculateDiscount_freeItem_returnsFlatValueRegardlessOfOrderAmount() {
        PromoCampaign campaign = PromoCampaign.create(UUID.randomUUID(), tenantId(), "CODE", "Name",
                PromoDiscountType.FREE_ITEM, new BigDecimal("15"), BigDecimal.ZERO, 0, 0, Instant.now(), null);
        assertEquals(0, new BigDecimal("15").compareTo(campaign.calculateDiscount(new BigDecimal("1000"))));
    }

    @Test
    void reconstruct_bypassesValidation() {
        Instant now = Instant.now();
        PromoCampaign reconstructed = PromoCampaign.reconstruct(UUID.randomUUID(), tenantId(), "code",
                "", PromoDiscountType.PERCENTAGE, new BigDecimal("500"), BigDecimal.ZERO, -1, -1,
                now, now.minusSeconds(1), true, now, now);
        assertTrue(reconstructed.isActive());
    }
}
