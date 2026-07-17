package com.yowyob.loyalty.shared.exception;

import com.yowyob.loyalty.api.shared.dto.ProblemDetails;
import com.yowyob.loyalty.domain.bonification.exception.BonificationException;
import com.yowyob.loyalty.domain.campaign.exception.CampaignDomainException;
import com.yowyob.loyalty.domain.campaign.exception.CampaignNotFoundException;
import com.yowyob.loyalty.domain.loyalty.exception.LoyaltyDomainException;
import com.yowyob.loyalty.domain.promo.exception.*;
import com.yowyob.loyalty.domain.subscription.exception.*;
import com.yowyob.loyalty.domain.tenant.exception.ApplicationDomainException;
import com.yowyob.loyalty.domain.tenant.exception.ApplicationNotFoundException;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BonificationException.class)
    public Mono<ResponseEntity<ProblemDetails>> handleBonificationException(BonificationException ex, ServerWebExchange exchange) {
        String requestId = extractRequestId(exchange);
        ProblemDetails problemDetails = new ProblemDetails(
                "https://loyalty.yowyob.com/errors/bonification_unavailable",
                ErrorCode.BONIFICATION_UNAVAILABLE.name(),
                HttpStatus.BAD_GATEWAY.value(),
                ex.getMessage(),
                requestId,
                Instant.now(),
                null
        );
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(problemDetails));
    }

    @ExceptionHandler(AppException.class)
    public Mono<ResponseEntity<ProblemDetails>> handleAppException(AppException ex, ServerWebExchange exchange) {
        String requestId = extractRequestId(exchange);
        ProblemDetails problemDetails = ProblemDetails.from(ex, requestId);
        return Mono.just(ResponseEntity.status(ex.getHttpStatus()).body(problemDetails));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ProblemDetails>> handleWebExchangeBindException(WebExchangeBindException ex, ServerWebExchange exchange) {
        String requestId = extractRequestId(exchange);
        Map<String, Object> fieldErrors = ex.getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        err -> err.getDefaultMessage() != null ? err.getDefaultMessage() : "Invalid value",
                        (msg1, msg2) -> msg1 + ", " + msg2
                ));

        ProblemDetails problemDetails = new ProblemDetails(
                "https://loyalty.yowyob.com/errors/validation_error",
                ErrorCode.VALIDATION_ERROR.name(),
                HttpStatus.BAD_REQUEST.value(),
                "Erreur de validation des champs",
                requestId,
                Instant.now(),
                fieldErrors
        );

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetails));
    }

    @ExceptionHandler(CampaignDomainException.class)
    public Mono<ResponseEntity<ProblemDetails>> handleCampaignDomainException(CampaignDomainException ex, ServerWebExchange exchange) {
        String requestId = extractRequestId(exchange);
        ErrorCode code = ex instanceof CampaignNotFoundException
                ? ErrorCode.CAMPAIGN_NOT_FOUND
                : ErrorCode.CAMPAIGN_INVALID_TRANSITION;
        ProblemDetails problemDetails = new ProblemDetails(
                "https://loyalty.yowyob.com/errors/" + code.name().toLowerCase(),
                code.name(), code.getHttpStatus(), ex.getMessage(), requestId, Instant.now(), null
        );
        return Mono.just(ResponseEntity.status(code.getHttpStatus()).body(problemDetails));
    }

    @ExceptionHandler(PromoDomainException.class)
    public Mono<ResponseEntity<ProblemDetails>> handlePromoDomainException(PromoDomainException ex, ServerWebExchange exchange) {
        String requestId = extractRequestId(exchange);
        ErrorCode code;
        if (ex instanceof PromoCampaignNotFoundException) code = ErrorCode.PROMO_CAMPAIGN_NOT_FOUND;
        else if (ex instanceof PromoNotActiveException)    code = ErrorCode.PROMO_NOT_ACTIVE;
        else if (ex instanceof PromoNotStartedException)   code = ErrorCode.PROMO_NOT_STARTED;
        else if (ex instanceof PromoExpiredException)      code = ErrorCode.PROMO_EXPIRED;
        else if (ex instanceof PromoExhaustedException)    code = ErrorCode.PROMO_EXHAUSTED;
        else if (ex instanceof PromoAlreadyUsedException)  code = ErrorCode.PROMO_ALREADY_USED;
        else if (ex instanceof PromoMinOrderAmountException) code = ErrorCode.PROMO_MIN_ORDER_AMOUNT_NOT_MET;
        else code = ErrorCode.VALIDATION_ERROR;

        ProblemDetails problemDetails = new ProblemDetails(
                "https://loyalty.yowyob.com/errors/" + code.name().toLowerCase(),
                code.name(),
                code.getHttpStatus(),
                ex.getMessage(),
                requestId,
                Instant.now(),
                null
        );
        return Mono.just(ResponseEntity.status(code.getHttpStatus()).body(problemDetails));
    }

    @ExceptionHandler(ApplicationDomainException.class)
    public Mono<ResponseEntity<ProblemDetails>> handleApplicationDomainException(ApplicationDomainException ex, ServerWebExchange exchange) {
        String requestId = extractRequestId(exchange);
        ErrorCode code = ex instanceof ApplicationNotFoundException
                ? ErrorCode.RESOURCE_NOT_FOUND
                : ErrorCode.VALIDATION_ERROR;
        ProblemDetails problemDetails = new ProblemDetails(
                "https://loyalty.yowyob.com/errors/" + code.name().toLowerCase(),
                code.name(), code.getHttpStatus(), ex.getMessage(), requestId, Instant.now(), null
        );
        return Mono.just(ResponseEntity.status(code.getHttpStatus()).body(problemDetails));
    }

    @ExceptionHandler(LoyaltyDomainException.class)
    public Mono<ResponseEntity<ProblemDetails>> handleLoyaltyDomainException(LoyaltyDomainException ex, ServerWebExchange exchange) {
        String requestId = extractRequestId(exchange);
        ProblemDetails problemDetails = new ProblemDetails(
                "https://loyalty.yowyob.com/errors/loyalty_rule_violation",
                "LOYALTY_RULE_VIOLATION",
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                ex.getMessage(),
                requestId,
                Instant.now(),
                ex.getDetails()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problemDetails));
    }

    @ExceptionHandler(SubscriptionDomainException.class)
    public Mono<ResponseEntity<ProblemDetails>> handleSubscriptionDomainException(SubscriptionDomainException ex, ServerWebExchange exchange) {
        String requestId = extractRequestId(exchange);
        ErrorCode code;
        if (ex instanceof PlanNotFoundException)                     code = ErrorCode.PLAN_NOT_FOUND;
        else if (ex instanceof SubscriptionNotFoundException)        code = ErrorCode.SUBSCRIPTION_NOT_FOUND;
        else if (ex instanceof AlreadySubscribedException)           code = ErrorCode.ALREADY_SUBSCRIBED;
        else if (ex instanceof SubscriptionAlreadyTerminalException) code = ErrorCode.SUBSCRIPTION_ALREADY_TERMINAL;
        else                                                         code = ErrorCode.INTERNAL_ERROR;

        ProblemDetails problemDetails = new ProblemDetails(
                "https://loyalty.yowyob.com/errors/" + code.name().toLowerCase(),
                code.name(), code.getHttpStatus(), ex.getMessage(), requestId, Instant.now(), null
        );
        return Mono.just(ResponseEntity.status(code.getHttpStatus()).body(problemDetails));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public Mono<ResponseEntity<ProblemDetails>> handleResponseStatusException(ResponseStatusException ex, ServerWebExchange exchange) {
        // Covers framework-level rejections (e.g. a @PathVariable UUID that fails to parse)
        // which already carry their own HTTP status -- must not be downgraded to a 500 by
        // the generic handler below.
        String requestId = extractRequestId(exchange);
        ProblemDetails problemDetails = new ProblemDetails(
                "https://loyalty.yowyob.com/errors/bad_request",
                "BAD_REQUEST",
                ex.getStatusCode().value(),
                ex.getReason() != null ? ex.getReason() : "Requête invalide",
                requestId,
                Instant.now(),
                null
        );
        return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(problemDetails));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ProblemDetails>> handleGenericException(Exception ex, ServerWebExchange exchange) {
        String requestId = extractRequestId(exchange);
        log.error("Unhandled exception [requestId: {}]", requestId, ex);

        ProblemDetails problemDetails = new ProblemDetails(
                "https://loyalty.yowyob.com/errors/internal_error",
                ErrorCode.INTERNAL_ERROR.name(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Une erreur interne inattendue s'est produite.",
                requestId,
                Instant.now(),
                null
        );

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetails));
    }

    private String extractRequestId(ServerWebExchange exchange) {
        Object requestIdObj = exchange.getAttributes().get("requestId");
        return requestIdObj != null ? requestIdObj.toString() : "unknown";
    }
}
