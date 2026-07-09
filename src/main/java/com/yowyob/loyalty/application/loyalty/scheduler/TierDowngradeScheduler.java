package com.yowyob.loyalty.application.loyalty.scheduler;

import com.yowyob.loyalty.domain.loyalty.model.tier.MemberTier;
import com.yowyob.loyalty.domain.loyalty.model.tier.TierLevel;
import com.yowyob.loyalty.domain.loyalty.model.tier.TierPolicy;
import com.yowyob.loyalty.domain.loyalty.port.out.MemberTierRepository;
import com.yowyob.loyalty.domain.loyalty.port.out.PointsAccountRepository;
import com.yowyob.loyalty.domain.loyalty.port.out.TierPolicyRepository;
import com.yowyob.loyalty.domain.loyalty.service.TierCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class TierDowngradeScheduler {

    private static final Logger log = LoggerFactory.getLogger(TierDowngradeScheduler.class);

    private final MemberTierRepository memberTierRepository;
    private final PointsAccountRepository pointsAccountRepository;
    private final TierPolicyRepository tierPolicyRepository;
    private final TierCalculationService tierCalculationService;

    public TierDowngradeScheduler(MemberTierRepository memberTierRepository,
                                   PointsAccountRepository pointsAccountRepository,
                                   TierPolicyRepository tierPolicyRepository,
                                   TierCalculationService tierCalculationService) {
        this.memberTierRepository = memberTierRepository;
        this.pointsAccountRepository = pointsAccountRepository;
        this.tierPolicyRepository = tierPolicyRepository;
        this.tierCalculationService = tierCalculationService;
    }

    @Scheduled(cron = "${loyalty.tier.downgrade.cron:0 0 2 * * *}")
    public void evaluateTierDowngrades() {
        log.info("Starting tier downgrade evaluation");
        int downgraded = 0;

        List<MemberTier> candidates = memberTierRepository.findAllAboveBronze();
        for (MemberTier tier : candidates) {
            try {
                TierPolicy policy = tierPolicyRepository.findByTenantId(tier.tenantId())
                        .blockOptional()
                        .orElseGet(() -> TierPolicy.defaults(tier.tenantId()));

                var accountOpt = pointsAccountRepository.findByMemberId(tier.tenantId(), tier.memberId());
                if (accountOpt.isEmpty()) continue;

                var newTierOpt = tierCalculationService.evaluateNewTier(accountOpt.get(), tier, policy);
                if (newTierOpt.isPresent() && isDowngrade(tier.level(), newTierOpt.get())) {
                    BigDecimal multiplier = tierCalculationService.getMultiplierForTier(newTierOpt.get(), policy);
                    memberTierRepository.save(tier.withLevel(newTierOpt.get(), multiplier));
                    downgraded++;
                    log.info("Member {} downgraded from {} to {} (tenant: {})",
                            tier.memberId(), tier.level(), newTierOpt.get(), tier.tenantId());
                }
            } catch (Exception e) {
                log.warn("Error evaluating tier for member {}: {}", tier.memberId(), e.getMessage());
            }
        }

        log.info("Tier downgrade evaluation complete: {} member(s) downgraded", downgraded);
    }

    private boolean isDowngrade(TierLevel current, TierLevel candidate) {
        return candidate.ordinal() < current.ordinal();
    }
}
