package com.yowyob.loyaulty.program.infrastructure.payment.mtn;

import com.yowyob.loyaulty.program.domain.wallet.model.enums.PaymentProvider;
import com.yowyob.loyaulty.program.domain.wallet.port.out.PaymentGatewayPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class MtnMobileMoneyAdapter implements PaymentGatewayPort {

    private static final Logger log = LoggerFactory.getLogger(MtnMobileMoneyAdapter.class);

    @Override
    public PaymentProvider getProvider() {
        return PaymentProvider.MTN;
    }

    @Override
    public Mono<PaymentResult> initiatePayment(String memberId, String phoneNumber,
                                                BigDecimal amount, String currency,
                                                String reference) {
        log.info("[MTN-STUB] Initiating payment: member={}, phone={}, amount={} {}",
                memberId, phoneNumber, amount, currency);
        String externalRef = "MTN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return Mono.just(PaymentResult.ok(externalRef, "{\"status\":\"PENDING\",\"provider\":\"MTN\"}"));
    }

    @Override
    public Mono<PaymentResult> initiateWithdrawal(String memberId, String phoneNumber,
                                                   BigDecimal amount, String currency,
                                                   String reference) {
        log.info("[MTN-STUB] Initiating withdrawal: member={}, phone={}, amount={} {}",
                memberId, phoneNumber, amount, currency);
        String externalRef = "MTN-W-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return Mono.just(PaymentResult.ok(externalRef, "{\"status\":\"PENDING\",\"provider\":\"MTN\"}"));
    }
}
