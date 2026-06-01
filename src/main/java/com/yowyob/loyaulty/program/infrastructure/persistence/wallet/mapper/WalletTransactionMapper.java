package com.yowyob.loyaulty.program.infrastructure.persistence.wallet.mapper;

import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.wallet.model.WalletTransaction;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionSource;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionStatus;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionType;
import com.yowyob.loyaulty.program.infrastructure.persistence.wallet.entity.WalletTransactionEntity;
import org.springframework.stereotype.Component;

@Component
public class WalletTransactionMapper {

    public WalletTransaction toDomain(WalletTransactionEntity e) {
        return WalletTransaction.reconstitute(
                e.getId(),
                e.getWalletId(),
                TenantId.of(e.getTenantId()),
                TransactionType.valueOf(e.getType()),
                e.getAmount(),
                e.getCurrency(),
                TransactionStatus.valueOf(e.getStatus()),
                e.getSource() != null ? TransactionSource.valueOf(e.getSource()) : null,
                e.getIdempotencyKey(),
                e.getBalanceBefore(),
                e.getBalanceAfter(),
                e.getRelatedTransactionId(),
                e.getMetadata(),
                e.getCreatedAt()
        );
    }

    public WalletTransactionEntity toEntity(WalletTransaction tx) {
        WalletTransactionEntity e = new WalletTransactionEntity();
        e.setId(tx.getId());
        e.setWalletId(tx.getWalletId());
        e.setTenantId(tx.getTenantId().value());
        e.setType(tx.getType().name());
        e.setAmount(tx.getAmount());
        e.setCurrency(tx.getCurrency());
        e.setStatus(tx.getStatus().name());
        e.setSource(tx.getSource() != null ? tx.getSource().name() : null);
        e.setIdempotencyKey(tx.getIdempotencyKey());
        e.setBalanceBefore(tx.getBalanceBefore());
        e.setBalanceAfter(tx.getBalanceAfter());
        e.setRelatedTransactionId(tx.getRelatedTransactionId());
        e.setMetadata(tx.getMetadata());
        e.setCreatedAt(tx.getCreatedAt());
        return e;
    }
}
