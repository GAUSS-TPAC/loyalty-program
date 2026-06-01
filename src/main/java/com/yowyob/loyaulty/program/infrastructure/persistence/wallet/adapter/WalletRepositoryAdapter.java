package com.yowyob.loyaulty.program.infrastructure.persistence.wallet.adapter;

import com.yowyob.loyaulty.program.domain.wallet.model.Wallet;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.WalletStatus;
import com.yowyob.loyaulty.program.domain.wallet.port.out.WalletRepository;
import com.yowyob.loyaulty.program.infrastructure.persistence.wallet.mapper.WalletMapper;
import com.yowyob.loyaulty.program.infrastructure.persistence.wallet.repository.WalletR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Adaptateur infrastructure implémentant le port de sortie {@link WalletRepository}.
 *
 * <p>Délègue à {@link WalletR2dbcRepository} (Spring Data R2DBC) et utilise
 * {@link WalletMapper} pour convertir entre modèle domaine et entité.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WalletRepositoryAdapter implements WalletRepository {

    private final WalletR2dbcRepository r2dbcRepository;
    private final WalletMapper mapper;

    @Override
    public Mono<Wallet> findById(UUID walletId, UUID tenantId) {
        return r2dbcRepository.findByIdAndTenantId(walletId, tenantId)
                .map(mapper::toDomain)
                .doOnNext(w -> log.debug("Wallet trouvé : id={}, tenant={}", walletId, tenantId))
                .doOnEmpty(() -> log.debug("Wallet introuvable : id={}, tenant={}", walletId, tenantId));
    }

    @Override
    public Mono<Wallet> findByMemberId(UUID memberId, UUID tenantId) {
        return r2dbcRepository.findByMemberIdAndTenantId(memberId, tenantId)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Wallet> save(Wallet wallet) {
        return r2dbcRepository.save(mapper.toEntity(wallet))
                .map(mapper::toDomain)
                .doOnNext(w -> log.debug("Wallet sauvegardé : id={}", w.getId()));
    }

    @Override
    public Mono<Wallet> updateStatus(UUID walletId, UUID tenantId,
                                      WalletStatus newStatus, String freezeReason) {
        return r2dbcRepository
                .updateStatus(walletId, tenantId, newStatus.name(), freezeReason)
                .flatMap(rowsAffected -> {
                    if (rowsAffected == 0) {
                        return Mono.error(new IllegalStateException(
                                "Wallet introuvable ou non mis à jour : id=" + walletId));
                    }
                    return findById(walletId, tenantId);
                })
                .doOnNext(w -> log.info("Statut wallet mis à jour : id={}, statut={}",
                        walletId, newStatus));
    }

    @Override
    public Mono<Boolean> existsByMemberId(UUID memberId, UUID tenantId) {
        return r2dbcRepository.existsByMemberIdAndTenantId(memberId, tenantId);
    }
}
