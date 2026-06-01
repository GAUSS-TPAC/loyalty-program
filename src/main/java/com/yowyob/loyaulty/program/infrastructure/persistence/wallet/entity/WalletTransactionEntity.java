package com.yowyob.loyaulty.program.infrastructure.persistence.wallet.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entité R2DBC représentant la table {@code wallet_transactions}.
 *
 * <p>Immuable en pratique : jamais mis à jour après création sauf
 * {@code status} et {@code completed_at} (gérés via requête dédiée).
 * Pas d'{@code @Version} car append-only.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("wallet_transactions")
public class WalletTransactionEntity {

    @Id
    @Column("id")
    private UUID id;

    @Column("wallet_id")
    private UUID walletId;

    @Column("tenant_id")
    private UUID tenantId;

    // ── Idempotence ───────────────────────────────────────────
    @Column("idempotency_key")
    private String idempotencyKey;

    // ── Nature ─────────────────────────────────────────────────
    @Column("type")
    private String type;                    // TransactionType.name()

    @Column("source")
    private String source;                  // TransactionSource.name()

    @Column("status")
    private String status;                  // TransactionStatus.name()

    // ── Montant ─────────────────────────────────────────────────
    @Column("amount")
    private BigDecimal amount;

    @Column("currency")
    private String currency;

    @Column("balance_after")
    private BigDecimal balanceAfter;

    // ── Liens ────────────────────────────────────────────────────
    @Column("payment_request_id")
    private UUID paymentRequestId;

    @Column("original_transaction_id")
    private UUID originalTransactionId;

    // ── Contexte ─────────────────────────────────────────────────
    @Column("description")
    private String description;

    @Column("metadata")
    private String metadata;

    // ── Horodatages ───────────────────────────────────────────────
    @Column("created_at")
    private Instant createdAt;

    @Column("completed_at")
    private Instant completedAt;
}
