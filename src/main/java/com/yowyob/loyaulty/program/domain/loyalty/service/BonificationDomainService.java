package com.yowyob.loyaulty.program.domain.loyalty.service;

import com.yowyob.loyaulty.program.domain.loyalty.event.BonificationRewardTriggeredEvent;
import com.yowyob.loyaulty.program.domain.loyalty.event.BonificationTransactionRecordedEvent;
import com.yowyob.loyaulty.program.domain.loyalty.model.BonificationTransaction;
import com.yowyob.loyaulty.program.domain.loyalty.model.PointsResult;
import com.yowyob.loyaulty.program.domain.loyalty.port.out.BonificationEventPort;
import com.yowyob.loyaulty.program.domain.loyalty.port.out.BonificationPort;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class BonificationDomainService {

    private final BonificationPort bonificationPort;
    private final BonificationEventPort eventPort;

    public BonificationDomainService(BonificationPort bonificationPort,
                                     BonificationEventPort eventPort) {
        this.bonificationPort = bonificationPort;
        this.eventPort = eventPort;
    }

    public Mono<PointsResult> processTransaction(TenantId tenantId,
                                                  String memberId,
                                                  BigDecimal amount,
                                                  String description) {
        return bonificationPort.recordTransaction(tenantId, memberId, amount, description)
                .flatMap(result -> publishEvents(tenantId, memberId, result)
                        .thenReturn(result));
    }

    public Flux<BonificationTransaction> getTransactionHistory(TenantId tenantId, String memberId) {
        return bonificationPort.getTransactionHistory(tenantId, memberId);
    }

    private Mono<Void> publishEvents(TenantId tenantId, String memberId, PointsResult result) {
        BonificationTransactionRecordedEvent txEvent = new BonificationTransactionRecordedEvent(
                UUID.randomUUID(),
                Instant.now(),
                tenantId,
                memberId,
                result.transactionId(),
                result.pointsEarned(),
                result.rewardTriggered()
        );

        Mono<Void> publishTx = eventPort.publish(txEvent);

        if (result.hasReward()) {
            BonificationRewardTriggeredEvent rewardEvent = new BonificationRewardTriggeredEvent(
                    UUID.randomUUID(),
                    Instant.now(),
                    tenantId,
                    memberId,
                    result.triggeredReward().rewardId(),
                    result.triggeredReward().name()
            );
            return publishTx.then(eventPort.publish(rewardEvent));
        }

        return publishTx;
    }
}
