package com.yowyob.loyaulty.program.api.wallet;

import com.yowyob.loyaulty.program.api.wallet.dto.request.FreezeRequest;
import com.yowyob.loyaulty.program.api.wallet.dto.response.WalletBalanceResponse;
import com.yowyob.loyaulty.program.domain.wallet.port.in.CloseWalletUseCase;
import com.yowyob.loyaulty.program.domain.wallet.port.in.FreezeWalletUseCase;
import com.yowyob.loyaulty.program.domain.wallet.port.in.GetWalletBalanceUseCase;
import com.yowyob.loyaulty.program.shared.multitenancy.TenantContextHolder;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/admin/wallets")
public class WalletAdminController {

    private final FreezeWalletUseCase freezeUseCase;
    private final CloseWalletUseCase closeUseCase;
    private final GetWalletBalanceUseCase balanceUseCase;

    public WalletAdminController(FreezeWalletUseCase freezeUseCase,
                                  CloseWalletUseCase closeUseCase,
                                  GetWalletBalanceUseCase balanceUseCase) {
        this.freezeUseCase = freezeUseCase;
        this.closeUseCase = closeUseCase;
        this.balanceUseCase = balanceUseCase;
    }

    @GetMapping("/{memberId}")
    public Mono<WalletBalanceResponse> getWallet(@PathVariable String memberId) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> balanceUseCase.getWallet(tenantId, memberId))
                .map(WalletBalanceResponse::from);
    }

    @PostMapping("/{memberId}/freeze")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> freeze(@PathVariable String memberId,
                              @Valid @RequestBody FreezeRequest request) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> freezeUseCase.freeze(tenantId, memberId, request.reason()));
    }

    @PostMapping("/{memberId}/unfreeze")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> unfreeze(@PathVariable String memberId) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> freezeUseCase.unfreeze(tenantId, memberId));
    }

    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> close(@PathVariable String memberId) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> closeUseCase.close(tenantId, memberId));
    }
}
