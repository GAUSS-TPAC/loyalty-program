package com.yowyob.loyalty.api.webhook.dto;

import java.util.List;

public record UpdateWebhookEndpointRequest(
        String url,
        String description,
        List<String> eventTypes,
        Boolean active
) {}
