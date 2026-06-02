package com.yowyob.loyalty.api.wallet;

import com.yowyob.loyalty.api.wallet.dto.request.DebitRequest;
import com.yowyob.loyalty.api.wallet.dto.request.TopUpRequest;
import com.yowyob.loyalty.api.wallet.dto.response.WalletResponse;
import com.yowyob.loyalty.domain.shared.model.TenantId;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.port.in.GetWalletUseCase;
import com.yowyob.loyalty.domain.wallet.port.out.WalletPolicyRepository;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {
    
    private final GetWalletUseCase getWalletUseCase;
    private final WalletPolicyRepository policyRepo;

    public WalletController(GetWalletUseCase getWalletUseCase, WalletPolicyRepository policyRepo) {
        this.getWalletUseCase = getWalletUseCase;
        this.policyRepo = policyRepo;
    }

    @GetMapping
    public Mono<WalletResponse> getWallet() {
        // En vrai: extraire tenantId et memberId du SecurityContext / TenantContextHolder
        // Pour l'exemple on simule ou on utilise des stubs si dispo
        TenantId tenantId = TenantId.of("00000000-0000-0000-0000-000000000001");
        UserId userId = UserId.of("00000000-0000-0000-0000-000000000002");
        
        return getWalletUseCase.getWallet(tenantId, userId)
            .flatMap(wallet -> policyRepo.findByTenant(tenantId)
                .map(policy -> WalletResponse.from(wallet, policy)));
    }
}
