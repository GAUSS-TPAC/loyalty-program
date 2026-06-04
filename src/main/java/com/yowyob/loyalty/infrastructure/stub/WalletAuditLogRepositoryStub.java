package com.yowyob.loyalty.infrastructure.stub;

import com.yowyob.loyalty.domain.wallet.port.out.WalletAuditLogRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Component
@Profile({"test", "stub"})
public class WalletAuditLogRepositoryStub implements WalletAuditLogRepository {

    @Override
    public Mono<Void> log(UUID walletId, String action, String actor, String reason, Map<String, Object> metadata) {
        return Mono.empty();
    }
}
