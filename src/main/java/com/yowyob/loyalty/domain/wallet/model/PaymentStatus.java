package com.yowyob.loyalty.domain.wallet.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public enum PaymentStatus {
    INITIATED,
    PENDING,
    COMPLETED,
    FAILED,
    EXPIRED
}
