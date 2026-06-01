package com.yowyob.loyaulty.program.shared.exception;

import com.yowyob.loyaulty.program.api.shared.dto.ProblemDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String BASE_TYPE = "https://loyalty.yowyob.com/errors/";

    @ExceptionHandler(AppException.class)
    public Mono<ResponseEntity<ProblemDetails>> handleAppException(
            AppException ex, ServerWebExchange exchange) {
        ProblemDetails body = ProblemDetails.of(
                BASE_TYPE + ex.getErrorCode().name().toLowerCase().replace('_', '-'),
                ex.getErrorCode().name(),
                ex.getHttpStatus().value(),
                ex.getDetail(),
                exchange.getRequest().getPath().value()
        );
        return Mono.just(ResponseEntity.status(ex.getHttpStatus()).body(body));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ProblemDetails>> handleValidation(
            WebExchangeBindException ex, ServerWebExchange exchange) {
        Map<String, Object> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = error instanceof FieldError fe ? fe.getField() : error.getObjectName();
            errors.put(field, error.getDefaultMessage());
        });
        ProblemDetails body = ProblemDetails.withErrors(
                BASE_TYPE + "validation-error",
                "Validation Error",
                HttpStatus.BAD_REQUEST.value(),
                "Request validation failed",
                exchange.getRequest().getPath().value(),
                errors
        );
        return Mono.just(ResponseEntity.badRequest().body(body));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ProblemDetails>> handleGeneric(
            Exception ex, ServerWebExchange exchange) {
        log.error("Unhandled exception on {}", exchange.getRequest().getPath().value(), ex);
        ProblemDetails body = ProblemDetails.of(
                BASE_TYPE + "internal-error",
                "Internal Server Error",
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "An unexpected error occurred",
                exchange.getRequest().getPath().value()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body));
    }
}
