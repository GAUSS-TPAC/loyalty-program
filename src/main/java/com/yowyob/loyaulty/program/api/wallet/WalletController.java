package com.yowyob.loyaulty.program.api.wallet;

import com.yowyob.loyaulty.program.api.wallet.dto.request.DebitRequest;
import com.yowyob.loyaulty.program.api.wallet.dto.request.TopUpRequest;
import com.yowyob.loyaulty.program.api.wallet.dto.response.TransactionPageResponse;
import com.yowyob.loyaulty.program.api.wallet.dto.response.TransactionResponse;
import com.yowyob.loyaulty.program.api.wallet.dto.response.WalletBalanceResponse;
import com.yowyob.loyaulty.program.domain.shared.model.PageRequest;
import com.yowyob.loyaulty.program.domain.wallet.model.enums.TransactionSource;
import com.yowyob.loyaulty.program.domain.wallet.port.in.*;
import com.yowyob.loyaulty.program.shared.multitenancy.TenantContextHolder;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallet")
public class WalletController {

    private final CreditWalletUseCase creditUseCase;
    private final DebitWalletUseCase debitUseCase;
    private final GetWalletBalanceUseCase balanceUseCase;
    private final GetTransactionHistoryUseCase historyUseCase;
    private final ReverseTransactionUseCase reverseUseCase;

    public WalletController(CreditWalletUseCase creditUseCase,
                             DebitWalletUseCase debitUseCase,
                             GetWalletBalanceUseCase balanceUseCase,
                             GetTransactionHistoryUseCase historyUseCase,
                             ReverseTransactionUseCase reverseUseCase) {
        this.creditUseCase = creditUseCase;
        this.debitUseCase = debitUseCase;
        this.balanceUseCase = balanceUseCase;
        this.historyUseCase = historyUseCase;
        this.reverseUseCase = reverseUseCase;
    }

    @GetMapping("/balance")
    public Mono<WalletBalanceResponse> getBalance() {
        return TenantContextHolder.getTenantId()
                .zipWith(resolveMemberId())
                .flatMap(t -> balanceUseCase.getWallet(t.getT1(), t.getT2()))
                .map(WalletBalanceResponse::from);
    }

    @PostMapping("/topup")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TransactionResponse> topUp(@Valid @RequestBody TopUpRequest request,
                                            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        TransactionSource source = resolveTopUpSource(request.provider());
        return TenantContextHolder.getTenantId()
                .zipWith(resolveMemberId())
                .flatMap(t -> creditUseCase.credit(t.getT1(), t.getT2(), request.amount(), source, idempotencyKey))
                .map(TransactionResponse::from);
    }

    @PostMapping("/debit")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TransactionResponse> debit(@Valid @RequestBody DebitRequest request,
                                            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return TenantContextHolder.getTenantId()
                .zipWith(resolveMemberId())
                .flatMap(t -> debitUseCase.debit(t.getT1(), t.getT2(), request.amount(),
                        TransactionSource.PURCHASE, idempotencyKey))
                .map(TransactionResponse::from);
    }

    @GetMapping("/transactions")
    public Mono<TransactionPageResponse> getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return TenantContextHolder.getTenantId()
                .zipWith(resolveMemberId())
                .flatMap(t -> historyUseCase.getHistory(t.getT1(), t.getT2(), pageRequest))
                .map(TransactionPageResponse::from);
    }

    @PostMapping("/transactions/{transactionId}/reverse")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<TransactionResponse> reverseTransaction(
            @PathVariable UUID transactionId,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return TenantContextHolder.getTenantId()
                .zipWith(resolveMemberId())
                .flatMap(t -> reverseUseCase.reverse(t.getT1(), t.getT2(), transactionId, idempotencyKey))
                .map(TransactionResponse::from);
    }

    private Mono<String> resolveMemberId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName());
    }

    private TransactionSource resolveTopUpSource(String provider) {
        if (provider == null) return TransactionSource.MANUAL_CREDIT;
        return switch (provider.toUpperCase()) {
            case "MTN"    -> TransactionSource.TOPUP_MTN;
            case "ORANGE" -> TransactionSource.TOPUP_ORANGE;
            case "STRIPE" -> TransactionSource.TOPUP_STRIPE;
            default       -> TransactionSource.MANUAL_CREDIT;
        };
    }
}
