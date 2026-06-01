package com.yowyob.loyaulty.program.application.wallet.handler;

import com.yowyob.loyaulty.program.application.wallet.command.CreditWalletCommand;
import com.yowyob.loyaulty.program.domain.wallet.model.Wallet;
import com.yowyob.loyaulty.program.domain.wallet.model.WalletPolicy;
import com.yowyob.loyaulty.program.domain.wallet.model.WalletTransaction;
import com.yowyob.loyaulty.program.domain.wallet.port.in.CreditWalletUseCase;
import com.yowyob.loyaulty.program.domain.wallet.port.out.*;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionSource;
import com.yowyob.loyaulty.program.shared.exception.WalletNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
public class CreditWalletHandler implements CreditWalletUseCase {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final WalletEventPublisherPort eventPublisher;
    private final IdempotencyPort idempotency;

    public CreditWalletHandler(WalletRepository walletRepository,
                                WalletTransactionRepository transactionRepository,
                                WalletEventPublisherPort eventPublisher,
                                IdempotencyPort idempotency) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
        this.idempotency = idempotency;
    }

    @Override
    public Mono<WalletTransaction> credit(TenantId tenantId, String memberId,
                                           BigDecimal amount, TransactionSource source,
                                           String idempotencyKey) {
        return idempotency.isNew(idempotencyKey)
                .flatMap(isNew -> {
                    if (!isNew) {
                        return transactionRepository.findByIdempotencyKey(idempotencyKey);
                    }
                    return idempotency.markProcessing(idempotencyKey)
                            .then(processCredit(tenantId, memberId, amount, source, idempotencyKey));
                });
    }

    public Mono<WalletTransaction> handle(CreditWalletCommand cmd) {
        return credit(cmd.tenantId(), cmd.memberId(), cmd.amount(), cmd.source(), cmd.idempotencyKey());
    }

    private Mono<WalletTransaction> processCredit(TenantId tenantId, String memberId,
                                                    BigDecimal amount, TransactionSource source,
                                                    String idempotencyKey) {
        return walletRepository.findByMemberAndTenant(memberId, tenantId)
                .switchIfEmpty(Mono.error(new WalletNotFoundException(memberId + "@" + tenantId)))
                .flatMap(wallet -> {
                    WalletTransaction tx = wallet.credit(amount, source, idempotencyKey);
                    return walletRepository.save(wallet)
                            .then(transactionRepository.save(tx))
                            .flatMap(savedTx -> {
                                var events = wallet.drainEvents();
                                return eventPublisher.publishAll("wallet.events", events)
                                        .thenReturn(savedTx);
                            });
                });
    }
}
