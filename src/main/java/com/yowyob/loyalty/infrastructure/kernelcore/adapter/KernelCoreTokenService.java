package com.yowyob.loyalty.infrastructure.kernelcore.adapter;

import com.yowyob.loyalty.infrastructure.kernelcore.config.KernelCoreProperties;
import com.yowyob.loyalty.infrastructure.kernelcore.dto.KernelTokenResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Gère le token service-to-service (OAuth2 client_credentials) vers Kernel Core.
 * Le token est mis en cache en mémoire jusqu'à 30s avant son expiration.
 */
@Component
public class KernelCoreTokenService {

    private final KernelCoreProperties props;
    private final WebClient tokenWebClient;

    private final AtomicReference<String> cachedToken = new AtomicReference<>();
    private volatile Instant tokenExpiry = Instant.EPOCH;

    public KernelCoreTokenService(KernelCoreProperties props) {
        this.props = props;
        this.tokenWebClient = WebClient.builder()
                .defaultHeader("Content-Type", MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .build();
    }

    public Mono<String> getServiceToken() {
        if (!props.hasTokenEndpoint()) {
            return Mono.empty();
        }
        String current = cachedToken.get();
        if (current != null && Instant.now().isBefore(tokenExpiry.minusSeconds(30))) {
            return Mono.just(current);
        }
        return fetchNewToken();
    }

    private Mono<String> fetchNewToken() {
        return tokenWebClient.post()
                .uri(props.getTokenEndpoint())
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", props.getServiceClientId())
                        .with("client_secret", props.getServiceClientSecret()))
                .retrieve()
                .bodyToMono(KernelTokenResponse.class)
                .doOnNext(resp -> {
                    cachedToken.set(resp.getAccessToken());
                    tokenExpiry = Instant.now().plusSeconds(resp.getExpiresIn());
                })
                .map(KernelTokenResponse::getAccessToken);
    }
}
