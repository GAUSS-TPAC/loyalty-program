package com.yowyob.loyalty.infrastructure.persistence.wallet.repository;

import com.yowyob.loyalty.infrastructure.persistence.wallet.entity.WalletAuditLogEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import java.util.UUID;

public interface WalletAuditLogR2dbcRepository extends ReactiveCrudRepository<WalletAuditLogEntity, UUID> {
}
