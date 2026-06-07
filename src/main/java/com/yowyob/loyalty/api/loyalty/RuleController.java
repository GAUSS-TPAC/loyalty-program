package com.yowyob.loyalty.api.loyalty;

import com.yowyob.loyalty.api.loyalty.dto.request.CreateRuleRequest;
import com.yowyob.loyalty.api.loyalty.dto.response.RuleResponse;
import com.yowyob.loyalty.application.loyalty.handler.ActivateRuleHandler;
import com.yowyob.loyalty.application.loyalty.handler.CreateRuleHandler;
import com.yowyob.loyalty.domain.loyalty.port.out.RuleRepository;
import com.yowyob.loyalty.shared.multitenancy.TenantContextHolder;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/rules")
@Tag(name = "Rules", description = "Gestion des règles de fidélité (admin)")
public class RuleController {

    private final CreateRuleHandler createRuleHandler;
    private final ActivateRuleHandler activateRuleHandler;
    private final RuleRepository ruleRepository;

    public RuleController(
            CreateRuleHandler createRuleHandler,
            ActivateRuleHandler activateRuleHandler,
            RuleRepository ruleRepository
    ) {
        this.createRuleHandler = createRuleHandler;
        this.activateRuleHandler = activateRuleHandler;
        this.ruleRepository = ruleRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<RuleResponse> createRule(@Valid @RequestBody CreateRuleRequest request) {
        return createRuleHandler.create(
                request.name(),
                request.description(),
                LoyaltyApiMapper.toTrigger(request.trigger()),
                LoyaltyApiMapper.toConditions(request.conditions()),
                LoyaltyApiMapper.toEffects(request.effects()),
                request.priority(),
                request.validFrom(),
                request.validUntil()
        ).map(RuleResponse::from);
    }

    @GetMapping
    public Flux<RuleResponse> listRules() {
        return TenantContextHolder.getTenantId()
                .flatMapMany(tenantId -> Flux.fromIterable(ruleRepository.findByTenant(tenantId)))
                .map(RuleResponse::from);
    }

    @GetMapping("/{ruleId}")
    public Mono<RuleResponse> getRule(@PathVariable UUID ruleId) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> Mono.justOrEmpty(ruleRepository.findById(ruleId)
                        .filter(r -> r.getTenantId().equals(tenantId))))
                .map(RuleResponse::from);
    }

    @PatchMapping("/{ruleId}/activate")
    public Mono<RuleResponse> activateRule(@PathVariable UUID ruleId) {
        return activateRuleHandler.activate(ruleId).map(RuleResponse::from);
    }
}
