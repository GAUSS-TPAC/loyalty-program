package com.yowyob.loyaulty.program.domain.wallet.model;

import com.yowyob.loyaulty.program.domain.shared.model.AuditInfo;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.*;
import com.yowyob.loyaulty.program.domain.wallet.event.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Wallet {

    private final UUID id;
    private final TenantId tenantId;
    private final String memberId;
    private final String currency;
    private BigDecimal balance;
    private WalletStatus status;
    private WalletPolicy policy;
    private AuditInfo auditInfo;

    private final List<Object> domainEvents = new ArrayList<>();

    private Wallet(UUID id, TenantId tenantId, String memberId, String currency,
                   BigDecimal balance, WalletStatus status, WalletPolicy policy,
                   AuditInfo auditInfo) {
        this.id = id;
        this.tenantId = tenantId;
        this.memberId = memberId;
        this.currency = currency;
        this.balance = balance;
        this.status = status;
        this.policy = policy;
        this.auditInfo = auditInfo;
    }

    public static Wallet create(TenantId tenantId, String memberId, String currency,
                                 WalletPolicy policy, boolean autoActivate) {
        WalletStatus initialStatus = autoActivate ? WalletStatus.ACTIVE : WalletStatus.PENDING_KYC;
        Wallet wallet = new Wallet(
                UUID.randomUUID(), tenantId, memberId, currency,
                BigDecimal.ZERO, initialStatus, policy,
                AuditInfo.create("system")
        );
        wallet.domainEvents.add(new WalletCreatedEvent(wallet.id, tenantId, memberId, currency));
        return wallet;
    }

    public static Wallet reconstitute(UUID id, TenantId tenantId, String memberId,
                                       String currency, BigDecimal balance,
                                       WalletStatus status, WalletPolicy policy,
                                       AuditInfo auditInfo) {
        return new Wallet(id, tenantId, memberId, currency, balance, status, policy, auditInfo);
    }

    // ── CREDIT ────────────────────────────────────────────────────────────

    public WalletTransaction credit(BigDecimal amount, TransactionSource source,
                                     String idempotencyKey) {
        assertNotClosed();

        WalletPolicy.ValidationResult result = policy.validateCredit(amount, balance);
        if (!result.valid()) {
            throw new IllegalArgumentException("Credit validation failed: " + result.reason());
        }

        BigDecimal before = this.balance;
        this.balance = this.balance.add(amount);

        WalletTransaction tx = WalletTransaction.createCredit(
                id, tenantId, amount, currency, source, idempotencyKey, before, balance
        );
        domainEvents.add(new WalletCreditedEvent(id, tenantId, memberId, amount, balance, source));
        return tx;
    }

    public WalletTransaction creditPending(BigDecimal amount, TransactionSource source,
                                            String idempotencyKey) {
        assertNotClosed();
        WalletPolicy.ValidationResult result = policy.validateCredit(amount, balance);
        if (!result.valid()) {
            throw new IllegalArgumentException("Credit validation failed: " + result.reason());
        }
        return WalletTransaction.createPendingCredit(
                id, tenantId, amount, currency, source, idempotencyKey, balance, balance
        );
    }

    public void confirmPendingCredit(WalletTransaction pendingTx) {
        if (pendingTx.getStatus() != com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionStatus.PENDING) {
            throw new IllegalStateException("Transaction is not pending");
        }
        BigDecimal before = this.balance;
        this.balance = this.balance.add(pendingTx.getAmount());
        pendingTx.complete();
        domainEvents.add(new WalletCreditedEvent(id, tenantId, memberId, pendingTx.getAmount(), balance, pendingTx.getSource()));
    }

    // ── DEBIT ─────────────────────────────────────────────────────────────

    public WalletTransaction debit(BigDecimal amount, TransactionSource source,
                                    String idempotencyKey, BigDecimal dailyTotalDebited) {
        assertActive();

        WalletPolicy.ValidationResult result = policy.validateDebit(amount, balance, dailyTotalDebited);
        if (!result.valid()) {
            throw new IllegalArgumentException("Debit validation failed: " + result.reason());
        }

        BigDecimal before = this.balance;
        this.balance = this.balance.subtract(amount);

        WalletTransaction tx = WalletTransaction.createDebit(
                id, tenantId, amount, currency, source, idempotencyKey, before, balance
        );
        domainEvents.add(new WalletDebitedEvent(id, tenantId, memberId, amount, balance, source));
        return tx;
    }

    // ── REVERSAL ──────────────────────────────────────────────────────────

    public WalletTransaction reverse(WalletTransaction originalTx) {
        assertNotClosed();
        if (originalTx.getStatus() == TransactionStatus.REVERSED) {
            throw new IllegalStateException("Transaction is already reversed: " + originalTx.getId());
        }

        BigDecimal before = this.balance;
        // Reversal credits back a debit or debits back a credit
        if (originalTx.getType() == TransactionType.DEBIT) {
            this.balance = this.balance.add(originalTx.getAmount());
        } else if (originalTx.getType() == TransactionType.CREDIT) {
            this.balance = this.balance.subtract(originalTx.getAmount());
        }

        originalTx.reverse();

        WalletTransaction reversalTx = WalletTransaction.createReversal(
                id, tenantId, originalTx.getAmount(), currency, originalTx.getId(), before, balance
        );
        domainEvents.add(new WalletCreditedEvent(id, tenantId, memberId,
                originalTx.getAmount(), balance, TransactionSource.REVERSAL));
        return reversalTx;
    }

    // ── FREEZE / UNFREEZE ─────────────────────────────────────────────────

    public void freeze(String reason) {
        if (status == WalletStatus.CLOSED) throw new IllegalStateException("Cannot freeze a closed wallet");
        if (status == WalletStatus.FROZEN) return;
        this.status = WalletStatus.FROZEN;
        domainEvents.add(new WalletFrozenEvent(id, tenantId, memberId, reason));
    }

    public void unfreeze() {
        if (status != WalletStatus.FROZEN) throw new IllegalStateException("Wallet is not frozen");
        this.status = WalletStatus.ACTIVE;
        domainEvents.add(new WalletUnfrozenEvent(id, tenantId, memberId));
    }

    // ── CLOSE ─────────────────────────────────────────────────────────────

    public void close() {
        if (status == WalletStatus.CLOSED) return;
        if (balance.compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Cannot close wallet with non-zero balance: " + balance);
        }
        this.status = WalletStatus.CLOSED;
        domainEvents.add(new WalletClosedEvent(id, tenantId, memberId));
    }

    // ── HELPERS ───────────────────────────────────────────────────────────

    private void assertActive() {
        if (status == WalletStatus.FROZEN) throw new IllegalStateException("Wallet is frozen");
        if (status == WalletStatus.CLOSED) throw new IllegalStateException("Wallet is closed");
        if (status == WalletStatus.PENDING_KYC) throw new IllegalStateException("Wallet pending KYC");
    }

    private void assertNotClosed() {
        if (status == WalletStatus.CLOSED) throw new IllegalStateException("Wallet is closed");
    }

    public boolean isActive() { return status == WalletStatus.ACTIVE; }
    public boolean isFrozen() { return status == WalletStatus.FROZEN; }
    public boolean isClosed() { return status == WalletStatus.CLOSED; }

    public List<Object> drainEvents() {
        List<Object> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return Collections.unmodifiableList(events);
    }

    public UUID getId() { return id; }
    public TenantId getTenantId() { return tenantId; }
    public String getMemberId() { return memberId; }
    public String getCurrency() { return currency; }
    public BigDecimal getBalance() { return balance; }
    public WalletStatus getStatus() { return status; }
    public WalletPolicy getPolicy() { return policy; }
    public AuditInfo getAuditInfo() { return auditInfo; }
    public void setPolicy(WalletPolicy policy) { this.policy = policy; }
}
