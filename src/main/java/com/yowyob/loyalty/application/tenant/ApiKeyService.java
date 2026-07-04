package com.yowyob.loyalty.application.tenant;

import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.tenant.model.ApiKey;
import com.yowyob.loyalty.domain.tenant.model.enums.ApiKeyMode;
import com.yowyob.loyalty.domain.tenant.port.out.ApiKeyRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Service
public class ApiKeyService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final ApiKeyRepository repository;

    public ApiKeyService(ApiKeyRepository repository) {
        this.repository = repository;
    }

    public record CreatedKey(ApiKey record, String rawKey) {}

    public Mono<CreatedKey> create(TenantId tenantId, String name, ApiKeyMode mode) {
        ApiKeyMode effectiveMode = mode != null ? mode : ApiKeyMode.LIVE;
        String raw = generateRawKey(effectiveMode);
        String prefix = raw.substring(0, Math.min(raw.length(), 12));
        String hash = sha256(raw);
        ApiKey key = ApiKey.create(tenantId, name, hash, prefix, effectiveMode);
        return repository.save(key).map(saved -> new CreatedKey(saved, raw));
    }

    public Flux<ApiKey> listForTenant(TenantId tenantId) {
        return repository.findByTenantId(tenantId);
    }

    public Mono<Void> revoke(TenantId tenantId, UUID keyId) {
        return repository.findByTenantId(tenantId)
                .filter(k -> k.id().equals(keyId))
                .next()
                .flatMap(k -> repository.save(k.revoke()))
                .then();
    }

    public Mono<ApiKey> validate(String rawKey) {
        String hash = sha256(rawKey);
        return repository.findByKeyHash(hash)
                .flatMap(k -> repository.save(k.markUsed()));
    }

    private static String generateRawKey(ApiKeyMode mode) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String modePrefix = mode == ApiKeyMode.TEST ? "lk_test_" : "lk_live_";
        return modePrefix + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 unavailable", e);
        }
    }
}
