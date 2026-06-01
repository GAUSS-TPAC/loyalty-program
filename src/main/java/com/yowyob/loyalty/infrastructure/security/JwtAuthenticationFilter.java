package com.yowyob.loyalty.infrastructure.security;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter extends AuthenticationWebFilter {

    public JwtAuthenticationFilter(SecurityContextRepository securityContextRepository) {
        super((ReactiveAuthenticationManager) authentication -> Mono.just(authentication));
        setSecurityContextRepository(securityContextRepository);
    }
}
