package com.yowyob.loyaulty.program.api.promo.dto.response;

import java.math.BigDecimal;

public record PromoValidationResponse(
        String code,
        BigDecimal discountAmount,
        String discountType
) {}
