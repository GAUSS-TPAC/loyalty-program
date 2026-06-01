package com.yowyob.loyaulty.program.api.member.dto.response;

import com.yowyob.loyaulty.program.domain.member.model.Member;

import java.util.UUID;

public record MemberResponse(
        UUID id,
        String externalId,
        String email,
        String phone,
        String displayName,
        String status
) {
    public static MemberResponse from(Member m) {
        return new MemberResponse(
                m.getId(), m.getExternalId(), m.getEmail(),
                m.getPhone(), m.getDisplayName(), m.getStatus().name()
        );
    }
}
