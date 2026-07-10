package com.yowyob.loyalty.domain.referral;

import com.yowyob.loyalty.domain.referral.event.ReferralDomainEvent;
import com.yowyob.loyalty.domain.referral.exception.MaxReferralsExceededException;
import com.yowyob.loyalty.domain.referral.exception.ReferralLinkNotFoundException;
import com.yowyob.loyalty.domain.referral.exception.SelfReferralException;
import com.yowyob.loyalty.domain.referral.model.ReferralEvent;
import com.yowyob.loyalty.domain.referral.model.ReferralLink;
import com.yowyob.loyalty.domain.referral.model.ReferralProgram;
import com.yowyob.loyalty.domain.referral.model.ReferralStatus;
import com.yowyob.loyalty.domain.referral.port.out.ReferralEventPublisherPort;
import com.yowyob.loyalty.domain.referral.port.out.ReferralEventRepository;
import com.yowyob.loyalty.domain.referral.port.out.ReferralLinkRepository;
import com.yowyob.loyalty.domain.referral.port.out.ReferralProgramRepository;
import com.yowyob.loyalty.domain.referral.service.ReferralDomainService;
import com.yowyob.loyalty.domain.reward.model.GrantSource;
import com.yowyob.loyalty.domain.reward.model.RewardGrant;
import com.yowyob.loyalty.domain.reward.port.in.GrantRewardUseCase;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ReferralDomainServiceTest {

    private final Map<UUID, ReferralProgram> programs = new ConcurrentHashMap<>();
    private final Map<String, ReferralLink> linksByCode = new ConcurrentHashMap<>();
    private final Map<UUID, ReferralLink> linksByReferrer = new ConcurrentHashMap<>();
    private final Map<UUID, ReferralEvent> events = new ConcurrentHashMap<>();
    private final List<ReferralDomainEvent> published = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final List<UUID> grantedRewardIds = new java.util.concurrent.CopyOnWriteArrayList<>();
    private final AtomicInteger grantCalls = new AtomicInteger();

    private ReferralDomainService service;
    private final TenantId tenantId = new TenantId(UUID.randomUUID());

    @BeforeEach
    void setup() {
        ReferralProgramRepository programRepository = new ReferralProgramRepository() {
            public Mono<ReferralProgram> save(ReferralProgram p) { programs.put(p.id(), p); return Mono.just(p); }
            public Mono<ReferralProgram> findById(TenantId t, UUID id) { return Mono.justOrEmpty(programs.get(id)); }
            public Mono<ReferralProgram> findActiveByTenantId(TenantId t) {
                return Flux.fromIterable(programs.values()).filter(ReferralProgram::isActive).next();
            }
        };
        ReferralLinkRepository linkRepository = new ReferralLinkRepository() {
            public Mono<ReferralLink> save(ReferralLink l) {
                linksByCode.put(l.code(), l);
                linksByReferrer.put(l.referrerId().value(), l);
                return Mono.just(l);
            }
            public Mono<ReferralLink> findByCode(TenantId t, String code) { return Mono.justOrEmpty(linksByCode.get(code)); }
            public Mono<ReferralLink> findByReferrerId(TenantId t, UserId referrerId) { return Mono.justOrEmpty(linksByReferrer.get(referrerId.value())); }
        };
        ReferralEventRepository eventRepository = new ReferralEventRepository() {
            public Mono<ReferralEvent> save(ReferralEvent e) { events.put(e.id(), e); return Mono.just(e); }
            public Mono<ReferralEvent> findById(TenantId t, UUID id) { return Mono.justOrEmpty(events.get(id)); }
            public Mono<ReferralEvent> findPendingByRefereeId(TenantId t, UserId refereeId) {
                return Flux.fromIterable(events.values())
                        .filter(e -> e.refereeId().equals(refereeId) && e.status() == ReferralStatus.ENROLLED)
                        .next();
            }
            public Flux<ReferralEvent> findByReferrerId(TenantId t, UserId referrerId) {
                return Flux.fromIterable(events.values()).filter(e -> e.referrerId().equals(referrerId));
            }
            public Mono<Long> countByReferrerIdAndStatus(TenantId t, UserId referrerId, ReferralStatus status) {
                return Flux.fromIterable(events.values())
                        .filter(e -> e.referrerId().equals(referrerId) && e.status() == status)
                        .count();
            }
        };
        ReferralEventPublisherPort publisher = event -> { published.add(event); return Mono.empty(); };
        GrantRewardUseCase grantRewardUseCase = (t, memberId, rewardId, source, sourceRuleId, sourceEventKey, idempotencyKey) -> {
            grantCalls.incrementAndGet();
            grantedRewardIds.add(rewardId);
            return Mono.empty(); // stubbed grant, we only assert call presence/absence
        };

        service = new ReferralDomainService(programRepository, linkRepository, eventRepository, publisher, grantRewardUseCase);
    }

    private ReferralProgram activeProgram(int maxReferrals, int minConversionAmount, UUID referrerRewardId, UUID refereeRewardId) {
        ReferralProgram program = ReferralProgram.create(UUID.randomUUID(), tenantId, "Program",
                maxReferrals, 30, referrerRewardId, refereeRewardId, minConversionAmount).activate();
        programs.put(program.id(), program);
        return program;
    }

    @Test
    void createLink_isIdempotent_returnsExistingLinkForSameReferrer() {
        activeProgram(5, 0, null, null);
        UserId referrer = new UserId(UUID.randomUUID());

        ReferralLink first = service.createLink(tenantId, referrer).block();
        ReferralLink second = service.createLink(tenantId, referrer).block();

        assertEquals(first.id(), second.id(), "a referrer that already has a link must not get a new one");
        assertEquals(1, published.stream().filter(e -> e.getClass().getSimpleName().equals("ReferralLinkCreatedEvent")).count());
    }

    @Test
    void register_selfReferral_throws() {
        activeProgram(5, 0, null, null);
        UserId referrer = new UserId(UUID.randomUUID());
        ReferralLink link = service.createLink(tenantId, referrer).block();

        StepVerifier.create(service.register(tenantId, link.code(), referrer))
                .expectError(SelfReferralException.class)
                .verify();
    }

    @Test
    void register_unknownCode_throwsNotFound() {
        StepVerifier.create(service.register(tenantId, "NOPE0000", new UserId(UUID.randomUUID())))
                .expectError(ReferralLinkNotFoundException.class)
                .verify();
    }

    @Test
    void register_inactiveLink_treatedAsNotFound() {
        activeProgram(5, 0, null, null);
        UserId referrer = new UserId(UUID.randomUUID());
        ReferralLink link = service.createLink(tenantId, referrer).block();
        // Deactivate the link by reconstructing it inactive under the same code.
        ReferralLink inactive = ReferralLink.reconstruct(link.id(), tenantId, referrer, link.code(),
                link.createdAt(), link.expiresAt(), link.usageCount(), link.conversionCount(), false);
        linksByCode.put(inactive.code(), inactive);

        StepVerifier.create(service.register(tenantId, link.code(), new UserId(UUID.randomUUID())))
                .expectError(ReferralLinkNotFoundException.class)
                .verify();
    }

    @Test
    void register_maxReferralsCountsOnlyConvertedNotAllReferrals() {
        activeProgram(1, 0, null, null);
        UserId referrer = new UserId(UUID.randomUUID());
        ReferralLink link = service.createLink(tenantId, referrer).block();

        // Register several referees: none are CONVERTED yet, so the (converted-only) count stays 0
        // and the max-referrals=1 cap should NOT trigger regardless of how many are enrolled.
        for (int i = 0; i < 3; i++) {
            UserId referee = new UserId(UUID.randomUUID());
            StepVerifier.create(service.register(tenantId, link.code(), referee))
                    .assertNext(event -> assertEquals(ReferralStatus.ENROLLED, event.status()))
                    .verifyComplete();
        }
    }

    @Test
    void register_maxReferralsExceeded_onceConvertedCountReachesLimit() {
        activeProgram(1, 0, null, null);
        UserId referrer = new UserId(UUID.randomUUID());
        ReferralLink link = service.createLink(tenantId, referrer).block();

        // Seed one already-CONVERTED referral event for this referrer.
        ReferralEvent converted = ReferralEvent.create(UUID.randomUUID(), tenantId, link.id(), referrer, new UserId(UUID.randomUUID()))
                .convert(BigDecimal.TEN);
        events.put(converted.id(), converted);

        StepVerifier.create(service.register(tenantId, link.code(), new UserId(UUID.randomUUID())))
                .expectError(MaxReferralsExceededException.class)
                .verify();
    }

    @Test
    void convert_belowMinConversionAmount_marksFraudInsteadOfThrowing() {
        activeProgram(5, 100, null, null);
        UserId referrer = new UserId(UUID.randomUUID());
        UserId referee = new UserId(UUID.randomUUID());
        ReferralLink link = service.createLink(tenantId, referrer).block();
        service.register(tenantId, link.code(), referee).block();

        StepVerifier.create(service.convert(tenantId, referee, new BigDecimal("10")))
                .assertNext(event -> assertEquals(ReferralStatus.FRAUD, event.status()))
                .verifyComplete();

        assertEquals(0, grantCalls.get(), "a fraud-flagged conversion must not grant any reward");
    }

    @Test
    void convert_success_grantsRewardsOnlyWhenConfigured() {
        UUID referrerRewardId = UUID.randomUUID();
        activeProgram(5, 0, referrerRewardId, null); // no referee reward configured
        UserId referrer = new UserId(UUID.randomUUID());
        UserId referee = new UserId(UUID.randomUUID());
        ReferralLink link = service.createLink(tenantId, referrer).block();
        service.register(tenantId, link.code(), referee).block();

        StepVerifier.create(service.convert(tenantId, referee, new BigDecimal("50")))
                .assertNext(event -> assertEquals(ReferralStatus.CONVERTED, event.status()))
                .verifyComplete();

        assertEquals(1, grantCalls.get(), "only the referrer reward should be granted (referee reward not configured)");
        assertEquals(List.of(referrerRewardId), grantedRewardIds);
    }

    @Test
    void convert_noPendingEvent_throwsGenericDomainException() {
        activeProgram(5, 0, null, null);
        StepVerifier.create(service.convert(tenantId, new UserId(UUID.randomUUID()), BigDecimal.TEN))
                .expectError(com.yowyob.loyalty.domain.referral.exception.ReferralDomainException.class)
                .verify();
    }

    @Test
    void createProgram_ignoresRewardAmountsAndDates_hardcodesWindowAndNullRewards() {
        // Documented quirk: createProgram's referrerReward/refereeReward/startDate/endDate params
        // are accepted but never used -- referralWindowDays is hardcoded to 30 and reward ids are
        // always null, regardless of what's passed in.
        ReferralProgram program = service.createProgram(tenantId, "Name",
                new BigDecimal("999"), new BigDecimal("999"), 7,
                LocalDate.now(), LocalDate.now().plusDays(10)).block();

        assertEquals(30, program.referralWindowDays());
        assertNull(program.referrerRewardId());
        assertNull(program.refereeRewardId());
        assertEquals(7, program.maxReferralsPerReferrer());
    }
}
