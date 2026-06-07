package com.yowyob.loyalty.api.bonification;

import com.yowyob.loyalty.api.bonification.dto.BonificationStatusResponse;
import com.yowyob.loyalty.api.bonification.dto.BonificationTransactionResponse;
import com.yowyob.loyalty.api.bonification.dto.SubmitBonificationTransactionRequest;
import com.yowyob.loyalty.application.bonification.BonificationCredentialsResolver;
import com.yowyob.loyalty.application.bonification.handler.SubmitBonificationTransactionHandler;
import com.yowyob.loyalty.domain.bonification.model.BonificationTransactionRequest;
import com.yowyob.loyalty.domain.bonification.port.out.BonificationPort;
import com.yowyob.loyalty.infrastructure.bonification.config.BonificationProperties;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/bonification")
@Tag(name = "Bonification", description = "Intégration API bonification externe")
public class BonificationController {

    private final BonificationPort bonificationPort;
    private final BonificationProperties properties;
    private final BonificationCredentialsResolver credentialsResolver;
    private final SubmitBonificationTransactionHandler submitHandler;

    public BonificationController(
            BonificationPort bonificationPort,
            BonificationProperties properties,
            BonificationCredentialsResolver credentialsResolver,
            SubmitBonificationTransactionHandler submitHandler
    ) {
        this.bonificationPort = bonificationPort;
        this.properties = properties;
        this.credentialsResolver = credentialsResolver;
        this.submitHandler = submitHandler;
    }

    @GetMapping("/status")
    public Mono<BonificationStatusResponse> status() {
        if (!properties.isEnabled()) {
            return Mono.just(new BonificationStatusResponse(
                    false, false, properties.getBaseUrl(),
                    "Intégration désactivée (app.bonification.enabled=false)"
            ));
        }

        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> credentialsResolver.resolve(tenantId)
                        .flatMap(credentials -> {
                            if (!credentials.isConfigured()) {
                                return Mono.just(new BonificationStatusResponse(
                                        true, false, properties.getBaseUrl(),
                                        "Identifiants manquants : BONIFICATION_LOGIN/PASSWORD ou TenantConfig"
                                ));
                            }
                            return bonificationPort.verifyCredentials(credentials)
                                    .map(ok -> new BonificationStatusResponse(
                                            true,
                                            ok,
                                            properties.getBaseUrl(),
                                            ok ? "Authentification API Bonification OK" : "Authentification refusée"
                                    ));
                        }))
                .switchIfEmpty(bonificationPort.checkConnectivity()
                        .map(reachable -> new BonificationStatusResponse(
                                true,
                                reachable,
                                properties.getBaseUrl(),
                                reachable
                                        ? "API joignable (BONIFICATION_LOGIN/PASSWORD dans .env)"
                                        : "API injoignable ou credentials .env manquants"
                        )));
    }

    @PostMapping("/transactions")
    public Mono<BonificationTransactionResponse> submitTransaction(
            @Valid @RequestBody SubmitBonificationTransactionRequest request
    ) {
        BonificationTransactionRequest domainRequest = request.debit()
                ? BonificationTransactionRequest.debit(request.amount(), request.clientLogin())
                : BonificationTransactionRequest.credit(request.amount(), request.clientLogin());

        return submitHandler.submit(domainRequest)
                .map(result -> new BonificationTransactionResponse(
                        result.transactionId(),
                        result.amount(),
                        result.clientLogin(),
                        result.debit(),
                        result.status(),
                        result.message()
                ));
    }
}
