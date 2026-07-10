package com.yowyob.loyalty.domain.subscription;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.subscription.model.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionModelsTest {

    private static TenantId tenantId() { return new TenantId(UUID.randomUUID()); }

    private static PlanFeatures features(int maxRules, int maxMembers, int maxEvents) {
        return new PlanFeatures(maxRules, maxMembers, maxEvents, true, true, true, true);
    }

    // ── SubscriptionPlan ──

    @Test
    void plan_create_blankCode_throwsPlainIllegalArgumentException() {
        // Documented quirk: this is a plain IllegalArgumentException, not a SubscriptionDomainException.
        assertThrows(IllegalArgumentException.class, () -> SubscriptionPlan.create(
                "  ", "Name", "desc", BigDecimal.TEN, BigDecimal.valueOf(100), "XAF", features(10, 100, 1000)));
    }

    @Test
    void plan_create_blankName_throws() {
        assertThrows(IllegalArgumentException.class, () -> SubscriptionPlan.create(
                "CODE", " ", "desc", BigDecimal.TEN, BigDecimal.valueOf(100), "XAF", features(10, 100, 1000)));
    }

    @Test
    void plan_create_normalizesCodeToUppercaseAndStartsActive() {
        SubscriptionPlan plan = SubscriptionPlan.create("  pro  ".trim(), "Pro", "desc",
                BigDecimal.TEN, BigDecimal.valueOf(100), "XAF", features(10, 100, 1000));
        assertEquals("PRO", plan.code());
        assertTrue(plan.active());
    }

    @Test
    void plan_priceFor_selectsMonthlyOrYearly() {
        SubscriptionPlan plan = SubscriptionPlan.create("PRO", "Pro", "desc",
                new BigDecimal("10"), new BigDecimal("100"), "XAF", features(10, 100, 1000));
        assertEquals(0, new BigDecimal("10").compareTo(plan.priceFor(BillingCycle.MONTHLY)));
        assertEquals(0, new BigDecimal("100").compareTo(plan.priceFor(BillingCycle.YEARLY)));
    }

    @Test
    void plan_activateDeactivate_haveNoPreconditionAndReturnVoid() {
        SubscriptionPlan plan = SubscriptionPlan.create("PRO", "Pro", "desc",
                BigDecimal.TEN, BigDecimal.valueOf(100), "XAF", features(10, 100, 1000));
        plan.deactivate();
        assertFalse(plan.active());
        plan.activate();
        assertTrue(plan.active());
    }

    // ── PlanFeatures ──

    @Test
    void planFeatures_unlimitedMembersAndEvents_signaledByZero() {
        PlanFeatures unlimited = features(10, 0, 0);
        assertTrue(unlimited.isUnlimitedMembers());
        assertTrue(unlimited.isUnlimitedEvents());
    }

    @Test
    void planFeatures_unlimitedRules_signaledByNegativeNotZero() {
        // Documented asymmetry vs members/events: 0 rules means "zero rules allowed", not unlimited.
        PlanFeatures zeroRules = features(0, 100, 1000);
        assertFalse(zeroRules.isUnlimitedRules());

        PlanFeatures negativeRules = features(-1, 100, 1000);
        assertTrue(negativeRules.isUnlimitedRules());
    }

    // ── TenantSubscription ──

    @Test
    void subscription_createTrial_startsInTrialWithMatchingPeriodEndAndTrialEnd() {
        TenantSubscription sub = TenantSubscription.createTrial(tenantId(), UUID.randomUUID(), 14);
        assertEquals(SubscriptionStatus.TRIAL, sub.status());
        assertEquals(BillingCycle.MONTHLY, sub.billingCycle());
        assertEquals(sub.currentPeriodEnd(), sub.trialEndDate());
    }

    @Test
    void subscription_createActive_yearlyCycleSetsThreeSixtyFiveDayPeriod() {
        TenantSubscription sub = TenantSubscription.createActive(tenantId(), UUID.randomUUID(), BillingCycle.YEARLY);
        assertEquals(SubscriptionStatus.ACTIVE, sub.status());
        assertNull(sub.trialEndDate());
        long daysUntilEnd = java.time.Duration.between(sub.currentPeriodStart(), sub.currentPeriodEnd()).toDays();
        assertEquals(365, daysUntilEnd);
    }

    @Test
    void subscription_isExpired_comparesAgainstCurrentPeriodEnd() {
        TenantSubscription sub = TenantSubscription.createTrial(tenantId(), UUID.randomUUID(), 1);
        assertFalse(sub.isExpired(Instant.now()));
        assertTrue(sub.isExpired(Instant.now().plusSeconds(2 * 24 * 3600)));
    }

    @Test
    void subscription_cancel_terminalGuard_throwsPlainIllegalStateException() {
        TenantSubscription sub = TenantSubscription.createActive(tenantId(), UUID.randomUUID(), BillingCycle.MONTHLY);
        sub.cancel();
        assertEquals(SubscriptionStatus.CANCELLED, sub.status());
        assertNotNull(sub.cancelledAt());
        // Documented quirk: plain IllegalStateException, not a SubscriptionDomainException.
        assertThrows(IllegalStateException.class, sub::cancel);
    }

    @Test
    void subscription_renew_forcesActiveRegardlessOfPriorStatus() {
        TenantSubscription sub = TenantSubscription.createActive(tenantId(), UUID.randomUUID(), BillingCycle.MONTHLY);
        sub.markPastDue();
        assertEquals(SubscriptionStatus.PAST_DUE, sub.status());
        sub.renew();
        assertEquals(SubscriptionStatus.ACTIVE, sub.status(), "renew() must force ACTIVE even from PAST_DUE");
    }

    @Test
    void subscription_version_neverIncrementsDespiteMutations() {
        // Documented quirk: `version` is final and set once at construction; none of the mutators
        // (cancel/renew/markExpired/markPastDue/changePlan/activate) touch it.
        TenantSubscription sub = TenantSubscription.createActive(tenantId(), UUID.randomUUID(), BillingCycle.MONTHLY);
        long initialVersion = sub.version();
        sub.renew();
        sub.markPastDue();
        sub.changePlan(UUID.randomUUID());
        sub.activate();
        assertEquals(initialVersion, sub.version());
    }

    // ── InvoiceRecord ──

    @Test
    void invoice_generate_startsPendingWithSevenDayDueDate() {
        Instant periodStart = Instant.now();
        InvoiceRecord invoice = InvoiceRecord.generate(tenantId(), UUID.randomUUID(), UUID.randomUUID(),
                new BigDecimal("10"), "XAF", periodStart, periodStart.plusSeconds(30L * 24 * 3600));
        assertEquals(InvoiceStatus.PENDING, invoice.status());
        assertNull(invoice.paidAt());
        long daysUntilDue = java.time.Duration.between(invoice.createdAt(), invoice.dueDate()).toDays();
        assertEquals(7, daysUntilDue);
    }

    @Test
    void invoice_isOverdue_trueOnlyWhenPendingAndPastDueDate() {
        InvoiceRecord paid = InvoiceRecord.reconstruct(UUID.randomUUID(), tenantId(), UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.TEN, "XAF", InvoiceStatus.PAID, Instant.now(), Instant.now(),
                Instant.now().minusSeconds(1), Instant.now(), Instant.now());
        assertFalse(paid.isOverdue(Instant.now()), "a PAID invoice is never overdue even past its due date");

        InvoiceRecord pendingPastDue = InvoiceRecord.reconstruct(UUID.randomUUID(), tenantId(), UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.TEN, "XAF", InvoiceStatus.PENDING, Instant.now(), Instant.now(),
                Instant.now().minusSeconds(1), null, Instant.now());
        assertTrue(pendingPastDue.isOverdue(Instant.now()));
    }

    @Test
    void invoice_markPaid_setsPaidAt() {
        InvoiceRecord invoice = InvoiceRecord.generate(tenantId(), UUID.randomUUID(), UUID.randomUUID(),
                BigDecimal.TEN, "XAF", Instant.now(), Instant.now());
        invoice.markPaid();
        assertEquals(InvoiceStatus.PAID, invoice.status());
        assertNotNull(invoice.paidAt());
    }
}
