package com.yowyob.loyalty.domain.wallet.service;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.event.*;
import com.yowyob.loyalty.domain.wallet.exception.WalletDomainException;
import com.yowyob.loyalty.domain.wallet.model.*;
import com.yowyob.loyalty.domain.wallet.port.in.*;
import com.yowyob.loyalty.domain.wallet.port.out.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Classe Java pure, pas d'annotation Spring.
 */
public class WalletDomainService implements
    CreditWalletUseCase,
    DebitWalletUseCase,
    GetWalletUseCase,
    GetTransactionHistoryUseCase,
    FreezeWalletUseCase,
    UnfreezeWalletUseCase,
    CreateWalletUseCase,
    ReverseTransactionUseCase,
    ListWalletsUseCase {

    private final WalletRepository walletRepo;
    private final WalletTransactionRepository txRepo;
    private final WalletPolicyRepository policyRepo;
    private final WalletAuditLogRepository auditRepo;
    private final WalletEventPublisherPort eventPublisher;

    public WalletDomainService(
        WalletRepository walletRepo, 
        WalletTransactionRepository txRepo, 
        WalletPolicyRepository policyRepo, 
        WalletAuditLogRepository auditRepo, 
        WalletEventPublisherPort eventPublisher
    ) {
        this.walletRepo = walletRepo;
        this.txRepo = txRepo;
        this.policyRepo = policyRepo;
        this.auditRepo = auditRepo;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Mono<Wallet> createWallet(TenantId tenantId, UserId memberId, String currencyCode, boolean autoActivate, String idempotencyKey) {
        return walletRepo.existsByMemberAndTenant(memberId, tenantId)
            .flatMap(exists -> {
                if (exists) return Mono.error(new WalletDomainException("Un wallet existe déjà pour ce membre"));
                
                Wallet wallet = Wallet.create(UUID.randomUUID(), tenantId, memberId, currencyCode, autoActivate);
                return walletRepo.save(wallet)
                    .flatMap(savedWallet -> {
                        WalletCreatedEvent event = new WalletCreatedEvent(UUID.randomUUID(), tenantId, savedWallet.getId(), memberId, Instant.now());
                        return eventPublisher.publish(event).thenReturn(savedWallet);
                    });
            });
    }

    @Override
    public Mono<WalletCreditResult> credit(TenantId tenantId, UserId memberId, BigDecimal amount, TransactionSource source, String referenceId, String idempotencyKey) {
        return Mono.zip(policyRepo.findByTenant(tenantId), walletRepo.findByMemberAndTenant(memberId, tenantId))
            .flatMap(tuple -> {
                WalletPolicy policy = tuple.getT1();
                Wallet wallet = tuple.getT2();
                
                BigDecimal balanceBefore = wallet.getBalance();
                WalletCreditResult result = wallet.credit(amount, policy);
                
                WalletTransaction tx = WalletTransaction.create(
                    wallet.getId(), tenantId, TransactionType.CREDIT, amount, 
                    balanceBefore, wallet.getBalance(), source, idempotencyKey
                );
                
                // Note : referenceId n'est pas dans create() par défaut dans WalletTransaction record, 
                // mais le guide mentionne UUID referenceId nullable. 
                // On peut utiliser une version exhaustive si besoin.
                
                return walletRepo.save(wallet)
                    .then(txRepo.save(tx))
                    .then(eventPublisher.publish(new WalletCreditedEvent(
                        UUID.randomUUID(), tenantId, wallet.getId(), amount, wallet.getBalance(), Instant.now()
                    )))
                    .thenReturn(result);
            });
    }

    @Override
    public Mono<WalletDebitResult> debit(TenantId tenantId, UserId memberId, BigDecimal amount, String description, String orderReference, String idempotencyKey) {
        return Mono.zip(policyRepo.findByTenant(tenantId), walletRepo.findByMemberAndTenant(memberId, tenantId))
            .flatMap(tuple -> {
                WalletPolicy policy = tuple.getT1();
                Wallet wallet = tuple.getT2();
                
                return txRepo.sumDebitsTodayForWallet(wallet.getId())
                    .defaultIfEmpty(BigDecimal.ZERO)
                    .flatMap(todaySpent -> {
                        BigDecimal balanceBefore = wallet.getBalance();
                        WalletDebitResult result = wallet.debit(amount, policy, todaySpent);
                        
                        if (result.otpRequired()) {
                            return Mono.just(result);
                        }
                        
                        WalletTransaction tx = WalletTransaction.create(
                            wallet.getId(), tenantId, TransactionType.DEBIT, amount,
                            balanceBefore, wallet.getBalance(), TransactionSource.PURCHASE, idempotencyKey
                        );
                        
                        return walletRepo.save(wallet)
                            .then(txRepo.save(tx))
                            .then(eventPublisher.publish(new WalletDebitedEvent(
                                UUID.randomUUID(), tenantId, wallet.getId(), amount, wallet.getBalance(), Instant.now()
                            )))
                            .thenReturn(result);
                    });
            });
    }

    @Override
    public Mono<Wallet> freeze(TenantId tenantId, UserId memberId, String reason, String actorId) {
        return walletRepo.findByMemberAndTenant(memberId, tenantId)
            .flatMap(wallet -> {
                wallet.freeze(reason);
                return walletRepo.save(wallet)
                    .then(auditRepo.log(wallet.getId(), tenantId, "FREEZE", actorId, reason, Map.of()))
                    .then(eventPublisher.publish(new WalletFrozenEvent(
                        UUID.randomUUID(), tenantId, wallet.getId(), reason, Instant.now()
                    )))
                    .thenReturn(wallet);
            });
    }

    @Override
    public Mono<Wallet> unfreeze(TenantId tenantId, UserId memberId, String actorId) {
        return walletRepo.findByMemberAndTenant(memberId, tenantId)
            .flatMap(wallet -> {
                wallet.unfreeze();
                return walletRepo.save(wallet)
                    .then(auditRepo.log(wallet.getId(), tenantId, "UNFREEZE", actorId, "Dégel manuel", Map.of()))
                    .then(eventPublisher.publish(new WalletUnfrozenEvent(
                        UUID.randomUUID(), tenantId, wallet.getId(), Instant.now()
                    )))
                    .thenReturn(wallet);
            });
    }

    @Override
    public Mono<Wallet> getWallet(TenantId tenantId, UserId memberId) {
        return walletRepo.findByMemberAndTenant(memberId, tenantId);
    }

    @Override
    public Mono<BigDecimal> getBalance(TenantId tenantId, UserId memberId) {
        return getWallet(tenantId, memberId).map(Wallet::getBalance);
    }

    @Override
    public Flux<Wallet> listWallets(TenantId tenantId, int page, int size) {
        return walletRepo.findAllByTenant(tenantId, page, size);
    }

    @Override
    public Flux<WalletTransaction> getHistory(TenantId tenantId, UserId memberId, TransactionType typeFilter, TransactionSource sourceFilter, Instant from, Instant to, int page, int size) {
        return walletRepo.findByMemberAndTenant(memberId, tenantId)
            .flatMapMany(wallet -> txRepo.findByWalletIdAndFilters(wallet.getId(), typeFilter, sourceFilter, from, to, page, size));
    }

    @Override
    public Mono<WalletTransaction> reverse(TenantId tenantId, UUID transactionId, String reason, String actorId, String idempotencyKey) {
        return txRepo.findById(transactionId)
            .switchIfEmpty(Mono.error(new WalletDomainException("Transaction introuvable: " + transactionId)))
            .flatMap(original -> {
                if (original.reversalOf() != null || original.status() == TransactionStatus.REVERSED) {
                    return Mono.error(new WalletDomainException("Transaction déjà reversée"));
                }
                return walletRepo.findById(original.walletId())
                    .flatMap(wallet -> {
                        WalletTransaction reversal = wallet.applyReversal(original, idempotencyKey);
                        return walletRepo.save(wallet)
                            .then(txRepo.save(reversal))
                            .then(auditRepo.log(wallet.getId(), tenantId, "REVERSAL", actorId, reason, Map.of("originalTxId", transactionId.toString())))
                            .thenReturn(reversal);
                    });
            });
    }
}
