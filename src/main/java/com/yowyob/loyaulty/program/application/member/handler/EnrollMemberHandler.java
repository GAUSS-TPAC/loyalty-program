package com.yowyob.loyaulty.program.application.member.handler;

import com.yowyob.loyaulty.program.domain.member.model.Member;
import com.yowyob.loyaulty.program.domain.member.model.MemberTier;
import com.yowyob.loyaulty.program.domain.member.model.PointsAccount;
import com.yowyob.loyaulty.program.domain.member.port.in.EnrollMemberUseCase;
import com.yowyob.loyaulty.program.domain.member.port.out.MemberRepository;
import com.yowyob.loyaulty.program.domain.member.port.out.MemberTierRepository;
import com.yowyob.loyaulty.program.domain.member.port.out.PointsAccountRepository;
import com.yowyob.loyaulty.program.domain.shared.model.TenantId;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class EnrollMemberHandler implements EnrollMemberUseCase {

    private final MemberRepository memberRepository;
    private final MemberTierRepository tierRepository;
    private final PointsAccountRepository pointsAccountRepository;

    public EnrollMemberHandler(MemberRepository memberRepository,
                                MemberTierRepository tierRepository,
                                PointsAccountRepository pointsAccountRepository) {
        this.memberRepository = memberRepository;
        this.tierRepository = tierRepository;
        this.pointsAccountRepository = pointsAccountRepository;
    }

    @Override
    public Mono<Member> enroll(TenantId tenantId, String externalId,
                                String email, String phone, String displayName) {
        return memberRepository.existsByExternalId(externalId, tenantId)
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return memberRepository.findByExternalId(externalId, tenantId);
                    }
                    Member member = Member.enroll(tenantId, externalId, email, phone, displayName);
                    MemberTier tier = MemberTier.create(tenantId, member.getId().toString());
                    PointsAccount account = PointsAccount.create(tenantId, member.getId().toString());

                    return memberRepository.save(member)
                            .flatMap(saved ->
                                    Mono.zip(
                                            tierRepository.save(tier),
                                            pointsAccountRepository.save(account)
                                    ).thenReturn(saved)
                            );
                });
    }
}
