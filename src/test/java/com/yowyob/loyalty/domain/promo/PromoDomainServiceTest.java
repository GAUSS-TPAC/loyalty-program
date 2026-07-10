package com.yowyob.loyalty.domain.promo;

import com.yowyob.loyalty.domain.promo.exception.*;
import com.yowyob.loyalty.domain.promo.model.PromoCampaign;
import com.yowyob.loyalty.domain.promo.model.PromoDiscountType;
import com.yowyob.loyalty.domain.promo.model.PromoUsage;
import com.yowyob.loyalty.domain.promo.port.out.PromoCampaignRepository;
import com.yowyob.loyalty.domain.promo.port.out.PromoUsageCounterPort;
import com.yowyob.loyalty.domain.promo.port.out.PromoUsageRepository;
import com.yowyob.loyalty.domain.promo.service.PromoDomainService;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Fakes en mémoire pour les 3 ports (pattern déjà utilisé par LoyaltyDomainServiceTest) — pas de
 * Mockito, juste des Map en mémoire, pour tester le vrai ordonnancement des règles métier de
 * PromoDomainService.checkEligibility (actif -> démarré -> non expiré -> montant mini -> quota
 * global -> quota par membre) et l'idempotence par orderId.
 */
class PromoDomainServiceTest {

    private final Map<UUID, PromoCampaign> campaigns = new ConcurrentHashMap<>();
    private final Map<String, PromoUsage> usagesByOrder = new ConcurrentHashMap<>();
    private final AtomicLong globalCount = new AtomicLong();
    private final Map<UUID, Long> perMemberCount = new HashMap<>();

    private PromoDomainService service;
    private final TenantId tenantId = new TenantId(UUID.randomUUID());
    private final UserId memberId = new UserId(UUID.randomUUID());

    @BeforeEach
    void setup() {
        PromoCampaignRepository campaignRepository = new PromoCampaignRepository() {
            public Mono<PromoCampaign> save(PromoCampaign c) { campaigns.put(c.id(), c); return Mono.just(c); }
            public Mono<PromoCampaign> findById(TenantId t, UUID id) { return Mono.justOrEmpty(campaigns.get(id)); }
            public Mono<PromoCampaign> findByCode(TenantId t, String code) {
                return Flux.fromIterable(campaigns.values()).filter(c -> c.code().equals(code)).next();
            }
            public Flux<PromoCampaign> findAll(TenantId t) { return Flux.fromIterable(campaigns.values()); }
            public Flux<PromoCampaign> findActive(TenantId t) { return Flux.fromIterable(campaigns.values()).filter(PromoCampaign::isActive); }
            public Mono<Void> deleteById(TenantId t, UUID id) { campaigns.remove(id); return Mono.empty(); }
        };
        PromoUsageRepository usageRepository = new PromoUsageRepository() {
            public Mono<PromoUsage> save(PromoUsage u) { usagesByOrder.put(u.orderId(), u); return Mono.just(u); }
            public Mono<Long> countByCampaignId(TenantId t, UUID campaignId) { return Mono.just(globalCount.get()); }
            public Mono<Long> countByMemberAndCampaign(TenantId t, UUID campaignId, UserId member) {
                return Mono.just(perMemberCount.getOrDefault(campaignId, 0L));
            }
            public Mono<Boolean> existsByOrderId(TenantId t, UUID campaignId, String orderId) {
                return Mono.just(usagesByOrder.containsKey(orderId));
            }
            public Flux<PromoUsage> findByMember(TenantId t, UserId member) { return Flux.fromIterable(usagesByOrder.values()); }
        };
        PromoUsageCounterPort usageCounter = new PromoUsageCounterPort() {
            public Mono<Long> increment(TenantId t, UUID campaignId) { return Mono.just(globalCount.incrementAndGet()); }
            public Mono<Long> getCount(TenantId t, UUID campaignId) { return Mono.just(globalCount.get()); }
            public Mono<Void> reset(TenantId t, UUID campaignId) { globalCount.set(0); return Mono.empty(); }
        };
        service = new PromoDomainService(campaignRepository, usageRepository, usageCounter);
    }

    private PromoCampaign activeCampaign(int maxUses, int perMemberLimit, BigDecimal minOrderAmount) {
        PromoCampaign campaign = PromoCampaign.create(UUID.randomUUID(), tenantId, "SAVE10", "Save 10",
                PromoDiscountType.FIXED_AMOUNT, BigDecimal.TEN, minOrderAmount, maxUses, perMemberLimit,
                Instant.now().minusSeconds(60), null).activate();
        campaigns.put(campaign.id(), campaign);
        return campaign;
    }

    @Test
    void apply_happyPath_recordsUsageAndIncrementsCounter() {
        activeCampaign(0, 0, BigDecimal.ZERO);

        StepVerifier.create(service.apply(tenantId, "SAVE10", memberId, "order-1", new BigDecimal("100")))
                .assertNext(usage -> assertEquals(0, new BigDecimal("10").compareTo(usage.discountApplied())))
                .verifyComplete();

        StepVerifier.create(service.apply(tenantId, "SAVE10", memberId, "order-1", new BigDecimal("100")))
                .expectError(PromoAlreadyUsedException.class)
                .verify();
    }

    @Test
    void apply_unknownCode_throwsNotFound() {
        StepVerifier.create(service.apply(tenantId, "NOPE", memberId, "order-1", BigDecimal.TEN))
                .expectError(PromoCampaignNotFoundException.class)
                .verify();
    }

