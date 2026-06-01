package com.yowyob.loyaulty.program.domain.loyalty.model;

import java.util.List;

public record TransactionHistory(
        List<BonificationTransaction> transactions,
        Integer totalCount,
        String memberId
) {}
