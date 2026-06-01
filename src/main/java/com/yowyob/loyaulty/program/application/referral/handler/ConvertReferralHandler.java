package com.yowyob.loyaulty.program.application.referral.handler;

import com.yowyob.loyaulty.program.domain.referral.model.ReferralEvent;
import com.yowyob.loyaulty.program.domain.referral.port.in.ConvertReferralUseCase;
import com.yowyob.loyaulty.program.domain.referral.port.out.ReferralEventRepository;
import com.yowyob.loyaulty.program.domain.referral.port.out.ReferralProgramRepository;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionSource;
import com.yowyob.loyaulty.program.domain.wallet.port.in.CreditWalletUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class ConvertReferralHandler implements ConvertReferralUseCase {

    private static final Logger log = LoggerFactory.getLogger(ConvertReferralHandler.class);

    private final ReferralEventRepository eventRepository;
    private final ReferralProgramRepository programRepository;
    private final CreditWalletUseCase creditWalletUseCase;

    public ConvertReferralHandler(ReferralEventRepository eventRepository,
                                   ReferralProgramRepository programRepository,
                                   CreditWalletUseCase creditWalletUseCase) {
        this.eventRepository   = eventRepository;
        this.programRepository = programRepository;
        this.creditWalletUseCase = creditWalletUseCase;
    }

    @Override
    public Mono<ReferralEvent> convert(TenantId tenantId, String refereeId,
                                        String eventType, BigDecimal amount) {
        return eventRepository.findPendingByReferee(tenantId, refereeId)
                .flatMap(referral -> {
                    if (referral.isExpired()) {
                        referral.expire();
                        return eventRepository.save(referral)
                                .then(Mono.empty());
                    }
                    return programRepository.findByTenant(tenantId)
                            .defaultIfEmpty(com.yowyob.loyaulty.program.domain.referral.model.ReferralProgram.createDefault(tenantId))
                            .flatMap(program -> {
                                // Vérifier que l'event correspond et que le montant est suffisant
                                if (!program.conversionEventType().equals(eventType)) {
                                    return Mono.<ReferralEvent>empty();
                                }
                                if (amount.compareTo(program.minConversionAmount()) < 0) {
                                    return Mono.<ReferralEvent>empty();
                                }

                                // Convertir le parrainage
                                referral.convert();

                                // Récompenser le parrain
                                Mono<Void> rewardReferrer = creditWalletUseCase.credit(
                                        tenantId,
                                        referral.getReferrerId(),
                                        program.referrerRewardValue(),
                                        TransactionSource.REFERRAL_BONUS,
                                        "referral-referrer-" + referral.getId()
                                ).doOnSuccess(tx -> log.info("Referrer {} rewarded: {} points",
                                        referral.getReferrerId(), program.referrerRewardValue()))
                                .then();

                                // Récompenser le filleul
                                Mono<Void> rewardReferee = creditWalletUseCase.credit(
                                        tenantId,
                                        referral.getRefereeId(),
                                        program.refereeRewardValue(),
                                        TransactionSource.REFERRAL_BONUS,
                                        "referral-referee-" + referral.getId()
                                ).doOnSuccess(tx -> log.info("Referee {} rewarded: {} points",
                                        referral.getRefereeId(), program.refereeRewardValue()))
                                .then();

                                return Mono.when(rewardReferrer, rewardReferee)
                                        .then(Mono.fromRunnable(referral::reward))
                                        .then(eventRepository.save(referral));
                            });
                });
    }
}
