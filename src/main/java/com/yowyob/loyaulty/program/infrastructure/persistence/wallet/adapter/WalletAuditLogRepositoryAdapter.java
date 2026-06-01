package com.yowyob.loyaulty.program.infrastructure.persistence.wallet.adapter;

import com.yowyob.loyaulty.program.domain.wallet.model.WalletAuditLog;
import com.yowyob.loyaulty.program.domain.wallet.port.out.WalletAuditLogRepository;
import com.yowyob.loyaulty.program.infrastructure.persistence.wallet.entity.WalletAuditLogEntity;
import com.yowyob.loyaulty.program.infrastructure.persistence.wallet.repository.WalletAuditLogR2dbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Adaptateur infrastructure implémentant {@link WalletAuditLogRepository}.
 *
 * <p>Inclut la logique de mapping inline (simple record → entity)
 * car WalletAuditLog n'a pas de mapper dédié dans la liste des fichiers
 * cibles (les champs sont 1:1 entre domaine et entité).</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WalletAuditLogRepositoryAdapter implements WalletAuditLogRepository {

    private final WalletAuditLogR2dbcRepository r2dbcRepository;

    @Override
    public Mono<WalletAuditLog> save(WalletAuditLog auditLog) {
        return r2dbcRepository.save(toEntity(auditLog))
                .map(this::toDomain)
                .doOnNext(log -> WalletAuditLogRepositoryAdapter.log
                        .debug("AuditLog créé : walletId={}, action={}",
                                log.getWalletId(), log.getAction()));
    }

    @Override
    public Flux<WalletAuditLog> findByWalletId(UUID walletId, UUID tenantId,
                                                Instant dateFrom, Instant dateTo,
                                                long offset, int limit) {
        return r2dbcRepository.findByWalletId(walletId, tenantId, dateFrom, dateTo,
                        limit, offset)
                .map(this::toDomain);
    }

    @Override
    public Mono<Long> countByWalletId(UUID walletId, UUID tenantId) {
        return r2dbcRepository.countByWalletId(walletId, tenantId);
    }

    // ── Mapping inline (1:1) ─────────────────────────────────────────────────

    private WalletAuditLogEntity toEntity(WalletAuditLog domain) {
        return WalletAuditLogEntity.builder()
                .id(domain.getId())
                .walletId(domain.getWalletId())
                .tenantId(domain.getTenantId())
                .actorId(domain.getActorId())
                .actorType(domain.getActorType())
                .action(domain.getAction())
                .reason(domain.getReason())
                .previousStatus(domain.getPreviousStatus())
                .newStatus(domain.getNewStatus())
                .relatedTransactionId(domain.getRelatedTransactionId())
                .metadata(domain.getMetadata())
                .ipAddress(domain.getIpAddress())
                .userAgent(domain.getUserAgent())
                .occurredAt(domain.getOccurredAt() != null ? domain.getOccurredAt() : Instant.now())
                .build();
    }

    private WalletAuditLog toDomain(WalletAuditLogEntity entity) {
        return WalletAuditLog.builder()
                .id(entity.getId())
                .walletId(entity.getWalletId())
                .tenantId(entity.getTenantId())
                .actorId(entity.getActorId())
                .actorType(entity.getActorType())
                .action(entity.getAction())
                .reason(entity.getReason())
                .previousStatus(entity.getPreviousStatus())
                .newStatus(entity.getNewStatus())
                .relatedTransactionId(entity.getRelatedTransactionId())
                .metadata(entity.getMetadata())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .occurredAt(entity.getOccurredAt())
                .build();
    }
}
