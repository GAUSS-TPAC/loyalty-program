package com.yowyob.loyalty.shared.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@Order(-1000) // Extremely early filter to ensure tracking ID is present
public class RequestLoggingFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String requestId = exchange.getRequest().getHeaders().getFirst("X-Request-Id");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }
        
        final String finalRequestId = requestId;
        exchange.getAttributes().put("requestId", finalRequestId);

        long startTime = System.currentTimeMillis();

        return chain.filter(exchange)
                .doOnSuccess(v -> log.info("Request [{}] {} completed in {} ms", exchange.getRequest().getMethod(), exchange.getRequest().getURI().getPath(), (System.currentTimeMillis() - startTime)))
                .doOnError(e -> log.error("Request [{}] {} failed in {} ms", exchange.getRequest().getMethod(), exchange.getRequest().getURI().getPath(), (System.currentTimeMillis() - startTime), e))
                .contextWrite(ctx -> ctx.put("requestId", finalRequestId));
    }
}
