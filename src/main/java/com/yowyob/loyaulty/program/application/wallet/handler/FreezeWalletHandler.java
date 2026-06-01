package com.yowyob.loyaulty.program.application.wallet.handler;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.wallet.port.in.FreezeWalletUseCase;
import com.yowyob.loyaulty.program.domain.wallet.port.out.WalletEventPublisherPort;
import com.yowyob.loyaulty.program.domain.wallet.port.out.WalletRepository;
import com.yowyob.loyaulty.program.shared.exception.WalletNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class FreezeWalletHandler implements FreezeWalletUseCase {

    private final WalletRepository walletRepository;
    private final WalletEventPublisherPort eventPublisher;

    public FreezeWalletHandler(WalletRepository walletRepository,
                                WalletEventPublisherPort eventPublisher) {
        this.walletRepository = walletRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Mono<Void> freeze(TenantId tenantId, String memberId, String reason) {
        return walletRepository.findByMemberAndTenant(memberId, tenantId)
                .switchIfEmpty(Mono.error(new WalletNotFoundException(memberId)))
                .flatMap(wallet -> {
                    wallet.freeze(reason);
                    return walletRepository.save(wallet)
                            .flatMap(saved -> eventPublisher.publishAll("wallet.events", saved.drainEvents()));
                });
    }

    @Override
    public Mono<Void> unfreeze(TenantId tenantId, String memberId) {
        return walletRepository.findByMemberAndTenant(memberId, tenantId)
                .switchIfEmpty(Mono.error(new WalletNotFoundException(memberId)))
                .flatMap(wallet -> {
                    wallet.unfreeze();
                    return walletRepository.save(wallet)
                            .flatMap(saved -> eventPublisher.publishAll("wallet.events", saved.drainEvents()));
                });
    }
}
