package com.yowyob.loyalty.domain.wallet.model;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.exception.WalletDomainException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Agrégat racine du module wallet.
 */
public class Wallet {
    private final UUID id;
    private final TenantId tenantId;
    private final UserId memberId;
    private BigDecimal balance;
    private final String currencyCode;
    private WalletStatus status;
    private long version;
    private Instant frozenAt;
    private String frozenReason;
    private Instant closedAt;
    private final Instant createdAt;
    private Instant updatedAt;

    public Wallet(UUID id, TenantId tenantId, UserId memberId, BigDecimal balance, String currencyCode, 
                   WalletStatus status, long version, Instant createdAt) {
        this.id = id;
        this.tenantId = tenantId;
        this.memberId = memberId;
        this.balance = balance;
        this.currencyCode = currencyCode;
        this.status = status;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
    }

    public static Wallet create(UUID id, TenantId tenantId, UserId memberId, String currencyCode, boolean autoActivate) {
        WalletStatus initialStatus = autoActivate ? WalletStatus.ACTIVE : WalletStatus.PENDING_KYC;
        return new Wallet(id, tenantId, memberId, BigDecimal.ZERO, currencyCode, initialStatus, 0, Instant.now());
    }

    public WalletCreditResult credit(BigDecimal amount, WalletPolicy policy) {
        policy.validateCredit(amount, this.balance)
                .ifPresent(msg -> { throw new WalletDomainException(msg); });
        
        if (!status.canCredit()) {
            throw new WalletDomainException("Wallet ne peut pas recevoir de crédit dans l'état " + status);
        }
        
        this.balance = this.balance.add(amount);
        this.version++;
        this.updatedAt = Instant.now();
        
        return new WalletCreditResult(this, amount, this.balance);
    }

    public WalletDebitResult debit(BigDecimal amount, WalletPolicy policy, BigDecimal todaySpent) {
        policy.validateDebit(amount, this.balance, todaySpent)
                .ifPresent(msg -> { throw new WalletDomainException(msg); });
        
        if (!status.canDebit()) {
            throw new WalletDomainException("Wallet ne peut pas être débité dans l'état " + status);
        }
        
        boolean otpRequired = policy.requiresOtp(amount);
        
        if (!otpRequired) {
            this.balance = this.balance.subtract(amount);
            this.version++;
            this.updatedAt = Instant.now();
        }
        
        return new WalletDebitResult(this, amount, this.balance, otpRequired, null);
    }

    public Wallet freeze(String reason) {
        if (!status.canTransitionTo(WalletStatus.FROZEN)) {
            throw new WalletDomainException("Transition vers FROZEN non autorisée depuis " + status);
        }
        this.status = WalletStatus.FROZEN;
        this.frozenAt = Instant.now();
        this.frozenReason = reason;
        this.version++;
        this.updatedAt = Instant.now();
        return this;
    }

    public Wallet unfreeze() {
        if (!status.canTransitionTo(WalletStatus.ACTIVE)) {
            throw new WalletDomainException("Transition vers ACTIVE non autorisée depuis " + status);
        }
        this.status = WalletStatus.ACTIVE;
        this.frozenAt = null;
        this.frozenReason = null;
        this.version++;
        this.updatedAt = Instant.now();
        return this;
    }

    public Wallet activate() {
        if (status != WalletStatus.PENDING_KYC) {
            throw new WalletDomainException("Seul un wallet PENDING_KYC peut être activé");
        }
        this.status = WalletStatus.ACTIVE;
        this.version++;
        this.updatedAt = Instant.now();
        return this;
    }

    public Wallet close() {
        if (!status.canTransitionTo(WalletStatus.CLOSED)) {
            throw new WalletDomainException("Transition vers CLOSED non autorisée depuis " + status);
        }
        // Il est souvent requis que le solde soit à zéro, mais le guide ne le mentionne pas explicitement ici.
        // On suit strictement les instructions du guide.
        this.status = WalletStatus.CLOSED;
        this.closedAt = Instant.now();
        this.version++;
        this.updatedAt = Instant.now();
        return this;
    }

    public WalletTransaction applyReversal(WalletTransaction original, String idempotencyKey) {
        if (status.isFinal()) {
            throw new WalletDomainException("Wallet fermé, reversal impossible");
        }
        BigDecimal balanceBefore = this.balance;
        if (original.type().isDebit()) {
            this.balance = this.balance.add(original.amount());
        } else {
            if (this.balance.compareTo(original.amount()) < 0) {
                throw new WalletDomainException("Solde insuffisant pour reversal de crédit");
            }
            this.balance = this.balance.subtract(original.amount());
        }
        this.version++;
        this.updatedAt = Instant.now();
        return new WalletTransaction(
            UUID.randomUUID(), this.id, this.tenantId, TransactionType.REVERSAL,
            original.amount(), balanceBefore, this.balance, TransactionStatus.COMPLETED,
            original.source(), idempotencyKey, null, original.id(), Map.of(), Instant.now()
        );
    }

    // Getters
    public UUID getId() { return id; }
    public TenantId getTenantId() { return tenantId; }
    public UserId getMemberId() { return memberId; }
    public BigDecimal getBalance() { return balance; }
    public String getCurrencyCode() { return currencyCode; }
    public WalletStatus getStatus() { return status; }
    public long getVersion() { return version; }
    public Instant getFrozenAt() { return frozenAt; }
    public String getFrozenReason() { return frozenReason; }
    public Instant getClosedAt() { return closedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
