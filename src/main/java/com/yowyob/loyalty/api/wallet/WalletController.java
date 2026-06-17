package com.yowyob.loyalty.api.wallet;

import com.yowyob.loyalty.api.wallet.dto.request.CreateWalletRequest;
import com.yowyob.loyalty.api.wallet.dto.request.CreditRequest;
import com.yowyob.loyalty.api.wallet.dto.request.DebitRequest;
import com.yowyob.loyalty.api.wallet.dto.request.FreezeRequest;
import com.yowyob.loyalty.api.wallet.dto.response.WalletResponse;
import com.yowyob.loyalty.api.wallet.dto.response.WalletTransactionResponse;
import com.yowyob.loyalty.domain.shared.model.UserId;
import com.yowyob.loyalty.domain.wallet.model.TransactionSource;
import com.yowyob.loyalty.domain.wallet.model.TransactionType;
import com.yowyob.loyalty.domain.wallet.port.in.*;
import com.yowyob.loyalty.domain.wallet.port.out.WalletPolicyRepository;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/members/{memberId}/wallet")
@Tag(name = "Wallet", description = "Portefeuille et soldes")
public class WalletController {

    private final CreateWalletUseCase createWalletUseCase;
    private final GetWalletUseCase getWalletUseCase;
    private final CreditWalletUseCase creditWalletUseCase;
    private final DebitWalletUseCase debitWalletUseCase;
    private final FreezeWalletUseCase freezeWalletUseCase;
    private final UnfreezeWalletUseCase unfreezeWalletUseCase;
    private final GetTransactionHistoryUseCase getTransactionHistoryUseCase;
    private final WalletPolicyRepository policyRepo;

    public WalletController(
            @Qualifier("createWalletHandler") CreateWalletUseCase createWalletUseCase,
            @Qualifier("walletDomainService") GetWalletUseCase getWalletUseCase,
            @Qualifier("creditWalletHandler") CreditWalletUseCase creditWalletUseCase,
            @Qualifier("debitWalletHandler") DebitWalletUseCase debitWalletUseCase,
            @Qualifier("freezeWalletHandler") FreezeWalletUseCase freezeWalletUseCase,
            @Qualifier("unfreezeWalletHandler") UnfreezeWalletUseCase unfreezeWalletUseCase,
            @Qualifier("walletDomainService") GetTransactionHistoryUseCase getTransactionHistoryUseCase,
            WalletPolicyRepository policyRepo) {
        this.createWalletUseCase = createWalletUseCase;
        this.getWalletUseCase = getWalletUseCase;
        this.creditWalletUseCase = creditWalletUseCase;
        this.debitWalletUseCase = debitWalletUseCase;
        this.freezeWalletUseCase = freezeWalletUseCase;
        this.unfreezeWalletUseCase = unfreezeWalletUseCase;
        this.getTransactionHistoryUseCase = getTransactionHistoryUseCase;
        this.policyRepo = policyRepo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<WalletResponse> createWallet(
            @PathVariable UUID memberId,
            @Valid @RequestBody CreateWalletRequest request) {
        UserId userId = UserId.of(memberId.toString());
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> createWalletUseCase.createWallet(
                        tenantId, userId, request.currencyCode(), request.autoActivate(), null)
                        .flatMap(wallet -> policyRepo.findByTenant(tenantId)
                                .map(policy -> WalletResponse.from(wallet, policy))));
    }

    @GetMapping
    public Mono<WalletResponse> getWallet(@PathVariable UUID memberId) {
        UserId userId = UserId.of(memberId.toString());
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> getWalletUseCase.getWallet(tenantId, userId)
                        .flatMap(wallet -> policyRepo.findByTenant(tenantId)
                                .map(policy -> WalletResponse.from(wallet, policy))));
    }

    @PostMapping("/credit")
    public Mono<WalletResponse> credit(
            @PathVariable UUID memberId,
            @Valid @RequestBody CreditRequest request) {
        UserId userId = UserId.of(memberId.toString());
        TransactionSource source = TransactionSource.valueOf(request.source());
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> creditWalletUseCase.credit(
                        tenantId, userId, request.amount(), source, request.referenceId(), request.idempotencyKey())
                        .flatMap(result -> policyRepo.findByTenant(tenantId)
                                .map(policy -> WalletResponse.from(result.updatedWallet(), policy))));
    }

    @PostMapping("/debit")
    public Mono<WalletResponse> debit(
            @PathVariable UUID memberId,
            @Valid @RequestBody DebitRequest request) {
        UserId userId = UserId.of(memberId.toString());
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> debitWalletUseCase.debit(
                        tenantId, userId, request.amount(), request.description(), request.orderReference(), null)
                        .flatMap(result -> policyRepo.findByTenant(tenantId)
                                .map(policy -> WalletResponse.from(result.updatedWallet(), policy))));
    }

    @PostMapping("/freeze")
    public Mono<WalletResponse> freeze(
            @PathVariable UUID memberId,
            @Valid @RequestBody FreezeRequest request) {
        UserId userId = UserId.of(memberId.toString());
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> freezeWalletUseCase.freeze(tenantId, userId, request.reason(), null)
                        .flatMap(wallet -> policyRepo.findByTenant(tenantId)
                                .map(policy -> WalletResponse.from(wallet, policy))));
    }

    @PostMapping("/unfreeze")
    public Mono<WalletResponse> unfreeze(@PathVariable UUID memberId) {
        UserId userId = UserId.of(memberId.toString());
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> unfreezeWalletUseCase.unfreeze(tenantId, userId, null)
                        .flatMap(wallet -> policyRepo.findByTenant(tenantId)
                                .map(policy -> WalletResponse.from(wallet, policy))));
    }

    @GetMapping("/transactions")
    public Flux<WalletTransactionResponse> getTransactions(
            @PathVariable UUID memberId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        UserId userId = UserId.of(memberId.toString());
        TransactionType typeFilter = type != null ? TransactionType.valueOf(type) : null;
        TransactionSource sourceFilter = source != null ? TransactionSource.valueOf(source) : null;
        return TenantContextHolder.getTenantId()
                .flatMapMany(tenantId -> getTransactionHistoryUseCase.getHistory(
                        tenantId, userId, typeFilter, sourceFilter, from, to, page, size))
                .map(WalletTransactionResponse::from);
    }
}
