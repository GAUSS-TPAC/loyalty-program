package com.yowyob.loyalty.api.webhook.dto;

import com.yowyob.loyalty.domain.webhook.port.out.WebhookSenderPort.WebhookAttemptResult;

public record TestPingResponse(boolean success, Integer httpStatus, String responseSnippet) {
    public static TestPingResponse from(WebhookAttemptResult result) {
        return new TestPingResponse(result.success(), result.httpStatusCode(), result.responseSnippet());
    }
}
