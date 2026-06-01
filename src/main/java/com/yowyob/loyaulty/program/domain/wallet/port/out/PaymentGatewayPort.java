package com.yowyob.loyaulty.program.domain.wallet.port.out;

import com.yowyob.loyaulty.program.domain.wallet.model.enums.PaymentProvider;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

public interface PaymentGatewayPort {

    PaymentProvider getProvider();

    Mono<PaymentResult> initiatePayment(String memberId, String phoneNumber,
                                         BigDecimal amount, String currency,
                                         String reference);

    Mono<PaymentResult> initiateWithdrawal(String memberId, String phoneNumber,
                                            BigDecimal amount, String currency,
                                            String reference);

    record PaymentResult(
            boolean success,
            String externalRef,
            String rawResponse,
            String errorMessage
    ) {
        public static PaymentResult ok(String externalRef, String rawResponse) {
            return new PaymentResult(true, externalRef, rawResponse, null);
        }

        public static PaymentResult failed(String errorMessage, String rawResponse) {
            return new PaymentResult(false, null, rawResponse, errorMessage);
        }
    }
}
