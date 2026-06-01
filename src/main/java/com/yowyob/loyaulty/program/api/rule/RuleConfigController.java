package com.yowyob.loyaulty.program.api.rule;

import com.yowyob.loyaulty.program.api.rule.dto.request.CreateRuleRequest;
import com.yowyob.loyaulty.program.api.rule.dto.response.RuleResponse;
import com.yowyob.loyaulty.program.domain.rule.model.*;
import com.yowyob.loyaulty.program.domain.rule.model.enums.ConditionType;
import com.yowyob.loyaulty.program.domain.rule.model.enums.EffectType;
import com.yowyob.loyaulty.program.domain.rule.port.out.RuleRepository;
import com.yowyob.loyaulty.program.shared.multitenancy.TenantContextHolder;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/rules")
public class RuleConfigController {

    private final RuleRepository ruleRepository;

    public RuleConfigController(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @GetMapping
    public Flux<RuleResponse> listRules() {
        return TenantContextHolder.getTenantId()
                .flatMapMany(ruleRepository::findAllByTenant)
                .map(RuleResponse::from);
    }

    @GetMapping("/{id}")
    public Mono<RuleResponse> getRule(@PathVariable UUID id) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> ruleRepository.findById(id, tenantId))
                .map(RuleResponse::from);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<RuleResponse> createRule(@Valid @RequestBody CreateRuleRequest req) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> {
                    Trigger trigger = new Trigger(
                            req.trigger().eventType(),
                            req.trigger().filters() != null ? req.trigger().filters() : Map.of()
                    );
                    List<Condition> conditions = toConditions(req.conditions());
                    List<Effect>    effects    = toEffects(req.effects());

                    Rule rule = Rule.create(tenantId, req.name(), req.description(),
                            req.priority(), trigger, conditions, effects,
                            req.validFrom(), req.validUntil());

                    return ruleRepository.save(rule);
                })
                .map(RuleResponse::from);
    }

    @PostMapping("/{id}/activate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> activate(@PathVariable UUID id) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> ruleRepository.findById(id, tenantId))
                .flatMap(rule -> { rule.activate(); return ruleRepository.save(rule); })
                .then();
    }

    @PostMapping("/{id}/suspend")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> suspend(@PathVariable UUID id) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> ruleRepository.findById(id, tenantId))
                .flatMap(rule -> { rule.suspend(); return ruleRepository.save(rule); })
                .then();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable UUID id) {
        return TenantContextHolder.getTenantId()
                .flatMap(tenantId -> ruleRepository.delete(id, tenantId));
    }

    // ── Conversion DTOs → domaine ─────────────────────────────────────────

    private List<Condition> toConditions(List<CreateRuleRequest.ConditionDto> dtos) {
        if (dtos == null) return List.of();
        return dtos.stream()
                .map(d -> new Condition(
                        ConditionType.valueOf(d.type().toUpperCase()),
                        d.operator(),
                        BigDecimal.valueOf(d.value()),
                        d.window()
                ))
                .toList();
    }

    private List<Effect> toEffects(List<CreateRuleRequest.EffectDto> dtos) {
        if (dtos == null) return List.of();
        return dtos.stream()
                .map(d -> new Effect(
                        EffectType.valueOf(d.type().toUpperCase()),
                        d.params() != null ? d.params() : Map.of()
                ))
                .toList();
    }
}
