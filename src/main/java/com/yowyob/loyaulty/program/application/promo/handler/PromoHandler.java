package com.yowyob.loyaulty.program.application.promo.handler;

import com.yowyob.loyaulty.program.domain.promo.model.PromoCampaign;
import com.yowyob.loyaulty.program.domain.promo.model.PromoUsage;
import com.yowyob.loyaulty.program.domain.promo.model.enums.DiscountType;
import com.yowyob.loyaulty.program.domain.promo.port.in.ValidatePromoUseCase;
import com.yowyob.loyaulty.program.domain.promo.port.out.PromoCampaignRepository;
import com.yowyob.loyaulty.program.domain.promo.port.out.PromoUsageRepository;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.shared.exception.AppException;
import com.yowyob.loyaulty.program.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class PromoHandler implements ValidatePromoUseCase {

    private final PromoCampaignRepository campaignRepository;
    private final PromoUsageRepository usageRepository;

    public PromoHandler(PromoCampaignRepository campaignRepository,
                        PromoUsageRepository usageRepository) {
        this.campaignRepository = campaignRepository;
        this.usageRepository = usageRepository;
    }

    @Override
    public Mono<BigDecimal> validate(TenantId tenantId, String code,
                                      String memberId, BigDecimal orderAmount) {
        return campaignRepository.findByCode(code, tenantId)
                .switchIfEmpty(Mono.error(new PromoNotFoundException(code)))
                .flatMap(campaign -> {
                    String error = campaign.validate(orderAmount, Instant.now());
                    if (error != null) return Mono.error(new PromoInvalidException(error));

                    return checkLimits(campaign, memberId, tenantId)
                            .thenReturn(campaign.calculateDiscount(orderAmount));
                });
    }

    @Override
    public Mono<PromoUsage> apply(TenantId tenantId, String code, String memberId,
                                   BigDecimal orderAmount, String orderReference) {
        return campaignRepository.findByCode(code, tenantId)
                .switchIfEmpty(Mono.error(new PromoNotFoundException(code)))
                .flatMap(campaign -> {
                    String error = campaign.validate(orderAmount, Instant.now());
                    if (error != null) return Mono.error(new PromoInvalidException(error));

                    return checkLimits(campaign, memberId, tenantId)
                            .flatMap(ignored -> {
                                BigDecimal discount = campaign.calculateDiscount(orderAmount);
                                PromoUsage usage = PromoUsage.record(
                                        tenantId, campaign.getId(),
                                        memberId, orderReference, discount);
                                return usageRepository.save(usage);
                            });
                });
    }

    @Override
    public Mono<PromoCampaign> createCampaign(TenantId tenantId, String name, String code,
                                               String discountType, BigDecimal discountValue,
                                               Integer maxUsesTotal, Integer maxUsesPerMember,
                                               BigDecimal minOrderAmount,
                                               String validFrom, String validUntil) {
        PromoCampaign campaign = PromoCampaign.create(
                tenantId, name, code,
                DiscountType.valueOf(discountType.toUpperCase()),
                discountValue, maxUsesTotal, maxUsesPerMember, minOrderAmount,
                validFrom != null ? Instant.parse(validFrom) : null,
                validUntil != null ? Instant.parse(validUntil) : null
        );
        return campaignRepository.save(campaign);
    }

    @Override
    public Flux<PromoCampaign> listCampaigns(TenantId tenantId) {
        return campaignRepository.findAllByTenant(tenantId);
    }

    @Override
    public Mono<PromoCampaign> activateCampaign(TenantId tenantId, UUID campaignId) {
        return campaignRepository.findById(campaignId, tenantId)
                .switchIfEmpty(Mono.error(new PromoNotFoundException(campaignId.toString())))
                .flatMap(campaign -> {
                    campaign.activate();
                    return campaignRepository.save(campaign);
                });
    }

    private Mono<Void> checkLimits(PromoCampaign campaign, String memberId, TenantId tenantId) {
        Mono<Void> globalCheck = campaign.getMaxUsesTotal() == null ? Mono.empty() :
                usageRepository.countByCampaign(campaign.getId(), tenantId)
                        .flatMap(count -> count >= campaign.getMaxUsesTotal()
                                ? Mono.error(new PromoInvalidException("Promo usage limit reached"))
                                : Mono.empty());

        Mono<Void> memberCheck = campaign.getMaxUsesPerMember() == null ? Mono.empty() :
                usageRepository.countByCampaignAndMember(campaign.getId(), memberId, tenantId)
                        .flatMap(count -> count >= campaign.getMaxUsesPerMember()
                                ? Mono.error(new PromoInvalidException("Member usage limit reached"))
                                : Mono.empty());

        return globalCheck.then(memberCheck);
    }

    static class PromoNotFoundException extends AppException {
        PromoNotFoundException(String code) {
            super(ErrorCode.VALIDATION_ERROR, "Promo not found: " + code);
        }
    }

    static class PromoInvalidException extends AppException {
        PromoInvalidException(String reason) {
            super(ErrorCode.VALIDATION_ERROR, reason);
        }
    }
}
