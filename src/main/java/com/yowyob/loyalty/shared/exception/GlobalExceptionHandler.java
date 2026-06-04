package com.yowyob.loyalty.shared.exception;

import com.yowyob.loyalty.api.shared.dto.ProblemDetails;
import com.yowyob.loyalty.domain.bonification.exception.BonificationException;
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
