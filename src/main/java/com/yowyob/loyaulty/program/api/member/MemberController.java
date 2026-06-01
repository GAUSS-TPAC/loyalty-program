package com.yowyob.loyaulty.program.api.member;

import com.yowyob.loyaulty.program.api.member.dto.request.EnrollMemberRequest;
import com.yowyob.loyaulty.program.api.member.dto.response.MemberResponse;
import com.yowyob.loyaulty.program.api.member.dto.response.TierResponse;
import com.yowyob.loyaulty.program.domain.member.port.in.EnrollMemberUseCase;
import com.yowyob.loyaulty.program.domain.member.port.in.GetMemberUseCase;
import com.yowyob.loyaulty.program.shared.multitenancy.TenantContextHolder;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/members")
public class MemberController {

    private final EnrollMemberUseCase enrollUseCase;
    private final GetMemberUseCase getUseCase;

    public MemberController(EnrollMemberUseCase enrollUseCase, GetMemberUseCase getUseCase) {
        this.enrollUseCase = enrollUseCase;
        this.getUseCase = getUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MemberResponse> enroll(@Valid @RequestBody EnrollMemberRequest request) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> enrollUseCase.enroll(
                        tenantId, request.externalId(),
                        request.email(), request.phone(), request.displayName()))
                .map(MemberResponse::from);
    }

    @GetMapping
    public Flux<MemberResponse> listAll() {
        return TenantContextHolder.getTenantId()
                .flatMapMany(getUseCase::listAll)
                .map(MemberResponse::from);
    }

    @GetMapping("/{memberId}")
    public Mono<MemberResponse> getById(@PathVariable UUID memberId) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> getUseCase.getById(tenantId, memberId))
                .map(MemberResponse::from);
    }

    @GetMapping("/by-external/{externalId}")
    public Mono<MemberResponse> getByExternalId(@PathVariable String externalId) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> getUseCase.getByExternalId(tenantId, externalId))
                .map(MemberResponse::from);
    }

    @GetMapping("/{memberId}/tier")
    public Mono<TierResponse> getTier(@PathVariable String memberId) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> getUseCase.getTier(tenantId, memberId))
                .map(TierResponse::from);
    }
}
