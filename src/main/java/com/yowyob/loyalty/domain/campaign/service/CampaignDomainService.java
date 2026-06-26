package com.yowyob.loyalty.domain.campaign.service;

import com.yowyob.loyalty.domain.campaign.exception.CampaignNotFoundException;
import com.yowyob.loyalty.domain.campaign.model.Campaign;
import com.yowyob.loyalty.domain.campaign.model.CampaignStatus;
import com.yowyob.loyalty.domain.campaign.model.CampaignType;
import com.yowyob.loyalty.domain.campaign.port.in.*;
import com.yowyob.loyalty.domain.campaign.port.out.CampaignRepository;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class CampaignDomainService implements
        CreateCampaignUseCase,
        GetCampaignUseCase,
        ManageCampaignUseCase,
        ProcessCampaignScheduleUseCase {

    private static final Logger log = LoggerFactory.getLogger(CampaignDomainService.class);

    private final CampaignRepository campaignRepository;

    public CampaignDomainService(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    @Override
    public Mono<Campaign> createCampaign(TenantId tenantId, String name, String description,
                                          CampaignType type, String targetEventType,
                                          BigDecimal bonusMultiplier, long bonusPoints,
                                          Instant startDate, Instant endDate) {
        Campaign campaign = Campaign.create(UUID.randomUUID(), tenantId, name, description,
                type, targetEventType, bonusMultiplier, bonusPoints, startDate, endDate);
        return campaignRepository.save(campaign);
    }

    @Override
    public Mono<Campaign> getById(TenantId tenantId, UUID campaignId) {
        return campaignRepository.findById(tenantId, campaignId)
                .switchIfEmpty(Mono.error(new CampaignNotFoundException(campaignId)));
    }

    @Override
    public Flux<Campaign> listAll(TenantId tenantId) {
        return campaignRepository.findAll(tenantId);
    }

    @Override
    public Flux<Campaign> listActive(TenantId tenantId) {
        return campaignRepository.findByStatus(tenantId, CampaignStatus.ACTIVE);
    }

    @Override
    public Mono<Campaign> activate(TenantId tenantId, UUID campaignId) {
        return campaignRepository.findById(tenantId, campaignId)
                .switchIfEmpty(Mono.error(new CampaignNotFoundException(campaignId)))
                .flatMap(c -> campaignRepository.save(c.activate()));
    }

    @Override
    public Mono<Campaign> pause(TenantId tenantId, UUID campaignId) {
        return campaignRepository.findById(tenantId, campaignId)
                .switchIfEmpty(Mono.error(new CampaignNotFoundException(campaignId)))
                .flatMap(c -> campaignRepository.save(c.pause()));
    }

    @Override
    public Mono<Campaign> cancel(TenantId tenantId, UUID campaignId) {
        return campaignRepository.findById(tenantId, campaignId)
                .switchIfEmpty(Mono.error(new CampaignNotFoundException(campaignId)))
                .flatMap(c -> campaignRepository.save(c.cancel()));
    }

    @Override
    public Mono<Integer> activateDueCampaigns() {
        Instant now = Instant.now();
        AtomicInteger count = new AtomicInteger(0);
        return campaignRepository.findDueForActivation(now)
                .flatMap(campaign -> {
                    try {
                        Campaign activated = campaign.activate();
                        return campaignRepository.save(activated)
                                .doOnSuccess(c -> {
                                    count.incrementAndGet();
                                    log.info("Campaign auto-activated: {} ({})", c.name(), c.id());
                                });
                    } catch (Exception e) {
                        log.warn("Could not activate campaign {}: {}", campaign.id(), e.getMessage());
                        return Mono.empty();
                    }
                })
                .then(Mono.fromCallable(count::get));
    }

    @Override
    public Mono<Integer> deactivateExpiredCampaigns() {
        Instant now = Instant.now();
        AtomicInteger count = new AtomicInteger(0);
        return campaignRepository.findDueForCompletion(now)
                .flatMap(campaign -> {
                    try {
                        Campaign completed = campaign.complete();
                        return campaignRepository.save(completed)
                                .doOnSuccess(c -> {
                                    count.incrementAndGet();
                                    log.info("Campaign auto-completed: {} ({})", c.name(), c.id());
                                });
                    } catch (Exception e) {
                        log.warn("Could not complete campaign {}: {}", campaign.id(), e.getMessage());
                        return Mono.empty();
                    }
                })
                .then(Mono.fromCallable(count::get));
    }
}
