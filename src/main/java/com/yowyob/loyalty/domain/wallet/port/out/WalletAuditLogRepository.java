package com.yowyob.loyalty.domain.wallet.port.out;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import reactor.core.publisher.Mono;
import java.util.Map;
import java.util.UUID;

public interface WalletAuditLogRepository {
    Mono<Void> log(UUID walletId, TenantId tenantId, String action, String actor, String reason, Map<String, Object> metadata);
}
