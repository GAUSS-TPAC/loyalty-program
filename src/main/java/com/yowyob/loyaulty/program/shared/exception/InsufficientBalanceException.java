package com.yowyob.loyaulty.program.shared.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends AppException {
    public InsufficientBalanceException(BigDecimal available, BigDecimal required) {
        super(ErrorCode.INSUFFICIENT_BALANCE,
                "Insufficient balance: available=" + available + ", required=" + required);
    }
}
