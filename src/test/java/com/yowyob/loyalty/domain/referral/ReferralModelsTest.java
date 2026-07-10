package com.yowyob.loyalty.domain.referral;

import com.yowyob.loyalty.domain.referral.exception.ReferralAlreadyConvertedException;
import com.yowyob.loyalty.domain.referral.exception.ReferralDomainException;
import com.yowyob.loyalty.domain.referral.model.ReferralEvent;
import com.yowyob.loyalty.domain.referral.model.ReferralLink;
import com.yowyob.loyalty.domain.referral.model.ReferralProgram;
import com.yowyob.loyalty.domain.referral.model.ReferralStatus;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReferralModelsTest {

    private static TenantId tenantId() { return new TenantId(UUID.randomUUID()); }
    private static UserId userId() { return new UserId(UUID.randomUUID()); }

    // ── ReferralLink ──

    @Test
    void link_create_expiresAtNullWhenExpiryDaysNotPositive() {
        ReferralLink link = ReferralLink.create(UUID.randomUUID(), tenantId(), userId(), 0);
        assertNull(link.expiresAt());
        assertTrue(link.isValid(), "no expiry means always valid while active");
    }

    @Test
    void link_create_setsExpiryWhenDaysPositive() {
        ReferralLink link = ReferralLink.create(UUID.randomUUID(), tenantId(), userId(), 30);
        assertNotNull(link.expiresAt());
        assertTrue(link.isValid());
    }

    @Test
    void link_isValid_falseOnceExpired() {
        ReferralLink expired = ReferralLink.reconstruct(UUID.randomUUID(), tenantId(), userId(), "ABCD1234",
                java.time.Instant.now().minusSeconds(100), java.time.Instant.now().minusSeconds(1), 0, 0, true);
        assertFalse(expired.isValid());
    }

    @Test
    void link_isValid_falseWhenInactiveEvenIfNotExpired() {
        ReferralLink inactive = ReferralLink.reconstruct(UUID.randomUUID(), tenantId(), userId(), "ABCD1234",
                java.time.Instant.now(), null, 0, 0, false);
        assertFalse(inactive.isValid());
    }

    @Test
    void link_incrementUsageAndConversion_mutateInPlace() {
        ReferralLink link = ReferralLink.create(UUID.randomUUID(), tenantId(), userId(), 30);
        assertEquals(0, link.usageCount());
        link.incrementUsage();
        assertEquals(1, link.usageCount());
        link.incrementConversion();
        assertEquals(1, link.conversionCount());
    }

    @Test
    void link_create_generatesRandomEightCharCode() {
        ReferralLink a = ReferralLink.create(UUID.randomUUID(), tenantId(), userId(), 30);
        ReferralLink b = ReferralLink.create(UUID.randomUUID(), tenantId(), userId(), 30);
        assertEquals(8, a.code().length());
        assertNotEquals(a.code(), b.code(), "codes should be randomized, not deterministic");
    }

    // ── ReferralProgram ──

    @Test
    void program_create_blankName_throws() {
        assertThrows(ReferralDomainException.class, () -> ReferralProgram.create(
                UUID.randomUUID(), tenantId(), "  ", 5, 30, null, null, 0));
    }

    @Test
    void program_create_startsInactive() {
        ReferralProgram program = ReferralProgram.create(UUID.randomUUID(), tenantId(), "Refer a friend",
                5, 30, null, null, 0);
        assertFalse(program.isActive());
    }

    @Test
    void program_activateDeactivate_toggleState() {
        ReferralProgram program = ReferralProgram.create(UUID.randomUUID(), tenantId(), "Program", 5, 30, null, null, 0);
        assertTrue(program.activate().isActive());
        assertFalse(program.activate().deactivate().isActive());
    }

    @Test
    void program_update_keepsOldNameWhenBlankProvided() {
        ReferralProgram program = ReferralProgram.create(UUID.randomUUID(), tenantId(), "Original", 5, 30, null, null, 0);
        ReferralProgram updated = program.update("  ", 10, 60, 100);
        assertEquals("Original", updated.name(), "blank name must not overwrite the existing one");
        assertEquals(10, updated.maxReferralsPerReferrer());
        assertEquals(100, updated.minConversionAmount());
    }

    @Test
    void program_update_overwritesNameWhenNonBlank() {
        ReferralProgram program = ReferralProgram.create(UUID.randomUUID(), tenantId(), "Original", 5, 30, null, null, 0);
        ReferralProgram updated = program.update("New name", 10, 60, 100);
        assertEquals("New name", updated.name());
    }

    // ── ReferralEvent ──

    @Test
    void event_create_startsAsPending() {
        ReferralEvent event = ReferralEvent.create(UUID.randomUUID(), tenantId(), UUID.randomUUID(), userId(), userId());
        assertEquals(ReferralStatus.PENDING, event.status());
        assertNull(event.convertedAt());
    }

    @Test
    void event_enroll_onlyFromPending() {
        ReferralEvent event = ReferralEvent.create(UUID.randomUUID(), tenantId(), UUID.randomUUID(), userId(), userId());
        ReferralEvent enrolled = event.enroll();
        assertEquals(ReferralStatus.ENROLLED, enrolled.status());
        assertThrows(ReferralDomainException.class, enrolled::enroll, "cannot enroll twice");
    }

    @Test
    void event_convert_blockedOnceInFinalState() {
        ReferralEvent event = ReferralEvent.create(UUID.randomUUID(), tenantId(), UUID.randomUUID(), userId(), userId());
        ReferralEvent converted = event.convert(new BigDecimal("100"));
        assertEquals(ReferralStatus.CONVERTED, converted.status());
        assertNotNull(converted.convertedAt());

        assertThrows(ReferralAlreadyConvertedException.class, () -> converted.convert(new BigDecimal("50")));
    }

    @Test
    void event_convert_allowedDirectlyFromPending_notOnlyFromEnrolled() {
        ReferralEvent event = ReferralEvent.create(UUID.randomUUID(), tenantId(), UUID.randomUUID(), userId(), userId());
        // Quirk documented in the domain service report: convert() only guards against final states,
        // it does not require status == ENROLLED first.
        ReferralEvent converted = event.convert(BigDecimal.TEN);
        assertEquals(ReferralStatus.CONVERTED, converted.status());
    }

    @Test
    void event_markFraud_hasNoPreconditionAndSetsReason() {
        ReferralEvent event = ReferralEvent.create(UUID.randomUUID(), tenantId(), UUID.randomUUID(), userId(), userId());
        ReferralEvent fraud = event.markFraud("suspicious amount");
        assertEquals(ReferralStatus.FRAUD, fraud.status());
        assertEquals("suspicious amount", fraud.fraudReason());
        assertTrue(fraud.status().isFinal());
    }

    @Test
    void event_expire_hasNoPrecondition() {
        ReferralEvent event = ReferralEvent.create(UUID.randomUUID(), tenantId(), UUID.randomUUID(), userId(), userId());
        assertEquals(ReferralStatus.EXPIRED, event.expire().status());
    }
}
