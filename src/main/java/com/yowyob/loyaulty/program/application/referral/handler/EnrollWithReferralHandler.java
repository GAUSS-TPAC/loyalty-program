package com.yowyob.loyaulty.program.application.referral.handler;

import com.yowyob.loyaulty.program.domain.referral.model.ReferralEvent;
import com.yowyob.loyaulty.program.domain.referral.port.in.EnrollWithReferralUseCase;
import com.yowyob.loyaulty.program.domain.referral.port.out.ReferralEventRepository;
import com.yowyob.loyaulty.program.domain.referral.port.out.ReferralLinkRepository;
import com.yowyob.loyaulty.program.domain.referral.port.out.ReferralProgramRepository;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class EnrollWithReferralHandler implements EnrollWithReferralUseCase {

    private final ReferralLinkRepository linkRepository;
    private final ReferralEventRepository eventRepository;
    private final ReferralProgramRepository programRepository;

    public EnrollWithReferralHandler(ReferralLinkRepository linkRepository,
                                      ReferralEventRepository eventRepository,
                                      ReferralProgramRepository programRepository) {
        this.linkRepository   = linkRepository;
        this.eventRepository  = eventRepository;
        this.programRepository = programRepository;
    }

    @Override
    public Mono<ReferralEvent> enroll(TenantId tenantId, String refereeId, String referralCode) {
        // Vérifier que le filleul n'a pas déjà un parrainage en cours
        return eventRepository.findPendingByReferee(tenantId, refereeId)
                .flatMap(existing -> Mono.<ReferralEvent>error(
                        new IllegalStateException("Member already has a pending referral")))
                .switchIfEmpty(Mono.defer(() ->
                        // Résoudre le code vers le parrain
                        linkRepository.findByCode(tenantId, referralCode)
                                .switchIfEmpty(Mono.error(new IllegalArgumentException("Invalid referral code: " + referralCode)))
                                .flatMap(link -> {
                                    // Empêcher l'auto-parrainage
                                    if (link.referrerId().equals(refereeId)) {
                                        return Mono.error(new IllegalArgumentException("Cannot refer yourself"));
                                    }
                                    // Charger le programme pour obtenir le délai
                                    return programRepository.findByTenant(tenantId)
                                            .defaultIfEmpty(com.yowyob.loyaulty.program.domain.referral.model.ReferralProgram.createDefault(tenantId))
                                            .flatMap(program -> {
                                                ReferralEvent referralEvent = ReferralEvent.create(
                                                        tenantId,
                                                        link.referrerId(),
                                                        refereeId,
                                                        referralCode,
                                                        program.conversionDeadlineDays()
                                                );
                                                return eventRepository.save(referralEvent);
                                            });
                                })
                ));
    }
}
