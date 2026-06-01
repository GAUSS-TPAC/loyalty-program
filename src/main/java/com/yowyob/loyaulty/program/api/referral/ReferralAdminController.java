package com.yowyob.loyaulty.program.api.referral;

import com.yowyob.loyaulty.program.api.referral.dto.ReferralEventResponse;
import com.yowyob.loyaulty.program.domain.referral.model.ReferralProgram;
import com.yowyob.loyaulty.program.domain.referral.port.out.ReferralEventRepository;
import com.yowyob.loyaulty.program.domain.referral.port.out.ReferralProgramRepository;
import com.yowyob.loyaulty.program.shared.multitenancy.TenantContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/admin/referral")
public class ReferralAdminController {

    private final ReferralProgramRepository programRepository;
    private final ReferralEventRepository   eventRepository;

    public ReferralAdminController(ReferralProgramRepository programRepository,
                                    ReferralEventRepository eventRepository) {
        this.programRepository = programRepository;
        this.eventRepository   = eventRepository;
    }

    /** Configurer le programme de parrainage du tenant */
    @PutMapping("/program")
    @ResponseStatus(HttpStatus.OK)
    public Mono<ReferralProgram> configureProgram(
            @RequestParam(defaultValue = "CREDIT_POINTS") String referrerRewardType,
            @RequestParam(defaultValue = "500")           BigDecimal referrerRewardValue,
            @RequestParam(defaultValue = "CREDIT_POINTS") String refereeRewardType,
            @RequestParam(defaultValue = "200")           BigDecimal refereeRewardValue,
            @RequestParam(defaultValue = "purchase.completed") String conversionEventType,
            @RequestParam(defaultValue = "0")             BigDecimal minConversionAmount,
            @RequestParam(defaultValue = "30")            int conversionDeadlineDays) {

        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> programRepository.findByTenant(tenantId)
                        .defaultIfEmpty(ReferralProgram.createDefault(tenantId))
                        .map(existing -> new ReferralProgram(
                                existing.id(), tenantId, true,
                                referrerRewardType, referrerRewardValue,
                                refereeRewardType,  refereeRewardValue,
                                conversionEventType, minConversionAmount,
                                conversionDeadlineDays
                        ))
                        .flatMap(programRepository::save)
                );
    }

    /** Lister tous les parrainages du tenant */
    @GetMapping("/events")
    public Flux<ReferralEventResponse> listEvents() {
        return TenantContextHolder.getTenantId()
                .flatMapMany(eventRepository::findAllByTenant)
                .map(ReferralEventResponse::from);
    }
}
