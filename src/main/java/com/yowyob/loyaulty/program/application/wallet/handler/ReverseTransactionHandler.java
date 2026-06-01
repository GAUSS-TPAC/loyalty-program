package com.yowyob.loyaulty.program.application.wallet.handler;

import com.yowyob.loyaulty.program.application.wallet.command.ReverseTransactionCommand;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.wallet.model.WalletTransaction;
import com.yowyob.loyaulty.program.domain.wallet.port.in.ReverseTransactionUseCase;
import com.yowyob.loyaulty.program.domain.wallet.port.out.WalletEventPublisherPort;
import com.yowyob.loyaulty.program.domain.wallet.port.out.WalletRepository;
import com.yowyob.loyaulty.program.domain.wallet.port.out.WalletTransactionRepository;
import com.yowyob.loyaulty.program.shared.exception.WalletNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class ReverseTransactionHandler implements ReverseTransactionUseCase {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;
    private final WalletEventPublisherPort eventPublisher;

    public ReverseTransactionHandler(WalletRepository walletRepository,
                                      WalletTransactionRepository transactionRepository,
                                      WalletEventPublisherPort eventPublisher) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Mono<WalletTransaction> reverse(TenantId tenantId, String memberId,
                                            UUID transactionId, String idempotencyKey) {
        return walletRepository.findByMemberAndTenant(memberId, tenantId)
                .switchIfEmpty(Mono.error(new WalletNotFoundException(memberId)))
                .flatMap(wallet -> transactionRepository.findById(transactionId)
                        .switchIfEmpty(Mono.error(new IllegalArgumentException("Transaction not found: " + transactionId)))
                        .flatMap(originalTx -> {
                            WalletTransaction reversalTx = wallet.reverse(originalTx);
                            return walletRepository.save(wallet)
                                    .then(transactionRepository.save(reversalTx))
                                    .then(transactionRepository.save(originalTx))
                                    .thenReturn(reversalTx)
                                    .flatMap(saved -> {
                                        var events = wallet.drainEvents();
                                        return eventPublisher.publishAll("wallet.events", events)
                                                .thenReturn(saved);
                                    });
                        }));
    }

    public Mono<WalletTransaction> handle(ReverseTransactionCommand cmd) {
        return reverse(cmd.tenantId(), cmd.memberId(), cmd.transactionId(), cmd.idempotencyKey());
    }
}