    @Test
    void apply_inactiveCampaign_throwsNotActive() {
        PromoCampaign draft = PromoCampaign.create(UUID.randomUUID(), tenantId, "DRAFT10", "Draft",
                PromoDiscountType.FIXED_AMOUNT, BigDecimal.TEN, BigDecimal.ZERO, 0, 0, Instant.now(), null);
        campaigns.put(draft.id(), draft);

        StepVerifier.create(service.apply(tenantId, "DRAFT10", memberId, "order-1", BigDecimal.TEN))
                .expectError(PromoNotActiveException.class)
                .verify();
    }

    @Test
    void apply_notYetStarted_throwsNotStarted() {
        PromoCampaign future = PromoCampaign.create(UUID.randomUUID(), tenantId, "FUTURE10", "Future",
                PromoDiscountType.FIXED_AMOUNT, BigDecimal.TEN, BigDecimal.ZERO, 0, 0,
                Instant.now().plus(1, ChronoUnit.DAYS), null).activate();
        campaigns.put(future.id(), future);

        StepVerifier.create(service.apply(tenantId, "FUTURE10", memberId, "order-1", BigDecimal.TEN))
                .expectError(PromoNotStartedException.class)
                .verify();
    }

    @Test
    void apply_expiredCampaign_throwsExpired() {
        PromoCampaign expired = PromoCampaign.create(UUID.randomUUID(), tenantId, "OLD10", "Old",
                PromoDiscountType.FIXED_AMOUNT, BigDecimal.TEN, BigDecimal.ZERO, 0, 0,
                Instant.now().minus(10, ChronoUnit.DAYS), Instant.now().minusSeconds(1)).activate();
        campaigns.put(expired.id(), expired);

        StepVerifier.create(service.apply(tenantId, "OLD10", memberId, "order-1", BigDecimal.TEN))
                .expectError(PromoExpiredException.class)
                .verify();
    }

    @Test
    void apply_belowMinOrderAmount_throwsMinOrderAmount() {
        activeCampaign(0, 0, new BigDecimal("50"));

        StepVerifier.create(service.apply(tenantId, "SAVE10", memberId, "order-1", new BigDecimal("10")))
                .expectError(PromoMinOrderAmountException.class)
                .verify();
    }

    @Test
    void apply_globalQuotaExhausted_throwsExhausted() {
        activeCampaign(1, 0, BigDecimal.ZERO);
        globalCount.set(1); // already at the limit

        StepVerifier.create(service.apply(tenantId, "SAVE10", memberId, "order-1", BigDecimal.TEN))
                .expectError(PromoExhaustedException.class)
                .verify();
    }

    @Test
    void apply_unlimitedCampaign_skipsGlobalQuotaCheckEvenWithHighCount() {
        activeCampaign(0, 0, BigDecimal.ZERO); // maxUses=0 => unlimited
        globalCount.set(999999);

        StepVerifier.create(service.apply(tenantId, "SAVE10", memberId, "order-1", BigDecimal.TEN))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void apply_perMemberLimitReached_throwsAlreadyUsed() {
        PromoCampaign campaign = activeCampaign(0, 1, BigDecimal.ZERO);
        perMemberCount.put(campaign.id(), 1L);

        StepVerifier.create(service.apply(tenantId, "SAVE10", memberId, "order-1", BigDecimal.TEN))
                .expectError(PromoAlreadyUsedException.class)
                .verify();
    }

    @Test
    void validate_eligibleCampaign_returnsValidResultWithComputedDiscount() {
        activeCampaign(0, 0, BigDecimal.ZERO);

        StepVerifier.create(service.validate(tenantId, "SAVE10", memberId, new BigDecimal("100")))
                .assertNext(result -> {
                    assertEquals(true, result.isValid());
                    assertEquals(0, new BigDecimal("10").compareTo(result.calculatedDiscount()));
                })
                .verifyComplete();
    }

    @Test
    void validate_doesNotConsumeGlobalQuota() {
        activeCampaign(1, 0, BigDecimal.ZERO);

        // Validate is read-only: repeated calls must not exhaust maxUses=1.
        StepVerifier.create(service.validate(tenantId, "SAVE10", memberId, BigDecimal.TEN)).expectNextCount(1).verifyComplete();
        StepVerifier.create(service.validate(tenantId, "SAVE10", memberId, BigDecimal.TEN)).expectNextCount(1).verifyComplete();
        assertEquals(0L, globalCount.get());
    }

    @Test
    void activateDeactivateDelete_notFound_throw404() {
        UUID randomId = UUID.randomUUID();
        StepVerifier.create(service.activate(tenantId, randomId)).expectError(PromoCampaignNotFoundException.class).verify();
        StepVerifier.create(service.deactivate(tenantId, randomId)).expectError(PromoCampaignNotFoundException.class).verify();
        StepVerifier.create(service.delete(tenantId, randomId)).expectError(PromoCampaignNotFoundException.class).verify();
    }

    @Test
    void delete_resetsUsageCounterBeforeDeleting() {
        PromoCampaign campaign = activeCampaign(0, 0, BigDecimal.ZERO);
        globalCount.set(5);

        StepVerifier.create(service.delete(tenantId, campaign.id())).verifyComplete();

        assertEquals(0L, globalCount.get());
        assertEquals(null, campaigns.get(campaign.id()));
    }
}
