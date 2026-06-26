package com.yowyob.loyalty.domain.referral.service;

import com.yowyob.loyalty.domain.referral.event.*;
import com.yowyob.loyalty.domain.referral.exception.*;
import com.yowyob.loyalty.domain.referral.model.*;
import com.yowyob.loyalty.domain.referral.port.in.*;
import com.yowyob.loyalty.domain.referral.port.out.*;
import com.yowyob.loyalty.domain.reward.model.GrantSource;
import com.yowyob.loyalty.domain.reward.port.in.GrantRewardUseCase;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class ReferralDomainService implements
        CreateReferralLinkUseCase,
        RegisterReferralUseCase,
        ConvertReferralUseCase,
        GetReferralStatsUseCase,
        ManageReferralProgramUseCase {

    private final ReferralProgramRepository programRepository;
    private final ReferralLinkRepository linkRepository;
    private final ReferralEventRepository eventRepository;
    private final ReferralEventPublisherPort publisher;
    private final GrantRewardUseCase grantRewardUseCase;

    public ReferralDomainService(ReferralProgramRepository programRepository,
                                  ReferralLinkRepository linkRepository,
                                  ReferralEventRepository eventRepository,
                                  ReferralEventPublisherPort publisher,
                                  GrantRewardUseCase grantRewardUseCase) {
        this.programRepository = programRepository;
        this.linkRepository = linkRepository;
        this.eventRepository = eventRepository;
        this.publisher = publisher;
        this.grantRewardUseCase = grantRewardUseCase;
    }

    @Override
    public Mono<ReferralLink> createLink(TenantId tenantId, UserId referrerId) {
        return linkRepository.findByReferrerId(tenantId, referrerId)
                .switchIfEmpty(
                        programRepository.findActiveByTenantId(tenantId)
                                .flatMap(program -> {
                                    ReferralLink link = ReferralLink.create(
                                            UUID.randomUUID(), tenantId, referrerId,
                                            program.referralWindowDays());
                                    return linkRepository.save(link)
                                            .flatMap(saved -> publisher.publish(
                                                    new ReferralLinkCreatedEvent(
                                                            UUID.randomUUID(), Instant.now(), tenantId,
                                                            saved.id(), referrerId, saved.code()))
                                                    .thenReturn(saved));
                                })
                );
    }

    @Override
    public Mono<ReferralEvent> register(TenantId tenantId, String referralCode, UserId refereeId) {
        return linkRepository.findByCode(tenantId, referralCode)
                .switchIfEmpty(Mono.error(new ReferralLinkNotFoundException(referralCode)))
                .flatMap(link -> {
                    if (!link.isValid()) {
                        return Mono.error(new ReferralLinkNotFoundException(referralCode));
                    }
                    if (link.referrerId().equals(refereeId)) {
                        return Mono.error(new SelfReferralException());
                    }
                    return programRepository.findActiveByTenantId(tenantId)
                            .flatMap(program -> eventRepository.countByReferrerIdAndStatus(
                                            tenantId, link.referrerId(), ReferralStatus.CONVERTED)
                                    .flatMap(count -> {
                                        if (count >= program.maxReferralsPerReferrer()) {
                                            return Mono.error(new MaxReferralsExceededException(
                                                    program.maxReferralsPerReferrer()));
                                        }
                                        ReferralEvent event = ReferralEvent.create(
                                                UUID.randomUUID(), tenantId, link.id(),
                                                link.referrerId(), refereeId);
                                        event.enroll();
                                        link.incrementUsage();
                                        return linkRepository.save(link)
                                                .then(eventRepository.save(event))
                                                .flatMap(saved -> publisher.publish(
                                                        new ReferralEnrolledEvent(
                                                                UUID.randomUUID(), Instant.now(), tenantId,
                                                                saved.id(), saved.referrerId(), refereeId))
                                                        .thenReturn(saved));
                                    }));
                });
    }

    @Override
    public Mono<ReferralEvent> convert(TenantId tenantId, UserId refereeId, BigDecimal conversionAmount) {
        return eventRepository.findPendingByRefereeId(tenantId, refereeId)
                .switchIfEmpty(Mono.error(new ReferralDomainException("Aucun parrainage en attente pour ce membre")))
                .flatMap(event -> {
                    if (event.status().isFinal()) {
                        return Mono.error(new ReferralAlreadyConvertedException(event.id()));
                    }
                    return programRepository.findActiveByTenantId(tenantId)
                            .flatMap(program -> {
                                if (conversionAmount.compareTo(BigDecimal.valueOf(program.minConversionAmount())) < 0) {
                                    event.markFraud("Montant de conversion insuffisant: " + conversionAmount);
                                    return eventRepository.save(event)
                                            .flatMap(saved -> publisher.publish(
                                                    new ReferralFraudDetectedEvent(
                                                            UUID.randomUUID(), Instant.now(), tenantId,
                                                            saved.id(), refereeId, saved.fraudReason()))
                                                    .thenReturn(saved));
                                }
                                event.convert(conversionAmount);
                                return eventRepository.save(event)
                                        .flatMap(saved -> {
                                            String idempotencyKey = "referral-referrer-" + saved.id();
                                            String idempotencyKeyReferee = "referral-referee-" + saved.id();
                                            Mono<Void> grantReferrer = program.referrerRewardId() != null
                                                    ? grantRewardUseCase.grantReward(tenantId, saved.referrerId(),
                                                    program.referrerRewardId(), GrantSource.REFERRAL_BONUS,
                                                    null, "referral:" + saved.id(), idempotencyKey).then()
                                                    : Mono.empty();
                                            Mono<Void> grantReferee = program.refereeRewardId() != null
                                                    ? grantRewardUseCase.grantReward(tenantId, refereeId,
                                                    program.refereeRewardId(), GrantSource.REFERRAL_BONUS,
                                                    null, "referral:" + saved.id(), idempotencyKeyReferee).then()
                                                    : Mono.empty();
                                            return linkRepository.findByReferrerId(tenantId, saved.referrerId())
                                                    .flatMap(link -> {
                                                        link.incrementConversion();
                                                        return linkRepository.save(link);
                                                    })
                                                    .then(grantReferrer)
                                                    .then(grantReferee)
                                                    .then(publisher.publish(
                                                            new ReferralConvertedEvent(
                                                                    UUID.randomUUID(), Instant.now(), tenantId,
                                                                    saved.id(), saved.referrerId(), refereeId,
                                                                    conversionAmount)))
                                                    .thenReturn(saved);
                                        });
                            });
                });
    }

    @Override
    public Mono<ReferralLink> getMyLink(TenantId tenantId, UserId referrerId) {
        return linkRepository.findByReferrerId(tenantId, referrerId)
                .switchIfEmpty(Mono.error(new ReferralLinkNotFoundException("no-link")));
    }

    @Override
    public Flux<ReferralEvent> getMyReferrals(TenantId tenantId, UserId referrerId) {
        return eventRepository.findByReferrerId(tenantId, referrerId);
    }

    @Override
    public Mono<ReferralProgram> createProgram(TenantId tenantId, String name, BigDecimal referrerReward,
                                                BigDecimal refereeReward, int maxReferrals,
                                                LocalDate startDate, LocalDate endDate) {
        ReferralProgram program = ReferralProgram.create(
                UUID.randomUUID(), tenantId, name, maxReferrals,
                30, null, null, 0);
        return programRepository.save(program);
    }

    @Override
    public Mono<ReferralProgram> activateProgram(TenantId tenantId, UUID programId) {
        return programRepository.findById(tenantId, programId)
                .switchIfEmpty(Mono.error(new ReferralProgramNotFoundException(programId)))
                .flatMap(program -> programRepository.save(program.activate()));
    }

    @Override
    public Mono<ReferralProgram> deactivateProgram(TenantId tenantId, UUID programId) {
        return programRepository.findById(tenantId, programId)
                .switchIfEmpty(Mono.error(new ReferralProgramNotFoundException(programId)))
                .flatMap(program -> programRepository.save(program.deactivate()));
    }

    @Override
    public Mono<ReferralProgram> getActiveProgram(TenantId tenantId) {
        return programRepository.findActiveByTenantId(tenantId)
                .switchIfEmpty(Mono.error(new ReferralProgramNotFoundException(null)));
    }
}
