package com.yowyob.loyaulty.program.application.rule.handler;

import com.yowyob.loyaulty.program.domain.rule.model.*;
import com.yowyob.loyaulty.program.domain.rule.port.in.ProcessEventUseCase;
import com.yowyob.loyaulty.program.domain.rule.port.out.CounterRepository;
import com.yowyob.loyaulty.program.domain.rule.port.out.RuleEventPublisherPort;
import com.yowyob.loyaulty.program.domain.rule.port.out.RuleRepository;
import com.yowyob.loyaulty.program.domain.rule.service.RuleEngine;
import com.yowyob.loyaulty.program.domain.wallet.port.out.IdempotencyPort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class ProcessEventHandler implements ProcessEventUseCase {

    private final RuleRepository ruleRepository;
    private final CounterRepository counterRepository;
    private final RuleEventPublisherPort eventPublisher;
    private final IdempotencyPort idempotencyPort;
    private final RuleEngine ruleEngine;

    public ProcessEventHandler(RuleRepository ruleRepository,
                                CounterRepository counterRepository,
                                RuleEventPublisherPort eventPublisher,
                                IdempotencyPort idempotencyPort,
                                RuleEngine ruleEngine) {
        this.ruleRepository    = ruleRepository;
        this.counterRepository = counterRepository;
        this.eventPublisher    = eventPublisher;
        this.idempotencyPort   = idempotencyPort;
        this.ruleEngine        = ruleEngine;
    }

    @Override
    public Mono<EvaluationResult> process(LoyaltyEvent event) {
        return idempotencyPort.isNew(event.idempotencyKey())
                .flatMap(isNew -> {
                    if (!isNew) {
                        return Mono.just(EvaluationResult.empty(event.eventId(), event.memberId()));
                    }
                    return idempotencyPort.markProcessing(event.idempotencyKey())
                            .then(doProcess(event));
                });
    }

    private Mono<EvaluationResult> doProcess(LoyaltyEvent event) {
        return ruleRepository.findActiveByTenant(event.tenantId())
                .collectList()
                .flatMap(rules -> {
                    // Règles dont le trigger correspond à cet event
                    List<UUID> matchedRuleIds = rules.stream()
                            .filter(r -> r.triggerMatches(event.eventType(), event.payload()))
                            .map(Rule::getId)
                            .toList();

                    if (matchedRuleIds.isEmpty()) {
                        return Mono.just(EvaluationResult.empty(event.eventId(), event.memberId()));
                    }

                    // Charger les compteurs courants, puis les incrémenter
                    return counterRepository.getAll(event.tenantId(), event.memberId(), matchedRuleIds)
                            .flatMap(currentCounters ->
                                    incrementMatchedCounters(event, matchedRuleIds, currentCounters)
                                            .flatMap(updatedCounters -> {
                                                // Réinitialiser les compteurs des règles dont les conditions viennent d'être remplies
                                                EvaluationContext ctx = new EvaluationContext(event, updatedCounters);
                                                EvaluationResult result = ruleEngine.evaluate(rules, ctx, updatedCounters);
                                                return resetCountersIfNeeded(event, result, rules)
                                                        .then(publishEffects(event, result))
                                                        .thenReturn(result);
                                            })
                            );
                });
    }

    private Mono<Map<UUID, Long>> incrementMatchedCounters(LoyaltyEvent event,
                                                            List<UUID> ruleIds,
                                                            Map<UUID, Long> current) {
        Map<UUID, Long> updated = new HashMap<>(current);
        List<Mono<Void>> increments = ruleIds.stream()
                .map(ruleId -> counterRepository.increment(event.tenantId(), event.memberId(), ruleId)
                        .doOnNext(newVal -> updated.put(ruleId, newVal))
                        .then())
                .toList();
        return Mono.when(increments).thenReturn(updated);
    }

    private Mono<Void> resetCountersIfNeeded(LoyaltyEvent event,
                                              EvaluationResult result,
                                              List<Rule> rules) {
        // Pour chaque effet RESET_COUNTER dans les effets appliqués
        List<Mono<Void>> resets = result.appliedEffects().stream()
                .flatMap(ae -> rules.stream()
                        .filter(r -> r.getId().equals(ae.ruleId()))
                        .flatMap(r -> r.getEffects().stream())
                        .filter(e -> e.type().name().equals("RESET_COUNTER"))
                        .map(e -> counterRepository.reset(event.tenantId(), event.memberId(), ae.ruleId()))
                )
                .toList();
        return resets.isEmpty() ? Mono.empty() : Mono.when(resets);
    }

    private Mono<Void> publishEffects(LoyaltyEvent event, EvaluationResult result) {
        if (!result.hasEffects()) return Mono.empty();
        return eventPublisher.publishEffects(event, result.appliedEffects());
    }
}
