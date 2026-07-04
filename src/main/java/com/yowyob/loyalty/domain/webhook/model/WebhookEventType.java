package com.yowyob.loyalty.domain.webhook.model;

public enum WebhookEventType {
    POINTS_EARNED("points.earned"),
    POINTS_REDEEMED("points.redeemed"),
    REWARD_GRANTED("reward.granted"),
    REWARD_REDEEMED("reward.redeemed"),
    TIER_CHANGED("tier.changed"),
    WEBHOOK_TEST("webhook.test");

    private final String code;

    WebhookEventType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static WebhookEventType fromCode(String code) {
        for (WebhookEventType type : values()) {
            if (type.code.equals(code)) return type;
        }
        throw new IllegalArgumentException("Type d'événement webhook inconnu : " + code);
    }
}
