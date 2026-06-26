package com.yowyob.loyalty.domain.promo.service;

import com.yowyob.loyalty.domain.promo.exception.*;
import com.yowyob.loyalty.domain.promo.model.*;
import com.yowyob.loyalty.domain.promo.port.in.*;
import com.yowyob.loyalty.domain.promo.port.out.*;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class PromoDomainService implements
        CreatePromoCampaignUseCase,
        ValidatePromoCodeUseCase,
        ApplyPromoCodeUseCase,
        GetPromoCampaignUseCase,
        ManagePromoCampaignUseCase {

    private final PromoCampaignRepository campaignRepository;
    private final PromoUsageRepository usageRepository;
    private final PromoUsageCounterPort usageCounter;

    public PromoDomainService(PromoCampaignRepository campaignRepository,
                               PromoUsageRepository usageRepository,
                               PromoUsageCounterPort usageCounter) {
        this.campaignRepository = campaignRepository;
        this.usageRepository = usageRepository;
        this.usageCounter = usageCounter;
    }

    @Override
    public Mono<PromoCampaign> createCampaign(TenantId tenantId, String code, String name,
                                               PromoDiscountType discountType, BigDecimal discountValue,
                                               BigDecimal minOrderAmount, int maxUses, int perMemberLimit,
                                               Instant startDate, Instant endDate) {
        PromoCampaign campaign = PromoCampaign.create(UUID.randomUUID(), tenantId, code, name,
                discountType, discountValue, minOrderAmount, maxUses, perMemberLimit, startDate, endDate);
        return campaignRepository.save(campaign);
    }

    @Override
    public Mono<PromoValidationResult> validate(TenantId tenantId, String code, UserId memberId, BigDecimal orderAmount) {
        return campaignRepository.findByCode(tenantId, code)
                .switchIfEmpty(Mono.error(new PromoCampaignNotFoundException(code)))
                .flatMap(campaign -> checkEligibility(campaign, code, memberId, orderAmount)
                        .thenReturn(PromoValidationResult.valid(campaign, orderAmount)));
    }

    @Override
    public Mono<PromoUsage> apply(TenantId tenantId, String code, UserId memberId, String orderId, BigDecimal orderAmount) {
        return campaignRepository.findByCode(tenantId, code)
                .switchIfEmpty(Mono.error(new PromoCampaignNotFoundException(code)))
                .flatMap(campaign -> usageRepository.existsByOrderId(tenantId, campaign.id(), orderId)
                        .flatMap(alreadyApplied -> {
                            if (alreadyApplied) {
                                return Mono.error(new PromoAlreadyUsedException(code));
                            }
                            return checkEligibility(campaign, code, memberId, orderAmount)
                                    .then(usageCounter.increment(tenantId, campaign.id()))
                                    .flatMap(count -> {
                                        BigDecimal discount = campaign.calculateDiscount(orderAmount);
                                        PromoUsage usage = PromoUsage.record(tenantId, campaign.id(),
                                                memberId, orderId, discount);
                                        return usageRepository.save(usage);
                                    });
                        }));
    }

    private Mono<Void> checkEligibility(PromoCampaign campaign, String code, UserId memberId, BigDecimal orderAmount) {
        Instant now = Instant.now();

        if (!campaign.isActive()) {
            return Mono.error(new PromoNotActiveException(code));
        }
        if (!campaign.isStarted(now)) {
            return Mono.error(new PromoNotStartedException(code));
        }
        if (campaign.isExpired(now)) {
            return Mono.error(new PromoExpiredException(code));
        }
        if (orderAmount.compareTo(campaign.minOrderAmount()) < 0) {
            return Mono.error(new PromoMinOrderAmountException(campaign.minOrderAmount(), orderAmount));
        }

        Mono<Void> checkGlobal = campaign.isUnlimited() ? Mono.empty() :
                usageCounter.getCount(campaign.tenantId(), campaign.id())
                        .flatMap(count -> count >= campaign.maxUses()
                                ? Mono.error(new PromoExhaustedException(code))
                                : Mono.empty());

        Mono<Void> checkMember = !campaign.hasPerMemberLimit() ? Mono.empty() :
                usageRepository.countByMemberAndCampaign(campaign.tenantId(), campaign.id(), memberId)
                        .flatMap(count -> count >= campaign.perMemberLimit()
                                ? Mono.error(new PromoAlreadyUsedException(code))
                                : Mono.empty());

        return checkGlobal.then(checkMember);
    }

    @Override
    public Mono<PromoCampaign> getById(TenantId tenantId, UUID campaignId) {
        return campaignRepository.findById(tenantId, campaignId)
                .switchIfEmpty(Mono.error(new PromoCampaignNotFoundException(campaignId.toString())));
    }

    @Override
    public Mono<PromoCampaign> getByCode(TenantId tenantId, String code) {
        return campaignRepository.findByCode(tenantId, code)
                .switchIfEmpty(Mono.error(new PromoCampaignNotFoundException(code)));
    }

    @Override
    public Flux<PromoCampaign> listAll(TenantId tenantId) {
        return campaignRepository.findAll(tenantId);
    }

    @Override
    public Flux<PromoCampaign> listActive(TenantId tenantId) {
        return campaignRepository.findActive(tenantId);
    }

    @Override
    public Mono<PromoCampaign> activate(TenantId tenantId, UUID campaignId) {
        return campaignRepository.findById(tenantId, campaignId)
                .switchIfEmpty(Mono.error(new PromoCampaignNotFoundException(campaignId.toString())))
                .flatMap(campaign -> campaignRepository.save(campaign.activate()));
    }

    @Override
    public Mono<PromoCampaign> deactivate(TenantId tenantId, UUID campaignId) {
        return campaignRepository.findById(tenantId, campaignId)
                .switchIfEmpty(Mono.error(new PromoCampaignNotFoundException(campaignId.toString())))
                .flatMap(campaign -> campaignRepository.save(campaign.deactivate()));
    }

    @Override
    public Mono<Void> delete(TenantId tenantId, UUID campaignId) {
        return campaignRepository.findById(tenantId, campaignId)
                .switchIfEmpty(Mono.error(new PromoCampaignNotFoundException(campaignId.toString())))
                .then(usageCounter.reset(tenantId, campaignId))
                .then(campaignRepository.deleteById(tenantId, campaignId));
    }
}
