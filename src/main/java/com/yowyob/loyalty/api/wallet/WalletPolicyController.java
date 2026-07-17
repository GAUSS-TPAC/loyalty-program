package com.yowyob.loyalty.api.wallet;

import com.yowyob.loyalty.api.wallet.dto.request.UpdatePointsConversionRequest;
import com.yowyob.loyalty.api.wallet.dto.response.WalletPolicyResponse;
import com.yowyob.loyalty.domain.wallet.port.in.UpdateWalletPolicyUseCase;
import com.yowyob.loyalty.domain.wallet.port.out.WalletPolicyRepository;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/wallet/policy")
@Tag(name = "Wallet Policy", description = "Politique wallet du tenant (correspondance des points, limites)")
public class WalletPolicyController {

    private final WalletPolicyRepository policyRepo;
    private final UpdateWalletPolicyUseCase updateWalletPolicyUseCase;

    public WalletPolicyController(
            WalletPolicyRepository policyRepo,
            UpdateWalletPolicyUseCase updateWalletPolicyUseCase) {
        this.policyRepo = policyRepo;
        this.updateWalletPolicyUseCase = updateWalletPolicyUseCase;
    }

    @GetMapping
    public Mono<WalletPolicyResponse> getPolicy() {
        return TenantContextHolder.getTenantId()
                .flatMap(policyRepo::findByTenant)
                .map(WalletPolicyResponse::from);
    }

    @PutMapping("/points-conversion")
    @PreAuthorize("hasRole('TENANT_ADMIN')")
    public Mono<WalletPolicyResponse> updatePointsConversion(
            @Valid @RequestBody UpdatePointsConversionRequest request) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> updateWalletPolicyUseCase.updatePointsConversion(
                        tenantId, request.currencyName(), request.currencySymbol(), request.exchangeRate()))
                .map(WalletPolicyResponse::from);
    }
}
