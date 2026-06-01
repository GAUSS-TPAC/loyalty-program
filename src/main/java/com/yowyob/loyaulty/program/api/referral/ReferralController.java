package com.yowyob.loyaulty.program.api.referral;

import com.yowyob.loyaulty.program.api.referral.dto.ReferralEventResponse;
import com.yowyob.loyaulty.program.api.referral.dto.ReferralLinkResponse;
import com.yowyob.loyaulty.program.domain.referral.port.in.ConvertReferralUseCase;
import com.yowyob.loyaulty.program.domain.referral.port.in.EnrollWithReferralUseCase;
import com.yowyob.loyaulty.program.domain.referral.port.in.GetReferralLinkUseCase;
import com.yowyob.loyaulty.program.domain.referral.port.out.ReferralEventRepository;
import com.yowyob.loyaulty.program.shared.multitenancy.TenantContextHolder;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/referral")
public class ReferralController {

    private static final String BASE_URL = "https://app.yowyob.com";

    private final GetReferralLinkUseCase getReferralLinkUseCase;
    private final EnrollWithReferralUseCase enrollUseCase;
    private final ConvertReferralUseCase convertUseCase;
    private final ReferralEventRepository eventRepository;

    public ReferralController(GetReferralLinkUseCase getReferralLinkUseCase,
                               EnrollWithReferralUseCase enrollUseCase,
                               ConvertReferralUseCase convertUseCase,
                               ReferralEventRepository eventRepository) {
        this.getReferralLinkUseCase = getReferralLinkUseCase;
        this.enrollUseCase          = enrollUseCase;
        this.convertUseCase         = convertUseCase;
        this.eventRepository        = eventRepository;
    }

    /** Récupère (ou génère) le lien de parrainage du membre courant */
    @GetMapping("/link")
    public Mono<ReferralLinkResponse> getMyLink() {
        return TenantContextHolder.getTenantId()
                .zipWith(resolveMemberId())
                .flatMap(t -> getReferralLinkUseCase.getOrCreate(t.getT1(), t.getT2()))
                .map(link -> ReferralLinkResponse.from(link, BASE_URL));
    }

    /** Inscrit le membre courant avec un code de parrainage */
    @PostMapping("/enroll")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ReferralEventResponse> enroll(@RequestParam @NotBlank String code) {
        return TenantContextHolder.getTenantId()
                .zipWith(resolveMemberId())
                .flatMap(t -> enrollUseCase.enroll(t.getT1(), t.getT2(), code))
                .map(ReferralEventResponse::from);
    }

    /** Historique des parrainages effectués par le membre courant */
    @GetMapping("/my-referrals")
    public Flux<ReferralEventResponse> getMyReferrals() {
        return TenantContextHolder.getTenantId()
                .zipWith(resolveMemberId())
                .flatMapMany(t -> eventRepository.findByReferrer(t.getT1(), t.getT2()))
                .map(ReferralEventResponse::from);
    }

    /** Déclenche manuellement la conversion d'un filleul (pour tests / intégration) */
    @PostMapping("/convert")
    public Mono<ReferralEventResponse> convert(
            @RequestParam(defaultValue = "purchase.completed") String eventType,
            @RequestParam(defaultValue = "5000") BigDecimal amount) {
        return TenantContextHolder.getTenantId()
                .zipWith(resolveMemberId())
                .flatMap(t -> convertUseCase.convert(t.getT1(), t.getT2(), eventType, amount))
                .map(ReferralEventResponse::from);
    }

    private Mono<String> resolveMemberId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName());
    }
}
