package com.yowyob.loyalty.application.bonification.handler;

import com.yowyob.loyalty.application.bonification.BonificationCredentialsResolver;
import com.yowyob.loyalty.domain.bonification.model.BonificationTransactionRequest;
import com.yowyob.loyalty.domain.bonification.model.BonificationTransactionResult;
import com.yowyob.loyalty.domain.bonification.port.out.BonificationPort;
import com.yowyob.loyalty.domain.loyalty.model.event.IncomingEvent;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SubmitBonificationTransactionHandler {

    private final BonificationPort bonificationPort;
    private final BonificationCredentialsResolver credentialsResolver;

    public SubmitBonificationTransactionHandler(
            BonificationPort bonificationPort,
            BonificationCredentialsResolver credentialsResolver
    ) {
        this.bonificationPort = bonificationPort;
        this.credentialsResolver = credentialsResolver;
    }

    public Mono<BonificationTransactionResult> submit(BonificationTransactionRequest request) {
        return TenantContextHolder.getTenantId()
                .flatMap((TenantId tenantId) -> credentialsResolver.resolve(tenantId)
                        .flatMap(credentials -> bonificationPort.submitTransaction(tenantId, credentials, request)));
    }

    public Mono<BonificationTransactionResult> submitFromLoyaltyEvent(IncomingEvent event) {
        return mapEventToRequest(event)
                .flatMap(this::submit);
    }

    private Mono<BonificationTransactionRequest> mapEventToRequest(IncomingEvent event) {
        String clientLogin = event.getPayloadString("clientLogin")
                .or(() -> event.getPayloadString("client_login"))
                .orElseGet(() -> event.memberId().value().toString());

        return event.getPayloadDecimal("amount")
                .map(amount -> {
                    boolean debit = event.getPayloadValue("isDebit")
                            .map(v -> Boolean.parseBoolean(v.toString()))
                            .orElse(false);
                    BonificationTransactionRequest request = debit
                            ? BonificationTransactionRequest.debit(amount.doubleValue(), clientLogin)
                            : BonificationTransactionRequest.credit(amount.doubleValue(), clientLogin);
                    return request;
                })
                .map(Mono::just)
                .orElseGet(() -> Mono.error(new IllegalArgumentException(
                        "Le champ payload.amount est requis pour transmettre l'événement à l'API Bonification")));
    }

    public Mono<BonificationTransactionResult> submitForTenant(
            TenantId tenantId,
            BonificationTransactionRequest request
    ) {
        return credentialsResolver.resolve(tenantId)
                .flatMap(credentials -> bonificationPort.submitTransaction(tenantId, credentials, request));
    }
}
