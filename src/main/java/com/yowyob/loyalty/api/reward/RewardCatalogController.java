package com.yowyob.loyalty.api.reward;

import com.yowyob.loyalty.api.reward.dto.request.CreateRewardRequest;
import com.yowyob.loyalty.api.reward.dto.request.UpdateRewardRequest;
import com.yowyob.loyalty.api.reward.dto.response.RewardResponse;
import com.yowyob.loyalty.domain.reward.model.RewardType;
import com.yowyob.loyalty.domain.reward.model.RewardValue;
import com.yowyob.loyalty.domain.reward.port.in.CreateRewardUseCase;
import com.yowyob.loyalty.domain.reward.port.in.GetRewardCatalogUseCase;
import com.yowyob.loyalty.domain.reward.port.in.UpdateRewardUseCase;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@Tag(name = "Rewards Catalog", description = "Gestion du catalogue de récompenses")
public class RewardCatalogController {

    private final CreateRewardUseCase createRewardUseCase;
    private final UpdateRewardUseCase updateRewardUseCase;
    private final GetRewardCatalogUseCase getCatalogUseCase;

    public RewardCatalogController(CreateRewardUseCase createRewardUseCase,
                                    UpdateRewardUseCase updateRewardUseCase,
                                    GetRewardCatalogUseCase getCatalogUseCase) {
        this.createRewardUseCase = createRewardUseCase;
        this.updateRewardUseCase = updateRewardUseCase;
        this.getCatalogUseCase = getCatalogUseCase;
    }

    @PostMapping("/api/v1/admin/rewards")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<RewardResponse> createReward(
            @Valid @RequestBody CreateRewardRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> {
                    int mac = request.maxApplicationCount() < 1 ? 1 : request.maxApplicationCount();
                    RewardValue value = new RewardValue(request.numericValue(), request.valueUnit(), mac);
                    return createRewardUseCase.createReward(
                            tenantId, request.name(), request.description(),
                            RewardType.valueOf(request.type()), value,
                            request.costInPoints(), request.stockTotal(),
                            request.validFrom(), request.validUntil(),
                            request.grantExpiryDays(), request.imageUrl(),
                            request.metadata(), idempotencyKey);
                })
                .map(RewardResponse::from);
    }

    @GetMapping("/api/v1/admin/rewards")
    public Flux<RewardResponse> getCatalogAdmin(
            @RequestParam(defaultValue = "true") boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return TenantContextHolder.getTenantId()
                .flatMapMany(tenantId -> getCatalogUseCase.getCatalog(tenantId, activeOnly, page, size))
                .map(RewardResponse::from);
    }

    @GetMapping("/api/v1/admin/rewards/{rewardId}")
    public Mono<RewardResponse> getRewardAdmin(@PathVariable UUID rewardId) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> getCatalogUseCase.getReward(tenantId, rewardId))
                .map(RewardResponse::from);
    }

    @PatchMapping("/api/v1/admin/rewards/{rewardId}/activate")
    public Mono<RewardResponse> activateReward(@PathVariable UUID rewardId) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> updateRewardUseCase.activateReward(tenantId, rewardId))
                .map(RewardResponse::from);
    }

    @PatchMapping("/api/v1/admin/rewards/{rewardId}/pause")
    public Mono<RewardResponse> pauseReward(@PathVariable UUID rewardId) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> updateRewardUseCase.pauseReward(tenantId, rewardId))
                .map(RewardResponse::from);
    }

    @PatchMapping("/api/v1/admin/rewards/{rewardId}/archive")
    public Mono<RewardResponse> archiveReward(@PathVariable UUID rewardId) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> updateRewardUseCase.archiveReward(tenantId, rewardId))
                .map(RewardResponse::from);
    }

    @PutMapping("/api/v1/admin/rewards/{rewardId}")
    public Mono<RewardResponse> updateReward(@PathVariable UUID rewardId,
                                              @RequestBody UpdateRewardRequest request) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> updateRewardUseCase.updateReward(
                        tenantId, rewardId, request.name(), request.description(),
                        request.imageUrl(), request.metadata()))
                .map(RewardResponse::from);
    }

    @GetMapping("/api/v1/rewards")
    public Flux<RewardResponse> getMemberCatalog(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return TenantContextHolder.getTenantId()
                .flatMapMany(tenantId -> getCatalogUseCase.getCatalog(tenantId, true, page, size))
                .map(RewardResponse::from);
    }
}
