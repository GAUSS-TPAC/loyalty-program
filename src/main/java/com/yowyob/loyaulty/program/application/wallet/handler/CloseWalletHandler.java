package com.yowyob.loyaulty.program.application.wallet.handler;

import com.yowyob.loyaulty.program.application.wallet.command.CloseWalletCommand;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.wallet.port.in.CloseWalletUseCase;
import com.yowyob.loyaulty.program.domain.wallet.port.out.WalletEventPublisherPort;
import com.yowyob.loyaulty.program.domain.wallet.port.out.WalletRepository;
import com.yowyob.loyaulty.program.shared.exception.WalletNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class CloseWalletHandler implements CloseWalletUseCase {

    private final WalletRepository walletRepository;
    private final WalletEventPublisherPort eventPublisher;

    public CloseWalletHandler(WalletRepository walletRepository,
                               WalletEventPublisherPort eventPublisher) {
        this.walletRepository = walletRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Mono<Void> close(TenantId tenantId, String memberId) {
        return walletRepository.findByMemberAndTenant(memberId, tenantId)
                .switchIfEmpty(Mono.error(new WalletNotFoundException(memberId)))
                .flatMap(wallet -> {
                    wallet.close();
                    return walletRepository.save(wallet)
                            .flatMap(saved -> eventPublisher.publishAll("wallet.events", saved.drainEvents()));
                });
    }

    public Mono<Void> handle(CloseWalletCommand cmd) {
        return close(cmd.tenantId(), cmd.memberId());
    }
}
