package com.yowyob.loyalty.api.referral;

import com.yowyob.loyalty.api.referral.dto.request.ConvertReferralRequest;
import com.yowyob.loyalty.api.referral.dto.request.CreateReferralProgramRequest;
import com.yowyob.loyalty.api.referral.dto.request.EnrollReferralRequest;
import com.yowyob.loyalty.api.referral.dto.response.ReferralEventResponse;
import com.yowyob.loyalty.api.referral.dto.response.ReferralLinkResponse;
import com.yowyob.loyalty.api.referral.dto.response.ReferralProgramResponse;
import com.yowyob.loyalty.domain.referral.port.in.*;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/referral")
@Tag(name = "Referral", description = "Programme de parrainage")
public class ReferralController {

    private final CreateReferralLinkUseCase createLinkUseCase;
    private final RegisterReferralUseCase registerUseCase;
    private final ConvertReferralUseCase convertUseCase;
    private final GetReferralStatsUseCase statsUseCase;
    private final ManageReferralProgramUseCase manageUseCase;

    public ReferralController(CreateReferralLinkUseCase createLinkUseCase,
                               RegisterReferralUseCase registerUseCase,
                               ConvertReferralUseCase convertUseCase,
                               GetReferralStatsUseCase statsUseCase,
                               ManageReferralProgramUseCase manageUseCase) {
        this.createLinkUseCase = createLinkUseCase;
        this.registerUseCase = registerUseCase;
        this.convertUseCase = convertUseCase;
        this.statsUseCase = statsUseCase;
        this.manageUseCase = manageUseCase;
    }

    @GetMapping("/program")
    public Mono<ReferralProgramResponse> getProgram() {
        return TenantContextHolder.getTenantId()
                .flatMap(manageUseCase::getActiveProgram)
                .map(ReferralProgramResponse::from);
    }

    @PostMapping("/admin/program")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ReferralProgramResponse> createProgram(@Valid @RequestBody CreateReferralProgramRequest request) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> manageUseCase.createProgram(
                        tenantId, request.name(),
                        request.referrerRewardAmount(), request.refereeRewardAmount(),
                        request.maxReferrals(), request.startDate(), request.endDate()))
                .map(ReferralProgramResponse::from);
    }

    @PatchMapping("/admin/program/{programId}/activate")
    public Mono<ReferralProgramResponse> activateProgram(@PathVariable UUID programId) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> manageUseCase.activateProgram(tenantId, programId))
                .map(ReferralProgramResponse::from);
    }

    @PatchMapping("/admin/program/{programId}/deactivate")
    public Mono<ReferralProgramResponse> deactivateProgram(@PathVariable UUID programId) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> manageUseCase.deactivateProgram(tenantId, programId))
                .map(ReferralProgramResponse::from);
    }

    @GetMapping("/my-link")
    public Mono<ReferralLinkResponse> getOrCreateMyLink() {
        return Mono.zip(
                TenantContextHolder.getTenantId(),
                ReactiveSecurityContextHolder.getContext()
                        .map(ctx -> UserId.of(ctx.getAuthentication().getName()))
        ).flatMap(tuple -> statsUseCase.getMyLink(tuple.getT1(), tuple.getT2())
                .onErrorResume(e -> createLinkUseCase.createLink(tuple.getT1(), tuple.getT2())))
        .map(ReferralLinkResponse::from);
    }

    @PostMapping("/enroll")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ReferralEventResponse> enroll(@Valid @RequestBody EnrollReferralRequest request) {
        return Mono.zip(
                TenantContextHolder.getTenantId(),
                ReactiveSecurityContextHolder.getContext()
                        .map(ctx -> UserId.of(ctx.getAuthentication().getName()))
        ).flatMap(tuple -> registerUseCase.register(tuple.getT1(), request.referralCode(), tuple.getT2()))
        .map(ReferralEventResponse::from);
    }

    @PostMapping("/me/convert")
    public Mono<ReferralEventResponse> convert(@Valid @RequestBody ConvertReferralRequest request) {
        return Mono.zip(
                TenantContextHolder.getTenantId(),
                ReactiveSecurityContextHolder.getContext()
                        .map(ctx -> UserId.of(ctx.getAuthentication().getName()))
        ).flatMap(tuple -> convertUseCase.convert(tuple.getT1(), tuple.getT2(), request.conversionAmount()))
        .map(ReferralEventResponse::from);
    }

    @GetMapping("/my-referrals")
    public Flux<ReferralEventResponse> getMyReferrals() {
        return Mono.zip(
                TenantContextHolder.getTenantId(),
                ReactiveSecurityContextHolder.getContext()
                        .map(ctx -> UserId.of(ctx.getAuthentication().getName()))
        ).flatMapMany(tuple -> statsUseCase.getMyReferrals(tuple.getT1(), tuple.getT2()))
        .map(ReferralEventResponse::from);
    }
}
