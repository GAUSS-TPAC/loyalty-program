package com.yowyob.loyaulty.program.application.member.handler;

import com.yowyob.loyaulty.program.domain.member.model.Member;
import com.yowyob.loyaulty.program.domain.member.model.MemberTier;
import com.yowyob.loyaulty.program.domain.member.port.in.GetMemberUseCase;
import com.yowyob.loyaulty.program.domain.member.port.out.MemberRepository;
import com.yowyob.loyaulty.program.domain.member.port.out.MemberTierRepository;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import com.yowyob.loyaulty.program.shared.exception.AppException;
import com.yowyob.loyaulty.program.shared.exception.ErrorCode;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class GetMemberHandler implements GetMemberUseCase {

    private final MemberRepository memberRepository;
    private final MemberTierRepository tierRepository;

    public GetMemberHandler(MemberRepository memberRepository,
                             MemberTierRepository tierRepository) {
        this.memberRepository = memberRepository;
        this.tierRepository = tierRepository;
    }

    @Override
    public Mono<Member> getById(TenantId tenantId, UUID memberId) {
        return memberRepository.findById(memberId, tenantId)
                .switchIfEmpty(Mono.error(new MemberNotFoundException(memberId.toString())));
    }

    @Override
    public Mono<Member> getByExternalId(TenantId tenantId, String externalId) {
        return memberRepository.findByExternalId(externalId, tenantId)
                .switchIfEmpty(Mono.error(new MemberNotFoundException(externalId)));
    }

    @Override
    public Flux<Member> listAll(TenantId tenantId) {
        return memberRepository.findAllByTenant(tenantId);
    }

    @Override
    public Mono<MemberTier> getTier(TenantId tenantId, String memberId) {
        return tierRepository.findByMemberId(memberId, tenantId)
                .switchIfEmpty(Mono.error(new MemberNotFoundException(memberId)));
    }

    static class MemberNotFoundException extends AppException {
        MemberNotFoundException(String id) {
            super(ErrorCode.MEMBER_NOT_FOUND, "Member not found: " + id);
        }
    }
}
