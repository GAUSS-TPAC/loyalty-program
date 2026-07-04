package com.yowyob.loyalty.api.loyalty;

import com.yowyob.loyalty.api.loyalty.dto.response.PointsTransactionLogResponse;
import com.yowyob.loyalty.domain.loyalty.port.in.GetPointsLedgerUseCase;
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
@RequestMapping("/api/v1/admin/points-transactions")
@PreAuthorize("hasRole('TENANT_ADMIN')")
@Tag(name = "Logs", description = "Journal des transactions de points du tenant (résultat du moteur de règles)")
public class AdminLogsController {

    private final GetPointsLedgerUseCase getPointsLedgerUseCase;

    public AdminLogsController(GetPointsLedgerUseCase getPointsLedgerUseCase) {
        this.getPointsLedgerUseCase = getPointsLedgerUseCase;
    }

    @GetMapping
    @Operation(summary = "Journal des transactions de points du tenant", description = "Historique tenant-wide des crédits/débits de points, résultat des événements traités par le moteur de règles.")
    public Flux<PointsTransactionLogResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return TenantContextHolder.getTenantId()
                .flatMapMany(tenantId -> Flux.fromIterable(getPointsLedgerUseCase.getTenantLedger(tenantId, page, size)))
                .map(PointsTransactionLogResponse::from);
    }
}
