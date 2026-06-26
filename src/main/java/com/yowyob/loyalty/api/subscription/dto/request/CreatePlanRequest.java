package com.yowyob.loyalty.api.subscription.dto.request;

import java.math.BigDecimal;

public record CreatePlanRequest(
        String code,
        String name,
        String description,
        BigDecimal priceMonthly,
        BigDecimal priceYearly,
        String currency,
        int maxRules,
        int maxMembers,
        int maxEventsPerMonth,
        boolean referralEnabled,
        boolean campaignsEnabled,
        boolean promoCodesEnabled,
        boolean analyticsEnabled
) {}
