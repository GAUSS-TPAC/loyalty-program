package com.yowyob.loyalty.api.system;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/public")
public class HealthController {

    @GetMapping("/health")
    public Mono<Map<String, String>> health() {
        return Mono.just(Map.of("status", "OK", "message", "Loyalty API is running"));
    }
}
