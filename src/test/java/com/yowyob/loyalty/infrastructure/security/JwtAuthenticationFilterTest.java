package com.yowyob.loyalty.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class JwtAuthenticationFilterTest {

    private SecurityContextRepository repository;

    @BeforeEach
    public void setup() {
        repository = Mockito.mock(SecurityContextRepository.class);
    }

    @Test
    public void testLoadContextDelegatesToRepository() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/")
                .header("Authorization", "Bearer token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        
        JwtAuthenticationToken mockToken = Mockito.mock(JwtAuthenticationToken.class);
        SecurityContext context = new SecurityContextImpl(mockToken);
        Mockito.when(repository.load(exchange)).thenReturn(Mono.just(context));

        StepVerifier.create(repository.load(exchange))
                .expectNextMatches(ctx -> ctx.getAuthentication() != null)
                .verifyComplete();
    }
}
