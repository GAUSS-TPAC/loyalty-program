package com.yowyob.loyaulty.program.domain.loyalty;

import com.yowyob.loyaulty.program.domain.loyalty.model.BonificationReward;
import com.yowyob.loyaulty.program.domain.loyalty.model.BonificationTransaction;
import com.yowyob.loyaulty.program.domain.loyalty.model.PointsResult;
import com.yowyob.loyaulty.program.domain.loyalty.port.out.BonificationEventPort;
import com.yowyob.loyaulty.program.domain.loyalty.port.out.BonificationPort;
import com.yowyob.loyaulty.program.domain.loyalty.service.BonificationDomainService;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.shared.port.DomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

class BonificationDomainServiceTest {

    private InMemoryBonificationPort bonificationPort;
    private InMemoryEventPort eventPort;
    private BonificationDomainService service;

    private final TenantId tenantId = TenantId.of(UUID.randomUUID().toString());

    @BeforeEach
    void setUp() {
        bonificationPort = new InMemoryBonificationPort();
        eventPort = new InMemoryEventPort();
        service = new BonificationDomainService(bonificationPort, eventPort);
    }

    @Test
    void processTransaction_noReward_returnsResultAndPublishesOneEvent() {
        bonificationPort.nextResult = new PointsResult("txn-1", 500, 2500, false, null);

        StepVerifier.create(service.processTransaction(tenantId, "member-1", BigDecimal.valueOf(5000), "Achat test"))
                .assertNext(result -> {
                    assert result.transactionId().equals("txn-1");
                    assert result.pointsEarned() == 500;
                    assert !result.rewardTriggered();
                })
                .verifyComplete();

        assert eventPort.publishedEvents.size() == 1 : "Expected 1 event, got " + eventPort.publishedEvents.size();
        assert eventPort.publishedEvents.get(0).eventType().equals("bonification.transaction.recorded");
    }

    @Test
    void processTransaction_withReward_publishesTwoEvents() {
        BonificationReward reward = new BonificationReward("rwd-1", "Ticket gratuit", "Un ticket offert", "FREE_PRODUCT", BigDecimal.TEN);
        bonificationPort.nextResult = new PointsResult("txn-2", 1000, 5000, true, reward);

        StepVerifier.create(service.processTransaction(tenantId, "member-1", BigDecimal.valueOf(10000), "Achat premium"))
                .assertNext(result -> {
                    assert result.rewardTriggered();
                    assert result.hasReward();
                    assert result.triggeredReward().rewardId().equals("rwd-1");
                })
                .verifyComplete();

        assert eventPort.publishedEvents.size() == 2 : "Expected 2 events, got " + eventPort.publishedEvents.size();
        assert eventPort.publishedEvents.get(1).eventType().equals("bonification.reward.triggered");
    }

    @Test
    void processTransaction_domainServiceRunsWithoutSpring() {
        bonificationPort.nextResult = PointsResult.degraded();

        StepVerifier.create(service.processTransaction(tenantId, "member-1", BigDecimal.ONE, "Test"))
                .assertNext(result -> {
                    assert result.pointsEarned() == 0;
                    assert !result.rewardTriggered();
                })
                .verifyComplete();
    }

    // ── Fakes en mémoire — zéro Spring, zéro infrastructure ──────────────

    static class InMemoryBonificationPort implements BonificationPort {
        PointsResult nextResult = PointsResult.degraded();

        @Override
        public Mono<PointsResult> recordTransaction(TenantId tenantId, String externalUserId,
                                                     BigDecimal amount, String description) {
            return Mono.just(nextResult);
        }

        @Override
        public Flux<BonificationTransaction> getTransactionHistory(TenantId tenantId, String externalUserId) {
            return Flux.empty();
        }

        @Override
        public Mono<Void> createBeneficiary(TenantId tenantId, String externalUserId, String email, String name) {
            return Mono.empty();
        }

        @Override
        public Flux<BonificationReward> getAvailableRewards(TenantId tenantId, String externalUserId) {
            return Flux.empty();
        }
    }

    static class InMemoryEventPort implements BonificationEventPort {
        final List<DomainEvent> publishedEvents = new ArrayList<>();

        @Override
        public Mono<Void> publish(DomainEvent event) {
            publishedEvents.add(event);
            return Mono.empty();
        }
    }
}
