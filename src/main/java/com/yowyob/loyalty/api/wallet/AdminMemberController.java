package com.yowyob.loyalty.api.wallet;

import com.yowyob.loyalty.api.wallet.dto.response.MemberSummaryResponse;
import com.yowyob.loyalty.domain.wallet.port.in.ListWalletsUseCase;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/admin/members")
@PreAuthorize("hasRole('TENANT_ADMIN')")
@Tag(name = "Members Directory", description = "Annuaire des membres (portefeuilles) du tenant")
public class AdminMemberController {

    private final ListWalletsUseCase listWalletsUseCase;

    public AdminMemberController(ListWalletsUseCase listWalletsUseCase) {
        this.listWalletsUseCase = listWalletsUseCase;
    }

    @GetMapping
    @Operation(summary = "Lister les membres du tenant", description = "Retourne un résumé (solde, statut) par membre, basé sur les portefeuilles enregistrés.")
    public Flux<MemberSummaryResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return TenantContextHolder.getTenantId()
                .flatMapMany(tenantId -> listWalletsUseCase.listWallets(tenantId, page, size))
                .map(MemberSummaryResponse::from);
    }
}
