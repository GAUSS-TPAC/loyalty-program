package com.yowyob.loyaulty.program.infrastructure.persistence.wallet.mapper;

import com.yowyob.loyaulty.program.domain.wallet.model.Wallet;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.WalletStatus;
import com.yowyob.loyaulty.program.infrastructure.persistence.wallet.entity.WalletEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Mapper bidirectionnel entre {@link Wallet} (domaine) et {@link WalletEntity} (infrastructure).
 *
 * <p>Centralise toute la logique de conversion pour garder le domaine
 * libre de toute annotation Spring/R2DBC.</p>
 */
@Component
public class WalletMapper {

    /**
     * Convertit une entité R2DBC en modèle domaine.
     *
     * @param entity l'entité lue depuis la base de données.
     * @return le modèle domaine {@link Wallet}.
     */
    public Wallet toDomain(WalletEntity entity) {
        if (entity == null) return null;

        return Wallet.builder()
                .id(entity.getId())
                .memberId(entity.getMemberId())
                .tenantId(entity.getTenantId())
                .availableBalance(entity.getAvailableBalance())
                .reservedBalance(nullSafeBalance(entity.getReservedBalance()))
                .expiringBalance(nullSafeBalance(entity.getExpiringBalance()))
                .status(WalletStatus.valueOf(entity.getStatus()))
                .walletPolicyId(entity.getWalletPolicyId())
                .kycValidated(entity.isKycValidated())
                .freezeReason(entity.getFreezeReason())
                .frozenAt(entity.getFrozenAt())
                .closedAt(entity.getClosedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    /**
     * Convertit un modèle domaine en entité à persister.
     *
     * @param wallet le modèle domaine.
     * @return l'entité R2DBC {@link WalletEntity}.
     */
    public WalletEntity toEntity(Wallet wallet) {
        if (wallet == null) return null;

        return WalletEntity.builder()
                .id(wallet.getId())
                .memberId(wallet.getMemberId())
                .tenantId(wallet.getTenantId())
                .availableBalance(wallet.getAvailableBalance())
                .reservedBalance(wallet.getReservedBalance())
                .expiringBalance(wallet.getExpiringBalance())
                .status(wallet.getStatus().name())
                .walletPolicyId(wallet.getWalletPolicyId())
                .kycValidated(wallet.isKycValidated())
                .freezeReason(wallet.getFreezeReason())
                .frozenAt(wallet.getFrozenAt())
                .closedAt(wallet.getClosedAt())
                .createdAt(wallet.getCreatedAt() != null ? wallet.getCreatedAt() : Instant.now())
                .updatedAt(Instant.now())
                // version null → INSERT ; non-null → UPDATE optimistic lock
                .build();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private BigDecimal nullSafeBalance(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
