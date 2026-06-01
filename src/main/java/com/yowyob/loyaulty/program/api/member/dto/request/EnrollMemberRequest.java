package com.yowyob.loyaulty.program.api.member.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnrollMemberRequest(
        @NotBlank @Size(max = 255) String externalId,
        String email,
        String phone,
        String displayName
) {}
