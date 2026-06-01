package com.yowyob.loyaulty.program.api.promo;

import com.yowyob.loyaulty.program.api.promo.dto.response.PromoValidationResponse;
import com.yowyob.loyaulty.program.domain.promo.port.in.ValidatePromoUseCase;
import com.yowyob.loyaulty.program.shared.multitenancy.TenantContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/promos")
public class PromoController {

    private final ValidatePromoUseCase promoUseCase;

    public PromoController(ValidatePromoUseCase promoUseCase) {
        this.promoUseCase = promoUseCase;
    }

    /** Vérifie un code promo avant confirmation de commande (lecture seule). */
    @GetMapping("/{code}/validate")
    public Mono<PromoValidationResponse> validate(
            @PathVariable String code,
            @RequestParam BigDecimal orderAmount) {
        return TenantContextHolder.getTenantId()
                .zipWith(resolveMemberId())
                .flatMap(t -> promoUseCase.validate(t.getT1(), code, t.getT2(), orderAmount))
                .map(discount -> new PromoValidationResponse(code, discount, "CALCULATED"));
    }

    /** Applique un code promo après confirmation de commande. */
    @PostMapping("/{code}/apply")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> apply(
            @PathVariable String code,
            @RequestParam BigDecimal orderAmount,
            @RequestParam(required = false) String orderReference) {
        return TenantContextHolder.getTenantId()
                .zipWith(resolveMemberId())
                .flatMap(t -> promoUseCase.apply(t.getT1(), code, t.getT2(),
                        orderAmount, orderReference != null ? orderReference : ""))
                .then();
    }

    private Mono<String> resolveMemberId() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getName());
    }
}
