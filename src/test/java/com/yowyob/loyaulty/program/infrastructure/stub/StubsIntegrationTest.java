package com.yowyob.loyaulty.program.infrastructure.stub;

import com.yowyob.loyaulty.program.domain.wallet.model.KycStatus;
import com.yowyob.loyaulty.program.domain.wallet.model.PaymentStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Tests d'intégration Spring pour les stubs du Kernel Core.
 *
 * <p>Vérifie que les trois stubs sont correctement chargés par Spring dans le profil
 * {@code stub} et retournent les valeurs attendues.</p>
 *
 * <p>Ces tests démarrent un contexte Spring allégé pour valider le câblage des beans.</p>
 */
@SpringBootTest
@ActiveProfiles("stub")
class StubsIntegrationTest {

    @Autowired
    private TenantQueryStub tenantQueryStub;

    @Autowired
    private PaymentGatewayStub paymentGatewayStub;

    @Autowired
    private KycVerificationStub kycVerificationStub;

    // ── TenantQueryStub ───────────────────────────────────────────────────────

    @Test
    void tenantQueryStub_tenantExists_returnsTrue() {
        UUID tenantId = UUID.randomUUID();

        StepVerifier.create(tenantQueryStub.tenantExists(tenantId))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void tenantQueryStub_getTenantCurrencyCode_returnsXAF() {
        UUID tenantId = UUID.randomUUID();

        StepVerifier.create(tenantQueryStub.getTenantCurrencyCode(tenantId))
                .expectNext("XAF")
                .verifyComplete();
    }

    @Test
    void tenantQueryStub_getTenantMaxRules_returns50() {
        UUID tenantId = UUID.randomUUID();

        StepVerifier.create(tenantQueryStub.getTenantMaxRules(tenantId))
                .expectNext(50)
                .verifyComplete();
    }

    // ── PaymentGatewayStub ────────────────────────────────────────────────────

    @Test
    void paymentGatewayStub_initiateTopUp_returnsPendingWithUssdCode() {
        UUID tenantId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        StepVerifier.create(paymentGatewayStub.initiateTopUp(
                        tenantId, memberId,
                        BigDecimal.valueOf(5000), "XAF",
                        "MTN", UUID.randomUUID().toString()))
                .assertNext(result -> {
                    assert result.status() == PaymentStatus.PENDING;
                    assert result.externalRef() != null && !result.externalRef().isBlank();
                    assert result.ussdCode() != null && result.ussdCode().contains("5000");
                    assert result.requiresUserAction();
                    assert result.expiresAt() != null;
                })
                .verifyComplete();
    }

    @Test
    void paymentGatewayStub_getPaymentStatus_returnsCompleted() {
        UUID tenantId = UUID.randomUUID();

        StepVerifier.create(paymentGatewayStub.getPaymentStatus(tenantId, "ref-stub-test"))
                .expectNext(PaymentStatus.COMPLETED)
                .verifyComplete();
    }

    @Test
    void paymentGatewayStub_cancelPayment_completesWithoutError() {
        UUID tenantId = UUID.randomUUID();

        StepVerifier.create(paymentGatewayStub.cancelPayment(tenantId, "ref-to-cancel"))
                .verifyComplete();
    }

    // ── KycVerificationStub ───────────────────────────────────────────────────

    @Test
    void kycVerificationStub_isMemberVerified_returnsTrue() {
        UUID tenantId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        StepVerifier.create(kycVerificationStub.isMemberVerified(tenantId, memberId))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void kycVerificationStub_getMemberKycStatus_returnsVerified() {
        UUID tenantId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();

        StepVerifier.create(kycVerificationStub.getMemberKycStatus(tenantId, memberId))
                .expectNext(KycStatus.VERIFIED)
                .verifyComplete();
    }
}
