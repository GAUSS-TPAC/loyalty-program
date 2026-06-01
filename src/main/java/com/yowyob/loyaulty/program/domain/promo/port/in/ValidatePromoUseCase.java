package com.yowyob.loyaulty.program.domain.promo.port.in;

import com.yowyob.loyaulty.program.domain.promo.model.PromoCampaign;
import com.yowyob.loyaulty.program.domain.promo.model.PromoUsage;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

public interface ValidatePromoUseCase {

    /** Vérifie si le code est valide et retourne la réduction à appliquer. */
    Mono<BigDecimal> validate(TenantId tenantId, String code,
                               String memberId, BigDecimal orderAmount);

    /** Applique le code (enregistre l'usage) après confirmation de la commande. */
    Mono<PromoUsage> apply(TenantId tenantId, String code, String memberId,
                            BigDecimal orderAmount, String orderReference);

    /** Crée une nouvelle campagne promo. */
    Mono<PromoCampaign> createCampaign(TenantId tenantId, String name, String code,
                                        String discountType, BigDecimal discountValue,
                                        Integer maxUsesTotal, Integer maxUsesPerMember,
                                        BigDecimal minOrderAmount,
                                        String validFrom, String validUntil);

    Flux<PromoCampaign> listCampaigns(TenantId tenantId);

    Mono<PromoCampaign> activateCampaign(TenantId tenantId, UUID campaignId);
}
