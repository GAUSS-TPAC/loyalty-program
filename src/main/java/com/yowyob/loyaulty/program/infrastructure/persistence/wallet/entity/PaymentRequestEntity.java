package com.yowyob.loyaulty.program.infrastructure.persistence.wallet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entité R2DBC représentant la table {@code payment_requests}.
 *
 * <p>Cycle de vie mutable (statut évolue selon les webhooks provider).
 * L'{@code @Version} protège contre les mises à jour concurrentes
 * (ex. plusieurs webhooks reçus simultanément pour la même requête).</p>
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("payment_requests")
public class PaymentRequestEntity {

    @Id
    @Column("id")
    private UUID id;

    @Column("wallet_id")
    private UUID walletId;

    @Column("tenant_id")
    private UUID tenantId;

    @Column("wallet_transaction_id")
    private UUID walletTransactionId;

    // ── Provider & direction ──────────────────────────────────
    @Column("provider")
    private String provider;                // PaymentProvider.name()

    @Column("direction")
    private String direction;               // PaymentDirection.name()

    // ── Montant ───────────────────────────────────────────────
    @Column("amount")
    private BigDecimal amount;

    @Column("currency")
    private String currency;

    // ── Identifiants provider ─────────────────────────────────
    @Column("external_reference")
    private String externalReference;

    @Column("mobile_money_phone_number")
    private String mobileMoneyPhoneNumber;

    // ── État & retry ──────────────────────────────────────────
    @Column("status")
    private String status;                  // PaymentRequestStatus.name()

    @Column("retry_count")
    private int retryCount;

    @Column("max_retries")
    private int maxRetries;

    // ── Horodatages ───────────────────────────────────────────
    @Column("created_at")
    private Instant createdAt;

    @Column("next_retry_at")
    private Instant nextRetryAt;

    @Column("expires_at")
    private Instant expiresAt;

    @Column("resolved_at")
    private Instant resolvedAt;

    // ── Réponse provider ─────────────────────────────────────
    @Column("provider_error_message")
    private String providerErrorMessage;

    @Column("webhook_payload")
    private String webhookPayload;

    @Version
    @Column("version")
    private Long version;
}
