package com.yowyob.loyalty.api.reward.dto.request;

import java.util.Map;

public record UpdateRewardRequest(
        String name,
        String description,
        String imageUrl,
        Map<String, Object> metadata
) {}
