package com.yowyob.loyalty.infrastructure.bonification.dto;

import java.util.List;

public record BonificationJwtResponseDto(
        String token,
        String type,
        String id,
        String username,
        String email,
        String refreshToken,
        List<String> roles
) {}
