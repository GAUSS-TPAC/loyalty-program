package com.yowyob.loyaulty.program.application.wallet.handler;

import com.yowyob.loyaulty.program.application.wallet.command.DebitWalletCommand;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.wallet.model.WalletTransaction;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionSource;
import com.yowyob.loyaulty.program.domain.wallet.port.in.DebitWalletUseCase;
import com.yowyob.loyaulty.program.domain.wallet.port.out.*;
import com.yowyob.loyaulty.program.shared.exception.WalletNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Service
public class DebitWalletHandler implements DebitWalletUseCase {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final WalletEventPublisherPort eventPublisher;
    private final IdempotencyPort idempotency;

    public DebitWalletHandler(WalletRepository walletRepository,
                               WalletTransactionRepository transactionRepository,
                               WalletEventPublisherPort eventPublisher,
                               IdempotencyPort idempotency) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
        this.idempotency = idempotency;
    }

    @Override
    public Mono<WalletTransaction> debit(TenantId tenantId, String memberId,
                                          BigDecimal amount, TransactionSource source,
                                          String idempotencyKey) {
        return idempotency.isNew(idempotencyKey)
                .flatMap(isNew -> {
                    if (!isNew) {
                        return transactionRepository.findByIdempotencyKey(idempotencyKey);
                    }
                    return idempotency.markProcessing(idempotencyKey)
                            .then(processDebit(tenantId, memberId, amount, source, idempotencyKey));
                });
    }

    public Mono<WalletTransaction> handle(DebitWalletCommand cmd) {
        return debit(cmd.tenantId(), cmd.memberId(), cmd.amount(), cmd.source(), cmd.idempotencyKey());
    }

    private Mono<WalletTransaction> processDebit(TenantId tenantId, String memberId,
                                                   BigDecimal amount, TransactionSource source,
                                                   String idempotencyKey) {
        Instant dayStart = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);

        return walletRepository.findByMemberAndTenant(memberId, tenantId)
                .switchIfEmpty(Mono.error(new WalletNotFoundException(memberId + "@" + tenantId)))
                .flatMap(wallet ->
                        transactionRepository.sumDebitedToday(wallet.getId(), dayStart)
                                .defaultIfEmpty(BigDecimal.ZERO)
                                .flatMap(dailyTotal -> {
                                    WalletTransaction tx = wallet.debit(amount, source, idempotencyKey, dailyTotal);
                                    return walletRepository.save(wallet)
                                            .then(transactionRepository.save(tx))
                                            .flatMap(savedTx -> {
                                                var events = wallet.drainEvents();
                                                return eventPublisher.publishAll("wallet.events", events)
                                                        .thenReturn(savedTx);
                                            });
                                })
                );
    }
}
