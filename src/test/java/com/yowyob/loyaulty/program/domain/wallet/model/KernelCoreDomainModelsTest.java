package com.yowyob.loyaulty.program.domain.wallet.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests des nouveaux modèles de domaine liés au Kernel Core.
 *
 * <p>Tests purs JUnit 5 sans contexte Spring.
 * Aucun appel réseau, aucune base de données.</p>
 */
class KernelCoreDomainModelsTest {

    // ── PaymentStatus ─────────────────────────────────────────────────────────

    @Test
    void paymentStatus_completed_isFinalAndSuccessful() {
        assertTrue(PaymentStatus.COMPLETED.isFinal());
        assertTrue(PaymentStatus.COMPLETED.isSuccessful());
    }

    @Test
    void paymentStatus_failed_isFinalButNotSuccessful() {
        assertTrue(PaymentStatus.FAILED.isFinal());
        assertFalse(PaymentStatus.FAILED.isSuccessful());
    }

    @Test
    void paymentStatus_cancelled_isFinal() {
        assertTrue(PaymentStatus.CANCELLED.isFinal());
        assertFalse(PaymentStatus.CANCELLED.isSuccessful());
    }

    @Test
    void paymentStatus_expired_isFinal() {
        assertTrue(PaymentStatus.EXPIRED.isFinal());
        assertFalse(PaymentStatus.EXPIRED.isSuccessful());
    }

    @Test
    void paymentStatus_pending_isNotFinal() {
        assertFalse(PaymentStatus.PENDING.isFinal());
        assertFalse(PaymentStatus.PENDING.isSuccessful());
    }

    // ── KycStatus ─────────────────────────────────────────────────────────────

    @Test
    void kycStatus_verified_allowsWithdrawal() {
        assertTrue(KycStatus.VERIFIED.allowsWithdrawal());
    }

    @Test
    void kycStatus_notStarted_doesNotAllowWithdrawal() {
        assertFalse(KycStatus.NOT_STARTED.allowsWithdrawal());
    }

    @Test
    void kycStatus_pendingReview_doesNotAllowWithdrawal() {
        assertFalse(KycStatus.PENDING_REVIEW.allowsWithdrawal());
    }

    @Test
    void kycStatus_rejected_doesNotAllowWithdrawal() {
        assertFalse(KycStatus.REJECTED.allowsWithdrawal());
    }

    // ── PaymentInitiationResult ───────────────────────────────────────────────

    @Test
    void paymentInitiationResult_withUssdCode_requiresUserAction() {
        PaymentInitiationResult result = new PaymentInitiationResult(
                "ref-123",
                PaymentStatus.PENDING,
                null,
                "*126*1*500#",
                Instant.now().plusSeconds(300)
        );

        assertTrue(result.requiresUserAction());
        assertEquals("ref-123", result.externalRef());
        assertEquals(PaymentStatus.PENDING, result.status());
        assertNull(result.redirectUrl());
        assertNotNull(result.ussdCode());
        assertNotNull(result.expiresAt());
    }

    @Test
    void paymentInitiationResult_withRedirectUrl_requiresUserAction() {
        PaymentInitiationResult result = new PaymentInitiationResult(
                "pi_stripe_123",
                PaymentStatus.PENDING,
                "https://checkout.stripe.com/pay/xxx",
                null,
                Instant.now().plusSeconds(1800)
        );

        assertTrue(result.requiresUserAction());
        assertNotNull(result.redirectUrl());
        assertNull(result.ussdCode());
    }

    @Test
    void paymentInitiationResult_withNoUserAction_doesNotRequireAction() {
        PaymentInitiationResult result = new PaymentInitiationResult(
                "ref-auto",
                PaymentStatus.COMPLETED,
                null,
                null,
                null
        );

        assertFalse(result.requiresUserAction());
    }
}
