package com.yowyob.loyalty.domain.wallet.port.out;

import com.yowyob.loyalty.domain.wallet.model.OtpChallenge;
import reactor.core.publisher.Mono;

import java.time.Duration;

public interface OtpChallengePort {
    Mono<Void> store(OtpChallenge challenge, Duration ttl);
    Mono<OtpChallenge> findById(String challengeId);
    Mono<Void> delete(String challengeId);
}
